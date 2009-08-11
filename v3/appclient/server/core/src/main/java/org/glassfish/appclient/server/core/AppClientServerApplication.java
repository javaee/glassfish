/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 * 
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

package org.glassfish.appclient.server.core;

import org.glassfish.appclient.server.core.jws.Util;
import com.sun.enterprise.deployment.Application;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import org.glassfish.api.admin.config.Property;
import org.glassfish.api.container.EndpointRegistrationException;
import org.glassfish.api.deployment.DeployCommandParameters;
import org.glassfish.appclient.server.core.jws.JavaWebStartState;
import com.sun.enterprise.config.serverbeans.Applications;
import com.sun.enterprise.deployment.ApplicationClientDescriptor;
import com.sun.enterprise.deployment.runtime.JnlpDocDescriptor;
import com.sun.logging.LogDomains;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.glassfish.api.container.RequestDispatcher;
import org.glassfish.api.deployment.ApplicationContainer;
import org.glassfish.api.deployment.ApplicationContext;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.appclient.server.core.jws.servedcontent.ASJarSigner;
import org.glassfish.appclient.server.core.jws.servedcontent.AutoSignedContent;
import org.glassfish.appclient.server.core.jws.servedcontent.DynamicContent;
import org.glassfish.appclient.server.core.jws.servedcontent.Content;
import org.glassfish.appclient.server.core.jws.servedcontent.FixedContent;
import org.glassfish.appclient.server.core.jws.servedcontent.SimpleDynamicContentImpl;
import org.glassfish.appclient.server.core.jws.servedcontent.StaticContent;
import org.glassfish.appclient.server.core.jws.servedcontent.TokenHelper;
import org.glassfish.deployment.common.DownloadableArtifacts.FullAndPartURIs;
import org.jvnet.hk2.config.ConfigListener;
import org.jvnet.hk2.config.UnprocessedChangeEvent;
import org.jvnet.hk2.config.UnprocessedChangeEvents;

/**
 * Represents an app client module, either stand-alone or nested inside
 * an EAR, loaded on the server.
 * <p>
 * The primary purpose of this class is to implement Java Web Start support for
 * launches of this app client.  Other than in that sense, app clients do not
 * run in the server.  To support a client for Java Web Start launches, this
 * class figures out what static content (JAR files) and dynamic content (JNLP
 * documents) are needed by the client.  It then generates the required
 * dynamic content templates and submits them and the static content to a
 * Grizzly adapter which actually serves the data in response to requests.
 *
 * @author tjquinn
 */
