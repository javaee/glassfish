/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 *
 *
 * This file incorporates work covered by the following copyright and
 * permission notice:
 *
 * Copyright 2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */




package org.apache.catalina;


import javax.servlet.ServletContext;

import com.sun.grizzly.util.http.mapper.Mapper;

import org.apache.catalina.deploy.ApplicationParameter;
import org.apache.catalina.deploy.ContextEjb;
import org.apache.catalina.deploy.ContextEnvironment;
import org.apache.catalina.deploy.ContextLocalEjb;
import org.apache.catalina.deploy.ContextResource;
import org.apache.catalina.deploy.ContextResourceLink;
import org.apache.catalina.deploy.ErrorPage;
import org.apache.catalina.deploy.FilterDef;
import org.apache.catalina.deploy.FilterMap;
import org.apache.catalina.deploy.LoginConfig;
import org.apache.catalina.deploy.NamingResources;
import org.apache.catalina.deploy.SecurityConstraint;
import org.apache.catalina.util.CharsetMapper;


/**
 * A <b>Context</b> is a Container that represents a servlet context, and
 * therefore an individual web application, in the Catalina servlet engine.
 * It is therefore useful in almost every deployment of Catalina (even if a
 * Connector attached to a web server (such as Apache) uses the web server's
 * facilities to identify the appropriate Wrapper to handle this request.
 * It also provides a convenient mechanism to use Interceptors that see
 * every request processed by this particular web application.
 * <p>
 * The parent Container attached to a Context is generally a Host, but may
 * be some other implementation, or may be omitted if it is not necessary.
 * <p>
 * The child containers attached to a Context are generally implementations
 * of Wrapper (representing individual servlet definitions).
 * <p>
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.4 $ $Date: 2007/05/05 05:31:51 $
 */

public interface Context extends Container {


    // ----------------------------------------------------- Manifest Constants


    /**
     * The LifecycleEvent type sent when a context is reloaded.
     */
    public static final String RELOAD_EVENT = "reload";


    // ------------------------------------------------------------- Properties


    /**
     * Return the set of initialized application event listener objects,
     * in the order they were specified in the web application deployment
     * descriptor, for this application.
     *
     * @exception IllegalStateException if this method is called before
     *  this application has started, or after it has been stopped
     */
    public Object[] getApplicationEventListeners();


    /**
     * Store the set of initialized application event listener objects,
     * in the order they were specified in the web application deployment
     * descriptor, for this application.
     *
     * @param listeners The set of instantiated listener objects.
     */
    public void setApplicationEventListeners(Object listeners[]);


    /**
     * Return the set of initialized application lifecycle listener objects,
     * in the order they were specified in the web application deployment
     * descriptor, for this application.
     *
     * @exception IllegalStateException if this method is called before
     *  this application has started, or after it has been stopped
     */
    public Object[] getApplicationLifecycleListeners();


    /**
     * Store the set of initialized application lifecycle listener objects,
     * in the order they were specified in the web application deployment
     * descriptor, for this application.
     *
     * @param listeners The set of instantiated listener objects.
     */
    public void setApplicationLifecycleListeners(Object listeners[]);


    /**
     * Return the application available flag for this Context.
     */
    public boolean getAvailable();


    /**
     * Set the application available flag for this Context.
     *
     * @param available The new application available flag
     */
    public void setAvailable(boolean available);


    /**
     * Return the Locale to character set mapper for this Context.
     */
    public CharsetMapper getCharsetMapper();


    /**
     * Set the Locale to character set mapper for this Context.
     *
     * @param mapper The new mapper
     */
    public void setCharsetMapper(CharsetMapper mapper);


    /**
     * Return the path to a file to save this Context information.
     */
    public String getConfigFile();


    /**
     * Set the path to a file to save this Context information.
     *
     * @param configFile The path to a file to save this Context information.
     */
    public void setConfigFile(String configFile);


    /**
     * Return the "correctly configured" flag for this Context.
     */
    public boolean getConfigured();


    /**
     * Set the "correctly configured" flag for this Context.  This can be
     * set to false by startup listeners that detect a fatal configuration
     * error to avoid the application from being made available.
     *
     * @param configured The new correctly configured flag
     */
    public void setConfigured(boolean configured);


