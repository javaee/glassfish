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

/*
 * ConfigurationBean.java
 */
    
package com.sun.jbi.jsf.configuration.beans;

import com.sun.data.provider.TableDataProvider;
import com.sun.data.provider.impl.ObjectListDataProvider;
import com.sun.jbi.jsf.framework.common.GenericConstants;
import com.sun.jbi.jsf.framework.common.Util;
import com.sun.jbi.jsf.framework.common.resources.Messages;
import com.sun.jbi.jsf.framework.common.BaseBean;
import com.sun.jbi.jsf.framework.services.ServiceManager;
import com.sun.jbi.jsf.framework.services.configuration.ConfigurationService;
import com.sun.jbi.jsf.configuration.xml.ConfigurationParser;
import com.sun.jbi.jsf.configuration.xml.model.Configuration;
import com.sun.jbi.jsf.configuration.xml.schema.SchemaInstanceParser;
import com.sun.jbi.jsf.framework.services.management.*;
import com.sun.webui.jsf.component.Checkbox;
import com.sun.webui.jsf.component.DropDown;
import com.sun.webui.jsf.component.PasswordField;
import com.sun.webui.jsf.component.Alert;
import com.sun.webui.jsf.component.Property;
import com.sun.webui.jsf.component.PropertySheetSection;
import com.sun.webui.jsf.model.Option;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.el.ELContext;
import javax.el.MethodExpression;
import javax.faces.application.Application;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import com.sun.webui.jsf.component.TextField;
import com.sun.webui.jsf.component.PropertySheet;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.TabularData;
import com.sun.jbi.jsf.configuration.xml.model.DisplayInformation;
import org.apache.xmlbeans.SchemaType;
import javax.xml.namespace.QName;
import com.sun.jbi.jsf.util.JBILogger;



/**
 * 01/25/2007 - supports XSD/XML data type
 *   - valiidation for enumerated strings, positive integer
 *
 * @todo - support for primitive types for Value content
 *
 * @author Sun Microsystems
 */

public class ConfigurationBean extends BaseBean implements Serializable {
    

    private ConfigurationService configService = null;
    protected ManagementService managementService = null;
    
    private static String FIELD_VALUE_NAME = "propValue";
    private static String TABULAR_DATA_TYPE = "TabularData";
    private static String COMP_LIST = "compList";
    private static String LABEL_LIST = "labelList";
    private static int INVALID_VALUE = -1;
    private static String TARGET_LIST_KEY = "jbiConfigUpdateSelectedInstances";
    private static String INSTANCES_LIST_KEY = "instanceList";
    private static String SELECTED_INSTANCE_KEY = "jbiConfigViewSelectedInstance";
    private Option[] targetOptions = null;
    private String[] selectedTargetOptions = null;
  
    private PropertySheet propertySheet;
    private Map<String,Object> configData;

    private static String ALERT_TYPE_ERROR = "error";
    private static String ALERT_TYPE_SUCCESS = "success";
    private static String ALERT_TYPE_WARNING = "warning";
    private static String ALERT_TYPE_INFO = "info";
    
    /** Holds value of property alertDetail. */
    transient private String alertMessage = null;
  
    /** Holds value of property alertRendered. */
    transient private boolean renderAlertMessage = false;
    transient private boolean mSaveButtonDisabled = false;
    
    transient private String alertType = null;
    transient private String alertSummary;
    
    protected String serviceUnitName;
    private String schema = null;
    private String xmlData = null;
    private String[] attrNames = null;
    
    transient Map<String, UIInput> webUIMap = new HashMap<String, UIInput>();
    transient private Map<String, DisplayInformation> displayMap = new HashMap<String, DisplayInformation>();
    transient private Map<String, DisplayInformation> labelDisplayMap = new HashMap<String, DisplayInformation>();
    transient Map<String, SchemaType> coreTypeMap = new HashMap<String, SchemaType>();
  
    private static String STRING_TYPE = "STRING";
    private static String BOOLEAN_TYPE= "BOOLEAN";
    private static String NUMBER_TYPE="NUMBER";
    private static String PASSWORD_TYPE = "PASSWORD"; 
    
    //Get Logger to log fine mesages for debugging
    private static Logger sLog = JBILogger.getInstance();
    
    /** Creates a new instance of ConfigurationBean */
    public ConfigurationBean() {
        //
    }
    

    @SuppressWarnings("unchecked")
    private void setBooleanProperty(Map.Entry prop, Boolean value) {
        prop.setValue(value);
    }
   

    @SuppressWarnings("unchecked")
    private void setTextProperty(Map.Entry prop, String value) {
        prop.setValue(value);
    }
    

    @SuppressWarnings("unchecked")
    private void setProperty(Map.Entry prop, Object value) {
        prop.setValue(value);
    }
   
    
    private Map findValues(Map map, String key) {
        Map m = null;
        for ( Iterator iter=map.entrySet().iterator(); iter.hasNext(); ) {
            Map.Entry entry = (Map.Entry)iter.next();
            String ekey = (String)entry.getKey();
            Map m1 = (Map)entry.getValue();
            if ( ekey.equalsIgnoreCase(key) ) {
                m = m1;
                break;
            }
        }
        return m;
    }
    

