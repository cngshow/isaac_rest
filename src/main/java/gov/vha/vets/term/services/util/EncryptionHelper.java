package gov.vha.vets.term.services.util;

import gov.vha.vets.term.services.exception.STSException;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


/**
 * Excryption helper methods
 * @author vhaislnobleb
 */
public class EncryptionHelper
{
    /*
     * Encrypt a clear text string into a MD5 encrypted hex string
     * @return encrypted string
     */
    public static String encrypt(String clearText) throws STSException
    {
        try
        {
            return ChecksumHelper.byteArrayToHexString(MessageDigest.getInstance("MD5").digest(clearText.getBytes()));
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new STSException(e);
        }
    }
}
