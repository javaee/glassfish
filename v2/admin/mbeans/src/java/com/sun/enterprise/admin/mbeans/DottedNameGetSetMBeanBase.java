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
import com.sun.enterprise.admin.dottedname.valueaccessor.*;

import javax.management.*;
import java.util.*;
import java.util.regex.Pattern;



/*
 *  Base class for dotted name access. This was originally an inner class within
 *  DottedNameGetSetMBeanImpl. It was moved out as a separate class for
 *  extensibility reasons.
 *
 *  @author <a <a href=mailto:lloyd.chambers@sun.com>Lloyd Chambers</a>
 *  @author <a href=mailto:shreedhar.ganapathy@sun.com>Shreedhar Ganapathy</a>
 *         Date: Jun 9, 2004
 * @version $Revision: 1.3 $
 */
abstract public class DottedNameGetSetMBeanBase{
    final MBeanServerConnection		mConn;
    final DottedNameRegistry		mRegistry;
    protected final ValueAccessor				mValueAccessor;
    final DottedNameServerInfo		mServerInfo;

    public final static char	ASSIGNMENT_DELIM	= '=';

    /*
        Instantiate with a reference to an MBeanServerConnection which will be used
        as the server when searching for required objects (which is possibly different
        than the MBeanServer in which this object will be registered).

        Due to a bug in the server startup sequence, this is the only allowed
        constructor; avoiding the bug requires the SunoneInterceptor as 'conn'.com
        Ideally the only constructor would be one that takes no arguments, and obtains
        its MBeanServerConnection from preRegister().
     */
        public
    DottedNameGetSetMBeanBase(
        final MBeanServerConnection		conn,
        final DottedNameRegistry		registry,
        final DottedNameServerInfo		serverInfo )
    {
        mConn			= conn;

        mRegistry		= registry;

        mValueAccessor	= new AnyValueAccessor( mConn );

        mServerInfo		= serverInfo;
    }


    abstract DottedNameQuery	createQuery( );
    abstract DottedNameResolver	getResolver();



        protected MBeanServerConnection
    getMBS()
    {
        return( mConn );
    }

    protected DottedNameRegistry
    getRegistry( )
    {
        return( mRegistry );
    }

        protected static ObjectName
    getTarget( final DottedNameForValue dottedName, final DottedNameResolver resolver)
        throws Exception
    {
        final DottedName	prefixDN	= dottedName.getPrefix();

        final ObjectName theObject	= resolver.resolveDottedName( prefixDN.toString() );

        if ( theObject == null )
        {
            final String	msg	= DottedNameStrings.getString(
                    DottedNameStrings.OBJECT_INSTANCE_NOT_FOUND_KEY, dottedName.toString() );

            throw new InstanceNotFoundException( msg );
        }

        return( theObject );
    }


    /*
        static void
    dm( Object o )
    {
        System.out.println( o.toString() );
    }
        static void
    dm( Object o )
    {
        DottedNameLogger.dm( o );
    }
    */

    protected static void
    logException( final Exception e )
    {
        DottedNameLogger.logException( e );
    }


    /*
        Can override the creation of the attribute eg filtering the output.
     */
        Attribute
    formAttribute( final DottedName prefix, final String valueName, Object value )
    {
        return( new Attribute( prefix + "." + valueName, value ) );
    }


    /*
      get the value for a single dotted name which must not be a wildcard name
     */
    protected    void
    doGet( final String dottedNameString, final AttributeList attrsOut )
        throws Exception
    {
        // NOTE: this name includes the value-name
        final DottedName			dn	= getDottedName( dottedNameString );
        final DottedNameForValue	dnv	= new DottedNameForValue( dn );

        final ObjectName			target		= getTarget( dnv, getResolver( ) );
        final String				valueName	= dnv.getValueName();

        final Attribute	attr	= mValueAccessor.getValue( target, valueName);

        if ( attr != null )
        {
            // emit the name in its full form
            attrsOut.add( formAttribute( dnv.getPrefix(), valueName, attr.getValue() ) );
        }
    }

    public	void
    doGet( final Set dottedNames, final AttributeList attrsOut ) throws Exception
    {
        final Iterator	iter	= dottedNames.iterator();

        while ( iter.hasNext() )
        {
            try
            {
                doGet( (String)iter.next(), attrsOut );
            }
            catch( Exception e )
            {
                // propogate up up if it's a single item
                if ( dottedNames.size() == 1 )
                {
                    throw e;
                }
                else
                {
                    // do not log the exception if the attribute wasn't found;
                    // this is very common with wildcards
                    if ( ! (e instanceof AttributeNotFoundException) )
                    {
                        logException( e );
                    }
                }
            }
        }
    }

