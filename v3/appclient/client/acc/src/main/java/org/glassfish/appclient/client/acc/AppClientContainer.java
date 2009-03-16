/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
package org.glassfish.appclient.client.acc;

import com.sun.enterprise.container.common.spi.util.InjectionException;
import com.sun.enterprise.container.common.spi.util.InjectionManager;
import com.sun.enterprise.deployment.ApplicationClientDescriptor;
import com.sun.enterprise.deployment.ServiceReferenceDescriptor;
import com.sun.enterprise.security.webservices.ClientPipeCloser;
import com.sun.enterprise.util.LocalStringManager;
import com.sun.enterprise.util.LocalStringManagerImpl;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.security.auth.callback.CallbackHandler;
import org.apache.naming.resources.DirContextURLStreamHandlerFactory;
import org.glassfish.api.invocation.InvocationManager;
import org.glassfish.appclient.client.acc.config.AuthRealm;
import org.glassfish.appclient.client.acc.config.ClientCredential;
import org.glassfish.appclient.client.acc.config.MessageSecurityConfig;
import org.glassfish.appclient.client.acc.config.Property;
import org.glassfish.appclient.client.acc.config.Security;
import org.glassfish.appclient.client.acc.config.TargetServer;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.PerLookup;

/**
 * Embeddable Glassfish app client container (ACC).
 * <p>
 * Allows Java programs to:
 * <ul>
 * <li>create a new configurator for an ACC (see {@link #newConfigurator} and {@link AppClientContainerConfigurator}),
 * <li>optionally modify the configuration by invoking various configurator methods,
 * <li>create an embedded instance of the ACC from the configuration using {@link AppClientContainerConfigurator#newContainer() },
 * <li>startClient the client using {@link #startClient(String[])}, and
 * <li>stop the container using {@link #stop()}.
 * </ul>
 * <p>
 * Each instance of the {@link TargetServer} class passed to the <code>newConfigurator</code>
 * method represents one
 * server, conveying its host and port number, which the ACC can use to
 * "bootstrap" into the server-side ORB(s).  The calling
 * program can request to use secured communication to a server by also passing
 * an instance of the {@link Security} configuration class when it creates the <code>TargetServer</code>
 * object.  Note that the caller prepares the <code>TargetServer</code>
 * array completely before passing it to one of the <code>newConfig</code>
 * factory methods.
 * The <code>Configurator</code> implementation
 * does not override or augment the list of target servers using
 * system property values, property settings in the container configuration, etc.  If such work
 * is necessary to find additional target servers the calling program should do it
 * and prepare the array of <code>TargetServer</code> objects accordingly.
 * <p>
 * The calling program also passes either a File or URI for the app client
 * archive to be run or a Class object for the main class to be run as an app client.
 * <p>
 * After the calling program has created a new <code>AppClientContainer.Configurator</code> instance
 * it can set optional
 * information to control the ACC's behavior, such as
 * <ul>
 * <li>setting the authentication realm
 * <li>setting client credentials
 * (and optionally setting an authentication realm in which the username and password
 * are valid)
 * <li>setting the callback handler class
 * <li>adding one or more {@link MessageSecurityConfig} objects
 * </ul>
 * <p>
 * Once the calling program has used the builder to configure the ACC to its liking it invokes the
 * builder's <code>newContainer()</code> method.
 * The return type is an <code>AppClientContainer</code>, and by the time
 * <code>newContainer</code> returns the <code>AppClientContainer</code>
 * has invoked the app client's main method and that method has returned to the ACC.
 * Any new thread the client creates or any GUI work it triggers on the AWT
 * dispatcher thread continues independently from the thread that called <code>newContainer</code>.
 * <p>
 * If needed, the calling program can invoke the <code>stop</code> method on
 * the <code>AppClientContainer</code> to shut down the ACC-provided services.
 * Invoking <code>stop</code> does not stop any
 * threads the client might have started.  If the calling program needs to
 * control such threads it should do so itself, outside the <code>AppClientContainer</code>
 * API.  If the calling program does not invoke <code>stop</code> the ACC will
 * clean up automatically as the JVM exits.
 * <p>
 * A simple case in which the calling program provides an app client JAR file and
 * a single TargetServer might look like this:
 * <p>
 * <code>
 *
 * import org.glassfish.appclient.client.acc.AppClientContainer;<br>
 * import org.glassfish.appclient.client.acc.config.TargetServer;<br>
 * <br>
 * AppClientContainerConfigurator accBuilder = AppClientContainer.newBuilder(<br>
 * &nbsp;&nbsp;    new TargetServer("localhost", 3700), new File("myAC.jar"));<br>
 * <br>
 * AppClientContainer acc = accBuilder.newContainer();<br>
 * // The newContainer method returns as soon as the client's main method returns,<br>
 * // even if the client has started another thread or is using the AWT event<br>
 * // dispatcher thread
 * <br>
 * // At some later point, the program can synchronize with the app client in<br>
 * // a user-specified way at which point it could invoke<br>
 * <br>
 * acc.stop();<br>
 * <br>
 * </code>
 * <p>
 * Public methods on the Configurator interfaces which set configuration information return the
 * Configurator object itself.  This allows the calling program to chain together
 * several method invocations, such as
 * <p>
 * <code>
 * AppClientContainerConfigurator accBuilder = AppClientContainer.newBuilder(...);<br>
 * accBuilder.clientCredentials(myUser, myPass).clientArgs(argsToClient);<br>
 * </code>
 * «
 *
 * @author tjquinn
 */