public class AppClientServerApplication implements 
        ApplicationContainer<ApplicationClientDescriptor>, ConfigListener {

    private final DeploymentContext dc;
    private final AppClientDeployer deployer;

    private final Logger logger;
    
    private final AppClientDeployerHelper helper;

    private static final String JNLP_MIME_TYPE = "application/x-java-jnlp-file";
    private static final String HTML_MIME_TYPE = "text/html";
    private static final String XML_MIME_TYPE = "application/xml";

    private static final String DOC_TEMPLATE_PREFIX = "/org/glassfish/appclient/server/core/jws/templates/";
//    private static final String DOC_TEMPLATE_PREFIX = "jws/templates/";
    static final String MAIN_DOCUMENT_TEMPLATE = 
            DOC_TEMPLATE_PREFIX + "appclientMainDocumentTemplate.jnlp";
    private static final String CLIENT_DOCUMENT_TEMPLATE =
            DOC_TEMPLATE_PREFIX + "appclientClientDocumentTemplate.jnlp";
    
        //, "appclientClientDocumentTemplate.jnlp"
        //, "appclientClientFacadeDocumentTemplate.jnlp"

    private static final String MAIN_IMAGE_XML_PROPERTY_NAME =
            "appclient.main.information.images";
    private static final String APP_LIBRARY_EXTENSION_PROPERTY_NAME = "app.library.extension";
    private static final String APP_CLIENT_MAIN_CLASS_ARGUMENTS_PROPERTY_NAME =
            "appclient.main.class.arguments";
    private static final String CLIENT_FACADE_JAR_PATH_PROPERTY_NAME =
            "client.facade.jar.path";
    private static final String CLIENT_JAR_PATH_PROPERTY_NAME =
            "client.jar.path";
    

    /**
     * records if the app client is eligible for Java Web Start support, as
     * defined in the developer-provided sun-application-client.xml descriptor
     */
    private final boolean isJWSEligible;
    
    /**
     * records if the containing app is set to enable Java Web
     * Start access (in the domain.xml config for the application and the
     * module) - could be updated from a separate
     * thread if the administrator changes the java-web-start-enabled setting
     */
    private volatile boolean isJWSEnabledAtApp = true;
    private volatile boolean isJWSEnabledAtModule = true;

    private final JavaWebStartState jwsState = new JavaWebStartState();

    private final static String JAVA_WEB_START_ENABLED_PROPERTY_NAME = "" +
            "java-web-start-enabled";

    private final static String GLASSFISH_DIRECTORY_PREFIX = "glassfish/";

    private static class SignedSystemContentFromApp {
        private final String tokenName;
        private final String relativePath;

        private SignedSystemContentFromApp(String tokenName, String relativePath) {
            this.tokenName = tokenName;
            this.relativePath = relativePath;
        }

        String getRelativePath() {
            return relativePath;
        }

        String getTokenName() {
            return tokenName;
        }

        URI getRelativePathURI() {
            return URI.create(relativePath);
        }
    }

    private final static List<SignedSystemContentFromApp> SIGNED_SYSTEM_CONTENT_SERVED_AT_APP_LEVEL =
            Arrays.asList(
                new SignedSystemContentFromApp(
                    "gf-client.jar",
                    GLASSFISH_DIRECTORY_PREFIX + "modules/gf-client.jar"),
                new SignedSystemContentFromApp(
                    "gf-client-module.jar", 
                    GLASSFISH_DIRECTORY_PREFIX + "modules/gf-client-module.jar")
                    );
    
    private final RequestDispatcher requestDispatcher;

    private final ApplicationClientDescriptor acDesc;
    private final Application appDesc;
    private final com.sun.enterprise.config.serverbeans.Applications applications;
    private final String deployedAppName;
    private final TokenHelper tHelper;
    private final ASJarSigner jarSigner;

    private Set<Content> myContent = null;

    AppClientServerApplication(final DeploymentContext dc, 
            final AppClientDeployer deployer,
            final AppClientDeployerHelper helper,
            final RequestDispatcher requestDispatcher,
            final Applications applications,
            final ASJarSigner jarSigner,
            final Logger logger) {
        this.dc = dc;
        this.deployer = deployer;
        this.helper = helper;
        this.logger = LogDomains.getLogger(AppClientServerApplication.class,
                LogDomains.ACC_LOGGER);
        this.requestDispatcher = requestDispatcher;

        acDesc = helper.appClientDesc();

        isJWSEligible = acDesc.getJavaWebStartAccessDescriptor().isEligible();
        isJWSEnabledAtApp = isJWSEnabled(dc.getAppProps());
        isJWSEnabledAtModule = isJWSEnabled(dc.getModuleProps());
        appDesc = acDesc.getApplication();

        deployedAppName = dc.getCommandParameters(DeployCommandParameters.class).name();
        this.applications = applications;
        tHelper = TokenHelper.newInstance(helper);
        this.jarSigner = jarSigner;

    }
        

    public ApplicationClientDescriptor getDescriptor() {
        return acDesc;
    }

    public boolean start(ApplicationContext startupContext) throws Exception {
        return start();
    }


    boolean start() {
        /*
         * The developer might have disabled Java Web Start support in the
         * sun-application-client.xml or in the domain's configuration,
         * so check those before starting JWS services.
         */
        if (isJWSRunnable()) {
            jwsState.transition(JavaWebStartState.Action.START, new Runnable() {
                public void run() {
                    try {
                        startJWSServices();
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                }
            });
        }
        return true;
    }

    public boolean stop(ApplicationContext stopContext) {
        return stop();
    }

    boolean stop() {
        jwsState.transition(JavaWebStartState.Action.STOP, new Runnable() {
            public void run() {
                try {
                    stopJWSServices();
                } catch (EndpointRegistrationException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
        return true;
    }

    public boolean suspend() {
        jwsState.transition(JavaWebStartState.Action.SUSPEND, new Runnable() {
            public void run() {
                suspendJWSServices();
            }
        });
        return true;
    }

    public boolean resume() throws Exception {
        if (isJWSRunnable()) {
            jwsState.transition(JavaWebStartState.Action.RESUME, new Runnable() {
                public void run() {
                    resumeJWSServices();
                }
            });
        }
        return true;
    }

    public ClassLoader getClassLoader() {
        /*
         * This cannot be null or it prevents the framework from invoking unload
         * on the deployer for this app.
         */
        return new URLClassLoader(new URL[0]);
    }

    DeploymentContext dc() {
        return dc;
    }
        
    private void startJWSServices() throws EndpointRegistrationException, IOException {
        if (myContent == null) {
            addClientContentToHTTPAdapter();
        }
        deployer.addContributor(deployedAppName, this);

        /*
         * Currently, we implement the ability to disable or enable app clients
         * within an EAR by marking the associated content as disabled or
         * enabled, which the Grizzly adapter looks at before responding to
         * a request for that bit of content.  So mark all the content as
         * started.
         */
        for (Content c : myContent) {
            c.start();
        }
        logger.log(Level.INFO, "enterprise.deployment.appclient.jws.started",
            new Object[] {moduleExpression(),
            "TBD"});
    }

    private void stopJWSServices() throws EndpointRegistrationException {
        /*
         * Mark all this client's content as stopped so the Grizzly adapter
         * will not serve it.
         */
        for (Content c : myContent) {
            c.stop();
        }

        deployer.removeContributor(deployedAppName, this);
        logger.log(Level.INFO, "enterprise.deployment.appclient.jws.stopped",
                moduleExpression());
    }

    private void suspendJWSServices() {
        for (Content c : myContent) {
            c.suspend();
        }
    }

    private void resumeJWSServices() {
        for (Content c : myContent) {
            c.resume();
        }
    }

    private void addClientContentToHTTPAdapter() throws EndpointRegistrationException, IOException {


        /*
         * NOTE - Be sure to initialize the static content first.  That method
         * assigns some properties that can appear as placeholders in the
         * dynamic content.
         */
        final Map<String,StaticContent> staticContent =
                initClientStaticContent();

        final Map<String,DynamicContent> dynamicContent =
                initClientDynamicContent();

        final String jnlpDoc = acDesc.getJavaWebStartAccessDescriptor().getJnlpDocument();
        if (jnlpDoc != null && jnlpDoc.length() > 0) {
            DeveloperContentHandler.addDeveloperContent(dc.getClassLoader(),
                    jnlpDoc, tHelper,
                    dc.getSourceDir(), staticContent, dynamicContent);

        }
        myContent = new HashSet<Content>(staticContent.values());
        myContent.addAll(dynamicContent.values());

        deployer.addContentToHTTPAdapter(deployedAppName, this, tHelper.tokens(),
                staticContent, dynamicContent);
    }

    private Map<String,StaticContent> initClientStaticContent() 
            throws IOException, EndpointRegistrationException {
        final Map<String,StaticContent> result = new HashMap<String,StaticContent>();

        /*
         * The client-level adapter's static content is the app client JAR and
         * the app client facade.
         */
        createAndAddStaticContent(result, helper.appClientServerURI(dc),
                helper.appClientUserURI(dc),
                CLIENT_JAR_PATH_PROPERTY_NAME);

        createAndAddSignedStaticContentFromGeneratedFile(result, helper.facadeServerURI(dc),
                helper.facadeUserURI(dc),
                CLIENT_FACADE_JAR_PATH_PROPERTY_NAME);

        /*
         * Make sure that there are versions of gf-client.jar and gf-client-module.jar
         * that are signed by the same cert used to sign the facade JAR for
         * this app.  That's because the user might have chosen to sign using
         * a particular alias so the end-users will accept JARs signed by
         * the corresponding cert.  (Java Web Start will prompt them to do this
         * during the download of signed JARs.)
         *
         * Note that the following logic makes sure that such signed versions
         * exist.  If multiple apps use the same cert to sign JARs, then the
         * multiple instances of AutoSignedContent class for the same signed
         * JAR will point to and reuse the same signed JAR, rather than
         * re-sign it each time an app needed it is started.
         */
        addSignedSystemContent(result);
        
        /*
         * The developer might have used the sun-application-client.xml
         * java-web-start-support/vendor setting to communicate icon and/or
         * splash screen images URIs.
         */
        prepareImageInfo(result, tHelper.tokens());

        /*
         * Make sure that all the EAR-level JARs that this client refers to
         * are represented as content as well.  This could result in a JAR
         * referenced from multiple clients being added more than once, but
         * these library JARs are recorded in a set so each appears at most once.
         */
        for (FullAndPartURIs artifact : helper.earLevelDownloads()) {
            final String uriString = artifact.getPart().toASCIIString();
            result.put(uriString, new FixedContent(new File(artifact.getFull())));
        }

        // TODO: needs to be expanded to handle signed library JARS, perhap signed by different certs
        tHelper.setProperty(APP_LIBRARY_EXTENSION_PROPERTY_NAME, "");
        return result;
    }

    private void addSignedSystemContent(
            final Map<String,StaticContent> content) {
        for (SignedSystemContentFromApp signedContentFromApp : SIGNED_SYSTEM_CONTENT_SERVED_AT_APP_LEVEL) {
            final AutoSignedContent signedContent =
                    deployer.appLevelSignedSystemContent(
                        signedContentFromApp.getRelativePath(),
                        helper.signingAlias());
            recordStaticContent(content, signedContent, 
                    signedContentFromApp.getRelativePathURI(),
                    signedContentFromApp.getTokenName());
        }
    }

    private void createAndAddStaticContent(final Map<String,StaticContent> content,
            final URI uriToFile, 
            final URI uriForLookup,
            final String tokenName) {
        
        final StaticContent jarContent = new FixedContent(
                new File(uriToFile));
        recordStaticContent(content, jarContent, uriForLookup, tokenName);
    }

    private void createAndAddSignedStaticContentFromGeneratedFile(final Map<String,StaticContent> content,
            final URI uriToFile,
            final URI uriForLookup,
            final String tokenName) {

        final File unsignedFile = new File(uriToFile);
        final File signedFile = signedFileForGeneratedAppFile(unsignedFile);
        signedFile.getParentFile().mkdirs();
        final StaticContent signedJarContent = new AutoSignedContent(
                unsignedFile, 
                signedFile,
                helper.signingAlias(),
                jarSigner);
        recordStaticContent(content, signedJarContent, uriForLookup, tokenName);
    }

    private void recordStaticContent(final Map<String,StaticContent> content,
            final StaticContent newContent,
            final URI uriForLookup,
            final String tokenName) {

        final String uriStringForLookup = uriForLookup.toASCIIString();
        recordStaticContent(content, newContent, uriStringForLookup);
        tHelper.setProperty(tokenName, uriForLookup.toASCIIString());
    }

    private void recordStaticContent(final Map<String,StaticContent> content,
            final StaticContent newContent,
            final String uriStringForLookup) {
        content.put(uriStringForLookup, newContent);
        logger.fine("Recording static content: URI for lookup = " +
                uriStringForLookup + "; content = " + newContent.toString());
    }

    private File signedFileForGeneratedAppFile(final File unsignedFile) {
        /*
         * Signed files at the app level go in 
         *
         * generated/xml/(appName)/signed/(path-within-app-of-unsigned-file)
         * 
         * and when we're signing a generated file we just use its URI
         * relative to the app's scratch directory to compute the URI relative
         * to generated/xml/(appName)/signed where the signed file should reside.
         */
        final File rootForSignedFilesInApp = new File(dc.getScratchDir("xml"), "signed/");
        rootForSignedFilesInApp.mkdir();
        final URI unsignedFileURIRelativeToXMLDir = dc.getScratchDir("xml").toURI().
                relativize(unsignedFile.toURI());
        final URI signedFileURI = rootForSignedFilesInApp.toURI().resolve(unsignedFileURIRelativeToXMLDir);
        return new File(signedFileURI);
    }
    
    private Map<String,DynamicContent> initClientDynamicContent() throws IOException {


        final Map<String,DynamicContent> result = new HashMap<String,DynamicContent>();

        // TODO: needs to be expanded to pass any args we need to pass to the ACC (maybe all via agent args?)
        tHelper.setProperty(APP_CLIENT_MAIN_CLASS_ARGUMENTS_PROPERTY_NAME, "");

        createAndAddDynamicContent(result, tHelper.mainJNLP(), MAIN_DOCUMENT_TEMPLATE);
        
        /*
         * Add the main JNLP again but with an empty URI string so the user
         * can launch the app client by specifying only the context root.
         */
        createAndAddDynamicContent(result, "", MAIN_DOCUMENT_TEMPLATE);
        createAndAddDynamicContent(result, tHelper.clientJNLP(), CLIENT_DOCUMENT_TEMPLATE);


        // more templates and jnlps to come
        
        return result;
    }

    private void createAndAddDynamicContent(final Map<String,DynamicContent> content,
            final String uriStringForContent, final String uriStringForTemplate) throws IOException {
        final String processedTemplate = Util.replaceTokens(
                textFromURL(uriStringForTemplate), tHelper.tokens());
        content.put(uriStringForContent, newDynamicContent(processedTemplate,
                JNLP_MIME_TYPE));
        logger.fine("Adding dyn content " + uriStringForContent + System.getProperty("line.separator") +
                (logger.isLoggable(Level.FINER) ? processedTemplate : ""));


    }

    public static String textFromURL(final String templateURLString) throws IOException {
        final InputStream is = AppClientServerApplication.class.getResourceAsStream(templateURLString);
        if (is == null) {
            throw new FileNotFoundException(templateURLString);
        }
        StringBuilder sb = new StringBuilder();
        InputStreamReader reader = new InputStreamReader(is);
        char[] buffer = new char[1024];
        int charsRead;
        try {
            while ((charsRead = reader.read(buffer)) != -1) {
                sb.append(buffer, 0, charsRead);
            }
            return sb.toString();
        } catch (IOException e) {
            throw new IOException(templateURLString, e);
        } finally {
            try {
                reader.close();
            } catch (IOException ignore) {
                throw new IOException("Error closing template stream after error", ignore);
            }
        }

    }

    private static DynamicContent newDynamicContent(final String template,
            final String mimeType) {
        return new SimpleDynamicContentImpl(template, mimeType);
    }

    /**
     * Returns XML which specifies the icon image, the splash screen image, neither, or
     * both, depending on the contents of the <vendor> text in the descriptor.
     *
     * @return XML specifying one or both images; empty if the developer encoded
     * no image information in the <vendor> element of the sun-application-client.xml
     * descriptor
     */
    private void prepareImageInfo(final Map<String,StaticContent> staticContent,
            final Properties tokens) {
        StringBuilder result = new StringBuilder();
        final VendorInfo vendorInfo = getVendorInfo();
        String imageURI = vendorInfo.getImageURI();
        if (imageURI.length() > 0) {
            result.append("<icon href=\"" + imageURI + "\"/>");
            addImageContent(imageURI, staticContent);
        }
        String splashImageURI = vendorInfo.getSplashImageURI();
        if (splashImageURI.length() > 0) {
            result.append("<icon kind=\"splash\" href=\"" + splashImageURI + "\"/>");
            addImageContent(splashImageURI, staticContent);
        }
        if (result.length() == 0) {
            result.append("<!-- No image information specified in sun-application-client.xml -->");
        }
        tokens.setProperty(MAIN_IMAGE_XML_PROPERTY_NAME, result.toString());
    }

    private void addImageContent(final String imageURI,
            final Map<String,StaticContent> staticContent) {
        StaticContent sc = helper.fixedContentWithinEAR(imageURI);
        /*
         * The user might specify an image within a stand-alone client's
         * vendor setting in the sun-application-client.xml, which we cannot
         * support.  Add the content only if it's not null.
         */
        if (sc != null) {
            staticContent.put(imageURI, sc);
        }
    }

    private String ensureLeadingSlash(String s) {
        if ( ! s.startsWith("/")) {
            return "/" + s;
        } else {
            return s;
        }
    }

    /**
     * Returns if this client is enabled for Java Web Start access.
     * <p>
     * The administrator can set the java-web-start-enabled property at
     * either the application level or the module level or both.  For this
     * client to be enabled, any such specified property must be set to true.
     * The default is true.
     */
    private boolean isJWSEnabled(final Properties props) {
        boolean result = true;
        final String propsSetting = props.getProperty(JAVA_WEB_START_ENABLED_PROPERTY_NAME);
        if (propsSetting != null) {
            result &= Boolean.parseBoolean(propsSetting);
        }
        return result;
    }
    
    private boolean isJWSEnabled() {
        return isJWSEnabledAtApp && isJWSEnabledAtModule;
    }

    private boolean isJWSRunnable() {
        if ( ! isJWSEligible) {
            logger.log(Level.INFO, "enterprise.deployment.appclient.jws.noStart.ineligible",
                    moduleExpression());
        }

        if ( ! isJWSEnabled()) {
            logger.log(Level.INFO, "enterprise.deployment.appclient.jws.noStart.disabled",
                    moduleExpression());
        }
        return isJWSEligible && isJWSEnabled();
    }

    public UnprocessedChangeEvents changed(PropertyChangeEvent[] events) {
        /* Record any events we tried to process but could not. */
        List<UnprocessedChangeEvent> unprocessedEvents = new ArrayList<UnprocessedChangeEvent>();

        for (PropertyChangeEvent event : events) {
            try {
                processChangeEventIfInteresting(event);
            } catch (Exception e) {
                UnprocessedChangeEvent uce =
                        new UnprocessedChangeEvent(event, e.getLocalizedMessage());
                unprocessedEvents.add(uce);
            }
        }

        return (unprocessedEvents.size() > 0) ? new UnprocessedChangeEvents(unprocessedEvents) : null;
    }

    private void processChangeEventIfInteresting(final PropertyChangeEvent event) throws EndpointRegistrationException {
        /*
         * If the source is of type Application or Module and the newValue is of type
         * Property then this could be a change we're interested in.
         */
        final boolean isSourceApp = event.getSource() instanceof
                com.sun.enterprise.config.serverbeans.Application;
        final boolean isSourceModule = event.getSource() instanceof
                com.sun.enterprise.config.serverbeans.Module;

        if (     (! isSourceApp && ! isSourceModule)
              || ! (event.getNewValue() instanceof Property)) {
            return;
        }

        /*
         * Make sure the property name is java-web-start-enabled.
         */
        Property newPropertySetting = (Property) event.getNewValue();
        if ( ! newPropertySetting.getName().equals(JAVA_WEB_START_ENABLED_PROPERTY_NAME)) {
            return;
        }

        String eventSourceName;
        String thisAppOrModuleName;
        if (isSourceApp) {
            eventSourceName = ((com.sun.enterprise.config.serverbeans.Application) event.getSource()).getName();
            thisAppOrModuleName = appDesc.getRegistrationName();
        } else {
            eventSourceName = ((com.sun.enterprise.config.serverbeans.Module) event.getSource()).getName();
            thisAppOrModuleName = acDesc.getModuleName();
        }

        if ( ! thisAppOrModuleName.equals(eventSourceName)) {
            return;
        }

        /*
         * At this point we know that the event applies to this app client,
         * so return a Boolean carrying the newly-assigned value.
         */
        final Boolean newEnabledValue = Boolean.valueOf(newPropertySetting.getValue());
        final Property oldPropertySetting = (Property) event.getOldValue();
        final String oldPropertyValue = (oldPropertySetting != null)
                ? oldPropertySetting.getValue()
                : null;
        final Boolean oldEnabledValue = (oldPropertyValue == null
                ? Boolean.TRUE
                : Boolean.valueOf(oldPropertyValue));

        /*
         * Record the new value of the relevant enabled setting.
         */
        if (isSourceApp) {
            isJWSEnabledAtApp = newEnabledValue;
                } else {
            isJWSEnabledAtModule = newEnabledValue;
        }
        
        /*
         * Now act on the change of state.
         */
        if ( ! newEnabledValue.equals(oldEnabledValue)) {
            if (newEnabledValue) {
                start();
            } else {
                stop();
            }
        }
    }

    /**
     * Returns the client's contextRoot as specified by the developer or,
     * otherwise, in the format appName (for a stand-alone
     * client) and appName/moduleName (for a client in an EAR).  moduleName is
     * the URI to the client JAR.
     * @return
     */
    private String clientContextRoot() {
        String contextRoot;
        final String contextRootInDesc = developerSpecifiedContextRoot();
                
        if (contextRootInDesc != null && ! contextRootInDesc.equals("")) {
            contextRoot = contextRootInDesc;
        } else {
            contextRoot = moduleExpression();
        }
        return "/" + contextRoot;
    }

    private String moduleExpression() {
        String moduleExpression;
        if (appDesc.isVirtual()) {
            moduleExpression = appDesc.getRegistrationName();
        } else {
            moduleExpression = appDesc.getRegistrationName() + "/" + acDesc.getModuleName();
        }
        return moduleExpression;
    }

    private String developerSpecifiedContextRoot() {
        return acDesc.getJavaWebStartAccessDescriptor().getContextRoot();
    }

    private VendorInfo getVendorInfo() {
        VendorInfo vendorInfo = new VendorInfo(acDesc.getJavaWebStartAccessDescriptor().getVendor());
        return vendorInfo;
    }

    private static class VendorInfo {
        private String vendorStringFromDescriptor;
        private String vendor = "";
        private String imageURIString = "";
        private String splashImageURIString = "";

        private VendorInfo(String vendorStringFromDescriptor) {
            this.vendorStringFromDescriptor = vendorStringFromDescriptor != null ?
                vendorStringFromDescriptor : "";
            String [] parts = this.vendorStringFromDescriptor.split("::");
            if (parts.length == 1) {
                vendor = parts[0];
            } else if (parts.length == 2) {
                imageURIString = parts[0];
                vendor = parts[0];
            } else if (parts.length == 3) {
                imageURIString = parts[0];
                splashImageURIString = parts[1];
                vendor = parts[2];
            }
        }

        private String getVendor() {
            return vendor;
        }

        private String getImageURI() {
            return imageURIString;
        }

        private String getSplashImageURI() {
            return splashImageURIString;
        }
    }}
