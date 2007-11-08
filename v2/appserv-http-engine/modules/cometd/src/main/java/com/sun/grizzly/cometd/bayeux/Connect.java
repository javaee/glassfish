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

package com.sun.grizzly.cometd.bayeux;

import org.apache.tomcat.util.http.FastHttpDateFormat;

/**
 * Bayeux Connect implementation. 
 * See http://svn.xantus.org/shortbus/trunk/bayeux/protocol.txt for the technical
 * details.
 *
 *	// here's what the client then POST's:
 *
 *	//-----------------
 *	// CLIENT -> SERVER
 *	//-----------------
 *	[
 *		{
 *			"channel":	"/meta/connect",
 *			"clientId":	"SOME_UNIQUE_CLIENT_ID",
 *			"connectionType": "iframe",
 *			// optional
 *			"authToken":"SOME_NONCE_PREVIOUSLY_PROVIDED_BY_SERVER"
 *		}
 *		// , ...
 *	]
 *
 *	// NOTE: data should be POSTed with an encoding of
 *	// application/x-www-form-urlencoded, and the preceeding payload is
 *	// expected to be stored in the "message" parameter
 *
 *	// the server now replies with the preamble followed by any number of
 *	// messages encoded in the tunnel-specific envelope:
 *
 *	//-----------------
 *	// SERVER -> CLIENT
 *	//-----------------
 *
 *	<!-- begin preamble -->
 *	<html>
 *		<head>
 *			<title>Comet -- cleaning up web development</title>
 *			<script type="text/javascript">
 *				window.parent.cometd.deliver([
 *					{
 *						// user-sent data
 *						"channel":		"/meta/connect",
 *						"successful":	true,
 *						"error":		"",
 *						"connectionId":	"/meta/connections/26",
 *						"clientId":		"SOME_UNIQUE_CLIENT_ID",
 *						"timestamp":	"TimeAtServer",
 *						// optional
 *						"authToken":	"SOME_NONCE_THAT_NEEDS_TO_BE_PROVIDED_SUBSEQUENTLY"
 *					}
 *				]);
 *			</script>
 *		</head>
 *		<body>
 *	<!-- end preamble -->
 *	<!-- begin envelope -->
 *			<script type="text/javascript">
 *				window.parent.cometd.deliver([
 *					{
 *						// user-sent data
 *						"data": {
 *							"someField":	["some", "random", "values"],
 *						},
 *						// the usual message meta-data
 *						"channel":		"/originating/channel",
 *						// event ID
 *						"id":			"slkjdlkj32",
 *						"timestamp":	"TimeAtServer",
 *						// optional meta-data
 *						"authToken":	"SOME_NONCE_THAT_NEEDS_TO_BE_PROVIDED_SUBSEQUENTLY"
 *					},
 *					{
 *						"data": {
 *							"blah blah":	["more", "random", "values"],
 *						},
 *						// the usual message meta-data
 *						"channel":		"/originating/channel",
 *						// event ID
 *						"id":			"slkjdlkj31",
 *						"timestamp":	"TimeAtServer",
 *						"authToken":	"SOME_NONCE_THAT_NEEDS_TO_BE_PROVIDED_SUBSEQUENTLY"
 *					}
 *					// , ...
 *				]);
 *			</script>
 *			<br><!-- insert 2K of whitespace here -->
 *	<!-- end envelope -->
 *	...
 *	<!-- begin signoff -->
 *			<script type="text/javascript">
 *				window.parent.cometd.tunnelCollapse();
 *			</script>
 *		</body>
 *	</html>
 *	<!-- end signoff -->
 *  
 * @author Jeanfrancois Arcand
 */
public class Connect extends VerbBase{
    
    public final static String HTML_HEADER = 
        "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\"" +
        "\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">" +
        "<html>\n<head>\n<title>Grizzly Cometd</title>" +
        "\n<script type=\"text/javascript\">\nwindow.parent.cometd.deliver(";
            
            
    public final static String DATA_WRAPPER_HEADER =
        ");\n</script>\n</head>\n<body>\n\n<script type=\"text/javascript\">" +
        "window.parent.cometd.deliver([\n{";
            
            
    public final static String DATA_WRAPPER_FOOTER =            
        "</script>\n<br>\n<script type=\"text/javascript\">\n"
      + "window.parent.cometd.tunnelCollapse();\n</script>\n</body>\n</html>";
    
    
    
    public final static String META_CONNECTIONS="/meta/connections/";
    
    
    protected String clientId;
    
    protected String connectionType;
    
    protected String timestamp = FastHttpDateFormat.getCurrentDate();    
    
    public Connect() {
        type = Verb.CONNECT;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getConnectionType() {
        return connectionType;
    }

    public void setConnectionType(String connectionType) {
        this.connectionType = connectionType;
    }


    public String toJSON() {            
        return "[{" 
                + "\"timestamp\":\"" + FastHttpDateFormat.getCurrentDate() + "\","
                + "\"error\":\"" + error + "\","
                + "\"successful\":" + successful + ","                
                + "\"channel\":\"" + channel + "\""
                + "}]\n" ;   
    }

}