@Service
@Scoped(PerLookup.class)
public class AppClientContainer {

    // XXX move this
    /** Prop name for keeping temporary files */
    public static final String APPCLIENT_RETAIN_TEMP_FILES_PROPERTYNAME = "com.sun.aas.jws.retainTempFiles";

    private static final LocalStringManager localStrings = new LocalStringManagerImpl(AppClientContainer.class);

    /**
     * Creates a new ACC configurator object, preset with the specified
     * target servers.
     *
     * @param targetServers server(s) to contact during ORB bootstrapping
     * @return <code>AppClientContainer.Configurator</code> object
     */
    public static AppClientContainer.Configurator newConfigurator(
            final TargetServer[] targetServers) {
        return new AppClientContainerConfigurator(targetServers);
    }

//    /**
//     * Creates a new ACC configurator object.
//     * <p>
//     * This variant could be invoked, for example, from the main method of
//     * our main class in the facade JAR file generated during deployment.  If
//     * such a generated JAR is launched directly using a java command (and
//     * not the appclient script) then that class would have no way to find
//     * any configuration information.
//     *
//     * @return <code>AppClientContainer.Configurator</code> object
//     */
//    public static AppClientContainer.Configurator newConfigurator() {
//        return new AppClientContainerConfigurator();
//    }

    private AppClientContainerSecurityHelper secHelper;

    @Inject
    private InjectionManager injectionManager;

    @Inject
    private InvocationManager invocationManager;

    private Configurator configurator;
    private Logger logger;
    private Cleanup cleanup = null;

    private State state = State.INSTANTIATED; // HK2 will create the instance

    private Class mainClass = null;

    private ApplicationClientDescriptor descriptor = null;
    

    private ClientMainClassSetting clientMainClassSetting = null;
//    private URI clientURI = null;

    private ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

    private AppClientInfo appClientInfo = null;

    private boolean isJWS = false;

    private Launchable client = null;

    private CallbackHandler callerSuppliedCallbackHandler = null;


    /*
     * ********************* ABOUT INITIALIZATION ********************
     *
     * Note that, internally, the AppClientContainerConfigurator's newContainer
     * methods use HK2 to instantiate the AppClientContainer object (so we can
     * inject references to various other services).
     *
     * The newContainer method then invokes one of the ACC's
     * <code>prepare</code> methods to initialize the ACC fully.  All that is
     * left at that point is for the client's main method to be invoked.
     *
     */

    public void startClient(String[] args) throws Exception {
        launch(args);
    }

    void setCallbackHandler(final CallbackHandler callerSuppliedCallbackHandler) {
        this.callerSuppliedCallbackHandler = callerSuppliedCallbackHandler;
    }

    void setConfigurator(final Configurator configurator) {
        this.configurator = configurator;
    }

    public void prepare() throws NamingException, IOException, InstantiationException, IllegalAccessException, InjectionException, ClassNotFoundException {
        completePreparation();
    }

    void setClient(final Launchable client) {
        this.client = client;
    }
    
    protected Class loadClass(final String className) throws ClassNotFoundException {
        return Class.forName(className, true, classLoader);
    }

    protected ClassLoader getClassLoader() {
        return classLoader;
    }
    
