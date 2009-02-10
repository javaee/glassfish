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

import com.sun.enterprise.container.common.spi.util.InjectionManager;
import com.sun.enterprise.deployment.ApplicationClientDescriptor;
import com.sun.enterprise.deployment.ServiceReferenceDescriptor;
import com.sun.enterprise.security.UsernamePasswordStore;
import com.sun.enterprise.security.webservices.ClientPipeCloser;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.security.auth.callback.CallbackHandler;
import org.glassfish.appclient.client.acc.config.AuthRealm;
import org.glassfish.appclient.client.acc.config.ClientContainer;
import org.glassfish.appclient.client.acc.config.ClientCredential;
import org.glassfish.appclient.client.acc.config.MessageSecurityConfig;
import org.glassfish.appclient.client.acc.config.Security;
import org.glassfish.appclient.client.acc.config.TargetServer;

/**
 * Embeddable Glassfish app client container.
 * <p>
 * Calling programs can use any of the static factory <code>newContainer</code> methods to create a new
 * <code>AppClientContainer</code> instance.  Depending on how much information the calling
 * program wants or needs to provide, a single invocation of a factory method
 * might be all that is required.  Or, for additional security set-up, the
 * calling program might need to construct other objects using inner classes defined
 * in this class or invoke additional methods on the new container object
 * before starting the container.
 * <p>
 * For example, each instance of the {@link TargetServer} class represents one
 * server, conveying its host and port number, which the client can use to
 * "bootstrap" into a cluster.  The calling
 * program can request to use secured communication to that server by also passing
 * an instance of the {@link Security} class when it creates the <code>TargetServer</code>
 * object.  Note that the caller is ALWAYS expected to prepare the <code>TargetServer</code>
 * array completely and in the order in which the hosts should be used.  This class
 * does not attempt to override or augment the list which the caller passes using
 * system property values, property settings in the configuration, etc.
 * <p>
 * After the caller has created a new <code>AppClientContainer</code> instance
 * but before it has <code>start</code>ed that instance, it can add optional
 * information to control the ACC's behavior, such as
 * <ul>
 * <li>specifying the name of the main client class to be loaded and run (the
 * default is the class specified by the Main-Class attribute of the client's
 * manifest if the client file is a client JAR or directory, and the .class file
 * itself if the calling program passed a .class file as the client file)
 * <li>setting the authentication realm
 * <li>setting client credentials
 * (and optionally setting an authentication realm in which the username and password
 * are valid)
 * <li>setting the callback handler class
 * <li>adding one or more {@link MessageSecurityConfig} objects
 * </ul>
 *
 * @author tjquinn
 */
public class AppClientContainer implements Runnable {

    /** caller-optional logger  - initialized to logger name from the class; caller can override with setLogger */
    private Logger logger = Logger.getLogger(getClass().getName());

    /** Prop name for keeping temporary files */
    public static final String APPCLIENT_RETAIN_TEMP_FILES_PROPERTYNAME = "com.sun.aas.jws.retainTempFiles";


//    /** caller-specified client File */
//    private File clientFile = null;

    /** caller-specified client URI or the URI of the caller-specified File */
    private URI clientURI = null;

    /** caller-specified main class */
    private Class mainClass = null;

    /** caller-specified main class name */
    private String mainClassName = null;

    /** caller-specified */
    private boolean sendPassword = true;

    /** optional caller-specified properties governing the ACC's behavior.
     * Correspond to the property elements available in the client-container
     * element from sun-application-client-containerxxx.dtd.
     */
    private Properties containerProperties = null;

    /** caller-specified target servers */
    private final TargetServer[] targetServers;


    /** caller-specified callback handler name */
    private String callbackHandlerClassName = null;

    /** caller-specified authentication realm */
    private AuthRealm authRealm = null;

    /** whether to execute the app client (caller-specified) */
    private boolean runClient = true;

    /** arguments to pass to the user's client */
    private String[] clientArgs = null;

    /** shared ref to Cleanup object */
    private final Cleanup cleanup = new Cleanup(this);
    private Thread cleanupThread = null;

    private enum State {
        INITIALIZED, RUNNING, STOPPED;

        private State checkConfigure() {
            if (this != INITIALIZED) {
                throw new IllegalStateException("must be INITED; is " + this.toString());
            }
            return this;
        }

        private State checkRun() {
            if (this != INITIALIZED) {
                throw new IllegalStateException("must be INITED; is " + this.toString());
            }
            return RUNNING;

        }

        private State checkStop() {
            if (this != RUNNING) {
                throw new IllegalStateException("must be RUNNING; is " + this.toString());
            }
            return STOPPED;
        }
    }

    private State state = State.INITIALIZED;

    /**
     * The caller can pre-set the client credentials using the
     * <code>setClientCredentials</code> method.  The ACC will use the
     * username and realm values in intializing a callback handler if one is
     * needed.
     */
    private String username = System.getProperty("user.name");
    private char[] password = null;
    private String realmName = null;

