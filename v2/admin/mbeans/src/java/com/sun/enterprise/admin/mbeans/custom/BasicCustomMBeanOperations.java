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


package com.sun.enterprise.admin.mbeans.custom;

import com.sun.enterprise.admin.server.core.CustomMBeanException;
import com.sun.enterprise.admin.server.core.CustomMBeanRegistration;
import com.sun.enterprise.admin.common.MBeanServerFactory;
import com.sun.enterprise.admin.mbeans.custom.loading.CustomMBeanRegistrationImpl;
import com.sun.enterprise.admin.meta.MBeanRegistryFactory;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.serverbeans.Mbean;
import com.sun.enterprise.config.serverbeans.ServerBeansFactory;
import com.sun.enterprise.admin.target.TargetBuilder;
import com.sun.enterprise.admin.target.Target;

import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.util.HashMap;
import javax.management.MBeanInfo;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.MBeanInfo;
import javax.management.MBeanServerConnection;
import javax.management.MBeanServer;


public class BasicCustomMBeanOperations implements CustomMBeanOperationsMBean {

    /** The instance of AdminContext that correpsponds to configuration on disk */
    protected final ConfigContext acc;
    
    public BasicCustomMBeanOperations() {
        this.acc = MBeanRegistryFactory.getAdminContext().getAdminConfigContext();
    }

    public String createMBean(final String target, final String className) throws CustomMBeanException {
        final Map<String, String> params        = CustomMBeanConstants.unmodifiableMap(CustomMBeanConstants.IMPL_CLASS_NAME_KEY, className);
        final Map<String, String> attributes    = Collections.emptyMap();
        return ( this.createMBean(target, params, attributes) );
    }

    public String createMBean(String target, Map<String, String> params) throws CustomMBeanException {
        final Map<String, String> attributes = Collections.emptyMap();
        return ( this.createMBean(target, params, attributes) );
    }

