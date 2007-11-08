/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * // Copyright (c) 1998, 2007, Oracle. All rights reserved.
 * 
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
package oracle.toplink.essentials.sessions;

import java.util.*;
import java.io.*;
import oracle.toplink.essentials.descriptors.ClassDescriptor;
import oracle.toplink.essentials.exceptions.ValidationException;
import oracle.toplink.essentials.internal.sessions.DatabaseSessionImpl;
import oracle.toplink.essentials.internal.helper.*;
import oracle.toplink.essentials.queryframework.SQLResultSetMapping;
import oracle.toplink.essentials.threetier.*;

/**
 * <b>Purpose</b>: Maintain all of the TopLink configuration information for a system.
 * <p><b>Responsibilities</b>:<ul>
 * <li> Project wide parameters (name, default for use method accessing, and base filepath for other INI files)
 * <li> JDBC Login information
 * <li> Descriptors
 * <li> Validate Descriptors (instance variable & method access)
 * <li> Maintain sequencing information & other project wide parameters
 * <li> Construct valid DatabaseLogin objects for each platform (with sequence info set)
 * </ul>
 *
 * @see DatabaseLogin
 */
public class Project implements Serializable, Cloneable {
    protected String name;
    protected Login datasourceLogin;
    protected Map descriptors;
    protected Vector orderedDescriptors;

    /** Holds the default set of read-only classes that apply to each UnitOfWork. */
    protected Vector defaultReadOnlyClasses;

    /** Cache the EJBQL descriptor aliases. */
    protected Map aliasDescriptors;

    /** Cache if any descriptor is isolated. (set during initialization) */
    protected boolean hasIsolatedClasses;
    /** Cache if any descriptor has history. (set during initialization) */
    protected boolean hasGenericHistorySupport;
    /** Cache if any descriptor is using ProxyIndirection. (set during initialization */
    protected boolean hasProxyIndirection;
    /** Cache if all descriptors are CMP1/2 */
    protected boolean isPureCMP2Project;
    
    /** This a collection of 'maps' that allow users to map custom SQL to query results */
    protected Map sqlResultSetMappings;

    /**
     * PUBLIC:
     * Create a new project.
     */
    public Project() {
        this.name = "";
        this.descriptors = new HashMap();
        this.defaultReadOnlyClasses = oracle.toplink.essentials.internal.helper.NonSynchronizedVector.newInstance();
        this.orderedDescriptors = oracle.toplink.essentials.internal.helper.NonSynchronizedVector.newInstance();
        this.hasIsolatedClasses = false;
        this.hasGenericHistorySupport = false;
        this.isPureCMP2Project = false;
        this.hasProxyIndirection = false;
    }

    /**
     * PUBLIC:
     * Create a new project that will connect through the login information.
     * This method can be used if the project is being create in code instead of being read from a file.
     */
    public Project(Login login) {
        this();
        this.datasourceLogin = login;
    }

    /**
     * PUBLIC:
     * Create a new project that will connect through JDBC using the login information.
     * This method can be used if the project is being create in code instead of being read from a file.
     */
    public Project(DatabaseLogin login) {
        this();
        this.datasourceLogin = login;
    }

    /**
     * PUBLIC:
     * Add the read-only class which apply to each UnitOfWork created by default.
     */
    public void addDefaultReadOnlyClass(Class readOnlyClass) {
        getDefaultReadOnlyClasses().addElement(readOnlyClass);
    }

    /**
     * PUBLIC:
     * Add the descriptor to the project.
     */
    public void addDescriptor(ClassDescriptor descriptor) {
        getOrderedDescriptors().add(descriptor);
        String alias = descriptor.getAlias();
        if (alias != null) {
            addAlias(alias, descriptor);
        }

        // Avoid loading class definition at this point if we haven't done so yet.
        if ((descriptors != null) && !descriptors.isEmpty()) {
            getDescriptors().put(descriptor.getJavaClass(), descriptor);
        }
    }

