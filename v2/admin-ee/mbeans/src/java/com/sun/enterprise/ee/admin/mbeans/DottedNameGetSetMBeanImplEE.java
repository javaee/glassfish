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

package com.sun.enterprise.ee.admin.mbeans;

import com.sun.enterprise.ee.admin.dottedname.*;
import com.sun.enterprise.ee.admin.dottedname.DottedNameStrings;
import com.sun.enterprise.admin.dottedname.*;
import com.sun.enterprise.admin.dottedname.valueaccessor.PropertyValueAccessorBase;
import com.sun.enterprise.admin.dottedname.valueaccessor.PropertyValueAccessor;
import com.sun.enterprise.admin.dottedname.valueaccessor.SystemPropertyValueAccessor;
import com.sun.enterprise.admin.dottedname.valueaccessor.PrefixedValueSupport;
import com.sun.enterprise.admin.mbeans.DottedNameGetSetMBeanImpl;
import com.sun.enterprise.admin.mbeans.DottedNameGetSetForConfig;
import com.sun.enterprise.admin.mbeans.DottedNameGetSetMBeanBase;
import com.sun.enterprise.admin.util.ArrayConversion;
import com.sun.enterprise.util.i18n.StringManager;


import javax.management.*;
import java.util.*;

/**
 * @author <a href=mailto:shreedhar.ganapathy@sun.com>Shreedhar Ganapathy</a>
 *         Date: Jun 2, 2004
 * @version $Revision: 1.1.1.1 $
 */
public class DottedNameGetSetMBeanImplEE extends DottedNameGetSetMBeanImpl{
    final DottedNameGetSetForConfig  mConfigImpl;
    final DottedNameServerInfoCacheEE mServerInfo;
    final DottedNameClusterInfoCache mClusterInfo;
    private static final StringManager _strMgr =
                                        StringManager.getManager(DottedNameGetSetMBeanImplEE.class);

    public DottedNameGetSetMBeanImplEE(final MBeanServerConnection conn,
                                       final DottedNameRegistry registry,
                                       final DottedNameRegistry monitoringRegistry)
                                       throws NotCompliantMBeanException,
                                              MalformedObjectNameException {

        super(conn, registry, monitoringRegistry);
        mServerInfo =  new DottedNameServerInfoCacheEE((DottedNameServerInfoEE) createServerInfo(conn));
		mClusterInfo = new DottedNameClusterInfoCache(createClusterInfo(conn));
        mConfigImpl	 = new DottedNameGetSetForConfigEE(conn, registry, mServerInfo, mClusterInfo );
    }

    protected DottedNameClusterInfo createClusterInfo(final MBeanServerConnection conn) {
        return( new DottedNameClusterInfoImpl(conn));
    }

    protected DottedNameServerInfo createServerInfo(final MBeanServerConnection conn){
		return( new DottedNameServerInfoImplEE( conn ) );
    }

    protected void pre() {
        mClusterInfo.refresh();
        mServerInfo.refresh();
        super.pre();
    }

    protected Object [] dottedNameAnyGet(
            final DottedNameGetSetMBeanBase impl, final String [] names ) {

        pre();
        return( super.dottedNameAnyGet(impl, names) );
    }

    public Object [] dottedNameGet( final String [] names ) {
        return( dottedNameAnyGet( mConfigImpl, names ) );
    }

    public Object dottedNameGet( final String name ) {
        final Object	result	= dottedNameAnyGet( mConfigImpl, name );
        return( result );
    }

    public Object [] dottedNameSet( final String [] nameValuePairs ) {
        pre();

       final Object [] results	= mConfigImpl.dottedNameSet( nameValuePairs );

        assert( checkSetResults( results ) );
        assert( results.length == nameValuePairs.length );

        return( convertArrayType( results ) );
    }

/*    public Object dottedNameSet( final String nameValuePair ) {
        final Object [] results	= dottedNameSet( new String [] { nameValuePair } );

        return( results[ 0 ] );
    }
  */

    public String [] dottedNameList( final String [] namePrefixes ) {
        pre();

        return( mConfigImpl.dottedNameList( namePrefixes ) );
    }

