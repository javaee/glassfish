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

package com.sun.enterprise.cli.framework;


import java.util.Vector;
import java.io.Serializable;

/**
 * Definition for the valid option
 *    @version  $Revision: 1.4 $
 */
public class ValidOption implements Serializable
{

    // name of the option
    private String name;
    // short name for the option
    private Vector shortNames = new Vector();
    // type of the option
    private String type;
    // is the value required for this option
    private int required;
    // default value for this option
    private String defaultValue = null;
    // corresponding deprecated option if any
    private String deprecatedOption = null;

    // option value is required
    public static final int REQUIRED = 1;
    // option value is optional
    public static final int OPTIONAL = 2;
    // option value is not required
    public static final int NOT_REQUIRED = 3;
    
    
    /** Creates new ValidOption */
    public ValidOption() 
    {
    }

    
    /** 
     Overloaded constructor for ValidOption
     @param name the name of the option
     @param type the datatype of this option
     @param required specifies if the value for this option is required or optional
     @param defaultValue the default value for this option if nonce specified on command line
     */
    public ValidOption(String name, String type, int required, 
                        String defaultValue )
    {
        this.name = name;
        this.type = type;
        this.required = required;
        this.defaultValue = defaultValue;
    }

    
    /** copy constructor for ValidOption
        @param vo
    */
    public ValidOption(ValidOption vo)
    {
        this.name = vo.name;
        this.shortNames = vo.shortNames;
        this.type = vo.type;
        this.required = vo.required; 
        this.defaultValue = vo.defaultValue;
        this.deprecatedOption = vo.deprecatedOption;
    }
    

    
    /**
     Gets the name of the option
     @return the name of the option
     */
    public String getName() 
    {
        return name;
    }
    
    
    /**
     Sets the name of the option
     @param name name of the option to set
     */
    public void setName(String name) 
    {
        this.name = name;
    }
    
    
    /**
     Returns the short name of the option
     @return the short name
     */
    public Vector<String> getShortNames()
    {
        return shortNames;
    }
    
    
    /**
     Sets the short option name
     @param shortName short name to set
     */
    public void setShortName(String shortName)
    {
        this.shortNames.add(shortName);
    }
    
    
    /**
     Sets the short option names
     @param shortNames short names to set
     */
    public void setShortNames(Vector shortNames)
    {
        this.shortNames.addAll(shortNames);
    }
    
    
    /**
     Returns the type of the option value
     @return the type
     */
    public String getType() 
    {
        return type;
    }
    
    
    /**
     Sets the type of the option value
     @param type the type to set
     */
    public void setType(String type) 
    {
        this.type = type;
    }
    
    
    /**
     Checks to see if the value is required
     @return the value required
     */
    public int isValueRequired() 
    {
        return required;
    }
    
    
    /**
     Sets the required field of the option
     @param isValueRequired the value to set
     */
    public void setRequired(int isValueRequired) 
    {
        required = isValueRequired;
    }
    
    
    /**
     Returns the default value of the option
     @return the default value
     */
    public String getDefaultValue() 
    {
        return defaultValue;
    }
    
    
    /**
     Sets the default value of the option
     @param defaultValue the default value to set
     */
    public void setDefaultValue(String defaultValue) 
    {
        this.defaultValue = defaultValue;
    }
    
    
    /**
     Returns the deprecated option
     @return the deprecated option
     */
    public String getDeprecatedOption()
    {
        return deprecatedOption;
    }
    
    
    /**
     Sets the deprecated option
     @param deprecatedOption option value
     */
    public void setDeprecatedOption(String deprecatedOption) 
    {
        this.deprecatedOption = deprecatedOption;
    }
    
    
    /**
     Checks to see if there is a default value for this option
     @return if there is a default value
     */
    public boolean hasDefaultValue()
    {
        return (defaultValue != null);
    }
    
    
    /**
     Checks to see if there is a short option name for this option
     @return if there is a short option name
     */
    public boolean hasShortName()
    {
        if (shortNames.size() > 0)
        {
            return true;
        }
        return false;
    }

    
    /**
     Checks to see if there is a default value for this option
     @return if there is a default value
     */
    public boolean hasDeprecatedOption()
    {
        return (deprecatedOption != null);
    }
    
    
    /**
     converts the object to string
      returns toString() of this object
     */
    public String toString()
    {
        String shortNamesStr = "";
        
        for (int i = 0; i < shortNames.size(); i++)
        {
            shortNamesStr += shortNames.get(i) + ",";
        }
        return (name + " " + type + " " + shortNamesStr + " " + defaultValue);
    }
}
