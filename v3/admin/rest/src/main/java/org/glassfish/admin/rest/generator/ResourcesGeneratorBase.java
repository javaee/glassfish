/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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

package org.glassfish.admin.rest.generator;

import org.glassfish.admin.rest.Constants;
import org.glassfish.api.admin.RestRedirect;
import org.glassfish.api.admin.RestRedirects;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.ConfigModel;
import org.jvnet.hk2.config.DomDocument;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Mitesh Meswani
 */
public abstract class ResourcesGeneratorBase implements ResourcesGenerator {

    private Set<String> alreadyGenerated = new HashSet<String>();

    @Override
    /**
     * Generate REST resource for a single config model.
     */
    public void generateSingle(ConfigModel model, DomDocument domDocument) {
        processRedirectsAnnotation(model);

        String serverConfigName = getLastAfterDot(model.targetTypeName);
        String beanName = getBeanName(serverConfigName);
        String className = getClassName(beanName);

        if (alreadyGenerated(className)) return;

        String baseClassName = "TemplateResource";
        String resourcePath = null;

        if (beanName.equals("Domain")) {
            baseClassName = "org.glassfish.admin.rest.resources.GlassFishDomainResource";
            resourcePath  = "domain";
        } 

        ClassWriter classWriter = getClassWriter(className, baseClassName, resourcePath);

        generateCommandResources(beanName, classWriter);

        generateGetDeleteCommandMethod(beanName, classWriter);

        for (String elementName : model.getElementNames()) {
            ConfigModel.Property childElement = model.getElement(elementName);
            if (childElement.isLeaf()) {
                if (childElement.isCollection()) {
                    //handle the CollectionLeaf config objects.
                    //JVM Options is an example of CollectionLeaf object.
                    String childResourceBeanName = getBeanName(elementName); 
                    String childResourceClassName = getClassName(childResourceBeanName);
                    classWriter.createGetChildResource(elementName, childResourceClassName);

                    //create resource class
                    generateCollectionLeafResource(childResourceBeanName);
                }
            } else {  // => !childElement.isLeaf()
                //TODO code below is not elegantly laid out. Refactor it after digesting the flow.
                /* create a method processNonLeafChildElement() {
                      which encapsulates all of below and recurses to itself as required.
                   }
                 */

                ConfigModel.Node node = (ConfigModel.Node) childElement;
                ConfigModel childModel = node.getModel();
                String childBeanName = getLastAfterDot(childModel.targetTypeName);

                if (elementName.equals("*")) {
                    List<ConfigModel> childConfigModels = null;
                    try {
                        Class<?> subType = childModel.classLoaderHolder.get().loadClass(childModel.targetTypeName);
                        childConfigModels = domDocument.getAllModelsImplementing(subType);
                    } catch (ClassNotFoundException e) {
                        throw new GeneratorException(e);
                    }
                    if (childConfigModels != null) {
                        for (ConfigModel childConfigModel : childConfigModels) {
                            processNonLeafChildConfigModel(childConfigModel, childElement, domDocument, classWriter);
                        }
                    } else {
                        processNonLeafChildConfigModel(childModel, childElement, domDocument, classWriter);
                    }
                } else { // => !childElement.isLeaf() && !elementName.equals("*")
                    if (childBeanName.equals("Property")) {
                        classWriter.createGetChildResource("property", "PropertiesBagResource");
                    } else {
                        String childResourceClassName = getClassName(childBeanName);
                        if(childElement.isCollection()) {
                            childResourceClassName = "List" + childResourceClassName;
                        }
                        classWriter.createGetChildResource(childModel.getTagName(), childResourceClassName);
                    }

                    if (childElement.isCollection()) {
                        generateList(childModel, domDocument);
                    } else {
                        generateSingle(childModel, domDocument);
                    }
                }
            }
        }

        classWriter.done();
    }

