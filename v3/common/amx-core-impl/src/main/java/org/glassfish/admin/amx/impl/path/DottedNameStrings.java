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
package org.glassfish.admin.amx.impl.path;

import org.glassfish.admin.amx.impl.path.*;
import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
   Message strings for various messages and errors.
 */
public final class DottedNameStrings
{
	private	DottedNameStrings()	{}
	private static final ResourceStringSource	mImpl	= initImpl();
	
	private final static String	PACKAGE_NAME		= "org.glassfish.admin.amx.impl.dotted";
	private final static String	STRINGS_FILENAME	= "DottedNameStrings";	// .properties
	
		private static ResourceStringSource
	initImpl()
	{
		ResourceStringSource	src	= null;
		
		try
		{
			src	= new ResourceStringSource( PACKAGE_NAME, STRINGS_FILENAME );
		}
		catch( MissingResourceException e)
		{
			e.printStackTrace();
		}
		
		return( src );
	}
	
		public static String
	getString( String	key )
	{
		if ( mImpl == null )
		{
			return( key );
		}

		return( mImpl.getString( key ) );
	}
	
		public static String
	getString( String	key, final Object toInsert)
	{
		return( getString( key, new Object [] { toInsert } ) );
	}
	
		public static String
	getString( String	key, final Object toInsert1, final Object toInsert2)
	{
		return( getString( key, new Object [] { toInsert1, toInsert2 } ) );
	}
	
		public static String
	getString( String	key, final Object[] toInsert)
	{
		if ( mImpl == null )
		{
			return( key );
		}
		
		return( mImpl.getString( key, toInsert) );
	}
	
	
	public final static String	OBJECT_INSTANCE_NOT_FOUND_KEY	= "ObjectInstanceNotFound";
	public final static String	MALFORMED_DOTTED_NAME_KEY		= "MalformedDottedName";
	public final static String	WILDCARD_DISALLOWED_FOR_SET_KEY	= "WildcardDisallowedForSet";
	public final static String	ATTRIBUTE_NOT_FOUND_KEY			= "AttributeNotFound";
	public final static String	ILLEGAL_TO_SET_NULL_KEY			= "IllegalToSetNull";
	public final static String	ILLEGAL_CHARACTER_KEY			= "IllegalCharacter";
	public final static String	MISSING_EXPECTED_NAME_PART_KEY	= "MissingExpectedNamePart";
	public final static String	DOTTED_NAME_MUST_HAVE_ONE_PART_KEY		= "DottedNameMustHaveAtLeastOnePart";
	public final static String	NO_VALUE_NAME_SPECIFIED_KEY		= "NoValueNameSpecified";
	
	
}


class ResourceStringSource
{
	private final String         	mPackageName;
	private final String          	mPropertyFile;
	private final ResourceBundle	mResourceBundle;
	
	public static String   DEFAULT_STRING_VALUE = "Key not found";

	/** Creates a new instance of LocalStringsManager */
		public
	ResourceStringSource(String packageName, String propertyFile) 
	{
	    mPackageName		= packageName;
	    mPropertyFile		= propertyFile;
	    mResourceBundle 	=  ResourceBundle.getBundle( packageName + "." + propertyFile);
	}

	public String getPropertiesFile()
	{
	    return mPropertyFile;
	}

	public String getPackageName()
	{
	    return mPackageName;
	}

	public String getString(String key)
	{
		return( getString( key, DEFAULT_STRING_VALUE + " (" + key + ")" ) );
	}
	
	public String getString(String key, String defaultValue)
	{
	    String	value = defaultValue;
	    
        try
        {
            value = mResourceBundle.getString(key);
        }
        catch (MissingResourceException mre)
        {
        	// return the default string
        }
	    return value;
	}

	/*
	 *  Return the Localized string with the inserted values
	 */
	public String getString(String key, Object[] toInsert) 
	{
	    String	template = getString(key);
	    String	result	= template;
	    
        final MessageFormat msgFormat	=  new MessageFormat( template );
        result		= msgFormat.format(toInsert);
	        
	    return result;
	}
}