    /**
     * specialization class for operations that include clusters and servers.
     */
    class DottedNameGetSetForConfigEE extends DottedNameGetSetForConfig {
	    final DottedNameResolverForAliases	mResolver;
	    final DottedNameQuery				mQuery;
        final DottedNameRegistry            mRegistry;
        DottedNameGetSetForConfigEE(final MBeanServerConnection conn, final DottedNameRegistry registry,
            final DottedNameServerInfo serverInfo, final DottedNameClusterInfo clusterInfo){

            super( conn, registry, serverInfo );
            mRegistry = registry;
            // DottedNameResolver for regular dotted names needs to account for aliases.
            mResolver	= new DottedNameResolverForAliasesEE( registry, serverInfo, clusterInfo);
            mQuery		= new DottedNameAliasedQueryEE( registry, serverInfo, clusterInfo );
        }

        protected Set doList( final String [] namePrefixes )
            throws DottedNameServerInfo.UnavailableException
        {
            return super.doList(namePrefixes);
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

            if ( dn.getParts().size() == 0
                    && ! mClusterInfo.isClusteredInstance(candidateString))
            {
                all.add( candidateString );
            }
        }
        return( all );
        }


        /*
            Get the starting set to search, given a dottedName.

            The dotted name may be wildcarded or not; different sets may be selected
            based on special rules for aliasing:

            (1) if it starts with DOMAIN_SCOPE or starts with a config name,
            then the non-aliased registry is selected.

            (2) otherwise, the aliased server/cluster names are used

            The caller will likely filter the resulting Set.
         */
        protected Set getSearchSet( final String dottedNameExpr )   {
            final DottedName	dn		= getDottedName( dottedNameExpr );
            final String		scope	= dn.getScope();
            final Set					s;

            // consider it to be a domain if it starts with "domain"
            final boolean	isDomain	= scope.startsWith( DottedNameAliasSupport.DOMAIN_SCOPE );

            // consider it to be a config if it starts with a config name
            final boolean	isConfig	= startsWithConfigName( dottedNameExpr );

            if ( isDomain || isConfig ) {
                s	= getRegistry().allDottedNameStrings();
            }
            else  {
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
                    resolvedSet = resolveAppsOrResourcesToTarget(getDottedName(dottedNamePrefix), resolvedSet, resolvedSet);
                    resolvedSet = resolveDomainSvrsCfgsClstrsQuery(getDottedName(dottedNamePrefix), resolvedSet, resolvedSet);
                }
                else
                {
                    final Set	searchSet	= getSearchSet( dottedNamePrefix );

                    resolvedSet = performRegexpMatch(dottedNamePrefix, searchSet);

                    resolvedSet = resolveAppsOrResourcesToTarget(getDottedName(dottedNamePrefix), searchSet, resolvedSet);
                    resolvedSet = resolveDomainSvrsCfgsClstrsQuery(getDottedName(dottedNamePrefix), searchSet, resolvedSet);
                }
            }
            else
            {
                resolvedSet	= Collections.singleton( dottedNamePrefix );
            }