    /**
     * INTERNAL: Used by the BuilderInterface when reading a Project from INI files.
     */
    public void addDescriptor(ClassDescriptor descriptor, DatabaseSessionImpl session) {
        getOrderedDescriptors().add(descriptor);
        String alias = descriptor.getAlias();
        if (alias != null) {
            addAlias(alias, descriptor);
        }

        // Avoid loading class definition at this point if we haven't done so yet.
        if ((descriptors != null) && !descriptors.isEmpty()) {
            getDescriptors().put(descriptor.getJavaClass(), descriptor);
        }
        session.initializeDescriptorIfSessionAlive(descriptor);
    }

    /**
     * INTERNAL:
     * Add the descriptors to the session.
     * All persistent classes must have a descriptor registered for them with the session.
     * This method allows for a batch of descriptors to be added at once so that TopLink
     * can resolve the dependancies between the descriptors and perform initialization optimally.
     */
    public void addDescriptors(Vector descriptors, DatabaseSessionImpl session) {
        for (Enumeration enumeration = descriptors.elements(); enumeration.hasMoreElements();) {
            ClassDescriptor descriptor = (ClassDescriptor)enumeration.nextElement();
            getDescriptors().put(descriptor.getJavaClass(), descriptor);
            String alias = descriptor.getAlias();
            if (alias != null) {
                addAlias(alias, descriptor);
            }
        }

        if (session.isConnected()) {
            session.initializeDescriptors(descriptors);
            // The commit order must be maintain whenever new descriptors are added.
            session.getCommitManager().initializeCommitOrder();
        }

        getOrderedDescriptors().addAll(descriptors);
    }

    /**
     * PUBLIC:
     * Merge the descriptors from another project into this one.
     * All persistent classes must have a descriptor registered for them with the session.
     * This method allows for a batch of descriptors to be added at once so that TopLink
     * can resolve the dependancies between the descriptors and perform initialization optimially.
     */
    public void addDescriptors(Project project, DatabaseSessionImpl session) {
        Iterator descriptors = project.getDescriptors().values().iterator();
        while (descriptors.hasNext()) {
            ClassDescriptor descriptor = (ClassDescriptor)descriptors.next();
            getDescriptors().put(descriptor.getJavaClass(), descriptor);
            String alias = descriptor.getAlias();
            if (alias != null) {
                addAlias(alias, descriptor);
            }
        }

        if (session.isConnected()) {
            session.initializeDescriptors(project.getDescriptors());
            // The commit order must be maintained whenever new descriptors are added.
            session.getCommitManager().initializeCommitOrder();
        }

        getOrderedDescriptors().addAll(project.getOrderedDescriptors());
    }

    /**
     * PUBLIC:
     * Add a named SQLResultSetMapping to this project.  These SQLResultSetMappings
     * can be later used by ResultSetMappingQueries to map Custom sql results to
     * results as defined by the SQLResultSetMappings.
     */
    public void addSQLResultSetMapping(SQLResultSetMapping sqlResultSetMapping){
        if (sqlResultSetMapping == null || sqlResultSetMapping.getName() == null){
            return;
        }
        if (this.sqlResultSetMappings == null){
            this.sqlResultSetMappings = new HashMap();
        }
        this.sqlResultSetMappings.put(sqlResultSetMapping.getName(), sqlResultSetMapping);
    }
    
    /**
     * PUBLIC:
     * Set all this project's descriptors to conform all read queries within the context of the unit of work.
     */
    public void conformAllDescriptors() {
        Iterator descriptors = getDescriptors().values().iterator();
        while (descriptors.hasNext()) {
            ClassDescriptor descriptor = (ClassDescriptor)descriptors.next();
            descriptor.setShouldAlwaysConformResultsInUnitOfWork(true);
        }
    }

    /**
     * INTERNAL:
     * Convert all the class-name-based settings in this project to actual class-based
     * settings
     * @param classLoader 
     */
    public void convertClassNamesToClasses(ClassLoader classLoader){
        Iterator ordered = orderedDescriptors.iterator();
        while (ordered.hasNext()){
            ClassDescriptor descriptor = (ClassDescriptor)ordered.next();
            descriptor.convertClassNamesToClasses(classLoader);
        }
        // convert class names to classes for each SQLResultSetMapping
        if (sqlResultSetMappings != null) {
            for (Iterator mappingIt = sqlResultSetMappings.keySet().iterator(); mappingIt.hasNext();) {
                SQLResultSetMapping mapping = (SQLResultSetMapping) sqlResultSetMappings.get(mappingIt.next());
                mapping.convertClassNamesToClasses(classLoader);
            }
        }
    }

