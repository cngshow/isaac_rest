package gov.vha.isaac.rest.session;

import static gov.vha.isaac.ochre.api.logic.LogicalExpressionBuilder.And;
import static gov.vha.isaac.ochre.api.logic.LogicalExpressionBuilder.ConceptAssertion;
import static gov.vha.isaac.ochre.api.logic.LogicalExpressionBuilder.NecessarySet;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;

import org.glassfish.hk2.api.Rank;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.vha.isaac.MetaData;
import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.LookupService;
import gov.vha.isaac.ochre.api.User;
import gov.vha.isaac.ochre.api.UserService;
import gov.vha.isaac.ochre.api.chronicle.ObjectChronology;
import gov.vha.isaac.ochre.api.commit.ChangeCheckerMode;
import gov.vha.isaac.ochre.api.commit.CommitRecord;
import gov.vha.isaac.ochre.api.component.concept.ConceptBuilder;
import gov.vha.isaac.ochre.api.component.concept.ConceptBuilderService;
import gov.vha.isaac.ochre.api.component.concept.ConceptChronology;
import gov.vha.isaac.ochre.api.component.concept.ConceptSpecification;
import gov.vha.isaac.ochre.api.component.concept.ConceptVersion;
import gov.vha.isaac.ochre.api.coordinate.EditCoordinate;
import gov.vha.isaac.ochre.api.coordinate.LanguageCoordinate;
import gov.vha.isaac.ochre.api.coordinate.LogicCoordinate;
import gov.vha.isaac.ochre.api.identity.StampedVersion;
import gov.vha.isaac.ochre.api.logic.LogicalExpression;
import gov.vha.isaac.ochre.api.logic.LogicalExpressionBuilder;
import gov.vha.isaac.ochre.api.logic.LogicalExpressionBuilderService;
import gov.vha.isaac.ochre.api.util.UuidT5Generator;
import gov.vha.isaac.ochre.model.configuration.EditCoordinates;
import gov.vha.isaac.ochre.model.configuration.LanguageCoordinates;
import gov.vha.isaac.ochre.model.configuration.LogicCoordinates;

/**
 * 
 * {@link UserProvider}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */

@Service
@Rank(value = 10)
@Singleton
public class UserProvider implements UserService {
	private static final Logger log = LoggerFactory.getLogger(UserProvider.class);
	
	private static final long userMaxAge = 1000l * 60l * 60l;  //Any users in our list that haven't had their roles checked in an hour, should be purged.
	
	private static final ConcurrentHashMap<Integer, User> USERS = new ConcurrentHashMap<>();  //Store of all currently active users
	
	@PostConstruct
	protected void onStart()
	{
		Get.workExecutors().getScheduledThreadPoolExecutor().scheduleAtFixedRate(() -> {expireOldUsers();}, 15, 15, TimeUnit.MINUTES);
	}
	
	private void expireOldUsers()
	{
		try
		{
			log.info("Expiring out-of-date users - size before: " + USERS.size());
			Iterator<Entry<Integer, User>> x = USERS.entrySet().iterator();
			while (x.hasNext())
			{
				if ((System.currentTimeMillis() -  x.next().getValue().rolesCheckedAt()) > userMaxAge)
				{
					x.remove();
				}
			}
			log.info("Finished expiring unused tokens - size after: " + USERS.size());
		}
		catch (Exception e)
		{
			log.error("Unexpected error expiring unused tokens", e);
		}
	}

	@Override
	public void addUser(User user) {
		
		log.debug("Adding user to cache: " + user.toString());
		
		int authorSequence;
		if (!Get.identifierService().hasUuid(user.getId()))
		{
			//This creates the user concept in our DB
			authorSequence = getAuthorSequence(user.getName());
		}
		else
		{
			authorSequence = Get.identifierService().getConceptSequenceForUuids(user.getId());
		}
		
		USERS.put(authorSequence, user);
		
		return;
	}

	@Override
	public Optional<User> get(UUID userId) {
		Optional<User> temp = Optional.ofNullable(USERS.get(Get.identifierService().getConceptSequenceForUuids(userId)));
		log.debug("User Cache " + (temp.isPresent() ? "hit" : "miss") + " for user " + userId);
		return temp;
	}