    /**
     * Return the "use cookies for session ids" flag.
     */
    public boolean getCookies();


    /**
     * Set the "use cookies for session ids" flag.
     *
     * @param cookies The new flag
     */
    public void setCookies(boolean cookies);


    /**
     * Return the "allow crossing servlet contexts" flag.
     */
    public boolean getCrossContext();


    
    /**
     * Return the alternate Deployment Descriptor name.
     */
    public String getAltDDName();
    
    
    /**
     * Set an alternate Deployment Descriptor name.
     */
    public void setAltDDName(String altDDName) ;
    
    
    /**
     * Set the "allow crossing servlet contexts" flag.
     *
     * @param crossContext The new cross contexts flag
     */
    public void setCrossContext(boolean crossContext);


    /**
     * Return the display name of this web application.
     */
    public String getDisplayName();


    /**
     * Set the display name of this web application.
     *
     * @param displayName The new display name
     */
    public void setDisplayName(String displayName);


    /**
     * Return the distributable flag for this web application.
     */
    public boolean getDistributable();


    /**
     * Set the distributable flag for this web application.
     *
     * @param distributable The new distributable flag
     */
    public void setDistributable(boolean distributable);


    /**
     * Return the document root for this Context.  This can be an absolute
     * pathname, a relative pathname, or a URL.
     */
    public String getDocBase();


    /**
     * Set the document root for this Context.  This can be an absolute
     * pathname, a relative pathname, or a URL.
     *
     * @param docBase The new document root
     */
    public void setDocBase(String docBase);


    /**
     * Return the URL encoded context path, using UTF-8.
     */
    public String getEncodedPath();


    /**
     * Return the login configuration descriptor for this web application.
     */
    public LoginConfig getLoginConfig();


    /**
     * Set the login configuration descriptor for this web application.
     *
     * @param config The new login configuration
     */
    public void setLoginConfig(LoginConfig config);


    /**
     * Get the request dispatcher mapper.
     */
    public Mapper getMapper();


    /**
     * Return the naming resources associated with this web application.
     */
    public NamingResources getNamingResources();


    /**
     * Set the naming resources for this web application.
     *
     * @param namingResources The new naming resources
     */
    public void setNamingResources(NamingResources namingResources);


    /**
     * Return the context path for this web application.
     */
    public String getPath();


    /**
     * Set the context path for this web application.
     *
     * @param path The new context path
     */
    public void setPath(String path);


    /**
     * Return the public identifier of the deployment descriptor DTD that is
     * currently being parsed.
     */
    public String getPublicId();


    /**
     * Set the public identifier of the deployment descriptor DTD that is
     * currently being parsed.
     *
     * @param publicId The public identifier
     */
    public void setPublicId(String publicId);


    /**
     * Return the reloadable flag for this web application.
     */
    public boolean getReloadable();


    /**
     * Set the reloadable flag for this web application.
     *
     * @param reloadable The new reloadable flag
     */
    public void setReloadable(boolean reloadable);


    /**
     * Return the override flag for this web application.
     */
    public boolean getOverride();


    /**
     * Set the override flag for this web application.
     *
     * @param override The new override flag
     */
    public void setOverride(boolean override);


    /**
     * Return the privileged flag for this web application.
     */
    public boolean getPrivileged();


    /**
     * Set the privileged flag for this web application.
     *
     * @param privileged The new privileged flag
     */
    public void setPrivileged(boolean privileged);


    /**
     * Return the servlet context for which this Context is a facade.
     */
    public ServletContext getServletContext();


    /**
     * Return the default session timeout (in minutes) for this
     * web application.
     */
    public int getSessionTimeout();


    /**
     * Set the default session timeout (in minutes) for this
     * web application.
     *
     * @param timeout The new default session timeout
     */
    public void setSessionTimeout(int timeout);


    /**
     * Return the Java class name of the Wrapper implementation used
     * for servlets registered in this Context.
     */
    public String getWrapperClass();


    /**
     * Set the Java class name of the Wrapper implementation used
     * for servlets registered in this Context.
     *
     * @param wrapperClass The new wrapper class
     */
    public void setWrapperClass(String wrapperClass);


