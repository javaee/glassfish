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

import com.sun.enterprise.util.io.FileUtils;

import java.io.File;
import java.io.Serializable;
import java.util.Properties;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.Map;
import java.util.Map.Entry;
import java.util.HashSet;
import javax.servlet.ServletRequest;
import java.net.URLEncoder;
import com.sun.enterprise.util.i18n.StringManager;
import java.io.UnsupportedEncodingException;

/**
 * Class SynchronizationRequest
 */
public class SynchronizationRequest implements Serializable
{

    public static final int TIMESTAMP_FILE = 0;
    public static final int TIMESTAMP_MODIFICATION_TIME = 1;
    public static final int TIMESTAMP_NONE = 2;
    public static final int TIMESTAMP_MODIFIED_SINCE = 3;

    private static final transient StringManager _localStrMgr = 
        StringManager.getManager(SynchronizationRequest.class);

    private String _metaFileName;    
    private String _targetDirectory;
    private long _timestamp;
    private int _timestampType;
    private String _timestampFileName;
    private String _baseDirectory;

    /** name of the server instance (client) */
    private String _serverName; 

    /** time stamp file in repository cache */
    private String _cacheTimestampFile;

    /** holds environment values for tokens */
    private Properties _env;

    /** true if request should exclude un-associated apps */
    private boolean _exclude = false;

    /** true if GC is enabled */
    private boolean _gcEnabled = false;

    /** inventory for this request */
    private List _inventory;

    /** exclude regular expression pattern list */
    private List _excludePatternList;

    /** include regular expression list - overwrites all exclude patterns */
    private List _includePatternList;

    /** true if request should shallow copy a dir */
    private boolean _shallowCopyEnabled = false;

    /** meta information about repository cache */
    private Set _clientRepositoryInfo;

    /** true when client repository info is included in the request */
    private boolean _clientRepositoryInfoSent = false;

    /**
     * Constructor SynchronizationRequest
     *
     * @param name
     * @param type
     * @param targetDirectory
     * @param timestamp
     * @param timestampType
     * @param timestampFileName
     */
    public SynchronizationRequest(String name, long timestamp,
        int timestampType) {
        this(name, ".", timestamp, timestampType, null);
    }    
     
    public SynchronizationRequest(String name,
                                  String targetDirectory, long timestamp,
                                  int timestampType, String timestampFileName) {

        init(name,targetDirectory,timestamp,timestampType,timestampFileName);
    }

    /**
     * Initializes the request. This is used by both constructors.
     */
    private void init(String name, String targetDirectory, long timestamp,
                                  int timestampType, String timestampFileName) {

        _metaFileName = name;            
        _targetDirectory = targetDirectory;
        _timestamp = timestamp;
        _timestampType = timestampType;
        _env = new Properties();

        assert((_timestampType == TIMESTAMP_FILE)
               || (_timestampType == TIMESTAMP_MODIFICATION_TIME)
               || (_timestampType == TIMESTAMP_NONE)
               || (_timestampType == TIMESTAMP_MODIFIED_SINCE));
        
        if (_timestampType != TIMESTAMP_FILE) {
            assert(timestampFileName == null);
        }
        
        _timestampFileName = timestampFileName;
        _inventory = new ArrayList();
        _excludePatternList = new ArrayList();
        _includePatternList = new ArrayList();
        _clientRepositoryInfo = new HashSet();
    }

    /**
     * Method getFileName
     *
     * @return
     */
    public String getFileName() {
        String fn=TextProcess.tokenizeConfig(_metaFileName, _serverName, _env);
        return processPath(fn);
    }

    /**
     * We want to ensure that on UNIX systems, all windows specific 
     * path characters (i.e. '\' is replaced with '/').
     */
    private String processPath(String s) {
        if (s != null) {
            return FileUtils.makeForwardSlashes(s);            
        }
        return s;
    }

    public String getMetaFileName() {
        return _metaFileName;
    }

    void setMetaFileName(String file) {
        _metaFileName = file;
    }

    public Properties getEnvironmentProperties() {
        return _env;
    }