    static private final char	BACKSLASH	= '\\';
    /*
        We support only '*" and '?'-- the '.' is a literal character
     */
        protected static String
    convertWildcardStringToJavaFormat( final String wildcardString )
    {
        final int 			length	= wildcardString.length();
        final StringBuffer	buf	= new StringBuffer();

        for( int i = 0; i < length; ++i )
        {
            final char	theChar	= wildcardString.charAt( i );

            if ( theChar == '.' )
            {
                buf.append( "[.]" );
            }
            else if ( theChar == '*' )
            {
                buf.append( ".*" );
            }
            else if ( theChar == BACKSLASH )
            {
                buf.append( "" + BACKSLASH + BACKSLASH );
            }
            else
            {
                buf.append( theChar );
            }
        }
        return( buf.toString() );
    }


        private boolean
    startsWithDomain( final String dottedNameExpr )
    {
        return( dottedNameExpr.startsWith( DottedNameAliasSupport.DOMAIN_SCOPE ) );
    }

    protected boolean
    startsWithConfigName( final String dottedNameExpr )
    {
        boolean	startsWithConfig	= false;

        try
        {
            final Iterator	iter	= mServerInfo.getConfigNames().iterator();
            while ( iter.hasNext() )
            {
                final String	configName	= (String)iter.next();

                if ( dottedNameExpr.startsWith( configName ) )
                {
                    startsWithConfig	= true;
                    break;
                }
            }
        }
        catch( DottedNameServerInfo.UnavailableException e )
        {
            logException( e );
        }

        return( startsWithConfig );
    }

    /*
        Get the starting set to search, given a dottedName.

        The dotted name may be wildcarded or not; different sets may be selected
        based on special rules for aliasing:

        (1) if it starts with DOMAIN_SCOPE or starts with a config name,
        then the non-aliased registry is selected.

        (2) otherwise, the aliased server names are used

        The caller will likely filter the resulting Set.
     */
    protected Set
    getSearchSet( final String dottedNameExpr )
    {
        final DottedName	dn		= getDottedName( dottedNameExpr );
        final String		scope	= dn.getScope();
        Set					s	= null;

        // consider it to be a domain if it starts with "domain"
        final boolean	isDomain	= scope.startsWith( DottedNameAliasSupport.DOMAIN_SCOPE );

        // consider it to be a config if it starts with a config name
        boolean	isConfig	= startsWithConfigName( dottedNameExpr );

        if ( isDomain || isConfig )
        {
            s	= getRegistry().allDottedNameStrings();
        }
        else
        {
            // this will create a search set consisting of everything "server."
            s	= createQuery().allDottedNameStrings();
        }

        return( s );
    }

    /*
        Resolve the (possibly wildcarded) dotted name prefix to a Set of dotted name Strings.
     */
    protected    Set
    resolveWildcardPrefix( final String dottedNamePrefix)
        throws DottedNameServerInfo.UnavailableException
    {
        Set	resolvedSet	= Collections.EMPTY_SET;

        if ( DottedName.isWildcardName( dottedNamePrefix ) )
        {
            if ( dottedNamePrefix.equals( "*" ) )	// optimization
            {
                resolvedSet	= createQuery().allDottedNameStrings();
            }
            else
            {
                final Set	searchSet	= getSearchSet( dottedNamePrefix );

                final String	regex	= convertWildcardStringToJavaFormat( dottedNamePrefix );

                final DottedNameWildcardMatcher matcher	=
                        new DottedNameWildcardMatcherImpl( searchSet );

                resolvedSet	= matcher.matchDottedNames( regex );
            }
        }
        else
        {
            resolvedSet	= Collections.singleton( dottedNamePrefix );
        }

        return( resolvedSet );
    }

    /*
        Given a dotted-name prefix, generate the dotted names for all values belonging to that
        prefix, as specified by the suffix, which is some wildcarded form.
    */
    protected    Set
    prefixToValueDottedNamesWild(
        final DottedNameResolver	resolver,
        final String				prefix,
        final String				suffix)
    {
        final Set			all	= new HashSet();

        try
        {
            final ObjectName	objectName	= resolver.resolveDottedName( prefix );

            if ( objectName != null )
            {
                Set	allValueNames	= null;
                PropertyValueAccessorBase prop_accessor = null;
                // wildcarded properties must not wildcard the "property" part
                // eg "property.<regex>"
                if ( suffix.equals( "*" ) )
                {
                    // all attributes *and* all properties
                    allValueNames	= getAllPropertyNames( new PropertyValueAccessor(getMBS()), objectName );
                    allValueNames.addAll(getAllPropertyNames( new SystemPropertyValueAccessor(getMBS()), objectName ));
                    allValueNames.addAll( getAllValueNames( getMBS(), objectName ) );
                }
                else if ((prop_accessor=(new PrefixedValueSupport(getMBS()).getPrefixedValueAccessor(suffix)))!=null)
                {
                    allValueNames	= getAllPropertyNames( prop_accessor, objectName );
                }
                else
                {
                    // any other expression should match just attributes
                    allValueNames	= getAllValueNames( getMBS(), objectName );
                }

                final Set	valuesDottedNames	= generateDottedNamesForValues( allValueNames, prefix, suffix );

                all.addAll( valuesDottedNames );
            }
        }
        catch( Exception e )
        {
            logException( e );
        }

        return( all );
    }

