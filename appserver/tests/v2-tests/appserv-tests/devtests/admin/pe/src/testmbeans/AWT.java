package testmbeans;

import javax.management.*;
import java.io.*;
import javax.swing.JFrame;
import java.awt.BorderLayout;
import java.awt.Label;
import java.net.URL;


public class AWT implements AWTMBean
{
    
    private final String JMX_MONITOR_GAUGE_LOW = "jmx.monitor.gauge.low";
    private final String JMX_MONITOR_GAUGE_HIGH = "jmx.monitor.gauge.high";
    //private final String JMX_MONITOR_COUNTER_THRESHOLD = "jmx.monitor.counter.threshold";
    public AWT()
    {
        trigger();
        //throw new NullPointerException("NPE Here!!");
    }
    public synchronized void trigger()
    {
        try
        {
            // test the getResource in MBeanClassLoader
            // also test this AWT code which will have a failed
            // resource load of a log file
            
            Class	clazz	= getClass();
            URL url = clazz.getResource("/testmbeans/AWT.class");

            String filename = "error";

            if(url == null)
                url = clazz.getResource("AWT.class");
            if(url != null)
                filename = url.getPath();
            
            String mesg = "If you see a filename -- getResource succeeded: " + filename;
            JFrame.setDefaultLookAndFeelDecorated(true);
            JFrame myFrame = new JFrame("Glassfish");
            myFrame.setSize(1000, 180);
            myFrame.setLocation(100,100);
            //myFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                myFrame.getContentPane().add(new Label(mesg,Label.CENTER), BorderLayout.CENTER);
            myFrame.setVisible(true);
            myFrame.show();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
