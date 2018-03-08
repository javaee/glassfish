/*
 * Copyright (c) 1997-2018 Oracle and/or its affiliates. All rights reserved.
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

/*
 * This class is based on a class originally written by Jason Hunter
 * <jhunter@acm.org> as part of the book "Java Servlet Programming"
 * (O'Reilly).  See http://www.servlets.com/book for more information.
 * Used by Sun Microsystems with permission.
 */

package org.apache.catalina.util;


import java.io.InputStream;
import java.util.Locale;
import java.util.Properties;



/**
 * Utility class that attempts to map from a Locale to the corresponding
 * character set to be used for interpreting input text (or generating
 * output text) when the Content-Type header does not include one.  You
 * can customize the behavior of this class by modifying the mapping data
 * it loads, or by subclassing it (to change the algorithm) and then using
 * your own version for a particular web application.
 *
 * @author Craig R. McClanahan
 * @revision $Date: 2005/12/08 01:28:14 $ $Version$
 */

/* SJSAS 6292972
public class CharsetMapper {
*/
// START SJSAS 6292972
public class CharsetMapper implements Cloneable {
// END SJSAS 6292972


    // ---------------------------------------------------- Manifest Constants
    private static final char HYPHEN = '-';

    /**
     * Default properties resource name.
     */
    public static final String DEFAULT_RESOURCE =
      "/org/apache/catalina/util/CharsetMapperDefault.properties";


    // ---------------------------------------------------------- Constructors


    /**
     * Construct a new CharsetMapper using the default properties resource.
     */
    public CharsetMapper() {

        this(DEFAULT_RESOURCE);

    }


    /**
     * Construct a new CharsetMapper using the specified properties resource.
     *
     * @param name Name of a properties resource to be loaded
     *
     * @exception IllegalArgumentException if the specified properties
     *  resource could not be loaded for any reason.
     */
    public CharsetMapper(String name) {

        InputStream stream = null;
        try {
            stream =
                this.getClass().getResourceAsStream(name);
            map.load(stream);
        } catch (Throwable t) {
            throw new IllegalArgumentException(t.toString());
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch(Throwable t2) {
                }
            }
        }


    }


    // ---------------------------------------------------- Instance Variables


    /**
     * The mapping properties that have been initialized from the specified or
     * default properties resource.
     */
    // START RIMOD 4870531
    /*
    private Properties map = new Properties();
    */
    protected Properties map = new Properties();
    // END RIMOD 4870531


    // ------------------------------------------------------- Public Methods


    /**
     * Calculate the name of a character set to be assumed, given the specified
     * Locale and the absence of a character set specified as part of the
     * content type header.
     *
     * @param locale The locale for which to calculate a character set
     */
    public String getCharset(Locale locale) {

        String charset = null;

        // First, try a full name match (language and country)
        charset = map.getProperty(locale.toString());
        if (charset != null)
            return (charset);

        // Second, try to match just the language
        charset = map.getProperty(locale.getLanguage());
        return (charset);

    }

    /**
     * The deployment descriptor can have a
     * locale-encoding-mapping-list element which describes the
     * webapp's desired mapping from locale to charset.  This method
     * gets called when processing the web.xml file for a context
     *
     * @param locale The locale for a character set
     * @param charset The charset to be associated with the locale
     */
    public void addCharsetMappingFromDeploymentDescriptor(String locale,String charset) {
        if (((locale.indexOf(HYPHEN)) > -1)) {
            map.put(Locale.forLanguageTag(locale).toString(),charset);
        }
        else{
            map.put( locale, charset );
        }
    }


    // START SJSAS 6292972
    public final Object clone() {
        
        try {
            CharsetMapper clone = (CharsetMapper)super.clone();
            clone.map = (Properties)map.clone();
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e.toString());
        }
    }
    // END SJSAS 6292972
}
