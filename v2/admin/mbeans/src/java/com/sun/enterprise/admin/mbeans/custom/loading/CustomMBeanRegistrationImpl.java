/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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


package com.sun.enterprise.admin.mbeans.custom.loading;
import com.sun.enterprise.admin.server.core.CustomMBeanException;
import java.util.logging.Logger;
import javax.management.MBeanServer;
import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectInstance;
import javax.management.ObjectName;

import com.sun.enterprise.admin.mbeans.custom.CMBStrings;
import com.sun.enterprise.admin.mbeans.custom.ObjectNameSelectionAlgorithm;
import com.sun.enterprise.config.serverbeans.Mbean;
import com.sun.enterprise.config.serverbeans.ElementProperty;
import com.sun.enterprise.admin.common.constant.AdminConstants;
import com.sun.enterprise.admin.mbeans.custom.CustomMBeanConstants;
import com.sun.enterprise.admin.server.core.CustomMBeanRegistration;
import com.sun.enterprise.util.SystemPropertyConstants;
import java.util.Hashtable;
import java.util.List;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanException;
import javax.management.ReflectionException;
import javax.management.RuntimeOperationsException;

/** Class to register all the MBeans defined in the server's configuration. Registering the MBeans is done 
 * as described in the design document. The self-management rules are to be taken into account for this.
 * As it stands now, (AS 9.0) this class is not designed to be thread-safe.
 * The calling code must ensure serial access if needed.
 * @since SJSAS 9.0
 */
public final class CustomMBeanRegistrationImpl implements CustomMBeanRegistration {
    
    /** Creates a new instance of CustomMBeanRegistrar */
    private final MBeanServer mbs;
    private ClassLoader cl;
    private static final Logger logger = Logger.getLogger(AdminConstants.kLoggerName);

    public CustomMBeanRegistrationImpl(final MBeanServer mbs) throws CustomMBeanException {
        this.mbs    = mbs;
        this.cl     = new MBeanClassLoader();
    }
    
    
    public void registerMBeans(final List<Mbean> mbeans, final boolean continueReg) throws RuntimeException {
        if (mbeans == null)
            throw new IllegalArgumentException(
                CMBStrings.get("InternalError", "registerMBeans() received a null mbeans argument"));
        for (Mbean mbean : mbeans) {
            try {
                registerMBean(mbean);
            } catch(final Throwable t) {
                if (continueReg) {
                    logger.info(CMBStrings.get("cmb.registerError", mbean.getName()));
                }
                else {
                    throw new RuntimeException(t);
                }
            }
        }
    }
    
    public void registerMBeans(final List<Mbean> mbeans) throws RuntimeException {
        this.registerMBeans(mbeans, true);
    }
    public void setClassLoader(final ClassLoader cl) throws IllegalArgumentException {
        if (cl == null)
            throw new IllegalArgumentException(CMBStrings.get("InternalError", "setClassLoader() received a null argument"));
        this.cl = cl;
    }
    public ObjectName registerMBean(final Mbean mbean) throws CustomMBeanException {
        /* This is the only place where the "registration of the MBean happens.
         */
        if (mbean == null)
            throw new CustomMBeanException(CMBStrings.get("InternalError", "registerMBean() received a null argument"));
        
        ObjectName ron = null;
        try {
            logger.fine(CMBStrings.get("cmb.loadingMBean1", mbean.getName()));
            final ObjectName mon    = getCascadingAwareObjectName(mbean);
            logger.fine(CMBStrings.get("cmb.loadingMBean2", mon.toString()));
            final Class mc          = loadIt(mbean);
            final Object mo         = newIt(mc);
            ron = registerIt(mo, mon);
            
            // if the MBean implements MBeanRegistration -- it can set the ON to 
            // anything at all...
            if(!ObjectNameSelectionAlgorithm.implementsMBeanRegistrationInterface(mbean.getImplClassName()))
            {
                if(!mon.equals(ron))
                    throw new CustomMBeanException(CMBStrings.get("objNameMismatch", mon, ron));
            }
            initIt(mbean, ron);
   
            // WBN 12-15-2005
            // this is just defensive programming -- the Listener should not be
            // calling us on a disabled mbean.  In the case we are being called
            // as part of pre-reg in order to get an objectname, this will make
            // one extra unneeded call to unregisterIt()
            // I say, safety first, performance second!
            if(!mbean.isEnabled())
                unregisterIt(mbean, ron);
            return ( ron );

        }catch (final CustomMBeanException cmbe) {
            if(ron != null) {
                try {
                    // paranoia...
                    unregisterIt(mbean, ron);
                }catch(Throwable t) {
                }
            }
            throw cmbe;
        }catch (final Throwable e) {
            if(ron != null) {
                try {
                    // paranoia...
                    unregisterIt(mbean, ron);
                }catch(Throwable t) {
                }
            }
            throw new CustomMBeanException(e);
        }
    }
    
