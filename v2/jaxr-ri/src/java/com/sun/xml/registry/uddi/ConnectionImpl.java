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
* Copyright 2007 Sun Microsystems, Inc. All rights reserved.
*/


package com.sun.xml.registry.uddi;

import javax.xml.registry.*;
import javax.xml.registry.infomodel.*;

import java.util.*;
import java.io.*;
import java.net.*;

import java.security.Security;
import java.security.AccessController;
import java.security.PrivilegedAction;
import javax.security.auth.*;
import javax.security.auth.login.*;

import com.sun.xml.registry.common.util.*;

import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Class Declaration for ConnectionImpl
 * @see
 * @author Kathy Walsh
 */
public class ConnectionImpl implements Connection {
    
    static final String QUERY_URL_PROP = "javax.xml.registry.queryManagerURL";
    static final String LIFE_CYCLE_URL_PROP = "javax.xml.registry.lifeCycleManagerURL";
    static final String SEMANTIC_EQ_PROP = "javax.xml.registry.semanticEquivalences";
    static final String AUTH_METHOD_PROP = "javax.xml.registry.security.authenticationMethod";
    static final String MAX_ROWS = "javax.xml.registry.uddi.maxRows";
    static final String POSTAL_SCHEME_PROP = "javax.xml.registry.postalAddressScheme";
    static final String HTTP_PROXY_HOST = "com.sun.xml.registry.http.proxyHost";
    static final String HTTP_PROXY_PORT = "com.sun.xml.registry.http.proxyPort";
    static final String HTTPS_PROXY_HOST = "com.sun.xml.registry.https.proxyHost";
    static final String HTTPS_PROXY_PORT = "com.sun.xml.registry.https.proxyPort";
    static final String PROXY_USER_NAME = "com.sun.xml.registry.http.proxyUserName";
    static final String PROXY_PASSWORD = "com.sun.xml.registry.http.proxyPassword";
    static final String USE_SOAP = "com.sun.xml.registry.useSOAP";
    static final String USE_CACHE = "com.sun.xml.registry.useCache";
    static final String AUTH_TOKEN_TIMEOUT = "com.sun.xml.registry.authTokenTimeout";
    static final long DEFAULT_TIMEOUT = 15000; 

    // used in JAXRConceptsManager
    public static final String USER_DEF_TAXONOMIES =
        "com.sun.xml.registry.userTaxonomyFilenames";
    String userDefinedTaxonomy;

    Logger logger = (Logger)
	AccessController.doPrivileged(new PrivilegedAction() {
	    public Object run() {
		return Logger.getLogger(com.sun.xml.registry.common.util.Utility.LOGGING_DOMAIN + ".uddi");
	    }
	});
    
    String connectionId;
    char[] authToken;
    
    private LoginContext lc;
    private Subject subject;
    
    // standard connection properties
    private String queryManagerURLString;
    private String lifeCycleManagerURLString;
    private String semanticEquivalences;
    private String authenticationMethod;
    private String maxRows;

    // implementation specific properties
    private String httpProxyHost;
    private String httpProxyPort;
    private String httpsProxyHost;
    private String httpsProxyPort;
    private String proxyUserName;
    private String proxyPassword;
    private String defaultPostalAddressScheme;
    
    private boolean useSOAP = false;
    private boolean useCache = true;
    private long timeout;
    private long timestamp;

    private RegistryServiceImpl service;
    private HashMap equivalences;
    private boolean synchronous = true;
    private Locale locale;
    boolean isClosed = false;
    //needs to be modified -TBD
    Set privateCredentials;
    
    ConnectionImpl(){
        connectionId = Utility.generateUUID();
    }
    
    ConnectionImpl(Properties properties) throws JAXRException, InvalidRequestException {
	     if (logger.isLoggable(Level.FINEST)) {
            logger.finest("JAXR implementation version: " + VersionUtil.getJAXRCompleteVersion());                   
         }
        queryManagerURLString =
            (String) properties.get(QUERY_URL_PROP);
        lifeCycleManagerURLString =
            (String) properties.get(LIFE_CYCLE_URL_PROP);
        
        if(queryManagerURLString == null) {
            throw new InvalidRequestException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("ConnectionImpl:Missing_connection_property_") +
                QUERY_URL_PROP);
        }
        if(lifeCycleManagerURLString == null) {
            logger.finest("making lifeCycleUrl to queryManagerUrl");
	    lifeCycleManagerURLString = queryManagerURLString;
        }