    private Object findValue(Map map,String key) {
        Object value = null;
        for ( Iterator iter=map.entrySet().iterator(); iter.hasNext(); ) {
            Map.Entry entry = (Map.Entry)iter.next();
            String ekey = (String)entry.getKey();
            Map m1 = (Map)entry.getValue();
            value = m1.get(key);
            if ( value!=null ) {
                break;
            }
        }
        return value;
    }
   
    
    public String reset() {
        sLog.fine("reset called...");
        alertMessage = null;
       // renderAlertMessage = false;
        schema = null;
        xmlData = null;
        attrNames = null;
        configData = null;
      
        targetOptions = null;
        selectedTargetOptions = null;
        mSaveButtonDisabled = false;

        // restore previous configuration
        return GenericConstants.SUCCESS;
    }
   
   
    public String getLabel() {
        String label = "";
        if ( GenericConstants.BC_TYPE.equals(componentType) ) {
            label = Messages.getString("jbi.configuration.bc.label");
        } else if ( GenericConstants.SE_TYPE.equals(componentType) ) {
            label = Messages.getString("jbi.configuration.se.label");
        } 
        return label;
    }
    

    public String getTitle() {
        return getTitle("jbi.configuration.title");
    }
    

    @SuppressWarnings("unchecked")
    private List createConfigProperties(Map<String,Object> props,List propertyList ) {
     
        sLog.fine("createConfigProperties...");
       
        // add each property from configuration
        int id = 0;
        for ( Iterator iterator = props.entrySet().iterator();  iterator.hasNext(); ) {
            Map.Entry entry = (Map.Entry)iterator.next();
            String key = (String)entry.getKey();
            Object value = entry.getValue();
            UIComponent component = null;
            if ( value instanceof Boolean ) {
                // create Boolean property
                component = createBooleanProperty(id,value.toString());
            } else if ( value instanceof TabularData ) {
                // open mbean tabular type data
                Map map = createTabularDataProperty(id,(TabularData)value);
                // create new PropertySheetSection
                List propList = createPropertySheetSection(key,id);
                List compList = (List)map.get(COMP_LIST);
                List labelList = (List)map.get(LABEL_LIST);
                if ( compList!=null ) {
                    propList.addAll(createProperties(id,compList,labelList));
                    id += compList.size();
                }
            } else {
                // create String property
                component = createStringProperty(id,key,value.toString(),null);
            }
            if ( component!=null ) {
                propertyList.add(createProperty(id,key,component));
                id++;
            }
        }

      
        sLog.fine("propertyList: "+propertyList);
        return propertyList;
    }
   
    
    @SuppressWarnings("unchecked")
    private Property createProperty(int id, String label, UIComponent comp) {
        Property property = new Property();
        property.setId("property"+id);
        property.setLabel(label);
        property.getChildren().add(comp);
        return property;
    }
    

    @SuppressWarnings("unchecked")
    private List createProperties(int id, List compList, List labelList) {
        List propList = new ArrayList();
        Iterator iter2 = labelList.iterator();
        for ( Iterator iter=compList.iterator(); iter.hasNext(); ) {
            UIComponent comp = (UIComponent)iter.next();
            String label = (String)iter2.next();
            Property property = createProperty(id,label,comp);
            propList.add(property);
            id++;
        }
        return propList;
    }
    

    @SuppressWarnings("unchecked")
    private void setTabularDataProperty(TabularData tabularData, Map propValues) {
       
        Iterator iter2 = propValues.entrySet().iterator();
        List cList = new ArrayList();
        for (Iterator dataIter=tabularData.values().iterator(); dataIter.hasNext(); ) {
            CompositeData compData = (CompositeData) dataIter.next();
            CompositeType compType = compData.getCompositeType();
            String name = null;
            String value = null;
            
            for (Iterator itemIt = compType.keySet().iterator(); itemIt.hasNext(); ) {
                String keyName  = (String)itemIt.next();
                String keyValue = (String)itemIt.next();
                name = (String)compData.get(keyName);
                value = (String) compData.get(keyValue);
                
                Map.Entry entry = (Map.Entry)iter2.next();
                String pKey = (String)entry.getKey();
                Object pVal = entry.getValue();
               
                String[] itemNames = (String[])compType.keySet().toArray(new String[1]);
                Object[] itemValues= {name,pVal};
                try {
                    CompositeDataSupport compDataSupport = new CompositeDataSupport(compType, itemNames, itemValues);
                    cList.add(compDataSupport);
                } catch(Exception e) {
                    e.printStackTrace();
                }
               
            }
            
        }
        

        // update tabularData with CompositeDataSupport
        tabularData.clear();
        for (Iterator iter=cList.iterator(); iter.hasNext(); ) {
            CompositeDataSupport compDataSupport = (CompositeDataSupport)iter.next();
            tabularData.put(compDataSupport);
        }
        
    }
    
    
    @SuppressWarnings("unchecked")
    /**
     * added support for parameter field "type"
     *   valid values = STRING, BOOLEAN, NUMBER and PASSWORD 
     * @param id
     * @param tabularData
     * @return Map of key-values pairs
     */ 
    private Map createTabularDataProperty(int id, TabularData tabularData) {
        sLog.fine("createTabularDataProperty...");
       
        Map map = new HashMap();
        List compList = new ArrayList();
        List labelList = new ArrayList();
        
        for (Iterator dataIter = tabularData.values().iterator(); dataIter.hasNext(); ) {
            CompositeData compData = (CompositeData) dataIter.next();
            CompositeType compType = compData.getCompositeType();
            compType.keySet().iterator();
            sLog.fine("compData:"+compData);
            sLog.fine("compType: "+compType);
            sLog.fine("compType.keySet: "+compType.keySet());
           
            String name = null;
            String type = null;
            String value = null;
            UIComponent component = null;
            int itemCount = compType.keySet().size();
            for (Iterator itemIt = compType.keySet().iterator(); itemIt.hasNext(); ) {
                String keyName  = (String)itemIt.next();
                String keyType = null;
                if ( itemCount>2 ) {
                    keyType = (String)itemIt.next();
                    type = (String)compData.get(keyName);
                }
                String keyValue = (String)itemIt.next();
                
                name = (String)compData.get(keyName);
                value = (String) compData.get(keyValue);
               
                // create property
                component = createProperty(id,name,value,type);
                compList.add(component);
                labelList.add(name);
                id++;
            }
        }
        
        map.put(COMP_LIST,compList);
        map.put(LABEL_LIST,labelList);
        return map;
    }
    
    
    private UIComponent createProperty(int id,String label, String value,String type) {
        UIComponent component = null;
        if ( PASSWORD_TYPE.equals(type) ) {
            component = createStringProperty(id,label,value,type);
        } else if ( BOOLEAN_TYPE.equals(type) ) {
            component = createBooleanProperty(id,value);
        } else if ( NUMBER_TYPE.equals(type) ) {
            component = createStringProperty(id,label,value,null);
        } else { 
             component = createStringProperty(id,label,value,null);
        }
        return component;
    }
    