    // START IASRI 4823322
    /**
     * Get Auditors associated with this context, if any.
     *
     * @return array of Auditor objects, or null
     *
     */
    public Auditor[] getAuditors();


    /**
     * Set the Auditors associated with this context.
     *
     * @param auditor array of Auditor objects
     *
     */
    public void setAuditors(Auditor[] auditor);
    // END IASRI 4823322


    // --------------------------------------------------------- Public Methods


    /**
     * Add a new Listener class name to the set of Listeners
     * configured for this application.
     *
     * @param listener Java class name of a listener class
     */
    public void addApplicationListener(String listener);


    /**
     * Add a new application parameter for this application.
     *
     * @param parameter The new application parameter
     */
    public void addApplicationParameter(ApplicationParameter parameter);


    /**
     * Add a security constraint to the set for this web application.
     */
    public void addConstraint(SecurityConstraint constraint);


    /**
     * Add an EJB resource reference for this web application.
     *
     * @param ejb New EJB resource reference
     */
    public void addEjb(ContextEjb ejb);


    /**
     * Add an environment entry for this web application.
     *
     * @param environment New environment entry
     */
    public void addEnvironment(ContextEnvironment environment);


    /**
     * Add an error page for the specified error or Java exception.
     *
     * @param errorPage The error page definition to be added
     */
    public void addErrorPage(ErrorPage errorPage);


    /**
     * Add a filter definition to this Context.
     *
     * @param filterDef The filter definition to be added
     */
    public void addFilterDef(FilterDef filterDef);


    /**
     * Add a filter mapping to this Context.
     *
     * @param filterMap The filter mapping to be added
     */
    public void addFilterMap(FilterMap filterMap);


    /**
     * Add the classname of an InstanceListener to be added to each
     * Wrapper appended to this Context.
     *
     * @param listener Java class name of an InstanceListener class
     */
    public void addInstanceListener(String listener);


    /**
     * Add the given URL pattern as a jsp-property-group.  This maps
     * resources that match the given pattern so they will be passed
     * to the JSP container.  Though there are other elements in the
     * property group, we only care about the URL pattern here.  The
     * JSP container will parse the rest.
     *
     * @param pattern URL pattern to be mapped 
     */
    public void addJspMapping(String pattern);


    /**
     * Add a Locale Encoding Mapping (see Sec 5.4 of Servlet spec 2.4)
     *
     * @param locale locale to map an encoding for
     * @param encoding encoding to be used for a give locale
     */
    public void addLocaleEncodingMappingParameter(String locale, String encoding);


    /**
     * Add a local EJB resource reference for this web application.
     *
     * @param ejb New local EJB resource reference
     */
    public void addLocalEjb(ContextLocalEjb ejb);


    /**
     * Add a new MIME mapping, replacing any existing mapping for
     * the specified extension.
     *
     * @param extension Filename extension being mapped
     * @param mimeType Corresponding MIME type
     */
    public void addMimeMapping(String extension, String mimeType);


    /**
     * Add a new context initialization parameter, replacing any existing
     * value for the specified name.
     *
     * @param name Name of the new parameter
     * @param value Value of the new  parameter
     */
    public void addParameter(String name, String value);


    /**
     * Add a resource reference for this web application.
     *
     * @param resource New resource reference
     */
    public void addResource(ContextResource resource);


    /**
     * Add a resource environment reference for this web application.
     *
     * @param name The resource environment reference name
     * @param type The resource environment reference type
     */
    public void addResourceEnvRef(String name, String type);


    /**
     * Add a resource link for this web application.
     *
     * @param resource New resource link
     */
    public void addResourceLink(ContextResourceLink resourceLink);


    /**
     * Add a security role reference for this web application.
     *
     * @param role Security role used in the application
     * @param link Actual security role to check for
     */
    public void addRoleMapping(String role, String link);


    /**
     * Add a new security role for this web application.
     *
     * @param role New security role
     */
    public void addSecurityRole(String role);


    /**
     * Add a new servlet mapping, replacing any existing mapping for
     * the specified pattern.
     *
     * @param pattern URL pattern to be mapped
     * @param name Name of the corresponding servlet to execute
     */
    public void addServletMapping(String pattern, String name);


