/*
 * Created on Apr 8, 2005
 */
package gov.vha.vets.term.services.loglayout;

import java.text.SimpleDateFormat;

import org.apache.log4j.HTMLLayout;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.helpers.Transform;
import org.apache.log4j.spi.LocationInfo;
import org.apache.log4j.spi.LoggingEvent;

/**
 * @author vhaislvaranb
 * 
 * Current HTMLLayout outputs the date/time in milliseconds. 
 * This class extends the HTMLLayout functionality (as discussed by Log4J developers)
 * to format the date/time in a human readable form. Future versions of Log4J
 * will have this feature built in to them.
 *  
 */

public class VETSHTMLLayout extends HTMLLayout
{
    //  output htmlTree appended to when format() is invoked
    private StringBuffer sbuf = new StringBuffer(BUF_SIZE);
    private SimpleDateFormat dateFormatter = null;    
    static String TRACE_PREFIX = "<br>&nbsp;&nbsp;&nbsp;&nbsp;";
   
    
    public String getTimeStampFormat()
    {
        return null;
    }
    
    public void setTimeStampFormat(String timeStampFormat)
    {
        try
        {
            dateFormatter = new SimpleDateFormat(timeStampFormat);
        }
        catch(Exception e)
        {
            // Catch any exceptions that may arise due to 
            // improper format. Should be changed at a later date.
        }       
        
    }
    
    
    // Override the format method to display time correctly
    public String format(LoggingEvent event)
    {
        if (sbuf.capacity() > MAX_CAPACITY)
        {
            sbuf = new StringBuffer(BUF_SIZE);
        }
        else
        {
            sbuf.setLength(0);
        }

        sbuf.append(Layout.LINE_SEP + "<tr>" + Layout.LINE_SEP);

        sbuf.append("<td>");
        if(dateFormatter != null)
        {
            sbuf.append(dateFormatter.format(new java.util.Date(event.timeStamp)));
        }
        else
        {
            sbuf.append(event.timeStamp - LoggingEvent.getStartTime());
        }
        sbuf.append("</td>" + Layout.LINE_SEP);       
        
        sbuf.append("<td title=\"" + event.getThreadName() + " thread\">");
        sbuf.append(Transform.escapeTags(event.getThreadName()));
        sbuf.append("</td>" + Layout.LINE_SEP);        
        
        sbuf.append("<td title=\"Level\">");
        if (event.getLevel().equals(Level.DEBUG))
        {
            sbuf.append("<font color=\"#339933\">");
            sbuf.append(event.getLevel());
            sbuf.append("</font>");
        }
        else if (event.getLevel().isGreaterOrEqual(Level.WARN))
        {
            sbuf.append("<font color=\"#993300\"><strong>");
            sbuf.append(event.getLevel());
            sbuf.append("</strong></font>");
        }
        else
        {
            sbuf.append(event.getLevel());
        }
        sbuf.append("</td>" + Layout.LINE_SEP);

        sbuf.append("<td title=\"" + event.getLoggerName() + " category\">");
        sbuf.append(Transform.escapeTags(event.getLoggerName()));
        sbuf.append("</td>" + Layout.LINE_SEP);

        if (getLocationInfo())
        {
            LocationInfo locInfo = event.getLocationInformation();
            sbuf.append("<td>");
            sbuf.append(Transform.escapeTags(locInfo.getFileName()));
            sbuf.append(':');
            sbuf.append(locInfo.getLineNumber());
            sbuf.append("</td>" + Layout.LINE_SEP);
        }

        sbuf.append("<td title=\"Message\">");
        sbuf.append(Transform.escapeTags(event.getRenderedMessage()));
        sbuf.append("</td>" + Layout.LINE_SEP);
        sbuf.append("</tr>" + Layout.LINE_SEP);

        if (event.getNDC() != null)
        {
            sbuf
                    .append("<tr><td bgcolor=\"#EEEEEE\" style=\"font-size : xx-small;\" colspan=\"6\" title=\"Nested Diagnostic Context\">");
            sbuf.append("NDC: " + Transform.escapeTags(event.getNDC()));
            sbuf.append("</td></tr>" + Layout.LINE_SEP);
        }

        String[] s = event.getThrowableStrRep();
        if (s != null)
        {
            sbuf
                    .append("<tr><td bgcolor=\"#993300\" style=\"color:White; font-size : xx-small;\" colspan=\"6\">");
            appendThrowableAsHTML(s, sbuf);
            sbuf.append("</td></tr>" + Layout.LINE_SEP);
        }

        return sbuf.toString();
    }

    protected void appendThrowableAsHTML(String[] s, StringBuffer sbuf)
    {
        if (s != null)
        {
            int len = s.length;
            if (len == 0)
                return;
            sbuf.append(Transform.escapeTags(s[0]));
            sbuf.append(Layout.LINE_SEP);
            for (int i = 1; i < len; i++)
            {
                sbuf.append(TRACE_PREFIX);
                sbuf.append(Transform.escapeTags(s[i]));
                sbuf.append(Layout.LINE_SEP);
            }
        }
    }
    
}