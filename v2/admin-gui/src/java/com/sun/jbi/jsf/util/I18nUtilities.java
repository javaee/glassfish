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
 *  I18nUtilities.java
 *
 */

package com.sun.jbi.jsf.util;


import java.util.Locale;
import java.util.ResourceBundle;
import javax.el.ELContext;
import javax.el.ExpressionFactory;
import javax.el.ValueExpression;
import javax.faces.application.Application;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import java.util.logging.Logger;

/**
 *
 * provides internationalization utilties
 *
 **/

public final class I18nUtilities
{
    /*
     *
     */
    public static final String ADMIN_CONSOLE_BUNDLE = "com.sun.enterprise.tools.admingui.resources.Strings";

  /**
     * Controls printing of diagnostic messages to the log
     */
	private static Logger sLog = JBILogger.getInstance();


    // Will return the text for the given resource string and resource bundle file.
    // If the given resource string is blank, then a blank string will be returned.
    // If the given resource string does not exist, the the resource string will be returned
    public static String getResourceString(String aResourceFile,
                                           String aResourceString,
                                           String aDefaultString) 
    {
        Locale loc = FacesContext.getCurrentInstance().getViewRoot().getLocale();
        String result = null;
        String resourceString = "";
        if (aResourceString == null)
        {
            resourceString = aDefaultString.trim();
        }
        else
        {
            resourceString = aResourceString.trim();
        }
        if (resourceString.length() > 0)
        {
            try
            {
                ResourceBundle resource = ResourceBundle.getBundle(aResourceFile, loc);
                result = resource.getString(resourceString);
            }
            catch (java.util.MissingResourceException e)
            {
                result = resourceString;
            }
        }

        return result;
    }

    /**
     * provides an internationalized resource String
     * @param aResourceKey
     * @param aDefaultValue
     * @return a resource value (or default value if key not found)
     */
    public static String getResourceString (String aResourceKey,
                                            String aDefaultValue)
    {
        String result = null;
        String resourceFile = ADMIN_CONSOLE_BUNDLE;
        result = getResourceString(resourceFile, aResourceKey, aDefaultValue);
        return result;
    }

    /* This Method accepts a string and searched the resource Bundle file
     * to get the matching Key for the value passed
     *@param aResourceString String value passed whose key is needed
     *                       from Resource Bundle file
     *@return matching key for the passed resourceString value
     */

    public static String getResourceString (String aResourceString)
    {
        String resourceFile = ADMIN_CONSOLE_BUNDLE;
        String str = getResourceString(aResourceString, "");
        return str;
    }

    public static String getStringPropertyUsingExpression(String anExpression)
    {
        String result = null;
        FacesContext fCtx = FacesContext.getCurrentInstance();
        ELContext elCtx = fCtx.getELContext();
        ExpressionFactory ef = fCtx.getApplication().getExpressionFactory();
        ValueExpression ve = ef.createValueExpression(elCtx, anExpression, String.class);
        result = (String) ve.getValue(elCtx);
        return result;
    }

    public static void setStringPropertyUsingExpression(String aStringValue, String anExpression)
    {
        FacesContext fCtx = FacesContext.getCurrentInstance();
        ELContext elCtx = fCtx.getELContext();
        ExpressionFactory ef = fCtx.getApplication().getExpressionFactory();
        ValueExpression ve = ef.createValueExpression(elCtx, anExpression, String.class);
        ve.setValue(elCtx, aStringValue);
    }

}