    /**
     * Gets the ACC ready so the main class can run.
     * This can be followed, immediately or after some time, by either an
     * invocation of {@link #launch(java.lang.String[])  or
     * by the JVM invoking the client's main method (as would happen during
     * a <code>java -jar theClient.jar</code> launch.
     *
     * @throws java.lang.Exception
     */
    private void completePreparation() throws NamingException, IOException, InstantiationException, IllegalAccessException, InjectionException, ClassNotFoundException {
        if (state != State.INSTANTIATED) {
            throw new IllegalStateException();
        }

        /*
         * Create the security helper and then register it with the habitat so
         * the app client container can find it as the acc is instantiated and
         * injected.  Do this before initializing
         * naming so the ORB intialization side-effects of getting the
         * InitialContext use the appropriate ORB settings.
         */
        secHelper = new AppClientContainerSecurityHelper(
                configurator.getTargetServers(),
                configurator.getContainerProperties(),
                configurator.getClientCredential(),
                callerSuppliedCallbackHandler,
                null /* use current context class loader */,
                client.getDescriptor(Thread.currentThread().getContextClassLoader()));

        /*
         * Initialize naming now to establish an InitialContext with the
         * side-effect of initializing the ORB.
         * correct Properties.  Then, when the habitat instantiates the
         * container which injects some services that also use InitialContext
         * the context created here will be reused by those other places.
         * As an example, the container injects the InjectionManager which
         * invokes new InitialContext().
         */

        initializeNaming(secHelper);



       
        /*
         * Arrange for cleanup now instead of during launch() because in some use cases
         * the JVM will invoke the client's main method itself and launch will
         * be skipped.
         */
        cleanup = Cleanup.arrangeForShutdownCleanup(logger);

        prepareURLStreamHandling();

        state = State.PREPARED;
    }

    void launch(String[] args) throws
            NoSuchMethodException,
            ClassNotFoundException,
            IllegalAccessException,
            IllegalArgumentException,
            InvocationTargetException {

        if (state != State.PREPARED) {
            throw new IllegalStateException();
        }
        Method mainMethod = getMainMethod();
	    // build args to the main and call it
	    Object params [] = new Object [1];
	    params[0] = args;
	    mainMethod.invoke(null, params);
        state = State.STARTED;
    }

    private void initializeNaming(final AppClientContainerSecurityHelper secHelper) throws NamingException {
        new InitialContext(secHelper.getIIOPProperties());
    }

   private Method getMainMethod() throws NoSuchMethodException, ClassNotFoundException {
	    // determine the main method using reflection
	    // verify that it is public static void and takes
	    // String[] as the only argument
	    Method result = null;

        result = clientMainClassSetting.getClientMainClass(classLoader).getMethod("main",
            new Class[] { String[].class } );

	    // check modifiers: public static
	    int modifiers = result.getModifiers ();
	    if (!Modifier.isPublic (modifiers) ||
		!Modifier.isStatic (modifiers))  {
		    String err = localStrings.getLocalString(
                    getClass(),
                    "appclient.notPublicOrNotStatic",
                    "The main method is either not public or not static");
	    	    throw new NoSuchMethodException(err);
	    }

	    // check return type and exceptions
	    if (!result.getReturnType().equals (Void.TYPE)) {
            String err = localStrings.getLocalString(
                getClass(),
                "appclient.notVoid",
                "The main method's return type is not void");
            throw new NoSuchMethodException(err);
	    }
        return result;
    }

    
    /**
     * Stops the app client container.
     * <p>
     * Note that the calling program should not stop the ACC if there might be
     * other threads running, such as the Swing event dispatcher thread.  Stopping
     * the ACC can shut down various services that those continuing threads might
     * try to use.
     * <p>
     * Also note that stopping the ACC will have no effect on any thread that
     * the app client itself might have created.  If the calling program needs
     * to control such threads it and the client code running in the threads
     * should agree on how they will communicate with each other.  The ACC cannot
     * help with this.
     */
    public void stop() {
        if ( state != State.STARTED) {
            throw new IllegalStateException();
        }
        cleanup.start();
        state = State.STOPPED;
    }

    /**
     * Records how the main class has been set - by name or by class - and
     * encapsulates the retrieval of the main class.
     */
    enum ClientMainClassSetting {
        BY_NAME,
        BY_CLASS;

        static protected String clientMainClassName;
        static protected Class clientMainClass;

        static ClientMainClassSetting set(final String name) {
            clientMainClassName = name;
            clientMainClass = null;
            return BY_NAME;
        }

        static ClientMainClassSetting set(final Class cl) {
            clientMainClass = cl;
            clientMainClassName = null;
            return BY_CLASS;
        }