    /*
        Given a Set of dotted name prefixes, generate the Set of all dotted names for
        appropriate values on those prefixes.
    */
        Set
    prefixesToValueDottedNames(
        final DottedNameResolver	resolver,
        final Set					prefixes,
        final String				suffix )
    {
        final Set			all		= new HashSet();
        final Iterator		iter	= prefixes.iterator();

        while ( iter.hasNext() )
        {
            final String	prefix	= (String)iter.next();

            if ( DottedName.isWildcardName( suffix ) )
            {
                all.addAll( prefixToValueDottedNamesWild( resolver, prefix, suffix ) );
            }
            else
            {
                // a fixed non-wildcard value-name--just append it
                final String	dottedName	= prefix + "." + suffix;
                all.add( dottedName );

            }
        }

        return( all );
    }

        String
    setToString( final Set	s )
    {
        final Iterator	iter	= s.iterator();
        final StringBuffer	buf	= new StringBuffer();

        while ( iter.hasNext() )
        {
            buf.append( (String)iter.next() + "\n" );
        }

        return( buf.toString() );
    }


        String
    normalizeWildcardName( final String name )
    {
        String	normalizedName	= name;

        if ( name.equals( "*" ) )
        {
            normalizedName	= "*.*";
        }
        return( normalizedName );
    }

        protected Set
    resolveInputNames( final String [] names )
        throws DottedNameServerInfo.UnavailableException
    {
        final Set			all	= new HashSet();

        for( int i = 0; i < names.length; ++i )
        {
            String	name	= names[ i ];

            if ( DottedName.isWildcardName( name ) )
            {
                name	= normalizeWildcardName( name );

                final DottedName			dn		= getDottedName( name );
                final DottedNameForValue	dnv	= new DottedNameForValue( dn );

                final String	prefix		= dnv.getPrefix().toString();
                final String	valueName	= dnv.getValueName();

                final Set		resolvedPrefixes	= resolveWildcardPrefix( prefix );


                final Set	newDottedNames	= prefixesToValueDottedNames( getResolver( ),
                                                    resolvedPrefixes, valueName );


                all.addAll( newDottedNames );
            }
            else
            {
                all.add( name );
            }

        }

        return( all );
    }

    protected DottedName
    getDottedName( final String s )
    {
        return( DottedNameFactory.getInstance().get( s ) );
    }

    private final class AttributeComparator implements java.util.Comparator
    {
            public int
        compare( Object o1, Object o2 )
        {
            final Attribute	attr1	= (Attribute)o1;
            final Attribute	attr2	= (Attribute)o2;

            return( attr1.getName().compareTo( attr2.getName() ) );
        }

            public boolean
        equals( Object other )
        {
            return( other instanceof AttributeComparator );
        }
    }

        protected Attribute []
    sortAttributeList( final AttributeList attrsIn )
    {
        final Attribute []	attrs	= new Attribute[ attrsIn.size() ];
        attrsIn.toArray( attrs );

        Arrays.sort( attrs, new AttributeComparator() );

        return( attrs );
    }


        Object []
    dottedNameGet( final String [] names )
    {
        final Object []	results	= new Object[ names.length ];

        for( int i = 0; i < names.length; ++i )
        {
            results[ i ]	= dottedNameGet( names[ i ] );
        }

        return( results );
    }

        Object
    dottedNameGet( final String name )
    {
        Object	result	= null;

        try
        {
            final Set	all	= resolveInputNames( new String [] { name } );

            final AttributeList	attrs	= new AttributeList();
            doGet( all, attrs );

            if ( ! DottedName.isWildcardName( name )  )
            {
                // return an Attribute if the input was a single dotted-name
                assert( attrs.size() == 1 );
                result	= (Attribute)attrs.get( 0 );
            }
            else
            {
                result	= sortAttributeList( attrs );
            }
        }
        catch( Exception e )
        {
            logException( e );
            // the result will be the exception itself
            result	= e;
        }

        assert( result != null );
        return( result );
    }


