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
import com.sun.enterprise.module.bootstrap.BootException;
import com.sun.enterprise.util.LocalStringManager;
import com.sun.enterprise.util.LocalStringManagerImpl;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;
import javax.security.auth.callback.CallbackHandler;

import org.glassfish.api.naming.ClientNamingConfigurator;
import org.glassfish.appclient.client.acc.config.AuthRealm;
import org.glassfish.appclient.client.acc.config.ClientCredential;
import org.glassfish.appclient.client.acc.config.MessageSecurityConfig;
import org.glassfish.appclient.client.acc.config.Property;
import org.glassfish.appclient.client.acc.config.TargetServer;
import org.glassfish.appclient.client.acc.config.util.XML;
import org.jvnet.hk2.component.Habitat;
import org.xml.sax.SAXParseException;

/**
 * Implements a builder for accumulating configuration information for the
 * app client container and then starting the ACC.
 * <p>
 * The interface for the  ACC builder is defined as AppClientContainer.Builder so the
 * relevant JavaDoc is concentrated in that one class.
 *<p>
 * The AppClientContainerBuilder class records the
 * information the container itself needs in order to operate.
 *
 * @author tjquinn
 */
public class AppClientContainerBuilder implements AppClientContainer.Builder {

    private static final LocalStringManager localStrings = new LocalStringManagerImpl(AppClientContainerBuilder.class);
    /** caller-specified target servers */
    private TargetServer[] targetServers;

    /** caller-optional logger  - initialized to logger name from the class; caller can override with logger method */
    private Logger logger = Logger.getLogger(getClass().getName());

    private AuthRealm authRealm = null;

    private ACCClassLoader classLoader = (ACCClassLoader) Thread.currentThread().getContextClassLoader();

    /**
     * The caller can pre-set the client credentials using the
     * <code>clientCredentials</code> method.  The ACC will use the
     * username and realm values in intializing a callback handler if one is
     * needed.
     */
    private ClientCredential clientCredential = null;

    private boolean sendPassword = true;

    /** caller-provided message security configurations */
    private final List<MessageSecurityConfig> messageSecurityConfigs = new ArrayList<MessageSecurityConfig>();

//    private Class<? extends CallbackHandler> callbackHandlerClass = null;

    /**
     * optional caller-specified properties governing the ACC's behavior.
     * Correspond to the property elements available in the client-container
     * element from sun-application-client-containerxxx.dtd.
     */
    private Properties containerProperties = null;

    AppClientContainerBuilder() {

    }
    
    /**
     * Creates a new builder with the specified target servers and client URI.
     *
     * @param targetServers the <code>TargetServer</code>s to use
     * @param clientURI the URI of the client archive to launch
     */
    AppClientContainerBuilder(final TargetServer[] targetServers) {
        this.targetServers = targetServers;
    }

    public AppClientContainer newContainer(final Class mainClass,
            final CallbackHandler callerSpecifiedCallbackHandler) throws Exception {
        prepareHabitatAndNaming();
        Launchable client = Launchable.LaunchableUtil.newLaunchable(
                ACCModulesManager.getHabitat(), mainClass);
        AppClientContainer container = createContainer(client, 
                callerSpecifiedCallbackHandler, false /* istextAuth */);
        return container;
    }
    
    public AppClientContainer newContainer(final Class mainClass) throws Exception {
        return newContainer(mainClass, null);

    }

    public AppClientContainer newContainer(final URI clientURI,
            final CallbackHandler callerSpecifiedCallbackHandler,
            final String callerSpecifiedMainClassName,
            final String callerSpecifiedAppClientName) throws Exception, UserError {
        return newContainer(clientURI, callerSpecifiedCallbackHandler,
                callerSpecifiedMainClassName,
                callerSpecifiedAppClientName,
                false /* isTextAuth */);
    }

    public AppClientContainer newContainer(final URI clientURI,
            final CallbackHandler callerSpecifiedCallbackHandler,
            final String callerSpecifiedMainClassName,
            final String callerSpecifiedAppClientName,
            final boolean isTextAuth) throws Exception, UserError {
        prepareHabitatAndNaming();
        Launchable client = Launchable.LaunchableUtil.newLaunchable(
                clientURI,
                callerSpecifiedMainClassName,
                callerSpecifiedAppClientName,
                ACCModulesManager.getHabitat());

        AppClientContainer container = createContainer(client, 
                callerSpecifiedCallbackHandler, isTextAuth);
        return container;
    }

    public AppClientContainer newContainer(final URI clientURI) throws Exception, UserError {
        return newContainer(clientURI, null, null, null);
    }

    private AppClientContainer createContainer(final Launchable client,
            final CallbackHandler callerSuppliedCallbackHandler,
            final boolean isTextAuth) throws BootException, BootException, URISyntaxException, ClassNotFoundException, InstantiationException, IllegalAccessException, InjectionException, IOException, SAXParseException {
        AppClientContainer container = ACCModulesManager.getComponent(AppClientContainer.class);
        container.setClient(client);
        container.setBuilder(this);
        CallbackHandler callbackHandler = 
                (callerSuppliedCallbackHandler != null ? 
                    callerSuppliedCallbackHandler : getCallbackHandlerFromDescriptor(client.getDescriptor(classLoader).getCallbackHandler()));
        container.prepareSecurity(targetServers, messageSecurityConfigs, containerProperties,
                clientCredential, callbackHandler, classLoader, isTextAuth);
        return container;
    }

