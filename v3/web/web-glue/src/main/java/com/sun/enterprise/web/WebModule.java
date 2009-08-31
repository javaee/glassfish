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

package com.sun.enterprise.web;

import java.io.*;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.*;
import javax.servlet.http.HttpSession;

import com.sun.enterprise.config.serverbeans.ConfigBeansUtilities;
import com.sun.enterprise.config.serverbeans.J2eeApplication;
import com.sun.enterprise.container.common.spi.util.JavaEEObjectStreamFactory;
import com.sun.enterprise.deployment.AbsoluteOrderingDescriptor;
import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.WebBundleDescriptor;
import com.sun.enterprise.deployment.WebServiceEndpoint;
import com.sun.enterprise.deployment.WebServicesDescriptor;
import com.sun.enterprise.deployment.runtime.web.CookieProperties;
import com.sun.enterprise.deployment.runtime.web.LocaleCharsetInfo;
import com.sun.enterprise.deployment.runtime.web.LocaleCharsetMap;
import com.sun.enterprise.deployment.runtime.web.SessionConfig;
import com.sun.enterprise.deployment.runtime.web.SessionManager;
import com.sun.enterprise.deployment.runtime.web.SessionProperties;
import com.sun.enterprise.deployment.runtime.web.SunWebApp;
import com.sun.enterprise.deployment.runtime.web.WebProperty;
import com.sun.enterprise.deployment.web.ServletFilterMapping;
import com.sun.enterprise.security.integration.RealmInitializer;
import com.sun.enterprise.universal.GFBase64Decoder;
import com.sun.enterprise.universal.GFBase64Encoder;
import com.sun.enterprise.util.StringUtils;
import com.sun.enterprise.web.pwc.PwcWebModule;
import com.sun.enterprise.web.session.PersistenceType;
import com.sun.enterprise.web.session.SessionCookieConfig;
import com.sun.logging.LogDomains;
import com.sun.web.security.RealmAdapter;
import org.apache.catalina.Container;
import org.apache.catalina.ContainerListener;
import org.apache.catalina.InstanceListener;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.Loader;
import org.apache.catalina.Pipeline;
import org.apache.catalina.Realm;
import org.apache.catalina.Valve;
import org.apache.catalina.Wrapper;
import org.apache.catalina.core.StandardPipeline;
import org.apache.catalina.core.StandardWrapper;
import org.apache.catalina.deploy.FilterMaps;
import org.apache.catalina.loader.WebappLoader;
import org.apache.catalina.session.StandardManager;
import org.jvnet.hk2.config.types.Property;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.internal.api.ServerContext;
import org.glassfish.web.admin.monitor.ServletProbeProvider;
import org.glassfish.web.admin.monitor.SessionProbeProvider;
import org.glassfish.web.admin.monitor.WebModuleProbeProvider;
import org.glassfish.web.loader.ServletContainerInitializerUtil;
import org.glassfish.web.valve.GlassFishValve;

/**
 * Class representing a web module for use by the Application Server.
 */

public class WebModule extends PwcWebModule {

    // ----------------------------------------------------- Class Variables

    private static final Logger logger = LogDomains.getLogger(WebModule.class, LogDomains.WEB_LOGGER);
    protected static final ResourceBundle rb = logger.getResourceBundle();

    private static final String ALTERNATE_FROM = "from=";
    private static final String ALTERNATE_DOCBASE = "dir=";

    private static final GFBase64Encoder gfEncoder = new GFBase64Encoder();
    private static final GFBase64Decoder gfDecoder = new GFBase64Decoder();

    private static final String WS_SERVLET_CONTEXT_LISTENER =
        "com.sun.xml.ws.transport.http.servlet.WSServletContextListener";

    // ----------------------------------------------------- Instance Variables

    // Object containing sun-web.xml information
    private SunWebApp iasBean = null;

    //locale-charset-info tag from sun-web.xml
    private LocaleCharsetMap[] _lcMap = null;

    /**
     * Is the default-web.xml parsed?
     */
    private boolean hasBeenXmlConfigured = false;

    private WebContainer webContainer;

    private final Map<String,AdHocServletInfo> adHocPaths;
    private boolean hasAdHocPaths;

    private final Map<String,AdHocServletInfo> adHocSubtrees;
    private boolean hasAdHocSubtrees;

    private StandardPipeline adHocPipeline;

    // File encoding of static resources
    private String fileEncoding;

    /**
     * Cached findXXX results
     */
    protected Object[] cachedFinds;

    private com.sun.enterprise.config.serverbeans.WebModule bean;

    /**
     * The bean corresponding to the j2ee-application element in domain.xml
     * representing the application (EAR file) in which this web module has
     * been embedded
     */
    protected J2eeApplication appBean = null;

    private WebBundleDescriptor webBundleDescriptor;

    private boolean hasStarted = false;
    private String compEnvId = null;
    private ServerContext serverContext = null;

    private ServletProbeProvider servletProbeProvider = null;
    private SessionProbeProvider sessionProbeProvider = null;
    private WebModuleProbeProvider webModuleProbeProvider = null;

    private JavaEEObjectStreamFactory javaEEObjectStreamFactory;

    // The id of the parent container (i.e., virtual server) on which this
    // web module was deployed
    private String vsId;

    private WebModuleConfig wmInfo;


    /**
     * Constructor.
     */
    public WebModule() {
        super();
        this.adHocPaths = new HashMap<String,AdHocServletInfo>();
        this.adHocSubtrees = new HashMap<String,AdHocServletInfo>();

        this.adHocPipeline = new StandardPipeline(this);
        this.adHocPipeline.setBasic(new AdHocContextValve(this));

        notifyContainerListeners = false;
    }


    /**
     * set the sun-web.xml config bean
     */
    public void setIasWebAppConfigBean(SunWebApp iasBean) {
       this.iasBean = iasBean;
    }


    /**
     * gets the sun-web.xml config bean
     */
    public SunWebApp getIasWebAppConfigBean() {
       return iasBean;
    }


    /**
     * Gets the web container in which this web module was loaded.
     *
     * @return the web container in which this web module was loaded
     */
    public WebContainer getWebContainer() {
        return webContainer;
    }


    /**
     * Sets the web container in which this web module was loaded.
     *
     */
    public void setWebContainer(WebContainer webContainer) {
        this.webContainer = webContainer;
        this.servletProbeProvider = webContainer.getServletProbeProvider();
        this.sessionProbeProvider = webContainer.getSessionProbeProvider();
        this.webModuleProbeProvider =
            webContainer.getWebModuleProbeProvider();

        this.javaEEObjectStreamFactory = webContainer.getJavaEEObjectStreamFactory();
    }

    public void setWebModuleConfig(WebModuleConfig wmInfo) {
        this.wmInfo = wmInfo;
    }

    public WebModuleConfig getWebModuleConfig() {
        return wmInfo;
    }