    void addEnvironmentProperty(String key, String value) {

        assert(key != null);
        assert(value != null);

        _env.put(key, value);
    }

    public boolean isExclude() {
        return _exclude;
    }

    void setExclude(boolean b) {
        this._exclude = b;
    }

    /**
     * Method getFile
     *
     * @return
     */
    public File getFile() {
        return new File( getFileName() );
    }

    /**
     * Method getTargetDirectory
     *
     * @return
     */
    public String getTargetDirectory() {
        String targetDir =
            TextProcess.tokenizeConfig(_targetDirectory, _serverName, _env);
        return processPath(targetDir);
    }

    void setTargetDirectory(String targetDir) {
        _targetDirectory = targetDir;
    }

    public String getBaseDirectory() {
        String baseDir = 
            TextProcess.tokenizeConfig(_baseDirectory, _serverName, _env);
        return processPath(baseDir);
    }

    public void setBaseDirectory(String baseDir) {
        _baseDirectory = baseDir;
    }

    /**
     * Method getTimestamp
     *
     * @return
     */
    public long getTimestamp() {
        return _timestamp;
    }

    /**
     * Method setTimestamp
     *
     * @param timestamp
     */
    public void setTimestamp(long timestamp) {
        _timestamp = timestamp;
    }

    /**
     * Method getTimestampType
     *
     * @return
     */
    public int getTimestampType() {
        return _timestampType;
    }

    /**
     * Sets the timestamp type.
     *
     * @param  type  timestamp type
     */
    public void setTimestampType(int type) {
        _timestampType = type;
    }

    /**
     * Method getTimestampFileName
     *
     * @return
     */
    public String getTimestampFileName() {
        String tsFile = 
            TextProcess.tokenizeConfig(_timestampFileName,_serverName,_env);
        return processPath(tsFile);
    }

    void setTimestampFileName(String fileName) {
        this._timestampFileName = fileName;
    }

    /**
     * Method getTimestampFile
     *
     * @return
     */
    public File getTimestampFile() {
        String file = 
            TextProcess.tokenizeConfig(_timestampFileName,_serverName,_env);
        return new File( processPath(file) );
    }

    public String getServerName() {
        return _serverName;
    }

    public void setServerName(String serverName) {
        _serverName = serverName;
    }

    public File getCacheTimestampFile() {
        String file = 
            TextProcess.tokenizeConfig(_cacheTimestampFile, _serverName, _env);
        return new File( processPath(file) );
    }

    void setCacheTimestampFile(String tsfile) {
        _cacheTimestampFile = tsfile;
    }

    /**
     * Returns true if the repository cleaner thread is going to 
     * garbage collect stale files or directories from this dir.
     * 
     * @return  true if GC is enabled for this request
     */
    public boolean isGCEnabled() {
        return _gcEnabled;
    }

    /**
     * Sets the GC Enabled flag for this request.
     *
     * @param  gcEnabled  true if cleaner thread will garbage collect this dir
     */
    void setGCEnabled(boolean gcEnabled) {

        if (gcEnabled == true) {
            //if ( (_metaFileName != null) && (!_metaFileName.endsWith("/")) ) {
            //    String msg = 
            //        _localStrMgr.getString("notADirectory", _metaFileName);
            //    throw new RuntimeException(msg);
            //}
        }
        _gcEnabled = gcEnabled;
    }

    /**
     * Returns the inventory list for this request. This contains list of
     * relative file paths currently available in the central repository.
     *
     * @return  inventory for this request
     */
    public List getInventory() {
        return _inventory;
    }

    /**
     * Sets the inventory for this request.
     *
     * @param  inventory  inventory list for this request
     */
    public void setInventory(List inventory) {
        _inventory = inventory;
    }

    /**
     * Returns the regular expression exclude list for this request. 
     * Any file or directory names matching to these expressions will
     * be excluded from the request.
     *
     * @return    regular expression exclude list
     */
    public List getExcludePatternList() {
        return _excludePatternList;
    }

