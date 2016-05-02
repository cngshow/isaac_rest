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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import gov.vha.isaac.ochre.api.State;
import gov.vha.isaac.ochre.api.coordinate.LanguageCoordinate;
import gov.vha.isaac.ochre.api.coordinate.LogicCoordinate;
import gov.vha.isaac.ochre.api.coordinate.PremiseType;
import gov.vha.isaac.ochre.api.coordinate.StampCoordinate;
import gov.vha.isaac.ochre.api.coordinate.TaxonomyCoordinate;
import gov.vha.isaac.ochre.api.externalizable.ByteArrayDataBuffer;
import gov.vha.isaac.ochre.api.util.PasswordHasher;

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
	
	private long stampTime;
	private int stampPath;
	private byte stampPrecedence;
	private int[] stampModules;
	private byte[] stampStates;
	
	private int langCoord;
	private int[] langDialects;
	private int[] langTypePrefs;
	
	private byte taxonomyType;

	private int logicStatedAssemblage;
	private int logicInferredAssemblage;
	private int logicDescLogicProfile;
	private int logicClassifier;
	
	public CoordinatesToken()
	{
		
	}
	
	// This constructor handles everything with the TaxonomyCoordinate with no overlap
	public CoordinatesToken(TaxonomyCoordinate tax) {
		this(tax.getStampCoordinate(), tax.getLanguageCoordinate(), tax.getLogicCoordinate(), tax.getTaxonomyType());
	}
	// This constructor passes overlapping/redundant data
//	public CoordinatesToken(StampCoordinate stamp, LanguageCoordinate lang, LogicCoordinate logic, TaxonomyCoordinate tax) {
//		this(tax.getStampCoordinate(), tax.getLanguageCoordinate(), tax.getLogicCoordinate(), tax.getTaxonomyType());
//	}
	// This constructor handles everything with constituents of the TaxonomyCoordinate with no overlap
	public CoordinatesToken(StampCoordinate stamp, LanguageCoordinate lang, LogicCoordinate logic, PremiseType taxType)
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
	
	public CoordinatesToken(String encodedData) throws Exception
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
		CoordinatesToken t = new CoordinatesToken();
		t.langCoord = 456;
		t.langDialects = new int[] {123};
		t.langTypePrefs = new int[] {3,4,5,2,1};
		t.stampModules = new int[] {4,5,6};
		t.stampPath = 4;
		t.stampPrecedence = 5;
		t.stampStates = new byte[] {2};
		t.stampTime = Long.MAX_VALUE;
		t.taxonomyType = 5;
		
		
		String token = t.serialize();
		System.out.println(token);
		new CoordinatesToken(token);
		
		CoordinatesToken r = new CoordinatesToken();
		r.langCoord = 456;
		r.langDialects = new int[] {123};
		r.langTypePrefs = new int[] {3,4,5,2,1};
		r.stampModules = new int[] {4,5,6,7};
		r.stampPath = 4;
		r.stampPrecedence = 5;
		r.stampStates = new byte[] {2};
		r.stampTime = Long.MAX_VALUE;
		r.taxonomyType = 5;

		r.logicStatedAssemblage = 1;
		r.logicInferredAssemblage = 2;
		r.logicDescLogicProfile = 3;
		r.logicClassifier = 4;
		
		String token1 = r.serialize();
		System.out.println(token1);
		new CoordinatesToken(token1);
	}
}