    /**
     * Add a JSP tag library for the specified URI.
     *
     * @param uri URI, relative to the web.xml file, of this tag library
     * @param location Location of the tag library descriptor
     */
    public void addTaglib(String uri, String location);


    /**
     * Add a resource which will be watched for reloading by the host auto
     * deployer. Note: this will not be used in embedded mode.
     * 
     * @param name Path to the resource, relative to docBase
     */
    public void addWatchedResource(String name);


    /**
     * Add a new welcome file to the set recognized by this Context.
     *
     * @param name New welcome file name
     */
    public void addWelcomeFile(String name);


    /**
     * Add the classname of a LifecycleListener to be added to each
     * Wrapper appended to this Context.
     *
     * @param listener Java class name of a LifecycleListener class
     */
    public void addWrapperLifecycle(String listener);


    /**
     * Add the classname of a ContainerListener to be added to each
     * Wrapper appended to this Context.
     *
     * @param listener Java class name of a ContainerListener class
     */
    public void addWrapperListener(String listener);


    /**
     * Factory method to create and return a new Wrapper instance, of
     * the Java implementation class appropriate for this Context
     * implementation.  The constructor of the instantiated Wrapper
     * will have been called, but no properties will have been set.
     */
    public Wrapper createWrapper();


    /**
     * Return the set of application listener class names configured
     * for this application.
     */
    public String[] findApplicationListeners();


    /**
     * Return the set of application parameters for this application.
     */
    public ApplicationParameter[] findApplicationParameters();


    /**
     * Return the set of security constraints for this web application.
     * If there are none, a zero-length array is returned.
     */
    public SecurityConstraint[] findConstraints();


    /**
     * Return the EJB resource reference with the specified name, if any;
     * otherwise, return <code>null</code>.
     *
     * @param name Name of the desired EJB resource reference
     */
    public ContextEjb findEjb(String name);


    /**
     * Return the defined EJB resource references for this application.
     * If there are none, a zero-length array is returned.
     */
    public ContextEjb[] findEjbs();


    /**
     * Return the environment entry with the specified name, if any;
     * otherwise, return <code>null</code>.
     *
     * @param name Name of the desired environment entry
     */
    public ContextEnvironment findEnvironment(String name);


    /**
     * Return the set of defined environment entries for this web
     * application.  If none have been defined, a zero-length array
     * is returned.
     */
    public ContextEnvironment[] findEnvironments();


    /**
     * Return the error page entry for the specified HTTP error code,
     * if any; otherwise return <code>null</code>.
     *
     * @param errorCode Error code to look up
     */
    public ErrorPage findErrorPage(int errorCode);


    /**
     * Return the error page entry for the specified Java exception type,
     * if any; otherwise return <code>null</code>.
     *
     * @param exceptionType Exception type to look up
     */
    public ErrorPage findErrorPage(String exceptionType);



    /**
     * Return the set of defined error pages for all specified error codes
     * and exception types.
     */
    public ErrorPage[] findErrorPages();


    /**
     * Return the filter definition for the specified filter name, if any;
     * otherwise return <code>null</code>.
     *
     * @param filterName Filter name to look up
     */
    public FilterDef findFilterDef(String filterName);


    /**
     * Return the set of defined filters for this Context.
     */
    public FilterDef[] findFilterDefs();


    /**
     * Return the set of filter mappings for this Context.
     */
    public FilterMap[] findFilterMaps();


    /**
     * Return the set of InstanceListener classes that will be added to
     * newly created Wrappers automatically.
     */
    public String[] findInstanceListeners();


    /**
     * Return the local EJB resource reference with the specified name, if any;
     * otherwise, return <code>null</code>.
     *
     * @param name Name of the desired EJB resource reference
     */
    public ContextLocalEjb findLocalEjb(String name);


    /**
     * Return the defined local EJB resource references for this application.
     * If there are none, a zero-length array is returned.
     */
    public ContextLocalEjb[] findLocalEjbs();


    /**
     * Return the MIME type to which the specified extension is mapped,
     * if any; otherwise return <code>null</code>.
     *
     * @param extension Extension to map to a MIME type
     */
    public String findMimeMapping(String extension);


