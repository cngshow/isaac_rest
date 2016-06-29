/**
 * 
 */
package gov.vha.vets.term.services.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * @author vhaislmurdoh
 *
 */
public class BuildVersion extends Task
{
    private String filepath = null;
    
    public void setFilepath(String filepath)
    {
        this.filepath = filepath;
    }
    
    public void execute() throws BuildException   
    {
        Properties buildProps = new Properties();
        FileInputStream in;
        String buildVersion = null;
        
        try
        {
            File buildfile = new File(filepath);
            buildfile.createNewFile();
            
            SimpleDateFormat formatter = new SimpleDateFormat("yyMMdd-HHmm");
            Date date = new Date();
            buildVersion = formatter.format(date);
            in = new FileInputStream(filepath);
            buildProps.load(in);
            in.close();
      
            FileOutputStream out = new FileOutputStream(filepath);
            
            //get hostname of box that build was done on
            InetAddress addr = InetAddress.getLocalHost();

            // Get IP Address
            byte[] ipAddr = addr.getAddress();
            StringBuffer ipBuff = new StringBuffer();
            for (int i = 0; i < ipAddr.length; i++)
            {
                ipBuff.append(ipAddr[i] + ".");
            }
            String ipAddress = ipBuff.toString();
            ipAddress = ipAddress.substring(0, ipAddress.lastIndexOf(".") - 1);
            
            //put data 
            buildProps.put("build", buildVersion);
            buildProps.put("hostname", addr.getHostName());
            buildProps.put("JDK-Version", System.getProperty("java.version"));
                        
            buildProps.store(out, "--BuildVersion Properties--");
            out.close();
            
            System.out.println(buildVersion);
            System.out.println("Hostname: " + addr.getHostName());
            System.out.println("JDK Version: " + System.getProperty("java.version"));
        }
        catch (UnknownHostException e)
        {
            e.printStackTrace();
        }   
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        } 
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
