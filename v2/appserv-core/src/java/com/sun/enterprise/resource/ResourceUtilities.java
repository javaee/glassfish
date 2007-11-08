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

package com.sun.enterprise.resource;

import com.sun.enterprise.config.ConfigBean;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.serverbeans.ResourceHelper;
import com.sun.enterprise.connectors.util.ResourcesUtil;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.logging.LogDomains;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.management.Attribute;
import javax.management.AttributeList;
import static com.sun.enterprise.resource.ResourceConstants.*;

/** A class that holds utility/helper routines. Expected to contain static
*  methods to perform utility operations.
*
* @since Appserver 9.0
*/
public class ResourceUtilities {
    
    private final static Logger _logger = LogDomains.getLogger(LogDomains.RSR_LOGGER);
    private final static StringManager localStrings =
            StringManager.getManager(ResourceUtilities.class);
    
     private ResourceUtilities()/*disallowed*/ {
   }

   /** 
    * Checks if any of the Resource in the given set has a conflict with
    * resource definitions in the domain.xml. A <b> conflict </b> is defined
    * based on the type of the resource. For example, a JDBC Resource has "jndi-name"
    * that is the identifying key where as for a JDBC Connection Pool, it is
    * the "name" that must be unique.
    * @param resSet a Set of Resource elements.
    * @param cc instance of ConfigContext that you want to confirm this against. May not be null.
    * @return a Set of Resource elements that contains conflicting elements from the given Set. This
    * method does not create any Resource elements. It just references an element in conflict
    * from a Set that is returned. If there are no conflicts, an empty Set is returned. This method
    * never returns a null. If the given Set is null, an empty Set is returned.
    * @throws ConfigException if there is any error with accessing the configuration
    * hierarchy.
    */
   public static Set<Resource> getResourceConfigConflicts(final Set<Resource> resSet,
       final ConfigContext cc) throws ConfigException {
       final Set<Resource> conflicts = new HashSet<Resource>();
       if (resSet != null) {
           for (final Resource res : resSet) {
               final String id = getIdToCompare(res);
               final ConfigBean sb = ResourceHelper.findResource(cc, id);
               if (sb != null) {
                   conflicts.add(res);
               }
           }
       }
       return ( conflicts );
   }

     private static String getIdToCompare(final Resource res) {
       final AttributeList attrs = res.getAttributes();
       final String type = res.getType();
       String id;
       if (Resource.JDBC_CONNECTION_POOL.equals(type) ||
           Resource.CONNECTOR_CONNECTION_POOL.equals(type)){
          id = getNamedAttributeValue(attrs, CONNECTION_POOL_NAME);   // this should come from refactored stuff TBD
       }
       else if (Resource.CONNECTOR_SECURITY_MAP.equals(type)) {
           id = getNamedAttributeValue(attrs, SECURITY_MAP_NAME);  // this should come from refactored stuff TBD
       }
       else if (Resource.RESOURCE_ADAPTER_CONFIG.equals(type)) {
           id = getNamedAttributeValue(attrs, RESOURCE_ADAPTER_CONFIG_NAME);  // this should come from refactored stuff TBD
       }
       else {
           //it is OK to assume that this Resource will one of the *RESOURCEs?
           id = getNamedAttributeValue(attrs, JNDI_NAME); // this should come from refactored stuff TBD
       }
       return ( id );
   }

     private static String getNamedAttributeValue(final AttributeList attrs, final String aName) {
       String value = null;
       for (final Object obj : attrs) {
           if (obj instanceof Attribute) {
               final Attribute a = (Attribute) obj;
               if (aName.equals(a.getName())) {
                   value = a.getValue().toString();
               }
           }
       }
       return ( value );
     }
     