    public static ObjectName getCascadingAwareObjectName(final Mbean mbean) throws CustomMBeanException {
        try {
            final ObjectName configON   = new ObjectName(mbean.getObjectName());
            return (getCascadingAwareObjectName(configON) );
        } catch(final CustomMBeanException cmbe) {
            throw cmbe;
        } catch(final Exception e) {
            throw new CustomMBeanException(e);
        }
    }
    public static ObjectName getCascadingAwareObjectName(final ObjectName configON) throws CustomMBeanException {
        try {
            final String serverNameKey  = CustomMBeanConstants.SERVER_KEY;
            final String serverNameVal  = System.getProperty(SystemPropertyConstants.SERVER_NAME);
            final Hashtable properties  = configON.getKeyPropertyList();
            properties.put(serverNameKey, serverNameVal);
            final ObjectName ron = new ObjectName(configON.getDomain(), properties);
            return ( ron );
        } catch(final Exception e) {
            throw new CustomMBeanException(e);
        }
    }
    public static ObjectName getCascadingUnawareObjectName(final ObjectName cascadeON) throws CustomMBeanException {
        if (cascadeON == null) 
            throw new CustomMBeanException(CMBStrings.get("InternalError", "getCascadingUnawareObjectName() received a null argument"));
        try {
            ObjectName ron = cascadeON;
            final String serverNameKey  = CustomMBeanConstants.SERVER_KEY;
            final Hashtable properties  = cascadeON.getKeyPropertyList(); // this may be unmodifiable
            if (properties.containsKey(serverNameKey)) {
                final Hashtable np = new Hashtable(properties);
                np.remove(serverNameKey);
                ron = new ObjectName(cascadeON.getDomain(), np);
            }
            return ( ron );
        } catch(final Exception e) {
            throw new CustomMBeanException(e);
        }        
    }
        
    ////////// Private Methods ///////
    private Class loadIt(Mbean mbean) throws CustomMBeanException {
        final String classname = mbean.getImplClassName();
        try {
            final Class c = cl.loadClass(classname);
            logger.fine(CMBStrings.get("cmb.loadingMBean8", c.getName(), cl.getClass().getName(), c.getClassLoader().getClass().getName()));
            return ( c );
        } catch (final ClassNotFoundException cnfe) {
            logger.info(CMBStrings.get("cmb.loadingMBean3", mbean.getImplClassName(), cl.getClass().getName()));
            throw new CustomMBeanException(cnfe);
        } catch (final NoClassDefFoundError ncdfe) {
            logger.info(CMBStrings.get("cmb.loadingMBean4", mbean.getImplClassName(), cl.getClass().getName()));
            throw new CustomMBeanException(ncdfe);
        } catch (final Exception e) {
            throw new CustomMBeanException(e);
        }
    }
    private Object newIt(final Class c) throws CustomMBeanException{
        String name = null;
        try{
            name = c.getName();
            return c.newInstance();
         } catch (final InstantiationException ie) {
            logger.info(CMBStrings.get("cmb.loadingMBean5", name));
            throw new CustomMBeanException(ie);
        } catch (final IllegalAccessException iae) {
            logger.info(CMBStrings.get("cmb.loadingMBean6", name));
            throw new CustomMBeanException(iae);
        } catch (final ExceptionInInitializerError eie) {
            logger.info(CMBStrings.get("cmb.loadingMBean7", name));
            throw new CustomMBeanException(eie);
        }catch(Throwable t) {
            // yes -- we are catching a Throwable here.  Normally this is highly 
            // discouraged.  But this is a special case.  We are calling the user's
            // code and he may be throwing a Throwable in the ctor...
            
            String message = CMBStrings.get("cmb.newingMBean", c.getName(), t);
            logger.warning(message);
            throw new CustomMBeanException(message, t);
        }
    }
    
    private ObjectName registerIt(final Object mo, final ObjectName on) throws CustomMBeanException {
        if(mo == null)
            throw new CustomMBeanException(CMBStrings.get("objNameNull"));
        try {
            final ObjectInstance oi = mbs.registerMBean(mo, on);
            return ( oi.getObjectName() );
        }catch(Exception e){
            throw new CustomMBeanException(e);
        }
    }
    private void initIt(final Mbean mbc, final ObjectName on) throws CustomMBeanException {
        try {
            final MBeanAttributeSetter mas = new MBeanAttributeSetter(mbs, on);
            final ElementProperty[] ats = mbc.getElementProperty();
            for (ElementProperty p : ats) {
                mas.setIt(p.getName(), p.getValue());
                logger.fine(CMBStrings.get("cmb.initMBean",  p.getName(), mbc.getName()));
            }
        }catch(CustomMBeanException cmbe) {
            // If we get a CMBE -- then everything is already documented...
            throw cmbe;
        }catch(Throwable t) {
            // indirect calls to user code -- thus Throwable!
            throw new CustomMBeanException(t);
        }
    }
    
    private void unregisterIt(final Mbean m, final ObjectName ron) {
        //attempt to unregister the mbean, not being able to do so is not fatal
        // DO NOT ALLOW Exceptions out of here -- this method gets called from
        // within catch blocks -- which would be a tragic java problem!
        try {
            if (mbs.isRegistered(ron))
                mbs.unregisterMBean(ron);
        } catch (final Exception e) { 
            logger.warning(CMBStrings.get("cmb.unloadMBeanError", m.getName()));
        }
    }
    ////////// Private Methods ///////
}