    private UIComponent createBooleanProperty(int id,String value) {
        Checkbox checkbox = new Checkbox();
        checkbox.setId("propb"+id);
        checkbox.setValue(value);
        checkbox.setLabel(" ");  // Specifying a label helps thing line up better
        return checkbox;
    }
    

    private UIComponent createStringProperty(int id,String label, String value, String type) {
        // @todo - this need to be refactored to use field type
        if ( label.indexOf("password")!=-1 || label.indexOf("Password")!=-1 || PASSWORD_TYPE.equalsIgnoreCase(type)) {
            PasswordField password = new PasswordField();
            password.setId("prop"+id);
            password.setText(value);
            password.setColumns(60);
            return password;
        } else {
            TextField text = new TextField();
            text.setId("prop"+id);
            text.setText(value);
            text.setColumns(60);
            return text;
        }
    }
    

    private ConfigurationService getConfigurationService(String targetName) {
        ConfigurationService configService = null;
        //  get configuration service
        configService = serviceManager.getConfigurationService(targetName);
        return configService;
    }
    

    protected ManagementService getManagementService(String targetName) {
        ManagementService managementService = null;
        //  get management service
        managementService = serviceManager.getManagementService(targetName);
        return managementService;
    }
    

    @SuppressWarnings("unchecked")
    private Map<String,Object> getConfigData() {
        
        sLog.fine("getConfigData...");
        
        configService = getConfigurationService(tName);
        managementService = getManagementService(tName);
        
        String name = Util.mapComponentValue(cName,componentName);
        String type = Util.mapComponentValue(cType,componentType);

        // check component status
        String state = managementService.getState(name,type);
        if ( GenericConstants.SHUTDOWN_STATE.equalsIgnoreCase(state) ) {
            mSaveButtonDisabled = true;
            displayStatusAlertMessage();            

        } else {

            if ( componentType.equals(GenericConstants.SU_TYPE) ) {
                // get SU configuration
                String suid = pName+ GenericConstants.HYPHEN_SEPARATOR +cName;
                configData = configService.getSUConfigurationProperties(name,type,suid);
            } else {
                // get container configuration (SE/BC)
                configData = configService.getConfigurationProperties(name,type);
            }

            sLog.fine("configData="+configData);

            // get Schema
            schema = configService.getSchema(name,type);
            // get xml data
            xmlData = configService.getXmlData(name,type);
            // get Attr Names
            attrNames = configService.getAttributeNames(name,type);
        }
        
        return configData;
    }
    
    
    @SuppressWarnings("unchecked")
    private List createPropertySheetSection(String propertySheetSectionName,int id) {
        // Property Section
        //List propertySectionList = propertySheet.getChildren();
        PropertySheetSection propertySheetSection = new PropertySheetSection();
        propertySheetSection.setId(TABULAR_DATA_TYPE+id);
        propertySheetSection.setLabel(propertySheetSectionName);        // @todo - localized this
        
        propertySheet.getChildren().add(propertySheetSection);
        return propertySheetSection.getChildren();
    }


    public void setRenderAlertOff() {
        renderAlertMessage = false;
    }
    

    @SuppressWarnings("unchecked")
    public PropertySheet getPropertySheet() {
        
        sLog.fine("getPropertySheet...");
        
        setup();
        reset();

        propertySheet = new PropertySheet();
        propertySheet.setId("propertySheet");

        // Property Section
        List propertySectionList = propertySheet.getChildren();
        PropertySheetSection propertySheetSection = new PropertySheetSection();
        propertySheetSection.setId("propertySheetSection");
        propertySheetSection.setLabel(Messages.getString("jbi.configuration.propertysection.runtime.configuration"));
        
        propertySectionList.add(propertySheetSection);
        List propertyList = propertySheetSection.getChildren();
        
        configData = getConfigData();
        if ( configData!=null ) {
            
            if ( schema!=null ) {
                createConfigPropertiesValidation(configData,propertyList);
            } else {
                createConfigProperties(configData,propertyList);
            }
        }
        
        return propertySheet;
        
    }
   
    
    public void setPropertySheet(PropertySheet propSheet) {
        this.propertySheet = propSheet;
    }
    
    
    public String getLabelSave() {
        return Messages.getString("jbi.configuration.label.save");
    }
    

    public String getLabelReset() {
        return Messages.getString("jbi.configuration.label.reset");
    }    
    

    public String getHelpInline() {
        return Messages.getString("jbi.configuration.helpInline");
    }
    

    ///
    ////////////////////////////// support for XSD/XML validation //////////////////////////
    ///
    
    private Alert alert = new Alert();
    private String mAlertMessage = "";

    /**
     * Getter method for the Configuration Alert.
     * @return The alert object.
     */
    public Alert getConfigurationAlert() {
        alert.setId("configure-alert");
        alert.setType("success");
        alert.setDetail(getAlertMessage());
        alert.setSummary(getAlertSummary());
        alert.setRendered(getAlertMessageRendered());
        renderAlertMessage = false;
        setAlertMessage("");
        return alert;
    }


