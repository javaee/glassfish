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
 * $Header: /cvs/glassfish/admin-cli/cli-api/src/cpp/solaris-monitoring/AttributeInfo.h,v 1.3 2005/12/25 03:45:25 tcfujii Exp $
 * $Revision: 1.3 $
 * $Date: 2005/12/25 03:45:25 $
 */
 
#include <string>
#include "FailureException.h"

/*
	All types are returned as strings, but AttributeType describes the semantic content. 
 */
typedef enum
{
	StringAttributeType,
	IntegerAttributeType,
	LongAttributeType,
	BooleanAttributeType,
	DecimalAttributeType
} AttributeType;

/*
	Contains all the information needed for an attribute; may be extended later
 */
class AttributeInfo
{
private:
	const std::string		mName;
	const std::string		mDescription;	// optional, may be blank
	const AttributeType		mAttributeType;
	
		static inline bool
	isValidNameChar( const char c )
	{
		return( ( c >= 'a' && c <= 'z' ) ||
				( c >= 'A' && c <= 'Z' ) ||
				( c >= '0' && c <= '9' ) ||
				( c == '_' ) 
			);
	}
	
		static const char *
	validateName( const char * name) throw ( FailureException )
	{
		if ( name == NULL || name[ 0 ] == '\0' )
		{
			throw FailureException( "name cannot be null or empty" );
		}
		
		char c	= name[ 0 ];
		if ( ( c >= '0' && c <= '9' ) )
		{
			throw FailureException( "name cannot start with a digit" );
		}

		const char *	cur	= name;
		while ( (c = *cur++) != '\0' )
		{
			if ( ! isValidNameChar( c ) )
			{
				throw FailureException( "name must consist of upper/lower case letters, digits and '_'" );
			}
		}
		
		return( name );
	}

public:

	AttributeInfo(
		const char * const		name,
		const AttributeType		attributeType )  throw ( FailureException )
		:	mName( validateName( name ) ),
			mDescription( "" ),
			mAttributeType( attributeType )
	{
	}
	
	AttributeInfo(
		const char * const			name,
		const char * const			description,
		const AttributeType			attributeType )  throw ( FailureException )
		:	mName( validateName( name ) ),
			mDescription( description ),
			mAttributeType( attributeType )
	{
	}
	
	AttributeInfo( const AttributeInfo &  rhs )
		:	mName( rhs.mName ),
			mDescription( rhs.mDescription ),
			mAttributeType( rhs.mAttributeType )
	{
	}
	
		virtual
	~AttributeInfo()
	{
	}
	
		std::string const &
	getName()
	{
		return( mName );
	}
	
		std::string const &
	getDescription()
	{
		return( mDescription );
	}
	
		AttributeType
	getAttributeType()
	{
		return( mAttributeType );
	}
};





