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

/**
 * Copyright 2000-2001 by iPlanet/Sun Microsystems, Inc.,
 * 901 San Antonio Road, Palo Alto, California, 94303, U.S.A.
 * All rights reserved.
 */

package com.sun.enterprise.web.util;

import java.util.Map;
import java.util.Locale;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServletRequest;

import org.apache.catalina.util.RequestUtil;
import com.sun.enterprise.deployment.runtime.web.LocaleCharsetMap;
import com.sun.logging.LogDomains;

public final class I18NParseUtil {

    /**
     * The logger to use for logging info about the charset
     * found from hidden field or locale-charset-map
     */
    public static final Logger _logger =
        LogDomains.getLogger(LogDomains.WEB_LOGGER);

    /**
     * This indicates whether debug logging is on or not
     */
    private static boolean _debugLog = _logger.isLoggable(Level.FINE);

    /**
     * Parse the request parameter according to the locale-charset-info tag
     * in ias-web.xml
     * 
     * <p>
     * <strong>IMPLEMENTATION NOTE</strong>: If hiddenFieldName is null,
     * get the encoding from lcMap object, and let the original
     * RequestUtil.parseParameters deal with the parsing. If hiddenFieldName
     * is not null, first prase queryString to get hidden field value.
     * if hidden field value is found in the queryString, then buf parsing
     * will be handled by RequestUtil.parseParameters according to that value.
     * If hidden field value is not found in queryString, then save the
     * query string keys in a linked list and look for a hidden field value
     * in buf. Once the hidden field value is found in buf, process 
     * the linked list of queryString according to that value.
     * If hidden field value not found in buf either, get encoding value
     * from lcMap. If none is found, then default to ISO-8859-1.
     * Then process both linked list objects.
     * 
     * NOTE: byte array buf is modified by this method.  Caller beware.
     *
     * @param map Map that accumulates the resulting parameters
     * @param queryString Input string containing request parameters
     * @param buf Input byte array containing request parameters
     * @param lcMap Inputcontainig locale-charset mapping info
     * @param hiddenFieldName hidden field which will specify the
     *        decoding charset
     * @param request object used to get the locale and agent header value
     *
     * @return The encoding of the request
     * @exception UnsupportedEncodingException if the data is malformed
     */
     public static String parseParametersUsingLCInfo(
                          Map map, String queryString, byte[] buf,
                          LocaleCharsetMap[] lcMap, String hiddenFieldName, 
                          HttpServletRequest request)
        throws UnsupportedEncodingException {

        byte[] queryStringBytes = null;
        if ((queryString != null) && (queryString.length() > 0))
            queryStringBytes = queryString.getBytes();

        //process query string
        LinkedList queryStringKeys = new LinkedList();
        String hiddenFieldValue = processBufferWithHiddenField(
                                      map, queryStringBytes, hiddenFieldName,
                                      queryStringKeys, null, null);
        //done with query String, process buf
        //test if hidden field was found in query string
        if (hiddenFieldValue != null) {
            if (_debugLog)
                _logger.fine("Got charset from queryString, hidden field " +
                    "name = " + hiddenFieldName + ", hidden field value = " +
                    hiddenFieldValue);
            RequestUtil.parseParameters(map, buf, hiddenFieldValue);
            return hiddenFieldValue;
        }
        
        // hidden field not found in query string, try to find it in POST data
        LinkedList bufKeys = new LinkedList();
        hiddenFieldValue = processBufferWithHiddenField(
                               map, buf, hiddenFieldName, bufKeys,
                               queryStringBytes, queryStringKeys);

        if (hiddenFieldValue != null) {
            if (_debugLog)
                _logger.fine("Got charset from POST data, hidden field " +
                    "name = " + hiddenFieldName + ", hidden field value = " +
                    hiddenFieldValue);
            return hiddenFieldValue;
        }

        String encoding = null;
        if (lcMap != null) {
            encoding = getLocaleCharsetEncoding(request, lcMap);
            hiddenFieldValue = encoding;
        }

        if (encoding == null) {
            encoding = "ISO-8859-1";
            if (_debugLog)
                _logger.fine("Using default encoding to parse params: " +
                             encoding);
        }
     
        processLinkedList(map, queryStringBytes, queryStringKeys, encoding);
        processLinkedList(map, buf, bufKeys, encoding);

        return hiddenFieldValue;
    }