    /**
     * Sets the parameter encoding (i18n) info from sun-web.xml.
     */
    public void setI18nInfo() {

        if (iasBean == null) {
            return;
        }

        if (iasBean.isParameterEncoding()) {
            formHintField = iasBean.getAttributeValue(
                                                SunWebApp.PARAMETER_ENCODING,
                                                SunWebApp.FORM_HINT_FIELD);
            defaultCharset = iasBean.getAttributeValue(
                                                SunWebApp.PARAMETER_ENCODING,
                                                SunWebApp.DEFAULT_CHARSET);
        }

        LocaleCharsetInfo lcinfo = iasBean.getLocaleCharsetInfo();
        if (lcinfo != null) {
            if (lcinfo.getAttributeValue(
                            LocaleCharsetInfo.DEFAULT_LOCALE) != null) {
               logger.warning("webmodule.defaultLocaleDeprecated");
            }
            /*
             * <parameter-encoding> subelem of <sun-web-app> takes precedence
             * over that of <locale-charset-info>
             */
            if (lcinfo.isParameterEncoding()
                    && !iasBean.isParameterEncoding()) {
                formHintField = lcinfo.getAttributeValue(
                                        LocaleCharsetInfo.PARAMETER_ENCODING,
                                        LocaleCharsetInfo.FORM_HINT_FIELD);
                defaultCharset = lcinfo.getAttributeValue(
                                        LocaleCharsetInfo.PARAMETER_ENCODING,
                                        LocaleCharsetInfo.DEFAULT_CHARSET);
            }
            _lcMap = lcinfo.getLocaleCharsetMap();
        }
    }


    /**
     * return locale-charset-map
     */
    public LocaleCharsetMap[] getLocaleCharsetMap() {
        return _lcMap;
    }


    /**
     * Returns true if this web module specifies a locale-charset-map in its
     * sun-web.xml, false otherwise.
     *
     * @return true if this web module specifies a locale-charset-map in its
     * sun-web.xml, false otherwise
     */
    @Override
    public boolean hasLocaleToCharsetMapping() {
        LocaleCharsetMap[] locCharsetMap = getLocaleCharsetMap();
        return (locCharsetMap != null && locCharsetMap.length > 0);
    }


    /**
     * Matches the given request locales against the charsets specified in
     * the locale-charset-map of this web module's sun-web.xml, and returns
     * the first matching charset.
     *
     * @param locales Request locales
     *
     * @return First matching charset, or null if this web module does not
     * specify any locale-charset-map in its sun-web.xml, or no match was
     * found
     */
    @Override
    public String mapLocalesToCharset(Enumeration locales) {

        String encoding = null;

        LocaleCharsetMap[] locCharsetMap = getLocaleCharsetMap();
        if (locCharsetMap != null && locCharsetMap.length > 0) {
            /*
             * Check to see if there is a match between the request
             * locales (in preference order) and the locales in the
             * locale-charset-map.
             */
            boolean matchFound = false;
            while (locales.hasMoreElements() && !matchFound) {
                Locale reqLoc = (Locale) locales.nextElement();
                for (int i=0; i<locCharsetMap.length && !matchFound; i++) {
                    String language = locCharsetMap[i].getAttributeValue(
                                                LocaleCharsetMap.LOCALE);
                    if (language == null || "".equals(language)) {
                        continue;
                    }
                    String country = null;
                    int index = language.indexOf('_');
                    if (index != -1) {
                        country = language.substring(index+1);
                        language = language.substring(0, index);
                    }
                    Locale mapLoc = null;
                    if (country != null) {
                        mapLoc = new Locale(language, country);
                    } else {
                        mapLoc = new Locale(language);
                    }
                    if (mapLoc.equals(reqLoc)) {
                        /*
                         * Match found. Get the charset to which the
                         * matched locale maps.
                         */
                        encoding = locCharsetMap[i].getAttributeValue(
                                                    LocaleCharsetMap.CHARSET);
                        matchFound = true;
                    }
                }
            }
        }

        return encoding;
    }


    /**
     * Creates an ObjectInputStream that provides special deserialization
     * logic for classes that are normally not serializable (such as
     * javax.naming.Context).
     */
    @Override
    public ObjectInputStream createObjectInputStream(InputStream is)
            throws IOException {

        ObjectInputStream ois = null;

        Loader loader = getLoader();
        if (loader != null) {
            ClassLoader classLoader = loader.getClassLoader();
            if (classLoader != null) {
                try {
                    ois = javaEEObjectStreamFactory.createObjectInputStream(
                        is, true, classLoader);
                } catch (Exception e) {
                    logger.log(Level.SEVERE,
                               "Unable to create custom ObjectInputStream",
                               e);
                }
            }
        }

        if (ois == null) {
            ois = new ObjectInputStream(is);
        }

        return ois;
    }


    /**
     * Creates an ObjectOutputStream that provides special serialization
     * logic for classes that are normally not serializable (such as
     * javax.naming.Context).
     */
    @Override
    public ObjectOutputStream createObjectOutputStream(OutputStream os)
            throws IOException {

        ObjectOutputStream oos = null;

        try {
            oos = javaEEObjectStreamFactory.createObjectOutputStream(os, true);
        } catch (IOException ioe) {
            logger.log(Level.SEVERE,
                       "Unable to create custom ObjectOutputStream",
                       ioe);
            oos = new ObjectOutputStream(os);
        }

        return oos;
    }


    /**
     * Set to <code>true</code> when the default-web.xml has been read for
     * this module.
     */
    public void setXmlConfigured(boolean hasBeenXmlConfigured){
        this.hasBeenXmlConfigured = hasBeenXmlConfigured;
    }


    /**
     * Return <code>true</code> if the default=web.xml has been read for
     * this module.
     */
    public boolean hasBeenXmlConfigured(){
        return hasBeenXmlConfigured;
    }


    /**
     * Cache the result of doing findXX on this object
     * NOTE: this method MUST be used only when loading/using
     * the content of default-web.xml
     */
    public void setCachedFindOperation(Object[] cachedFinds){
        this.cachedFinds = cachedFinds;
    }


    /**
     * Return the cached result of doing findXX on this object
     * NOTE: this method MUST be used only when loading/using
     * the content of default-web.xml
     */
    public Object[] getCachedFindOperation(){
        return cachedFinds;
    }


    @Override
    public void setRealm(Realm realm) {
        if ((realm != null) && !(realm instanceof RealmAdapter)) {
            logger.log(Level.SEVERE,
                       "Realm " + realm.getClass().getName() +
                       " not an instance of " +
                       RealmAdapter.class.getName() +
                       ", and will be ignored");
        } else {
            super.setRealm(realm);
        }
    }


    /**
     * Starts this web module.
     */
    @Override
    public synchronized void start() throws LifecycleException {
        // Get interestList of ServletContainerInitializers present, if any.
        List<String> orderingList = null;
        boolean hasOthers = false;
        Map<String, String> webFragmentMap = Collections.emptyMap();
        if (webBundleDescriptor != null) {
            AbsoluteOrderingDescriptor aod =
                    webBundleDescriptor.getAbsoluteOrderingDescriptor();
            if (aod != null) {
                orderingList = aod.getOrdering();
                hasOthers = aod.hasOthers();
            }
            webFragmentMap = webBundleDescriptor.getJarNameToWebFragmentNameMap();
        }

        Iterable<ServletContainerInitializer> allInitializers =
            ServletContainerInitializerUtil.getServletContainerInitializers(
                webFragmentMap, orderingList, hasOthers,
                wmInfo.getAppClassLoader());
        setServletContainerInitializerInterestList(allInitializers);

        // Start and register Tomcat mbeans
        super.start();

        // Configure catalina listeners and valves. This can only happen
        // after this web module has been started, in order to be able to
        // load the specified listener and valve classes.
        configureValves();
        configureCatalinaProperties();
        webModuleStartedEvent();
        hasStarted = true;
    }


