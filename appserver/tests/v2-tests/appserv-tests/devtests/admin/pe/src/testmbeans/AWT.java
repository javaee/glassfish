/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

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