    /**
     * @param className
     * @return true if the given className is already generated. false otherwise.
     */
    private boolean alreadyGenerated(String className) {
        boolean retVal = true;
        if (!alreadyGenerated.contains(className)) {
            alreadyGenerated.add(className);
            retVal = false;
        }
        return retVal;
    }

    public void generateList(ConfigModel model, DomDocument domDocument)  {

        String serverConfigName = getLastAfterDot(model.targetTypeName);
        String beanName = getBeanName(serverConfigName);
        String className = "List" + getClassName(beanName);

        if (alreadyGenerated(className)) return;

        ClassWriter classWriter = getClassWriter(className, "TemplateListOfResource", null);

        String keyAttributeName = getKeyAttributeName(model);
        String childResourceClassName = getClassName(beanName);
        classWriter.createGetChildResourceForListResources(keyAttributeName, childResourceClassName);
        generateCommandResources("List" + beanName, classWriter);

        generateGetPostCommandMethod("List" + beanName, classWriter);

        classWriter.done();

        generateSingle(model, domDocument);

    }

    private void generateCollectionLeafResource(String beanName) {
        String className = getClassName(beanName);

        if (alreadyGenerated(className)) return;

        ClassWriter classWriter = getClassWriter(className, "CollectionLeafResource", null);

        CollectionLeafMetaData metaData = configBeanToCollectionLeafMetaData.get(beanName);

        if (metaData != null) {
            if (metaData.postCommandName != null) {
                classWriter.createGetPostCommandForCollectionLeafResource(metaData.postCommandName);
            }

            if (metaData.deleteCommandName != null ) {
                classWriter.createGetDeleteCommandForCollectionLeafResource(metaData.deleteCommandName);
            }

            //display name method
            classWriter.createGetDisplayNameForCollectionLeafResource(metaData.displayName);
        }

        classWriter.done();

    }


    /**
     * @param model
     * @return name of the key attribute for the given model.
     */
    private String getKeyAttributeName(ConfigModel model) {
        String keyAttributeName = null;
        if (model.key == null) {
            for (String s : model.getAttributeNames()) {//no key, by default use the name attr
                if (s.equals("name")) {
                    keyAttributeName = getBeanName(s);
                }
            }
            if (keyAttributeName == null)//nothing, so pick the first one
            {
                Set<String> attributeNames =  model.getAttributeNames();
                if(!attributeNames.isEmpty()) {
                    keyAttributeName = getBeanName(attributeNames.iterator().next());
                } else {
                    //TODO carried forward from old generator. Should never reach here. But we do. Need to follow up.
                    keyAttributeName = "ThisIsAModelBug:NoKeyAttr"; //no attr choice fo a key!!! Error!!!
                }

            }
        } else {
            keyAttributeName = getBeanName(model.key.substring(1, model.key.length()));
        }
        return keyAttributeName;
    }

    /**
     * //TODO think of a better name after understanding what data is processed.
     * process given childConfigModel.
     * @param childConfigModel
     * @param childElement
     * @param domDocument
     * @param classWriter
     */
    private void processNonLeafChildConfigModel(ConfigModel childConfigModel, ConfigModel.Property childElement, DomDocument domDocument, ClassWriter classWriter) {
        String childResourceClassName = getClassName("List" + getLastAfterDot(childConfigModel.targetTypeName));
        String childPath = childConfigModel.getTagName();
        classWriter.createGetChildResource(childPath, childResourceClassName);
        if (childElement.isCollection()) {
            generateList(childConfigModel, domDocument);
        } else {
            //TODO think when would the code flow come here. We are generating a "List" getter here. What would happen if the code comes here. Who would generate the List we referred to above?
            generateSingle(childConfigModel, domDocument);
        }
    }

    private void generateGetDeleteCommandMethod(String beanName, ClassWriter classWriter) {
        String commandName = configBeanToDELETECommand.get(beanName);
        if (commandName != null) {
            classWriter.createGetDeleteCommand(commandName);
        }
    }