    /**
     * Sets the regular expression exclude list.
     *
     * @param  pattern  regular expression exclude list
     */
    void addToExcludePatternList(List pattern) {
        if (pattern != null) {
            _excludePatternList.addAll(pattern);
        } 
    }

    /**
     * Returns the regular expression include list for this request. 
     * Any file or directory names matching to these expressions 
     * will NOT be excluded from the request. Include list over writes 
     * all exclude patterns.
     *
     * @return    regular expression include list
     */
    public List getIncludePatternList() {

        int length = _includePatternList.size();

        // tokenize the include patterns
        for (int i=0; i<length; i++) {
            String p = TextProcess.tokenizeConfig(
                (String)_includePatternList.get(i), _serverName, _env);
            _includePatternList.set(i, p);
        }
        return _includePatternList;
    }

    /**
     * Sets the regular expression include list.
     *
     * @param  pattern  regular expression include list
     */
    void addToIncludePatternList(List pattern) {
        if (pattern != null) {
            _includePatternList.addAll(pattern);
        } 
    }

    /**
     * Returns true if shallow copy is enabled for this request. 
     * Shallow copy should be enabled only for a directory. When enabled,
     * only files under the directory will be synchronized. All directories
     * (and files under them) will be ignored.
     *
     * @return  true if shallow copy is enabled for a request
     */
    public boolean isShallowCopyEnabled() {
        return _shallowCopyEnabled;
    }

    /**
     * Sets the shallow copy enabled flag for a request.
     *
     * @param   tf   true if shallow copy is enabled
     */
    void setShallowCopyEnabled(boolean tf) {
        _shallowCopyEnabled = tf;
    }

    /**
     * Adds to the client repository info. This map contains information 
     * about the client repository.
     *
     * @param  crInfo client repository info
     */
    void addToClientRepositoryInfo(Set crInfo) {
        if (crInfo != null) {
            _clientRepositoryInfo.addAll(crInfo);
            _clientRepositoryInfoSent = true;

        }
    }

    /**
     * Returns the client repository info set.
     *
     * <p> Example usage: 
     * Contians application names found in the repository cache.
     *
     * @return  client repository info
     */
    public Set getClientRepositoryInfo() {
        return _clientRepositoryInfo;
    }

    /**
     * Returns true if client repository info is sent for this request.
     *
     * @return  true if client repository info is sent for this request
     */
    public boolean isClientRepositoryInfoSent() {
        return _clientRepositoryInfoSent;
    }

    /**
     * Deserializes a list from servlet request. It looks for listName_count
     * in the servlet request. All list entries are expected to be in 
     * listName0 format. This assumes that there are no gaps in the list and 
     * all entries are java.lang.String.
     * 
     * @param  listName  name of the list 
     * @param  req       servlet request
     * @param  c         collection object (list, set)
     *
     * @throws UnsupportedEncodingException  if an error during decoding
     */
    private void deserializeCollection(String listName, ServletRequest req, 
            Collection c) throws UnsupportedEncodingException {

        String count = req.getParameter(listName+COUNT);

        if (count != null) {
            int cnt = Integer.parseInt(count);
            for (int i=0; i<cnt; i++) {
                String value = req.getParameter(listName+i);
                if (value == null) {
                    String msg = 
                        _localStrMgr.getString("missingListElement",i,listName);
                    throw new IllegalArgumentException(msg);
                }

                c.add(value);
            }
        } 
    }

    /**
     * Serializes a collection (list, set) into HTTP request url format. 
     * It adds "&listName_count=<size>" to identify the size of the collection.
     * Each entries are added with "&listName<index>=<value>" format. 
     *
     * @param  c  collection to be serialized
     * @param  listName  name of the list
     *
     * @throws UnsupportedEncodingException  if an error during encoding
     */
    private String serializeCollection(Collection c, String listName) 
            throws UnsupportedEncodingException {

        StringBuffer sbuf = new StringBuffer();
        
        if (c != null) {
            int cnt = 0;
            Iterator itr = c.iterator();

            while (itr.hasNext()) {
                sbuf.append(AND + encode(listName+cnt)
                    + EQUAL + encode((String)itr.next()) );
                cnt++;
            }
            sbuf.append(AND + encode(listName+COUNT)
                + EQUAL + cnt);
        }

        return sbuf.toString();
    }

