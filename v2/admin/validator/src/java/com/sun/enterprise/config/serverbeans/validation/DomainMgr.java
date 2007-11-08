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

package com.sun.enterprise.config.serverbeans.validation;

//jdk imports
import javax.xml.parsers.*;
import org.w3c.dom.*;
import java.io.File;
import java.util.StringTokenizer;
import java.util.HashMap;
import org.xml.sax.SAXException;
import org.xml.sax.InputSource;
import java.io.IOException;
import java.util.Vector;
import java.util.StringTokenizer;
import java.lang.Class;
import java.lang.reflect.Constructor;
import java.net.URL;

// Logging
import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.LogDomains;

import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigContextEvent;
import com.sun.enterprise.config.ConfigContextEventListener;
import com.sun.enterprise.admin.AdminValidationException;

// config imports
import com.sun.enterprise.config.ConfigFactory;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.ConfigBean;
import com.sun.enterprise.config.serverbeans.*;
import com.sun.enterprise.admin.meta.MBeanRegistryFactory;
import com.sun.enterprise.admin.meta.MBeanRegistry;

/**
 *  Class which loads all the validator descriptor information from a xml file into a hash map. 
 *  Validator uses this Hash Map and invokes the particular test case depending on xml tag

    @author Srinivas Krishnan
    @version 2.0
*/

public class DomainMgr implements ConfigContextEventListener {
    
    // Logging
    static Logger _logger = LogDomains.getLogger(LogDomains.APPVERIFY_LOGGER);
    
    LocalStringManagerImpl smh = StringManagerHelper.getLocalStringsManager();
    HashMap tests = new HashMap();
    transient private long lastModified = 0; 
    public NameListMgr _nameListMgr;
    public MBeanRegistry _mbeanRegistry;
    public DomainMgr() {
        this(MBeanRegistryFactory.getAdminContext().getAdminConfigContext(), false);
    }

    public DomainMgr(ConfigContext ctx, boolean bStaticContext) {
        this(ctx, bStaticContext, null);
    }
   
    public DomainMgr(ConfigContext ctx, boolean bStaticContext, MBeanRegistry registry) {
        loadDescriptors();
        _nameListMgr = new NameListMgr(ctx, bStaticContext);
        if(registry==null)
            _mbeanRegistry = MBeanRegistryFactory.getAdminMBeanRegistry();
        else
            _mbeanRegistry = registry;
    }
    
    MBeanRegistry getMBeanRegistry()
    {
        return _mbeanRegistry;
    }
    // Get the path of validation descriptor xml file from System property class path
    private String getTestFile() throws Exception {
        URL propertiesFile = DomainMgr.class.getClassLoader().getResource(
            "com/sun/enterprise/config/serverbeans/validation/config/ServerTestList.xml");
//TODO: for test only
//  URL propertiesFile = (new File("/ias/admin/validator/descr.xml")).toURL();
        return propertiesFile.toString();
    }

