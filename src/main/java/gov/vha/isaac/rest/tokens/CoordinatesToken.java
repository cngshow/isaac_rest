/**
 * Copyright Notice
 *
 * This is a work of the U.S. Government and is not subject to copyright
 * protection in the United States. Foreign copyrights may apply.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.vha.isaac.rest.tokens;

import java.util.Arrays;
import java.util.Base64;
import java.util.EnumSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.vha.isaac.ochre.api.State;
import gov.vha.isaac.ochre.api.collections.ConceptSequenceSet;
import gov.vha.isaac.ochre.api.coordinate.LanguageCoordinate;
import gov.vha.isaac.ochre.api.coordinate.LogicCoordinate;
import gov.vha.isaac.ochre.api.coordinate.PremiseType;
import gov.vha.isaac.ochre.api.coordinate.StampCoordinate;
import gov.vha.isaac.ochre.api.coordinate.StampPrecedence;
import gov.vha.isaac.ochre.api.coordinate.TaxonomyCoordinate;
import gov.vha.isaac.ochre.api.externalizable.ByteArrayDataBuffer;
import gov.vha.isaac.ochre.api.util.PasswordHasher;
import gov.vha.isaac.ochre.model.coordinate.LanguageCoordinateImpl;
import gov.vha.isaac.ochre.model.coordinate.LogicCoordinateImpl;
import gov.vha.isaac.ochre.model.coordinate.StampCoordinateImpl;
import gov.vha.isaac.ochre.model.coordinate.StampPositionImpl;
import gov.vha.isaac.ochre.model.coordinate.TaxonomyCoordinateImpl;
import gov.vha.isaac.rest.ApplicationConfig;
import gov.vha.isaac.rest.api.exceptions.RestException;

/**
 * 
 * {@link CoordinatesToken}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class CoordinatesToken
{
	private static final transient Logger log = LoggerFactory.getLogger(CoordinatesToken.class);

	private static final byte tokenVersion = 1;
	private static final int hashRounds = 128;
	private static final int hashLength = 64;
	private static final int encodedHashLength = (int)Math.ceil(hashLength / 8f / 3f) * 4;  //http://stackoverflow.com/a/4715480

	private final long stampTime;
	private final int stampPath;
	private final byte stampPrecedence;
	private final int[] stampModules;
	private final byte[] stampStates;

	private final int langCoord;
	private final int[] langDialects;
	private final int[] langDescTypePrefs;

	private final byte taxonomyType;

	private final int logicStatedAssemblage;
	private final int logicInferredAssemblage;
	private final int logicDescLogicProfile;
	private final int logicClassifier;

	private transient TaxonomyCoordinate taxonomyCoordinate = null;
	private transient StampCoordinate stampCoordinate = null;
	private transient LanguageCoordinate languageCoordinate = null;
	private transient LogicCoordinate logicCoordinate = null;

	private final transient String serialization;

	private static <E extends Enum<E>> E getFromOrdinal(Class<E> clazz, int ordinal) {
		for (E value : clazz.getEnumConstants()) {
			if (value.ordinal() == ordinal) {
				return value;
			}
		}

		throw new IllegalArgumentException("invalid " + clazz.getName() + " ordinal value " + ordinal + ". Must be one of " + clazz.getEnumConstants());
	}

	/**
	 * @param stampTime
	 * @param stampPath
	 * @param stampPrecedence
	 * @param stampModules
	 * @param stampStates
	 * @param langCoord
	 * @param langDialects
	 * @param langDescTypePrefs
	 * @param taxonomyType
	 * @param logicStatedAssemblage
	 * @param logicInferredAssemblage
	 * @param logicDescLogicProfile
	 * @param logicClassifier
	 */
	CoordinatesToken(
			long stampTime,
			int stampPath,
			byte stampPrecedence,
			int[] stampModules,
			byte[] stampStates,
			int langCoord,
			int[] langDialects,
			int[] langDescTypePrefs,
			byte taxonomyType,
			int logicStatedAssemblage,
			int logicInferredAssemblage,
			int logicDescLogicProfile,
			int logicClassifier) {
		super();
		this.stampTime = stampTime;
		this.stampPath = stampPath;
		this.stampPrecedence = stampPrecedence;
		this.stampModules = stampModules;
		this.stampStates = stampStates;
		this.langCoord = langCoord;
		this.langDialects = langDialects;
		this.langDescTypePrefs = langDescTypePrefs;
		this.taxonomyType = taxonomyType;
		this.logicStatedAssemblage = logicStatedAssemblage;
		this.logicInferredAssemblage = logicInferredAssemblage;
		this.logicDescLogicProfile = logicDescLogicProfile;
		this.logicClassifier = logicClassifier;

		serialization = serialize(this);
	}
	
	CoordinatesToken(StampCoordinate stamp, LanguageCoordinate lang, LogicCoordinate logic, PremiseType taxType)
	{
		stampTime = stamp.getStampPosition().getTime();
		stampPath = stamp.getStampPosition().getStampPathSequence();
		stampPrecedence = (byte)stamp.getStampPrecedence().ordinal();
		stampModules = new int[stamp.getModuleSequences().size()];
		int pos = 0;
		for (int i : stamp.getModuleSequences().asArray())
		{
			stampModules[pos++] = i; 
		}
		stampStates = new byte[stamp.getAllowedStates().size()];
		pos = 0;
		for (State s : stamp.getAllowedStates())
		{
			stampStates[pos++] = (byte)s.ordinal(); 
		}

		langCoord = lang.getLanguageConceptSequence();
		langDialects = lang.getDialectAssemblagePreferenceList();
		langDescTypePrefs = lang.getDescriptionTypePreferenceList();

		taxonomyType = (byte)taxType.ordinal();

		logicStatedAssemblage = logic.getStatedAssemblageSequence();
		logicInferredAssemblage = logic.getInferredAssemblageSequence();
		logicDescLogicProfile = logic.getDescriptionLogicProfileSequence();
		logicClassifier = logic.getClassifierSequence();

		serialization = serialize(this);
	}

	CoordinatesToken(String encodedData) throws RestException
	{
		try
		{
			serialization = encodedData;

			long time = System.currentTimeMillis();
			String readHash = serialization.substring(0, encodedHashLength);
			String calculatedHash = PasswordHasher.hash(serialization.substring(encodedHashLength, serialization.length()), ApplicationConfig.getSecret(), hashRounds, hashLength);

			if (!readHash.equals(calculatedHash))
			{
				throw new RestException("Invalid token!");
			}

			byte[] readBytes = Base64.getUrlDecoder().decode(serialization.substring(encodedHashLength, serialization.length()));
			ByteArrayDataBuffer buffer = new ByteArrayDataBuffer(readBytes);
			byte version = buffer.getByte();
			if (version != tokenVersion)
			{
				log.warn("Expected token version " + tokenVersion + " but read " + version);
				throw new RestException("Invalid token - old token?");
			}
			stampTime = buffer.getLong();
			stampPath = buffer.getInt();
			stampPrecedence = buffer.getByte();
			stampModules = new int[buffer.getInt()];
			for (int i = 0; i < stampModules.length; i++)
			{
				stampModules[i] = buffer.getInt();
			}
			stampStates = new byte[buffer.getInt()];
			for (int i = 0; i < stampStates.length; i++)
			{
				stampStates[i] = buffer.getByte();
			}

			langCoord = buffer.getInt();
			langDialects = new int[buffer.getInt()];
			for (int i = 0; i < langDialects.length; i++)
			{
				langDialects[i] = buffer.getInt();
			}
			langDescTypePrefs = new int[buffer.getInt()];
			for (int i = 0; i < langDescTypePrefs.length; i++)
			{
				langDescTypePrefs[i] = buffer.getInt();
			}

			taxonomyType = buffer.getByte();

			logicStatedAssemblage = buffer.getInt();
			logicInferredAssemblage = buffer.getInt();
			logicDescLogicProfile = buffer.getInt();
			logicClassifier = buffer.getInt();

			log.debug("token decode time " + (System.currentTimeMillis() - time) + "ms");
		}
		catch (RestException e)
		{
			throw e;
		}
		catch (Exception e)
		{
			log.error("Unexpected", e);
			throw new RuntimeException(e);
		}
	}

	public TaxonomyCoordinate getTaxonomyCoordinate() {
		if (taxonomyCoordinate == null) {
			taxonomyCoordinate = new TaxonomyCoordinateImpl(
					getTaxonomyType(),
					getStampCoordinate(),
					getLanguageCoordinate(),
					getLogicCoordinate());
		}

		return taxonomyCoordinate;
	}
	public StampCoordinate getStampCoordinate() {
		if (stampCoordinate == null) {
			stampCoordinate = new StampCoordinateImpl(
					getStampPrecedence(),
					new StampPositionImpl(stampTime, stampPath),
					getStampModules(),
					getStampStates());
		}

		return stampCoordinate;
	}
	public LanguageCoordinate getLanguageCoordinate() {
		if (languageCoordinate == null) {
				languageCoordinate = new LanguageCoordinateImpl(langCoord, langDialects, langDescTypePrefs);
		}

		return languageCoordinate;
	}
	public LogicCoordinate getLogicCoordinate() {
		if (logicCoordinate == null) {
				logicCoordinate = new LogicCoordinateImpl(
						logicStatedAssemblage,
						logicInferredAssemblage,
						logicDescLogicProfile,
						logicClassifier);
		}

		return logicCoordinate;
	}

	/**
	 * @return the stampTime
	 */
	public long getStampTime() {
		return stampTime;
	}

	/**
	 * @return the stampPath
	 */
	public int getStampPath() {
		return stampPath;
	}

	/**
	 * @return the stampPrecedence
	 */
	public StampPrecedence getStampPrecedence() {
		return getFromOrdinal(StampPrecedence.class, this.stampPrecedence);
	}

	/**
	 * @return the stampModules
	 */
	public ConceptSequenceSet getStampModules() {
		return ConceptSequenceSet.of(stampModules);
	}

	/**
	 * @return the stampStates
	 */
	public EnumSet<State> getStampStates() {
		EnumSet<State> allowedStates = EnumSet.allOf(State.class);
		allowedStates.clear();
		for (byte state : stampStates) {
			allowedStates.add(getFromOrdinal(State.class, state));
		}

		return allowedStates;
	}

	/**
	 * @return the langCoord
	 */
	public int getLangCoord() {
		return langCoord;
	}

	/**
	 * @return the langDialects
	 */
	public int[] getLangDialects() {
		return Arrays.copyOf(langDialects, langDialects.length);
	}

	/**
	 * @return the langDescTypePrefs
	 */
	public int[] getLangDescTypePrefs() {
		return Arrays.copyOf(langDescTypePrefs, langDescTypePrefs.length);
	}

	/**
	 * @return the taxonomyType
	 */
	public PremiseType getTaxonomyType() {
		return getFromOrdinal(PremiseType.class, taxonomyType);
	}

	/**
	 * @return the logicStatedAssemblage
	 */
	public int getLogicStatedAssemblage() {
		return logicStatedAssemblage;
	}

	/**
	 * @return the logicInferredAssemblage
	 */
	public int getLogicInferredAssemblage() {
		return logicInferredAssemblage;
	}

	/**
	 * @return the logicDescLogicProfile
	 */
	public int getLogicDescLogicProfile() {
		return logicDescLogicProfile;
	}

	/**
	 * @return the logicClassifier
	 */
	public int getLogicClassifier() {
		return logicClassifier;
	}

	public String getSerialized()
	{
		return serialization;
	}

	private static String serialize(CoordinatesToken token) {
		try
		{
			String data = Base64.getUrlEncoder().encodeToString(token.getBytesToWrite());
			return PasswordHasher.hash(data, ApplicationConfig.getSecret(), hashRounds, hashLength) + data;
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	private byte[] getBytesToWrite()
	{
		ByteArrayDataBuffer buffer = new ByteArrayDataBuffer();
		buffer.putByte(tokenVersion);
		buffer.putLong(stampTime);
		buffer.putInt(stampPath);
		buffer.putByte(stampPrecedence);
		buffer.putInt(stampModules.length);
		for (int x: stampModules)
		{
			buffer.putInt(x);
		}
		buffer.putInt(stampStates.length);
		for (byte x : stampStates)
		{
			buffer.putByte(x);
		}

		buffer.putInt(langCoord);
		buffer.putInt(langDialects.length);
		for (int x: langDialects)
		{
			buffer.putInt(x);
		}
		buffer.putInt(langDescTypePrefs.length);
		for (int x: langDescTypePrefs)
		{
			buffer.putInt(x);
		}

		buffer.putByte(taxonomyType);

		buffer.putInt(logicStatedAssemblage);
		buffer.putInt(logicInferredAssemblage);
		buffer.putInt(logicDescLogicProfile);
		buffer.putInt(logicClassifier);

		buffer.trimToSize();
		return buffer.getData();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "CoordinatesToken [getStampTime()=" + getStampTime() + ", getStampPath()=" + getStampPath()
				+ ", getStampPrecedence()=" + getStampPrecedence() + ", getStampModules()=" + getStampModules()
				+ ", getStampStates()=" + getStampStates() + ", getLangCoord()=" + getLangCoord()
				+ ", getLangDialects()=" + getLangDialects() + ", getLangDescTypePrefs()=" + getLangDescTypePrefs()
				+ ", getTaxonomyType()=" + getTaxonomyType() + ", getLogicStatedAssemblage()="
				+ getLogicStatedAssemblage() + ", getLogicInferredAssemblage()=" + getLogicInferredAssemblage()
				+ ", getLogicDescLogicProfile()=" + getLogicDescLogicProfile() + ", getLogicClassifier()="
				+ getLogicClassifier() + ", getSerialized()=" + getSerialized() + "]";
	}

//	public static void main(String[] args) throws Exception
//	{
//		/*
//		 * 	long stampTime,
//			int stampPath,
//			byte stampPrecedence,
//			int[] stampModules,
//			byte[] stampStates,
//			int langCoord,
//			int[] langDialects,
//			int[] langDescTypePrefs,
//			byte taxonomyType,
//			int logicStatedAssemblage,
//			int logicInferredAssemblage,
//			int logicDescLogicProfile,
//			int logicClassifier)
//		 */
//		CoordinatesToken t = new CoordinatesToken(
//				Long.MAX_VALUE, // stampTime
//				4, // stampPath
//				(byte)5, // stampPrecedence
//				new int[] {4,5,6}, // stampModules
//				new byte[] {2}, // stampStates
//				456, // langCoord
//				new int[] {123}, // langDialects
//				new int[] {3,4,5,2,1}, // langDescTypePrefs
//				(byte)5, // taxonomyType
//				1, // logicStatedAssemblage
//				2, // logicInferredAssemblage
//				3, // logicDescLogicProfile
//				4); // logicClassifier
//
//
//		String token = t.getSerialized();
//		System.out.println(token);
//		new CoordinatesToken(token);
//
//		CoordinatesToken r = new CoordinatesToken(
//				Long.MAX_VALUE, // stampTime
//				4, // stampPath
//				(byte)5, // stampPrecedence
//				new int[] {4,5,6}, // stampModules
//				new byte[] {2}, // stampStates
//				456, // langCoord
//				new int[] {123}, // langDialects
//				new int[] {3,4,5,2,1}, // langDescTypePrefs
//				(byte)5, // taxonomyType
//				1, // logicStatedAssemblage
//				2, // logicInferredAssemblage
//				3, // logicDescLogicProfile
//				4); // logicClassifier
//
//		String token1 = r.getSerialized();
//		System.out.println(token1);
//		new CoordinatesToken(token1);
//	}
}