    /** caller-provided message security configurations */
    private final List<MessageSecurityConfig> messageSecurityConfigs = new ArrayList<MessageSecurityConfig>();

    /**
     * Returns a new <code>AppClientContainer<code> initialized to contact the specified
     * server during bootstrapping and to run the specified client file.
     * @param host server to contact during bootstrapping
     * @param port port on the server to connect to
     * @param client <code>File</code> for the client to execute. The
     * <code>File</code> can be for a client JAR file or for a directory.
     * @return initialized <code>AppClientContainer</code>
     * @throws java.io.IOException if the client file does not exist or cannot be read
     */
    public static AppClientContainer newContainer(final String host,
            final int port, final File client) throws IOException, NamingException {
        return newContainer(new TargetServer[] {new TargetServer(host, port)}, client);
    }

    /**
     * Returns a new <code>AppClientContainer</code> initialized to consider the
     * specified servers during bootstrapping and to run the specified client file.
     * @param targetServers server(s) to consider during bootstrapping
     * @param client <code>File</code> for the client to execute. The
     * <code>File</code> can be for a client JAR file or for a directory.
     * @return initialized <code>AppClientContainer</code>
     * @throws java.io.IOException if the client file does not exist or cannot be read
     */
    public static AppClientContainer newContainer(final TargetServer[] targetServers,
            final File client) throws IOException, NamingException {
        return new AppClientContainer(targetServers, client);
    }

    /**
     * Returns a new <code>AppClientContainer</code> initialized to contact the
     * specified server during bootstrapping and to run the client available at
     * the specified URI.
     * @param host server to contact during bootstrapping
     * @param port port on the server to connect to
     * @param clientURI <code>URI</code> for the client JAR or directory
     * @return initialized <code>AppClientContainer</code>
     */
    public static AppClientContainer newContainer(final String host,
            final int port, final URI clientURI) throws NamingException  {
        return newContainer(new TargetServer[] {new TargetServer(host, port)}, clientURI);
    }

    /**
     * Returns a new <code>AppClientContainer</code> initialized to consider the
     * specified server(s) during bootstrapping and to run the client available
     * at the specified URI.
     * @param targetServers server(s) to consider during bootstrapping
     * @param clientURI <code>URI</code> for the client JAR or directory
     * @return initialized <code>AppClientContainer</code>
     */
    public static AppClientContainer newContainer(final TargetServer[] targetServers,
            final URI clientURI) throws NamingException {
        return new AppClientContainer(targetServers, clientURI);
    }


    private AppClientContainer(final TargetServer[] targetServers) {
        this.targetServers = targetServers;
    }

    private AppClientContainer(final TargetServer[] targetServers, final File client) throws IOException, NamingException {
        this(targetServers);
        validateFile(client);
        initACC(client.toURI());
    }

    private AppClientContainer(final TargetServer[] targetServers, final URI clientURI) throws NamingException {
        this(targetServers);
        initACC(clientURI);
    }


    private void validateFile(final File f) throws IOException {
        if ( ! f.exists()) {
            throw new FileNotFoundException(f.getCanonicalPath());
        }
        if ( ! f.canRead()) {
            throw new IOException("Client file " + f.getCanonicalPath() + "exists but cannot be read");
        }
    }

    private void initACC(final URI clientURI) throws NamingException {
        this.clientURI = clientURI;
        AppClientContainerSecurityHelper secHelper =
                new AppClientContainerSecurityHelper(
                    targetServers,
                    containerProperties,
                    username,
                    password,
                    realmName);
        InitialContext ic = new InitialContext(secHelper.getIIOPProperties());
    }

    private void checkConfigure() {
        state = state.checkConfigure();
    }

    private void checkRun(){
        state = state.checkRun();
    }