    /*
        Return a Set of String of the names of all properties
        (INCLUDING the "property." prefix)
     */
        protected static Set
    getAllPropertyNames( PropertyValueAccessorBase accessor, final ObjectName objectName )
        throws Exception
    {
        final Set		allNames	= new HashSet();

        // add the property names
        final String []	propNames	= accessor.getAllPropertyNames( objectName, true );
        for( int i = 0; i < propNames.length; ++i )
        {
            // prepend the "properties." prefix
            allNames.add( propNames[ i ] );
        }

        return( allNames );
    }


    /*
        Return a Set of String of the names of all attributes and properties available within the MBean.com

        Properties are prefixed by "property" (DOTTED_NAME_PROPERTIES_PREFIX).
     */
        protected static Set
    getAllValueNames( final MBeanServerConnection conn, final ObjectName objectName )
        throws Exception
    {
        final Set		allNames	= new HashSet();

        allNames.addAll(getAllPropertyNames( new PropertyValueAccessor(conn), objectName ));
        allNames.addAll(getAllPropertyNames( new SystemPropertyValueAccessor(conn), objectName ));
        allNames.addAll( AttributeValueAccessor.getAllAttributeNames( conn, objectName ) );

        return( allNames );
    }

    /*
        Return a Set of String (dotted names)
     */
        protected static Set
    generateDottedNamesForValues(
        final Set			valueNames,
        final String		prefix,
        final String		suffix )
    {
        final Iterator	iter	= valueNames.iterator();
        final Set		allDottedNameStrings	= new HashSet();

        final Pattern	pattern	= Pattern.compile( convertWildcardStringToJavaFormat( suffix ) );

        while ( iter.hasNext() )
        {
            final String	valueName	= (String)iter.next();

            if ( pattern.matcher( valueName ).matches() )
            {
                allDottedNameStrings.add( prefix + "." + valueName );
            }
        }

        return( allDottedNameStrings );
    }


    /*
        Get all children of a name prefix.
     */
    protected	Set
    getAllDescendants( final String namePrefix )
    {
        final Set	searchSet	= getSearchSet( namePrefix );

        // a child must be prefix.xxx
        final String searchPrefix	= namePrefix + ".";

        final Set	resultSet	= new HashSet();
        final Iterator	iter	= searchSet.iterator();
        while ( iter.hasNext() )
        {
            final String	candidateString	= (String)iter.next();

            if ( candidateString.startsWith( searchPrefix ) )
            {
                resultSet.add( candidateString );
            }
        }

        return( resultSet );
    }


    /*
        Find all immediate children of the prefix.  An "immediate child" must have
        one more name part than its parent.
     */
    protected	Set
    getAllImmediateChildren( final String namePrefix )
    {
        final Set	allChildren	= getAllDescendants( namePrefix );

        final int			numParentParts	= getDottedName( namePrefix ).getParts().size();
        final Iterator		iter			= allChildren.iterator();

        final Set	resultSet	= new HashSet();
        while ( iter.hasNext() )
        {
            final String	descendant	= (String)iter.next();

            if ( getDottedName( descendant ).getParts().size() == numParentParts + 1 )
            {
                resultSet.add( descendant );
            }
        }

        return( resultSet );
    }

        protected Set
    getAllTopLevelNames()
    {
        final Set	all	= new HashSet();
        final Set	searchSet	= createQuery().allDottedNameStrings();

        // a child must be prefix.xxx
        final Iterator	iter	= searchSet.iterator();
        while ( iter.hasNext() )
        {
            final String		candidateString	= (String)iter.next();
            final DottedName	dn	= getDottedName( candidateString );

            if ( dn.getParts().size() == 0 )
            {
                all.add( candidateString );
            }
        }
        return( all );
    }

    protected Set
    doList( final String [] namePrefixes )
        throws DottedNameServerInfo.UnavailableException
    {
        final Set	all	= new HashSet();

        for( int i = 0; i < namePrefixes.length; ++i )
        {
            final String	dottedNamePrefix	= namePrefixes[ i ];

            Set	resolved	= null;
            if ( DottedName.isWildcardName( dottedNamePrefix ) )
            {
                resolved	= resolveWildcardPrefix( dottedNamePrefix );
            }
            else
            {
                // no wildcard means to list all immediate children of the prefix
                resolved	= getAllImmediateChildren( dottedNamePrefix );
            }

            all.addAll( resolved );
        }

        return( all );
    }

    public	String []
    dottedNameList( final String [] namePrefixes )
    {
        Set	all	= Collections.EMPTY_SET;

        try
        {
            // if nothing specified, get top-level names
            all	= (namePrefixes.length == 0) ?
                    getAllTopLevelNames() : doList( namePrefixes );
        }
        catch( Exception e )
        {
            logException( e );
        }

        final String []	allArray	= new String [ all.size() ];
        all.toArray( allArray );

        Arrays.sort( allArray );
        return( allArray );
    }


        protected DottedNameServerInfo
    getServerInfo( )
    {
        return( mServerInfo );
    }
}






