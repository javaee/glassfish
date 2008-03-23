/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.enterprise.security.web.integration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jvnet.hk2.annotations.Contract;

/**
 * Web specific Programmatic Login
 * An implementation of this will be injected into 
 * com.sun.appserv.security.api.ProgrammaticLogin
 */
@Contract
public interface WebProgrammaticLogin {
    
    /** 
     * Login and set up principal in request and session. This implements
     * programmatic login for servlets. 
     *
     * <P>Due to a number of bugs in RI the security context is not
     * shared between web container and ejb container. In order for an
     * identity established by programmatic login to be known to both
     * containers, it needs to be set not only in the security context but
     * also in the current request and, if applicable, the session object.
     * If a session does not exist this method does not create one.
     *
     * <P>See bugs 4646134, 4688449 and other referenced bugs for more
     * background.
     * 
     * <P>Note also that this login does not hook up into SSO.
     *
     * @param user User name to login.
     * @param password User password.
     * @param request HTTP request object provided by caller application. It
     *     should be an instance of HttpRequestFacade.
     * @param response HTTP response object provided by called application. It
     *     should be an instance of HttpServletResponse. This is not used
     *     currently.
     * @param realm the realm name to be authenticated to. If the realm is null, 
     * authentication takes place in default realm
     * @returns A Boolean object; true if login succeeded, false otherwise.
     * @see com.sun.appserv.security.ProgrammaticLogin
     * @throws Exception on login failure.
     *
     */
     public Boolean login(String user, String password, String realm,
                                HttpServletRequest request,
                                HttpServletResponse response);
     /** 
     * Logout and remove principal in request and session.
     *
     * @param request HTTP request object provided by caller application. It
     *     should be an instance of HttpRequestFacade.
     * @param response HTTP response object provided by called application. It
     *     should be an instance of HttpServletResponse. This is not used
     *     currently.
     * @returns A Boolean object; true if login succeeded, false otherwise.
     * @see com.sun.appserv.security.ProgrammaticLogin
     * @throws Exception any exception encountered during logout operation
     */
     public Boolean logout(HttpServletRequest request,
                                 HttpServletResponse response) throws Exception; 

}