    void generateGetPostCommandMethod(String resourceName, ClassWriter classWriter) {
        String commandName = configBeanToPOSTCommand.get(resourceName);
        if(commandName != null) {
            classWriter.createGetPostCommand(commandName);

        }
    }


    /**
     * Generate resources for commands mapped under given parentBeanName
     * @param parentBeanName
     * @param parentWriter
     */
    private void generateCommandResources(String parentBeanName, ClassWriter parentWriter)  {

        List<CommandResourceMetaData> commandMetaData = CommandResourceMetaData.getMetaData(parentBeanName);
        if(commandMetaData.size() > 0) {
            for (CommandResourceMetaData metaData : commandMetaData) {
                String commandResourceName = parentBeanName + getBeanName(metaData.resourcePath);
                String commandResourceClassName = getClassName(commandResourceName);

                //Generate command resource class
                generateCommandResourceClass(parentBeanName, metaData);

                //Generate getCommandResource() method in parent
                parentWriter.createGetCommandResource(commandResourceClassName, metaData.resourcePath);

            }
            //Generate GetCommandResourcePaths() method in parent 
            parentWriter.createGetCommandResourcePaths(commandMetaData);
        }
    }

    /**
     * Generate code for Resource class corresponding to given parentBeanName and command
     * @param parentBeanName
     * @param metaData
     */
    private void generateCommandResourceClass(String parentBeanName, CommandResourceMetaData metaData) {

        String commandResourceClassName = getClassName(parentBeanName + getBeanName(metaData.resourcePath));
        String commandName = metaData.command;
        String commandDisplayName = metaData.resourcePath;
        String httpMethod = metaData.httpMethod;
        String commandAction = metaData.displayName;
        String baseClassName;

        if (httpMethod.equals("GET")) {
            baseClassName = "org.glassfish.admin.rest.resources.TemplateCommandGetResource";
        } else if (httpMethod.equals("DELETE")) {
            baseClassName = "org.glassfish.admin.rest.resources.TemplateCommandDeleteResource";
        } else if (httpMethod.equals("POST")) {
            baseClassName = "org.glassfish.admin.rest.resources.TemplateCommandPostResource";
        } else {
            throw new GeneratorException("Invalid httpMethod specified: " + httpMethod);
        }

        ClassWriter writer = getClassWriter(commandResourceClassName, baseClassName, null);

        boolean isLinkedToParent = false;
        if(metaData.commandParams != null) {
            for(CommandResourceMetaData.ParameterMetaData parameterMeraData : metaData.commandParams) {
                if(Constants.PARENT_NAME_VARIABLE.equals(parameterMeraData.value) ) {
                    isLinkedToParent = true;
                }
            }
        }

        writer.createCommandResourceConstructor(commandResourceClassName, commandName, httpMethod, isLinkedToParent, metaData.commandParams, commandDisplayName, commandAction);

        writer.done();
    }

    /**
     * @param beanName
     * @return generated class name for given beanName
     */
    private String getClassName(String beanName) {
        return beanName + "Resource";
    }

    /**
     * @param elementName
     * @return bean name for the given element name. The name is derived by uppercasing first letter of elementName,
     *         eliminating hyphens from elementName and ppercasing letter followed by hyphen
     */
    private String getBeanName(String elementName) {
        String ret = "";
        boolean nextisUpper = true;
        for (int i = 0; i < elementName.length(); i++) {
            if (nextisUpper == true) {
                ret = ret + elementName.substring(i, i + 1).toUpperCase();
                nextisUpper = false;
            } else {
                if (elementName.charAt(i) == '-') {
                    nextisUpper = true;
                } else {
                    nextisUpper = false;
                    ret = ret + elementName.substring(i, i + 1);
                }
            }
        }
        return ret;
    }

    /**
     * TODO Rename this method to more meaningful name
     * @param input
     * @return
     */
    private String getLastAfterDot(String input) {
        return input.substring(input.lastIndexOf(".") + 1, input.length());
    }