    /**
     * PUBLIC:
     * Switch all descriptors to assume existence for non-null primary keys.
     */
    public void assumeExistenceForDoesExist() {
        Iterator descriptors = getDescriptors().values().iterator();
        while (descriptors.hasNext()) {
            ClassDescriptor descriptor = (ClassDescriptor)descriptors.next();
            descriptor.getQueryManager().assumeExistenceForDoesExist();
        }
    }

    /**
     * PUBLIC:
     * Switch all descriptors to check the cache for existence.
     */
    public void checkCacheForDoesExist() {
        Iterator descriptors = getDescriptors().values().iterator();
        while (descriptors.hasNext()) {
            ClassDescriptor descriptor = (ClassDescriptor)descriptors.next();
            descriptor.getQueryManager().checkCacheForDoesExist();
        }
    }

    /**
     * PUBLIC:
     * Switch all descriptors to check the database for existence.
     */
    public void checkDatabaseForDoesExist() {
        Iterator descriptors = getDescriptors().values().iterator();
        while (descriptors.hasNext()) {
            ClassDescriptor descriptor = (ClassDescriptor)descriptors.next();
            descriptor.getQueryManager().checkDatabaseForDoesExist();
        }
    }

    /**
     * INTERNAL:
     * Clones the descriptor
     */
    public Object clone() {
        try {
            return super.clone();
        } catch (Exception exception) {
            return null;
        }
    }

    /**
     * PUBLIC:
     * Factory method to create session.
     * This returns an implementor of the DatabaseSession interface, which can be used to login
     * and add descriptors from other projects.  The Session interface however should be used for
     * reading and writing once connected for complete portability.
     */
    public DatabaseSession createDatabaseSession() {
        return new DatabaseSessionImpl(this);
    }

    /**
     * PUBLIC:
     * Factory method to create a server session.
     * This returns an implementor of the Server interface, which can be used to login
     * and add descriptors from other projects, configure connection pooling and acquire client sessions.
     */
    public Server createServerSession() {
        return new ServerSession(this);
    }

    /**
     * PUBLIC:
     * Factory method to create a server session.
     * This returns an implementor of the Server interface, which can be used to login
     * and add descriptors from other projects, configure connection pooling and acquire client sessions.
     * Configure the min and max number of connections for the default pool.
     */
    public Server createServerSession(int min, int max) {
        return new ServerSession(this, min, max);
    }

    /**
     * PUBLIC:
     * Factory method to create a server session.
     * This returns an implementor of the Server interface, which can be used to login
     * and add descriptors from other projects, configure connection pooling and acquire client sessions.
     * Configure the default connection policy to be used.
     * This policy is used on the "acquireClientSession()" protocol.
     */
    public Server createServerSession(ConnectionPolicy defaultConnectionPolicy) {
        return new ServerSession(this, defaultConnectionPolicy);
    }

    /**
     * PUBLIC:
     * Returns the default set of read-only classes.
     */
    public Vector getDefaultReadOnlyClasses() {
        return defaultReadOnlyClasses;
    }

    /**
     * PUBLIC:
     * Return the descriptor specified for the class.
     */
    public ClassDescriptor getClassDescriptor(Class theClass) {
		ClassDescriptor desc = getDescriptor(theClass);
		if (desc instanceof ClassDescriptor) {
			return (ClassDescriptor)desc;
		} else {
			throw ValidationException.cannotCastToClass(desc, desc.getClass(), ClassDescriptor.class);
		}
    }

    /**
     * PUBLIC:
     * Return the descriptor specified for the class.
     */
    public ClassDescriptor getDescriptor(Class theClass) {
        return (ClassDescriptor)getDescriptors().get(theClass);
    }

    /**
     * PUBLIC:
     * Return the descriptors.
     */
    public Map getDescriptors() {
        // Lazy initialize class references from orderedDescriptors when reading from XML.
        if (descriptors.isEmpty() && (!orderedDescriptors.isEmpty())) {
            for (Iterator iterator = orderedDescriptors.iterator(); iterator.hasNext();) {
                ClassDescriptor descriptor = (ClassDescriptor)iterator.next();
                descriptors.put(descriptor.getJavaClass(), descriptor);
            }
        }
        return descriptors;
    }

