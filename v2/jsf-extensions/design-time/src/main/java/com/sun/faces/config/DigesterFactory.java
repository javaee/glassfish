/*
 * $Id: DigesterFactory.java,v 1.1 2005/09/20 21:11:23 edburns Exp $
 */

/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at
 * https://javaserverfaces.dev.java.net/CDDL.html or
 * legal/CDDLv1.0.txt. 
 * See the License for the specific language governing
 * permission and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at legal/CDDLv1.0.txt.    
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * [Name of File] [ver.__] [Date]
 * 
 * Copyright 2005 Sun Microsystems Inc. All Rights Reserved
 */


package com.sun.faces.config;

import com.sun.faces.util.ToolsUtil;
import org.apache.commons.digester.Digester;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;

import java.net.URL;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <p>A simple factory to hide <code>Digester</code> configuration
 * details.</p>
 */
public class DigesterFactory {

    private static final Logger logger = ToolsUtil.getLogger(ToolsUtil.FACES_LOGGER +
            ToolsUtil.CONFIG_LOGGER);    
    
    /**
     * <p><code>Xerces</code> specific feature to enable both
     * DTD and Schema validation.</p>
     */
    private static final String XERCES_VALIDATION =
        "http://xml.org/sax/features/validation";

    /**
     * <p><code>Xerces</code> specific feature to enable both
     * DTD and Schema validation.</p>
     */
    private static final String XERCES_SCHEMA_VALIDATION =
        "http://apache.org/xml/features/validation/schema";

    /**
     * <p><code>Xerces</code> specific feature to enabled constraint
     * validation.</p>
     */
    private static final String XERCES_SCHEMA_CONSTRAINT_VALIDATION =
        "http://apache.org/xml/features/validation/schema-full-checking";

    /**
     * <p>Custom <code>EntityResolver</code>.</p>
     */
    private static final JsfEntityResolver RESOLVER = new JsfEntityResolver();

    /**
     * <p>Indicates whether or not document validation is
     * requested or not.</p>
     */
    private boolean validating;



    // ------------------------------------------------------------ Constructors


    /**
     * <p>Creates a new DigesterFactory instance.</p>
     * @param isValidating - <code>true</code> if the <code>Digester</code>
     *  instance that is ultimately returned should be configured (if possible)
     *  for document validation.  If validation is not desired, pass
     *  <code>false</code>.
     */
    private DigesterFactory(boolean isValidating) {

        validating = isValidating;

    } // END DigesterFactory


    // ---------------------------------------------------------- Public Methods


    /**
     * <p>Returns a new <code>DigesterFactory</code> instance that will create
     * a non-validating <code>Digester</code> instance.</p>
     */
    public static DigesterFactory newInstance() {

        return DigesterFactory.newInstance(false);

    } // END newInstance


    /**
     * <p>Creates a new <code>DigesterFactory</code> instance that will
     * create a <code>Digester</code> instance where validation depends
     * on the value of <code>isValidating</code>.</p>
     * @param isValidating - <code>true</code> if the <code>Digester</code>
     *  instance that is ultimately returned should be configured (if possible)
     *  for document validation.  If validation is not desired, pass
     *  <code>false</code>.
     * @return a new <code>DigesterFactory</code> capable of creating
     *  <code>Digester</code>instances
     */
    public static DigesterFactory newInstance(boolean isValidating) {

        return new DigesterFactory(isValidating);

    } // END newInstance


    /**
     * <p>Creates a new <code>Digester</code> instance configured for use
     * with JSF.</p>
     */
    public Digester createDigester() {

        Digester digester = new Digester();
        configureDigester(digester);

        return digester;

    } // END getDigester


    // --------------------------------------------------------- Private Methods


    /**
     * <p>Configures the provided <code>Digester</code> instance appropriate
     * for use with JSF.</p>
     * @param digester - the <code>Digester</code> instance to configure
     */
    private void configureDigester(Digester digester) {

        digester.setNamespaceAware(true);
        digester.setUseContextClassLoader(true);
        digester.setEntityResolver(RESOLVER);

        if (validating) {

            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, "Attempting to configure Digester to perform" +
                    " document validation.");
            }

            // In order to validate using *both* DTD and Schema, certain
            // Xerces specific features are required.  Try to set these
            // features.  If an exception is thrown trying to set these
            // features, then disable validation.