            return( resolvedSet );
        }

        private Set performRegexpMatch(final String dottedNamePrefix, final Set searchSet) {
            final Set resolvedSet;
            final String	regex	= convertWildcardStringToJavaFormat( dottedNamePrefix );

            final DottedNameWildcardMatcher matcher	=
                    new DottedNameWildcardMatcherImpl( searchSet );

            resolvedSet	= matcher.matchDottedNames( regex );
            return resolvedSet;
        }

        private Set resolveDomainSvrsCfgsClstrsQuery(final DottedName prefixDottedName, final Set searchSet, final Set resolvedSet) {
            String scope = prefixDottedName.getScope();
            scope = scope.indexOf("*") <= 0 ? scope : scope.substring(0,scope.indexOf("*"));
            if(scope.equals("*")
                    ||
                DottedNameAliasSupportEE.scopeIsDomain(scope) &&
                    prefixDottedName.getParts().size()==0 ){

                addSpecialCaseResults(resolvedSet, searchSet);

            } else if(DottedNameAliasSupportEE.scopeIsDomain(scope) &&
                    prefixDottedName.getParts().size()>=1)
            {
                String prefix = prefixDottedName.toString();
                if(prefixDottedName.getPart(0).equals("*")){
                    addSpecialCaseResults(resolvedSet, searchSet);
                }

                if(prefix.indexOf("*")>0) {
                    prefix = prefix.substring(0, prefix.indexOf("*"));
                    if(prefix.endsWith("."))
                        prefix = prefix.substring(0,prefix.lastIndexOf("."));

                    if(isSpecialCaseQuery(prefix) ){
                        try {
                            resolvedSet.addAll(getSpecialCaseChildren(prefix));
                        } catch (Exception e) {
                            logException(e);
                        }
                    }
                }
            }
            return resolvedSet;
        }

        private void addSpecialCaseResults(final Set resolvedSet, final Set searchSet) {
            try {
                final HashMap s = getSpecialCases();
                for(Iterator keys = s.keySet().iterator(); keys.hasNext();){
                    for(Iterator values  = ((Collection)s.get(keys.next())).iterator();
                        values.hasNext();)
                    {
                         resolvedSet.addAll(performRegexpMatch((String)values.next(),searchSet));
                    }
                }
            } catch (Exception e) {
                logException(e);
            }
        }

        private Set resolveAppsOrResourcesToTarget(final DottedName prefixDottedName, final Set searchSet, final Set resolvedSet) {
            String candidate;
            String searchCand;
            DottedName candidateDN, searchCandDN;
            final Set result = new HashSet();

            if(!prefixDottedName.getScope().startsWith(DottedNameAliasSupport.DOMAIN_SCOPE))
            {
                if((prefixDottedName.getParts().size()==0) ||
                        (prefixDottedName.getParts().size()>0 &&
                        prefixDottedName.getPart(0).matches(".*resources.*") ||
                        prefixDottedName.getPart(0).matches(".*applications.*")))
                {
                    for(Iterator resolved=resolvedSet.iterator(); resolved.hasNext();)
                    {
                        candidate=(String)resolved.next();
                        candidateDN = getDottedName(candidate);
                        if(!DottedNameAliasSupportEE.scopeIsDomain(candidateDN.getScope()) &&
                                candidate.matches(".*resources.*") ||
                                candidate.matches(".*applications.*"))
                        {
                            for(Iterator search=searchSet.iterator();search.hasNext();)
                            {
                                searchCand = (String)search.next();
                                searchCandDN = getDottedName(searchCand);

                                if(!searchCandDN.getParts().isEmpty()){
                                    if( searchCandDN.getPart(0).equals("resource-ref")
                                    || searchCandDN.getPart(0).equals("application-ref"))
                                    {
                                        if(candidateDN.getParts().size()>1 &&
                                            candidateDN.getScope().equals(searchCandDN.getScope()))
                                        {
                                           if(candidateDN.getPart(1).equals(
                                                searchCandDN.getPart(1))
                                              ||
                                              candidateDN.getPart(2).equals(
                                                searchCandDN.getPart(1)))
                                            {
                                                result.add(candidate);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        else{
                            result.add(candidate);
                        }
                    }
                    return result;
                }
            }
            return resolvedSet;
        }

        /*
            Get all children of a name prefix.
         */
        protected Set
        getAllDescendants( final String namePrefix )
        {
            final Set	searchSet	= getSearchSet( namePrefix );

            // a child must be prefix.xxx
            String searchPrefix	= namePrefix + ".";

            Set	resultSet	= new HashSet();
            final Iterator	iter	= searchSet.iterator();
            final String scope = getDottedName(namePrefix).getScope();
            while ( iter.hasNext() )
            {
                final String	candidateString	= (String)iter.next();

                if(!DottedNameAliasSupport.scopeIsDomain(scope)){
                    //NOTE:refactored prior code here into methods for easier code reading
                    resultSet = filterAppAndRef(namePrefix, candidateString, DottedName.escapePart(scope), resultSet);
                    resultSet = filterResAndRef(namePrefix, candidateString, DottedName.escapePart(scope), resultSet);
                    // end refactor block
                    if ( candidateString.startsWith( searchPrefix ) ) {
                       resultSet.add( candidateString );
                    }
                }
                else{
                    // NOTE:for "list domain.node-agents", the response is expected to be
                    // domain.node-agent.<na_name>....(without the plural). Since dottednames for  node-agent are
                    // already available in that form,  popping the "s" from the query (domain.node-agent's' )
                    // is the easier route to return results.
                    searchPrefix = getNodeAgentsSearchPrefix(namePrefix, searchPrefix);
                    if ( candidateString.startsWith( searchPrefix ) ) {
                       resultSet.add( candidateString );
                    }
                }
            }
            return( resultSet );
        }

        //see note in method above.
        private String getNodeAgentsSearchPrefix(final String namePrefix, String searchPrefix) {
            final DottedName dn =getDottedName(namePrefix);
            if(dn.getParts().size()>0 && dn.getPart(0).equalsIgnoreCase("node-agents")){
                // pop the plural "s" from namePrefix (domain.node-agent's') and match
                // for the remaining string i.e. "domain.node-agent."
                searchPrefix = namePrefix.substring(0,namePrefix.length()-1) + ".";
            }
            return searchPrefix;
        }

        //see refactor note in method getAllDescendants()
        private Set filterResAndRef(final String namePrefix,
                                    final String candidateString,
                                    final String scope,
                                    final Set resultSet)
        {
            if(namePrefix.matches(".*resources$")){
                if(candidateString.startsWith(scope+".resource-ref")){
                   resultSet.add(candidateString);
                }
                else if(candidateString.startsWith(scope+".resources")){
                    resultSet.add(candidateString);
                }
            }
            return resultSet;
        }

        //see refactor note in method getAllDescendants()
        private Set filterAppAndRef(final String namePrefix,
                                        final String candidateString,
                                        final String scope,
                                        final Set resultSet)
        {
            if(namePrefix.matches(".*applications$")){
                if(candidateString.startsWith(scope+".application-ref")){

                    resultSet.add(candidateString);
                }
                else if(candidateString.startsWith(scope+".applications")){
                    resultSet.add(candidateString);
                }
            }
            return resultSet;
        }

        /*
            Find all immediate children of the prefix.  An "immediate child" must have
            one more name part than its parent.
         */
        protected Set
        getAllImmediateChildren( final String namePrefix )
        {
            //NOTE:added this block to deal with special cases such as domain.clusters, domain.servers and domain.configs
            // as these are direct queries to MBean APIs and not part of the parsing model used for other dotted names.
            if(isSpecialCaseQuery(namePrefix)){
                try {
                    return getSpecialCaseChildren(namePrefix);
                } catch (Exception e) {
                    logException(e);
                }
            }
            // end new block
            Set	allChildren	= getAllDescendants( namePrefix );
            final DottedName namePrefixDn =getDottedName( namePrefix );
            final int			numParentParts	= namePrefixDn.getParts().size();
            if(!namePrefixDn.getScope().equalsIgnoreCase(DottedNameAliasSupport.DOMAIN_SCOPE)){
                if(containsAppRes(namePrefixDn)){
                    allChildren  = resolveRefToSrc(allChildren);
                }
            }

            final Iterator		iter = allChildren.iterator();

            final Set	resultSet	= new HashSet();
            DottedName dn;
            while ( iter.hasNext() ) {
                String	descendant	= (String)iter.next();

                dn = getDottedName(descendant);
                if(containsServerRef(dn)){
                    descendant = replaceServerRefWithSrc(descendant);
                    dn = getDottedName(descendant);
                }
                if(numParentParts==0){
                    if (dn.getParts().size() == numParentParts + 1 )
                    {
                        resultSet.add( descendant );
                    }
                }
                else {
                    if(dn.getParts().size() <= numParentParts + 2)
                    {
                        resultSet.add( descendant );
                    }
                }
            }

            return( resultSet );
        }

        //see note in method getAllImmediateChildren()
        private Set getSpecialCaseChildren(final String namePrefix) throws Exception {
            return (Set) getSpecialCases().get(namePrefix);
        }

        //see note in method getAllImmediateChildren()
        private final HashMap getSpecialCases() throws Exception {
            final HashMap specialCases	= new HashMap();
            specialCases.put("domain.clusters", escapeNames(mClusterInfo.getClusterNames()));
            specialCases.put("domain.servers",  escapeNames(mServerInfo.getServerNames()));
            specialCases.put("domain.configs", escapeNames(mClusterInfo.getConfigNames()));

            return specialCases;
        }

        private Set escapeNames(final Set targetNames) {
            final Set escTargets = new HashSet();
            for(Iterator i = targetNames.iterator(); i.hasNext();){
                escTargets.add(DottedName.escapePart((String)i.next()));
            }
            return escTargets;
        }

        //see note in method getAllImmediateChildren()
        private boolean isSpecialCaseQuery(final String namePrefix) {
            final DottedName dn  = getDottedName(namePrefix);
            try {
                if(dn.getParts().size()>0 &&
                    getSpecialCases().keySet().contains(namePrefix))
                {
                    return true;
                }
            } catch (Exception e) {
                logException(e);
            }
            return false;
        }

        private String replaceServerRefWithSrc(final String descendant) {
            String temp = descendant.substring(0, descendant.indexOf("."));
            temp += descendant.substring(descendant.lastIndexOf("."), descendant.length());
            return temp;
        }

        private Set resolveRefToSrc(final Set children) {
            final Set resultSet = new HashSet();
            final Iterator iter =children.iterator();

            while(iter.hasNext()){
                final String candidate =(String) iter.next();
                if(candidate.matches(".+application-ref.+")||
                    candidate.matches(".+resource-ref.+")){

                    final Iterator tempIter = children.iterator();
                    while(tempIter.hasNext()){
                        final String cand = (String)tempIter.next();
                        if(cand.matches(".+applications.+") ||
                           cand.matches(".+resources.+")){
                            if(cand.endsWith(
                                candidate.substring(
                                    candidate.lastIndexOf(".")+1,
                                        candidate.length()))){

                                resultSet.add(cand);
                                resultSet.add(candidate);
                                break;
                            }
                        }
                    }
                }
            }
            return resultSet;
        }

        private final Set APP_RES_SET	= ArrayConversion.toSet( new String []
            {
                // these are prefixes
                "applications",
                "resources"
            } );

        private boolean containsAppRes(final DottedName dn) {
            boolean	contains	= false;

            final java.util.List	parts	= dn.getParts();

            if ( parts.size() >= 1 )
            {
                contains	= APP_RES_SET.contains( parts.get( 0 ) );
            }

            return( contains );
        }

        private boolean containsServerRef(final DottedName dn){
            boolean contains = false;
            if(((String)dn.getParts().get(0)).equalsIgnoreCase("server-ref")) {
                contains = true;
            }
            return contains;
        }

        protected DottedNameQuery createQuery( ){
            return mQuery;
        }

        public Object []  dottedNameSet( final String [] nameValuePairs ) {
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
        protected  Object  dottedNameSet( final String nameValuePair ) {
            Object	result;

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

        protected DottedNameResolver  getResolver( ) {
          return( mResolver );
        }

        boolean isDottedNameForClusterName( final DottedName prefix, final String valueName )
        throws DottedNameServerInfo.UnavailableException {

          return( valueName.equals( "name" ) &&
            isClusterName( prefix.getScope() ) &&	// these tests make sure it's just "<server>.name"
            prefix.getParts().size() == 0 );
        }

        private boolean isClusterName(final String scope)
            throws DottedNameServerInfo.UnavailableException {

            return mClusterInfo.getClusterNames().contains( scope );
        }

        protected boolean
        startsWithConfigName( final String dottedNameExpr )
        {
            boolean	startsWithConfig	= false;

            try
            {
                final Iterator	iter	= mClusterInfo.getConfigNames().iterator();
                while ( iter.hasNext() )
                {
                    final String	configName	= (String)iter.next();

                    if ( dottedNameExpr.startsWith( configName ) )
                    {
                        startsWithConfig	= true;
                        break;
                    }
                }
                if(!startsWithConfig){
                    startsWithConfig = super.startsWithConfigName(dottedNameExpr);
                }
            }
            catch( DottedNameServerInfo.UnavailableException e )
            {
                logException( e );
            }

            return( startsWithConfig );
        }

        public    Attribute
        doSet( final String dottedNameString, final String value )
            throws Exception
        {
            // NOTE: this name includes the value-name
            final DottedName	dn	= getDottedName( dottedNameString );

            if ( dn.isWildcardName() )
            {
                final String	msg	= _strMgr.getString(
                        DottedNameStrings.WILDCARD_DISALLOWED_FOR_SET_KEY,
                        dottedNameString );

                throw new IllegalArgumentException( msg );
            }

            Attribute resultAttr;
            //if scope is not domain, and not config name, and
            // dotted name contains parts then we check if
            // dotted name contains shared elements
            if(!DottedNameAliasSupportEE.scopeIsDomain(dn.getScope())
                    &&
                !startsWithConfigName(dn.getScope())
                    &&
                dn.getParts().size() > 0 )
            {

                try{
                    checkShared(dn);
                }
                catch(Exception e){
                    resultAttr = new Attribute(e.getClass().getName(), e.getMessage());
                    return resultAttr;
                }
            }

            final ObjectName	target;
            final DottedNameForValue	dnv	= new DottedNameForValue( dn );
            final String prefix = dnv.getPrefix().toString();
            if ( isDottedNameForClusterName( dnv.getPrefix(), dnv.getValueName()) ||
                    isDottedNameForServerName(dnv.getPrefix(), dnv.getValueName() ))
            {
                target	= getRegistry().dottedNameToObjectName( dnv.getPrefix().toString() );
            }
            else
            {
                if(prefix.equalsIgnoreCase(DottedNameAliasSupport.DOMAIN_SCOPE) ||
                        prefix.indexOf(".") < 0 ||
                        dnv.getValueName().matches(SystemPropertyValueAccessor.NAME_PREFIX+".+"))
                {
                    target = mRegistry.dottedNameToObjectName(prefix);
                }
                else{
                    target	= getTarget( dnv, getResolver( ) );
                }
            }

            if(target == null){
                throw new UnavailableException(_strMgr.getString(DottedNameStrings.OBJECT_INSTANCE_NOT_FOUND_KEY, prefix));
            }
            
            final Attribute	inAttr	= new Attribute( dnv.getValueName(), value );

            resultAttr	= mValueAccessor.setValue( target, inAttr );

            // special meaning of result with null value is that it has been deleted (yuck)
            // in this case, it is not added to the output list
            if ( resultAttr != null && resultAttr.getValue() != null )
            {
                final String	fullName	= dnv.getPrefix() + "." + inAttr.getName();

                resultAttr	= new Attribute( fullName, resultAttr.getValue() );
            }
            return( resultAttr );
        }

        private void checkShared(final DottedName dn) throws Exception {
            checkSharedResource(dn);
            checkSharedApplication(dn);
            checkSharedConfig(dn);
        }

        private void checkSharedConfig(final DottedName dn) throws UnavailableException {
            if(!isApplicationsQuery(dn) && !isResourcesQuery(dn)
                && !isAppOrResRefQuery(dn)){
                try {
                    final String resolvedScope =
                            DottedNameAliasSupportEE.resolveScope(mClusterInfo, mServerInfo, dn);
                    if(!startsWithConfigName(resolvedScope)){
                        return;
                    }

                    if(mClusterInfo.getTargetsSharingConfig(resolvedScope).length > 1)
                    {
                        if(!resolvedScope.equalsIgnoreCase(dn.getScope())){
                            final String	msg	= _strMgr.getString(
                                                    DottedNameStrings.SET_OPERATION_DISALLOWED_FOR_SHARED_CONFIGS_KEY,
                                                    resolvedScope, dn.getPart(dn.getParts().size()-1));
                            throw new SetDisallowedForSharedResourceException(msg);
                        }
                    }
                } catch (Exception e) {
                    throw new UnavailableException(getRootCause(e).getMessage());
                }
            }
        }

        private boolean isAppOrResRefQuery(final DottedName dn) {
            boolean retval = false;
            if(dn.getPart(0).equalsIgnoreCase("resource-ref")
                || dn.getPart(0).equalsIgnoreCase("application-ref")){

                retval = true;
            }
            return retval;
        }

        private boolean isResourcesQuery(final DottedName dn) {
            return dn.getPart(0).equalsIgnoreCase("resources");
        }

        private boolean isApplicationsQuery(final DottedName dn) {
            return dn.getPart(0).equalsIgnoreCase("applications");
        }

        private void checkSharedApplication(final DottedName dn)
                throws UnavailableException
        {
            if(isApplicationsQuery(dn)){
                try {
                    if(dn.getParts().size()>2){
                        final String appName =dn.getPart(2);

                        if(mClusterInfo.getTargetsSharingApplication(appName).length > 1)
                        {
                            final String	msg	= _strMgr.getString(
                                                    DottedNameStrings.SET_OPERATION_DISALLOWED_FOR_SHARED_APPLICATIONS_KEY,
                                                    appName, dn.getPart(dn.getParts().size()-1));
                            throw new SetDisallowedForSharedResourceException(msg);
                        }
                        else{
                            final String scope = dn.getScope();
                            if(mClusterInfo.getClusterNames().contains(scope))
                            {
                                if(! mClusterInfo
                                        .getApplicationNamesForCluster(scope)
                                        .contains(appName))
                                {
                                    final String	msg	= _strMgr.getString(
                                                            DottedNameStrings.APPLICATION_NOT_REFERENCED_BY_CLUSTER_KEY,
                                                            new Object[]{appName, scope, dn.getPart(dn.getParts().size()-1)});
                                    throw new UnavailableException(msg);
                                }
                            }
                            else if(mServerInfo.getUnclusteredServerNames().contains(scope))
                            {
                                if(! mServerInfo
                                        .getApplicationNamesForServer(scope)
                                        .contains(appName))
                                {
                                    final String	msg	= _strMgr.getString(
                                                            DottedNameStrings.APPLICATION_NOT_REFERENCED_BY_SERVER_KEY,
                                                            new Object[]{appName, scope, dn.getPart(dn.getParts().size()-1)});
                                    throw new UnavailableException(msg);
                                }
                            }
                        }
                    }
                    else{
                        final String	msg	= _strMgr.getString(
                                                DottedNameStrings.OBJECT_INSTANCE_NOT_FOUND_KEY,
                                                dn.toString());
                        throw new UnavailableException(msg);
                    }
                } catch (Exception e) {
                    logException(e);
                    throw new UnavailableException(e);
                }
            }
        }

        private void checkSharedResource(final DottedName dn) throws Exception{
            if(isResourcesQuery(dn)){
                if(dn.getParts().size()>2){
                    final String resName =dn.getPart(2);

                    if(mClusterInfo.getTargetsSharingResource(resName).length > 1){
                        final String	msg	= _strMgr.getString(
                                                DottedNameStrings.SET_OPERATION_DISALLOWED_FOR_SHARED_RESOURCES_KEY,
                                                resName, dn.getPart(dn.getParts().size()-1));
                        throw new SetDisallowedForSharedResourceException(msg);
                    }
                    else{
                        final String scope = dn.getScope();
                        if(mClusterInfo.getClusterNames().contains(scope))
                        {
                            if(! mClusterInfo
                                    .getResourceNamesForCluster(scope)
                                    .contains(resName))
                            {
                                final String	msg	= _strMgr.getString(
                                                        DottedNameStrings.RESOURCE_NOT_REFERENCED_BY_CLUSTER_KEY,
                                                        new Object[]{resName, scope, dn.getPart(dn.getParts().size()-1)});
                                throw new UnavailableException(msg);
                            }
                        }
                        else if(mServerInfo.getUnclusteredServerNames().contains(scope))
                        {
                            if(!scope.equals("server")&&
                                    ! mServerInfo
                                    .getResourceNamesForServer(scope)
                                    .contains(resName))
                            {
                                final String	msg	= _strMgr.getString(
                                                        DottedNameStrings.RESOURCE_NOT_REFERENCED_BY_SERVER_KEY,
                                                        new Object[]{resName, scope, dn.getPart(dn.getParts().size()-1)});
                                throw new UnavailableException(msg);
                            }
                        }
                    }
                }
                else{
                    final String	msg	= _strMgr.getString(
                                            DottedNameStrings.OBJECT_INSTANCE_NOT_FOUND_KEY,
                                            dn.toString());
                    throw new UnavailableException(msg);
                }
            }

        }

       /*
          get the value for a single dotted name which must not be a wildcard name
         */
        protected void
        doGet( final String dottedNameString, final AttributeList attrsOut)
            throws Exception
        {
            // NOTE: this name includes the value-name
            final DottedName			dn	= getDottedName( dottedNameString );
            final DottedNameForValue	dnv	= new DottedNameForValue( dn );

            final ObjectName			target;
            final String prefix = dnv.getPrefix().toString();
            if(prefix.equalsIgnoreCase(DottedNameAliasSupport.DOMAIN_SCOPE) ||
                    prefix.indexOf(".") < 0)
            {
                target = mRegistry.dottedNameToObjectName(prefix);
            }
            else {
                target = getTarget( dnv, getResolver( ) );
            }
            final String				valueName	= dnv.getValueName();

            if(target ==null){
                throw new UnavailableException(_strMgr.getString(DottedNameStrings.OBJECT_INSTANCE_NOT_FOUND_KEY, prefix));
            }
            final Attribute	attr	= mValueAccessor.getValue( target, valueName);

            if ( attr != null )
            {
                // emit the name in its full form
                attrsOut.add( formAttribute( dnv.getPrefix(), valueName, attr.getValue() ) );
            }
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
                //resolve the dotted name only if the prefix is not a single
                //element, for example: if the prefix is domain or only a
                // server/cluster/config name (with no further dotted elements),
                // then we do not resolve the dotted name to its config or domain equivalent.
                final ObjectName objectName;
                if(prefix.equalsIgnoreCase(DottedNameAliasSupport.DOMAIN_SCOPE) ||
                        (prefix.indexOf(".") < 0 ))
                {
                    objectName = mRegistry.dottedNameToObjectName(prefix);
                }
                else
                {
                    objectName	= resolver.resolveDottedName( prefix );
                }

                if ( objectName != null )
                {
                    final Set	allValueNames;
                    final PropertyValueAccessorBase prop_accessor;
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
        /**
            Get the chain of exceptions via getCause(). The first element is the
            Exception passed.

            @param start	the Exception to traverse
            @return		a Throwable[] or an Exception[] as appropriate
         */
        public Throwable[] getCauses( final Throwable start )
        {
            final ArrayList	list	= new ArrayList();

            boolean	haveNonException	= false;

            Throwable t	= start;
            while ( t != null )
            {
                list.add( t );

                if ( ! ( t instanceof Exception ) )
                {
                    haveNonException	= true;
                }

                final Throwable temp	= t.getCause();
                if ( temp == null )
                    break;
                t	= temp;
            }

            final Throwable[]	results	= haveNonException ?
                new Throwable[ list.size() ] : new Exception[ list.size() ];

            list.toArray( results );

            return( results );
        }


        /**
            Get the original troublemaker.

            @param e	the Exception to dig into
            @return		the original Throwable that started the problem
         */
        public Throwable getRootCause( final Throwable e )
        {
            final Throwable[]	causes	= getCauses( e );

            return( causes[ causes.length - 1 ] );
        }
    }

    class SetDisallowedForSharedApplicationException extends Exception{
        public SetDisallowedForSharedApplicationException(final String msg){
            super(msg);
        }

        public SetDisallowedForSharedApplicationException(final Throwable e){
            super(e);
        }

        public SetDisallowedForSharedApplicationException(final String msg, final Throwable e){
            super(msg, e);
        }
    }

    class SetDisallowedForSharedResourceException extends Exception{
        public SetDisallowedForSharedResourceException(final String msg) {
            super(msg);
        }

        public SetDisallowedForSharedResourceException(final Throwable e){
            super(e);
        }

        public SetDisallowedForSharedResourceException( final String msg, final Throwable e) {
            super(msg,e);
        }
    }

    class SetDisallowedForSharedConfigException extends Exception{
        public SetDisallowedForSharedConfigException(final String msg) {
            super(msg);
        }

        public SetDisallowedForSharedConfigException(final Throwable e){
            super(e);
        }

        public SetDisallowedForSharedConfigException( final String msg, final Throwable e) {
            super(msg,e);
        }
    }
}
