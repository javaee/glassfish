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

package com.sun.enterprise.admin.mbeans;

import com.sun.enterprise.admin.dottedname.*;

import javax.management.MBeanServerConnection;
import javax.management.Attribute;
import javax.management.ObjectName;
import java.util.Arrays;

/**
 * This was originally an inner class within DottedNameGetSetMBeanImpl. For
 * extensibility reasons this was moved out as a separate class.
 * @author <a href=mailto:lloyd.chambers@sun.com>Lloyd Chambers</a>
 * @author <a href=mailto:shreedhar.ganapathy@sun.com>Shreedhar Ganapathy</a>
 *         Date: Jun 9, 2004
 * @version $Revision: 1.3 $
 */
/*
	Implementing subclass for Config dotted names
 */
public class DottedNameGetSetForConfig extends DottedNameGetSetMBeanBase
  {
    final DottedNameResolverForAliases	mResolver;
    final DottedNameQuery				mQuery;


    public DottedNameGetSetForConfig(
        final MBeanServerConnection conn,
        final DottedNameRegistry	registry,
        final DottedNameServerInfo	serverInfo )
    {
        super( conn, registry, serverInfo );

        // DottedNameResolver for regular dotted names needs to account for aliases.
        mResolver	= new DottedNameResolverForAliases( registry, serverInfo );

        mQuery		= new DottedNameAliasedQuery( registry, serverInfo );
    }



    protected DottedNameResolver
    getResolver( )
    {
        return( mResolver );
    }

    public Object [] dottedNameSet( final String [] nameValuePairs )
    {
        final int	numItems	= nameValuePairs.length;

        // make a new array of sorted input pairs
        // do this first, because resulting output may contain a mix of type--
        // Attribute or Exception making it hard to sort properly.
        final String []		sortedPairs	= new String [ numItems ];
        for( int i = 0; i < numItems; ++i )
        {
            sortedPairs[ i ]	= nameValuePairs[ i ];
        }
        Arrays.sort( sortedPairs );


        final Object []		results	= new Object [ sortedPairs.length ];
        for( int i = 0; i < numItems; ++i )
        {
            results[ i ]	= dottedNameSet( sortedPairs[ i ] );
        }

        return( results );
    }

    /*
        Return either an Attribute or a Throwable to indicate status.
     */
    protected    Object dottedNameSet( final String nameValuePair )
    {
        Object	result	= null;

        try
        {
            result	= doSet( nameValuePair );
        }
        catch( Exception e )
        {
            // the result will be the exception itself
            logException( e );
            result	= e;
        }

        assert( result != null );
        return( result );
    }

     public   Attribute
    doSet( String pair )
        throws Exception
    {
        final int		delimIndex	= pair.indexOf( ASSIGNMENT_DELIM );
        final boolean	delete		= delimIndex < 0;

        // if there is no value delimiter ('='), this means to delete the property
        // this is only supported for properties however
        final String	dottedNameString	= delete ? pair : pair.substring( 0, delimIndex );
        final String	valueString			= delete ? null : pair.substring( delimIndex + 1, pair.length() );

        final Attribute attr	= doSet( dottedNameString, valueString );

        return( attr );
    }


    public    Attribute
    doSet( String dottedNameString, String value )
        throws Exception
    {
        // NOTE: this name includes the value-name
        final DottedName	dn	= getDottedName( dottedNameString );

        if ( dn.isWildcardName() )
        {
            final String	msg	= DottedNameStrings.getString(
                    DottedNameStrings.WILDCARD_DISALLOWED_FOR_SET_KEY,
                    dottedNameString );

            throw new IllegalArgumentException( msg );
        }

        ObjectName	target	= null;
        final DottedNameForValue	dnv	= new DottedNameForValue( dn );
        if ( isDottedNameForServerName( dnv.getPrefix(), dnv.getValueName() ) )
        {
            // YUCK special case "server.name" or otherwise the attribute will
            // be set on the config
            target	= getRegistry().dottedNameToObjectName( dnv.getPrefix().toString() );
        }
        else
        {
            target	= getTarget( dnv, getResolver( ) );
        }

        final Attribute	inAttr	= new Attribute( dnv.getValueName(), value );

        Attribute resultAttr	= mValueAccessor.setValue( target, inAttr );

        // special meaning of result with null value is that it has been deleted (yuck)
        // in this case, it is not added to the output list
        if ( resultAttr != null && resultAttr.getValue() != null )
        {
            final String	fullName	= dnv.getPrefix() + "." + inAttr.getName();

            resultAttr	= new Attribute( fullName, resultAttr.getValue() );
        }

        return( resultAttr );
    }


        protected DottedNameQuery
    createQuery(  )
    {
        return( mQuery );
    }


        boolean
    isServerName( String name )
        throws DottedNameServerInfo.UnavailableException
    {
        boolean	isServerName	= false;

        isServerName	= mServerInfo.getServerNames().contains( name );

        return( isServerName );
    }

        protected boolean
    isDottedNameForServerName( final DottedName prefix, final String valueName )
        throws DottedNameServerInfo.UnavailableException
    {
        return( valueName.equals( "name" ) &&
            isServerName( prefix.getScope() ) &&	// these tests make sure it's just "<server>.name"
            prefix.getParts().size() == 0 );
    }

    /*
        Create an Attribute for a given prefix, value name and associated value.

        We override for special cases that aliasing doesn't work correctly;
        namely when names alias into config.

        Aliasing can't work properly in this case because aliasing works on prefixes;
        the attributes are determine only after an alias has been resolved.
     */
    protected Attribute
    formAttribute( final DottedName prefix, final String valueName, Object value )
    {
        // default behavior
        Attribute	attr	= super.formAttribute( prefix, valueName, value );

        try
        {
            // Is it a special case for server.xxx which is into 'server' itself?
            if ( isDottedNameForServerName( prefix, valueName ) )
            {
                // <server>.name value has been set to <config>.name value
                // skip the aliasing and get the name from the server object
                final ObjectName	objectName	= getRegistry().dottedNameToObjectName( prefix.getScope() );

                final Attribute	newAttr	= mValueAccessor.getValue( objectName, valueName );
                assert( newAttr != null );
                attr	= super.formAttribute( prefix, valueName, newAttr.getValue() );
            }
        }
        catch( Exception e)
        {
            logException( e );
        }

        return( attr );
    }
}