    /**
     * Stops this web module.
     */
    @Override
    public void stop() throws LifecycleException {
        // Unregister monitoring mbeans only if this web module was
        // successfully started, because if stop() is called during an
        // aborted start(), no monitoring mbeans will have been registered
        if (hasStarted) {
            webModuleStoppedEvent();
            hasStarted = false;
        }

        if (webBundleDescriptor != null && webBundleDescriptor.getServiceReferenceDescriptors() != null) {
            for (Object obj: webBundleDescriptor.getServiceReferenceDescriptors()) {
                //ClientPipeCloser.getInstance().cleanupClientPipe((ServiceReferenceDescriptor)obj);
            }
        }

        // Stop and unregister Tomcat mbeans
        super.stop(getWebContainer().isShutdown());
    }


    /**
     * Sets the virtual server parent of this web module, and passes it on to
     * this web module's realm adapter..
     *
     * @param container The virtual server parent
     */
    @Override
    public void setParent(Container container) {
        super.setParent(container);

        vsId = ((VirtualServer) container).getID();

        // The following assumes that the realm has been set on this WebModule
        // before the WebModule is added as a child to the virtual server on
        // which it is being deployed.
        /*RealmAdapter ra = (RealmAdapter) getRealm();
        if (ra != null) {
          1  ra.setVirtualServer(container);
        }*/
        Realm ra = getRealm();
        if (ra != null && ra instanceof RealmInitializer) {
            ((RealmInitializer) ra).setVirtualServer(container);
        }
    }

    /**
     * Indicates whether this web module contains any ad-hoc paths.
     *
     * An ad-hoc path is a servlet path that is mapped to a servlet
     * not declared in the web module's deployment descriptor.
     *
     * A web module all of whose mappings are for ad-hoc paths is called an
     * ad-hoc web module.
     *
     * @return true if this web module contains any ad-hoc paths, false
     * otherwise
     */
    @Override
    public boolean hasAdHocPaths() {
        return this.hasAdHocPaths;
    }


    /**
     * Indicates whether this web module contains any ad-hoc subtrees.
     *
     * @return true if this web module contains any ad-hoc subtrees, false
     * otherwise
     */
    public boolean hasAdHocSubtrees() {
        return this.hasAdHocSubtrees;
    }


    /*
     * Adds the given ad-hoc path and subtree, along with information about
     * the servlet that will be responsible for servicing it, to this web
     * module.
     *
     * @param path The ad-hoc path to add
     * @param subtree The ad-hoc subtree path to add
     * @param servletInfo Information about the servlet that is responsible
     * for servicing the given ad-hoc path
     */
    void addAdHocPathAndSubtree(String path,
                                String subtree,
                                AdHocServletInfo servletInfo) {

        if (path == null && subtree == null) {
            return;
        }

        Wrapper adHocWrapper = (Wrapper)
            findChild(servletInfo.getServletName());
        if (adHocWrapper == null) {
            adHocWrapper = createAdHocWrapper(servletInfo);
            addChild(adHocWrapper);
        }

        if (path != null) {
            adHocPaths.put(path, servletInfo);
            hasAdHocPaths = true;
        }

        if (subtree != null) {
            adHocSubtrees.put(subtree, servletInfo);
            hasAdHocSubtrees = true;
        }
    }


    /*
     * Adds the given ad-hoc path to servlet mappings to this web module.
     *
     * @param newPaths Mappings of ad-hoc paths to the servlets responsible
     * for servicing them
     */
    void addAdHocPaths(Map<String, AdHocServletInfo> newPaths) {

        if (newPaths == null || newPaths.isEmpty()) {
            return;
        }
        for(String adHocPath : newPaths.keySet()) {
            AdHocServletInfo servletInfo = newPaths.get(adHocPath);
            Wrapper adHocWrapper = (Wrapper)
                findChild(servletInfo.getServletName());
            if(adHocWrapper == null) {
                adHocWrapper = createAdHocWrapper(servletInfo);
                addChild(adHocWrapper);
            }
            adHocPaths.put(adHocPath, servletInfo);
        }

        hasAdHocPaths = true;
    }


    /*
     * Adds the given ad-hoc subtree path to servlet mappings to this web
     * module.
     *
     * @param newSubtrees Mappings of ad-hoc subtree paths to the servlets
     * responsible for servicing them
     */
    void addAdHocSubtrees(Map<String, AdHocServletInfo> newSubtrees) {

        if (newSubtrees == null || newSubtrees.isEmpty()) {
            return;
        }
        for(String adHocSubtree : newSubtrees.keySet()) {
            AdHocServletInfo servletInfo = newSubtrees.get(adHocSubtree);
            Wrapper adHocWrapper = (Wrapper)findChild(servletInfo.getServletName());
            if(adHocWrapper == null) {
                adHocWrapper = createAdHocWrapper(servletInfo);
                addChild(adHocWrapper);
            }
            adHocSubtrees.put(adHocSubtree, servletInfo);
        }

        hasAdHocSubtrees = true;
    }


    /*
     * Gets the ad-hoc path to servlet mappings managed by this web module.
     *
     * @return The ad-hoc path to servlet mappings managed by this web
     * module.
     */
    Map<String, AdHocServletInfo> getAdHocPaths() {
        return adHocPaths;
    }


    /*
     * Gets the ad-hoc subtree path to servlet mappings managed by this
     * web module.
     *
     * @return The ad-hoc subtree path to servlet mappings managed by
     * this web module.
     */
    Map<String, AdHocServletInfo> getAdHocSubtrees() {
        return adHocSubtrees;
    }


    /**
     * Returns the name of the ad-hoc servlet responsible for servicing the
     * given path.
     *
     * @param path The path whose associated ad-hoc servlet is needed
     *
     * @return The name of the ad-hoc servlet responsible for servicing the
     * given path, or null if the given path does not represent an ad-hoc
     * path
     */
    @Override
    public String getAdHocServletName(String path) {

        if (!hasAdHocPaths() && !hasAdHocSubtrees()) {
            return null;
        }

        AdHocServletInfo servletInfo = null;

        // Check if given path matches any of the ad-hoc paths (exact match)
        if (path == null) {
            servletInfo = adHocPaths.get("");
        } else {
            servletInfo = adHocPaths.get(path);
        }

        // Check if given path starts with any of the ad-hoc subtree paths
        if (servletInfo == null && path != null && hasAdHocSubtrees()) {
            for(String adHocSubtree : adHocSubtrees.keySet()) {
                if(path.startsWith(adHocSubtree)) {
                    servletInfo = adHocSubtrees.get(adHocSubtree);
                    break;
                }
            }
        }

        if (servletInfo != null) {
            return servletInfo.getServletName();
        } else {
            return null;
        }
    }


    /*
     * Removes the given ad-hoc path from this web module.
     *
     * @param path The ad-hoc path to remove
     */
    void removeAdHocPath(String path) {

        if (path == null) {
            return;
        }

        adHocPaths.remove(path);
        if (adHocPaths.isEmpty()) {
            this.hasAdHocPaths = false;
        }
    }


    /*
     * Removes the given ad-hoc path from this web module.
     *
     * @param subtree The ad-hoc subtree to remove
     */
    void removeAdHocSubtree(String subtree) {

        if (subtree == null) {
            return;
        }

        adHocSubtrees.remove(subtree);
        if (adHocSubtrees.isEmpty()) {
            this.hasAdHocSubtrees = false;
        }
    }


    /**
     * Adds the given valve to this web module's ad-hoc pipeline.
     *
     * @param valve The valve to add
     */
    public void addAdHocValve(GlassFishValve valve) {
        adHocPipeline.addValve(valve);
    }


