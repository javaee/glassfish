/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
package org.glassfish.admin.rest;

import com.sun.enterprise.config.serverbeans.Domain;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import javax.ws.rs.Path;

import org.glassfish.api.admin.CommandModel;
import org.glassfish.api.admin.CommandRunner;
import org.jvnet.hk2.config.ConfigModel;
import org.jvnet.hk2.config.Dom;
import org.jvnet.hk2.config.DomDocument;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.glassfish.api.admin.RestRedirects;
import org.glassfish.api.admin.RestRedirect;

/**
 * @author Ludovic Champenois ludo@dev.java.net
 * @author Rajeshwar Patil
 */
@Path("/generator/")
public class GeneratorResource {

    private DomDocument document;
    protected String genDir;

    /** Creates a new instance of xxxResource */
    public GeneratorResource() {
    }

    @GET
    @Produces({"text/plain"})
    public String get() {

        Domain entity = RestService.getDomain();

        File loc =
                new File(System.getProperty("user.home") + "/acvs/v3/admin/rest/src/main/java/org/glassfish/admin/rest/resources");
        loc.mkdirs();
        genDir = loc.getAbsolutePath();

        //generate date info in 1 single file
        File file = new File(genDir + "/codegeneration.properties");
        try {
            file.createNewFile();
            FileWriter fstream = new FileWriter(file);
            BufferedWriter out = new BufferedWriter(fstream);
            out.write("generation_date=" + new Date() + "\n");
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }







        //        DomDocument dodo = RestService.getHabitat().getComponent(DomDocument.class);
        //        Dom root = dodo.getRoot();
        // System.out.println(" root "+ root );
        Dom dom1 = Dom.unwrap(entity);
        Dom root = dom1.document.getRoot();
        document = dom1.document;

        ConfigModel rootModel = root.model;


        //+ Domain domain = Domain.class.cast(RestService.getHabitat().getComponent(Domain.class.getName(), ""));
        try {
            generateSingle(rootModel);
        } catch (Exception ex) {
            Logger.getLogger(GeneratorResource.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "Code Generation done at : " + genDir;

    }

    private void processRedirectsAnnotation(ConfigModel model) {

        Class<? extends ConfigBeanProxy> cbp = null;
        System.out.println("\n\nAnnotation" + model.targetTypeName);
        try {
            cbp = (Class<? extends ConfigBeanProxy>) model.classLoaderHolder.get().loadClass(model.targetTypeName);
            // cbp = (Class<? extends ConfigBeanProxy>)this.getClass().getClassLoader().loadClass(model.targetTypeName) ;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        System.out.println("re Annotation" + model.targetTypeName);
        RestRedirects restRedirects = cbp.getAnnotation(RestRedirects.class);
        System.out.println("re Annotation restRedirects" + restRedirects);
        if (restRedirects != null) {
            System.out.println("LUDO: NOT NULL                Annotation restRedirects" + restRedirects);

            RestRedirect[] values = restRedirects.value();
            for (RestRedirect r : values) {
                System.out.println(r.commandName());
                System.out.println(r.opType());
            }
        }


    }


    private void genHeader(BufferedWriter out) throws IOException {
        out.write("/*\n");
        out.write(" * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.\n");
        out.write(" *\n");
        out.write(" * Copyright 2009 Sun Microsystems, Inc. All rights reserved.\n");
        out.write(" *\n");
        out.write(" * The contents of this file are subject to the terms of either the GNU\n");
        out.write(" * General Public License Version 2 only (\"GPL\") or the Common Development\n");
        out.write(" * and Distribution License(\"CDDL\") (collectively, the \"License\").  You\n");
        out.write(" * may not use this file except in compliance with the License. You can obtain\n");
        out.write(" * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html\n");
        out.write(" * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific\n");
        out.write(" * language governing permissions and limitations under the License.\n");
        out.write(" *\n");
        out.write(" * When distributing the software, include this License Header Notice in each\n");
        out.write(" * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.\n");
        out.write(" * Sun designates this particular file as subject to the \"Classpath\" exception\n");
        out.write(" * as provided by Sun in the GPL Version 2 section of the License file that\n");
        out.write(" * accompanied this code.  If applicable, add the following below the License\n");
        out.write(" * Header, with the fields enclosed by brackets [] replaced by your own\n");
        out.write(" * identifying information: \"Portions Copyrighted [year]\n");
        out.write(" * [name of copyright owner]\"\n");
        out.write(" *\n");
        out.write(" * Contributor(s):\n");
        out.write(" *\n");
        out.write(" * If you wish your version of this file to be governed by only the CDDL or\n");
        out.write(" * only the GPL Version 2, indicate your decision by adding \"[Contributor]\n");
        out.write(" * elects to include this software in this distribution under the [CDDL or GPL\n");
        out.write(" * Version 2] license.\"  If you don't indicate a single choice of license, a\n");
        out.write(" * recipient has the option to distribute your version of this file under\n");
        out.write(" * either the CDDL, the GPL Version 2 or to extend the choice of license to\n");
        out.write(" * its licensees as provided above.  However, if you add GPL Version 2 code\n");
        out.write(" * and therefore, elected the GPL Version 2 license, then the option applies\n");
        out.write(" * only if the new code is made subject to such option by the copyright\n");
        out.write(" * holder.\n");
        out.write(" */\n");
    }
    private HashMap<String, String> genSingleFiles = new HashMap<String, String>();
    private HashMap<String, String> genListFiles = new HashMap<String, String>();
    private HashMap<String, String> genCommandResourceFiles = new HashMap<String, String>();

    public void generateList(ConfigModel model) throws IOException {

        String serverConfigName = model.targetTypeName.substring(model.targetTypeName.lastIndexOf(".") + 1,
                model.targetTypeName.length());

        if (genListFiles.containsKey(serverConfigName)) {
            return;
        }
        genListFiles.put(serverConfigName, serverConfigName);
        String beanName = getBeanName(serverConfigName);
        File file = new File(genDir + "/List" + beanName + "Resource.java");
        // File file = new File("/Users/ludo/tmp/" + beanName + "Resource.java");
        try {
            file.createNewFile();
        } catch (Exception e) {
            Logger.getLogger(GeneratorResource.class.getName()).log(Level.SEVERE, e.getMessage());
        }




        FileWriter fstream = new FileWriter(file);
        BufferedWriter out = new BufferedWriter(fstream);
        genHeader(out);
        out.write("package org.glassfish.admin.rest.resources;\n");
        out.write("import javax.ws.rs.*;\n");
//        out.write("import org.glassfish.admin.rest.TemplateResource;\n");
        out.write("import org.glassfish.admin.rest.TemplateListOfResource;\n");
//        out.write("import com.sun.jersey.api.core.ResourceContext;\n");
        out.write("import " + model.targetTypeName + ";\n");



        // out.write("@Path(\"/" + serverConfigName + "/\")\n");
        out.write("public class List" + beanName + "Resource extends TemplateListOfResource<" + beanName + "> {\n\n");

////        out.write("private List<" + beanName + "> entity;\n");
////        out.write("public void setEntity(List<" + beanName + "> p ){\n");
////        out.write("entity = p;\n");
////        out.write("}\n");
////
////
////        out.write("public  List<" + beanName + "> getEntity() {\n");
////        if (beanName.equals("Domain")) {
////            out.write("return RestService.theDomain;\n");
////        } else {
////            out.write("return entity;\n");
////
////        }
////        out.write("}\n");
        String keyAttributeName = null;
        if (model.key == null) {
            try {
                for (String s : model.getAttributeNames()) {//no key, by default use the name attr
                    if (s.equals("name")) {
                        keyAttributeName = getBeanName(s);
                    }
                }
                if (keyAttributeName == null)//nothing, so pick the ifrst one
                {
                    keyAttributeName = getBeanName(model.getAttributeNames().iterator().next());
                }
            } catch (Exception e) {
                keyAttributeName = "ThisIsAModelBug:NoKeyAttr"; //no attr choice fo a key!!! Error!!!
                } //firstone
            } else {
            keyAttributeName = getBeanName(model.key.substring(1, model.key.length()));
        }
        out.write("\n");
        out.write("\t@Path(\"{" + keyAttributeName + "}/\")\n");
        out.write("\tpublic " + beanName + "Resource get" + beanName + "Resource(@PathParam(\"" + keyAttributeName + "\") String id) {\n");
        out.write("\t\t" + beanName + "Resource resource = resourceContext.getResource(" + beanName + "Resource.class);\n");
        out.write("\t\tfor (" + beanName + " c: entity){\n");
        if (model.key == null) {
            out.write("//THIS KEY IS THE FIRST Attribute ONE ludo\n");

        }

        out.write("\t\t\t//Using '-' for back-slash in resource names\n");
        out.write("\t\t\t//For example, jndi names has back-slash in it.\n");
        out.write("\t\t\tif(c.get" + keyAttributeName + "().replace('/', '-').equals(id)){\n");
        out.write("\t\t\t\tresource.setEntity(c);\n");
        out.write("\t\t\t}\n");
        out.write("\t\t}\n");
        out.write("\t\treturn resource;\n");
        out.write("\t}\n\n");
 ///////ludo       generateCommand("List" + beanName, out);
        generateCommandResources("List" + beanName, out);

        out.write("\n");
        generateGetPostCommandMethod("List" + beanName, out);

        out.write("}\n");

        out.close();
        System.out.println("created:" + file.getAbsolutePath());

        generateSingle(model);


    }

    public void generateSingle(ConfigModel model) throws IOException {
        processRedirectsAnnotation(model);

        String serverConfigName = model.targetTypeName.substring(model.targetTypeName.lastIndexOf(".") + 1,
                model.targetTypeName.length());

        if (genSingleFiles.containsKey(serverConfigName)) {
            return;
        }
        genSingleFiles.put(serverConfigName, serverConfigName);
        String beanName = getBeanName(serverConfigName);
        File file = new File(genDir + "/" + beanName + "Resource.java");
        // File file = new File("/Users/ludo/tmp/" + beanName + "Resource.java");
        try {
            file.createNewFile();
        } catch (Exception e) {
            Logger.getLogger(GeneratorResource.class.getName()).log(Level.SEVERE, e.getMessage());
        }




        FileWriter fstream = new FileWriter(file);
        BufferedWriter out = new BufferedWriter(fstream);
        genHeader(out);
        out.write("package org.glassfish.admin.rest.resources;\n");
        out.write("import javax.ws.rs.*;\n");
//        out.write("import java.util.*;\n");
        out.write("import org.glassfish.admin.rest.TemplateResource;\n");
//        out.write("import org.glassfish.admin.rest.TemplateListOfResource;\n");
//        out.write("import com.sun.jersey.api.core.ResourceContext;\n");
        out.write("import " + model.targetTypeName + ";\n");



        if (beanName.equals("Domain")) {
            out.write("@Path(\"/" + "domain" + "/\")\n");
        }

        out.write("public class " + beanName + "Resource extends TemplateResource<" + beanName + "> {\n\n");

//        if (!beanName.equals("Domain")) {
//            out.write("private " + beanName + " entity;\n");
//            out.write("public void setEntity(" + beanName + " p ){\n");
//            out.write("entity = p;\n");
//            out.write("}\n");
//        }

        if (beanName.equals("Domain")) {
            out.write("@Override public " + beanName + " getEntity() {\n");
            out.write("return org.glassfish.admin.rest.RestService.getDomain();\n");
            out.write("}\n");
        }

     /////ludo   generateCommand(beanName, out);

        generateCommandResources(beanName, out);

        generateGetDeleteCommandMethod(beanName, out);

        Set<String> elem = model.getElementNames();

        for (String a : elem) {
            System.out.println("a= " + a);



            ConfigModel.Property prop = model.getElement(a);


            if (prop != null && prop.isLeaf()) {
                System.out.println("proxy.getElement(a).isLeaf() " + a);
                if (prop.isCollection()) {
                    //handle the CollectionLeaf config objects.
                    //JVM Options is an example of CollectionLeaf object.
                    String name = getBeanName(a);
                    out.write("\t@Path(\"" + a + "/\")\n");
                    out.write("\tpublic " + name + "Resource get" + name + "Resource() {\n");

                    out.write("\t\t" + name + "Resource resource = resourceContext.getResource(" + name + "Resource.class);\n");
                    out.write("\t\tresource.setEntity(getEntity().get" + name + "() );\n");
                    out.write("\t\treturn resource;\n");
                    out.write("\t}\n");

                    //create resource class
                    createCollectionLeafResourceFile(name);
                }
                System.out.println("proxy.getElement(a).isCollection() " + a);
            } else {
                ConfigModel.Node node = (ConfigModel.Node) prop;
                //String childbeanName = getBeanName(a);

                ConfigModel childModel = node.getModel();

                String getterName = getBeanName(a);

                System.out.println("Model.targetTypeName" + model.targetTypeName);
                System.out.println("newModel.targetTypeName" + childModel.targetTypeName);
                System.out.println("ConfigModel.Node node isCollection=" + prop.isCollection());
                System.out.println("ConfigModel.Node node isLeaf=" + prop.isLeaf());
                System.out.println("ConfigModel.Node node xlmname=" + prop.xmlName());
                if (childModel.targetTypeName.endsWith("Named")) {
                    a = "application";
                    getterName = "Applications";
                    try {
                        Class<?> subType = childModel.classLoaderHolder.get().loadClass(childModel.targetTypeName); ///  a shoulf be the typename


                        List<ConfigModel> lcm = document.getAllModelsImplementing(subType);
                        if (lcm != null) {
                            for (ConfigModel childmodel : lcm) {
                                System.out.println("***childmodel.targetTypeName" + childmodel.targetTypeName);
                                if (childmodel.targetTypeName.equals("com.sun.enterprise.config.serverbeans.Application")) {
                                    childModel = childmodel;
                                }
                            }
                        }
                    } catch (Exception e) {
                        Logger.getLogger(GeneratorResource.class.getName()).log(Level.SEVERE, e.getMessage());
                    }


                }

                String childbeanName = childModel.targetTypeName.substring(childModel.targetTypeName.lastIndexOf(".") + 1,
                        childModel.targetTypeName.length());

                if (!childModel.targetTypeName.endsWith("Resource")) {
                    String prefix = "";
                    if (prop.isCollection()) {
                        prefix = "List";
                    }

                    if (a.equals("*")) {
                        getterName = childbeanName + "s";
                    }

                    out.write("\t@Path(\"" + childModel.getTagName() + "/\")\n");
                    out.write("\tpublic " + prefix + childbeanName + "Resource get" + childbeanName + "Resource() {\n");

                    out.write("\t\t" + prefix + childbeanName + "Resource resource = resourceContext.getResource(" + prefix + childbeanName + "Resource.class);\n");
                    out.write("\t\tresource.setEntity(getEntity().get" + getterName + "() );\n");
                    out.write("\t\treturn resource;\n");
                    out.write("\t}\n");

                    if (prop.isCollection()) {
                        generateList(childModel);
                    } else {
                        generateSingle(childModel);

                    }


                } else {




                    try {
                        Class<?> subType = childModel.classLoaderHolder.get().loadClass(childModel.targetTypeName); ///  a shoulf be the typename


                        List<ConfigModel> lcm = document.getAllModelsImplementing(subType);
                        if (lcm != null) {
                            for (ConfigModel childmodel : lcm) {
                                System.out.println("--->targetTypeName=" + childmodel.targetTypeName);
                                String newName = childmodel.targetTypeName.substring(childmodel.targetTypeName.lastIndexOf(".") + 1,
                                        childmodel.targetTypeName.length());
                                out.write("@Path(\"" + childmodel.getTagName() + "/\")\n");
                                out.write("public List" + newName + "Resource get" + newName + "Resource() {\n");
                                out.write("List" + newName + "Resource resource = resourceContext.getResource(List" + newName + "Resource.class);\n");
                                out.write("java.util.List<com.sun.enterprise.config.serverbeans.Resource> l = entity.getResources();\n");
                                out.write("java.util.List<" + childmodel.targetTypeName + "> newList = new java.util.ArrayList();\n");
                                out.write("for (com.sun.enterprise.config.serverbeans.Resource r: l){\n");
                                out.write("try {\n");
                                out.write("if (r instanceof " + childmodel.targetTypeName + ") {\n");
                                out.write("newList.add((" + childmodel.targetTypeName + ")r);\n");
                                out.write("}\n");
                                out.write("} catch (Exception e){\n");

                                out.write("}\n");
                                out.write("}\n");
                                out.write("resource.setEntity(newList );\n");
                                out.write("return resource;\n");
                                out.write("}\n\n\n");
                                generateList(childmodel);



                            }
                        }

                        //com.sun.enterprise.config.serverbeans.CustomResource

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }


            }

        }


        out.write("}\n");

        out.close();
        System.out.println("created:" + file.getAbsolutePath());


    }

 

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


    void generateGetPostCommandMethod(String resourceName, BufferedWriter out) throws IOException {
        String commandName = getPostCommandName(resourceName);
        out.write("@Override\n");
        out.write("public String getPostCommand() {\n");
        if (commandName != null) {
            out.write("\treturn \"" + commandName + "\";\n");
        } else {
            out.write("\treturn " + commandName + ";\n");
        }
        out.write("}\n");       
        }


    private String getPostCommandName(String resourceName) {
        //FIXME - fetch command name from config bean(RestRedirect annotation).
        for (int i = 0; i < MappingConfigBeansToPOSTCommands.length; i++) {
            if (resourceName.equals(MappingConfigBeansToPOSTCommands[i][0])) {
                    return MappingConfigBeansToPOSTCommands[i][1];
            }
        }
        return null; //POST is not mapped to any create command for this resource
    }


    void generateGetDeleteCommandMethod(String resourceName, BufferedWriter out) throws IOException {
        String commandName = getDeleteCommandName(resourceName);
        if (commandName != null) {
            out.write("@Override\n");
            out.write("public String getDeleteCommand() {\n");
            out.write("\treturn \"" + commandName + "\";\n");
            out.write("}\n");
        }
    }


    private String getDeleteCommandName(String resourceName) {
        //FIXME - fetch command name from config bean(RestRedirect annotation).
        //This delete commands meta-data witll move to com.sun.grizzly.config.dom.* config beans
        for (int i = 0; i < MappingConfigBeansToDELETECommands.length; i++) {
            if (resourceName.equals(MappingConfigBeansToDELETECommands[i][0])) {
                    return MappingConfigBeansToDELETECommands[i][1];
            }
        }
        return null;
    }


    private String[] getCollectionLeafResourceInfo(String resourceName) {
        for (int i = 0; i < ConfigBeansToCommands.length; i++) {
            if (resourceName.equals(ConfigBeansToCommands[i][0])) {
                    return ConfigBeansToCommands[i];
            }
        }
        return null;
    }


    /*
     * temporary mapping to add Admin Commands to some of our configbeans
     *
     * */
    
 /*   private static String MappingConfigBeansToCommands[][] = {
        {"Domain", "stop-domain"},
        {"Domain", "restart-domain"},
        {"Domain", "uptime"},
        {"Domain", "version"},
        {"Domain", "rotate-log"},
        {"Domain", "get-host-and-port"},
        {"ListApplication", "deploy"},
        {"Application", "redeploy"},
        //  {"ListExternalJndiResource", ""},
        //{"ListWorkSecurityMap", ""},
        //{"ListPersistenceManagerFactoryResource", ""},

        //{"", "create-connector-work-security-map"},
        //  {"", "create-file-user"},
        //    {"", "create-jmsResource"},
        //    {"", "create-jmsdest"},
        //   {"", "create-jvm-options"},
        //    {"", "create-password-alias"},
        ////    {"", "create-ssl"},
        {"ConnectionPool", "ping-connection-pool"},/*
    addResources
    change-admin-password
    disable
    enable
    generate-jvm-report
    get
    get-client-stubs
    // get-host-and-port
    //redeploy
    //undeploy
    // ping-connection-pool

    // create-admin-object
    //create-connector-connection-pool
    // create-connectorResource
    //create-customResource
    //create-javamailResource
    //create-jdbc-connection-pool
    //create-jdbcResource
    // createResource-adapter-config
    delete-admin-object
    delete-audit-module
    delete-auth-realm
    delete-connector-connection-pool
    delete-connectorResource
    delete-connector-work-security-map
    delete-customResource
    delete-file-user
    delete-http-listener
    delete-iiop-listener
    delete-javamailResource
    delete-jdbc-connection-pool
    delete-jdbcResource
    delete-jms-host
    monitor
    delete-jmsResource
    delete-jmsdest
    delete-jvm-options
    delete-message-security-provider
    delete-password-alias
    delete-profiler
    deleteResource-adapter-config
    deleteResource-ref
    update-file-user
    delete-ssl
    update-password-alias
    delete-system-property
    delete-virtual-server


     };
*/

    private static String MappingConfigBeansToPOSTCommands[][] = {
        {"ListApplication", "deploy"},
        {"Application", "redeploy"},
        {"ListAdminObjectResource", "create-admin-object"},
        {"ListCustomResource", "create-custom-resource"},
        {"ListJdbcResource", "create-jdbc-resource"},
        {"ListJdbcConnectionPool", "create-jdbc-connection-pool"},
        {"ListConnectorResource", "create-connector-resource"},
        {"ListMailResource", "create-javamail-resource"},
        {"ListResourceAdapterConfig", "create-resource-adapter-config"},
        {"ListConnectorConnectionPool", "create-connector-connection-pool"},
        {"ListAuthRealm", "create-auth-realm"},
        {"ListAuditModule", "create-audit-module"},
        {"ListHttpListener", "create-http-listener"},
        {"ListIiopListener", "create-iiop-listener"},
        {"ListNetworkListener", "create-network-listener"},
        {"ListTransport", "create-transport"},
        {"ListProtocol", "create-protocol"},
        {"ListJmsHost", "create-jms-host"},
        {"ListMessageSecurityConfig", "create-message-security-provider"},
        {"JavaConfig", "create-profiler"},
        {"ListResourceRef", "create-resource-ref"},
        {"ListSystemProperty", "create-system-properties"},
        {"ListVirtualServer", "create-virtual-server"},
        {"ListThreadPool", "create-threadpool"}
     };


    private static String MappingConfigBeansToDELETECommands[][] = {
        {"Transport", "delete-transport"},
        {"ThreadPool", "delete-threadpool"},
        {"NetworkListener", "delete-network-listener"},
        {"Protocol", "delete-protocol"}
    };


    //This map is used to generate CollectionLeaf resources.
    //Example: JVM Options. This information will eventually move to config bean-
    //JavaConfig or JvmOptionBag
    private static String ConfigBeansToCommands[][] = {
        //{config-bean, post command, delete command, disaplay name}
        {"JvmOptions", "create-jvm-options", "delete-jvm-options", "JvmOption"}
    };


    private static String ConfigBeansToCommandResourcesMap[][] = {
        //{config-bean, command, method, resource-path, command-action, command-params...}
        {"Domain", "stop-domain", "POST", "stop", "Stop"},
        {"Domain", "restart-domain", "POST", "restart", "Restart"},
        {"Domain", "uptime", "GET", "uptime", "Uptime"},
        {"Domain", "version", "GET", "version", "Version"},
        {"Domain", "rotate-log", "POST", "rotate-log", "RotateLog"},
        {"Domain", "get-host-and-port", "GET", "host-port", "HostPort"},
        ///{"ListApplication", "deploy"},
        ///{"Application", "redeploy"},
        {"Application", "enable", "POST", "enable", "Enable", "id=$parent"},
        {"Application", "disable", "POST", "disable", "Disable", "id=$parent"},
        {"ConnectionPool", "ping-connection-pool", "GET", "ping", "Ping"},
        {"IiopService", "create-ssl", "POST", "create-ssl", "Create", "type=iiop-service"},
        {"IiopService", "delete-ssl", "DELETE", "delete-ssl", "Delete", "type=iiop-service"},
        {"IiopListener", "create-ssl", "POST", "create-ssl", "Create", "id=$parent", "type=iiop-listener"},
        {"IiopListener", "delete-ssl", "DELETE", "delete-ssl", "Delete", "id=$parent", "type=iiop-listener"},
        {"AuthRealm", "create-file-user", "POST", "create-user", "Create", "authrealmname=$parent"},
        {"AuthRealm", "delete-file-user", "DELETE", "delete-user", "Delete", "authrealmname=$parent"},
        {"AuthRealm", "list-file-users", "GET", "list-users", "List", "authrealmname=$parent"},
        {"NetworkListener", "create-ssl", "POST", "create-ssl", "Create", "id=$parent", "type=http-listener"},
        {"NetworkListener", "delete-ssl", "DELETE", "delete-ssl", "Delete", "id=$parent", "type=http-listener"},
        {"Protocol", "create-http", "POST", "create-http", "Create", "id=$parent"},
        {"Protocol", "delete-http", "DELETE", "delete-http", "Delete", "id=$parent"}
    };


    private void generateCommandResources(String resourceName, 
        BufferedWriter out) throws IOException {

        if (genCommandResourceFiles.containsKey(resourceName)) {
            return;
        }
        genCommandResourceFiles.put(resourceName, resourceName);


        String commandResourcesPaths = "{";
        for (int i = 0; i < ConfigBeansToCommandResourcesMap.length; i++) {
            if (resourceName.equals(ConfigBeansToCommandResourcesMap[i][0])) {
                if (commandResourcesPaths.length() > 1) {
                    commandResourcesPaths = commandResourcesPaths + ", ";
                }
                commandResourcesPaths = commandResourcesPaths + "{" +
                    "\"" + ConfigBeansToCommandResourcesMap[i][3] + "\"" + ", " +
                        "\"" + ConfigBeansToCommandResourcesMap[i][2] + "\"" + "}";

                String commandResourceFileName = genDir + "/" + resourceName +
                    getBeanName(ConfigBeansToCommandResourcesMap[i][3]) +
                        "Resource.java";
                String commandResourceName = resourceName +
                    getBeanName(ConfigBeansToCommandResourcesMap[i][3]) +
                        "Resource";

                //generate command resource for the resource- resourceName
                createCommandResourceFile(commandResourceFileName,
                    commandResourceName, ConfigBeansToCommandResourcesMap[i]);

                //define method with @Path in resource- resourceName
                out.write("@Path(\"" + ConfigBeansToCommandResourcesMap[i][3] + "/\")\n");
                out.write("public " + commandResourceName + " get" +
                    commandResourceName + "() {\n");
                out.write(commandResourceName + " resource = resourceContext.getResource(" + commandResourceName + ".class);\n");
                out.write("return resource;\n");
                out.write("}\n\n");
            }
        }
        commandResourcesPaths = commandResourcesPaths + "}";

        //define method to return command resource paths. only if needed
        if (!commandResourcesPaths.equals("{}")){
        out.write("@Override\n");
        out.write("public String[][] getCommandResourcesPaths() {\n");
        out.write("return new String[][]" +  commandResourcesPaths + ";\n");
        out.write("}\n\n");
        }
    }

    private void createCommandResourceFile(String commandResourceFileName,
        String commandResourceName, String [] configBeansToCommandResourcesArray)
            throws IOException {
        String resourceName = configBeansToCommandResourcesArray[0] +
            getBeanName(configBeansToCommandResourcesArray[3]);
        String commandName = configBeansToCommandResourcesArray[1];
        String commandDisplayName = configBeansToCommandResourcesArray[3];
        String commandMethod = configBeansToCommandResourcesArray[2];
        String commandAction = configBeansToCommandResourcesArray[4];

        File file = new File(commandResourceFileName);
        try {
            file.createNewFile();
        } catch (Exception e) {
            Logger.getLogger(GeneratorResource.class.getName()).log(Level.SEVERE, e.getMessage());
        }

        FileWriter fstream = new FileWriter(file);
        BufferedWriter out = new BufferedWriter(fstream);

        //header
        genHeader(out);

        //package
        out.write("package org.glassfish.admin.rest.resources;\n\n");

        //imports
        out.write("import java.util.HashMap;\n\n");
        out.write("import javax.ws.rs.*;\n");
        out.write("import javax.ws.rs.core.Context;\n");
        out.write("import javax.ws.rs.core.HttpHeaders;\n");
        out.write("import javax.ws.rs.core.MediaType;\n");
        out.write("import javax.ws.rs.core.Response;\n");
        out.write("import javax.ws.rs.core.UriInfo;\n\n");
        out.write("import com.sun.enterprise.util.LocalStringManagerImpl;\n\n");
        out.write("import org.glassfish.admin.rest.provider.OptionsResult;\n");
        out.write("import org.glassfish.admin.rest.provider.MethodMetaData;\n");
        if (commandMethod.equals("GET")) {
            out.write("import org.glassfish.admin.rest.provider.StringResult;\n");
        }
        out.write("import org.glassfish.admin.rest.Constants;\n");
        out.write("import org.glassfish.admin.rest.ResourceUtil;\n");
        out.write("import org.glassfish.admin.rest.RestService;\n");
        out.write("import org.glassfish.api.ActionReport;\n\n");

        //class header
        out.write("public class " + commandResourceName + " {\n\n");

        //constructor
        out.write("public " + commandResourceName + "() {\n");
        out.write("__resourceUtil = new ResourceUtil();\n");
        out.write("}\n");

        //create command method
        if (commandMethod.equals("GET")) {
            //get method
            createCommandGetMethod(commandName, commandMethod, out);

            //create options method
            createCommandOptionsMethod(out, Constants.QUERY_PARAMETER);
        } else {
            //post, put or delete method
            createCommandMethod(commandMethod, out);

            //get method
            createGetMethod(out);

            //create options method
            createCommandOptionsMethod(out, Constants.MESSAGE_PARAMETER);
        }


        //variable declarations
        out.write("public final static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(ResourceUtil.class);\n");
        out.write("@Context\n");
        out.write("protected HttpHeaders requestHeaders;\n");
        out.write("@Context\n");
        out.write("protected UriInfo uriInfo;\n\n");
        out.write("private static final String resourceName = \"" + resourceName + "\";\n");
        out.write("private static final String commandName = \"" + commandName + "\";\n");
        out.write("private static final String commandDisplayName = \"" + commandDisplayName + "\";\n");
        out.write("private static final String commandMethod = \"" + commandMethod + "\";\n");
        out.write("private static final String commandAction = \"" + commandAction + "\";\n");

        boolean isLinkedToParent = false;
        if (configBeansToCommandResourcesArray.length > 5 ) {
            out.write("private HashMap<String, String> commandParams =\n");
            out.write("new HashMap<String, String>() {{\n");
            for (int i=5; i <= configBeansToCommandResourcesArray.length - 1; i++) {
                String[] name_value = stringToArray(configBeansToCommandResourcesArray[i], "=");
                if (name_value[1].equals(Constants.PARENT_NAME_VARIABLE)) {
                    isLinkedToParent = true;
                }
                out.write("put(\"" + name_value[0] + "\",\"" + name_value[1] + "\");\n");
            }

            out.write("}};\n");
        } else {
            out.write("private HashMap<String, String> commandParams = null;\n");
        }
        out.write("private static final boolean isLinkedToParent = " + isLinkedToParent + ";\n");
        out.write("private ResourceUtil __resourceUtil;\n");
        out.write("}\n");

        out.close();
        System.out.println("created:" + file.getAbsolutePath());
    }


    private void createCollectionLeafResourceFile(String beanName) throws IOException {
        String resourceFileName = genDir + "/" + beanName + "Resource.java";
        String resourceName = beanName + "Resource";

        File file = new File(resourceFileName);
        try {
            file.createNewFile();
        } catch (Exception e) {
            Logger.getLogger(GeneratorResource.class.getName()).log(Level.SEVERE, e.getMessage());
        }
        FileWriter fstream = new FileWriter(file);
        BufferedWriter out = new BufferedWriter(fstream);

        //header
        genHeader(out);

        //package
        out.write("package org.glassfish.admin.rest.resources;\n\n");

        //imports
        out.write("import org.glassfish.admin.rest.CollectionLeafResource;\n\n");

        //class header
        out.write("public class " + resourceName + " extends CollectionLeafResource {\n\n");

        String[] collectionLeafResourceInfo = getCollectionLeafResourceInfo(beanName);

        if (collectionLeafResourceInfo != null) {
            //post method
            if ((collectionLeafResourceInfo[1] != null) && (!collectionLeafResourceInfo[1].equals(""))) {
                out.write("@Override\n");
                out.write("protected String getPostCommand(){\n");
                out.write("return \"" + collectionLeafResourceInfo[1] + "\";\n");
                out.write("}\n");
            }

            //delete method
            if ((collectionLeafResourceInfo[2] != null) && (!collectionLeafResourceInfo[2].equals(""))) {
                out.write("@Override\n");
                out.write("protected String getDeleteCommand(){\n");
                out.write("return \"" + collectionLeafResourceInfo[2] + "\";\n");
                out.write("}\n");
            }

            //display name method
            out.write("@Override\n");
            out.write("protected String getName(){\n");
            out.write("return \"" + collectionLeafResourceInfo[3] + "\";\n");
            out.write("}\n");
        }

        out.write("}\n");
        out.close();
    }


    private void createCommandMethod(String commandMethod, BufferedWriter out) throws IOException {
        out.write("@" + commandMethod + "\n");
        out.write("@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_FORM_URLENCODED})\n");
        out.write("public Response executeCommand(HashMap<String, String> data) {\n");
        out.write("try {\n");
        out.write("if (data.containsKey(\"error\")) {\n");
        out.write("String errorMessage = localStrings.getLocalString(\"rest.request.parsing.error\", \"Unable to parse the input entity. Please check the syntax.\");\n");
        out.write("return __resourceUtil.getResponse(400, /*parsing error*/\n errorMessage, requestHeaders, uriInfo);\n");
        out.write("}\n\n");

        out.write("if (commandParams != null) {\n");
        out.write("//formulate parent-link attribute for this command resource\n");
        out.write("//Parent link attribute may or may not be the id/target attribute\n");
        out.write("if (isLinkedToParent) {\n");
        out.write("__resourceUtil.resolveParentParamValue(commandParams, uriInfo);\n");
        out.write("}\n\n");
        out.write("data.putAll(commandParams);\n");
        out.write("}\n\n");
        out.write("__resourceUtil.adjustParameters(data);\n\n");
        out.write("__resourceUtil.purgeEmptyEntries(data);\n\n");

        out.write("ActionReport actionReport = __resourceUtil.runCommand(commandName, data, RestService.getHabitat());\n\n");
        out.write("ActionReport.ExitCode exitCode = actionReport.getActionExitCode();\n\n");

        out.write("if (exitCode == ActionReport.ExitCode.SUCCESS) {\n");
        out.write("String successMessage = localStrings.getLocalString(\"rest.request.success.message\",\n");
        out.write("\"{0} of {1} executed successfully.\", new Object[] {commandMethod, uriInfo.getAbsolutePath()});\n");
        out.write("return __resourceUtil.getResponse(200, /*200 - ok*/\n successMessage, requestHeaders, uriInfo);\n");
        out.write("}\n\n");

        out.write("String errorMessage = actionReport.getMessage();\n");
        out.write("return __resourceUtil.getResponse(400, /*400 - bad request*/\n errorMessage, requestHeaders, uriInfo);\n");
        out.write("} catch (Exception e) {\n");
        out.write("throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);\n");
        out.write("}\n");
        out.write("}\n");

        out.write("//Handle POST request without any entity(input).\n");
        out.write("//Do not care what the Content-Type is.\n");
        out.write("@" + commandMethod + "\n");
        out.write("public Response executeCommand() {\n");
        out.write("try {\n");
        out.write("return executeCommand(new HashMap<String, String>());\n");
        out.write("} catch (Exception e) {\n");
        out.write("throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);\n");
        out.write("}\n");
        out.write("}\n");


        //hack-1 : support delete method for html
        //Currently, browsers do not support delete method. For html media,
        //delete operations can be supported through POST. Redirect html
        //client POST request for delete operation to DELETE method.

        //In case of delete command reosurce, we will also create post method
        //which simply forwards the request to delete method. Only in case of
        //html client delete request is routed through post. For other clients
        //delete request is directly handled by delete method.
        if (commandMethod.equals("DELETE")) {
            out.write("//hack-1 : support delete method for html\n");
            out.write("//Currently, browsers do not support delete method. For html media,\n");
            out.write("//delete operations can be supported through POST. Redirect html\n");
            out.write("//client POST request for delete operation to DELETE method.\n\n");

            out.write("//In case of delete command reosurce, we will also create post method\n");
            out.write("//which simply forwards the request to delete method. Only in case of\n");
            out.write("//html client delete request is routed through post. For other clients\n");
            out.write("//delete request is directly handled by delete method.\n");

            out.write("@" + "POST" + "\n");
            out.write("@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_FORM_URLENCODED})\n");
            out.write("public Response hack(HashMap<String, String> data) {\n");
            out.write("if ((data.containsKey(\"operation\")) &&\n");
            out.write("(data.get(\"operation\").equals(\"__deleteoperation\"))) {\n");
            out.write("data.remove(\"operation\");\n");
            out.write("}\n");
            out.write("return executeCommand(data);\n");
            out.write("}\n");
        }
    }


    private void createGetMethod(BufferedWriter out)
                throws IOException {
        out.write("@" + "GET" + "\n");
        out.write("@Produces({MediaType.TEXT_HTML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})\n");
        out.write("public org.glassfish.admin.rest.provider.CommandResourceGetResult get() {\n");
        out.write("try {\n");
        out.write("return new org.glassfish.admin.rest.provider.CommandResourceGetResult(resourceName, commandName, commandDisplayName, commandMethod, commandAction, options());\n");
        out.write("} catch (Exception e) {\n");
        out.write("throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);\n");
        out.write("}\n");
        out.write("}\n");
    }


    private void createCommandGetMethod(String commandName,
            String commandMethod, BufferedWriter out) throws IOException {
        CommandRunner cr = RestService.getHabitat().getComponent(CommandRunner.class);
        CommandModel cm = null;
        try {
            cm = cr.getModel(commandName, RestService.logger);
        } catch (Exception e) {
            System.out.println("Error - Command Unknown: " + commandName);
            return;
        }
        if (cm == null) {
            System.out.println("Error - Command Unknown: " + commandName);
            return;
        }
        java.util.Collection<CommandModel.ParamModel> params = cm.getParameters();

        out.write("@" + commandMethod + "\n");//commandMethod - GET
        out.write("@Produces({MediaType.TEXT_HTML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_FORM_URLENCODED})\n");
        out.write("public StringResult executeCommand(\n");
        boolean first = true;
        for (CommandModel.ParamModel pm : params) {
            if (first == false) {
                out.write(" ,\n");
            }
            first = false;
            out.write("\t @QueryParam(\"" + pm.getName() + "\") ");
            out.write(" @DefaultValue(\"" + pm.getParam().defaultValue() + "\") ");
            out.write(" String " + getBeanName(pm.getName()) + " \n");
        }
        out.write(" \t) {\n");

        out.write("try {\n");
        out.write("\tjava.util.Properties properties = new java.util.Properties();\n");
        for (CommandModel.ParamModel pm : params) {
            out.write("\tif (!" + getBeanName(pm.getName()) + ".isEmpty()) {\n");
            out.write("\t\tproperties.put(\"" + pm.getName() + "\", " + getBeanName(pm.getName()) + ");\n");
            out.write("\t}");
        }

        out.write("if (commandParams != null) {\n");
        out.write("//formulate parent-link attribute for this command resource\n");
        out.write("//Parent link attribute may or may not be the id/target attribute\n");
        out.write("if (isLinkedToParent) {\n");
        out.write("__resourceUtil.resolveParentParamValue(commandParams, uriInfo);\n");
        out.write("}\n");
        out.write("properties.putAll(commandParams);\n");
        out.write("}\n\n");

        out.write("ActionReport actionReport = __resourceUtil.runCommand(commandName, properties, RestService.getHabitat());\n\n");
        out.write("ActionReport.ExitCode exitCode = actionReport.getActionExitCode();\n\n");
        out.write("StringResult results = new StringResult(commandName, __resourceUtil.getMessage(actionReport), options());\n");
        out.write("if (exitCode == ActionReport.ExitCode.SUCCESS) {\n");
        out.write("results.setStatusCode(200); /*200 - ok*/\n");
        out.write("} else {\n");
        out.write("results.setStatusCode(400); /*400 - bad request*/\n");
        out.write("results.setIsError(true);\n");
        out.write("results.setErrorMessage(actionReport.getMessage());\n");
        out.write("}\n\n");

        out.write("return results;\n\n");

        out.write("} catch (Exception e) {\n");
        out.write("throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);\n");
        out.write("}\n");
        out.write("}\n");
    }


    private void createCommandOptionsMethod(BufferedWriter out, int parameterType) throws IOException {
        out.write("@OPTIONS\n");
        out.write("@Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_HTML, MediaType.APPLICATION_XML})\n");
        out.write("public OptionsResult options() {\n");
        out.write("OptionsResult optionsResult = new OptionsResult(resourceName);\n");
        out.write("try {\n");
        out.write("//command method metadata\n");
        out.write("MethodMetaData methodMetaData = __resourceUtil.getMethodMetaData(\n");
        if (parameterType == Constants.QUERY_PARAMETER) {
             out.write("commandName, commandParams, Constants.QUERY_PARAMETER, RestService.getHabitat(), RestService.logger);\n");
        } else {
            //message parameter
            out.write("commandName, commandParams, Constants.MESSAGE_PARAMETER, RestService.getHabitat(), RestService.logger);\n");

            //GET meta data
            out.write("//GET meta data\n");
            out.write("optionsResult.putMethodMetaData(\"GET\", new MethodMetaData());\n");
        }

        out.write("optionsResult.putMethodMetaData(commandMethod, methodMetaData);\n");
        out.write("} catch (Exception e) {\n");
        out.write("throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);\n");
        out.write("}\n\n");

        out.write("return optionsResult;\n");
        out.write("}\n\n");
    }


    //This method converts a string into stringarray, uses the delimeter as the
    //separator character.
    private static String[] stringToArray(String str, String delimiter) {
        String[] retString = new String[0];

        if (str != null) {
            if(delimiter != null) {
                StringTokenizer tokens = new StringTokenizer(str, delimiter);
                retString = new String[tokens.countTokens()];
                int i = 0;
                while(tokens.hasMoreTokens()) {
                    retString[i++] = tokens.nextToken();
                }
            }
        }
        return retString;
    }
}
