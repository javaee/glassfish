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

package com.sun.security.auth.message.module;

import java.lang.StringBuffer;
import java.io.IOException;

import java.security.Principal;

import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.message.*;
import javax.security.auth.message.module.ServerAuthModule;
import javax.security.auth.message.callback.CallerPrincipalCallback;
import javax.security.auth.message.callback.GroupPrincipalCallback;
import javax.security.auth.Subject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ietf.jgss.Oid;
import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSManager;
import org.ietf.jgss.GSSName;

import org.apache.catalina.util.Base64;

import com.sun.security.jgss.GSSUtil;

/**
 * An implementation of this interface is used to validate received service 
 * request messages, and to secure service response messages.
 *
 * @version %I%, %G%
 * @see MessageInfo
 * @see Subject
 */
public class SPNEGOServerAuthModule implements ServerAuthModule {

    private static Logger logger = Logger.getLogger
        (SPNEGOServerAuthModule.class.getName());

    private static String DEBUG_OPTIONS_KEY ="debug";

    private static String ASSIGN_GROUPS_OPTIONS_KEY = 
        "assign.groups";

    private static String POLICY_CONTEXT_OPTIONS_KEY = 
        "javax.security.jacc.PolicyContext";

    public static final String AUTH_TYPE_INFO_KEY = 
        "javax.servlet.http.authType";

    private static String IS_MANDATORY_INFO_KEY = 
        "javax.security.auth.message.MessagePolicy.isMandatory";

    private static String AUTHORIZATION_HEADER = "authorization";

    private static String AUTHENTICATION_HEADER = "WWW-Authenticate";

    private static String NEGOTIATE = "Negotiate";

    private static String NTLM_INITIAL_TOKEN = "NTLMSSP";

    private static Class[] supportedMessageTypes = new Class[] 
    { 
	javax.servlet.http.HttpServletRequest.class,
	javax.servlet.http.HttpServletResponse.class 
    };            
    
    private MessagePolicy requestPolicy;

    private MessagePolicy responsePolicy;

    private CallbackHandler handler;

    private Map options;

    private boolean debug;

    private Level debugLevel;

    private String policyContextID;

    private String[] assignedGroups;

    private boolean isMandatory;

    private GSSManager gssManager;

    /**
     * Initialize this module with request and response message policies
     * to enforce, a CallbackHandler, and any module-specific configuration
     * properties.
     *
     * <p> The request policy and the response policy must not both be null.
     *
     * @param requestPolicy The request policy this module must enforce,
     *		or null.
     *
     * @param responsePolicy The response policy this module must enforce,
     *		or null.
     *
     * @param handler CallbackHandler used to request information.
     *
     * @param options A Map of module-specific configuration properties.
     *
     * @exception AuthException If module initialization fails, including for
     * the case where the options argument contains elements that are not 
     * supported by the module.
     */

    public void initialize(MessagePolicy requestPolicy,
	       MessagePolicy responsePolicy,
	       CallbackHandler handler,
	       Map options)
	throws AuthException {

	    this.requestPolicy = requestPolicy;
	    this.responsePolicy = responsePolicy;

	    this.isMandatory = requestPolicy.isMandatory();

	    this.handler = handler;
	    this.options = options;
	   
	    if (options != null) {
		debug = options.containsKey(DEBUG_OPTIONS_KEY);
		policyContextID = (String) 
		    options.get(POLICY_CONTEXT_OPTIONS_KEY);
	    } else {
		debug = false;
		policyContextID = null;
	    }

	    assignedGroups = getAssignedGroupNames();

	    debugLevel = (logger.isLoggable(Level.FINE) && !debug) ? 
		Level.FINE : Level.INFO;

	    gssManager = GSSManager.getInstance(); 
    }

    /**
     * Get the one or more Class objects representing the message types 
     * supported by the module.
     *
     * @return An array of Class objects, with at least one element 
     * defining a message type supported by the module.
     */
    public Class[] getSupportedMessageTypes() {
	return supportedMessageTypes;
    }