    /**
     * Removes the given valve from this web module's ad-hoc pipeline.
     *
     * @param valve The valve to remove
     */
    public void removeAdHocValve(GlassFishValve valve) {
        adHocPipeline.removeValve(valve);
    }


    /**
     * Gets this web module's ad-hoc pipeline.
     *
     * @return This web module's ad-hoc pipeline
     */
    public Pipeline getAdHocPipeline() {
        return adHocPipeline;
    }


    /**
     * Sets the file encoding of all static resources of this web module.
     *
     * @param enc The file encoding of static resources of this web module
     */
    public void setFileEncoding(String enc) {
        this.fileEncoding = enc;
    }


    /**
     * Gets the file encoding of all static resources of this web module.
     *
     * @return The file encoding of static resources of this web module
     */
    public String getFileEncoding() {
        return fileEncoding;
    }


    /**
     * Sets the context attribute with the given name and value.
     *
     * @param name The context attribute name
     * @param value The context attribute value
     */
    public void setAttribute(String name, Object value) {
        context.setAttribute(name, value);
    }


    /**
     * Configures this web module with the filter mappings specified in the
     * deployment descriptor.
     *
     * @param sfm The filter mappings of this web module as specified in the
     * deployment descriptor
     */
    @SuppressWarnings({"unchecked"})
    void addFilterMap(ServletFilterMapping sfm) {

        FilterMaps filterMaps = new FilterMaps();
        filterMaps.setFilterName(sfm.getName());
        filterMaps.setDispatcherTypes(sfm.getDispatchers());

        List<String> servletNames = sfm.getServletNames();
        if (servletNames != null) {
            for(String servletName : servletNames) {
                filterMaps.addServletName(servletName);
            }
        }

        List<String> urlPatterns = sfm.getURLPatterns();
        if (urlPatterns != null) {
            for(String urlPattern : urlPatterns) {
                filterMaps.addURLPattern(urlPattern);
            }
        }

        addFilterMaps(filterMaps);
    }


    /**
     * Creates an ad-hoc servlet wrapper from the given ad-hoc servlet info.
     *
     * @param servletInfo Ad-hoc servlet info from which to generate
     * ad-hoc servlet wrapper
     *
     * @return The generated ad-hoc servlet wrapper
     */
    private Wrapper createAdHocWrapper(AdHocServletInfo servletInfo) {

        Wrapper adHocWrapper = new StandardWrapper();
        adHocWrapper.setServletClassName(
            servletInfo.getServletClass().getName());
        adHocWrapper.setName(servletInfo.getServletName());
        Map<String,String> initParams = servletInfo.getServletInitParams();
        if (initParams != null && !initParams.isEmpty()) {
            for(String paramName : initParams.keySet()) {
                adHocWrapper.addInitParameter(paramName, initParams.get(paramName));
            }
        }

        return adHocWrapper;
    }



    /**
     * Configure the <code>WebModule</code> valves.
     */
    protected void configureValves(){
        if (iasBean != null && iasBean.getValve() != null && iasBean.sizeValve() > 0) {
            com.sun.enterprise.deployment.runtime.web.Valve[] valves = iasBean.getValve();
            for (com.sun.enterprise.deployment.runtime.web.Valve valve: valves) {
                addValve(valve);
            }
        }

    }

    /**
     * Configure the <code>WebModule</code< properties.
     */
    protected void configureCatalinaProperties(){
        String propName = null;
        String propValue = null;
        if (bean != null) {
            List<Property> props = bean.getProperty();
            if (props != null) {
                for (Property prop : props) {
                    propName = prop.getName();
                    propValue = prop.getValue();
                    configureCatalinaProperties(propName,propValue);
                }
            }
        }

        if (iasBean != null && iasBean.sizeWebProperty() > 0) {
            WebProperty[] wprops = iasBean.getWebProperty();
            for(WebProperty wprop : wprops) {
                propName = wprop.getAttributeValue("name");
                propValue = wprop.getAttributeValue("value");
                configureCatalinaProperties(propName, propValue);
            }
        }
    }


    /**
     * Configure the <code>WebModule</code< properties.
     * @param propName the property name
     * @param propValue the property value
     */
    protected void configureCatalinaProperties(String propName,
                                               String propValue){
        if (propName == null || propValue == null) {
            logger.log(Level.WARNING,
                        "webcontainer.nullWebModuleProperty",
                        getName());
            return;
        }

        if (propName.startsWith("valve_")) {
            addValve(propValue);
        } else if (propName.startsWith("listener_")) {
            addCatalinaListener(propValue);
        }
    }


    /**
     * Instantiates a <tt>Valve</tt> from the given <tt>className</tt>
     * and adds it to the <tt>Pipeline</tt> of this WebModule.
     *
     * @param className the fully qualified class name of the <tt>Valve</tt>
     */
    protected void addValve(String className) {
        Object valve = loadInstance(className);
        if (valve instanceof Valve) {
            super.addValve((Valve) valve);
        } else if (valve instanceof GlassFishValve) {
            super.addValve((GlassFishValve) valve);
        } else {
            logger.log(Level.WARNING, "webmodule.valve.classNameNoValve",
                       className);
        }
    }

    /**
     * Constructs a <tt>Valve</tt> from the given <tt>valveDescriptor</tt>
     * and adds it to the <tt>Pipeline</tt> of this WebModule.
     * @param valveDescriptor the object containing the information to
     * create the valve.
     */
    protected void addValve(com.sun.enterprise.deployment.runtime.web.Valve valveDescriptor) {
        String valveName = valveDescriptor.getAttributeValue(
                com.sun.enterprise.deployment.runtime.web.Valve.NAME);
        String className = valveDescriptor.getAttributeValue(
                com.sun.enterprise.deployment.runtime.web.Valve.CLASS_NAME);
        if (valveName == null) {
            logger.log(Level.WARNING, "webmodule.valve.missingName",
                       getName());
            return;
        }
        if (className == null) {
            logger.log(Level.WARNING, "webmodule.valve.missingClassname",
                       new Object[]{valveName, getName()});
            return;
        }
        Object valve = loadInstance(className);
        if (valve == null) {
            return;
        }
        if (!(valve instanceof GlassFishValve) &&
                !(valve instanceof Valve)) {
            logger.log(Level.WARNING, "webmodule.valve.classNameNoValve",
                       className);
            return;
        }
        WebProperty[] props = valveDescriptor.getWebProperty();
        if (props != null && props.length > 0) {
            for (WebProperty property: props) {
                String propName = getSetterName(
                    property.getAttributeValue(WebProperty.NAME));
                if (propName != null && propName.length() != 0) {
                    String value = property.getAttributeValue(
                        WebProperty.VALUE);
                    try {
                        Method method = valve.getClass().getMethod(
                            propName, String.class);
                        method.invoke(valve, value);
                    } catch (NoSuchMethodException ex) {
                        String msg = rb.getString(
                            "webmodule.valve.specifiedMethodMissing");
                        msg = MessageFormat.format(msg,
                            new Object[] { propName, valveName, getName()});
                        logger.log(Level.SEVERE, msg, ex);
                    } catch (Throwable t) {
                        String msg = rb.getString(
                            "webmodule.valve.setterCausedException");
                        msg = MessageFormat.format(msg,
                            new Object[] { propName, valveName, getName()});
                        logger.log(Level.SEVERE, msg, t);
                    }
                }
                else {
                    String msg = rb.getString(
                        "webmodule.valve.missingPropertyName");
                    msg = MessageFormat.format(msg, new Object[] {valveName});
                    logger.log(Level.WARNING,
                        "webmodule.valve.missingPropertyName",
                        new Object[]{valveName, getName()});
                    return;
                }
            }
        }
        if (valve instanceof Valve) {
            super.addValve((Valve) valve);
        } else if (valve instanceof GlassFishValve) {
            super.addValve((GlassFishValve) valve);
        }
    }

