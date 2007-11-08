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

package com.sun.jbi.jsf.util;

import com.sun.appserv.management.config.ConfigConfig;
import com.sun.appserv.management.config.ModuleLogLevelsConfig;
import com.sun.enterprise.tools.admingui.util.GuiUtil;
import com.sun.jbi.jsf.util.BeanUtilities;
import com.sun.jbi.jsf.util.SharedConstants;
import com.sun.jbi.jsf.util.SystemLoggerUtilities;
import com.sun.jbi.jsf.bean.LoggingBean;
import com.sun.jbi.jsf.factory.PropertySheetAdaptorBase;
import com.sun.jbi.ui.common.JBIAdminCommands;
import com.sun.jsftemplating.layout.descriptors.LayoutComponent;
import com.sun.webui.jsf.component.Property;
import com.sun.webui.jsf.component.StaticText;
import com.sun.webui.jsf.component.DropDown;
import com.sun.webui.jsf.component.HiddenField;
import com.sun.webui.jsf.model.Option;
import java.util.HashMap;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.Map;
import java.util.Set;
import java.util.Iterator;
import java.io.IOException;
import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;


/**
 *  <p> The <code>JBILogLevelsPropertySheetAdaptor</code> implementation must have a
 *	<code>public static JBILogLevelsPropertySheetAdaptor getInstance(FacesContext,
 *	LayoutComponent, UIComponent)</code> method in order to get access to
 *	an instance of the <code>JBILogLevelsPropertySheetAdaptor</code> instance.</p>
 *
 *  <p>	This class is used by <code>DynamicPropertySheetNodeFactory</code>.</p>
 *
 *
 */
public class JBILogLevelsPropertySheetAdaptor extends PropertySheetAdaptorBase 
{

    private static Logger sLog;
    private JBIAdminCommands mJac = BeanUtilities.getClient();
     
    private static final Option[] mLogLevelOptions = {
        new Option ((Level.parse("FINEST")).getName(),  GuiUtil.getMessage(I18nUtilities.getResourceString("loglevel.FINEST"))),
        new Option ((Level.parse("FINER")).getName(),   GuiUtil.getMessage(I18nUtilities.getResourceString("loglevel.FINER"))),
        new Option ((Level.parse("FINE")).getName(),    GuiUtil.getMessage(I18nUtilities.getResourceString("loglevel.FINE"))),
        new Option ((Level.parse("CONFIG")).getName(),  GuiUtil.getMessage(I18nUtilities.getResourceString("loglevel.CONFIG"))),
        new Option ((Level.parse("INFO")).getName(),    GuiUtil.getMessage(I18nUtilities.getResourceString("loglevel.INFO"))),
        new Option ((Level.parse("WARNING")).getName(), GuiUtil.getMessage(I18nUtilities.getResourceString("loglevel.WARNING"))),
        new Option ((Level.parse("SEVERE")).getName(),  GuiUtil.getMessage(I18nUtilities.getResourceString("loglevel.SEVERE"))),
        new Option ((Level.parse("OFF")).getName(),     GuiUtil.getMessage(I18nUtilities.getResourceString("loglevel.OFF"))),
        new Option ((Level.parse("INFO")).getName(),    GuiUtil.getMessage(I18nUtilities.getResourceString("loglevel.DEFAULT"))),
    };

    /**
     *	<p> This constructor is not used.</p>
     */
    private JBILogLevelsPropertySheetAdaptor() 
    {
    }


    /**
     *	<p> This constructor saves the <code>LayoutComponent</code> descriptor
     *	    and the <code>UIComponent</code> associated with this
     *	    <code>PropertySheetAdaptor</code>.  This constructor is used by the
     *	    getInstance() method.</p>
     */
    protected JBILogLevelsPropertySheetAdaptor(LayoutComponent desc, UIComponent parent) 
    {
        super(desc, parent);
    }


    /**
     *	<p> This method provides access to an <code>JBILogLevelsPropertySheetAdaptor</code>
     *	    instance.  Each time it is invoked, it returns a new instance.</p>
     */
    public static JBILogLevelsPropertySheetAdaptor getInstance(FacesContext ctx, LayoutComponent desc, UIComponent parent) 
    {
        return new JBILogLevelsPropertySheetAdaptor(desc, parent);
    }


    /**
     *	<p> Method that is called to initialize the PropertySheet component.</p>
     */
    public void init() 
    {
        // Initialise the logger
        sLog = JBILogger.getInstance();

        // The parent UIComponent
        UIComponent parent = getParentUIComponent();

        // Retrieve the required option values
        mPropertySheetId           = getRequiredOptionValue("propertySheetId", parent);
        mPropertySheetSectionIdTag = getRequiredOptionValue("propertySheetSectionIdTag", parent);
        mPropertyIdTag             = getRequiredOptionValue("propertyIdTag", parent);
        mStaticTextIdTag           = getRequiredOptionValue("staticTextIdTag", parent);
        mDropDownIdTag             = getRequiredOptionValue("dropDownIdTag", parent);
        mHiddenFieldIdTag          = getRequiredOptionValue("hiddenFieldIdTag", parent);
    }