    public String createMBean(final String target, final Map<String, String> params, final Map<String, String> attributes) throws CustomMBeanException {
        if (params == null || attributes == null)
            throw new IllegalArgumentException(CMBStrings.get("InternalError", "null argument"));
        Target t = null;
        try {
            t = TargetBuilder.INSTANCE.createTarget(target, this.acc);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
        return ( this.createMBeanDefinitionAddingReferenceToServer(t.getName(), params, attributes) );
    }
    
    public void createMBeanRef(final String target, final String ref) throws CustomMBeanException {
        throw new UnsupportedOperationException(CMBStrings.get("InternalError", "Not to be called on PE"));
    }

    public String deleteMBean(final String target, final String name) throws CustomMBeanException {
        if (name == null)
          throw new IllegalArgumentException(CMBStrings.get("InternalError", "null argument"));
     
        Target t = null;
        try {
            t = TargetBuilder.INSTANCE.createTarget(target, this.acc);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
        
        return ( this.deleteMBeanDefinitionRemovingReferenceFromDomain(t.getName(), name) );
    }

    public void deleteMBeanRef(final String target, final String ref) throws CustomMBeanException {
        throw new UnsupportedOperationException(CMBStrings.get("InternalError", "Not to be called on PE"));
    }
    
    /**
     * Return the MBeanInfo of a given Custom MBean.  
     * The MBean must be loadable from the standard App Server location.
     * The code does this:
     * <ul>
     * <li>Register the MBean in the MBeanServer
     * <li>Fetch and save the MBeanInfo
     * <li>Unregister the MBean
     * </ul>
     * Note that if the MBean can't be deployed successfully then this method won't work.
     * @param classname 
     * @throws com.sun.enterprise.admin.mbeans.custom.CustomMBeanException 
     * @return The MBeanInfo object.
     */
    public MBeanInfo getMBeanInfo(String classname) throws CustomMBeanException
    {
        try
        {
            // create some unique names...
            final String oname = "user:getMBeanInfo=" + System.nanoTime();   
            final String name = "getMBeanInfo" + System.nanoTime();   

            // create an MBean object because our helper code uses them...
            Mbean mbean = new Mbean();
            mbean.setImplClassName(classname);
            mbean.setName(name);
            mbean.setObjectName(oname);
            mbean.setEnabled(true);

            // fetch the MBeanServer
            final MBeanServer mbs = MBeanServerFactory.getMBeanServer();

            // Create a helper to do the reg/unreg...
            CustomMBeanRegistrationImpl cmr = new CustomMBeanRegistrationImpl(mbs);

            // register it
            cmr.registerMBean(mbean);

            // get the REAL ObjectName
            ObjectName ron = cmr.getCascadingAwareObjectName(mbean);

            // now fetch the MBeanInfo
            MBeanInfo info = mbs.getMBeanInfo(ron);

            // unregister it...
            mbs.unregisterMBean(ron);

            return info;
        }
        catch(Exception e)
        {
            throw new CustomMBeanException(e);
        }
    }
    
    /** Method that does bulk of the work to fulfill most of the functionality of this class. Other methods
     * in this class and any subclasses should consider calling this method. It does the validation
     * checks before creating the <i> mbean definition </i> in server's configuration. Most importantly, if
     * all is well, a custom MBean definition will be created and an application-ref will be created for a 
     * server so that when the target server starts up, this mbean (which is referenced) is available for registration.
     * @param s String representing the name of the server instance
     * @param params a Map<String, String> specifying the other parameters optionally needed for mbean
     * @param attributes a Map<String, String> specifying the custom MBean initialization attributes
     */
    protected String createMBeanDefinitionAddingReferenceToServer(final String s, final Map<String, String> params, final Map<String, String> attributes) throws CustomMBeanException {
        try {
            final Mbean mbean = this.createMBeanDefinitionInDomain(params,  attributes);
            final ObjectName onPostReg = new ObjectName(mbean.getObjectName());

			// bug 6308668
			// the mbean was created but never deleted IN THE CONTEXT
			if (onExists(s, onPostReg))  //use references here, because object-name is NOT a primary key unlike name
			{
				ServerBeansFactory.removeMbeanDefinition(acc, mbean.getName());
	
                throw new CustomMBeanException(CMBStrings.get("AlreadyExists",  onPostReg.toString()));
			}
            ServerBeansFactory.addMbeanReference(this.acc, mbean.getName(), s);
            return ( mbean.getName() );
        } catch (final Exception e) {
            throw new CustomMBeanException(e);
        }
    }

    protected Mbean createMBeanDefinitionInDomain(final Map<String, String> params, 
                final Map<String, String> attributes) throws CustomMBeanException
    {
        final Map<String, String> mm = checkAndModifyParamsForName(params);
        final String name = mm.get(CustomMBeanConstants.NAME_KEY);
        if (definitionExists(name))
            throw new CustomMBeanException(CMBStrings.get("AlreadyExists",  name));
        final ObjectName onPostReg = selectObjectName(mm, attributes);
        if (!CustomMBeanConstants.CUSTOM_MBEAN_DOMAIN.equals(onPostReg.getDomain())) {
                throw new IllegalArgumentException (CMBStrings.get("BadDomainName", onPostReg.getDomain(), CustomMBeanConstants.CUSTOM_MBEAN_DOMAIN));
        }
        // Now we mutate the params with "final" valid values
        mm.put(CustomMBeanConstants.OBJECT_NAME_KEY, onPostReg.toString()); //put the correct object-name to map
        final Mbean mbean = MBeanValidator.toMbean(mm, attributes, true);
        try {
            ServerBeansFactory.addMbeanDefinition(this.acc, mbean);
        } catch (final Exception e) {
            throw new CustomMBeanException(e);
        }
        return ( mbean );
    }
    protected Map<String, String> checkAndModifyParamsForName(final Map<String, String> params) throws CustomMBeanException {
        checkParams(params);
        final Map<String, String> paramsWithNameKey = putNameIfAbsent(params); // possibly changes the params
        //at this point, it can be safely assumed that both name and class-name of the custom-mbean are available
        final String name = paramsWithNameKey.get(CustomMBeanConstants.NAME_KEY);
        return ( paramsWithNameKey );
    }
    
    protected ObjectName selectObjectName(final Map<String, String> mm, final Map<String, String> attributes) throws CustomMBeanException {
        final ObjectName onPreReg   = ObjectNameSelectionAlgorithm.select(mm);
        //by the time the algorithm is accessed, if the map contains object-name it is valid
        final MBeanValidator mv     = new MBeanValidator();
        final ObjectName onPostReg  = mv.registerTestMBean(createTestMap(Collections.unmodifiableMap(mm), onPreReg), attributes);
        final String className = mm.get(CustomMBeanConstants.IMPL_CLASS_NAME_KEY);
        final boolean selfReg = ObjectNameSelectionAlgorithm.implementsMBeanRegistrationInterface(className);
        if (onPostReg != null) { // todo -- should throw an exception if it is null!!
            // note that onPostReg is *always* going to contain the "server" property, so we must account for that
            final ObjectName cascadedON = CustomMBeanRegistrationImpl.getCascadingAwareObjectName(onPreReg);

            // It may be setting its own name if it implements MBeanRegistration...
            if (!onPostReg.equals(cascadedON) && !selfReg) {
                throw new CustomMBeanException(CMBStrings.get("ObjectNameMismatch", onPreReg, CustomMBeanRegistrationImpl.getCascadingUnawareObjectName(onPostReg)));
            }
        }
        try {
            mv.unregisterTestMBean(onPostReg); //cleanup, if fails, could be ignored, as a new MBS is employed every time
        } catch (final Exception e) {
            e.printStackTrace();
        }
        //Note that we should always return the object-name without "server" property
        final ObjectName onInConfig = CustomMBeanRegistrationImpl.getCascadingUnawareObjectName(onPostReg);
        return ( onInConfig );
    }
    protected String deleteMBeanDefinitionRemovingReferenceFromDomain(final String s, final String name) throws RuntimeException {
        boolean refd;
        try {
            refd = ServerBeansFactory.isReferencedMBean(acc, s, name);
        } catch(final Exception e) {
            throw new RuntimeException(e);
        }
        if (!refd) {
            throw new RuntimeException(CMBStrings.get("RefsNotFound", s, name));
        }
        removeMBeanDefinitionAndReferenceFromConfigTree(s, name);
        return ( name );
    }

    protected Map<String, String> putNameIfAbsent(final Map<String, String> m) {
        final Map<String, String> nm    = new HashMap<String, String>(m);
        final String c                  = nm.get(CustomMBeanConstants.IMPL_CLASS_NAME_KEY); // must not be null
        assert (c != null);
        if (!nm.containsKey(CustomMBeanConstants.NAME_KEY))
            nm.put(CustomMBeanConstants.NAME_KEY, c);
        return ( nm );
    }

    protected void checkParams(final Map<String, String> params) throws IllegalArgumentException {
        if (!params.containsKey(CustomMBeanConstants.IMPL_CLASS_NAME_KEY))
            throw new IllegalArgumentException(CMBStrings.get("NoImplClass"));
        checkValidIfPresent(params);
        checkValidObjectNameIfPresent(params);
    }

    protected boolean definitionExists(final String name) throws CustomMBeanException {
        boolean exists = false;
        try {
            final List<Mbean> mbeans = ServerBeansFactory.getAllMBeanDefinitions(acc);
            for (Mbean m : mbeans) {
                if (m.getName().equals(name)) {
                    exists = true;
                    break;
                }
                    
            }
            return (exists);
        } catch (final Exception e) {
            throw new CustomMBeanException(e);
        }
    }
    
    protected boolean onExists(final String server, final ObjectName on) throws RuntimeException {
        try {
            boolean exists = false;
            final List<Mbean> mbeans = ServerBeansFactory.getReferencedMBeans(acc, server);
            for (Mbean m : mbeans) {
                final String onsFromConfig = m.getObjectName();
                final ObjectName onFromConfig = new ObjectName(onsFromConfig);
                if (onFromConfig.equals(on)) {
                    exists = true;
                    break;
                }
            }
            return ( exists ) ;
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }    
    ///// Private Methods /////
    
    private void checkValidIfPresent(final Map<String, String> params) throws IllegalArgumentException {
        final String nameKey    = CustomMBeanConstants.NAME_KEY;
        final String name       = params.get(nameKey);
        final String cKey       = CustomMBeanConstants.IMPL_CLASS_NAME_KEY;
        final String cVal       = params.get(cKey);
        final String onKey      = CustomMBeanConstants.OBJECT_NAME_KEY;
        final String onVal      = params.get(onKey);
        if (params.containsKey(nameKey) && name == null)

            throw new IllegalArgumentException(CMBStrings.get("MapHasNullParam", "Name"));
        if (params.containsKey(cKey) && cVal == null)
            throw new IllegalArgumentException(CMBStrings.get("MapHasNullParam", "ClassName"));
        if (params.containsKey(onKey) && onVal == null)
            throw new IllegalArgumentException(CMBStrings.get("MapHasNullParam", "ObjectName"));
    }
        
    private void checkValidObjectNameIfPresent(final Map<String, String> params) throws IllegalArgumentException {
        final String onKey      = CustomMBeanConstants.OBJECT_NAME_KEY;
        final String onVal      = params.get(onKey);
        boolean onSpecified     = params.containsKey(onKey) && onVal != null;
        ObjectName on           = null;
        if (onSpecified) {
            try {
                on = new ObjectName(onVal); //see if JMX likes it
            } catch(final MalformedObjectNameException me) {
                throw new IllegalArgumentException(me);
            }
            if (on.isPattern())
                throw new IllegalArgumentException(CMBStrings.get("ObjectNamePattern"));
            final String d = on.getDomain();
            if (!CustomMBeanConstants.CUSTOM_MBEAN_DOMAIN.equals(d)) {
                throw new IllegalArgumentException (CMBStrings.get("BadDomainName", d, CustomMBeanConstants.CUSTOM_MBEAN_DOMAIN));
            }
            if (on.getKeyProperty(CustomMBeanConstants.SERVER_KEY) != null) {
                throw new IllegalArgumentException(CMBStrings.get("ObjectNameReserved", on.toString(), CustomMBeanConstants.SERVER_KEY));
            }
        }
    }
        
    private void removeMBeanDefinitionAndReferenceFromConfigTree(final String server, final String ref) throws RuntimeException {
        try {
            ServerBeansFactory.removeMbeanReference(acc, ref, server);
            ServerBeansFactory.removeMbeanDefinition(acc, ref);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }
    private Map<String, String> createTestMap(final Map<String, String> params, final ObjectName on) {
        final Map<String, String> nm = new HashMap<String, String> (params);
        nm.put(CustomMBeanConstants.OBJECT_NAME_KEY, on.toString());
        return ( nm );
    }
    ///// Private Methods /////
}