    private CallbackHandler getCallbackHandlerFromDescriptor(final String callbackHandlerName) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        if (callbackHandlerName != null && ! callbackHandlerName.equals("")) {
            Class<CallbackHandler> callbackHandlerClass =
                    (Class<CallbackHandler>) Class.forName(callbackHandlerName, true, classLoader);
            return callbackHandlerClass.newInstance();
        }
        return null;
    }

    private void prepareHabitatAndNaming() throws URISyntaxException {
        ACCModulesManager.initialize(Thread.currentThread().getContextClassLoader());
        ClientNamingConfigurator namingConfig = ACCModulesManager.getHabitat().getByContract(ClientNamingConfigurator.class);
        if (targetServers.length > 0) {
            namingConfig.setDefaultHost(targetServers[0].getAddress());
            namingConfig.setDefaultPort(Integer.toString(targetServers[0].getPort()));
        }
    }
//    /**
//     * Returns an AppClientContainer prepared to execute the app client implied
//     * by the launch info and the app client args.
//     * @param launchInfo info about the launch (type, name)
//     * @param appclientArgs appclient command line arguments
//     * @return
//     */
//    AppClientContainer newContainer(final CommandLaunchInfo launchInfo, final AppclientCommandArguments appclientArgs) {
//        AppClientContainer acc = null;
////        switch (launchInfo.getClientLaunchType()) {
////            case JAR:
////                /*
////                 * The user will have used local file path syntax, so create a
////                 * file first and then get its URI.
////                 */
////                File f = new File(launchInfo.getClientName());
////                acc = newContainer(f.toURI());
////
////                StandAloneAppClientInfo acInfo = new StandAloneAppClientInfo(
////                        false /* isJWS */,
////                        logger,
////                        f, archivist, mainClassFromCommandLine)
////        }
//        return acc;
//    }

    public AppClientContainerBuilder addMessageSecurityConfig(final MessageSecurityConfig msConfig) {
        messageSecurityConfigs.add(msConfig);
        return this;
    }

    public List<MessageSecurityConfig> getMessageSecurityConfig() {
        return this.messageSecurityConfigs;
    }

    public AppClientContainerBuilder logger(final Logger logger) {
        this.logger = logger;
        return this;
    }

    public Logger getLogger() {
        return logger;
    }


    public AppClientContainerBuilder authRealm(final String className) {
        authRealm = new AuthRealm(className);
        return this;
    }

    public AuthRealm getAuthRealm() {
        return authRealm;
    }

    public AppClientContainerBuilder clientCredentials(final String user, final char[] password) {
        return clientCredentials(user, password, null);
    }

    public AppClientContainerBuilder clientCredentials(final String user, final char[] password, final String realm) {
//        this.clientCredential = new ClientCredential()
//        this.user = user;
//        this.password = password;
//        this.realmName = realm;
        ClientCredential cc = new ClientCredential(user, new XML.Password(password), realm);
        return clientCredentials(cc);
    }

    public AppClientContainerBuilder clientCredentials(final ClientCredential cc) {
        clientCredential = cc;
        return this;
    }

    public ClientCredential getClientCredential() {
        return clientCredential;
    }

    public AppClientContainerBuilder containerProperties(final Properties props) {
        this.containerProperties = props;
        return this;
    }

    public AppClientContainerBuilder containerProperties(final List<Property> props) {
        containerProperties = XML.toProperties(props);
        return this;
    }

    public Properties getContainerProperties() {
        return containerProperties;
    }

    public AppClientContainerBuilder sendPassword(final boolean sendPassword){
        this.sendPassword = sendPassword;
        return this;
    }

    public boolean getSendPassword() {
        return sendPassword;
    }

//    public AppClientContainerBuilder callbackHandler(final Class<? extends CallbackHandler> callbackHandlerClass) {
//        this.callbackHandlerClass = callbackHandlerClass;
//        return this;
//    }

//    public Class<? extends CallbackHandler> getCallbackHandler() {
//        return callbackHandlerClass;
//    }

    public TargetServer[] getTargetServers() {
        return targetServers;
    }

//    public AppClientContainerBuilder mainClass(Class mainClass) {
//        this.mainClass = mainClass;
//        return this;
//    }

//    public Class getMainClass() {
//        return mainClass;
//    }
//
//    public AppClientContainerBuilder mainClassName(String mainClassName) {
//        if (isMainClassFromCaller) {
//            throw new IllegalStateException();
//        }
//        this.mainClassName = mainClassName;
//        return this;
//    }
//
//    public String getMainClassName() {
//        return mainClassName;
//    }
//
//    public AppClientContainerBuilder mainClass(final Class mainClass) {
//        this.mainClass = mainClass;
//        mainClassName = mainClass.getName();
//        return this;
//    }
//
//    public Method getMainMethod() {
//        return mainMethod;
//    }
//
//    private void completeConfig() throws NoSuchMethodException {
//        mainMethod = initMainMethod();
//    }
//

}