    /**
     *	<p> Method that is called to retrieve the property sheet object.
     * 
     *	@param	parent - The parent component
     *  @return PropertySheet component
     */
    public UIComponent getPropertySheet(UIComponent parent) 
    {
        constructPropertySheet(parent);
        return parent;
    }

    
    /**
     * Helper class that will create the property sheet components.
     */
    private UIComponent constructPropertySheet(UIComponent parent) 
    {
        String componentName  = getRequiredOptionValue("componentName", parent);
        String instanceName   = getRequiredOptionValue("instanceName", parent);
        String targetName     = (String)getOptionValue("targetName", parent);
        String additionalFile = (String)getOptionValue("additionalLoggerFile", parent);

        // Initialize the Save button to be enabled
        LoggingBean loggingBean = BeanUtilities.getLoggingBean();
        loggingBean.setSaveButtonDisabled(false);

        // Retrieve any default value.  This would be used instead in the dropdown
        // instead of the level value read from the MBean.
        String defaultLevel  = (String)getOptionValue("dropDownDefaultLevel", parent);

        boolean PE_Flag = false;
        if (targetName == null)
        {
            PE_Flag = true;
            targetName = instanceName;
        }

        Map logLevels = getLoggerLevels(componentName,targetName,instanceName);
        if (logLevels == null)
        {
            logLevels = new HashMap();
        }

        // Add any additional appserver loggers.  Note, this uses the xml file
        // AdditionalLoggers.xml file located in the com/sun/jbi/config folder.
        if (additionalFile != null)
        {
            logLevels = SystemLoggerUtilities.addAdditionalSystemLoggers (logLevels, 
                                                    componentName, 
                                                    targetName,
                                                    additionalFile);
        }

        UIComponent propertySheetSection = getPropertySheetSectionComponent(parent);

        if (logLevels.size() == 0)
        {
            UIComponent propertyComponent = getPropertyComponent(propertySheetSection);
            if (PE_Flag) {
                Object[] args = {componentName};
                String msg = GuiUtil.getMessage(I18nUtilities.getResourceString("jbi.configure.loggers.pe.page.no.loggers"), args);
                ((Property)propertyComponent).setLabel(msg);
            }
            else {
                Object[] args = {componentName, instanceName};
                String msg = GuiUtil.getMessage(I18nUtilities.getResourceString("jbi.configure.loggers.page.no.loggers"), args);
                ((Property)propertyComponent).setLabel(msg);
            }
            loggingBean.setSaveButtonDisabled(true);
        } else
        {
            Set logLevelSet = null;
            Level level = null;
            logLevelSet = logLevels.keySet();
            Iterator iter = logLevelSet.iterator();
            while (iter.hasNext())
            {
                String name = (String)iter.next();
                String label = null;
                String loggerTagName = (String)SystemLoggerUtilities.loggerNames.get(name);
                if (loggerTagName != null)
                {
                    label = (String)SystemLoggerUtilities.loggerLabels.get(loggerTagName.toLowerCase());
                }
                else
                {
                    label = extractLoggerDisplayName(name);
                }
                level = (Level)logLevels.get(name);
                String displayName = "(" + name + ")";

                // If a default value was given, then we want to use that as the level
                if (defaultLevel != null)
                {
                    level = Level.parse(defaultLevel);
                }

                UIComponent propertyComponent    = getPropertyComponent(propertySheetSection);
                UIComponent dropDownComponent    = getDropDownComponent(propertyComponent);
                UIComponent staticTextComponent  = getStaticTextComponent(propertyComponent);
                UIComponent hiddenFieldComponent = getHiddenFieldComponent(propertyComponent);

                ((Property)propertyComponent).setLabelAlign("left");
                ((Property)propertyComponent).setNoWrap(true);
                ((Property)propertyComponent).setOverlapLabel(false);
                ((Property)propertyComponent).setLabel(label);
                ((DropDown)dropDownComponent).setItems(mLogLevelOptions);
                ((DropDown)dropDownComponent).setSelected(level.getName());
                ((StaticText)staticTextComponent).setText(displayName);
                ((HiddenField)hiddenFieldComponent).setText(name);
            } 
        }
        return parent;
    }


    /**
     * Helper class that will extract the logger display from the logger name.
     */
    private String extractLoggerDisplayName (String aFullname)
    {
        String displayName = aFullname;
        int index = displayName.lastIndexOf(".");
        if (index > 0)
        {
            displayName = aFullname.substring(index+1);
            if (displayName.length() > 0)
            {
                displayName = capitalize(displayName);
            }
        }
        return displayName;
    }