        static Class getClientMainClass(final ClassLoader loader) throws ClassNotFoundException {
            if (clientMainClass == null) {
                if (clientMainClassName == null) {
                    throw new IllegalStateException("neither client main class nor its class name has been set");
                }
                clientMainClass = Class.forName(clientMainClassName, true, loader);
            }
            return clientMainClass;
        }
    }

    /**
     * Records the current state of the ACC.
     */
    enum State {
        /**
         * HK2 has created the ACC instance
         */
        INSTANTIATED,

        /**
         * ACC is ready for the client to run
         */
        PREPARED,

        /**
         * the ACC has started the client.
         * <p>
         * Note that if the user launches the client JAR directly (using
         * java -jar theClient.jar) the ACC will not be aware of this and
         * so the state remains PREPARED.
         */
        STARTED,

        /**
         * the ACC has stopped in response to a request from the calling
         * program
         */
        STOPPED;
    }

    /**
     * Sets the name of the main class to be executed.
     * <p>
     * Normally the ACC reads the app client JAR's manifest to get the
     * Main-Class attribute.  The calling program can override that value
     * by invoking this method.  The main class name is also useful if
     * the calling program provides an EAR that contains multiple app clients
     * as submodules within it; the ACC needs the calling program to specify
     * which of the possibly several app client modules is the one to execute.
     *
     * @param mainClassName
     * @return
     */
    public void setClientMainClassName(final String clientMainClassName) throws ClassNotFoundException {
        clientMainClassSetting = ClientMainClassSetting.set(clientMainClassName);
    }

    void setClientMainClass(final Class clientMainClass) {
       clientMainClassSetting = ClientMainClassSetting.set(clientMainClass);
    }

    /**
     * Assigns the URL stream handler factory.
     * <p>
     * Needed for web services support.
     */
    private static void prepareURLStreamHandling() {
        // Set the HTTPS URL stream handler.
        java.security.AccessController.doPrivileged(new
                                       java.security.PrivilegedAction() {
                public Object run() {
                    URL.setURLStreamHandlerFactory(new
                                       DirContextURLStreamHandlerFactory());
                    return null;
                }
            });
    }

    void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    /**
     * Prescribes the exposed behavior of ACC configuration that can be
     * set up further, and can be used to newContainer an ACC.
     */
    public interface Configurator {

        public AppClientContainer newContainer(URI archiveURI) throws Exception;

        public AppClientContainer newContainer(Class mainClass) throws Exception;

        public TargetServer[] getTargetServers();

        /**
         * Adds an optional {@link MessageSecurityConfig} setting.
         *
         * @param msConfig the new MessageSecurityConfig
         * @return the <code>Configurator</code> instance
         */
        public Configurator addMessageSecurityConfig(final MessageSecurityConfig msConfig);

        public List<MessageSecurityConfig> getMessageSecurityConfig();

       /**
         * Sets the optional authentication realm for the ACC.
         * <p>
         * Each specific realm will determine which properties should be set in the
         * Properties argument.
         *
         * @param className name of the class which implements the realm
         * @return the <code>Configurator</code> instance
         */
        public Configurator authRealm(final String className);

        public AuthRealm getAuthRealm();

//        /**
//         * Sets the callback handler the ACC will use when authentication is
//         * required.  If the program does not invoke this method the ACC will use
//         * the callback handler specified in the client's deployment descriptor,
//         * if any.  Failing that, the ACC will use its own default callback handler
//         * to prompt for and collect information required during authentication.
//         * <p>
//         * A callback handler class set using this method overrides the
//         * callback handler setting from the client's descriptor, if any, or from
//         * any previous invocations of <code>callbackHandler</code>.
//         *
//         * @param callbackHandlerClassName fully-qualified name of the developer's
//         * callback handler class
//          * @return the <code>Configurator</code> instance
//        */
//        public Configurator callbackHandler(final Class<? extends CallbackHandler> callbackHandlerClass);
//
//        public Class<? extends CallbackHandler> getCallbackHandler();

        /**
         * Sets the optional client credentials to be used during authentication to the
         * back-end.
         * <p>
         * If the client does not invoke <code>clientCredentials</code> then the
         * ACC will use a {@link CallbackHandler} when it discovers that authentication
         * is required.  See {@link #callerSuppliedCallbackHandler}.
         *
         * @param username username valid in the default realm on the server
         * @param password password valid in the default realm on the server for the username
         * @return the <code>Configurator</code> instance
        */
        public Configurator clientCredentials(final String user, final char[] password);

