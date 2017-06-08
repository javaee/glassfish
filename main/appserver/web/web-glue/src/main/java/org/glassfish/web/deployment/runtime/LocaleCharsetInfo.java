/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package org.glassfish.web.deployment.runtime;

import com.sun.enterprise.deployment.runtime.RuntimeDescriptor;

/**
* this class contains runtime information for the web bundle 
* it was kept to be backward compatible with the schema2beans descriptors
* generated by iAS 7.0 engineering team.
*
* @author Jerome Dochez
*/
public class LocaleCharsetInfo extends RuntimeDescriptor
{
    
    static public final String LOCALE_CHARSET_MAP = "LocaleCharsetMap";	// NOI18N
    static public final String PARAMETER_ENCODING = "ParameterEncoding";	// NOI18N
    static public final String FORM_HINT_FIELD = "FormHintField"; // NOI18N
    static public final String DEFAULT_LOCALE = "DefaultLocale";  // NOI18N
    static public final String DEFAULT_CHARSET = "DefaultCharset";
    
    // This attribute is an array containing at least one element
    public void setLocaleCharsetMap(int index, LocaleCharsetMap value)
    {
	this.setValue(LOCALE_CHARSET_MAP, index, value);
    }
    
    //
    public LocaleCharsetMap getLocaleCharsetMap(int index)
    {
	return (LocaleCharsetMap)this.getValue(LOCALE_CHARSET_MAP, index);
    }
    
    // This attribute is an array containing at least one element
    public void setLocaleCharsetMap(LocaleCharsetMap[] value)
    {
	this.setValue(LOCALE_CHARSET_MAP, value);
    }
    
    //
    public LocaleCharsetMap[] getLocaleCharsetMap()
    {
	return (LocaleCharsetMap[])this.getValues(LOCALE_CHARSET_MAP);
    }
    
    // Return the number of properties
    public int sizeLocaleCharsetMap()
    {
	return this.size(LOCALE_CHARSET_MAP);
    }
    
    // Add a new element returning its index in the list
    public int addLocaleCharsetMap(LocaleCharsetMap value)
    {
	return this.addValue(LOCALE_CHARSET_MAP, value);
    }	
    
    //
    // Remove an element using its reference
    // Returns the index the element had in the list
    //
    public int removeLocaleCharsetMap(LocaleCharsetMap value)
    {
	return this.removeValue(LOCALE_CHARSET_MAP, value);
    }
    
    // This attribute is optional
    public void setParameterEncoding(boolean value)
    {
	this.setValue(PARAMETER_ENCODING, Boolean.valueOf(value));
    }
    
    //
    public boolean isParameterEncoding()
    {
	Boolean ret = (Boolean)this.getValue(PARAMETER_ENCODING);
	if (ret == null) {
	    return false;
	}
	return ret.booleanValue();
    }
    
    // This method verifies that the mandatory properties are set
    public boolean verify()
    {
	return true;
    }
}