    /**
     * Adds the Catalina listener with the given class name to this
     * WebModule.
     *
     * @param listenerName The fully qualified class name of the listener
     */
    protected void addCatalinaListener(String listenerName) {
        Object listener = loadInstance(listenerName);

        if ( listener == null ) return;

        if (listener instanceof ContainerListener) {
            addContainerListener((ContainerListener)listener);
        } else if (listener instanceof LifecycleListener ){
            addLifecycleListener((LifecycleListener)listener);
        } else if (listener instanceof InstanceListener){
            addInstanceListener(listenerName);
        } else {
            logger.log(Level.SEVERE, "webmodule.invalidListener",
                new Object[] {listenerName, getName()});
        }
    }

    private Object loadInstance(String className){
        try{
            Class clazz = getLoader().getClassLoader().loadClass(className);
            return clazz.newInstance();
        } catch (Throwable ex){
            String msg = rb.getString("webcontainer.unableToLoadExtension");
            msg = MessageFormat.format(msg, new Object[] { className,
                                                           getName() });
            logger.log(Level.SEVERE, msg, ex);
        }
        return null;
    }

    private String getSetterName(String propName) {
        if (propName != null) {
            if (propName.length() > 1) {
                propName = "set" + Character.toUpperCase(propName.charAt(0)) +
                        propName.substring(1);
            }
            else {
                propName = "set" + Character.toUpperCase(propName.charAt(0));
            }
        }
        return propName;
    }

    public com.sun.enterprise.config.serverbeans.WebModule getBean() {
        return bean;
    }


    public void setBean(com.sun.enterprise.config.serverbeans.WebModule bean) {
        this.bean = bean;
    }

    /**
     * Sets the bean corresponding to the j2ee-application element in
     * domain.xml representing the application (EAR file) in which this
     * web module has been embedded.
     *
     * @param appBean The application bean
     */
    void setApplicationBean(J2eeApplication appBean) {
        this.appBean = appBean;
    }

    /**
     * Gets the bean corresponding to the j2ee-application element in
     * domain.xml representing the application (EAR file) in which this
     * web module has been embedded.
     *
     * @return The application bean, or null if this web module
     * is standalone
     */
    public J2eeApplication getApplicationBean() {
        return appBean;
    }

    /**
     * Sets the WebBundleDescriptor (web.xml) for this WebModule.
     *
     * @param wbd The WebBundleDescriptor
     */
    void setWebBundleDescriptor(WebBundleDescriptor wbd) {
        this.webBundleDescriptor = wbd;
    }

    /**
     * Gets the WebBundleDesciptor (web.xml) for this WebModule.
     */
    public WebBundleDescriptor getWebBundleDescriptor() {
        return this.webBundleDescriptor;
    }

    /**
     * Gets ComponentId for Invocation.
     */
    public String getComponentId() {
        return compEnvId;
    }

    /**
     * Sets ComponentId for Invocation.
     */
    void setComponentId(String compEnvId) {
        this.compEnvId = compEnvId;
    }

    /**
     * Gets ServerContext.
     */
    public ServerContext getServerContext() {
        return serverContext;
    }

    /**
     * Sets ServerContext.
     */
    void setServerContext(ServerContext serverContext) {
        this.serverContext = serverContext;
    }

    /**
     * Sets the alternate docroots of this web module from the given
     * "alternatedocroot_" properties.
     */
    void setAlternateDocBases(List <Property> props) {

        if (props == null) {
            return;
        }

        for (Property prop : props) {
            parseAlternateDocBase(prop.getName(), prop.getValue());
        }
    }


    void parseAlternateDocBase(String propName, String propValue) {

        if (propName == null || propValue == null) {
            logger.log(Level.WARNING, "Null property name or value");
            return;
        }

        if (!propName.startsWith("alternatedocroot_")) {
            return;
        }

        /*
         * Validate the prop value
         */
        String urlPattern = null;
        String docBase = null;

        int fromIndex = propValue.indexOf(ALTERNATE_FROM);
        int dirIndex = propValue.indexOf(ALTERNATE_DOCBASE);

        if (fromIndex < 0 || dirIndex < 0) {
            logger.log(
                Level.WARNING,
                "webmodule.alternateDocBase.missingPathOrUrlPattern",
                propValue);
            return;
        }

        if (fromIndex > dirIndex) {
            urlPattern = propValue.substring(
                fromIndex + ALTERNATE_FROM.length());
            docBase = propValue.substring(
                dirIndex + ALTERNATE_DOCBASE.length(),
                fromIndex);
        } else {
            urlPattern = propValue.substring(
                fromIndex + ALTERNATE_FROM.length(),
                dirIndex);
            docBase = propValue.substring(
                dirIndex + ALTERNATE_DOCBASE.length());
        }

        urlPattern = urlPattern.trim();
        if (!validateURLPattern(urlPattern)) {
            logger.log(Level.WARNING,
                       "webmodule.alternateDocBase.illegalUrlPattern",
                       urlPattern);
            return;
        }

        docBase = docBase.trim();

        addAlternateDocBase(urlPattern, docBase);
    }


    private boolean validateURLPattern(String urlPattern) {

        if (urlPattern == null) {
            return (false);
	}

        if (urlPattern.indexOf('\n') >= 0 || urlPattern.indexOf('\r') >= 0) {
            logger.log(Level.WARNING,
                       "webmodule.alternateDocBase.crlfInUrlPattern",
                       urlPattern);
            return false;
        }

        if (urlPattern.startsWith("*.")) {
            if (urlPattern.indexOf('/') < 0) {
                return (true);
            } else {
                return (false);
            }
        }
        if ( (urlPattern.startsWith("/")) &&
	     (!urlPattern.contains("*."))) {
            return (true);
        } else {
            return (false);
        }
    }


