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
package com.sun.enterprise.ee.synchronization;

import java.net.URL;
import java.net.MalformedURLException;
import java.io.IOException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStream;
import java.util.Properties;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

import org.w3c.dom.Document;
import org.w3c.dom.*;
import com.sun.enterprise.util.SystemPropertyConstants;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException; 

import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.ee.EELogDomains;


class SynchronizationConfig {

    private static Logger _logger = Logger.getLogger(
        EELogDomains.SYNCHRONIZATION_LOGGER);

    protected Document configDoc;
    protected NodeList maps;
    private static String serverName = null;

    /** Creates a new instance of SyncConfig */
    SynchronizationConfig(String xmlFile) {
        this(xmlFile, null);
    }

    SynchronizationConfig(String xmlFile, String sName) {

        if (sName == null) {
            serverName=System.getProperty(SystemPropertyConstants.SERVER_NAME);
        } else {
            serverName = sName;
        }

        try{
            configDoc  = readConfiguration(xmlFile);        
        }
        catch(MalformedURLException e){
            _logger.log(Level.SEVERE, 
                "synchronization.malformed_xml", 
                new Object[] {xmlFile, e.getLocalizedMessage()});
        }
        catch(IOException e){
            _logger.log(Level.SEVERE, 
                "synchronization.config.io_exception", 
                new Object[] {xmlFile, e.getLocalizedMessage()});
        }

        NodeList mappings = configDoc.getElementsByTagName("mappings");
        Element e = (Element)mappings.item(0);
        maps = e.getElementsByTagName("mapping");
    }
    
    private Document readConfiguration(String url) throws IOException {
        Document doc=null;

        try{
            DocumentBuilderFactory docBuilderFactory = 
                DocumentBuilderFactory.newInstance();
            docBuilderFactory.setValidating(false);
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            InputStream in = ClassLoader.getSystemResourceAsStream(url);
            doc = docBuilder.parse(in);

            // normalize text representation
            doc.getDocumentElement ().normalize ();
        }
        catch(ParserConfigurationException e){
            _logger.log(Level.SEVERE,
                "synchronization.parser_config_exception", 
                            e.getLocalizedMessage());
        }
        catch(SAXParseException e){
            _logger.log(Level.SEVERE,
                "synchronization.sax_parse_exception", 
                new Object[] {new Integer(e.getLineNumber()), e.getSystemId(), 
                                e.getLocalizedMessage()});
        }
        catch(SAXException e){
            Exception x = e.getException ();
            ((x == null) ? e : x).printStackTrace ();
        }
        return doc;
    }

    int getSyncCount() {
        return maps.getLength();
    }

    String getAttrInternal(int i, String attr) {

        Element mapping = (Element)maps.item(i);

        String val = mapping.getAttribute(attr).trim();
        return val;
    }

    String getNextSource(int i) {
        return getAttrInternal(i, "source");
    }


    String getNextBaseDir(int i) {
        return getAttrInternal(i, "basedir");
    }

    String getNextSrcTimestampFile(int i) {
        return getAttrInternal(i, "src-timestamp-file");
    }

    String getNextDestTimestampFile(int i) {
        return getAttrInternal(i, "dest-timestamp-file");
    }

    String getNextDestination(int i) {
        return getAttrInternal(i, "destination");
    }    

    String getNextExclude(int i) {
        return getAttrInternal(i, "exclude");
    }    

    String getNextGCEnabled(int i) {
        return getAttrInternal(i, "gc-enabled");
    }    

    String getNextShallowCopyEnabled(int i) {
        return getAttrInternal(i, "shallow-copy-enabled");
    }    

    int getNextTimestampType(int i) {
        Element mapping = (Element)maps.item(i);

        String val = mapping.getAttribute("timestamp-type").trim();
        if ( val.compareTo("none") == 0 )
            return SynchronizationRequest.TIMESTAMP_NONE;
        else if ( val.compareTo("modification-time") == 0 )
            return SynchronizationRequest.TIMESTAMP_MODIFICATION_TIME;
        else if ( val.compareTo("modified-since") == 0 )
            return SynchronizationRequest.TIMESTAMP_MODIFIED_SINCE;
        else if ( val.compareTo("file") == 0 )
            return SynchronizationRequest.TIMESTAMP_FILE;
        else
            return -1; //error condition
    }