        semanticEquivalences =
            (String) properties.get(SEMANTIC_EQ_PROP);
        authenticationMethod =
            (String) properties.get(AUTH_METHOD_PROP);
        logger.finest("authentication method=" + authenticationMethod);
        if ((authenticationMethod != null) &&
            (!authenticationMethod.equals("UDDI_GET_AUTHTOKEN"))) {
            throw new InvalidRequestException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("ConnectionImpl:Connection_only_supports_UDDI_GET_AUTHTOKEN_authentication"));
        }
        maxRows =
            (String) properties.get(MAX_ROWS);

        httpProxyHost = (String) properties.get(HTTP_PROXY_HOST);
	httpProxyPort = (String) properties.get(HTTP_PROXY_PORT);
	httpsProxyHost = (String) properties.get(HTTPS_PROXY_HOST);
	httpsProxyPort = (String) properties.get(HTTPS_PROXY_PORT);
	proxyUserName = (String) properties.get(PROXY_USER_NAME);
	proxyPassword = (String) properties.get(PROXY_PASSWORD);

	// these will be overridden in RegistryService if proxies change
	if (httpProxyHost != null) {
	    final String fHttpHost = httpProxyHost;
	    AccessController.doPrivileged(new PrivilegedAction() {
		public Object run() {
		    System.setProperty("http.proxyHost", fHttpHost);
		    return null;
		}
	    });
	}
	if (httpProxyPort != null) {
	    final String fHttpPort = httpProxyPort;
	    AccessController.doPrivileged(new PrivilegedAction() {
		public Object run() {
		    System.setProperty("http.proxyPort", fHttpPort);
		    return null;
		}
	    });
	}
	if (httpsProxyHost != null) {
	    final String fHttpsHost = httpsProxyHost;
	    AccessController.doPrivileged(new PrivilegedAction() {
		public Object run() {
		    System.setProperty("https.proxyHost", fHttpsHost);
		    return null;
		}
	    });
	}
	if (httpsProxyPort != null) {
	    final String fHttpsPort = httpsProxyPort;
	    AccessController.doPrivileged(new PrivilegedAction() {
		public Object run() {
		    System.setProperty("https.proxyPort", fHttpsPort);
		    return null;
		}
	    });
	}
        
         // check system first, then connection property
        userDefinedTaxonomy = (String)
	    AccessController.doPrivileged(new PrivilegedAction() {
		public Object run() {
		    return System.getProperty(USER_DEF_TAXONOMIES);
		}
	    });

        String propDefinedTaxonomy =
            (String) properties.get(USER_DEF_TAXONOMIES);
        
        if ((propDefinedTaxonomy != null) && (!propDefinedTaxonomy.equals("")))
            userDefinedTaxonomy = propDefinedTaxonomy;

        // check system first, then connection property
        defaultPostalAddressScheme = (String)
	    AccessController.doPrivileged(new PrivilegedAction() {
		public Object run() {
		    return System.getProperty(POSTAL_SCHEME_PROP);
		}
	    });

        String propPostalAddressScheme =
            (String) properties.get(POSTAL_SCHEME_PROP);
        
        if ((propPostalAddressScheme != null) && (!propPostalAddressScheme.equals("")))
            defaultPostalAddressScheme = propPostalAddressScheme;

        String useSoapString = (String) properties.get(USE_SOAP);
	if ((useSoapString != null) && (useSoapString.equalsIgnoreCase("true"))) {
	    useSOAP = true;
	} 
        
        String useCacheString = (String) properties.get(USE_CACHE);
	if ((useCacheString != null) && (useCacheString.equalsIgnoreCase("false"))) {
	    useCache = false;
	} 

        String timeoutString = (String) properties.get(AUTH_TOKEN_TIMEOUT);
	if ((timeoutString != null) && (!timeoutString.equalsIgnoreCase("0"))) {
	    timeout = (new Long(timeoutString)).longValue();
	} else
            timeout = DEFAULT_TIMEOUT;

        subject = new Subject();
        service = new RegistryServiceImpl(this);
        connectionId = Utility.generateUUID();
    }
    
    /**
     * Gets the RegistryService interface associated with the Connection.
     * @associates <{RegistryService}>
     * @see <{RegistryService}>
     */
    public RegistryService getRegistryService() throws JAXRException {
        synchronized (this) {
            if (!isClosed) {
                if (service == null) {
                    service = new RegistryServiceImpl(this);
                }
            } else throw new JAXRException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("ConnectionImpl:Connection_is_closed"));
            return service;
        }
    }
    
    /**
     * Since a provider typically allocates significant resources outside
     * the JVM on behalf of a Connection, clients should close them when
     * they are not needed.
     *
     * @exception JAXRException if a JARR error occurs.
     */
    
    public void close() throws JAXRException {
        synchronized (this) {
            if (!isClosed) {
                if (logger.isLoggable(Level.FINEST)) {
                    logger.finest("Closing UDDI connection" + this);
                }
                service = null;
                isClosed = true;
            }
        }
    }
    
    /**
     * Returns true if this connection has been closed.
     *
     * @return closed state of connection
     */
    public boolean isClosed() {
        synchronized (this) {
            return isClosed;
        }
    }
    
    /** gets the URL to the registry provider that is teh end point for this Connection */
    String getQueryManagerURL() throws JAXRException {
        if (!isClosed) {
            return queryManagerURLString;
        } else {
            throw new JAXRException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("ConnectionImpl:Connection_is_Closed"));
        }
    }
    
    /** gets the URL to the registry provider that is teh end point for this Connection */
    String getLifeCycleManagerURL() throws JAXRException {
        if (!isClosed) {
            return lifeCycleManagerURLString;
        } else {
            throw new JAXRException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("ConnectionImpl:Connection_is_closed"));
        }
    }
    
    /**
     * Internal method for getting the semantic equivalences.
     * This method initializes the hash map the first time
     * it is called. If no equivalences have been specified,
     * the method returns null.
     */
    HashMap getSemanticEquivalences() {

	// none specified
	if (semanticEquivalences == null) {
	    return null;
	}

	if (equivalences == null) {
	    if (logger.isLoggable(Level.FINEST)) {
		logger.finest("Parsing semantic equivalences");
	    }
            String space = " ";
	    String comma = ",";
	    String prefix = "urn:";
	    String delimiter = "|";
            StringBuffer seBuffer = new StringBuffer();
	    equivalences = new HashMap();

	    // remove extra spaces from string
	    logger.finest(semanticEquivalences);
	    StringTokenizer spaceTokenizer =
                new StringTokenizer(semanticEquivalences, space);
            while (spaceTokenizer.hasMoreElements()) {
                seBuffer.append(spaceTokenizer.nextToken());
            }
            semanticEquivalences = seBuffer.toString();
	    logger.finest(semanticEquivalences);

	    // break string up into separate eq's
	    StringTokenizer tokenizer =
                new StringTokenizer(semanticEquivalences, delimiter);
	    while (tokenizer.hasMoreElements()) {
		String token = tokenizer.nextToken();
		String source = token.substring(
		    token.indexOf(prefix) + prefix.length(),
		    token.indexOf(comma));
		String target = token.substring(
                    token.lastIndexOf(prefix) + prefix.length());
		logger.finest(source + "->" + target);
		equivalences.put(source, target);
	    }
	}
	return equivalences;
    }

    HashMap fetchEquivalences(){
        return equivalences;
    }

    /**
     * Internal method for getting value of
     * defaultPostalScheme property.
     */
    String getDefaultPostalAddressScheme() {
        return defaultPostalAddressScheme;
    }
    
    /**
     * Internal method for getting value of
     * authenticationMethod property.
     */
    String getAuthenticationMethod() {
        return authenticationMethod;
    }
    
    /**
     * Internal method for getting the value of
     * maxRows property.
     */
    String getMaxRows() {
        return maxRows;
    }
    
    /**
     * Return true if client uses synchronous communication with JAXR provider.
     * Return false by default. Note that a JAXR provider must support both modes
     * of communication, while the client can choose which mode it wants to use.
     */
    public boolean isSynchronous() throws JAXRException {
        synchronized (this) {
            if (!isClosed) {
                return synchronous;
            } else {
                throw new JAXRException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("ConnectionImpl:Connection_is_Closed"));
            }
        }
    }
    
    /**
     * Sets whether the client uses synchronous communication or not.
     * A RegistryClient may dynamically change its communication style
     * preference.
     */
    public void setSynchronous(boolean sync) throws JAXRException {
        synchronized (this) {
            if (!isClosed) {
                synchronous = sync;
            } else {
                throw new JAXRException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("ConnectionImpl:Connection_is_closed"));
            }
        }
    }
    
    /**
     * Gets the Locale associated with this client.
     */
    Locale getLocale() throws JAXRException {
        if (!isClosed) {
            if (locale == null) {
                return Locale.getDefault();
            } else {
                return locale;
            }
        } else {
            throw new JAXRException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("ConnectionImpl:Connection_is_closed"));
        }
    }
    
    /**
     * Sets the Locale associated with this client.
     * A RegistryClient may dynamically change its locale
     * preference.
     */
    void setLocale(Locale locale) throws JAXRException {
        if (!isClosed) {
            this.locale = locale;
        } else {
            throw new JAXRException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("ConnectionImpl:Connection_is_closed"));
        }
    }
    
    public void setCredentials(Set credentials) throws JAXRException {
        synchronized (this) {
            Iterator iterator;
            //kmw -- fix in redesign
            privateCredentials = credentials;
            if (!isClosed) {
                // get rid of the old credentials
                authToken = (service.getUDDIMapper().getAuthorizationToken(credentials)).toCharArray();
                
            } else {
                throw new JAXRException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("ConnectionImpl:Connection_is_closed"));
            }
        }
    }
    
    public Set getCredentials() throws JAXRException {
        synchronized (this) {
            if (!isClosed) {
                return privateCredentials;

            } else {
                throw new JAXRException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("ConnectionImpl:Connection_is_closed"));
            }
        }
    }
    
    String getAuthToken() throws JAXRException {
        if (!isClosed) {
            return new String(authToken);
        } else {
            throw new JAXRException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("ConnectionImpl:Connection_is_closed"));
        }
    }
    
    void setAuthToken(char[] token) throws JAXRException {
        if (!isClosed) {
            authToken = token;
        } else {
            throw new JAXRException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("ConnectionImpl:Connection_is_closed"));
        }
    }
    
    void setAuthTokenTimestamp(long stamp) {
        timestamp = stamp;
    }
    
    long getAuthTokenTimestamp(){
        return timestamp;
    }
    
    Set getAuthCreds() throws JAXRException {
        if (!isClosed) {
            return privateCredentials;
        } else {
            throw new JAXRException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("ConnectionImpl:Connection_is_closed"));
        }
    }
    
    long getTokenTimeout(){
        if (timeout == 0)
            return DEFAULT_TIMEOUT;
        return timeout;
    }
    
    private LoginContext getLoginContext() throws JAXRException {
        if (lc == null) {
            try {
                lc = new LoginContext("Prototype", subject, new ProtoCallbackHandler(service.getUDDIMapper()));
            } catch (LoginException lex) {
                throw new JAXRException(lex);
            }
        }
        
        return lc;
    }
    
    void login() throws JAXRException {
        try {
            getLoginContext().login();
        } catch (LoginException lex) {
            throw new JAXRException(lex);
        }
    }

    String getHttpProxyHost() {
	return httpProxyHost;
    }

    String getHttpProxyPort() {
	return httpProxyPort;
    }

    String getHttpsProxyHost() {
	return httpsProxyHost;
    }

    String getHttpsProxyPort() {
	return httpsProxyPort;
    }

    String getProxyUserName() {
	return proxyUserName;
    }

    String getProxyPassword() {
	return proxyPassword;
    }
    public String getUserDefinedTaxonomy(){
        return userDefinedTaxonomy;
    }
   
    /*
     * Internal method used for switching from jaxm 
     * to soap4j
     */
    boolean useSOAP() {
	return useSOAP;
    }
    
    public boolean useCache() {
        return useCache;
    }

    public String getCurrentUser() {

         if ((privateCredentials != null) && (!privateCredentials.isEmpty())){
         Iterator iterator = privateCredentials.iterator();
         while (iterator.hasNext()) {
             Object credential = iterator.next();
             if (credential instanceof java.net.PasswordAuthentication)
                   return ((java.net.PasswordAuthentication)credential).getUserName();
         }
         }
        return null;
    }
}


