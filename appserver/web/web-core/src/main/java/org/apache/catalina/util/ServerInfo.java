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

package org.apache.catalina.util;


/**
 * Simple utility module to make it easy to plug in the server identifier
 * when integrating Tomcat.
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.3 $ $Date: 2005/12/08 01:28:20 $
 */

public class ServerInfo {


    // ------------------------------------------------------- Static Variables


    /**
     * The server information string used for logging
     */
    private static String serverInfo = null;

    /**
     * The public server information string that is exposed in
     * container-generated error pages and as the value of the "Server"
     * HTTP response header
     */
    private static String publicServerInfo = null;

    static {

        // BEGIN S1AS 5022949
        /*
        try {
            InputStream is = ServerInfo.class.getResourceAsStream
                ("/org/apache/catalina/util/ServerInfo.properties");
            Properties props = new Properties();
            props.load(is);
            is.close();
            serverInfo = props.getProperty("server.info");
        } catch (Throwable t) {
            ;
        }
        if (serverInfo == null)
            serverInfo = "Apache Tomcat";
        */
        // END S1AS 5022949
    }


    // --------------------------------------------------------- Public Methods


    // START PWC 5022949
    public static void setServerInfo(String info) {
        serverInfo = info;
    }
    // END PWC 5022949

    /**
     * Return the server identification for this version of Tomcat.
     */
    public static String getServerInfo() {
        return (serverInfo);
    }

    public static void setPublicServerInfo(String info) {
        publicServerInfo = info;
    }

    public static String getPublicServerInfo() {
        return publicServerInfo;
    }

}
