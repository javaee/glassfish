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
package com.sun.enterprise.deployment;

import com.sun.enterprise.deployment.util.DOLUtils;
import com.sun.enterprise.deployment.util.ModuleDescriptor;
import com.sun.enterprise.deployment.util.XModuleType;

import javax.enterprise.deploy.shared.ModuleType;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * This descriptor contains all common information amongst root element 
 * of the J2EE Deployment Descriptors (application, ejb-jar, web-app, 
 * connector...). 
 *
 * @author Jerome Dochez
 */
public abstract class RootDeploymentDescriptor extends Descriptor {

     // the spec versions we should start to look at annotations
    private final static double ANNOTATION_EJB_VER = 3.0;
    private final static double ANNOTATION_WAR_VER = 2.5;
    private final static double ANNOTATION_CAR_VER = 5.0;

    /**
     * each module is uniquely identified with a moduleID
     */
    protected String moduleID;
    
    /**
     * version of the specification loaded by this descriptor
     */
    private String specVersion;
    
    /**
     * class loader associated to this module to load classes 
     * contained in the archive file
     */
    protected transient ClassLoader classLoader = null;

    /**
     * key is the URI representing PURoot.
     */
    protected Map<String, PersistenceUnitsDescriptor> persistenceUnitsDescriptors
            = new HashMap<String, PersistenceUnitsDescriptor>();

        /**
     * contains the information for this module (like it's module name)
     */
    protected ModuleDescriptor moduleDescriptor;

    private boolean fullFlag = false;
    private boolean fullAttribute = false;    

    /**
     * Construct a new RootDeploymentDescriptor 
     */
    public RootDeploymentDescriptor() {
        super();
    }
    
    /**
     * Construct a new RootDeploymentDescriptor with a name and description
     */
    public RootDeploymentDescriptor(String name, String description) {
        super(name, description);
    }
    
    /**
     * each module is uniquely identified with a moduleID
     * @param moduleID for this module
     */
    public void setModuleID(String moduleID) {
        this.moduleID = moduleID;
    }
    
    /**
     * @return the module ID for this module descriptor
     */
    public abstract String getModuleID();

    /**
     * @return the default version of the deployment descriptor
     * loaded by this descriptor
     */
    public abstract String getDefaultSpecVersion();

        
    /**
     * @return the specification version of the deployment descriptor
     * loaded by this descriptor
     */
    public String getSpecVersion() {
        if (specVersion == null) {
            specVersion = getDefaultSpecVersion();
        } 
        try {
            Double.parseDouble(specVersion); 
        } catch (NumberFormatException nfe) {
            DOLUtils.getDefaultLogger().log(Level.WARNING, "invalidSpecVersion",
                new Object[] {specVersion, getDefaultSpecVersion()});
            specVersion = getDefaultSpecVersion();
        }

        return specVersion;
    }
    
    /**
     * Sets the specification version of the deployment descriptor
     * @param specVersion version number
     */
    public void setSpecVersion(String specVersion) {
        this.specVersion = specVersion;
    }      
    
    /**
     * @return the module type for this bundle descriptor
     */
    public abstract XModuleType getModuleType();

