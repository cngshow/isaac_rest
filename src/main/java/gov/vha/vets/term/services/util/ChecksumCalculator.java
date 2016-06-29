/*
 * Created on Dec 22, 2005
 *
 */
package gov.vha.vets.term.services.util;

import gov.vha.vets.term.services.exception.STSException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author vhaislnobleb
 *
 */
public class ChecksumCalculator
{
    
    private static final int MAX_FILE_NAME_LENGTH = 100;
    private static final String FILE_APPEND_NAME = "_log";
    private static final String FILE_EXTENSION = ".csm";
    private BufferedWriter bufferWriter;
    private MessageDigest appmd5;
    
    /**
     * Constuctor to create the checksum calculator object
     * This object will log all the data which is use to calculate
     * the checksum to a file
     * 
     * @param name
     * @throws IOException
     */
    public ChecksumCalculator(String regionName) throws STSException
    {
    	String checksumDir = "checksum";
    	
    	String modifiedRegionName = regionName.replaceAll("[^a-zA-Z0-9 ]", "");
    	modifiedRegionName = modifiedRegionName.substring(0, Math.min(modifiedRegionName.length(), MAX_FILE_NAME_LENGTH));
    	
    	try
        {
            //Determine of the 'checksum' directory exists
            boolean checksumDirExists = (new File(checksumDir)).exists();
            if(!checksumDirExists)
            {
            	//The directory doesn't exist so create it
            	new File(checksumDir).mkdir();
            }
            
            //Determine if the directory already exists
            boolean directoryExists = (new File(checksumDir + "/" + modifiedRegionName)).exists();
            if(!directoryExists)
            {
            	//The directory doesn't exist so create it
            	new File(checksumDir + "/" + modifiedRegionName).mkdir();
            }
            
            //Write the file to the path
            FileWriter fileWriter = new FileWriter(checksumDir + "/" + modifiedRegionName + "/"
            						+ modifiedRegionName + FILE_APPEND_NAME + FILE_EXTENSION);
            
            // Use a buffered writer
            this.bufferWriter = new BufferedWriter(fileWriter);
        }
        catch (IOException e1)
        {
            throw new STSException(e1);
        }
        
        try
        {
            // get MD5 message digest
            this.appmd5 = MessageDigest.getInstance("MD5");
        }
        catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }
    }
    
    /**
     * Constuctor to create the checksum calculator object
     * This object will log all the data which is use to calculate
     * the checksum to a file
     * 
     * @param name
     * @param datastreamFilePrefix Optional file name prefix
     * @throws IOException
     */
    public ChecksumCalculator(String regionName, String datastreamFilePrefix) throws STSException
    {
    	String checksumDir = "checksum";

    	String modifiedRegionName = regionName.replaceAll("[^a-zA-Z0-9 ]", "");
        modifiedRegionName = modifiedRegionName.substring(0, Math.min(modifiedRegionName.length(), MAX_FILE_NAME_LENGTH));
    	
    	try
        {
            //Determine of the 'checksum' directory exists
            boolean checksumDirExists = (new File(checksumDir)).exists();
            if(!checksumDirExists)
            {
            	//The directory doesn't exist so create it
            	new File(checksumDir).mkdir();
            }
            
            //Determine if the directory already exists
            boolean directoryExists = (new File(checksumDir + "/" + modifiedRegionName)).exists();
            if(!directoryExists)
            {
            	//The directory doesn't exist so create it
            	new File(checksumDir + "/" + modifiedRegionName).mkdir();
            }
            
            //Write the file to the path
            String filePrefix = (datastreamFilePrefix != null && datastreamFilePrefix.length() > 0) ? datastreamFilePrefix + "_" : "";
            FileWriter fileWriter = new FileWriter(checksumDir + "/" + modifiedRegionName + "/"
            						+ filePrefix + modifiedRegionName + FILE_APPEND_NAME + FILE_EXTENSION);
            
            // Use a buffered writer
            this.bufferWriter = new BufferedWriter(fileWriter);
        }
        catch (IOException e1)
        {
            throw new STSException(e1);
        }
        
        try
        {
            // get MD5 message digest
            this.appmd5 = MessageDigest.getInstance("MD5");
        }
        catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }
    }
    
    
    /**
     * Write the data use to calculate the checksum to a file
     * 
     * @param data
     * @throws IOException
     */
    public void write(String data) throws STSException
    {
        if (appmd5 != null)
        {
            appmd5.update(data.getBytes());
        }

        try
        {
            bufferWriter.write(data);
        }
        catch (IOException e)
        {
            throw new STSException(e);
        }
    }
    
    /**
     * Closes the file which is use to log data which is use to calculate the checksum
     * @throws IOException
     */
    public void close() throws STSException
    {
        
        try
        {
            bufferWriter.close();
        }
        catch (IOException e)
        {
            throw new STSException();
        }
    }
    
    /**
     * Returns the checksum of the data that was written to the file
     * 
     * @return Checksum string
     */
    public String getChecksum()
    {
        byte[] appHash = new byte[0];
        if (appmd5 != null)
        {
            appHash = appmd5.digest();
        }

        return byteArrayToHexString(appHash);
    }
    
    /**
     * Convert a byte[] array to readable string format. This makes the "hex"
     * readable!
     * 
     * @return result String buffer in String format
     * @param in
     *            byte[] buffer to convert to string format
     */
    public static String byteArrayToHexString(byte in[])
    {
        byte ch = 0x00;
        int i = 0;
        if (in == null || in.length <= 0)
            return null;

        String pseudo[] =
        { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d",
                "e", "f" };
        StringBuffer out = new StringBuffer(in.length * 2);

        while (i < in.length)
        {
            ch = (byte) (in[i] & 0xF0); // Strip off high nibble
            ch = (byte) (ch >>> 4);
            // shift the bits down
            ch = (byte) (ch & 0x0F);
            //       must do this is high order bit is on!
            out.append(pseudo[ch]); // convert the nibble to a String
            // Character
            ch = (byte) (in[i] & 0x0F); // Strip off low nibble
            out.append(pseudo[ch]); // convert the nibble to a String
            // Character
            i++;
        }
        String rslt = new String(out);
        
        return rslt;
    }

    
}