    /**
     * Return the extensions for which MIME mappings are defined.  If there
     * are none, a zero-length array is returned.
     */
    public String[] findMimeMappings();


    /**
     * Return the value for the specified context initialization
     * parameter name, if any; otherwise return <code>null</code>.
     *
     * @param name Name of the parameter to return
     */
    public String findParameter(String name);


    /**
     * Return the names of all defined context initialization parameters
     * for this Context.  If no parameters are defined, a zero-length
     * array is returned.
     */
    public String[] findParameters();


    /**
     * Return the resource reference with the specified name, if any;
     * otherwise return <code>null</code>.
     *
     * @param name Name of the desired resource reference
     */
    public ContextResource findResource(String name);


    /**
     * Return the resource environment reference type for the specified
     * name, if any; otherwise return <code>null</code>.
     *
     * @param name Name of the desired resource environment reference
     */
    public String findResourceEnvRef(String name);


    /**
     * Return the set of resource environment reference names for this
     * web application.  If none have been specified, a zero-length
     * array is returned.
     */
    public String[] findResourceEnvRefs();


    /**
     * Return the resource link with the specified name, if any;
     * otherwise return <code>null</code>.
     *
     * @param name Name of the desired resource link
     */
    public ContextResourceLink findResourceLink(String name);


    /**
     * Return the defined resource links for this application.  If
     * none have been defined, a zero-length array is returned.
     */
    public ContextResourceLink[] findResourceLinks();


    /**
     * Return the defined resource references for this application.  If
     * none have been defined, a zero-length array is returned.
     */
    public ContextResource[] findResources();


    /**
     * For the given security role (as used by an application), return the
     * corresponding role name (as defined by the underlying Realm) if there
     * is one.  Otherwise, return the specified role unchanged.
     *
     * @param role Security role to map
     */
    public String findRoleMapping(String role);


    /**
     * Return <code>true</code> if the specified security role is defined
     * for this application; otherwise return <code>false</code>.
     *
     * @param role Security role to verify
     */
    public boolean findSecurityRole(String role);


    /**
     * Return the security roles defined for this application.  If none
     * have been defined, a zero-length array is returned.
     */
    public String[] findSecurityRoles();


    /**
     * Return the servlet name mapped by the specified pattern (if any);
     * otherwise return <code>null</code>.
     *
     * @param pattern Pattern for which a mapping is requested
     */
    public String findServletMapping(String pattern);


    /**
     * Return the patterns of all defined servlet mappings for this
     * Context.  If no mappings are defined, a zero-length array is returned.
     */
    public String[] findServletMappings();


    /**
     * Return the context-relative URI of the error page for the specified
     * HTTP status code, if any; otherwise return <code>null</code>.
     *
     * @param status HTTP status code to look up
     */
    public String findStatusPage(int status);


    /**
     * Return the set of HTTP status codes for which error pages have
     * been specified.  If none are specified, a zero-length array
     * is returned.
     */
    public int[] findStatusPages();


    /**
     * Return the tag library descriptor location for the specified taglib
     * URI, if any; otherwise, return <code>null</code>.
     *
     * @param uri URI, relative to the web.xml file
     */
    public String findTaglib(String uri);


    /**
     * Return the URIs of all tag libraries for which a tag library
     * descriptor location has been specified.  If none are specified,
     * a zero-length array is returned.
     */
    public String[] findTaglibs();


    /**
     * Return the set of watched resources for this Context. If none are 
     * defined, a zero length array will be returned.
     */
    public String[] findWatchedResources();


    /**
     * Return <code>true</code> if the specified welcome file is defined
     * for this Context; otherwise return <code>false</code>.
     *
     * @param name Welcome file to verify
     */
    public boolean findWelcomeFile(String name);


    /**
     * Return the set of welcome files defined for this Context.  If none are
     * defined, a zero-length array is returned.
     */
    public String[] findWelcomeFiles();


    /**
     * Return the set of LifecycleListener classes that will be added to
     * newly created Wrappers automatically.
     */
    public String[] findWrapperLifecycles();


    /**
     * Return the set of ContainerListener classes that will be added to
     * newly created Wrappers automatically.
     */
    public String[] findWrapperListeners();


