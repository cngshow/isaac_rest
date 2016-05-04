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

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
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
import gov.vha.isaac.ochre.model.configuration.LanguageCoordinates;
import gov.vha.isaac.ochre.model.configuration.StampCoordinates;
import gov.vha.isaac.ochre.model.configuration.TaxonomyCoordinates;
import gov.vha.isaac.ochre.model.coordinate.LanguageCoordinateImpl;
import gov.vha.isaac.ochre.model.coordinate.LogicCoordinateImpl;
import gov.vha.isaac.ochre.model.coordinate.StampCoordinateImpl;
import gov.vha.isaac.ochre.model.coordinate.StampPositionImpl;
import gov.vha.isaac.ochre.model.coordinate.TaxonomyCoordinateImpl;

/**
 * 
 * {@link CoordinatesToken}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class CoordinatesToken
{
	private static final byte tokenVersion = 1;
	private static final int hashRounds = 128;
	private static final int hashLength = 64;
	private static final int encodedHashLength = (int)Math.ceil(hashLength / 8f / 3f) * 4;  //http://stackoverflow.com/a/4715480
	
	private static final Logger log = LoggerFactory.getLogger(CoordinatesToken.class);
	private static transient byte[] secret_;
	
	private final long stampTime;
	private final int stampPath;
	private final byte stampPrecedence;
	private final int[] stampModules;
	private final byte[] stampStates;
	
	private final int langCoord;
	private final int[] langDialects;
	private final int[] langTypePrefs;
	
	private final byte taxonomyType;

	private final int logicStatedAssemblage;
	private final int logicInferredAssemblage;
	private final int logicDescLogicProfile;
	private final int logicClassifier;

	//private final TaxonomyCoordinate taxonomyCoordinate;
	
	public static CoordinatesToken get(
			long stampTime,
			int stampPath,
			byte stampPrecedence,
			int[] stampModules,
			byte[] stampStates,
			int langCoord,
			int[] langDialects,
			int[] langTypePrefs,
			byte taxonomyType,
			int logicStatedAssemblage,
			int logicInferredAssemblage,
			int logicDescLogicProfile,
			int logicClassifier) throws Exception {
		return CoordinatesTokens.get(
				new CoordinatesToken(
						stampTime,
						stampPath,
						stampPrecedence,
						stampModules,
						stampStates,
						langCoord,
						langDialects,
						langTypePrefs,
						taxonomyType,
						logicStatedAssemblage,
						logicInferredAssemblage,
						logicDescLogicProfile,
						logicClassifier).serialize());
	}
	public static CoordinatesToken get() {
		try {
			return CoordinatesTokens.getDefaultCoordinatesTokenObject();
		} catch (Exception e) {
			// This should never fail, as token is created from existing objects
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	public static CoordinatesToken get(TaxonomyCoordinate tax) {
		try {
			return CoordinatesTokens.get(new CoordinatesToken(tax).serialize());
		} catch (Exception e) {
			// This should never fail, as token is created from existing objects
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	public static CoordinatesToken get(
			StampCoordinate stamp,
			LanguageCoordinate lang,
			LogicCoordinate logic,
			PremiseType taxType) {
		try {
			return CoordinatesTokens.get(new CoordinatesToken(stamp, lang, logic, taxType).serialize());
		} catch (Exception e) {
			// This should never fail, as token is created from existing objects
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	public static CoordinatesToken get(String tokenStr) throws Exception {
		return CoordinatesTokens.get(tokenStr);
	}

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
	 * @param langTypePrefs
	 * @param taxonomyType
	 * @param logicStatedAssemblage
	 * @param logicInferredAssemblage
	 * @param logicDescLogicProfile
	 * @param logicClassifier
	 */
	public CoordinatesToken(
			long stampTime,
			int stampPath,
			byte stampPrecedence,
			int[] stampModules,
			byte[] stampStates,
			int langCoord,
			int[] langDialects,
			int[] langTypePrefs,
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
		this.langTypePrefs = langTypePrefs;
		this.taxonomyType = taxonomyType;
		this.logicStatedAssemblage = logicStatedAssemblage;
		this.logicInferredAssemblage = logicInferredAssemblage;
		this.logicDescLogicProfile = logicDescLogicProfile;
		this.logicClassifier = logicClassifier;
	}
	
	CoordinatesToken() {
		this(TaxonomyCoordinates.getStatedTaxonomyCoordinate(StampCoordinates.getDevelopmentLatest().makeAnalog(State.ACTIVE),LanguageCoordinates.getUsEnglishLanguageFullySpecifiedNameCoordinate()));
	}

	// This constructor handles everything with the TaxonomyCoordinate with no overlap
	private CoordinatesToken(TaxonomyCoordinate tax) {
		this(tax.getStampCoordinate(), tax.getLanguageCoordinate(), tax.getLogicCoordinate(), tax.getTaxonomyType());
	}
	private CoordinatesToken(StampCoordinate stamp, LanguageCoordinate lang, LogicCoordinate logic, PremiseType taxType)
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
		langTypePrefs = lang.getDescriptionTypePreferenceList();
		
		taxonomyType = (byte)taxType.ordinal();
		
		logicStatedAssemblage = logic.getStatedAssemblageSequence();
		logicInferredAssemblage = logic.getInferredAssemblageSequence();
		logicDescLogicProfile = logic.getDescriptionLogicProfileSequence();
		logicClassifier = logic.getClassifierSequence();
	}
	
	CoordinatesToken(String encodedData) throws Exception
	{
		long time = System.currentTimeMillis();
		String readHash = encodedData.substring(0, encodedHashLength);
		String calculatedHash = PasswordHasher.hash(encodedData.substring(encodedHashLength, encodedData.length()), getSecret(), hashRounds, hashLength);
		
		if (!readHash.equals(calculatedHash))
		{
			throw new RuntimeException("Invalid token!");
		}
		
		byte[] readBytes = Base64.getDecoder().decode(encodedData.substring(encodedHashLength, encodedData.length()));
		ByteArrayDataBuffer buffer = new ByteArrayDataBuffer(readBytes);
		byte version = buffer.getByte();
		if (version != tokenVersion)
		{
			throw new Exception("Expected token version " + tokenVersion + " but read " + version);
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
		langTypePrefs = new int[buffer.getInt()];
		for (int i = 0; i < langTypePrefs.length; i++)
		{
			langTypePrefs[i] = buffer.getInt();
		}
		
		taxonomyType = buffer.getByte();
		
		logicStatedAssemblage = buffer.getInt();
		logicInferredAssemblage = buffer.getInt();
		logicDescLogicProfile = buffer.getInt();
		logicClassifier = buffer.getInt();
		
		log.debug("token decode time " + (System.currentTimeMillis() - time) + "ms");
	}

	public TaxonomyCoordinate getTaxonomyCoordinate() {
		return new TaxonomyCoordinateImpl(
				getTaxonomyType(),
				new StampCoordinateImpl(
						getStampPrecedence(),
						new StampPositionImpl(stampTime, stampPath),
						getStampModules(),
						getStampStates()),
				new LanguageCoordinateImpl(langCoord, langDialects, langTypePrefs),
				new LogicCoordinateImpl(
						logicStatedAssemblage,
						logicInferredAssemblage,
						logicDescLogicProfile,
						logicClassifier));
	}
	public StampCoordinate getStampCoordinate() {
		return new StampCoordinateImpl(
				getStampPrecedence(),
				new StampPositionImpl(stampTime, stampPath),
				getStampModules(),
				getStampStates());
	}
	public LanguageCoordinate getLanguageCoordinate() {
		return new LanguageCoordinateImpl(langCoord, langDialects, langTypePrefs);
	}
	public LogicCoordinate getLogicCoordinate() {
		return new LogicCoordinateImpl(
				logicStatedAssemblage,
				logicInferredAssemblage,
				logicDescLogicProfile,
				logicClassifier);
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
	public ConceptSequenceSet getLangDialects() {
		return ConceptSequenceSet.of(langDialects);
	}

	/**
	 * @return the langTypePrefs
	 */
	public ConceptSequenceSet getLangTypePrefs() {
		return ConceptSequenceSet.of(langTypePrefs);
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

	public String serialize()
	{
		try
		{
			String data = Base64.getEncoder().encodeToString(getBytesToWrite());
			return PasswordHasher.hash(data, getSecret(), hashRounds, hashLength) + data;
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
		buffer.putInt(langTypePrefs.length);
		for (int x: langTypePrefs)
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
	
	private byte[] getSecret()
	{
		if (secret_ == null)
		{
			try
			{
				synchronized (CoordinatesToken.class)
				{
					if (secret_ == null)
					{
						byte[] temp = new byte[20];
						
						SecureRandom.getInstanceStrong().nextBytes(temp);
						secret_ = temp;
					}
				}
			}
			catch (NoSuchAlgorithmException e)
			{
				throw new RuntimeException(e);
			}
		}
		return secret_;
	}
 
	public static void main(String[] args) throws Exception
	{
		/*
		 * 	long stampTime,
			int stampPath,
			byte stampPrecedence,
			int[] stampModules,
			byte[] stampStates,
			int langCoord,
			int[] langDialects,
			int[] langTypePrefs,
			byte taxonomyType,
			int logicStatedAssemblage,
			int logicInferredAssemblage,
			int logicDescLogicProfile,
			int logicClassifier)
		 */
		CoordinatesToken t = new CoordinatesToken(
				Long.MAX_VALUE, // stampTime
				4, // stampPath
				(byte)5, // stampPrecedence
				new int[] {4,5,6}, // stampModules
				new byte[] {2}, // stampStates
				456, // langCoord
				new int[] {123}, // langDialects
				new int[] {3,4,5,2,1}, // langTypePrefs
				(byte)5, // taxonomyType
				1, // logicStatedAssemblage
				2, // logicInferredAssemblage
				3, // logicDescLogicProfile
				4); // logicClassifier
		
		
		String token = t.serialize();
		System.out.println(token);
		new CoordinatesToken(token);
		
		CoordinatesToken r = new CoordinatesToken(
				Long.MAX_VALUE, // stampTime
				4, // stampPath
				(byte)5, // stampPrecedence
				new int[] {4,5,6}, // stampModules
				new byte[] {2}, // stampStates
				456, // langCoord
				new int[] {123}, // langDialects
				new int[] {3,4,5,2,1}, // langTypePrefs
				(byte)5, // taxonomyType
				1, // logicStatedAssemblage
				2, // logicInferredAssemblage
				3, // logicDescLogicProfile
				4); // logicClassifier
		
		String token1 = r.serialize();
		System.out.println(token1);
		new CoordinatesToken(token1);
	}
}