    private void checkStop() {
        state = state.checkStop();
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    public void setSendPassword(final boolean sendPassword) {
        this.sendPassword = sendPassword;
    }

    public void setClientArgs(String[] args) {
        clientArgs = args;
    }

    public void setContainerProperties(final Properties props) {
        containerProperties = props;
    }

    /**
     * Sets the optional authentication realm for the ACC.
     * <p>
     * Each specific realm will determine which properties should be set in the
     * Properties argument.
     *
     * @param className name of the class which implements the realm
     */
    public void setAuthenticationRealm(String className) {
        checkConfigure();
        authRealm = new AuthRealm(className);
    }

    /**
     * Sets the name of the main client class to be loaded, injected, and run
     * when the container is started.
     * <p>
     * If the calling program passed a .class file as the client File, then it
     * does not also need to invoke this method.  Otherwise, if the calling
     * program does not invoke this method, then the ACC will
     * use the Main-Class attribute from the client's manifest to choose what
     * main class to execute.  If the manifest contains no such attribute and
     * the calling program has not set the main class name by invoking this method,
     * then the run method will throw an exception.
     * 
     * @param mainClassName name of the main class to execute
     */
    public void setMainClass(final String mainClassName) {
        checkConfigure();
        this.mainClassName = mainClassName;
    }

    /**
     * Sets the optional client credentials to be used during authentication to the
     * back-end.
     * <p>
     * If the client does not invoke <code>setClientCredentials</code> then the
     * ACC will use a {@link CallbackHandler} when it discovers that authentication
     * is required.  The ACC will use a developer-provided callback handler as
     * specified either by an invocation of {@link #setCallbackHandler} or
     * by the setting in the client's deployment descriptor.  Otherwise the ACC
     * uses its default login callback handler to prompt for and collect
     * authentication information when it is needed.
     *
     * @param username username valid in the default realm on the server
     * @param password password valid in the default realm on the server for the username
     */
    public void setClientCredentials(final String username, final char[] password) {
        checkConfigure();
        setClientCredentials(username, password, null);
    }

    /**
     * Sets the optional client credentials and server-side realm to be used during
     * authentication to the back-end.
     * <p>
     * If the client does not invoke <code>setClientCredentials</code> then the
     * ACC will use a {@link CallbackHandler} when it discovers that authentication
     * is required.  The ACC will use a developer-provided callback handler as
     * specified either by an invocation of {@link #setCallbackHandler} or
     * by the setting in the client's deployment descriptor.  Otherwise the ACC
     * uses its default login callback handler to prompt for and collect
     * authentication information when it is needed.
     *
     * @param username username valid in the specified realm on the server
     * @param password password valid in the specified realm on the server for the username
     * @param realmName name of the realm on the server within which the credentials are valid
     */
    public void setClientCredentials(final String username, final char[] password, String realmName) {
        checkConfigure();
        this.username = username;
        this.password = password;
        this.realmName = realmName;
    }

    /**
     * Sets the callback handler the ACC will use when authentication is
     * required.  If the program does not invoke this method the ACC will use
     * the callback handler specified in the client's deployment descriptor,
     * if any.  Failing that, the ACC will use its own default callback handler
     * to prompt for and collect credentials.
     * <p>
     * A callback handler class name set using this method overrides the
     * callback handler setting from the client's descriptor, if any.
     *
     * @param callbackHandlerClassName fully-qualified name of the developer's
     * callback handler class
     */
    public void setCallbackHandler(final String callbackHandlerClassName) {
        checkConfigure();
        this.callbackHandlerClassName = callbackHandlerClassName;
    }

    /**
     * Adds an optional {@link MessageSecurityConfig} setting.
     *
     * @param msConfig the new MessageSecurityConfig
     */
    public void addMessageSecurityConfig(final MessageSecurityConfig msConfig) {
        checkConfigure();
        messageSecurityConfigs.add(msConfig);
    }

    /**
     * Sets whether the ACC should execute the client.
     * 
     * @param runClient
     */
    public void setRunClient(final boolean runClient) {
        this.runClient = runClient;
    }

    /**
     * Runs the app client container.
     * <p>
     * The <code>run</code> method returns as soon as the container has
     * initialized itself and has loaded (if needed) and started the specified
     * client main program.  Any
     * threads which the client program creates can continue to run after
     * this method returns to the calling program.
     * <p>
     * The calling program can invoke {@link #stop} to shut down the container.
     */
    public void run() {
        checkRun();

        /*
         * Make sure the clean-up work will occur as part of VM shutdown, if
         * not before.
         */
        Runtime.getRuntime().addShutdownHook(cleanupThread = new Thread(cleanup, "Cleanup"));


    }

    /**
     * Shuts down the app client container.
     * <p>
     * If the calling program does not invoke <code>stop</code> the ACC will
     * invoke it automatically during JVM shutdown.  Note that <code>stop</code>
     * does not stop or notify any threads that the developer's app client might
     * have started.
     */
    public void stop() {
        checkStop();
        Runtime.getRuntime().removeShutdownHook(cleanupThread);
        cleanup.run();
    }

    /**
     * Encapsulates all clean-up activity.
     * <p>
     * The calling program can invoke clean-up by invoking the <code>stop</code>
     * method or by letting the JVM exit, in which case clean-up will occur as
     * part of VM shutdown.
     */
    private class Cleanup implements Runnable {
        private AppClientContainer acc = null;
        private AppClientInfo appClientInfo = null;
        private boolean cleanedUp = false;
        private InjectionManager injectionMgr = null;
        private ApplicationClientDescriptor appClient = null;
        private Class cls = null;

        public Cleanup(final AppClientContainer acc) {
            this.acc = acc;
        }

        public void setAppClientInfo(AppClientInfo info) {
            appClientInfo = info;
        }

        public void setInjectionManager(InjectionManager injMgr, Class cls, ApplicationClientDescriptor appDesc) {
            injectionMgr = injMgr;
            this.cls = cls;
            appClient = appDesc;
        }

        public void run() {
            logger.info("Clean-up starting");
            cleanUp();
        }

        private void cleanUp() {
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