    /**
     * Configure miscellaneous settings such as the pool size for
     * single threaded servlets, specifying a temporary directory other
     * than the default etc.
     *
     * Since the work directory is used when configuring the session manager
     * persistence settings, this method must be invoked prior to
     * <code>configureSessionSettings</code>.
     */
    void configureMiscSettings(SunWebApp bean, VirtualServer vs,
                               String contextPath) {

        /*
         * Web app inherits setting of allowLinking property from vs on which
         * it is being deployed, but may override it using allowLinking
         * property in its sun-web.xml
         */
        boolean allowLinking = vs.getAllowLinking();

        if ((bean != null) && (bean.sizeWebProperty() > 0)) {
            WebProperty[] props = bean.getWebProperty();
            for(WebProperty prop : props) {
                String name = prop.getAttributeValue("name");
                String value = prop.getAttributeValue("value");
                if(name == null || value == null) {
                    throw new IllegalArgumentException(
                        rb.getString("webcontainer.nullWebProperty"));
                }
                if("singleThreadedServletPoolSize".equalsIgnoreCase(name)) {
                    int poolSize = getSTMPoolSize();
                    try {
                        poolSize = Integer.parseInt(value);
                    } catch(NumberFormatException e) {
                        Object[] params =
                            {value, contextPath, Integer.toString(poolSize)};
                        logger.log(Level.WARNING,
                            "webcontainer.invalidServletPoolSize",
                            params);
                    }
                    if(poolSize > 0) {
                        setSTMPoolSize(poolSize);
                    }

                } else if("tempdir".equalsIgnoreCase(name)) {
                    setWorkDir(value);
                } else if("crossContextAllowed".equalsIgnoreCase(name)) {
                    boolean crossContext = Boolean.parseBoolean(value);
                    setCrossContext(crossContext);
                } else if("allowLinking".equalsIgnoreCase(name)) {
                    allowLinking = ConfigBeansUtilities.toBoolean(value);
                    // START S1AS8PE 4817642
                } else if("reuseSessionID".equalsIgnoreCase(name)) {
                    boolean reuse = ConfigBeansUtilities.toBoolean(value);
                    setReuseSessionID(reuse);
                    if(reuse) {
                        Object[] params = {
                            contextPath,
                            vs.getID()
                        };
                        logger.log(Level.WARNING,
                            "webcontainer.sessionIDsReused",
                            params);
                    }
                    // END S1AS8PE 4817642
                } else if("useResponseCTForHeaders".equalsIgnoreCase(name)) {
                    if("true".equalsIgnoreCase(value)) {
                        setResponseCTForHeaders();
                    }
                } else if("encodeCookies".equalsIgnoreCase(name)) {
                    boolean flag = ConfigBeansUtilities.toBoolean(value);
                    setEncodeCookies(flag);
                    // START RIMOD 4642650
                } else if("relativeRedirectAllowed".equalsIgnoreCase(name)) {
                    boolean relativeRedirect = ConfigBeansUtilities.toBoolean(value);
                    setAllowRelativeRedirect(relativeRedirect);
                    // END RIMOD 4642650
                } else if("fileEncoding".equalsIgnoreCase(name)) {
                    setFileEncoding(value);
                } else if("enableTldValidation".equalsIgnoreCase(name)
                    && ConfigBeansUtilities.toBoolean(value)) {
                    setTldValidation(true);
                } else if("enableTldNamespaceAware".equalsIgnoreCase(name)
                    && ConfigBeansUtilities.toBoolean(value)) {
                    setTldNamespaceAware(true);
                } else if("securePagesWithPragma".equalsIgnoreCase(name)) {
                    boolean securePagesWithPragma = ConfigBeansUtilities.toBoolean(value);
                    setSecurePagesWithPragma(securePagesWithPragma);
                } else if("useMyFaces".equalsIgnoreCase(name)) {
                    setUseMyFaces(ConfigBeansUtilities.toBoolean(value));
                } else if("useBundledJsf".equalsIgnoreCase(name)) {
                    setUseMyFaces(ConfigBeansUtilities.toBoolean(value));
                } else if(name.startsWith("alternatedocroot_")) {
                    parseAlternateDocBase(name, value);
                } else if(name.startsWith("valve_") ||
                        name.startsWith("listener_")) {
                    // do nothing; these properties are dealt with
                    // in configureCatalinaProperties()
                } else {
                    Object[] params = {name, value};
                    logger.log(Level.WARNING, "webcontainer.invalidProperty",
                        params);
                }
            }
        }

        setAllowLinking(allowLinking);
    }


    /**
     * Determines and sets the alternate deployment descriptor for
     * this web module.
     */
    void configureAlternateDD(WebBundleDescriptor wbd) {

        String altDDName =
            wbd.getModuleDescriptor().getAlternateDescriptor();
        if (altDDName == null) {
            return;
        }

        Application app = wbd.getApplication();
        if (app == null || app.isVirtual()) {
            // Alternate deployment descriptors are only supported for
            // WAR files embedded inside EAR files
            return;
        }

        DeploymentContext dc = getWebModuleConfig().getDeploymentContext();
        if (dc == null) {
            return;
        }

        altDDName = altDDName.trim();
        if (altDDName.startsWith("/")) {
            altDDName = altDDName.substring(1);
        } 
      
        String appLoc = dc.getSource().getParentArchive().getURI().getPath();
        altDDName = appLoc + altDDName;

        if (logger.isLoggable(Level.FINE)) {
            Object[] objs = {altDDName, wmInfo.getName()};
            logger.log(Level.FINE, "webcontainer.altDDName", objs);
        }

        setAltDDName(altDDName);
    }


    /*
     * Configures this web module with its web services, based on its
     * "hasWebServices" and "endpointAddresses" properties
     */
    void configureWebServices(WebBundleDescriptor wbd) {

        if (wbd.hasWebServices()) {

            setHasWebServices(true);

            // creates the list of endpoint addresses
            String[] endpointAddresses;
            WebServicesDescriptor webService = wbd.getWebServices();
            Vector<String> endpointList = new Vector<String>();
            for(WebServiceEndpoint wse : webService.getEndpoints()) {
                if(wbd.getContextRoot() != null) {
                    endpointList.add(wbd.getContextRoot() + "/" +
                        wse.getEndpointAddressUri());
                } else {
                    endpointList.add(wse.getEndpointAddressUri());
                }
            }
            endpointAddresses = new String[endpointList.size()];
            endpointList.copyInto(endpointAddresses);

            setEndpointAddresses(endpointAddresses);

        } else {
            setHasWebServices(false);
        }
    }


    /**
     * Configure the class loader for the web module based on the
     * settings in sun-web.xml's class-loader element (if any).
     */
    Loader configureLoader(SunWebApp bean) {

        com.sun.enterprise.deployment.runtime.web.ClassLoader clBean = null;

        WebappLoader loader = new V3WebappLoader(wmInfo.getAppClassLoader());

        loader.setUseMyFaces(isUseMyFaces());

        if (bean != null) {
            clBean = bean.getClassLoader();
        }
        if (clBean != null) {
            configureLoaderAttributes(loader, clBean);
            configureLoaderProperties(loader, clBean);
        } else {
            loader.setDelegate(true);
        }

        // START S1AS 6178005
        String stubPath = wmInfo.getStubPath();
        if (stubPath != null && stubPath.length() > 0) {
            if (stubPath.charAt(0) != '/') {
                stubPath = "/" + stubPath;
            }
            loader.addRepository("file:" + stubPath + File.separator);
        }
        // END S1AS 6178005

        // START PE 4985680
        /**
         * Adds the given package name to the list of packages that may
         * always be overriden, regardless of whether they belong to a
         * protected namespace
         */
        String packagesName =
                System.getProperty("com.sun.enterprise.overrideablejavaxpackages");

        if (packagesName != null) {
            List<String> overridablePackages =
                    StringUtils.parseStringList(packagesName, " ,");
            for(String overridablePackage : overridablePackages) {
                loader.addOverridablePackage(overridablePackage);
            }
        }
        // END PE 4985680

        setLoader(loader);

        return loader;
    }