    private void processRedirectsAnnotation(ConfigModel model) {
        Class<? extends ConfigBeanProxy> cbp = null;
        try {
            cbp = (Class<? extends ConfigBeanProxy>) model.classLoaderHolder.get().loadClass(model.targetTypeName);
            // cbp = (Class<? extends ConfigBeanProxy>)this.getClass().getClassLoader().loadClass(model.targetTypeName) ;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        RestRedirects restRedirects = cbp.getAnnotation(RestRedirects.class);
        if (restRedirects != null) {

            RestRedirect[] values = restRedirects.value();
            for (RestRedirect r : values) {
                System.out.println(r.commandName());
                System.out.println(r.opType());
            }
        }
    }

    //TODO - fetch command name from config bean(RestRedirect annotation).
    private static final Map<String, String> configBeanToDELETECommand = new HashMap<String, String>() {{
        put("ApplicationRef", "delete-application-ref");
        put("JaccProvider", "delete-jacc-provider");
        put("NetworkListener", "delete-network-listener");
        put("Property", "GENERIC-DELETE");
        put("Protocol", "delete-protocol");
        put("Transport", "delete-transport");
        put("ThreadPool", "delete-threadpool");
    }};

    //TODO - fetch command name from config bean(RestRedirect annotation).
    private static final Map<String, String> configBeanToPOSTCommand = new HashMap<String, String>()
    {{
        put("Application", "redeploy"); //TODO check : This row is not used
        put("JavaConfig", "create-profiler"); // TODO check: This row is not used
        put("ListAdminObjectResource", "create-admin-object");
        put("ListApplication", "deploy");
        put("ListAuditModule", "create-audit-module");
        put("ListAuthRealm", "create-auth-realm");
        put("ListCluster", "create-cluster");
        put("ListConnectorConnectionPool", "create-connector-connection-pool");
        put("ListConnectorResource", "create-connector-resource");
        put("ListCustomResource", "create-custom-resource");
        put("ListExternalJndiResource", "create-jndi-resource");
        put("ListExternalJndiResource", "create-jndi-resource");
        put("ListHttpListener", "create-http-listener");
        put("ListIiopListener", "create-iiop-listener");
        put("ListJdbcConnectionPool", "create-jdbc-connection-pool");
        put("ListJdbcResource", "create-jdbc-resource");
        put("ListJmsHost", "create-jms-host");
        put("ListMailResource", "create-javamail-resource");
        put("ListMessageSecurityConfig", "create-message-security-provider");
        put("ListNetworkListener", "create-network-listener");
        put("ListProtocol", "create-protocol");
        put("ListResourceAdapterConfig", "create-resource-adapter-config");
        put("ListResourceRef", "create-resource-ref");
        put("ListSystemProperty", "create-system-properties");
        put("ListThreadPool", "create-threadpool");
        put("ListTransport", "create-transport");
        put("ListVirtualServer", "create-virtual-server");
        put("ListWorkSecurityMap", "create-connector-work-security-map");
    }};

    private static class CollectionLeafMetaData {
        String postCommandName;
        String deleteCommandName;
        String displayName;

        CollectionLeafMetaData(String postCommandName, String deleteCommandName, String displayName) {
            this.postCommandName = postCommandName;
            this.deleteCommandName = deleteCommandName;
            this.displayName = displayName;
        }
    }

    //This map is used to generate CollectionLeaf resources.
    //Example: JVM Options. This information will eventually move to config bean-
    //JavaConfig or JvmOptionBag
    private static final Map<String, CollectionLeafMetaData> configBeanToCollectionLeafMetaData =
            new HashMap<String, CollectionLeafMetaData>() {{
        put("JvmOptions",new CollectionLeafMetaData("create-jvm-options", "delete-jvm-options", "JvmOption"));
    }};


}