    /**
     * Authenticate a received service request.
     *
     * This method is called to transform the mechanism-specific request 
     * message acquired by calling getRequestMessage (on messageInfo) 
     * into the validated application message to be returned to the message 
     * processing runtime. 
     * If the received message is a (mechanism-specific) meta-message, 
     * the method implementation must attempt to transform the meta-message 
     * into a corresponding mechanism-specific response message, or to the
     * validated application request message.
     * The runtime will bind a validated application message into the
     * the corresponding service invocation.
     * <p> This method conveys the outcome of its message processing either
     * by returning an AuthStatus value or by throwing an AuthException.
     *
     * @param messageInfo A contextual object that encapsulates the
     *          client request and server response objects, and that may be 
     *          used to save state across a sequence of calls made to the 
     *          methods of this interface for the purpose of completing a 
     *          secure message exchange.
     *
     * @param clientSubject A Subject that represents the source of the 
     *          service 
     *          request.  It is used by the method implementation to store
     *		Principals and credentials validated in the request.
     *
     * @param serviceSubject A Subject that represents the recipient of the
     *		service request, or null.  It may be used by the method 
     *          implementation as the source of Principals or credentials to
     *          be used to validate the request. If the Subject is not null, 
     *          the method implementation may add additional Principals or 
     *          credentials (pertaining to the recipient of the service 
     *          request) to the Subject.
     *
     * @return An AuthStatus object representing the completion status of
     *          the processing performed by the method.
     *          The AuthStatus values that may be returned by this method 
     *          are defined as follows:
     *
     * <ul>
     * <li> AuthStatus.SUCCESS when the application request message
     * was successfully validated. The validated request message is
     * available by calling getRequestMessage on messageInfo.
     *
     * <li> AuthStatus.SEND_SUCCESS to indicate that validation/processing
     * of the request message successfully produced the secured application 
     * response message (in messageInfo). The secured response message is 
     * available by calling getResponseMessage on messageInfo.
     *
     * <li> AuthStatus.SEND_CONTINUE to indicate that message validation is
     * incomplete, and that a preliminary response was returned as the
     * response message in messageInfo.
     *
     * When this status value is returned to challenge an 
     * application request message, the challenged request must be saved 
     * by the authentication module such that it can be recovered
     * when the module's validateRequest message is called to process
     * the request returned for the challenge.
     *
     * <li> AuthStatus.SEND_FAILURE to indicate that message validation failed
     * and that an appropriate failure response message is available by
     * calling getResponseMessage on messageInfo.
     * </ul>
     *
     * @exception AuthException When the message processing failed without
     *          establishing a failure response message (in messageInfo).
     */
    public AuthStatus validateRequest(MessageInfo messageInfo,
			       Subject clientSubject,
			       Subject serviceSubject) throws AuthException {

	assert (messageInfo.getMap().containsKey(IS_MANDATORY_INFO_KEY) == 
		this.isMandatory);

	HttpServletRequest request = 
	    (HttpServletRequest) messageInfo.getRequestMessage();

	HttpServletResponse response = 
	    (HttpServletResponse) messageInfo.getResponseMessage();

	debugRequest(request);

	// should specify encoder
	String authorization = request.getHeader(AUTHORIZATION_HEADER);

	if (authorization != null && authorization.startsWith(NEGOTIATE)) {

	    authorization = authorization.substring(NEGOTIATE.length()+1);

	    // should specify a decoder
	    byte[] requestToken = Base64.decode(authorization.getBytes());

	    try {

		GSSContext gssContext = 
		    gssManager.createContext((GSSCredential) null);

		byte[] gssToken = gssContext.acceptSecContext
		    (requestToken,0,requestToken.length);
		
		if (gssToken != null) {

		    byte[] responseToken = Base64.encode(gssToken);

		    response.setHeader(AUTHENTICATION_HEADER,
				       "Negotiate" + responseToken);

		    debugToken("jmac.servlet.authentication.token",
				   responseToken);
		}

		if (!gssContext.isEstablished()) {

		    if (debug || logger.isLoggable(Level.FINE)){
			logger.log(debugLevel,"jmac.gss_dialog_continued");
		    }

		    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		    return AuthStatus.SEND_CONTINUE;

		} else {

		    String mechID;
		    try {
			Oid oid = gssContext.getMech();
			mechID = oid.toString();
		    } catch (GSSException gsse) {
			mechID = "Undefined GSS Mechanism";

			if (debug || logger.isLoggable(Level.FINE)){
			    logger.log(debugLevel,
				       "jmac.gss_mechanism_undefined",gsse);
			}
		    } 

		    GSSName name = gssContext.getSrcName();

		    if (!setCallerPrincipal(name,clientSubject)) {

			return sendFailureMessage
			    (response,
			     HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
			     "Failed setting caller principal");
		    }

		    /* we may need to add something like a cookie to the 
		     * response (that will be returned in subsequent requests).
		     * At this point, I am presuming that the browser will
		     * resend the authorization token.
		     */
		    messageInfo.getMap().put(AUTH_TYPE_INFO_KEY,mechID);

		    if (debug || logger.isLoggable(Level.FINE)){
			logger.log(debugLevel,"jmac.gss_dialog_complete");
		    }

		}

	    } catch (GSSException gsse) {

		if (requestToken != null) {

		    debugToken("jmac.servlet.authorization.token",
			       requestToken);

		    if (isNTLMToken(requestToken)) {

			// until we add support for NTLM
			return sendFailureMessage
			    (response,
			     HttpServletResponse.SC_NOT_IMPLEMENTED,
			     "No support for NTLM");
		    }
		} 

		if (debug || logger.isLoggable(Level.FINE)){
		    logger.log(debugLevel,"jmac.gss_dialog_failed",gsse);
		}

		// for other errors throw an AuthException

		AuthException ae = new AuthException();
		ae.initCause(gsse);
		throw ae;
	    }

	} else if (this.isMandatory) {

	    response.setHeader(AUTHENTICATION_HEADER,NEGOTIATE);
	    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

	    if (debug || logger.isLoggable(Level.FINE)){
		logger.log(debugLevel,"jmac.sevlet_header_added_to_response",
			   NEGOTIATE);
	    }

	    return AuthStatus.SEND_CONTINUE;

	} else {

	    if (authorization != null) {
		logger.warning("jmac.servlet_authorization_header_ignored");
	    }

	    if (!setCallerPrincipal(null,clientSubject)) {
		return sendFailureMessage
		    (response,
		     HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
		     "Failed setting unauthenticated caller principal");
	    }

	}

	return AuthStatus.SUCCESS;
    }