    /**
     * Setter method for the Configuration Alert.
     * @param aAlert The alert object.
     */
    public void setConfigurationAlert(Alert aAlert)
    {
        renderAlertMessage = false;
        alert = aAlert;
        mAlertMessage = "";
    }


    /**
     * Local setter method which is called before the validation exception.  By doing
     * this, the alert values are set in case a validation error is incountered.
     */
    private void setConfigurationAlert() {
        alert.setId("configure-alert");
        alert.setType(getAlertMessage());
        alert.setDetail(getAlertMessage());
        alert.setSummary(getAlertSummary());
        alert.setRendered(true);
    }


    /** Get the value of property alertDetail. */
    public String getAlertMessage() {
        return alertMessage;
    }
    

    public void setAlertMessage(String msg) {
        alertMessage = msg;
    }
    

    /** Get the value of property alertRendered. */
    public boolean getAlertMessageRendered() {
        return renderAlertMessage;
    }
    

    public void setRenderAlertMessage(boolean value) {
        renderAlertMessage = value;
    }


    /** return the value of alert summary     */
    public String getAlertSummary() {
        return alertSummary;
    }
    

    public void setAlertSummary(String summary) {
        alertSummary = summary;
    }
    

    /** return the value of alert type */
    public String getAlertType() {
        return alertType;
    }
    

    public void setAlertType(String type) {
        alertType = type;
    }


    /** Get the value of property renderButtons. */
    public boolean getRenderButtons() {
        if ( configData==null || configData.isEmpty() ) {
            return false;
        } else {
            return true;
        }
    }
    

    private void resetAlerts() {
        alertMessage = null;
        renderAlertMessage = false;
        setAlertType(ALERT_TYPE_ERROR);
        setAlertSummary(Messages.getString("jbi.configuration.alert"));
    }
    
    
    /**
     *
     * @param props
     * @param propertyList
     * @return
     */
    @SuppressWarnings("unchecked")
    private List createConfigPropertiesValidation(Map<String, Object> props,
            List propertyList) {
        
        // System.out.println("createConfigProperties...");
        Application application = FacesContext.getCurrentInstance().getApplication();
        initialize(application, attrNames, new String[]{schema}, xmlData, configData);
        
        // add each property from configuration
        int id = 0;
        for (Iterator iterator = props.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            String key = (String) entry.getKey();
            Object value = entry.getValue();
            // System.out.println("{key = " + key + ", value = " + value + "}");
            UIComponent component = null;
            /*
            if (value instanceof Boolean) {
                // create Boolean property
                component = createBooleanProperty(id, value.toString());
            } else
             */
            if (value instanceof TabularData) {
                // open mbean tabular type data
                Map map = createTabularDataProperty(id, (TabularData) value);
                // create new PropertySheetSection
                List propList = createPropertySheetSection(key, id);
                List compList = (List) map.get(COMP_LIST);
                List labelList = (List) map.get(LABEL_LIST);
                if (compList != null) {
                    propList.addAll(createProperties(id, compList, labelList));
                    id += compList.size();
                }
            } else {
                // create String property
                component = this.webUIMap.get(key);
                if (component != null) {
                    DisplayInformation display = this.displayMap.get(key);
                    key = display.getDisplayName();
                }
                //System.out.println("Retrieving from webUIMap: key = " + key +", value = " + component);
            }
            if (component != null) {
                if (key != null) {
                    propertyList.add(createProperty(id, key, component));
                    //System.out.println("id = "+id+", key = " + key +", value = " + component);
                }
                /*
                 * else { propertyList.add(createProperty(id, component)); }
                 */
                id++;
            }
        }
        
        // System.out.println("propertyList: " + propertyList);
        return propertyList;
    }
    
    
    /**
     *
     * @param value
     * @param options
     * @return
     */
    private Option getOptionFromValue(String value, Option[] options) {
        Option returnValue = null;
        if ((value != null) && (options != null) && (options.length > 0)) {
            for (int index = 0; index < options.length; index++) {
                if (value.equals(options[index].getValue()) == true) {
                    returnValue = options[index];
                    break;
                }
            }
        }
        if ((value == null) && (options != null) && (options.length > 0)) {
            returnValue = options[0];
        }
        return returnValue;
    }
    
    
    /**
     *
     * @param attributeNames
     * @param actualXMLConfigData
     * @return
     */
    private Configuration parseConfigurationXML(String[] attributeNames,
            String actualXMLConfigData) {
        Configuration componentConfiguration = null;
        try {
            ConfigurationParser parser = ConfigurationParser.parseFromString(
                    actualXMLConfigData, attributeNames);
            componentConfiguration = parser.getComponentConfiguration();
        } catch(Exception e) {
            e.printStackTrace();
        }
        
        return componentConfiguration;
        
    }
    