    /**
     * INTERNAL:
     * Return the descriptors in the order added.
     * Used to maitain consistent order in XML.
     */
    public Vector getOrderedDescriptors() {
        return orderedDescriptors;
    }

    /**
     * INTERNAL:
     * Set the descriptors order.
     * Used to maitain consistent order in XML.
     */
    public void setOrderedDescriptors(Vector orderedDescriptors) {
        this.orderedDescriptors = orderedDescriptors;
        for (Enumeration e = orderedDescriptors.elements(); e.hasMoreElements();) {
            ClassDescriptor descriptor = (ClassDescriptor)e.nextElement();
            String alias = descriptor.getAlias();
            if (alias != null) {
                addAlias(alias, descriptor);
            }
        }
    }

    /**
     * INTERNAL:
     * Return the login, the login holds any database connection information given.
     * This has been replaced by getDatasourceLogin to make use of the Login interface
     * to support non-relational datasources,
     * if DatabaseLogin API is required it will need to be cast.
     */
    public DatabaseLogin getLogin() {
        return (DatabaseLogin)datasourceLogin;
    }

    /**
     * PUBLIC:
     * Return the login, the login holds any database connection information given.
     * This return the Login interface and may need to be cast to the datasource specific implementation.
     */
    public Login getDatasourceLogin() {
        return datasourceLogin;
    }

    /**
     * PUBLIC:
     * get the name of the project.
     */
    public String getName() {
        return name;
    }

    /**
     * PUBLIC:
     * Get a named SQLResultSetMapping from this project.  These SQLResultSetMappings
     * can be used by ResultSetMappingQueries to map Custom sql results to
     * results as defined by the SQLResultSetMappings.
     */
    public SQLResultSetMapping getSQLResultSetMapping(String sqlResultSetMapping){
        if (sqlResultSetMapping == null || this.sqlResultSetMappings == null){
            return null;
        }
        return (SQLResultSetMapping)this.sqlResultSetMappings.get(sqlResultSetMapping);
    }
    
    /**
     * INTERNAL:
     * Answers if at least one Descriptor or Mapping had a HistoryPolicy at initialize time.
     */
    public boolean hasGenericHistorySupport() {
        return hasGenericHistorySupport;
    }

    /**
     * PUBLIC:
     * Set the read-only classes which apply to each UnitOfWork create by default.
     */
    public void setDefaultReadOnlyClasses(Vector newValue) {
        this.defaultReadOnlyClasses = (Vector)newValue.clone();
    }

    /**
     * INTERNAL:
     * Set the descriptors registered with this session.
     */
    public void setDescriptors(Map descriptors) {
        this.descriptors = descriptors;
        for (Iterator iterator = descriptors.values().iterator(); iterator.hasNext();) {
            ClassDescriptor descriptor = (ClassDescriptor)iterator.next();
            String alias = descriptor.getAlias();
            if (alias != null) {
                addAlias(alias, descriptor);
            }
        }
    }
 
    /**
     * INTERNAL:
     * Set to true during descriptor initialize if any descriptor has hsitory.
     */
    public void setHasGenericHistorySupport(boolean hasGenericHistorySupport) {
        this.hasGenericHistorySupport = hasGenericHistorySupport;
    }
    
    /**
     * INTERNAL:
     * Return if all descriptors are for CMP1 or CMP2 beans.
     * Set to true during descriptor initialize.
     * Allows certain optimizations to be made.
     */
    public boolean isPureCMP2Project() {
        return isPureCMP2Project;
    }
    
    /**
     * INTERNAL:
     * Set if all descriptors are for CMP1 or CMP2 beans.
     * Set to true during descriptor initialize.
     * Allows certain optimizations to be made.
     */
    public void setIsPureCMP2Project(boolean isPureCMP2Project) {
        this.isPureCMP2Project = isPureCMP2Project;
    }
    
    /**
     * INTERNAL:
     * Return if any descriptors are isolated.
     * Set to true during descriptor initialize if any descriptor is isolated.
     * Determines if an isolated client session is required.
     */
    public boolean hasIsolatedClasses() {
        return hasIsolatedClasses;
    }
    