    /**
     * Helper class that is used to capitalize the first letter in a string.
     */
    private static String capitalize(String s) {
        char chars[] = s.toCharArray();
        chars[0] = Character.toUpperCase(chars[0]);
        return new String(chars);
    }


    /**
     *	<p> This method returns the "options" that should be supplied to the
     *	    factory that creates the <code>PropertySheet</code>.</p>
     *
     *	<p> Some useful options for the standard <code>PropertySheet</code>
     *	    component include:<p>
     *
     * <ul><li>propertySheetId</li>
     * <li>propertySheetSectionIdTag</li>
     * <li>propertyIdTag</li>
     * <li>staticTextIdTag</li>
     * <li>dropDownIdTag</li>
     * <li>dropDownDefaultLevel</li>
     * <li>hiddenFieldIdTag</li>
     * <li>componentName</li>
     * <li>targetName</li>
     * <li>instanceName</li>
     * <li>propertySheetAdaptorClass</li></ul>
     *
     *	<p> See PropertySheet component documentation for more details.</p>
     */
    public Map<String, Object> getFactoryOptions() 
    {
        Map<String, Object> result = null;
        LayoutComponent desc = getLayoutComponent();
        result = new HashMap<String, Object>();
        setProperty(result, "propertySheetId", desc.getOption("propertySheetId"));
        setProperty(result, "propertySheetSectionIdTag", desc.getOption("propertySheetSectionIdTag"));
        setProperty(result, "propertyIdTag", desc.getOption("propertyIdTag"));
        setProperty(result, "staticTextIdTag", desc.getOption("staticTextIdTag"));
        setProperty(result, "dropDownIdTag", desc.getOption("dropDownIdTag"));
        setProperty(result, "dropDownDefaultLevel", desc.getOption("dropDownDefaultLevel"));
        setProperty(result, "hiddenFieldIdTag", desc.getOption("hiddenFieldIdTag"));
        setProperty(result, "componentName", desc.getOption("componentName"));
        setProperty(result, "targetName", desc.getOption("targetName"));
        setProperty(result, "instanceName", desc.getOption("instanceName"));
        setProperty(result, "propertySheetAdaptorClass", desc.getOption("propertySheetAdaptorClass"));
        return result;
    }


    /**
     *	<p> Helper method for setting Properties while avoiding NPE's.</p>
     */
    private void setProperty(Map props, String key, Object value) 
    {
        if (value != null)
        {
            props.put(key, value);
        }
    }


    /**
     *	<p> This method returns any facets that should be applied to the
     *	    <code>PropertySheetNode (comp)</code>.  Useful facets for the sun
     *	    <code>PropertySheetNode</code> component are: "content" and "image".</p>
     *
     *	<p> Facets that already exist on <code>comp</code>, or facets that
     *	    are directly added to <code>comp</code> do not need to be returned
     *	    from this method.</p>
     *
     *	<p> This implementation directly adds a "content" facet and returns
     *	    <code>null</code> from this method.</p>
     *
     *	@param	comp	    The PropertySheet node <code>UIComponent</code>.
     *	@param	nodeObject  The (model) object representing the PropertySheet node.
     */
    public Map<String, UIComponent> getFacets(UIComponent comp, Object nodeObject) 
    {
        return null;
    }


    /**
     *	<p> Advanced framework feature which provides better handling for
     *	    things such as expanding PropertySheetNodes, beforeEncode, and other
     *	    events.</p>
     *
     *	<p> This method should return a <code>Map</code> of <code>List</code>
     *	    of <code>Handler</code> objects.  Each <code>List</code> in the
     *	    <code>Map</code> should be registered under a key that cooresponds
     *	    to to the "event" in which the <code>Handler</code>s should be
     *	    invoked.</p>
     */
    public Map getHandlersByType(UIComponent comp, Object nodeObject) 
    {
        return null;
    }


    /**
     *	<p> Given the component, target and instance name, this routine will call
     *      jbi api method to retrieve the logger names and log level.
     * 
     *	@param	componentName - The name of the component
     *	@param	targetName - The name of the target
     *	@param	instanceName - The name of the instance
     *  @return Map containing the logger names and the log levels
     */
    
    private Map getLoggerLevels(String componentName,
                                String targetName,
                                String instanceName)
    {
        Map result = null;
        try
        {
            if (null != mJac)
            {
                sLog.fine("JBILogLevelsPropertySheetAdaptor - getLoggerLevels: " +
                          "componentName=" + componentName + 
                          ", targetName=" + targetName +
                          ", instanceName=" + instanceName);
                result = mJac.getComponentLoggerLevels(componentName,
                                                       targetName,
                                                       instanceName);
            }
        } catch (com.sun.jbi.ui.common.JBIRemoteException jbiRemoteEx)
        {
            sLog.fine("JBILogLevelsPropertySheetAdaptor(): caught jbiRemoteEx=" + jbiRemoteEx);
        }
        return result;
    }



}