    /**
     * Reload this web application, if reloading is supported.
     *
     * @exception IllegalStateException if the <code>reloadable</code>
     *  property is set to <code>false</code>.
     */
    public void reload();


    /**
     * Remove the specified application listener class from the set of
     * listeners for this application.
     *
     * @param listener Java class name of the listener to be removed
     */
    public void removeApplicationListener(String listener);


    /**
     * Remove the application parameter with the specified name from
     * the set for this application.
     *
     * @param name Name of the application parameter to remove
     */
    public void removeApplicationParameter(String name);


    /**
     * Remove the specified security constraint from this web application.
     *
     * @param constraint Constraint to be removed
     */
    public void removeConstraint(SecurityConstraint constraint);


    /**
     * Remove any EJB resource reference with the specified name.
     *
     * @param name Name of the EJB resource reference to remove
     */
    public void removeEjb(String name);


    /**
     * Remove any environment entry with the specified name.
     *
     * @param name Name of the environment entry to remove
     */
    public void removeEnvironment(String name);


    /**
     * Remove the error page for the specified error code or
     * Java language exception, if it exists; otherwise, no action is taken.
     *
     * @param errorPage The error page definition to be removed
     */
    public void removeErrorPage(ErrorPage errorPage);


    /**
     * Remove the specified filter definition from this Context, if it exists;
     * otherwise, no action is taken.
     *
     * @param filterDef Filter definition to be removed
     */
    public void removeFilterDef(FilterDef filterDef);


    /**
     * Remove a filter mapping from this Context.
     *
     * @param filterMap The filter mapping to be removed
     */
    public void removeFilterMap(FilterMap filterMap);


    /**
     * Remove a class name from the set of InstanceListener classes that
     * will be added to newly created Wrappers.
     *
     * @param listener Class name of an InstanceListener class to be removed
     */
    public void removeInstanceListener(String listener);


    /**
     * Remove any local EJB resource reference with the specified name.
     *
     * @param name Name of the EJB resource reference to remove
     */
    public void removeLocalEjb(String name);


    /**
     * Remove the MIME mapping for the specified extension, if it exists;
     * otherwise, no action is taken.
     *
     * @param extension Extension to remove the mapping for
     */
    public void removeMimeMapping(String extension);


    /**
     * Remove the context initialization parameter with the specified
     * name, if it exists; otherwise, no action is taken.
     *
     * @param name Name of the parameter to remove
     */
    public void removeParameter(String name);


    /**
     * Remove any resource reference with the specified name.
     *
     * @param name Name of the resource reference to remove
     */
    public void removeResource(String name);


    /**
     * Remove any resource environment reference with the specified name.
     *
     * @param name Name of the resource environment reference to remove
     */
    public void removeResourceEnvRef(String name);


    /**
     * Remove any resource link with the specified name.
     *
     * @param name Name of the resource link to remove
     */
    public void removeResourceLink(String name);


    /**
     * Remove any security role reference for the specified name
     *
     * @param role Security role (as used in the application) to remove
     */
    public void removeRoleMapping(String role);


    /**
     * Remove any security role with the specified name.
     *
     * @param role Security role to remove
     */
    public void removeSecurityRole(String role);


    /**
     * Remove any servlet mapping for the specified pattern, if it exists;
     * otherwise, no action is taken.
     *
     * @param pattern URL pattern of the mapping to remove
     */
    public void removeServletMapping(String pattern);


    /**
     * Remove the tag library location forthe specified tag library URI.
     *
     * @param uri URI, relative to the web.xml file
     */
    public void removeTaglib(String uri);


    /**
     * Remove the specified watched resource name from the list associated
     * with this Context.
     * 
     * @param name Name of the watched resource to be removed
     */
    public void removeWatchedResource(String name);


    /**
     * Remove the specified welcome file name from the list recognized
     * by this Context.
     *
     * @param name Name of the welcome file to be removed
     */
    public void removeWelcomeFile(String name);


    /**
     * Remove a class name from the set of LifecycleListener classes that
     * will be added to newly created Wrappers.
     *
     * @param listener Class name of a LifecycleListener class to be removed
     */
    public void removeWrapperLifecycle(String listener);