    /**
     * Constructs a Map (example, java.util.Properties) object from 
     * ServletRequest. It first looks for the size of the map entries 
     * in <mapName>_count format. If a size exists, then it looks for
     * <mapName>_key_<index> for all the keys and <mapName>_value_<index>
     * for all the values.
     *
     * All keys and values are expected to be java.lang.String.
     *
     * @param  mapName  name of the map variable
     * @param  req      servlet request
     * @param  m        map object
     *
     * @throws UnsupportedEncodingException  if an error during decoding
     */
    private void deserializeMap(String mapName, ServletRequest req, Map m) 
            throws UnsupportedEncodingException {

        String count = req.getParameter(mapName+COUNT);
        if (count != null) {
            int cnt = Integer.parseInt(count);
            for (int i=0; i<cnt; i++) {
                String key = req.getParameter(mapName+KEY+i);
                String val = req.getParameter(mapName+VALUE+i);

                if ((key == null) || (val == null)) {
                    String msg = 
                        _localStrMgr.getString("missingListElement",i,mapName);
                    throw new IllegalArgumentException(msg);
                }
                m.put(key,val);
            }
        } 
    }

    /**
     * Serializes the given map into HTTP servlet request format. 
     * Map size is converted as <mapName>_count=<size>. Map keys are
     * converted as <mapName>_key_<index>=<key> and values are 
     * converted as <mapName>_value_<index>=<value>.
     * 
     * @param  map  map to be serialized
     * @param  mapName  name of the map variable
     *
     * @throws UnsupportedEncodingException  if an error during encoding
     */
    private String serializeMap(Map map, String mapName)
            throws UnsupportedEncodingException {

        StringBuffer sbuf = new StringBuffer();
        
        if (map != null) {
            int cnt = 0;
            Set eSet = map.entrySet();
            Iterator itr = eSet.iterator();

            while (itr.hasNext()) {
                Map.Entry e = (Map.Entry) itr.next();
                sbuf.append(AND + encode(mapName+KEY+cnt)
                    + EQUAL + encode((String)e.getKey()) );   
                sbuf.append(AND + encode(mapName+VALUE+cnt)
                    + EQUAL + encode((String)e.getValue()) );   
                cnt++;
            }
            sbuf.append(AND + encode(mapName+COUNT) + EQUAL + cnt);
        }

        return sbuf.toString();
    }

    /**
     * Serializes this request to HTTP servlet request encoded format. 
     * This method must be updated to reflect future changes to this class.
     *
     * @return  serialized request in HTTP servlet request encoded format
     *
     * @throws UnsupportedEncodingException  if an error during encoding
     */
    public String getPostData() throws UnsupportedEncodingException {
        StringBuffer sbuf = new StringBuffer();

        // action = synchronize
        sbuf.append(ACTION + EQUAL + SYNCHRONIZE);

        if (_metaFileName != null) {
            sbuf.append("&_metaFileName=" + encode(_metaFileName));
        }
        if (_targetDirectory != null) {
            sbuf.append("&_targetDirectory=" + encode(_targetDirectory));
        }
        // long
        sbuf.append("&_timestamp=" + _timestamp);
        // int
        sbuf.append("&_timestampType=" + _timestampType);

        if (_timestampFileName != null) {
            sbuf.append("&_timestampFileName=" + encode(_timestampFileName));
        }
        if (_baseDirectory != null) {
            sbuf.append("&_baseDirectory=" + encode(_baseDirectory));
        }
        if (_serverName != null) {
            sbuf.append("&_serverName=" + encode(_serverName)); 
        }
        if (_cacheTimestampFile != null) {
            sbuf.append("&_cacheTimestampFile=" + encode(_cacheTimestampFile));
        }

        // env
        sbuf.append(serializeMap(_env, "_env"));

        // boolean
        sbuf.append("&_exclude=" + _exclude);
        sbuf.append("&_gcEnabled=" + _gcEnabled);

        // list
        sbuf.append(serializeCollection(_inventory, "_inventory"));
        sbuf.append(serializeCollection(_excludePatternList, 
                        "_excludePatternList"));
        sbuf.append(serializeCollection(_includePatternList, 
                        "_includePatternList"));
        // boolean
        sbuf.append("&_shallowCopyEnabled=" + _shallowCopyEnabled);

        // set
        sbuf.append(serializeCollection(_clientRepositoryInfo, 
                        "_clientRepositoryInfo"));

        // boolean
        sbuf.append("&_clientRepositoryInfoSent=" +_clientRepositoryInfoSent); 

        return sbuf.toString();
    }