    /**
     * Saves all active sessions to the given deployment context, so they
     * can be restored following a redeployment.
     *
     * @param props the deployment context properties to which to save the sessions
     */
    void saveSessions(Properties props) {
        if (props == null) {
            return;
        }

        StandardManager manager = (StandardManager) getManager();
        if (manager == null) {
            return;
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            manager.writeSessions(baos);
            props.setProperty(getObjectName(),
                                      gfEncoder.encode(baos.toByteArray()));
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Unable to save sessions for " +
                       getName(), ex);
        }
    }


    /**
     * Loads any sessions that were stored in the given deployment context
     * prior to a redeployment of this web module.
     *
     * @param deploymentProperties the deployment context properties from which to load the sessions
     */
    void loadSessions(Properties deploymentProperties) {
        if (deploymentProperties == null) {
            return;
        }

        StandardManager manager = (StandardManager) getManager();
        if (manager == null) {
            return;
        }

        String sessions = deploymentProperties.getProperty(getObjectName());
        if (sessions != null) {
            try {
                ByteArrayInputStream bais = new ByteArrayInputStream(
                    gfDecoder.decodeBuffer(sessions));
                manager.readSessions(bais);
            } catch (Exception ex) {
                logger.log(Level.WARNING, "Unable to restore sessions for " +
                           getName(), ex);

            }
            deploymentProperties.remove(getObjectName());
        }
    }


    /**
     * Loads and instantiates the listener with the specified classname.
     *
     * @param loader the classloader to use
     * @param listenerClassName the fully qualified classname to instantiate
     *
     * @return the instantiated listener
     *
     * @throws Exception if the specified classname fails to be loaded or
     * instantiated
     */
    @Override
    protected EventListener loadListener(ClassLoader loader,
                                         String listenerClassName)
            throws Exception {
        try {
            return super.loadListener(loader, listenerClassName);
        } catch (Exception e) {
            if (WS_SERVLET_CONTEXT_LISTENER.equals(listenerClassName)) {
                logger.log(Level.WARNING, "webcontainer.missingmetro", e);
            }
            throw e;
        }
    }


    /**
     * Create and configure the session manager for this web application
     * according to the persistence type specified.
     *
     * Also configure the other aspects of session management for this
     * web application according to the values specified in the session-config
     * element of sun-web.xml (and whether app is distributable)
     */
    protected void configureSessionSettings(WebBundleDescriptor wbd,
                                            WebModuleConfig wmInfo) {

        SessionConfig cfg = null;
        SessionManager smBean = null;
        SessionProperties sessionPropsBean = null;
        CookieProperties cookieBean = null;

        if (iasBean != null) {
            cfg = iasBean.getSessionConfig();
            if (cfg != null) {
                smBean = cfg.getSessionManager();
                sessionPropsBean = cfg.getSessionProperties();
                cookieBean = cfg.getCookieProperties();
            }
        }

        configureSessionManager(smBean, wbd, wmInfo);
        configureSession(sessionPropsBean, wbd);
        configureCookieProperties(cookieBean);
    }

    /**
     * Configures the given classloader with its attributes specified in
     * sun-web.xml.
     *
     * @param loader The classloader to configure
     * @param clBean The class-loader info from sun-web.xml
     */
    private void configureLoaderAttributes(
            Loader loader,
            com.sun.enterprise.deployment.runtime.web.ClassLoader clBean) {

        String value = clBean.getAttributeValue(
                com.sun.enterprise.deployment.runtime.web.ClassLoader.DELEGATE);

        /*
         * The DOL will *always* return a value: If 'delegate' has not been
         * configured in sun-web.xml, its default value will be returned,
         * which is FALSE in the case of sun-web-app_2_2-0.dtd and
         * sun-web-app_2_3-0.dtd, and TRUE in the case of
         * sun-web-app_2_4-0.dtd.
         */
        boolean delegate = ConfigBeansUtilities.toBoolean(value);
        loader.setDelegate(delegate);
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("WebModule[" + getPath() +
                        "]: Setting delegate to " + delegate);
        }

        // Get any extra paths to be added to the class path of this
        // class loader
        value = clBean.getAttributeValue(
            com.sun.enterprise.deployment.runtime.web.ClassLoader.EXTRA_CLASS_PATH);
        if (value != null) {
            // Parse the extra classpath into its ':' and ';' separated
            // components. Ignore ':' as a separator if it is preceded by
            // '\'
            String[] pathElements = value.split(";|((?<!\\\\):)");
            if (pathElements != null) {
                for (String path : pathElements) {
                    path = path.replace("\\:", ":");
                    if (logger.isLoggable(Level.FINE)) {
                        logger.fine("WebModule[" + getPath() +
                                    "]: Adding " + path +
                                    " to the classpath");
                    }

                    try {
                        URL url = new URL(path);
                        loader.addRepository(path);
                    } catch (MalformedURLException mue1) {
                        // Not a URL, interpret as file
                        File file = new File(path);
                        // START GlassFish 904
                        if (!file.isAbsolute()) {
                            // Resolve relative extra class path to the
                            // context's docroot
                            file = new File(getDocBase(), path);
                        }
                        // END GlassFish 904

                        try {
                            URL url = file.toURI().toURL();
                            loader.addRepository(url.toString());
                        } catch (MalformedURLException mue2) {
                            String msg = rb.getString(
                                "webcontainer.classpathError");
                            Object[] params = { path };
                            msg = MessageFormat.format(msg, params);
                            logger.log(Level.SEVERE, msg, mue2);
                        }
                    }
                }
            }
        }

        value = clBean.getAttributeValue(
            com.sun.enterprise.deployment.runtime.web.ClassLoader.DYNAMIC_RELOAD_INTERVAL);
        if (value != null) {
            // Log warning if dynamic-reload-interval is specified
            // in sun-web.xml since it is not supported
            logger.log(Level.WARNING, "webcontainer.dynamicReloadInterval");
        }
    }

    /**
     * Configures the given classloader with its properties specified in
     * sun-web.xml.
     *
     * @param loader The classloader to configure
     * @param clBean The class-loader info from sun-web.xml
     */
    private void configureLoaderProperties(
            Loader loader,
            com.sun.enterprise.deployment.runtime.web.ClassLoader clBean) {

        String name = null;
        String value = null;

        WebProperty[] props = clBean.getWebProperty();
        if (props == null || props.length == 0) {
            return;
        }
        for(WebProperty prop : props) {
            name = prop.getAttributeValue(WebProperty.NAME);
            value = prop.getAttributeValue(WebProperty.VALUE);
            if(name == null || value == null) {
                throw new IllegalArgumentException(
                    rb.getString("webcontainer.nullWebProperty"));
            }
            if("ignoreHiddenJarFiles".equalsIgnoreCase(name)) {
                loader.setIgnoreHiddenJarFiles(ConfigBeansUtilities.toBoolean(value));
            } else {
                Object[] params = {name, value};
                logger.log(Level.WARNING, "webcontainer.invalidProperty",
                    params);
            }
        }
    }


    /**
     * Configure the session manager according to the persistence-type
     * specified in the <session-manager> element and the related
     * settings in the <manager-properties> and <store-properties> elements
     * in sun-web.xml.
     */
    private void configureSessionManager(SessionManager smBean,
                                         WebBundleDescriptor wbd,
                                         WebModuleConfig wmInfo) {

        PersistenceType persistence = PersistenceType.MEMORY;
        String frequency = null;
        String scope = null;

        SessionManagerConfigurationHelper configHelper =
            new SessionManagerConfigurationHelper(
                this, smBean, wbd, wmInfo,
                webContainer.getServerConfigLookup());

        persistence = configHelper.getPersistenceType();
        frequency = configHelper.getPersistenceFrequency();
        scope = configHelper.getPersistenceScope();

        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("IN WebContainer>>ConfigureSessionManager before builder factory");
            logger.finest("FINAL_PERSISTENCE-TYPE IS = "
                          + persistence.getType());
            logger.finest("FINAL_PERSISTENCE_FREQUENCY IS = " + frequency);
            logger.finest("FINAL_PERSISTENCE_SCOPE IS = " + scope);
        }

        PersistenceStrategyBuilderFactory factory =
            new PersistenceStrategyBuilderFactory(
                webContainer.getServerConfigLookup());
        PersistenceStrategyBuilder builder =
            factory.createPersistenceStrategyBuilder(persistence.getType(),
                                                     frequency, scope, this);
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("PersistenceStrategyBuilder class = "
                          + builder.getClass().getName());
        }

        builder.initializePersistenceStrategy(this, smBean,
            webContainer.getServerConfigLookup());
    }

    /**
     * Configure the properties of the session, such as the timeout,
     * whether to force URL rewriting etc.
     * HERCULES:mod passing in new param wbd
     */
    private void configureSession(SessionProperties spBean,
                                  WebBundleDescriptor wbd) {

        boolean timeoutConfigured = false;
        int timeoutSeconds = 1800; // tomcat default (see StandardContext)

        setCookies(webContainer.instanceEnableCookies);

        if ((spBean != null) && (spBean.sizeWebProperty() > 0)) {
            for(WebProperty prop : spBean.getWebProperty()) {
                String name = prop.getAttributeValue(WebProperty.NAME);
                String value = prop.getAttributeValue(WebProperty.VALUE);
                if(name == null || value == null) {
                    throw new IllegalArgumentException(
                        rb.getString("webcontainer.nullWebProperty"));
                }
                if("timeoutSeconds".equalsIgnoreCase(name)) {
                    try {
                        timeoutSeconds = Integer.parseInt(value);
                        timeoutConfigured = true;
                    } catch(NumberFormatException e) {
                        // XXX need error message
                    }
                } else if("enableCookies".equalsIgnoreCase(name)) {
                    setCookies(ConfigBeansUtilities.toBoolean(value));
                } else if("enableURLRewriting".equalsIgnoreCase(name)) {
                    setEnableURLRewriting(ConfigBeansUtilities.toBoolean(value));
                } else {
                    Object[] params = {name};
                    logger.log(Level.INFO, "webcontainer.notYet", params);
                }
            }
        }

        int webXmlTimeoutSeconds = -1;
        if (wbd != null) {
            webXmlTimeoutSeconds = wbd.getSessionConfig().getSessionTimeout() * 60;
        }

        //web.xml setting has precedence if it exists
        //ignore if the value is the 30 min default
        if (webXmlTimeoutSeconds != -1 && webXmlTimeoutSeconds != 1800) {
            getManager().setMaxInactiveIntervalSeconds(webXmlTimeoutSeconds);
        } else {
            /*
             * Do not override Tomcat default, unless 'timeoutSeconds' was
             * specified in sun-web.xml
             */
            if (timeoutConfigured) {
                getManager().setMaxInactiveIntervalSeconds(timeoutSeconds);
            }
        }
    }

    /**
     * Configure the settings for the session cookie using the values
     * in sun-web.xml's cookie-property
     */
    private void configureCookieProperties(CookieProperties bean) {
        if (bean != null) {
            WebProperty[] props = bean.getWebProperty();
            if (props != null) {
                SessionCookieConfig cookieConfig = new SessionCookieConfig();
                for(WebProperty prop : props) {
                    String name = prop.getAttributeValue(WebProperty.NAME);
                    String value = prop.getAttributeValue(WebProperty.VALUE);
                    if(name == null || value == null) {
                        throw new IllegalArgumentException(
                            rb.getString("webcontainer.nullWebProperty"));
                    }
                    if("cookieName".equalsIgnoreCase(name)) {
                        cookieConfig.setName(value);
                    } else if("cookiePath".equalsIgnoreCase(name)) {
                        cookieConfig.setPath(value);
                    } else if("cookieMaxAgeSeconds".equalsIgnoreCase(name)) {
                        try {
                            cookieConfig.setMaxAge(Integer.parseInt(value));
                        } catch(NumberFormatException e) {
                            // XXX need error message
                        }
                    } else if("cookieDomain".equalsIgnoreCase(name)) {
                        cookieConfig.setDomain(value);
                    } else if("cookieComment".equalsIgnoreCase(name)) {
                        cookieConfig.setComment(value);
                    } else if("cookieSecure".equalsIgnoreCase(name)) {
                        cookieConfig.setSecure(value);
                    } else {
                        Object[] params = {name, value};
                        logger.log(Level.WARNING,
                            "webcontainer.invalidProperty",
                            params);
                    }
                }
                if (props.length > 0) {
                    if (logger.isLoggable(Level.FINE)) {
                        logger.fine("WebModule[" + getPath() + "]: "
                                    + cookieConfig);
                    }
                    setSessionCookieConfigFromSunWebXml(cookieConfig);
                }
            }
        }
    }


    /*
     * Servlet related probe events
     */

    public void servletInitializedEvent(Servlet servlet) {
        servletProbeProvider.servletInitializedEvent(servlet, _id, vsId);
    }

    public void servletDestroyedEvent(Servlet servlet) {
        servletProbeProvider.servletDestroyedEvent(servlet, _id, vsId);
    }


    /*
     * HTTP session related probe events
     */

    @Override
    public void sessionCreatedEvent(HttpSession session) {
        sessionProbeProvider.sessionCreatedEvent(session, _id, vsId);
    }

    @Override
    public void sessionDestroyedEvent(HttpSession session) {
        sessionProbeProvider.sessionDestroyedEvent(session, _id, vsId);
    }

    @Override
    public void sessionRejectedEvent(int maxSessions) {
        sessionProbeProvider.sessionRejectedEvent(maxSessions, _id, vsId);
    }

    @Override
    public void sessionExpiredEvent(HttpSession session) {
        sessionProbeProvider.sessionExpiredEvent(session, _id, vsId);
    }

    @Override
    public void sessionPersistedStartEvent(HttpSession session) {
        sessionProbeProvider.sessionPersistedStartEvent(session, _id, vsId);
    }

    @Override
    public void sessionPersistedEndEvent(HttpSession session) {
        sessionProbeProvider.sessionPersistedEndEvent(session, _id, vsId);
    }

    @Override
    public void sessionActivatedStartEvent(HttpSession session) {
        sessionProbeProvider.sessionActivatedStartEvent(session, _id, vsId);
    }

    @Override
    public void sessionActivatedEndEvent(HttpSession session) {
        sessionProbeProvider.sessionActivatedEndEvent(session, _id, vsId);
    }

    @Override
    public void sessionPassivatedStartEvent(HttpSession session) {
        sessionProbeProvider.sessionPassivatedStartEvent(session, _id, vsId);
    }

    @Override
    public void sessionPassivatedEndEvent(HttpSession session) {
        sessionProbeProvider.sessionPassivatedEndEvent(session, _id, vsId);
    }


    /*
     * Web module lifecycle related probe events
     */

    public void webModuleStartedEvent() {
        webModuleProbeProvider.webModuleStartedEvent(_id, vsId);
    }

    public void webModuleStoppedEvent() {
        webModuleProbeProvider.webModuleStoppedEvent(_id, vsId);
    }

}


class V3WebappLoader extends WebappLoader {

    final ClassLoader cl;

    V3WebappLoader(ClassLoader cl) {
        this.cl = cl;
    }

    @Override
    protected ClassLoader createClassLoader() throws Exception {
        return cl;
    }

    /**
     * Stops the nested classloader
     */
    @Override
    public void stopNestedClassLoader() {
        // Do nothing. The nested (Webapp)ClassLoader is stopped in
        // WebApplication.stop()
    }

}