    /**
     * This method throws validator exception if specified String is invalid.
     *
     * @param context
     * @param component
     * @param value
     * @throws ValidatorException
     *
     * @see javax.faces.validator.Validator#validate(javax.faces.context.FacesContext,
     *      javax.faces.component.UIComponent, java.lang.Object)
     */
    public void validateEnumeratedString(FacesContext context,
            UIComponent component, Object value) throws ValidatorException {
        //this.resetAlerts();
        FacesMessage msg = null;
        String localPart = component.getId();

        //System.out.println("Enumerated String Validator key: " + localPart);
        //System.out.println("Enumerated String Validator value: " + value);
        //System.out.println("Enumerated String Validator value class: " + value.getClass().getName());
        if (value != null) {
            SchemaType coreType = this.coreTypeMap.get(localPart);
            SchemaType baseType = null;
            if (coreType.isPrimitiveType() == false) {
                baseType = coreType.getBaseType();
            } else {
                baseType = coreType;
            }
            if ((coreType != null) && (baseType != null)
            && (baseType.getBuiltinTypeCode() == SchemaType.BTC_STRING)) {
                String[] enumeratedValues = SchemaInstanceParser.getEnumeratedStringValues(coreType);
                int index = this.findStringInArray((String) value,enumeratedValues);
                if (index == INVALID_VALUE) {
                    String msgString = Messages.getString("jbi.configuration.msgInvalidString");
                    msg = new FacesMessage(msgString);
                    //System.out.println(msgString);
                    msg.setSeverity(FacesMessage.SEVERITY_ERROR);
                    alertMessage = msgString;            
                    renderAlertMessage = true;
                    displayValidationAlertMessage(alertMessage);

                    throw new ValidatorException(msg);
                    
                }
            }
        } else {
            String msgString = Messages.getString("jbi.configuration.msgNullString");
            msg = new FacesMessage(msgString);
            //System.out.println(msgString);
            msg.setSeverity(FacesMessage.SEVERITY_ERROR);
            alertMessage = msgString;
            renderAlertMessage = true;
            displayValidationAlertMessage(alertMessage);
            
            throw new ValidatorException(msg);
        }
        
        if ((alertMessage == null) || ( renderAlertMessage == false)) {
            resetAlerts();
        }
    }
    
    
    /**
     *
     * @param stringObject
     * @param stringArray
     * @return
     */
    private int findStringInArray(String stringObject, String[] stringArray) {
        int returnValue = INVALID_VALUE;
        if ((stringObject != null) && (stringArray != null)
        && (stringArray.length > 0)) {
            for (int index = 0; index < stringArray.length; index++) {
                if (stringObject.equals(stringArray[index]) == true) {
                    returnValue = index;
                    break;
                }
            }
            
        }
        return returnValue;
    }
    
    
    /**
     *
     * @param application
     * @param attributeNames
     * @param actualSchemas
     * @param actualXMLConfigData
     * @param configurationData
     */
    void initialize(Application application, String[] attributeNames,
            String[] actualSchemas, String actualXMLConfigData,
            Map<String, Object> configurationData) {
        SchemaInstanceParser parser = null;
        boolean allowNetworkDownloadsFlag = false;
        boolean disableParticleValidRestrictionFlag = false;
        boolean diableUniqueParticleAttributionFlag = false;
        
        if (actualSchemas != null) {
            parser = new SchemaInstanceParser(actualSchemas,
                    allowNetworkDownloadsFlag,
                    disableParticleValidRestrictionFlag,
                    diableUniqueParticleAttributionFlag);
        }
        
        SchemaType[] types = parser.getGlobalElements();
        initialize(application, attributeNames, types[0], actualXMLConfigData,
                configurationData);
    }
    
    
    /**
     *
     * @param application
     * @param attributeNames
     * @param root
     * @param actualXMLConfigData
     * @param configurationData
     */
    void initialize(Application application, String[] attributeNames,
            SchemaType root, String actualXMLConfigData,
            Map<String, Object> configurationData) {
        
        Configuration componentConfiguration = parseConfigurationXML(
                attributeNames, actualXMLConfigData);
        DisplayInformation display = null;
        if (componentConfiguration != null) {
            displayMap = componentConfiguration.getDisplayDetailsMap();
            labelDisplayMap = componentConfiguration.getLabelDisplayDetailsMap();
            // TODO: Remove this
            //componentConfiguration.dump();
        }
        
        QName qName = null;
        String prefix = root.getDocumentElementName().getPrefix();
        String namespaceURI = root.getDocumentElementName().getNamespaceURI();
        
        SchemaType coreType = null;
        SchemaType baseType = null;
        String[] enumValues = null;
        for (int count = 0; count < attributeNames.length; count++) {
            qName = new QName(namespaceURI, attributeNames[count], prefix);
            coreType = SchemaInstanceParser.getCoreType(qName, root);
            if(coreType == null) {
                coreType = root;
            }
            if (coreType.isPrimitiveType() == false) {
                baseType = coreType.getBaseType();
            } else {
                baseType = coreType;
            }
            //System.out.println("AttributeName = " + attributeNames[count]);
            //System.out.println("QName = " + qName);
            //System.out.println("CoreType = " + coreType.getName());
            //System.out.println("BaseType = " + baseType.getName());
            if ((qName != null) && (coreType != null)) {
                coreTypeMap.put(attributeNames[count], coreType);
                if (displayMap != null) {
                    display = displayMap.get(attributeNames[count]);
                }
            }
            if (baseType != null) {
                // BTC_BOOLEAN
                if(baseType.getBuiltinTypeCode() == SchemaType.BTC_BOOLEAN) {
                    Checkbox input = new Checkbox();
                    input.setId(attributeNames[count]);
                    if (display != null) {
                        // input.setLabel(display.getDisplayName());
                        input.setToolTip(display.getDisplayDescription());
                        String value = configurationData.get(attributeNames[count])+"";
                        // String value = display.getDefaultValue();
                        input.setValue(value);
                    }
                    input.setStyle("margin-top:10px;margin-bottom:10px;vertical-align:middle");         // $N0N-NLS$
                    this.webUIMap.put(attributeNames[count], input);
                    
                }
                // BTC_STRING
                if (baseType.getBuiltinTypeCode() == SchemaType.BTC_STRING) {
                    // System.out.println("CoreType is =
                    // SchemaType.BTC_STRING");
                    String[] enumeratedValues = SchemaInstanceParser
                            .getEnumeratedStringValues(coreType);
                    if ((enumeratedValues != null)
                    && (enumeratedValues.length > 0)) {
                        Option[] options = new Option[enumeratedValues.length];
                        for (int index = 0; index < enumeratedValues.length; index++) {
                            options[index] = new Option(
                                    enumeratedValues[index],
                                    enumeratedValues[index]);
                        }
                        DropDown input = new DropDown();
                        input.setId(attributeNames[count]);
                        input.setDisabled(false);
                        input.setSelected(options[0]);
                        FacesContext facesContext = FacesContext
                                .getCurrentInstance();
                        ELContext elcontext = facesContext.getELContext();
                        MethodExpression methodExpression = facesContext
                                .getApplication()
                                .getExpressionFactory()
                                .createMethodExpression(
                                elcontext,
                                "#{ConfigurationBean.validateEnumeratedString}",                // $NON-NLS$ 
                                null,
                                new Class[] { FacesContext.class,
                                UIComponent.class, Object.class });
                        input.setValidatorExpression(methodExpression);
                        
                        input.setItems(options);
                        if (display != null) {
                            input.setToolTip(display.getDisplayDescription());
                            String value = (String) configurationData.get(attributeNames[count]);
                            Option option = getOptionFromValue(value,options);
                            input.resetValue();
                            input.setSelected(option);
                            input.setValue(value);
                        }
                        this.webUIMap.put(attributeNames[count], input);
                        //System.out.println("Adding to webUIMap: key = " + attributeNames[count] + ", value = " + input);
                    } else {
                        // Create Text Field
                        if (display != null) {
                            if (display.isPasswordField() == true) {
                                // Create password Field
                                PasswordField input = new PasswordField();
                                input.setId(attributeNames[count]);
                                input.setDisabled(false);
                                if (display != null) {
                                    // input.setLabel(display.getDisplayName());
                                    input.setToolTip(display.getDisplayDescription());
                                    String value = (String) configurationData.get(attributeNames[count]);
                                    // String value = display.getDefaultValue();
                                    input.setText(value);
                                }
                                input.setColumns(60);
                                this.webUIMap.put(attributeNames[count], input);
                                //System.out.println("Adding to webUIMap: key =" + attributeNames[count] + ", value = " + input);
                                
                            } else {
                                // Create regular text field
                                TextField input = new TextField();
                                input.setId(attributeNames[count]);
                                input.setDisabled(false);
                                if (display != null) {
                                    // input.setLabel(display.getDisplayName());
                                    input.setToolTip(display.getDisplayDescription());
                                    String value = (String) configurationData.get(attributeNames[count]);
                                    // String value = display.getDefaultValue();
                                    input.setText(value);
                                }
                                input.setColumns(60);
                                this.webUIMap.put(attributeNames[count], input);
                                //System.out.println("Adding to webUIMap: key =" + attributeNames[count] + ", value = " +input);
                                
                            }
                        }
                        
                    }
                }
                // BTC_POSITIVE_INTEGER
                if ((baseType.getBuiltinTypeCode() == SchemaType.BTC_POSITIVE_INTEGER) ||
                        (baseType.getBuiltinTypeCode() == SchemaType.BTC_INT) ||
                        (baseType.getBuiltinTypeCode() == SchemaType.BTC_NON_NEGATIVE_INTEGER) ||
                        (baseType.getBuiltinTypeCode() == SchemaType.BTC_INTEGER)){
                    // System.out.println("CoreType is =
                    // SchemaType.BTC_POSITIVE_INTEGER");
                    int totalDigits = 0, minInclusive = 0, maxInclusive = 0;
                    totalDigits = SchemaInstanceParser.getDecimalFacet(
                            coreType, SchemaType.FACET_TOTAL_DIGITS);
                    minInclusive = SchemaInstanceParser.getDecimalFacet(
                            coreType, SchemaType.FACET_MIN_INCLUSIVE);
                    maxInclusive = SchemaInstanceParser.getDecimalFacet(
                            coreType, SchemaType.FACET_MAX_INCLUSIVE);
                    // Create Text Field
                    if ((totalDigits != INVALID_VALUE)
                    || (minInclusive != INVALID_VALUE)
                    || (maxInclusive != INVALID_VALUE)) {
                        TextField input = new TextField();
                        input.setId(attributeNames[count]);
                        input.setDisabled(false);
                        if (display != null) {
                            // input.setLabel(display.getDisplayName());
                            input.setToolTip(display.getDisplayDescription());
                            Integer integer = null;
                            Object objectValue = null;
                            try {
                                objectValue = configurationData.get(attributeNames[count]);
                                if ( objectValue!=null ) {
                                    if (objectValue instanceof Integer) {
                                        integer = (Integer)objectValue;
                                        input.setText(integer.toString());
                                        
                                    }                                                                 }
                            } catch(NumberFormatException e) {
                                sLog.fine(Messages.getString("jbi.configuration.msgInvalidXmlData")+attributeNames[count]+
                                       " " + Messages.getString("jbi.configuration.msgInvalidXmlData2")+objectValue);
                                //integer = new Integer(0);
                            }
                            // CR6562289
                            //String value = integer.toString();
                            //input.setText(value);
                        }
                        FacesContext facesContext = FacesContext
                                .getCurrentInstance();
                        ELContext elcontext = facesContext.getELContext();
                        MethodExpression methodExpression = facesContext
                                .getApplication()
                                .getExpressionFactory()
                                .createMethodExpression(
                                elcontext,
                                "#{ConfigurationBean.validateInteger}",
                                null,
                                new Class[] { FacesContext.class,
                                UIComponent.class, Object.class });
                        input.setValidatorExpression(methodExpression);
                        input.setColumns(60);
                        this.webUIMap.put(attributeNames[count], input);
                        //System.out.println("Adding to webUIMap: key = " +attributeNames[count] + ", value = " + input);
                        
                    } else {
                        // Create regular text field
                        TextField input = new TextField();
                        input.setId(attributeNames[count]);
                        input.setDisabled(false);
                        if (display != null) {
                            // input.setLabel(display.getDisplayName());
                            input.setToolTip(display.getDisplayDescription());
                            Integer integer = null;
                            Object objectValue = null;
                            objectValue = configurationData.get(qName.getLocalPart());
                            String value = objectValue+"";
                            input.setText(value);
                        }
                        input.setColumns(60);
                        this.webUIMap.put(attributeNames[count], input);
                        //System.out.println("Adding to webUIMap: key = " +attributeNames[count] + ", value = " + input);
                    }
                    
                }
            }
            
        }
        
    }
    
