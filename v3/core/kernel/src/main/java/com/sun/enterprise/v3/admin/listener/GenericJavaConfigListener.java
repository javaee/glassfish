/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.enterprise.v3.admin.listener;

import com.sun.enterprise.config.serverbeans.JavaConfig;
import com.sun.enterprise.config.serverbeans.Profiler;
import org.jvnet.hk2.config.types.Property;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Collection;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jvnet.hk2.annotations.*;
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

@Service
public final class GenericJavaConfigListener implements PostConstruct, ConfigListener {
    @Inject JavaConfig jc;
    
    volatile List<String> oldProps;
    /* Implementation note: See 6028*/
    
    @Inject 
    Logger logger; //gets a root logger, which is ok for now.
    
    public void postConstruct() {
        if(jc != null && jc.getJvmOptions() != null) {
            oldProps = new ArrayList<String>(jc.getJvmOptions()); //defensive copy
        }
    }
    
    /* force serial behavior; don't allow more than one thread to make a mess here */
    public synchronized UnprocessedChangeEvents changed(PropertyChangeEvent[] events) {
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
                    final JavaConfig njc = (JavaConfig) t; 
                    logFine(type, njc);
                    
                    result = handle(oldProps, njc.getJvmOptions() );
                    oldProps = new ArrayList<String>(((JavaConfig)t).getJvmOptions()); //defensive copy, required step
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
    
    private NotProcessed handle(List<String> old, List<String> cur) {
        NotProcessed np = null;
        
        final Set<String> added = new HashSet<String>(cur);
        added.removeAll(old);
        
        final Set<String> removed = new HashSet<String>(old);
        removed.removeAll(cur);
        
        return getNotProcessed(removed, added);
    }
    //using C-style ;)
    private static final String SYS_PROP_REGEX = "=";
    
    private String[] nvp(final String s) {
        final String[] nv = s.split(SYS_PROP_REGEX);
        final String name  = nv[0];
        final String value = s.substring(name.length());
        
        return new String[] { name, value };
    }
    
    private NotProcessed getNotProcessed(
        final Set<String> removals,
        final Set<String> additions)
    {
        //look at the list, clear and/or add system properties 
        // otherwise they require server restart
        final int propLen = "-D".length();
        
        final List<String> reasons = new ArrayList<String>();
        for( final String removed : removals) {
            final String[] nv = nvp(removed);
            final String name  = nv[0];
            
            if (possiblyDynamicallyReconfigurable(removed)) {
                System.clearProperty(name.substring(propLen));
            }
            else {
                // detect a removal/addition which is really a change
                String newItem = null;
                for( final String added : additions ) {
                    if ( name.equals( nvp(added)[0] ) ) {
                        newItem = added;
                        additions.remove(added);
                        break;
                    }
                }
                String msg = null;
                if ( newItem != null ) {
                    msg = "Change from '" + removed + "' to '" + newItem + "' cannot take effect without server restart";
                }
                else {
                    msg = "Removal of: " + removed + " cannot take effect without server restart";
                }
                reasons.add(msg);
            }
        }
        
        // process any remaining additions
        for( final String added : additions) {
            final String[] nv = nvp(added);
            final String   name  = nv[0];
            final String   newValue = nv[1];
            
            if (possiblyDynamicallyReconfigurable(added)) {
                System.setProperty( name.substring(propLen), newValue );
            }
            else {
                reasons.add( "Addition of: '" + added + "' cannot take effect without server restart" );
            }
        }
        
        
        if ( reasons.size() != 0) {
            return new NotProcessed( toString(reasons) );
        }
        return null;
    }
    
    private static String toString( final List<String> items ) {
        final StringBuffer buf = new StringBuffer();
        final String delim = ", ";
        for( final String s : items ) {
            if ( buf.length() != 0 ) {
                buf.append(delim);
            }
            buf.append(s);
        }
        
        return buf.toString();
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