        public ClientCredential getClientCredential();

        /**
         * Sets the optional client credentials and server-side realm to be used during
         * authentication to the back-end.
         * <p>
         * If the client does not invoke <code>clientCredentials</code> then the
         * ACC will use a {@link CallbackHandler} when it discovers that authentication
         * is required.  See {@link #callerSuppliedCallbackHandler}.
         *
         * @param username username valid in the specified realm on the server
         * @param password password valid in the specified realm on the server for the username
         * @param realmName name of the realm on the server within which the credentials are valid
         * @return the <code>Configurator</code> instance
         */
        public Configurator clientCredentials(final String user, final char[] password, final String realm);

        /**
         * Sets the container-level Properties.
         *
         * @param containerProperties
         * @return
         */
        public Configurator containerProperties(final Properties containerProperties);

        /**
         * Sets the container-level properties.
         * <p>
         * Typically used when setting the properties from the parsed XML config
         * file.
         *
         * @param containerProperties Property objects to use in setting the properties
         * @return
         */
        public Configurator containerProperties(final List<Property> containerProperties);

        /**
         * Returns the container-level Properties.
         * @return container-level properties
         */
        public Properties getContainerProperties();

        /**
         * Sets the logger which the ACC should use as it runs.
         *
         * @param logger
         * @return
         */
        public Configurator logger(final Logger logger);

        public Logger getLogger();

        /**
         * Sets whether the ACC should send the password to the server during
         * authentication.
         *
         * @param sendPassword
         * @return
         */
        public Configurator sendPassword(final boolean sendPassword);

        public boolean getSendPassword();

    }


    /**
     * Encapsulates all clean-up activity.
     * <p>
     * The calling program can invoke clean-up by invoking the <code>stop</code>
     * method or by letting the JVM exit, in which case clean-up will occur as
     * part of VM shutdown.
     */
    private static class Cleanup implements Runnable {
        private AppClientInfo appClientInfo = null;
        private boolean cleanedUp = false;
        private InjectionManager injectionMgr = null;
        private ApplicationClientDescriptor appClient = null;
        private Class cls = null;
        private final Logger logger;
        private Thread cleanupThread = null;

        static Cleanup arrangeForShutdownCleanup(final Logger logger) {
            final Cleanup cu = new Cleanup(logger);
            cu.enable();
            return cu;
        }

        private Cleanup(final Logger logger) {
            this.logger = logger;
        }

        void setAppClientInfo(AppClientInfo info) {
            appClientInfo = info;
        }

        void setInjectionManager(InjectionManager injMgr, Class cls, ApplicationClientDescriptor appDesc) {
            injectionMgr = injMgr;
            this.cls = cls;
            appClient = appDesc;
        }

        void enable() {
            Runtime.getRuntime().addShutdownHook(cleanupThread = new Thread(this, "Cleanup"));
        }

        void disable() {
            Runtime.getRuntime().removeShutdownHook(cleanupThread);
        }

        /**
         * Requests cleanup without relying on the VM's shutdown handling.
         */
        void start() {
            disable();
            run();
        }

        /**
         * Performs clean-up of the ACC.
         * <p>
         * This method should be invoked directly only by the VM's shutdown
         * handling (or by the CleanUp newContainer method).  To trigger clean-up
         * without relying on the VM's shutdown handling invoke Cleanup.newContainer()
         * not Cleanup.run().
         */
        public void run() {
            logger.info("Clean-up starting");
            /*
             * Do not invoke disable from here.  The run method might execute
             * while the VM shutdown is in progress, and attempting to remove
             * the shutdown hook at that time would trigger an exception.
             */
            cleanUp();
        }

        void cleanUp() {
            if( !cleanedUp ) {
                try {

                    if ( appClientInfo != null ) {
                        appClientInfo.close();
                    }
                    if ( injectionMgr != null) {
                        // inject the pre-destroy methods before shutting down
                        injectionMgr.invokeClassPreDestroy(cls, appClient);
                        injectionMgr = null;
                    }
                    if(appClient != null && appClient.getServiceReferenceDescriptors() != null) {
                        // Cleanup client pipe line, if there were service references
                        for (Object desc: appClient.getServiceReferenceDescriptors()) {
                             ClientPipeCloser.getInstance()
                                .cleanupClientPipe((ServiceReferenceDescriptor)desc);
                        }
                    }
                }
                catch(Throwable t) {
                }
                finally {
                    cleanedUp = true;
                }
            } // End if -- cleanup required
        }
    }
}