    /**
     * INTERNAL:
     * Set to true during descriptor initialize if any descriptor is isolated.
     * Determines if an isolated client session is required.
     */
    public void setHasIsolatedClasses(boolean hasIsolatedClasses) {
        this.hasIsolatedClasses = hasIsolatedClasses;
    }

    /**
     * INTERNAL:
     * Return if any descriptors use ProxyIndirection.
     * Set to true during descriptor initialize if any descriptor uses ProxyIndirection
     * Determines if ProxyIndirectionPolicy.getValueFromProxy should be called.
     */
    public boolean hasProxyIndirection() {
        return this.hasProxyIndirection;
    }
      
    /**
     * INTERNAL:
     * Set to true during descriptor initialize if any descriptor uses ProxyIndirection
     * Determines if ProxyIndirectionPolicy.getValueFromProxy should be called. 
     */
    public void setHasProxyIndirection(boolean hasProxyIndirection) {
        this.hasProxyIndirection = hasProxyIndirection;
    }
	
    /**
     * PUBLIC:
     * Set the login to be used to connect to the database for this project.
     */
    public void setLogin(DatabaseLogin datasourceLogin) {
        this.datasourceLogin = datasourceLogin;
    }

    /**
     * PUBLIC:
     * Set the login to be used to connect to the database for this project.
     */
    public void setLogin(Login datasourceLogin) {
        this.datasourceLogin = datasourceLogin;
    }

    /**
     * PUBLIC:
     * Set the login to be used to connect to the database for this project.
     */
    public void setDatasourceLogin(Login datasourceLogin) {
        this.datasourceLogin = datasourceLogin;
    }

    /**
     * PUBLIC:
     * Set the name of the project.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * INTERNAL:
     */
    public String toString() {
        return Helper.getShortClassName(getClass()) + "(" + getName() + ")";
    }

    /**
     * PUBLIC:
     * Switch all descriptors to use the cache identity map.
     */
    public void useCacheIdentityMap() {
        Iterator descriptors = getDescriptors().values().iterator();
        while (descriptors.hasNext()) {
            ClassDescriptor descriptor = (ClassDescriptor)descriptors.next();
            descriptor.useCacheIdentityMap();
        }
    }

    /**
     * PUBLIC:
     * Switch all descriptors to use the cache identity map the size.
     */
    public void useCacheIdentityMap(int cacheSize) {
        Iterator descriptors = getDescriptors().values().iterator();
        while (descriptors.hasNext()) {
            ClassDescriptor descriptor = (ClassDescriptor)descriptors.next();
            descriptor.useCacheIdentityMap();
            descriptor.setIdentityMapSize(cacheSize);
        }
    }

    /**
     * PUBLIC:
     * Switch all descriptors to use the full identity map.
     */
    public void useFullIdentityMap() {
        Iterator descriptors = getDescriptors().values().iterator();
        while (descriptors.hasNext()) {
            ClassDescriptor descriptor = (ClassDescriptor)descriptors.next();
            descriptor.useFullIdentityMap();
        }
    }

    /**
     * PUBLIC:
     * Switch all descriptors to use the full identity map with initial cache size.
     */
    public void useFullIdentityMap(int initialCacheSize) {
        Iterator descriptors = getDescriptors().values().iterator();
        while (descriptors.hasNext()) {
            ClassDescriptor descriptor = (ClassDescriptor)descriptors.next();
            descriptor.useFullIdentityMap();
            descriptor.setIdentityMapSize(initialCacheSize);
        }
    }

    /**
     * PUBLIC:
     * Switch all descriptors to use no identity map.
     */
    public void useNoIdentityMap() {
        Iterator descriptors = getDescriptors().values().iterator();
        while (descriptors.hasNext()) {
            ClassDescriptor descriptor = (ClassDescriptor)descriptors.next();
            descriptor.useNoIdentityMap();
        }
    }

    /**
     * PUBLIC:
     * Switch all descriptors to use the soft cache weak identity map.
     */
    public void useSoftCacheWeakIdentityMap() {
        Iterator descriptors = getDescriptors().values().iterator();
        while (descriptors.hasNext()) {
            ClassDescriptor descriptor = (ClassDescriptor)descriptors.next();
            descriptor.useSoftCacheWeakIdentityMap();
        }
    }