    /**
     * Sets the class loader for this application
     */
    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }
    
    /**
     * @return the class loader associated with this module
     */
    public abstract ClassLoader getClassLoader();

    /**
     * sets the display name for this bundle
     */
    public void setDisplayName(String name) {
        super.setName(name);
    }
    
    /** 
     * @return the display name
     */
    public String getDisplayName() {
        return super.getName();
    }
    
    /**
     * as of J2EE1.4, get/setName are deprecated, 
     * people should use the set/getDisplayName or 
     * the set/getModuleID.
     */
    public void setName(String name) {
        setModuleID(name);
    }
    
    /**
     * as of J2EE1.4, get/setName are deprecated, 
     * people should use the set/getDisplayName or 
     * the set/getModuleID.
     * note : backward compatibility
     */     
    public String getName() {
        if (getModuleID()!=null) {
            return getModuleID();
        } else {
            return getDisplayName();
        }
    }
        
    public void setSchemaLocation(String schemaLocation) {
        addExtraAttribute("schema-location", schemaLocation);
    }
    
    public String getSchemaLocation() {
        return (String) getExtraAttribute("schema-location");
    }

   /**
     * @return the module descriptor for this bundle
     */
    public ModuleDescriptor getModuleDescriptor() {
        if (moduleDescriptor==null) {
            moduleDescriptor = new ModuleDescriptor();
            moduleDescriptor.setModuleType(getModuleType());
            moduleDescriptor.setDescriptor(this);
        }
        return moduleDescriptor;
    }

    /**
     * Sets the module descriptor for this bundle
     * @param descriptor for the module
     */
    public void setModuleDescriptor(ModuleDescriptor descriptor) {
        moduleDescriptor = descriptor;
    }    
    
    /**
     * @return true if this module is an application object 
     */
    public abstract boolean isApplication();
    
    /**
     * print a meaningful string for this object
     */
    public void print(StringBuffer toStringBuffer) {
        super.print(toStringBuffer);
        toStringBuffer.append("\n Module Type = ").append(getModuleType());
        toStringBuffer.append("\n Module spec version = ").append(getSpecVersion());
        if (moduleID!=null) 
            toStringBuffer.append("\n Module ID = ").append(moduleID);
        if (getSchemaLocation()!=null)
            toStringBuffer.append("\n Client SchemaLocation = ").append(getSchemaLocation());
    }

    /**
     * This method returns PersistenceUnitDescriptor objects in the
     * scope of this RootDeploymentDescriptor.
     * @return it returns an unmodifiable collection.
     * returns an empty collection if there is no PersistenceUnitDescriptor.
     */
    public Collection<PersistenceUnitsDescriptor> getPersistenceUnitsDescriptors() {
        return Collections.unmodifiableCollection(persistenceUnitsDescriptors.values());
    }

    /**
     * This method returns PersistenceUnitDescriptor object with matching
     * PURoot in the scope of this RootDeploymentDescriptor.
     * @param puRoot used for lookup
     * @return return the PersisteneUnitsDescriptor for the given PURoot.
     * It returns null if not found.
     */
    public PersistenceUnitsDescriptor getPersistenceUnitsDescriptor(
            String puRoot) {
        return persistenceUnitsDescriptors.get(puRoot);
    }

    /**
     * Add deplyoment information about all the persistence units
     * defined in a persistence.xml to this RootDeploymentDescriptor.
     * All the persistence units  defined inside the same persistence.xml
     * will be added with same PURoot. This method also sets the parent
     * reference in PersistenceUnitsDescriptor.
     * @param puRoot root of the persistence unit (its a relative path)
     * @param persistenceUnitsDescriptor is the descriptor object
     * representing the persistence unit.
     */
    public void addPersistenceUnitsDescriptor(String puRoot, PersistenceUnitsDescriptor persistenceUnitsDescriptor) {
        // We don't expect the parent to be already set
        // because that indicates transfer of ownership.
        assert(persistenceUnitsDescriptor.getParent() == null);
        persistenceUnitsDescriptor.setParent(this);
        persistenceUnitsDescriptor.setPuRoot(puRoot);
        persistenceUnitsDescriptors.put(puRoot, persistenceUnitsDescriptor);
    }

    /**
     * Sets the full flag of the bundle descriptor. Once set, the annotations
     * of the classes contained in the archive described by this bundle
     * descriptor will be ignored.
     * @param flag a boolean to set or unset the flag
     */
     public void setFullFlag(boolean flag) {
         fullFlag=flag;
     }

    /**
     * Sets the full attribute of the deployment descriptor
     * @param value the full attribute
     */
    public void setFullAttribute(String value) {
        fullAttribute = Boolean.valueOf(value);
    }

    /**
     * Get the full attribute of the deployment descriptor
     * @return the full attribute
     */
    public boolean isFullAttribute() {
        return fullAttribute;
    }

    /**
     * @ return true for following cases:
     *   1. When the full attribute is true. This attribute only applies to
     *      ejb module with schema version equal or later than 3.0;
            web module and schema version equal or later than than 2.5;
            appclient module and schema version equal or later than 5.0.
     *   2. When it's been tagged as "full" when processing annotations.
     *   3. When DD has a version which doesn't allowed annotations.
     *   return false otherwise.
     */
    public boolean isFullFlag() {
        // if the full attribute is true or it's been tagged as full,
        // return true
        if (fullAttribute == true || fullFlag == true) {
            return true;
        }
        return isDDWithNoAnnotationAllowed();
    }


    /**
     * @ return true for following cases:
     *   a. connector module;
     *   b. ejb module and schema version earlier than 3.0;
     *   c. web module and schema version earlier than 2.5;
     *   d. appclient module and schema version earlier than 5.0.
     */
    public boolean isDDWithNoAnnotationAllowed() {
        XModuleType mType = getModuleType();

        double specVersion = Double.parseDouble(getSpecVersion());

        // connector DD doesn't have annotation, so always treated
        // as full DD
        if (XModuleType.RAR == mType) {
            return true;
        } else {
            // we do not process annotations for earlier versions of DD
            if ( (mType.equals(ModuleType.EJB) &&
                  specVersion < ANNOTATION_EJB_VER) ||
                 (mType.equals(ModuleType.WAR) &&
                  specVersion < ANNOTATION_WAR_VER) ||
                 (mType.equals(ModuleType.CAR) &&
                  specVersion < ANNOTATION_CAR_VER) ) {
                return true;
            } else {
                return false;
            }
        }
    }    

}