    /**
     * Secure a service response before sending it to the client.
     *
     * This method is called to transform the response message acquired by
     * calling getResponseMessage (on messageInfo) into the mechanism-specific
     * form to be sent by the runtime.
     * <p> This method conveys the outcome of its message processing either
     * by returning an AuthStatus value or by throwing an AuthException.
     *
     * @param messageInfo A contextual object that encapsulates the
     *          client request and server response objects, and that may be 
     *          used to save state across a sequence of calls made to the 
     *          methods of this interface for the purpose of completing a 
     *          secure message exchange.
     *
     * @param serviceSubject A Subject that represents the source of the 
     *          service
     *          response, or null. It may be used by the method implementation
     *          to retrieve Principals and credentials necessary to secure 
     *          the response. If the Subject is not null, 
     *          the method implementation may add additional Principals or 
     *          credentials (pertaining to the source of the service 
     *          response) to the Subject.
     *
     * @return An AuthStatus object representing the completion status of
     *          the processing performed by the method. 
     *          The AuthStatus values that may be returned by this method 
     *          are defined as follows:
     *
     * <ul>
     * <li> AuthStatus.SEND_SUCCESS when the application response 
     * message was successfully secured. The secured response message may be
     * obtained by calling getResponseMessage on messageInfo.
     *
     * <li> AuthStatus.SEND_CONTINUE to indicate that the application response 
     * message (within messageInfo) was replaced with a security message 
     * that should elicit a security-specific response (in the form of a 
     * request) from the peer.
     *
     * This status value serves to inform the calling runtime that
     * (to successfully complete the message exchange) it will
     * need to be capable of continuing the message dialog by processing
     * at least one additional request/response exchange (after having
     * sent the response message returned in messageInfo).
     *
     * When this status value is returned, the application response must 
     * be saved by the authentication module such that it can be recovered
     * when the module's validateRequest message is called to process
     * the elicited response.
     *
     * <li> AuthStatus.SEND_FAILURE to indicate that a failure occurred while
     * securing the response message and that an appropriate failure response
     * message is available by calling getResponseMeessage on messageInfo.
     * </ul>
     *
     * @exception AuthException When the message processing failed without
     *          establishing a failure response message (in messageInfo).
     */
    public AuthStatus secureResponse(MessageInfo messageInfo, 
	Subject serviceSubject) throws AuthException { 
	return AuthStatus.SEND_SUCCESS;
    }