    /**
     * Append request parameters from the specified String to the specified
     * Map.  It is presumed that the specified Map is not accessed from any
     * other thread, so no synchronization is performed.
     * <p>
     * <strong>IMPLEMENTATION NOTE</strong>: We look for a hidden field value  
     * in order to do URL decoding according it. While looking for that
     * hidden field, keys are stored in a linked list along with the start
     * and end pos of the correspondign value. This is done to avoid double
     * parsing. Once the hidden field is found, the linked list is processed
     * and parsing continues according to the already found hidden field value
     * Parsing is done individually on name and value elements, rather than on
     * the entire query string ahead of time, to properly deal with the case
     * where the name or value includes an encoded "=" or "&" character
     * that would otherwise be interpreted as a delimiter.
     *
     * NOTE: byte array data is modified by this method.  Caller beware.
     *
     * @param map Map that accumulates the resulting parameters
     * @param hiddenFieldName hidden field which will specify the
     *        decoding charset
     * @param keys linked list to store the keys
     *
     * @return The hidden field value corresponding to hiddenFieldName
     * @exception UnsupportedEncodingException if the data is malformed
     */
    public static String processBufferWithHiddenField(
                             Map map,  byte[] buf, String hiddenFieldName, 
                             LinkedList keys, byte[] queryStringBytes, 
                             LinkedList queryStringKeys)
        throws UnsupportedEncodingException {

        int    pos = 0;
        int    ix = 0;
        int    ox = 0;
        String key = null;
        String value = null;
        boolean foundHiddenField = false;
        String hiddenFieldValue = null;

        if (buf == null)
            return null;

        while (ix < buf.length) {      
            byte c = buf[ix++];
            switch ((char) c) {
            case '&':
                if (key != null) {
                    if (foundHiddenField) {
                        value = new String(buf, pos, ox - pos,
                                           hiddenFieldValue);
                        putMapEntry(map, key, value);
                    }
                    else if (key.equals(hiddenFieldName)) {
                        hiddenFieldValue = new String(buf, pos, ox - pos,
                                                      "ISO-8859-1");
                        // hidden field found, process the linked lists that
                        // have been created so far
                        if (queryStringKeys != null)
                            processLinkedList(map, queryStringBytes,
                                         queryStringKeys, hiddenFieldValue);
                        processLinkedList(map, buf, keys, hiddenFieldValue);
                        putMapEntry(map, key, hiddenFieldValue);
                        foundHiddenField = true;
                    }
                    else {
                        Object[] startEndPos = new Object[3];
                        startEndPos[0] = key;
                        startEndPos[1] = new Integer(pos);
                        startEndPos[2] = new Integer(ox);
                        keys.add(startEndPos);
                    }
                    key = null;
                }
                pos = ix;
                ox = ix;
                break;
            case '=':
                if (key == null) {
                    key = new String(buf, pos, ox - pos, "ISO-8859-1");
                    ox = ix;
                    pos = ix;
                } else {
                    buf[ox++] = c;
                }
                break;
            case '+':
                buf[ox++] = (byte)' ';
                break;
            case '%':
                buf[ox++] = (byte)((convertHexDigit(buf[ix++]) << 4) +
                                    convertHexDigit(buf[ix++]));
                break;
            default:
                buf[ox++] = c;
            }
        }

        //The last value does not end in '&'.  So save it now.
        if (key != null) {
            if (foundHiddenField) {
                value = new String(buf, pos, ox - pos, hiddenFieldValue);
                putMapEntry(map, key, value);
            }
            else if (key.equals(hiddenFieldName)) {
                hiddenFieldValue = new String(buf, pos, ox - pos, "ISO-8859-1");
                // hidden field found, process the linked lists that
                // have been created so far
                if (queryStringKeys != null)
                    processLinkedList(map, queryStringBytes, queryStringKeys,
                                      hiddenFieldValue);
                processLinkedList(map, buf, keys, hiddenFieldValue);
                putMapEntry(map, key, hiddenFieldValue);
                foundHiddenField = true;
            }
            else {
                Object[] startEndPos = new Object[3];
                startEndPos[0] = key;
                startEndPos[1] = new Integer(pos);
                startEndPos[2] = new Integer(ox);
                keys.add(startEndPos);
            }
        }

        return hiddenFieldValue;
    }