    /**
     * PUBLIC:
     * Switch all descriptors to use the soft cache weak identity map with soft cache size.
     */
    public void useSoftCacheWeakIdentityMap(int cacheSize) {
        Iterator descriptors = getDescriptors().values().iterator();
        while (descriptors.hasNext()) {
            ClassDescriptor descriptor = (ClassDescriptor)descriptors.next();
            descriptor.useSoftCacheWeakIdentityMap();
            descriptor.setIdentityMapSize(cacheSize);
        }
    }

    /**
     * INTERNAL:
     * Asks each descriptor if is uses optimistic locking.
     */
    public boolean usesOptimisticLocking() {
        Iterator descriptors = getDescriptors().values().iterator();
        while (descriptors.hasNext()) {
            ClassDescriptor descriptor = (ClassDescriptor)descriptors.next();
            if (descriptor.usesOptimisticLocking()) {
                return true;
            }
        }
        return false;
    }

    /**
     * INTERNAL:
     * Asks each descriptor if is uses sequencing.
     */
    public boolean usesSequencing() {
        Iterator descriptors = getDescriptors().values().iterator();
        while (descriptors.hasNext()) {
            ClassDescriptor descriptor = (ClassDescriptor)descriptors.next();
            if (descriptor.usesSequenceNumbers()) {
                return true;
            }
        }
        return false;
    }

    /**
     * PUBLIC:
     * Switch all descriptors to use the weak identity map.
     */
    public void useWeakIdentityMap() {
        Iterator descriptors = getDescriptors().values().iterator();
        while (descriptors.hasNext()) {
            ClassDescriptor descriptor = (ClassDescriptor)descriptors.next();
            descriptor.useWeakIdentityMap();
        }
    }

    /**
     * PUBLIC:
     * Switch all descriptors to use the weak identity map.
     */
    public void useWeakIdentityMap(int initialCacheSize) {
        Iterator descriptors = getDescriptors().values().iterator();
        while (descriptors.hasNext()) {
            ClassDescriptor descriptor = (ClassDescriptor)descriptors.next();
            descriptor.useWeakIdentityMap();
            descriptor.setIdentityMapSize(initialCacheSize);
        }
    }

    /**
     * INTERNAL:
     * Default apply  login implementation.
     * Defined for generated subclasses that may not have a login.
     * BUG#2669342
     */
    public void applyLogin() {
        // Do nothing by default.
    }

    /**
     * INTERNAL:
     * Returns the alias descriptors hashtable.
     */
    public Map getAliasDescriptors() {
        return aliasDescriptors;
    }

    /**
     * PUBLIC:
     * Add an alias for the descriptor.
     */
    public void addAlias(String alias, ClassDescriptor descriptor) {
        if (aliasDescriptors == null) {
            aliasDescriptors = new Hashtable(10);
        }
        aliasDescriptors.put(alias, descriptor);
    }

    /**
     * INTERNAL:
     * Get the descriptors from the project, and associate the aliases for each
     * descriptor with the descriptor.
     */
    public void addAliasesFromProject(oracle.toplink.essentials.sessions.Project project) {
        Iterator descriptors = getDescriptors().values().iterator();
        while (descriptors.hasNext()) {
            ClassDescriptor descriptor = (ClassDescriptor)descriptors.next();
            if (descriptor.getAlias() != null) {
                addAlias(descriptor.getAlias(), descriptor);
            }
        }
    }

    /**
     * PUBLIC:
     * Return the descriptor for the alias.
     */
    public ClassDescriptor getClassDescriptorForAlias(String alias) {
        ClassDescriptor d = null;
        if (aliasDescriptors != null) {
            d = (ClassDescriptor)aliasDescriptors.get(alias);
        }
        return d;
    }

    /**
     * PUBLIC:
     * Return the descriptor for the alias.
     * 
     * @deprecated    Replaced by {@link #getClassDescriptorForAlias(String)}
     */
    public ClassDescriptor getDescriptorForAlias(String alias) {
        ClassDescriptor d = null;
        if (aliasDescriptors != null) {
            d = (ClassDescriptor)aliasDescriptors.get(alias);
        }
        return d;
    }

    /**
     * INTERNAL:
     * Set the alias descriptors hashtable.
     */
    public void setAliasDescriptors(Map aHashtable) {
        aliasDescriptors = aHashtable;
    }
}