    /**
     * Encodes a string with null check.
     * 
     * @param  arg  string to be encoded
     * @return encoded string
     *
     * @throws UnsupportedEncodingException  if an error during encoding
     */
    private String encode(String arg) throws UnsupportedEncodingException {

        String encoded;

        if (arg != null) {
            encoded = URLEncoder.encode(arg, UTF8);
        } else {
            encoded = arg;
        }

        return encoded;
    }

    /**
     * Constructs a synchronization request from a servlet request.
     * This method must be updated to reflect future changes to this class.
     * 
     * @param  req  servlet request
     *
     * @throws UnsupportedEncodingException  if an error during decoding
     */
    public SynchronizationRequest(ServletRequest req) 
            throws UnsupportedEncodingException {

        String metaFileName = req.getParameter("_metaFileName");
        String targetDirectory = req.getParameter("_targetDirectory");

        // long
        String timestamp = req.getParameter("_timestamp");
        long ts = 0;
        if (timestamp != null) {
            ts = Long.parseLong(timestamp);
        }

        // int
        String timestampType = req.getParameter("_timestampType");
        int tsType = 3;
        if (timestampType != null) {
            tsType = Integer.parseInt(timestampType); 
        }
        String timestampFileName=req.getParameter("_timestampFileName");

        init(metaFileName, targetDirectory, ts, tsType, timestampFileName);

        _baseDirectory = req.getParameter("_baseDirectory");
        _serverName = req.getParameter("_serverName");
        _cacheTimestampFile = req.getParameter("_cacheTimestampFile");

        // properties
        deserializeMap("_env", req, _env);

        // boolean
        String exclude = req.getParameter("_exclude");
        if (exclude != null) {
            _exclude = Boolean.parseBoolean(exclude);
        }

        String gcEnabled = req.getParameter("_gcEnabled");
        if (gcEnabled != null) {
            _gcEnabled = Boolean.parseBoolean(gcEnabled);
        }

        // list
        deserializeCollection("_inventory", req, _inventory); 
        deserializeCollection("_excludePatternList", req, _excludePatternList); 
        deserializeCollection("_includePatternList", req, _includePatternList); 

        // boolean
        String shallowCopyEnabled = req.getParameter("_shallowCopyEnabled");
        if (shallowCopyEnabled != null) {
            _shallowCopyEnabled = Boolean.parseBoolean(shallowCopyEnabled);
        }

        // set
        deserializeCollection("_clientRepositoryInfo", req,
                            _clientRepositoryInfo); 

        // boolean
        String clientRepositoryInfoSent = 
            req.getParameter("_clientRepositoryInfoSent");
        if (clientRepositoryInfoSent != null) {
            _clientRepositoryInfoSent = 
                Boolean.parseBoolean(clientRepositoryInfoSent);
        }
    }

    // ---- SERIALIZER/DESERIALIZER CONSTANTS ----------------------
    private static final String COUNT        = "_count";
    private static final String KEY          = "_key_";
    private static final String VALUE        = "_value_";
    private static final String AND          = "&";
    private static final String EQUAL        = "=";
    public static final String ACTION        = "action";
    public static final String SYNCHRONIZE   = "synchronize";
    public static final String GET           = "get";
    public static final String UTF8          = "UTF-8";
}