    /**
     * Remove a class name from the set of ContainerListener classes that
     * will be added to newly created Wrappers.
     *
     * @param listener Class name of a ContainerListener class to be removed
     */
    public void removeWrapperListener(String listener);


    // START S1AS8PE 4817642
    /**
     * Return the "reuse session IDs when creating sessions" flag
     */
    public boolean getReuseSessionID();

    /**
     * Set the "reuse session IDs when creating sessions" flag
     *
     * @param reuse The new value for the flag
     */
    public void setReuseSessionID(boolean reuse);
    // END S1AS8PE 4817642

    
    // START RIMOD 4642650
    /**
     * Return whether this context allows sendRedirect() to redirect
     * to a relative URL.
     *
     * The default value for this property is 'false'.
     */
    public boolean getAllowRelativeRedirect();

    
    /**
     * Set whether this context allows sendRedirect() to redirect
     * to a relative URL.
     *
     * @param allowRelativeURLs The new value for this property. The
     *                          default value for this flag is 'false'.
     */
    public void setAllowRelativeRedirect(boolean allowRelativeURLs);


    // END RIMOD 4642650
       /**
     * Get the server.xml <context> attribute's xmlNamespaceAware.
     * @return true if namespace awarenes is enabled.
     *
     */
    public boolean getXmlNamespaceAware();


    /**
     * Get the server.xml <context> attribute's xmlValidation.
     * @return true if validation is enabled.
     *
     */
    public boolean getXmlValidation();


    /**
     * Set the validation feature of the XML parser used when
     * parsing xml instances.
     * @param xmlValidation true to enable xml instance validation
     */
    public void setXmlValidation(boolean xmlValidation);


   /**
     * Set the namespace aware feature of the XML parser used when
     * parsing xml instances.
     * @param xmlNamespaceAware true to enable namespace awareness
     */
    public void setXmlNamespaceAware(boolean xmlNamespaceAware);
    /**
     * Get the server.xml <context> attribute's xmlValidation.
     * @return true if validation is enabled.
     */
     

    /**
     * Set the validation feature of the XML parser used when
     * parsing tlds files. 
     * @param tldXmlValidation true to enable xml instance validation
     */
    public void setTldValidation(boolean tldValidation);


    /**
     * Get the server.xml <context> attribute's webXmlValidation.
     * @return true if validation is enabled.
     *
     */
    public boolean getTldValidation();


    /**
     * Get the server.xml <host> attribute's xmlNamespaceAware.
     * @return true if namespace awarenes is enabled.
     */
    public boolean getTldNamespaceAware();


    /**
     * Set the namespace aware feature of the XML parser used when
     * parsing xml instances.
     * @param xmlNamespaceAware true to enable namespace awareness
     */
    public void setTldNamespaceAware(boolean tldNamespaceAware);


    // START SJSAS 8.1 5049111    
    /**
     * Return <code>true</code> if this context contains the JSF servlet.
     */
    public boolean isJsfApplication();
    // END SJSAS 8.1 5049111


    // START SJSAS 6253524
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
    public boolean hasAdHocPaths();

    /**
     * Returns the name of the ad-hoc servlet responsible for servicing the
     * given path.
     *
     * @param path The path to service
     *
     * @return The name of the ad-hoc servlet responsible for servicing the
     * given path, or null if the given path is not an ad-hoc path
     */
    public String getAdHocServletName(String path);
    // END SJSAS 6253524

    /**
     * Indicates whether the Pragma and Cache-Control headers will be set
     * to "No-cache" if proxy caching has been disabled.
     *
     * @return true if Pragma and Cache-Control headers will be set to
     * "No-cache" if proxy caching has been disabled; false otherwise.
     */
    public boolean isSecurePagesWithPragma();

    /**
     * Sets the securePagesWithPragma property of this Context.
     *
     * Setting this property to true will result in Pragma and Cache-Control
     * headers with a value of "No-cache" if proxy caching has been disabled.
     *
     * Setting this property to false will not add any Pragma header,
     * but will set the Cache-Control header to "private".
     *
     * @param securePagesWithPragma true if Pragma and Cache-Control headers
     * are to be set to "No-cache" if proxy caching has been disabled, false
     * otherwise
     */
    public void setSecurePagesWithPragma(boolean securePagesWithPragma);
}