    /**
     * Remove method specific principals and credentials from the subject.
     *
     * @param messageInfo a contextual object that encapsulates the
     *          client request and server response objects, and that may be 
     *          used to save state across a sequence of calls made to the 
     *          methods of this interface for the purpose of completing a 
     *          secure message exchange.
     *
     * @param subject the Subject instance from which the Principals and 
     *          credentials are to be removed.
     *
     * @exception AuthException If an error occurs during the Subject 
     *          processing.
     */

    public void cleanSubject(MessageInfo messageInfo, Subject subject)
	throws AuthException {
    }

    AuthStatus sendFailureMessage(HttpServletResponse response,
				  int status, String message){
	try {
	    response.setStatus(status);
	    response.sendError(status,message);
	} catch (Throwable t) {
	    // status code has been set, and proper AuthStatus will be returned
	    logger.log(Level.WARNING,"jmac.servlet_failed_sending_failure",t);
	} finally {
	    return AuthStatus.SEND_FAILURE;
	}
    }

    private boolean setCallerPrincipal(GSSName name,Subject clientSubject) {
	
	Principal caller = null;

	if (name != null) {

	    // create Subject with principals from name
	    Subject s = GSSUtil.createSubject(name,null);

	    Set principals = s.getPrincipals();
       
	    if (principals.size() > 0) {

		clientSubject.getPrincipals().addAll(principals);

		// if more than 1 prin, caller selection is unpredictable

		caller = (Principal) principals.iterator().next();
	    } else if (debug || logger.isLoggable(Level.FINE)){
		logger.log(debugLevel,"jmac.no_gss_caller_principal");
	    }
	}

	CallerPrincipalCallback cPCB = 
	    new CallerPrincipalCallback(clientSubject,caller);

	GroupPrincipalCallback gPCB = new GroupPrincipalCallback
	    (clientSubject,(caller == null ? null : assignedGroups));

	try {
	    handler.handle(new Callback[] { cPCB, gPCB } );
	    if (debug || logger.isLoggable(Level.FINE)){
		logger.log(debugLevel,"jmac.caller_principal",
			   new Object[] { caller } );
	    }
	    return true;
	} catch (Exception e) {
	    // should not happen
	    logger.log(Level.WARNING,"jmac.failed_to_set_caller",e);
	} 

	return false;
    }

    boolean isNTLMToken(byte[] bytes) {

	String s = new String(bytes);
	return s.startsWith(NTLM_INITIAL_TOKEN);
    }

    void debugToken(String message, byte[] bytes) {

	if (debug || logger.isLoggable(Level.FINE)) {

	    StringBuffer sb = new StringBuffer();
	    sb.append("\n");
	    sb.append("Token " + 
		      (Base64.isArrayByteBase64(bytes) ? "is" : "is Not") +
		      " Base64 encoded" + "\n");
	    sb.append("bytes: " );
	    boolean first = true;
	    for (byte b : bytes) {
		int i = b;
		if (first) {
		    sb.append(i);
		    first = false;
		} else {
		    sb.append(", " + i);
		}
	    }

	    logger.log(debugLevel,message,sb);
	}
    }

    void debugRequest(HttpServletRequest request) {

	if (debug || logger.isLoggable(Level.FINE)){
	    StringBuffer sb = new StringBuffer();
	    sb.append("\n");
	    try {
		sb.append("Request: " +request.getRequestURL() + "\n");
		sb.append("UserPrincipal: " + request.getUserPrincipal() + "\n");
		sb.append("AuthType: " + request.getAuthType()+ "\n");
		sb.append("Headers:" + "\n");
		Enumeration names = request.getHeaderNames();
		while (names.hasMoreElements()) {
		    String name = (String) names.nextElement();
		    sb.append("\t" + name + "\t" + request.getHeader(name) + "\n");
		}
		
		logger.log(debugLevel,"jmac.servlet_request",sb);

	    } catch(Throwable t) {
		logger.log(Level.WARNING,"jmac.servlet_debug_request",t);
	    }
	}
    }

    private String[] getAssignedGroupNames() {
	String groupList = (String) 
	    options.get(ASSIGN_GROUPS_OPTIONS_KEY);
	String[] groups = null;
	if (groupList != null) {
	    StringTokenizer tokenizer = 
		new StringTokenizer(groupList," ,:,;");
	    int count = tokenizer.countTokens();
	    if (count > 0) {
		groups = new String[count];
		for (int i = 0; i < count; i++) {
		    groups[i] = tokenizer.nextToken();
		}
	    }
	}
	return groups;
    }
}
