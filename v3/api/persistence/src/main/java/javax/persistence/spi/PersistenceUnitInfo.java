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


package javax.persistence.spi;

import javax.sql.DataSource;
import java.net.URL;
import java.util.List;
import java.util.Properties;

/**
 * Interface implemented by the container and used by the persistence provider
 * when creating an {@link javax.persistence.EntityManagerFactory}.
 *
 * @since Java Persistence 1.0
 */
public interface PersistenceUnitInfo {
    /**
     * Returns the name of the persistence unit. Corresponds to the name attribute
     *         in the persistence.xml file.
     * @return The name of the persistence unit. Corresponds to the name attribute
     *         in the persistence.xml file.
     */
    public String getPersistenceUnitName();

    /**
     * Returns the fully qualified name of the persistence provider
     *         implementation class. Corresponds to the &lt;provider&gt; element in
     *         the persistence.xml file.
     * @return The fully qualified name of the persistence provider
     *         implementation class. Corresponds to the &lt;provider&gt; element in
     *         the persistence.xml file.
     */
    public String getPersistenceProviderClassName();

    /**
     * Returns the transaction type of the entity managers created by the
     *         EntityManagerFactory. The transaction type corresponds to the
     *         transaction-type attribute in the persistence.xml file.
     * @return The transaction type of the entity managers created by the
     *         EntityManagerFactory. The transaction type corresponds to the
     *         transaction-type attribute in the persistence.xml file.
     */
    public PersistenceUnitTransactionType getTransactionType();

    /**
     * Returns the JTA-enabled data source to be used by the persistence
     *         provider. The data source corresponds to the &lt;jta-data-source&gt;
     *         element in the persistence.xml file or is provided at deployment
     *         or by the container.
     * @return the JTA-enabled data source to be used by the persistence
     *         provider. The data source corresponds to the &lt;jta-data-source&gt;
     *         element in the persistence.xml file or is provided at deployment
     *         or by the container.
     */
    public DataSource getJtaDataSource();

    /**
     * Returns the non-JTA-enabled data source to be used by the persistence
     *         provider for accessing data outside a JTA transaction. The data
     *         source corresponds to the named &lt;non-jta-data-source&gt; element in
     *         the persistence.xml file or provided at deployment or by the
     *         container.
     * @return The non-JTA-enabled data source to be used by the persistence
     *         provider for accessing data outside a JTA transaction. The data
     *         source corresponds to the named &lt;non-jta-data-source&gt; element in
     *         the persistence.xml file or provided at deployment or by the
     *         container.
     */
    public DataSource getNonJtaDataSource();

    /**
     * Returns the list of mapping file names that the persistence provider must
     *         load to determine the mappings for the entity classes. The
     *         mapping files must be in the standard XML mapping format, be
     *         uniquely named and be resource-loadable from the application
     *         classpath. This list will not include the orm.xml file if one was
     *         specified. Each mapping file name corresponds to a &lt;mapping-file&gt;
     *         element in the persistence.xml file.
     * @return The list of mapping file names that the persistence provider must
     *         load to determine the mappings for the entity classes. The
     *         mapping files must be in the standard XML mapping format, be
     *         uniquely named and be resource-loadable from the application
     *         classpath. This list will not include the orm.xml file if one was
     *         specified. Each mapping file name corresponds to a &lt;mapping-file&gt;
     *         element in the persistence.xml file.
     */
    public List<String> getMappingFileNames();

    /**
     * Returns a list of URLs for the jar files or exploded jar
     * file directories that the persistence provider must examine
     * for managed classes of the persistence unit. Each URL
     * corresponds to a named <jar-file> element in the
     * persistence.xml file. A URL will either be a file:
     * URL referring to a jar file or referring to a directory
     * that contains an exploded jar file, or some other URL from
     * which an InputStream in jar format can be obtained.
     *
     * @return a list of URL objects referring to jar files or
     * directories.
     */
    public List<URL> getJarFileUrls();

    /**
     * Returns the URL for the jar file or directory that is the
     * root of the persistence unit. (If the persistence unit is
     * rooted in the WEB-INF/classes directory, this will be the
     * URL of that directory.)
     * The URL will either be a file: URL referring to a jar file
     * or referring to a directory that contains an exploded jar
     * file, or some other URL from which an InputStream in jar
     * format can be obtained.
     *
     * @return a URL referring to a jar file or directory. 
     */
    public URL getPersistenceUnitRootUrl();

    /**
     * Returns the list of the names of the classes that the persistence
     *         provider must add it to its set of managed classes. Each name
     *         corresponds to a named &lt;class&gt; element in the persistence.xml
     *         file.
     * @return The list of the names of the classes that the persistence
     *         provider must add it to its set of managed classes. Each name
     *         corresponds to a named &lt;class&gt; element in the persistence.xml
     *         file.
     */
    public List<String> getManagedClassNames();

    /**
     * Returns whether classes in the root of the persistence unit that have not
     *         been explicitly listed are to be included in the set of managed
     *         classes. This value corresponds to the &lt;exclude-unlisted-classes&gt;
     *         element in the persistence.xml file.
     * @return Whether classes in the root of the persistence unit that have not
     *         been explicitly listed are to be included in the set of managed
     *         classes. This value corresponds to the &lt;exclude-unlisted-classes&gt;
     *         element in the persistence.xml file.
     */
    public boolean excludeUnlistedClasses();

    /**
     * Returns properties object. Each property corresponds to a &lt;property&gt;
     *         element in the persistence.xml file
     * @return Properties object. Each property corresponds to a &lt;property&gt;
     *         element in the persistence.xml file
     */
    public Properties getProperties();

    /**
     * Returns ClassLoader that the provider may use to load any classes,
     *         resources, or open URLs.
     * @return ClassLoader that the provider may use to load any classes,
     *         resources, or open URLs.
     */
    public ClassLoader getClassLoader();

    /**
     * Add a transformer supplied by the provider that will be called for every
     * new class definition or class redefinition that gets loaded by
     * the loader returned by the {@link PersistenceUnitInfo#getClassLoader} method. The
     * transformer has no effect on the result returned by the
     * {@link PersistenceUnitInfo#getNewTempClassLoader} method. Classes are
     * only transformed once within the same classloading scope, regardless of
     * how many persistence units they may be a part of.
     *
     * @param transformer A provider-supplied transformer that the Container
     *                    invokes at class-(re)definition time
     */
    public void addTransformer(ClassTransformer transformer);

    /**
     * Return a new instance of a ClassLoader that the provider
     * may use to temporarily load any classes, resources, or
     * open URLs. The scope and classpath of this loader is
     * exactly the same as that of the loader returned by
     * {@link PersistenceUnitInfo#getClassLoader}. None of the classes loaded
     * by this class loader will be visible to application
     * components. The provider may only use this ClassLoader
     * within the scope of the {@link PersistenceProvider#createContainerEntityManagerFactory}
     * call.
     *
     * @return Temporary ClassLoader with same visibility as current
     * loader
     */
    public ClassLoader getNewTempClassLoader();
}
