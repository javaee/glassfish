/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * Header Notice in each file and include the License file 
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.  
 * If applicable, add the following below the CDDL Header, 
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */
package javax.ejb.embeddable;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.ServiceLoader;

import javax.naming.Context;
import javax.ejb.EJBException;
import javax.ejb.spi.EJBContainerProvider;

/** 
  * Used to execute an EJB application in an embeddable container.  
  */
public abstract class EJBContainer {

    /**
     * Standard property name for specifying the embeddable container implementation bootstrap
     * class.  Property value is a fully-qualified class name.
     */
    public static final String PROVIDER = "javax.ejb.embeddable.provider";

    /**
     * Standard property name for specifying the set of modules to be initialized.  Property
     * value is one of the following : 
     *   -- a single module name String from the JVM classpath
     *   -- a String[] array of module names from the JVM classpath
     *   -- a java.io.File representing an ejb-jar or exploded ejb-jar directory
     *   -- a java.io.File array, each element of which represents an ejb-jar 
     *        or exploded ejb-jar directory
     */
    public static final String MODULES = "javax.ejb.embeddable.modules";

    /**
     * Standard property name for specifying the application name of the EJB modules 
     * executing within the embeddable container. If specified, the property value 
     * applies to the <app-name> portion of the portable global JNDI name syntax. If 
     * this property is not specified, the <app-name> portion of the portable global 
     * JNDI name syntax does not apply.
     */
    public static final String APP_NAME = "javax.ejb.embeddable.appName";

    /**
     * Create and initialize an embeddable EJB container.  JVM classpath is 
     * searched for all ejb-jars or exploded ejb-jars in directory format.
     *
     * @return EJBContainer instance
     *
     * @exception javax.ejb.EJBException  Thrown if the container or application
     * could not be successfully initialized.
     */
    public static EJBContainer createEJBContainer() { 
        return createEJBContainer(null);
    }

    /**
     * Create and initialize an embeddable EJB container with a
     * set of configuration properties.
     *
     * @param properties  Spec-defined and/or vendor-specific properties.
     * The spec reserves the prefix "javax.ejb." for spec-defined properties.
     *
     * @return EJBContainer instance
     *
     * @exception javax.ejb.EJBException  Thrown if the container or application
     * could not be successfully initialized.
     */
    public static EJBContainer createEJBContainer(Map<?,?> properties) {
        EJBContainer container = null;

        Map<String, String> errors = new HashMap<String, String>();
        Set<String> returnedNull = new HashSet<String>();

        providers.reload();
        for (EJBContainerProvider provider : providers) {
            try {
                container = provider.createEJBContainer(properties);
                if (container != null) {
                    break;
                } else {
                    returnedNull.add(provider.getClass().getName());
                }
            } catch (EJBException e) {
                // The provider is eligible but encountered problems
                throw e;
            } catch (Throwable t) {
                // ignore but remember the message in case all fail: 
                // according to Spec the provider must return null from
                // createEJBContainer(), if not the right provider.
                // But non-compliant provider may throw exception
                errors.put(provider.getClass().getName(), createErrorMessage(t));
            }
        }

        if (container == null) {
            reportError(properties, errors, returnedNull);
        }
        return container;
    }

    /**
     * Retrieve a naming context for looking up references to session beans
     * executing in the embeddable container.
     *
     * @return naming context
     */
    abstract public Context getContext();

    /**
     * Shutdown an embeddable EJBContainer instance.  Embeddable applications
     * should always call close() in order to free up the resources
     * associated with the embeddable container.   
     */
    abstract public void close(); 

    //Private variables
    private static final String newLine = "\r\n";
    private static final ServiceLoader<EJBContainerProvider> providers = 
            ServiceLoader.load(EJBContainerProvider.class);

    /**
     * Create a meaningful EJBException in case no EJBContainer provider had
     * been found.
     *
     * @param properties the properties passed as an argument to createEJBContainer() method
     * @param errors the Map of errors encountered during createEJBContainer() call
     * @param returnedNull the Set of providers that returned null on createEJBContainer() call
     * @throws EJBException
     */
    private static void reportError(Map<?,?> properties, Map<String, String> errors, 
            Set<String> returnedNull) throws EJBException {
        StringBuffer message = new StringBuffer(
                "No EJBContainer provider available");

        if (properties != null) {
            Object specifiedProvider = properties.get(PROVIDER);
            if (specifiedProvider != null) {
                message.append(" for requested provider: " + specifiedProvider);
            }
        }

        if (errors.isEmpty() && returnedNull.isEmpty()) {
            message.append(": no provider names had been found.");
        } else {
            message.append("\n");
        }

        for (Map.Entry me: errors.entrySet()) {
            message.append("Provider named ");
            message.append(me.getKey());
            message.append(" threw unexpected exception at create EJBContainer: \n");
            message.append(me.getValue()).append("\n");
        }
        if (!returnedNull.isEmpty()) {
            message.append("The following providers:\n");
            for (String n: returnedNull) {
                message.append(n).append("\n");
            }
            message.append("Returned null from createEJBContainer call.\n");
        }
        throw new EJBException(message.toString());
    }

    private static String createErrorMessage(Throwable t) {
        StringWriter errorMessage = new StringWriter();
        errorMessage.append(t.getClass().getName()).append(newLine);
        t.printStackTrace(new PrintWriter(errorMessage));
        errorMessage.append(newLine);
        return errorMessage.toString();
    }

}