            try {
                digester.setFeature(XERCES_VALIDATION, true);
                digester.setFeature(XERCES_SCHEMA_VALIDATION, true);
                digester.setFeature(XERCES_SCHEMA_CONSTRAINT_VALIDATION, true);
                digester.setValidating(true);
            } catch (SAXNotSupportedException e) {

                if (logger.isLoggable(Level.WARNING)) {
                    logger.log(Level.WARNING, "Attempt to set supported feature on XMLReader, " +
                        "but the value provided was not accepted.  " +
                        "Validation will be disabledb.");
                }

                digester.setValidating(false);

            } catch (SAXNotRecognizedException e) {

                if (logger.isLoggable(Level.WARNING)) {
                    logger.log(Level.WARNING, "Attempt to set unsupported feature on XMLReader" +
                        " necessary for validation.  Validation will be" +
                        "disabled.");
                }

                digester.setValidating(false);

            } catch (ParserConfigurationException e) {

                if (logger.isLoggable(Level.WARNING)) {
                    logger.log(Level.WARNING, "Digester unable to configure underlying parser." +
                        "  Validation will be disabled.");
                }

                digester.setValidating(false);

            }
        } else {
            digester.setValidating(false);
        }

    } // END configureDigester


    // ----------------------------------------------------------- Inner Classes


    private static class JsfEntityResolver extends DefaultHandler {

        /**
         * <p>Contains associations between grammar name and the physical
         * resource.</p>
         */
        private static final String[][] DTD_SCHEMA_INFO = {
            {
                "web-facesconfig_1_0.dtd",
                "/com/sun/faces/web-facesconfig_1_0.dtd"
            },
            {
                "web-facesconfig_1_1.dtd",
                "/com/sun/faces/web-facesconfig_1_1.dtd"
            },
            {
                "web-facesconfig_1_2.xsd",
                "/com/sun/faces/web-facesconfig_1_2.xsd"
            },
            {
                "javaee_5.xsd",
                "/com/sun/faces/javaee_5.xsd"
            },
            {
                "javaee_web_services_client_1_2.xsd",
                "/com/sun/faces/javaee_web_services_client_1_2.xsd"
            },
            {
                "xml.xsd",
                "/com/sun/faces/xml.xsd"
            }
        };

        /**
         * <p>Contains mapping between grammar name and the local URL to the
         * physical resource.</p>
         */
        private HashMap<String,String> entities = new HashMap<String, String>();


        // -------------------------------------------------------- Constructors


        public JsfEntityResolver() {

            // Add mappings between last segment of system ID and
            // the expected local physical resource.  If the resource
            // cannot be found, then rely on default entity resolution
            // and hope a firewall isn't in the way or a proxy has
            // been configured
            for (String[] aDTD_SCHEMA_INFO : DTD_SCHEMA_INFO) {
                URL url = this.getClass().getResource(aDTD_SCHEMA_INFO[1]);
                if (url == null) {
                    if (logger.isLoggable(Level.WARNING)) {
                        logger.log(Level.WARNING,
                                   "nable to locate local resource '"
                                   + aDTD_SCHEMA_INFO[1]
                                   + "'.  Standard entity "
                                   + "resolution will be used when request "
                                   + "is present for '"
                                   + aDTD_SCHEMA_INFO[0] + '\'');
                    }
                } else {
                    entities.put(aDTD_SCHEMA_INFO[0], url.toString());
                }
            }

        } // END JsfEntityResolver


        // ----------------------------------------- Methods from DefaultHandler


        /**
         * <p>Resolves the physical resource using the last segment of
         * the <code>systemId</code>
         * (e.g. http://java.sun.com/dtds/web-facesconfig_1_1.dtd,
         * the last segment would be web-facesconfig_1_1.dtd).  If a mapping
         * cannot be found for the segment, then defer to the
         * <code>DefaultHandler</code> for resolution.</p>
         */
        public InputSource resolveEntity(String publicId, String systemId)
        throws SAXException {

            // publicId is ignored.  Resolution performed using
            // the systemId.

            // If no system ID, defer to superclass
            if (systemId == null) {
                InputSource result;
                try {
                    result = super.resolveEntity(publicId, systemId);
                }
                catch (Exception e) {
                    throw new SAXException(e);
                }
                return result;
            }

            String grammarName =
                systemId.substring(systemId.lastIndexOf('/') + 1);

            String entityURL = entities.get(grammarName);

            InputSource source;
            if (entityURL == null) {
                // we don't have a registered mapping, so defer to our
                // superclass for resolution

                if (logger.isLoggable(Level.FINE)) {
                    logger.log(Level.FINE, "Unknown entity, deferring to superclass.");
                }
                try {
                    source = super.resolveEntity(publicId, systemId);
                }
                catch (Exception e) {
                    throw new SAXException(e);
                }

            } else {

                try {
                    source = new InputSource(new URL(entityURL).openStream());
                } catch (Exception e) {
                    if (logger.isLoggable(Level.WARNING)) {
                        logger.log(Level.WARNING, "Unable to create InputSource for URL '" +
                            entityURL + "'");
                    }
                   
                    source = null;
                }
            }

            // Set the System ID of the InputSource with the URL of the local
            // resource - necessary to prevent parsing errors
            if (source != null) {
                source.setSystemId(entityURL);

                if (publicId != null) {
                    source.setPublicId(publicId);
                }
            }

            return source;

        } // END resolveEntity

    } // END JsfEntityResolver

}