    // Loads all validation descriptors from XML file into the Hash Map
    public boolean loadDescriptors() {
        boolean allIsWell = true;
       
        try {
            //tests.clear();
            DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            InputSource is = new InputSource(getTestFile());
            Document doc = db.parse(is);
            NodeList list = doc.getElementsByTagName("element");
            for (int i=0;i<list.getLength();i++) {
                Element e = (Element) list.item(i);
                String elementName  =  e.getAttribute("name");
                String elementXPath =  e.getAttribute("xpath");
                String elementCustomClass = e.getAttribute("custom-class") ;
                String testName = e.getAttribute("test-name");
                if(testName==null || testName.length()==0)
                {
                    testName = XPathHelper.convertName(elementName);
                }
                if (null == elementCustomClass || elementCustomClass.length() == 0){
                    elementCustomClass = testName;
                }
                String[] required_children = null;
                String[] exclusive_list    = null;
                String elemList = e.getAttribute("required-children");
                if(elemList!=null && elemList.length()>0)
                {
                    required_children = elemList.split(",");
                }
                elemList = e.getAttribute("exclusive-list");
                if(elemList!=null && elemList.length()>0)
                {
                    exclusive_list = elemList.split(",");
                }
                String key = e.getAttribute("key");
                if(key!=null && key.length()==0)
                    key = null;
                Vector attributes = new Vector();
                NodeList nl = e.getChildNodes();
                for (int index=0,j=0;j<nl.getLength();j++) {
                    String temp;
                    String nodeName = nl.item(j).getNodeName().trim();
                    String nameValue=null;
                    String typeValue=null;
                    NamedNodeMap nodeMap=null;
                    AttrType attr=null;
                   
                    Node n = nl.item(j);
                    
                    if("attribute".equals(nodeName) || "optional-attribute".equals(nodeName)) {
                        nodeMap = n.getAttributes();

                        nameValue   = getAttr(nodeMap, "name");
                        typeValue   = getAttr(nodeMap, "type");
                        if("string".equals(typeValue)) 
                        {
                            attr = new AttrString(nameValue, typeValue, "optional-attribute".equals(nodeName) );
                            if((temp=getAttr(nodeMap, "max-length"))!=null)
                               ((AttrString)attr).setMaxLength(Integer.parseInt(temp));
                            if((temp=getAttr(nodeMap, "enumeration"))!=null)
                            {
                                Vector ee = new Vector();
                                String[] strs = temp.split(",");
                                for(int k=0; k<strs.length; k++)
                                    ee.add(strs[k]);
                                ((AttrString)attr).setEnumstring(ee);
                            }
                            ((AttrString)attr).setRegExpression(getAttr(nodeMap, "regex"));
                        }
                        else if("file".equals(typeValue)) 
                        {
                            attr = new AttrFile(nameValue, typeValue, "optional-attribute".equals(nodeName));
                            if("true".equals(getAttr(nodeMap, "exists")))
                                ((AttrFile)attr).setCheckExists(true);
                        }
                        else if("integer".equals(typeValue))
                        {
                            attr = new AttrInt(nameValue,typeValue, "optional-attribute".equals(nodeName));
                            if((temp = getAttr(nodeMap, "range")) != null) 
                            {
                                String[] strs = temp.split(",");
                                if(!strs[0].equals("NA"))
                                    ((AttrInt)attr).setLowRange(Integer.parseInt(strs[0]));
                                if(!strs[1].equals("NA"))
                                    ((AttrInt)attr).setHighRange(Integer.parseInt(strs[1]));
                            }
                        }
                        else if("classname".equals(typeValue))
                            attr = new AttrClassName(nameValue, typeValue, "optional-attribute".equals(nodeName));
                        else if("address".equals(typeValue)) 
                            attr = new AttrAddress(nameValue,typeValue, "optional-attribute".equals(nodeName));
                        else if("jndi-unique".equals(typeValue)) 
                            attr = new AttrUniqueJNDI(nameValue,typeValue, "optional-attribute".equals(nodeName));

                        if(attr != null) 
                        {
                            attr.addRuleValue("belongs-to",     getAttrAsList(nodeMap, "belongs-to"));
                            attr.addRuleValue("references-to",  getAttrAsList(nodeMap, "references-to"));
                            attr.addRuleValue("le-than", getAttr(nodeMap, "le-than"));
                            attr.addRuleValue("ls-than", getAttr(nodeMap, "ls-than"));
                            attr.addRuleValue("ge-than", getAttr(nodeMap, "ge-than"));
                            attr.addRuleValue("gt-than", getAttr(nodeMap, "gt-than"));

                            attributes.add(index++,attr);
                        }
                    }
                }
                final ValidationDescriptor desc =
                       new ValidationDescriptor(this, elementName, 
                            elementXPath, elementCustomClass, 
                            key, attributes, required_children, exclusive_list);
                final GenericValidator validator= getGenericValidator(desc);
                if(validator != null) 
                    tests.put(testName, validator);
            }
        } catch (ParserConfigurationException e) {
            _logger.log(Level.WARNING, "parser_error", e);
            allIsWell = false;
        } catch (SAXException e) {
            _logger.log(Level.WARNING, "sax_error", e);
            allIsWell = false;     
        } catch (IOException e) {
            _logger.log(Level.WARNING, "error_loading_xmlfile", e);
            allIsWell = false;
        } catch(Exception e) {
            _logger.log(Level.WARNING, "error", e);
            allIsWell = false;
        }
        return allIsWell;
    }
    
