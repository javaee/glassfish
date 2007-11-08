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
 
package com.sun.enterprise.config.clientbeans;
public class ClientTags{ 
// Tags for Element client-container
static public final String CLIENT_CONTAINER = "client-container";
	static public final String SEND_PASSWORD = "send-password";
// Tags for Element target-server
static public final String TARGET_SERVER = "target-server";
	static public final String DESCRIPTION = "description";
	static public final String NAME = "name";
	static public final String ADDRESS = "address";
	static public final String PORT = "port";
// Tags for Element auth-realm
static public final String AUTH_REALM = "auth-realm";
	//static public final String NAME = "name";
	static public final String CLASSNAME = "classname";
// Tags for Element client-credential
static public final String CLIENT_CREDENTIAL = "client-credential";
	static public final String USER_NAME = "user-name";
	static public final String PASSWORD = "password";
	static public final String REALM = "realm";
// Tags for Element log-service
static public final String LOG_SERVICE = "log-service";
	static public final String FILE = "file";
	static public final String LEVEL = "level";
// Tags for Element message-security-config
static public final String MESSAGE_SECURITY_CONFIG = "message-security-config";
	static public final String AUTH_LAYER = "auth-layer";
	static public final String DEFAULT_PROVIDER = "default-provider";
	static public final String DEFAULT_CLIENT_PROVIDER = "default-client-provider";
// Tags for Element element-property
static public final String ELEMENT_PROPERTY = "element-property";
	//static public final String NAME = "name";
	static public final String VALUE = "value";
// Tags for Element provider-config
static public final String PROVIDER_CONFIG = "provider-config";
	static public final String PROVIDER_ID = "provider-id";
	static public final String PROVIDER_TYPE = "provider-type";
	static public final String CLASS_NAME = "class-name";
// Tags for Element request-policy
static public final String REQUEST_POLICY = "request-policy";
	static public final String AUTH_SOURCE = "auth-source";
	static public final String AUTH_RECIPIENT = "auth-recipient";
// Tags for Element response-policy
static public final String RESPONSE_POLICY = "response-policy";
	//static public final String AUTH_SOURCE = "auth-source";
	//static public final String AUTH_RECIPIENT = "auth-recipient";
// Tags for Element security
static public final String SECURITY = "security";
// Tags for Element ssl
static public final String SSL = "ssl";
	static public final String CERT_NICKNAME = "cert-nickname";
	static public final String SSL2_ENABLED = "ssl2-enabled";
	static public final String SSL2_CIPHERS = "ssl2-ciphers";
	static public final String SSL3_ENABLED = "ssl3-enabled";
	static public final String SSL3_TLS_CIPHERS = "ssl3-tls-ciphers";
	static public final String TLS_ENABLED = "tls-enabled";
	static public final String TLS_ROLLBACK_ENABLED = "tls-rollback-enabled";
// Tags for Element cert-db
static public final String CERT_DB = "cert-db";
	static public final String PATH = "path";
	//static public final String PASSWORD = "password";
	//static public final String DESCRIPTION = "description";
}