	@Override
	public Optional<User> get(int userId) {
		Optional<User> temp = Optional.ofNullable(USERS.get(Get.identifierService().getConceptSequence(userId)));
		log.debug("User Cache " + (temp.isPresent() ? "hit" : "miss") + " for user " + userId);
		return temp;
	}
	
	public static UUID getUuidFromUserName(String fsn) 
	{
		return UuidT5Generator.get(MetaData.USER.getPrimordialUuid(), fsn);
	}
	
	/**
	 * Get the concept sequence that corresponds to a given editors user name - constructing the concept if necessary.
	 * @param name
	 * @return
	 */
	public static int getAuthorSequence(String ssoProvidedUserName) 
	{
		UUID userUUID = getUuidFromUserName(ssoProvidedUserName);

		// If the SSO UUID already persisted
		if (Get.identifierService().hasUuid(userUUID)) 
		{
			// Set authorSequence to value corresponding to SSO UUID
			return Get.identifierService().getConceptSequenceForUuids(userUUID);
		}
		else
		{
			log.debug("Creating new concept for user '" + ssoProvidedUserName + "'");
			EditCoordinate adminEditCoordinate = EditCoordinates.getDefaultUserMetadata();
			LanguageCoordinate languageCoordinate = LanguageCoordinates.getUsEnglishLanguageFullySpecifiedNameCoordinate();
			LogicCoordinate logicCoordinate = LogicCoordinates.getStandardElProfile();
			
			ConceptSpecification defaultDescriptionsLanguageConceptSpec = Get.conceptSpecification(languageCoordinate.getLanguageConceptSequence());
			ConceptSpecification defaultDescriptionDialectConceptSpec = Get.conceptSpecification(languageCoordinate.getDialectAssemblagePreferenceList()[0]);

			ConceptBuilderService conceptBuilderService = LookupService.getService(ConceptBuilderService.class);
			conceptBuilderService.setDefaultLanguageForDescriptions(defaultDescriptionsLanguageConceptSpec);
			conceptBuilderService.setDefaultDialectAssemblageForDescriptions(defaultDescriptionDialectConceptSpec);
			conceptBuilderService.setDefaultLogicCoordinate(logicCoordinate);

			LogicalExpressionBuilder defBuilder = LookupService.getService(LogicalExpressionBuilderService.class).getLogicalExpressionBuilder();

			NecessarySet(And(ConceptAssertion(MetaData.USER, defBuilder)));

			LogicalExpression parentDef = defBuilder.build();

			ConceptBuilder builder = conceptBuilderService.getDefaultConceptBuilder(ssoProvidedUserName, null, parentDef);

			// Set new author concept UUID to SSO UUID
			builder.setPrimordialUuid(userUUID);

			if (languageCoordinate.getDialectAssemblagePreferenceList() != null && languageCoordinate.getDialectAssemblagePreferenceList().length > 0) 
			{
				for (int i : languageCoordinate.getDialectAssemblagePreferenceList()) 
				{
					builder.getFullySpecifiedDescriptionBuilder().addPreferredInDialectAssemblage(Get.conceptSpecification(i));
				}
			}

			List<ObjectChronology<? extends StampedVersion>> createdObjects = new ArrayList<>();
			ConceptChronology<? extends ConceptVersion<?>> newCon = builder.build(adminEditCoordinate, ChangeCheckerMode.ACTIVE, createdObjects).getNoThrow();

			try
			{
				Get.commitService().addUncommitted(newCon).get();

				@SuppressWarnings("deprecation")
				Optional<CommitRecord> commitRecord = Get.commitService().commit(
						"creating new concept: NID=" + newCon.getNid() + ", FSN=" + ssoProvidedUserName).get();
				return newCon.getConceptSequence();
			}
			catch (InterruptedException | ExecutionException  e)
			{
				throw new RuntimeException("Creation of user concept failed. Caught " + e.getClass().getSimpleName() + " " + e.getLocalizedMessage());
			}
		}
	}
}
