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
package javax.persistence;

// J2SE imports
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Set;
import java.util.Map;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Enumeration;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// persistence imports
import javax.persistence.spi.PersistenceProvider;

/**
 * Bootstrap class that is used to obtain an {@link EntityManagerFactory}.
 *
 * @since Java Persistence 1.0
 */
public class Persistence {

    public static final String PERSISTENCE_PROVIDER = "javax.persistence.spi.PeristenceProvider";

    //Need to keep this variable for compatibility with signature tests!!!
    protected static final Set<PersistenceProvider> providers = new HashSet<PersistenceProvider>();

    //Private variables
    private static final String SERVICE_NAME = "META-INF/services/" 
            + PersistenceProvider.class.getName();
    private static final String PERSISTENCE_XML_NAME = "META-INF/persistence.xml";
  
    /**
     * Create and return an EntityManagerFactory for the 
     * named persistence unit.
     * 
     * @param persistenceUnitName The name of the persistence unit
     * @return The factory that creates EntityManagers configured 
     * according to the specified persistence unit
     */
    public static EntityManagerFactory createEntityManagerFactory(String persistenceUnitName) {
        return createEntityManagerFactory(persistenceUnitName, null);
    }

    /**
     * Create and return an EntityManagerFactory for the 
     * named persistence unit using the given properties.
     * 
     * @param persistenceUnitName The name of the persistence unit
     * @param properties Additional properties to use when creating the 
     * factory. The values of these properties override any values
     * that may have been configured elsewhere.
     * @return The factory that creates EntityManagers configured 
     * according to the specified persistence unit.
     */
    public static EntityManagerFactory createEntityManagerFactory(
            String persistenceUnitName, Map properties) {
        EntityManagerFactory emf = null;
        Set<PersistenceProvider> providersFound = null;

        try{
            providersFound = findAllProviders();
        } catch (IOException exc){};

        Map<String, String> errors = new HashMap<String, String>();
        Set<String> returnedNull = new HashSet<String>();
        for (PersistenceProvider provider : providersFound) {
            try {
                emf = provider.createEntityManagerFactory(persistenceUnitName, properties);
                if (emf != null) {
                    break;
                } else {
                    returnedNull.add(provider.getClass().getName());
                }
            } catch (Throwable t) {
                // ignore : according to Spec the provider must return null from
                // createEntityManagerFactory(), if not the right provider.
                // But non-compliant provider may throw exception
                errors.put(provider.getClass().getName(), createErrorMessage(t));
            }
        }

        if (emf == null) {
            StringBuffer message = new StringBuffer(
                    "No Persistence provider for EntityManager named " +
                     persistenceUnitName + ": ");
            if (!exists(PERSISTENCE_XML_NAME)) {
                message.append (" No META-INF/persistence.xml was found in classpath.\n");
            } else {
                Map<String, String> reasons = getReasons();
                for (Map.Entry me: reasons.entrySet()) {
                    message.append("Provider named ");
                    message.append(me.getKey());
                    message.append(" threw exception at initialization: ");
                    message.append(me.getValue() + "\n");
                }

                for (Map.Entry me: errors.entrySet()) {
                    message.append("Provider named ");
                    message.append(me.getKey());
                    message.append(" threw unexpected exception at create EntityManagerFactory: \n");
                    message.append(me.getValue() + "\n");
                }

                if (!returnedNull.isEmpty()) {
                    message.append(" The following providers:\n");
                    for (String n: returnedNull) {
                        message.append(n + "\n");
                    }
                    message.append("Returned null to createEntityManagerFactory.\n");
                }
            }
            throw new PersistenceException(message.toString());
        }
        return emf;
    }
  
    // Helper methods

    private static Set<PersistenceProvider> findAllProviders() throws IOException {
        HashSet<PersistenceProvider> providersFound = new HashSet<PersistenceProvider>();
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        Enumeration<URL> resources = loader.getResources(SERVICE_NAME);

        if (!resources.hasMoreElements()) {
            throw new PersistenceException("No resource files named " + SERVICE_NAME 
            + " were found. Please make sure that the persistence provider jar file is in your classpath.");
        }
        Set<String> names = new HashSet<String>();
        while (resources.hasMoreElements()) {
            URL url = resources.nextElement();
            InputStream is = url.openStream();
            try {
                names.addAll(providerNamesFromReader(new BufferedReader(new InputStreamReader(is))));
            } finally {
                is.close();
            }
        }

        if (names.isEmpty()) {
            throw new PersistenceException("No provider names were found in " + SERVICE_NAME);
        }
        for (String s : names) {
            try{
                providersFound.add((PersistenceProvider)loader.loadClass(s).newInstance());
            } catch (ClassNotFoundException exc){
            } catch (InstantiationException exc){
            } catch (IllegalAccessException exc){
            }
        }
        return providersFound;
    }

    private static final Pattern nonCommentPattern = Pattern.compile("^([^#]+)");

    private static Set<String> providerNamesFromReader(BufferedReader reader) 
            throws IOException {
        Set<String> names = new HashSet<String>();
        String line;
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            Matcher m = nonCommentPattern.matcher(line);
            if (m.find()) {
                names.add(m.group().trim());
            }
        }
        return names;
    }

    private static boolean exists(String fileName) {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        Enumeration<URL> resources;
        try {
            resources = loader.getResources(fileName);
        } catch (IOException ex) {
            resources = null;
        }
        return resources==null? false : resources.hasMoreElements();
    }

    private static Map<String, String> getReasons() {
        Map<String, String> reasons = new HashMap<String, String>();
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        Set<String> names = new HashSet<String>();

        try {
            Enumeration<URL> resources = loader.getResources(SERVICE_NAME);
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                InputStream is = url.openStream();
                try {
                    names.addAll(providerNamesFromReader(new BufferedReader(new InputStreamReader(is))));
                } finally {
                    is.close();
                }
            }
        } catch (IOException exc){};

        for (String s : names) {
            try{
                loader.loadClass(s).newInstance();
            } catch (ClassNotFoundException exc){
                reasons.put(s, exc.getClass().getName() + " " + exc.getMessage());
            } catch (InstantiationException exc){
                reasons.put(s, createErrorMessage(exc));
            } catch (IllegalAccessException exc){
                reasons.put(s, createErrorMessage(exc));
            } catch (RuntimeException exc){
                reasons.put(s, createErrorMessage(exc));
            }
        }
        return reasons;
    }

    private static String createErrorMessage(Throwable t) {
        StringWriter errorMessage = new StringWriter();
        errorMessage.append(t.getClass().getName()).append("\r\n");
        t.printStackTrace(new PrintWriter(errorMessage));
        errorMessage.append("\r\n");
        return errorMessage.toString();
    }
}