    /**
     * Process a linked list containing. The linked list nodes consist of
     * (key, start pos, end pos)
     * start pos and end pos correspond to the start and end postion of the
     * value corresponding to the key.
     * <p>
     * <strong>IMPLEMENTATION NOTE</strong>: Process the whole linked.
     * A key, value pair is created from each node and stored in the map.
     *
     * @param map Map that accumulates the resulting parameters
     * @param buf array of bytes to be precessed
     * @param keys linked listcontaning the keys
     * @param encoding charset for converting bytes to java strings
     *
     * @exception UnsupportedEncodingException if the data is malformed
     */
    public static void processLinkedList(Map map, byte[] buf, LinkedList keys,
                                         String encoding)
        throws UnsupportedEncodingException {

        if (buf == null || keys == null)
            return;

        ListIterator keysIterator = keys.listIterator(0);
        while (keysIterator.hasNext()) {
            Object[] startEndPos = (Object[])keysIterator.next();
            String key = (String)startEndPos[0];
            int startPos = ((Integer)startEndPos[1]).intValue();
            int endPos = ((Integer)startEndPos[2]).intValue();
            String value = new String(buf, startPos, endPos - startPos,
                                      encoding);
            putMapEntry(map, key, value);
        }
        keys.clear();
    }

    /**
     * Return the charset corresponding to a (locale, agent) pair.
     * <p>
     * <strong>IMPLEMENTATION NOTE</strong>: The lcMap parameter contains
     * an array of LocaleCharsetMap object. A LocaleCharsetMap object consist
     * of (locale, agent, charset). The agent attribute may be a null value.
     * If agent is null, we match the locale attribute to the requst locale.
     * If agent is not null we match (locale, agent) to the request locale,
     * and request agent header value. We look for exact match of the agent,
     * so it is the user responsibility to give and exact value of the agent
     * in ias-web XML file. If no match is found, ISO-8859-1 is returned.
     *
     * @param request The request object for which a matching charset
     *        is looked for
     * @param lcMap an object representing locale-charset-map in
     *        sun-web.xml file
     * @return matching encoding or ISO-8859-1 if no match is found
     */
    public static String getLocaleCharsetEncoding(HttpServletRequest request,
                                         LocaleCharsetMap[] charsetMap) {
        if (charsetMap == null)
            return null;

        String requestLocale = request.getLocale().toString();
        if (requestLocale == null)
            return null;

        String userAgent = request.getHeader("user-agent");

        for (int i = 0; i < charsetMap.length; i++) {
            LocaleCharsetMap element = charsetMap[i];
            String s = (String)element.getAttributeValue("Locale");
            if (s.equals(requestLocale)) {
                String agent = (String)element.getAttributeValue("Agent");
                if (agent == null || agent.length() == 0 ||
                    userAgent == null || agent.equals(userAgent)) {
                    String encoding =
                               (String)element.getAttributeValue("Charset");
                    if (_debugLog)
                        _logger.fine("Got charset in locale-charset-map" +
                                     ", locale=" + requestLocale +
                                     ", agent=" + agent +
                                     ", charset=" + encoding);
                    return encoding;
                }
            }
        }
        return null;
    }

    /**
     * Put name value pair in map.
     *
     * @param b the character value byte
     *
     * Put name and value pair in map. When name already exist, add value
     * to array of values.
     */
    private static void putMapEntry(Map map, String name, String value) {
        String[] newValues = null;
        String[] oldValues = (String[]) map.get(name);
        if (oldValues == null) {
            newValues = new String[1];
            newValues[0] = value;
        } else {
            newValues = new String[oldValues.length + 1];
            System.arraycopy(oldValues, 0, newValues, 0, oldValues.length);
            newValues[oldValues.length] = value;
        }
        map.put(name, newValues);
    }

    /**
     * Convert a byte character value to hexidecimal digit value.
     *
     * @param b the character value byte
     */
    private static byte convertHexDigit( byte b ) {
        if ((b >= '0') && (b <= '9')) return (byte)(b - '0');
        if ((b >= 'a') && (b <= 'f')) return (byte)(b - 'a' + 10);
        if ((b >= 'A') && (b <= 'F')) return (byte)(b - 'A' + 10);
        return 0;
    }
}