    /**
     * This method throws validator exception if specified String is invalid.
     *
     * @param context
     * @param component
     * @param value
     * @throws ValidatorException
     *
     * @see javax.faces.validator.Validator#validate(javax.faces.context.FacesContext,
     *      javax.faces.component.UIComponent, java.lang.Object)
     */
    public void validateInteger(FacesContext context,
            UIComponent component, Object value) throws ValidatorException {
        String msgString = null;
        FacesMessage msg = null;
        String localPart = component.getId();
        //System.out.println("Positive Integer Validator key: " + localPart);
        //System.out.println("Positive Integer Validator value: " + value);
        //System.out.println("Positive Integer Validator value class: " + value.getClass().getName());
        if (value != null) {
            SchemaType coreType = this.coreTypeMap.get(localPart);
            SchemaType baseType = null;
            if (coreType.isPrimitiveType() == false) {
                baseType = coreType.getBaseType();
            } else {
                baseType = coreType;
            }
            //System.out.println("Positive Integer baseType is: " + baseType.getName());
            if ((coreType != null)
            && (baseType != null)
            && ((baseType.getBuiltinTypeCode() == SchemaType.BTC_POSITIVE_INTEGER)) ||
                    (baseType.getBuiltinTypeCode() == SchemaType.BTC_INT) ||
                    (baseType.getBuiltinTypeCode() == SchemaType.BTC_NON_NEGATIVE_INTEGER) ||
                    (baseType.getBuiltinTypeCode() == SchemaType.BTC_INTEGER)) {
                int totalDigits = 0, minInclusive = 0, maxInclusive = 0;
                totalDigits = SchemaInstanceParser.getDecimalFacet(coreType,
                        SchemaType.FACET_TOTAL_DIGITS);
                minInclusive = SchemaInstanceParser.getDecimalFacet(coreType,
                        SchemaType.FACET_MIN_INCLUSIVE);
                maxInclusive = SchemaInstanceParser.getDecimalFacet(coreType,
                        SchemaType.FACET_MAX_INCLUSIVE);
                String stringObject = (String) value;
                Integer integer = null;
                try {
                    integer = Integer.valueOf(stringObject);
                } catch(NumberFormatException e) {
                    msgString = value+ Messages.getString("jbi.configuration.msgNotNumber");
                    msg = new FacesMessage(msgString);
                    msg.setSeverity(FacesMessage.SEVERITY_ERROR);
                    alertMessage = msgString;
                    renderAlertMessage = true;
                    displayValidationAlertMessage(alertMessage);
                    throw new ValidatorException(msg);
                }
                if ((totalDigits != INVALID_VALUE)
                && (integer.toString().length() > totalDigits)) {
                    msgString = integer.toString()
                    + Messages.getString("jbi.configuration.msgValueExceedMax") + totalDigits;
                }
                int positiveInteger = integer.intValue();
                if ((minInclusive != INVALID_VALUE)
                && (positiveInteger < minInclusive)) {
                    msgString = integer.toString()
                    + Messages.getString("jbi.configuration.msgMinValueAllowed") + minInclusive;
                }
                if ((maxInclusive != INVALID_VALUE)
                && (positiveInteger > maxInclusive)) {
                    msgString = integer.toString()
                    + Messages.getString("jbi.configuration.msgMaxValueAllowed") + maxInclusive;
                    
                }
                if (msgString != null) {
                    msg = new FacesMessage(msgString);
                    //System.out.println(msgString);
                    msg.setSeverity(FacesMessage.SEVERITY_ERROR);
                    alertMessage = msgString;
                    renderAlertMessage = true;
                    displayValidationAlertMessage(alertMessage);
                    throw new ValidatorException(msg);
                }
            }
        } else {
            msgString = Messages.getString("jbi.configuration.msgIntegerNull");
            msg = new FacesMessage(msgString);
            //System.out.println(msgString);
            msg.setSeverity(FacesMessage.SEVERITY_ERROR);
            alertMessage = msgString;
            renderAlertMessage = true;
            displayValidationAlertMessage(alertMessage);
            throw new ValidatorException(msg);
        }
        
        if ((alertMessage == null) || ( renderAlertMessage == false)) {
            resetAlerts();
        }
        
    }
    
