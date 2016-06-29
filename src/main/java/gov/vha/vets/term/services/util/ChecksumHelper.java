package gov.vha.vets.term.services.util;

public class ChecksumHelper
{
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
        { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f" };
        StringBuffer out = new StringBuffer(in.length * 2);

        while (i < in.length)
        {
            ch = (byte) (in[i] & 0xF0); // Strip off high nibble
            ch = (byte) (ch >>> 4);
            // shift the bits down
            ch = (byte) (ch & 0x0F);
            // must do this is high order bit is on!
            out.append(pseudo[ch]); // convert the nibble to a String Character
            ch = (byte) (in[i] & 0x0F); // Strip off low nibble
            out.append(pseudo[ch]); // convert the nibble to a String Character
            i++;
        }
        String rslt = new String(out);
        return rslt;
    }

}
