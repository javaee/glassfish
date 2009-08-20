/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.enterprise.v3.admin.listener;

import com.sun.enterprise.config.serverbeans.JavaConfig;
import com.sun.enterprise.config.serverbeans.Profiler;
import org.glassfish.api.admin.config.Property;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.component.PostConstruct;
import org.jvnet.hk2.config.Changed;
import org.jvnet.hk2.config.Changed.TYPE;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.ConfigListener;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.NotProcessed;
import org.jvnet.hk2.config.UnprocessedChangeEvents;

/**
 *  Listens for the changes to the configuration of JVM and Java system
 *  properties (including the Java VM options).  Most of the effort involves the jvm-options
 *  list, but restart is also required for any changes to the java-config.
 *  <p>
 *  This class is implemented so that the server restart is NOT required if a deployer wants to deploy 
 *  an application and the application depends on a particular Java system property
 *  (-D) to be specified. As of now, the deployer specifies the system property
 *  and deploys the application and the application should find it when it does
 *  System.getProperty("property-name"). Here is the complete algorithm:
 * 
 *  <ol>
 *    <li> If any of the attributes of the java-config element (JavaConfig) change,
 *         this listener flags it as server-restart-required kind of change.
 *    </li>
 *    <li> If a system property is being defined and it is NOT one that starts with
 *         "-Djava." or "-Djavax.", it will be immediately set in the System using
 *         System.setProperty() call. A server restart won't be needed.
 *    </li>
 *    <li> If any other JVM option is defined that does not start with "-D" (excluding
 *         the cases covered above), it is deemed to be a JVM option resulting
 *         in server-restart-required flag set.
 *    </li>
 *    <li> If a System Property (with above distinctions) is removed, System.clearProperty()
 *         is called and server-restart-required flag is set accordingly.
 *    </li>
 *  </ol>
 * Change in the value of a particular system property level is not handled explicitly.
 * User interfaces should take a note of it. e.g. CLI does not make -Dfoo=bar and -Dfoo=bar1
 * as same properties being set to two different values since it is hard to distinguish it
 * in general case. Users should delete -Dfoo=bar and add -Dfoo=bar1explicitly in this case.
 * @author &#2325;&#2375;&#2342;&#2366;&#2352 (km@dev.java.net)
 * @since GlassFish V3
 * @see com.sun.enterprise.config.serverbeans.JavaConfig
 */

public final class GenericJavaConfigListener implements PostConstruct, ConfigListener {
    @Inject JavaConfig jc;
    List<String> oldProps;
    /* Implementation note: See 6028*/
    
    @Inject 
    Logger logger; //gets a root logger, which is ok for now.
    
    public void postConstruct() {
        if(jc != null && jc.getJvmOptions() != null) {
            oldProps = new ArrayList<String>(jc.getJvmOptions()); //defensive copy
        }
    }
    public UnprocessedChangeEvents changed(PropertyChangeEvent[] events) {
        final UnprocessedChangeEvents unp = ConfigSupport.sortAndDispatch(events, new Changed() {
            public <T extends ConfigBeanProxy> NotProcessed changed(TYPE type, Class<T> tc, T t) {
                NotProcessed result = null;
                
                if ( t instanceof Profiler )
                {
                    result = new NotProcessed("Creation or changes to a profiler require restart");
                }
                else if ( t instanceof Property )
                {
                    result = new NotProcessed("Addition of properties to JavaConfig requires restart");
                }
                else if ( t instanceof JavaConfig )
                {
                    JavaConfig njc = (JavaConfig) t; //this must not throw ClassCastException
                    logFine(type, njc);
                    
                    if ( oldProps.size() == njc.getJvmOptions().size() )
                    {
                        // the JavaConfig itself has changed 
                        result = new NotProcessed("A java-config attribute was changed, restart required");
                    }
                    else
                    {
                        result = handle(oldProps, njc.getJvmOptions());
                        oldProps = new ArrayList<String>(((JavaConfig)t).getJvmOptions()); //defensive copy, required step
                    }
                }
                else {
                    throw new IllegalArgumentException( "Unknown interface: " + tc.getName() );
                }

                return result;
            }
        }
        , logger);
         return unp;
    }
    
    private void logFine(TYPE ct, JavaConfig njc) {
        final Level level = Level.FINE;
        if (logger.isLoggable(level)) {
            logger.log(level, "<java-config> changed");
            int os = oldProps.size(), ns = njc.getJvmOptions().size();
            if (os > ns) {
                logger.log(level, "a system property or a JVM option was removed (old size = " + os + "), new size: (" + ns + "), restart is required, based on the property");
            } else if(os < ns) {
                logger.log(level, "a system property or a JVM option was added, (old size = " + os + "), new size: (" + ns + "), restart is required, based on the property");
            } else {
                logger.log(level, "an attribute was changed, restart required");
            }
        }
    }
    
    private NotProcessed handle(List<String> olds, List<String> news) {
        if (olds.size() > news.size()) { //removal
            List<String> removals = olds.subList(news.size(), olds.size()); //backed by olds
            NotProcessed np = getNotProcessedRemovals(removals);
            return np;
        } else if (olds.size() < news.size()) { //addition
            List<String> adds = news.subList(olds.size(), news.size()); //backed by news ;)
            NotProcessed np = getNotProcessedAdds(adds);
            return np;
        } else {
            //nothing should be "NotProcessed" as this implies no change to system properties, VM options
            return null;
        }
    }
    //using C-style ;)
    private static final String SYS_PROP_REGEX = "=";
    
    private NotProcessed getNotProcessedRemovals(List<String> removals) {
        //look at the list, see if you can really clear every item from "System", 
        // otherwise say you are unable to do so
        String npReason = "";
        for(String s : removals) {
            if (possiblyDynamicallyReconfigurable(s)) {
                String[] nv = s.split(SYS_PROP_REGEX);
                System.clearProperty(nv[0].substring(2));  //finally!
            } else {
                npReason += ("Removal of: " + s + " can not take effect without server restart, ");
            }
        }
        if (npReason.length() != 0)
            return new NotProcessed(npReason);
        return null;
    }
    
    private NotProcessed getNotProcessedAdds(List<String> adds) {
        //look at the list, see if you can really set every item in "System", 
        // otherwise say you are unable to do so
        String npReason = "";
        for(String s : adds) {
            if (possiblyDynamicallyReconfigurable(s)) {
                String[] nv = s.split(SYS_PROP_REGEX);
                System.setProperty(nv[0].substring(2), nv[1]);  //finally!
            } else {
                npReason += ("Setting of: " + s + " can not take effect without server restart, ");
            }
        }
        if (npReason.length() != 0)
            return new NotProcessed(npReason);
        return null;        
    }
    
    /** Determines with some confidence level if a particular String denotes
     *  a system property that can be set in the current JVM's (i.e. the JVM where
     *  this method's code runs) System. Anything that does not start with
     *  "-D" is not dynamically settable. However, anything that starts with "-Djava."
     *  or "-Djavax." is not dynamically settable.
     */
    private boolean possiblyDynamicallyReconfigurable(String s) {
        if (s.startsWith ("-D") && !s.startsWith("-Djava.")
            && !s.startsWith("-Djavax.")) 
            return true;
        return false;
    }
}