    private String getAttr(NamedNodeMap nodeMap, String attrName)
    {
        Node node = nodeMap.getNamedItem(attrName);
        if(node != null)
            return node.getNodeValue();
        return null;
    }
    
    private String[] getAttrAsList(NamedNodeMap nodeMap, String attrName)
    {
        String attrValue = getAttr(nodeMap, attrName);
        if(attrValue==null)
            return null;
        return attrValue.split(",");
    }
        /**
         * Get the generic validator to be used for the given
         * validation descriptor
         * @param v the validation descriptor
         * @return a GenericValidator instance that is to be used to
         * validate elements which match the ValidationDescription
         */
    GenericValidator getGenericValidator(final ValidationDescriptor v) throws InstantiationException, IllegalAccessException, java.lang.reflect.InvocationTargetException{
        final Class c = getValidatorClass(v.getCustomValidatorClass());
        try {
            final Constructor con = c.getConstructor(new Class[]{ValidationDescriptor.class});
            return (GenericValidator) con.newInstance(new Object[]{v});
        }
        catch (NoSuchMethodException e){
            return null;
        }
        
    }
    
        /**
         * Find a class that will perform validation for the given
         * class name. The class returned is the first that can be
         * loaded from the following ordered list:
         * <ol>
         * <li>The class as specified by the className param</li>
         * <li>The class as specified by the className param,
         * prepended with the TEST_PACKAGE package name. </li>
         * <li>The GenericValidator class</li>
         * Setting the log level to CONFIG will provide logging
         * information as to which class was actually found and loaded.
         * @param className the name of the class for which a
         * validator class is to be found
         * @return the class which is to be used for validation
         **/
    Class getValidatorClass(final String className){
        Class c;
        final String cn = TEST_PACKAGE+className+"Test";
            try {
            c= Class.forName(cn);
        }
        catch (ClassNotFoundException cnfe2){
            c = GenericValidator.class;
        }
        _logger.log(Level.CONFIG, "validator using class \""+c.getName()+"\" to validate \""+cn+"\"");
        return c;
    }
    
    // Method invokes the validation function of the test case
    public Result check(ConfigContextEvent cce) {
        String name = cce.getName();
        String beanName = cce.getBeanName();
        Result result = null;


        if(name == null && beanName == null)
                return result;
        
        DomainCheck validator = (DomainCheck) tests.get(name);
        if(validator == null && beanName != null)
            validator = (DomainCheck) tests.get(beanName);
        try {
            if(validator != null)
                result = validator.validate(cce);
        } catch(Exception e) {
//System.out.println("+++++name="+name + " xpath=" + ((ConfigBean)cce.getObject()).getXPath());
            _logger.log(Level.WARNING, "domainxmlverifier.error_on_validation", e);
        }
        return result;
    }
    
    public void postAccessNotification(ConfigContextEvent ccce) {
    }
    
    public void postChangeNotification(ConfigContextEvent ccce)  {
    }
    
    public void preAccessNotification(ConfigContextEvent ccce) {
    }
    
    // Function invoked by the Config Bean for validation before writing into domain.xml
    // Registered as listeners with ConfigContext while creation
    public void preChangeNotification(ConfigContextEvent cce) {
        Result result = null;
        try{
           result = check(cce);
        } catch(Throwable t)
        {
            _logger.log(Level.WARNING, "Exception during validation ", t);
        }
        
        if(result != null && result.getStatus() == Result.FAILED)
        {
            _logger.log(Level.WARNING, "Validation error: " + result.getErrorDetails().toString());
            throw new AdminValidationException(result.getErrorDetailsAsString());
        }
    }
    ValidationDescriptor findValidationDescriptor(String beanName)
    {
        if(beanName!=null)
        {
            GenericValidator genVal = (GenericValidator)tests.get(beanName);
            if(genVal!=null)
                return genVal.desc;
        }
        return null;
    }

    GenericValidator findConfigBeanValidator(ConfigBean configBean)
    {
        
        if(configBean!=null)
        {
            String className = configBean.getClass().getName();
            int iLastDot = className.lastIndexOf('.');
            if(iLastDot>0)
               return (GenericValidator)tests.get(className.substring(iLastDot+1));
        }
        return null;
    }
    
    public static final String TEST_PACKAGE="com.sun.enterprise.config.serverbeans.validation.tests.";
}
