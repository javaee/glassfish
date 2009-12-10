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
 */
package com.sun.enterprise.admin.cli.util;

import java.io.IOException;
import java.net.URL;
import java.net.MalformedURLException;
import java.net.URLConnection;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.KeyManagementException;

import com.sun.enterprise.universal.GFBase64Encoder;

import javax.net.ssl.*;

public final class HttpConnectorAddress
{
    static final String HTTP_CONNECTOR = "http";
    static final String HTTPS_CONNECTOR = "https";
    public static final String  AUTHORIZATION_KEY     = "Authorization";
    private static final String AUTHORIZATION_TYPE = "Basic ";

    private String host;
    private int    port;
    private String path;
    private boolean secure;
    private AuthenticationInfo  authInfo;

    
    public HttpConnectorAddress() {
    }

    public HttpConnectorAddress(String host, int port){
        this(host, port, false);
    }
    
    /**
	 * construct an address which indicates the host, port and
	 * security attributes desired.
	 * @param host a host address
	 * @param port a port number
	 * @param secure a boolean indication of whether the connection should be
         *  secure (i.e. confidential) or not
	 */
    public HttpConnectorAddress(String host, int port, boolean secure){
        this(host, port, secure, null);
    }

    public HttpConnectorAddress(String host, int port, boolean secure, String path) {
        this.host = host;
        this.port = port;
        this.secure = secure;
        this.path = path;
  }


    /**
	 * Open a connection using the reciever and the given path
	 * @param path the path to the required resource (path here is
	 * the portion after the <code>hostname:port</code> portion of a URL)
     * @return a connection to the required resource. The
     * connection returned may be a sub-class of
     * <code>URLConnection</code> including
     * <code>HttpsURLConnection</code>. If the sub-class is a
	 * <code>HttpsURLConnection</code> then this connection will
	 * accept any certificate from any server where the server's
	 * name matches the host name of this object. Specifically we
	 * allows the certificate <em>not</em> to contain the name of
	 * the server. This is a potential security hole, but is also a
	 * usability enhancement.
	 * @throws IOException if there's a problem in connecting to the
	 * resource
	 */
    public URLConnection openConnection(String path) throws IOException {
        configureSSL();
        if (path == null || path.trim().length() == 0)
            path = this.path;
        return this.openConnection(this.toURL(path));
    }

    private void configureSSL() throws IOException {
        if (secure) {
            try {
                SSLContext sc = SSLContext.getInstance("TLS");
                TrustManager[] tms = {new AsadminTrustManager()};
                sc.init(null, tms, new SecureRandom());
                HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
                HttpsURLConnection.setDefaultHostnameVerifier(new BasicHostnameVerifier(this.host));
            } catch (NoSuchAlgorithmException e) {
                throw new IOException(e);
            } catch (KeyManagementException ee) {
                throw new IOException(ee);
            }
        }
    }


    /**
	 * get the protocol prefix to be used for a connection for the
	 * receiver
	 * @return the protocol prefix - one of <code>http</code> or
	 *<code>https</code> depending upon the security setting.
	 */
    public String getConnectorType() {
        return this.isSecure() ? HTTPS_CONNECTOR : HTTP_CONNECTOR;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public AuthenticationInfo getAuthenticationInfo() {
        return authInfo;
    }

    public void setAuthenticationInfo(AuthenticationInfo authInfo) {
        this.authInfo = authInfo;
    }

	  /**
	   * Set the security attibute
	   */
    public void setSecure(boolean secure){
        this.secure = secure;
    }
  

	/**
	 * Indicate if the reciever represents a secure address
	 */
    public boolean isSecure(){
        return secure;
    }

    public URL toURL(String path) throws MalformedURLException{
        return new URL(this.asURLSpec(path));
    }
  
    private final String getUser(){
        return authInfo != null ? authInfo.getUser() : "";
    }

    private final String getPassword(){
        return authInfo != null ? authInfo.getPassword() : "";
    }

  	  /**
	   * Return a string which can be used as the specification to
	   * form an URL.
	   * @return a string which can be used as the specification to
	   *form an URL. This string is in the form of
	   *<code>&gt;protocol>://&gt;host>:&gtport>/</code> with the
	   *appropriate substitutions
	   */
    private final String asURLSpec(String path){
        return this.getConnectorType()
        +"://"+this.getAuthority()
        +(path != null? path : "");
    }

	/**
	 * Return the authority portion of the URL spec
	 */
    private final String getAuthority(){
        return this.getHost() + ":" + this.getPort();
    }


    private final URLConnection openConnection(URL url) throws IOException    {
        return this.setOptions(this.makeConnection(url));
    }

    private final URLConnection makeConnection(URL url) throws IOException {
        return ( url.openConnection() );
    }

    private final URLConnection setOptions(URLConnection uc) {
        uc.setDoOutput(true);
        uc.setUseCaches(false);
        uc.setRequestProperty("Content-type", "application/octet-stream");
        uc.setRequestProperty("Connection", "Keep-Alive"); 
        return this.setAuthentication(uc);
    }

    private final URLConnection setAuthentication(URLConnection uc){
        if (authInfo != null) {
            uc.setRequestProperty(AUTHORIZATION_KEY, this.getBasicAuthString());
        }
        return uc;
    }

    public final String getBasicAuthString(){
    /* taking care of the descripancies in the Base64Encoder, for very
       large lengths of passwords and/or usernames.
       Abhijit did the analysis and as per his suggestion, replacing
	   a newline in Base64 encoded String by newline followed by a space
	   should work for any length of password, independent of the
	   web server buffer length. That investigation is still on, but
	   in the meanwhile, it was found that the replacement of newline
	   character with empty string "" works. Hence implementing the same.
	   Date: 10/10/2003.
	*/
        String cs = null, user = this.getUser(), pass = this.getPassword();
        String up = (user == null) ? "" : user;
        String pp = (pass == null) ? "" : pass;
        cs = up + ":" + pp;
        String enc = this.getBase64Encoded(cs);
        enc = enc.replaceAll(System.getProperty("line.separator"), "");
        return ( AUTHORIZATION_TYPE + enc );
    }
  
    private final String getBase64Encoded(String clearString) {
        return new GFBase64Encoder().encode(clearString.getBytes());
    }

    private static class BasicHostnameVerifier implements HostnameVerifier {
        private final String host;
        public BasicHostnameVerifier(String host) {
            if (host == null)
                throw new IllegalArgumentException("null host");
            this.host = host;
        }

        public boolean verify(String s, SSLSession sslSession) {
            return host.equals(s);
        }
    }
}