     /**
      * Resolves all duplicates and conflicts within an archive and returns a set
      * of resources that needs to be created for the archive being deployed. The
      * deployment backend would then use these set of resources to check for
      * conflicts with resources existing in domain.xml and then continue
      * with deployment.
      *
      * All resource duplicates within an archive found are flagged with a
      * WARNING and only one resource is added in the final <code>Resource</code>
      * <code>Set</code> returned.
      *
      * We currently do not handle any resource conflicts found within the archive
      * and the method throws an exception when this condition is detected.
      *
      * @param sunResList a list of <code>SunResourcesXML</code> corresponding to
      * sun-resources.xml found within an archive.
      *
      * @return a Set of <code>Resource</code>s that have been resolved of
      * duplicates and conflicts.
      *
      * @throws ResourceConflictException an exception is thrown when an archive is found to
      * have two or more resources that conflict with each other.
      */
     public static Set<Resource> resolveResourceDuplicatesConflictsWithinArchive(
             List<SunResourcesXML> sunResList) throws ResourceConflictException{
         boolean conflictExist = false;
         StringBuffer conflictingResources = new StringBuffer();
         Set<Resource> resourceSet = new HashSet<Resource>();
         Iterator<SunResourcesXML> sunResourcesXMLIter = sunResList.iterator();
         while(sunResourcesXMLIter.hasNext()){
             //get list of resources from one sun-resources.xml file
             SunResourcesXML sunResXML = sunResourcesXMLIter.next(); 
             List<Resource> resources = sunResXML.getResourcesList();
             Iterator<Resource> resourcesIter = resources.iterator();
             //for each resource mentioned
             while(resourcesIter.hasNext()){
                 Resource res = resourcesIter.next();
                 Iterator<Resource> resSetIter = resourceSet.iterator();
                 boolean addResource = true;
                 //check if a duplicate has already been added
                 while(resSetIter.hasNext()){
                     Resource existingRes = resSetIter.next();
                     if(existingRes.equals(res)){
                         //duplicate within an archive
                         addResource = false;
                         _logger.warning(localStrings.getString(
                                 "duplicate.resource.sun.resource.xml",
                                 getIdToCompare(res),
                                 sunResXML.getXMLPath()));
                         break;
                     }
                     //check if another existing resource conflicts with the
                     //resource being added
                     if(existingRes.isAConflict(res)){
                         //conflict within an archive
                         addResource = false;
                         conflictingResources.append("\n");
                         String message = localStrings.getString(
                                 "conflict.resource.sun.resource.xml",
                                 getIdToCompare(res),
                                 sunResXML.getXMLPath());
                         conflictingResources.append(message);
                         _logger.warning(message);
                         if(_logger.isLoggable(Level.FINE))
                             logAttributes(res);
                     }
                 }
                 if(addResource)
                     resourceSet.add(res);
             }
         }
         if(conflictingResources.toString().length() > 0){
             throw new ResourceConflictException(conflictingResources.toString());
         }
         return resourceSet;
     }
     
     /**
      * Checks if any of the Resource in the given set has a conflict with
      * resource definitions in the domain.xml. A <b> conflict </b> is defined
      * based on the type of the resource. For example, a JDBC Resource has "jndi-name"
      * that is the identifying key where as for a JDBC Connection Pool, it is
      * the "name" that must be unique.
      *
      * @param resSet a Set of Resource elements.
      * @param configContext instance of ConfigContext that you want to confirm this against. May not be null.
      *
      * @throws ResourceConflictException an exception is thrown when an archive is found to
      * have two or more resources that conflict with resources already present in domain.xml.
      * @throws ConfigException if there is any error with accessing the configuration
      * hierarchy.
      */
     public static void getResourceConflictsWithDomainXML(final List<Resource> resList,
             final ConfigContext configContext) throws ResourceConflictException, ConfigException {
         if (resList != null) {
             Iterator<Resource> iterRes = resList.iterator();
             StringBuffer conflictingResources = new StringBuffer();
             while (iterRes.hasNext()) {
                 Resource res = iterRes.next();
                 final String id = getIdToCompare(res);
                 final ConfigBean sb = ResourceHelper.findResource(configContext, id);
                 if (sb != null) {
                     conflictingResources.append("\n");
                     String message = localStrings.getString(
                             "conflict.resource.with.domain.xml",
                             getIdToCompare(res));
                     conflictingResources.append(message);
                     _logger.warning(message);
                     if(_logger.isLoggable(Level.FINE))
                         logAttributes(res);
                 }
             }
             if(conflictingResources.toString().length() > 0){
                 throw new ResourceConflictException(conflictingResources.toString());
                 
             }
         }
     }

    private static void logAttributes(Resource res) {
        StringBuffer message = new StringBuffer();
        Iterator<Attribute> attributeIter = res.getAttributes().iterator();
        while(attributeIter.hasNext()){
            Attribute attribute = attributeIter.next();
            message.append(attribute.getName());
            message.append("=");
            message.append(attribute.getValue());
            message.append(" ");
        }
        _logger.fine(localStrings.getString("resource.attributes",
                message.toString()));
    }
} 