    List getListInternal(int i, String element, String attr) {

        List list = new ArrayList();

        // mapping elements
        Element mapping = (Element)maps.item(i);

        NodeList patterns = mapping.getElementsByTagName(element);

        int length = patterns.getLength();
        for (int idx=0; idx<length; idx++) {
            Element e = (Element)patterns.item(idx);

            String val = e.getAttribute(attr);
            list.add(val.trim());
        }

        return list;
    }

    List getExcludePatternList(int i) {
        return getListInternal(i, "exclude-pattern", "regular-expression");
    }

    List getIncludePatternList(int i) {
        return getListInternal(i,"include-pattern", "regular-expression"); 
    }

    List getClientRepositoryInfoList(int i) {
        return getListInternal(i,"client-repository-info", "path"); 
    }

    /**
     * Returns file names for each paths found in the client repository list.
     *
     * @param  list  client repository info paths defined in the meta data
     * @param  server name of the current server
     * @param  env  environment properties object
     * 
     * @return  a set containing all file names for the paths
     */
    Set processClientRepositoryInfo(List list, String server, Properties env) {

        HashSet criSet = new HashSet();

        if (list != null) {
            int cnt = list.size();

            for (int i=0; i<cnt; i++) {

                // process the tokens
                String f = TextProcess.tokenizeConfig((String)list.get(i), 
                                                    server, env); 
                File dir = new File(f);

                // list of files under the given path
                String[] fileNames = dir.list();

                if (fileNames != null) {

                    // adds the array to the set
                    for (int j=0; j<fileNames.length; j++) {
                        criSet.add(fileNames[j]);
                    }
                }
            }
        }

        return criSet;
    }

    public SynchronizationRequest [] getSyncRequests() {

        SynchronizationRequest[] requests = 
            new SynchronizationRequest[getSyncCount()];
        BufferedReader is = null;
        long modifiedTime = 0;

        for ( int i =0; i < getSyncCount(); i++ ) {
            modifiedTime = 0;
            String destTimestampFile = null;
            Properties tempEnv = new Properties();

            try {
                // converts the file path to the cached timestamp file
                destTimestampFile = TextProcess.tokenizeConfig(
                            getNextDestTimestampFile(i), serverName, tempEnv);

                if ( destTimestampFile != null ) {
                    is = new BufferedReader(
                        new FileReader( new File (destTimestampFile)));
                    modifiedTime = Long.parseLong(is.readLine());
                    is.close();
                    is = null;
                }
            } catch ( Exception e ) {
                _logger.log(Level.FINE,
                    "synchronization.timestamp_exception", 
                    new Object[] {destTimestampFile, e.getLocalizedMessage()});
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (Exception e) { }
                }
            }

            requests[i] = new SynchronizationRequest(getNextSource(i),
                                getNextDestination(i),
                                modifiedTime, 
                                getNextTimestampType(i),
                                getNextSrcTimestampFile(i));

            // adds all env keys from the temp obj
            Properties env = requests[i].getEnvironmentProperties();
            env.putAll(tempEnv);

            // sets the server instance name 
            requests[i].setServerName(serverName);

            // sets the timestamp file on the cache
            requests[i].setCacheTimestampFile(destTimestampFile);

            // sets the base directory for this request
            String base =
                TextProcess.tokenizeConfig(getNextBaseDir(i), serverName, env); 
            requests[i].setBaseDirectory(base);

            // sets the exclude flag
            String exclude = getNextExclude(i);
            requests[i].setExclude( Boolean.valueOf(exclude).booleanValue() );

            // sets the gcEnabled flag
            String gcEnabled = getNextGCEnabled(i);
            boolean enabled = Boolean.valueOf(gcEnabled).booleanValue(); 
            requests[i].setGCEnabled(enabled);

            // exclude pattern list
            List eList = getExcludePatternList(i);
            requests[i].addToExcludePatternList(eList);

            // sets the shallow-copy-enabled
            String sCopyEnabled = getNextShallowCopyEnabled(i);
            boolean scEnabled = Boolean.valueOf(sCopyEnabled).booleanValue(); 
            requests[i].setShallowCopyEnabled(scEnabled);

            // include pattern list
            List iList = getIncludePatternList(i);
            requests[i].addToIncludePatternList(iList);

            // client repository info list
            List criList = getClientRepositoryInfoList(i);
            if ((criList != null) && (criList.size() > 0)) {

                Set criSet = 
                    processClientRepositoryInfo(criList, serverName, env);

                if ((criSet != null) && (criSet.size() > 0)) {
                    requests[i].addToClientRepositoryInfo(criSet);
                }
            }
        }
        return requests;
    }
}