    @SuppressWarnings("unchecked")
    public String save(String[] targets) {
        sLog.fine("save to targets...");
        if ( alertMessage!=null && renderAlertMessage==true ) {
            resetAlerts();
            return null;
        }
        if ( targets==null || targets.length==0 ) {
            sLog.fine("No targets specified.");
            return null;
        }
        saveConfigData();
        // iterate thru each target
        for (int i=0; i<targets.length; i++) {
            sLog.fine("saving to target: "+targets[i]);
            ConfigurationService service = getConfigurationService(targets[i]);
            saveConfigService(service);
        }
        
        // display success alert
        displaySavedAlertMessage();
        
        //resetAlerts();
        return GenericConstants.SUCCESS;
    
    }    
    

    public boolean getSaveButtonDisabled ()
    {
        return mSaveButtonDisabled;
    }


    public void setSaveButtonDisabled (boolean mState)
    {
        mSaveButtonDisabled = mState;
    }


    private void displayAlertMessage(String type, String summary, String msg) {
        setAlertType(type);
        setAlertSummary(summary);
        setRenderAlertMessage(true);

        // If there are previous "error" messages, then we should append the
        // current message "msg" to the end.  This way all error messages 
        // will be displayed in the alert.
        if (mAlertMessage.length() > 0)
        {
            mAlertMessage = mAlertMessage + "<br>" + msg;
        }
        else
        {
            mAlertMessage = msg;
        }

        setAlertMessage(mAlertMessage);
        setConfigurationAlert();
    }
    
    private void displayValidationAlertMessage(String msg) {
        displayAlertMessage(ALERT_TYPE_ERROR,Messages.getString("jbi.configuration.alert.validation"),msg);
    }    

    private void displayStatusAlertMessage() {
        displayAlertMessage(ALERT_TYPE_ERROR,Messages.getString("jbi.configuration.alert"),
                Messages.getString("jbi.configuration.msgComponentDown"));
    }    
    
    private void displaySavedAlertMessage() {
        displayAlertMessage(ALERT_TYPE_SUCCESS,Messages.getString("jbi.configuration.alert.save.success"),"");
    }
        
    @SuppressWarnings("unchecked")
    public String save() {

        sLog.fine("save...");
        
        if ((alertMessage != null) && (renderAlertMessage == true)) {
            this.resetAlerts();
            return null;
        }
        saveConfigData();
        saveConfigService(configService);
        displaySavedAlertMessage();
        //resetAlerts();
        return GenericConstants.SUCCESS;
    }

    
    @SuppressWarnings("unchecked")
    public void saveConfigData() {
        
        // 1. first collect UI data
        Map map = new HashMap<String,PropertySheetSection>();    // contains <name,propertySheetSectionMap>
        
        if ( propertySheet!=null ) {
            List propSheetSectionList = propertySheet.getChildren();
            for ( Iterator iter=propSheetSectionList.iterator(); iter.hasNext(); ) {
                PropertySheetSection propSheetSection = (PropertySheetSection)iter.next();
                Map pmap = new LinkedHashMap<String,Object>();       // contains field names and values
                map.put(propSheetSection.getLabel(),pmap);
                List propList = propSheetSection.getChildren();
                for ( Iterator iter1=propList.iterator(); iter1.hasNext(); ) {
                    Property property = (Property)iter1.next();
                    String name = property.getLabel();
                    DisplayInformation info = null;
                    if (name != null) {
                        info = this.labelDisplayMap.get(name);
                    }
                    if (info != null) {
                        name = info.getAttributeName();
                    }
                    for (Iterator iter2=property.getChildren().iterator(); iter2.hasNext(); ) {
                        UIInput comp = (UIInput)iter2.next();
                        Object value = comp.getValue();
                        pmap.put(name,value);
                    }
                }
            }
        }
        
        // 2. update configData
        if ( configData !=null ) {
            for ( Iterator iterator = configData.entrySet().iterator();  iterator.hasNext(); ) {
                Map.Entry prop = (Map.Entry)iterator.next();
                String propKey = (String)prop.getKey();
                Object propValue = (Object)prop.getValue();
                Object value = null;
                if ( propValue instanceof Boolean ) {
                    value = findValue(map,propKey);
                }  else if ( propValue instanceof TabularData ) {
                    Map m = findValues(map,propKey);
                    if ( m!=null ) {
                        setTabularDataProperty((TabularData)propValue,m);
                    }
                } else {
                    value = findValue(map,propKey);
                }
                if ( value!=null ) {
                    prop.setValue(value);
                }
            }
        }
        
    }
    
    
    public void saveConfigService(ConfigurationService configService) {
        // 3. update config service
        if ( configData!=null ) {
            sLog.fine("updating config service...");
            String name = Util.mapComponentValue(cName,componentName);
            String type = Util.mapComponentValue(cType,componentType);
            configService.setConfigurationProperties(name,type,configData);
        }
    }
    

    /**
     * save configuration changes to list of targets which is stored in the session variable
     */
    public String saveConfigs() {
        sLog.fine("saveConfigs....");
        // retrieve list of targets from session variable
        //String[] targets = (String[])getParameter(TARGET_LIST_KEY);        // $NON-NLS-1$
        // get targets from listbox
        String[] targets = selectedTargetOptions;
        save(targets);
        return "";
    }



    /**
     * return a list of target instances for the listbox
     */
    public Option[] getTargetOptions() {
        // get list from session
        Option[] targetOptions = (Option[])getParameter(INSTANCES_LIST_KEY);
        sLog.fine("targetOptions: "+targetOptions);
        return targetOptions;
    }
    

    /**
     * set the list of target instances for the listbox
     */
    public void setTargetOptions(Option[] options) {
        sLog.fine("setting target options: "+options);
        targetOptions = options;
    }    
    

    /**
     * set the selected target instances options from the listbox
     */
    public void setSelectedTargetOptions(String[] selections) {
        sLog.fine("selections: "+selections);
        selectedTargetOptions = selections;
    }


    /**
     * get the selected target instances options from the listbox
     */
    public String[] getSelectedTargetOptions() {
        if ( selectedTargetOptions==null ) {
            selectedTargetOptions = new String[]{ (String)getParameter(SELECTED_INSTANCE_KEY) } ;
        }
        sLog.fine("get selections: "+selectedTargetOptions);
        return selectedTargetOptions;
    }
    

    public void setParameterValue (String aKey, String aValue) {
        setParameter(aKey,aValue);
    }

}



