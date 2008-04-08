

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * Portions Copyright Apache Software Foundation.
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


package org.apache.catalina.authenticator;


import java.io.IOException;
import java.security.Principal;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.sun.grizzly.util.buf.CharChunk;
import com.sun.grizzly.util.buf.MessageBytes;
import com.sun.grizzly.util.http.mapper.Mapper;
import com.sun.grizzly.util.http.mapper.MappingData;

import org.apache.catalina.HttpRequest;
import org.apache.catalina.HttpResponse;
import org.apache.catalina.Realm;
import org.apache.catalina.Session;
import org.apache.catalina.deploy.LoginConfig;
import com.sun.org.apache.commons.logging.Log;
import com.sun.org.apache.commons.logging.LogFactory;



/**
 * An <b>Authenticator</b> and <b>Valve</b> implementation of FORM BASED
 * Authentication, as described in the Servlet API Specification, Version 2.2.
 *
 * @author Craig R. McClanahan
 * @author Remy Maucherat
 * @version $Revision: 1.8 $ $Date: 2007/07/23 16:48:23 $
 */

public class FormAuthenticator
    extends AuthenticatorBase {
    private static Log log = LogFactory.getLog(FormAuthenticator.class);



    // ----------------------------------------------------- Instance Variables


    /**
     * Descriptive information about this implementation.
     */
    protected static final String info =
        "org.apache.catalina.authenticator.FormAuthenticator/1.0";


    // ------------------------------------------------------------- Properties


    /**
     * Return descriptive information about this Valve implementation.
     */
    public String getInfo() {

        return (this.info);

    }


    // --------------------------------------------------------- Public Methods


    /**
     * Authenticate the user making this request, based on the specified
     * login configuration.  Return <code>true</code> if any specified
     * constraint has been satisfied, or <code>false</code> if we have
     * created a response challenge already.
     *
     * @param request Request we are processing
     * @param response Response we are creating
     * @param login Login configuration describing how authentication
     *              should be performed
     *
     * @exception IOException if an input/output error occurs
     */
    public boolean authenticate(HttpRequest request,
                                HttpResponse response,
                                LoginConfig config)
        throws IOException {

        // References to objects we will need later
        HttpServletRequest hreq =
          (HttpServletRequest) request.getRequest();
        HttpServletResponse hres =
          (HttpServletResponse) response.getResponse();
        Session session = null;

        // Have we already authenticated someone?
        Principal principal = hreq.getUserPrincipal();
        if (principal != null) {
            if (log.isDebugEnabled())
                log.debug("Already authenticated '" +
                    principal.getName() + "'");
            String ssoId = (String) request.getNote(Constants.REQ_SSOID_NOTE);
            if (ssoId != null) {
                getSession(request, true);
            }
            return (true);
        }

        // Have we authenticated this user before but have caching disabled?
        if (!cache) {
            session = getSession(request, true);
            if (log.isDebugEnabled())
                log.debug("Checking for reauthenticate in session " + session);
            String username =
                (String) session.getNote(Constants.SESS_USERNAME_NOTE);
            String password =
                (String) session.getNote(Constants.SESS_PASSWORD_NOTE);
            if ((username != null) && (password != null)) {
                if (log.isDebugEnabled())
                    log.debug("Reauthenticating username '" + username + "'");
                principal =
                    context.getRealm().authenticate(username, password);
                if (principal != null) {
                    session.setNote(Constants.FORM_PRINCIPAL_NOTE, principal);
                    if (!matchRequest(request)) {
                        register(request, response, principal,
                                 Constants.FORM_METHOD,
                                 username, password);
                        return (true);
                    }
                }
                if (log.isDebugEnabled())
                    log.debug("Reauthentication failed, proceed normally");
            }
        }

        // Is this the re-submit of the original request URI after successful
        // authentication?  If so, forward the *original* request instead.
        if (matchRequest(request)) {
            session = getSession(request, true);
            if (log.isDebugEnabled())
                log.debug("Restore request from session '" + session.getIdInternal() 
                          + "'");
            principal = (Principal)
                session.getNote(Constants.FORM_PRINCIPAL_NOTE);
            register(request, response, principal, Constants.FORM_METHOD,
                     (String) session.getNote(Constants.SESS_USERNAME_NOTE),
                     (String) session.getNote(Constants.SESS_PASSWORD_NOTE));
            String ssoId = (String) request.getNote(Constants.REQ_SSOID_NOTE);
            if (ssoId != null)
                associate(ssoId, session);
            if (restoreRequest(request, session)) {
                if (log.isDebugEnabled())
                    log.debug("Proceed to restored request");
                return (true);
            } else {
                if (log.isDebugEnabled())
                    log.debug("Restore of original request failed");
                hres.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return (false);
            }
        }

        // Acquire references to objects we will need to evaluate
        MessageBytes uriMB = MessageBytes.newInstance();
        CharChunk uriCC = uriMB.getCharChunk();
        uriCC.setLimit(-1);
        String contextPath = hreq.getContextPath();
        String requestURI = request.getDecodedRequestURI();
        response.setContext(request.getContext());

        // Is this the action request from the login page?
        boolean loginAction =
            requestURI.startsWith(contextPath) &&
            requestURI.endsWith(Constants.FORM_ACTION);

        // No -- Save this request and redirect to the form login page
        if (!loginAction) {
            session = getSession(request, true);
            if (log.isDebugEnabled())
                log.debug("Save request in session '" + session.getIdInternal() + "'");
            saveRequest(request, session);

            //START Apache bug 36136: Refactor the login and error page forward
            /*
            RequestDispatcher disp =
                context.getServletContext().getRequestDispatcher
                (config.getLoginPage());
            try {
                disp.forward(hreq, hres);
                response.finishResponse();
            } catch (Throwable t) {
                log.warn("Unexpected error forwarding to login page", t);
            }
            */
            forwardToLoginPage(request, response, config);
            //END Apache bug 36136: Refactor the login and error page forward

            return (false);
        }

        // Yes -- Validate the specified credentials and redirect
        // to the error page if they are not correct
        Realm realm = context.getRealm();
        String username = hreq.getParameter(Constants.FORM_USERNAME);
        String password = hreq.getParameter(Constants.FORM_PASSWORD);
        if (log.isDebugEnabled())
            log.debug("Authenticating username '" + username + "'");
        principal = realm.authenticate(username, password);
        if (principal == null) {

            //START Apache bug 36136: Refactor the login and error page forward
            /*
            RequestDispatcher disp =
                context.getServletContext().getRequestDispatcher
                (config.getErrorPage());
            try {
                disp.forward(hreq, hres);
            } catch (Throwable t) {
                log.warn("Unexpected error forwarding to error page", t);
            }
            */
            forwardToErrorPage(request, response, config);
            //END Apache bug 36136: Refactor the login and error page forward

            return (false);
        }

        // Save the authenticated Principal in our session
        if (log.isDebugEnabled())
            log.debug("Authentication of '" + username + "' was successful");
        if (session == null)
            session = getSession(request, true);
        session.setNote(Constants.FORM_PRINCIPAL_NOTE, principal);

        // If we are not caching, save the username and password as well
        if (!cache) {
            session.setNote(Constants.SESS_USERNAME_NOTE, username);
            session.setNote(Constants.SESS_PASSWORD_NOTE, password);
        }

        // Redirect the user to the original request URI (which will cause
        // the original request to be restored)
        requestURI = savedRequestURL(session);
        if (requestURI == null) {
            // requestURI will be null if the login form is submitted
            // directly, i.e., if there has not been any original request
            // that was stored away before the redirect to the login form was
            // issued. In this case, assume that the original request has been
            // for the context root, and have the welcome page mechanism take
            // care of it
            requestURI = hreq.getContextPath() + "/";
            saveRequest(requestURI, hreq.getMethod(), session);
        }

        if (log.isDebugEnabled()) {
            log.debug("Redirecting to original '" + requestURI + "'");
        }

        hres.sendRedirect(hres.encodeRedirectURL(requestURI));

        return (false);

    }


    // ------------------------------------------------------ Protected Methods


    /**
     * Does this request match the saved one (so that it must be the redirect
     * we signalled after successful authentication?
     *
     * @param request The request to be verified
     */
    protected boolean matchRequest(HttpRequest request) {

      // Has a session been created?
      Session session = getSession(request, false);
      if (session == null)
          return (false);

      // Is there a saved request?
      SavedRequest sreq = (SavedRequest)
          session.getNote(Constants.FORM_REQUEST_NOTE);
      if (sreq == null)
          return (false);

      // Is there a saved principal?
      if (session.getNote(Constants.FORM_PRINCIPAL_NOTE) == null)
          return (false);

      // Does the request URI match?
      HttpServletRequest hreq = (HttpServletRequest) request.getRequest();
      String requestURI = hreq.getRequestURI();
      if (requestURI == null)
          return (false);
      return (requestURI.equals(sreq.getRequestURI()));

    }


    /**
     * Restore the original request from information stored in our session.
     * If the original request is no longer present (because the session
     * timed out), return <code>false</code>; otherwise, return
     * <code>true</code>.
     *
     * @param request The request to be restored
     * @param session The session containing the saved information
     */
    protected boolean restoreRequest(HttpRequest request, Session session) {

        // Retrieve and remove the SavedRequest object from our session
        SavedRequest saved = (SavedRequest)
            session.getNote(Constants.FORM_REQUEST_NOTE);
        session.removeNote(Constants.FORM_REQUEST_NOTE);
        session.removeNote(Constants.FORM_PRINCIPAL_NOTE);
        if (saved == null)
            return (false);

        // Modify our current request to reflect the original one
        request.clearCookies();
        Iterator cookies = saved.getCookies();
        while (cookies.hasNext()) {
            request.addCookie((Cookie) cookies.next());
        }
        request.clearHeaders();
        Iterator names = saved.getHeaderNames();
        while (names.hasNext()) {
            String name = (String) names.next();
            Iterator values = saved.getHeaderValues(name);
            while (values.hasNext()) {
                request.addHeader(name, (String) values.next());
            }
        }
        request.clearLocales();
        Iterator locales = saved.getLocales();
        while (locales.hasNext()) {
            request.addLocale((Locale) locales.next());
        }
        request.clearParameters();
        if ("POST".equalsIgnoreCase(saved.getMethod())) {
            Iterator paramNames = saved.getParameterNames();
            while (paramNames.hasNext()) {
                String paramName = (String) paramNames.next();
                String paramValues[] =
                    (String[]) saved.getParameterValues(paramName);
                request.addParameter(paramName, paramValues);
            }
        }
        request.setMethod(saved.getMethod());
        request.setQueryString(saved.getQueryString());
        request.setRequestURI(saved.getRequestURI());
        return (true);

    }


    /**
     * Called to forward or redirect to the login page.
     *
     * @param request HttpRequest we are processing
     * @param response HttpResponse we are creating
     * @param config    Login configuration describing how authentication
     *                  should be performed
     */
    protected void forwardToLoginPage(HttpRequest request,
                                      HttpResponse response,
                                      LoginConfig config) {
        ServletContext sc = context.getServletContext();
        try {
            if (request.getRequest().isSecure()) {
                RequestDispatcher disp = sc.getRequestDispatcher
                    (config.getLoginPage());  
                disp.forward(request.getRequest(), response.getResponse());
                response.finishResponse();
            } else {
                ((HttpServletResponse) response.getResponse()).sendRedirect(
                    sc.getContextPath() + config.getLoginPage());
            }
        } catch (Throwable t) {
            log.warn("Unexpected error forwarding or redirecting to "
                     + "login page", t);
        }
    }
    
    
    /**
     * Called to forward or redirect to the error page
     *
     * @param request HttpRequest we are processing
     * @param response HttpResponse we are creating
     * @param config    Login configuration describing how authentication
     *                  should be performed
     */
    protected void forwardToErrorPage(HttpRequest request,
                                HttpResponse response,
                                LoginConfig config) {
        ServletContext sc = context.getServletContext();
        try {
            if (request.getRequest().isSecure()) {
                RequestDispatcher disp = sc.getRequestDispatcher
                    (config.getErrorPage());
                disp.forward(request.getRequest(), response.getResponse());
            } else {
                ((HttpServletResponse) response.getResponse()).sendRedirect(
                    sc.getContextPath() + config.getErrorPage());
            }
        } catch (Throwable t) {
            log.warn("Unexpected error forwarding or redirecting to "
                     + "error page", t);
        }
    }
    
    /**
     * Save the original request information into our session.
     *
     * @param request The request to be saved
     * @param session The session to contain the saved information
     */
    //START Apache bug 36136: Refactor the login and error page forward
    //private void saveRequest(HttpRequest request, Session session) {
    protected void saveRequest(HttpRequest request, Session session) {
    //END Apache bug 36136: Refactor the login and error page forward

        // Create and populate a SavedRequest object for this request
        HttpServletRequest hreq = (HttpServletRequest) request.getRequest();
        SavedRequest saved = new SavedRequest();
        Cookie cookies[] = hreq.getCookies();
        if (cookies != null) {
            for (int i = 0; i < cookies.length; i++)
                saved.addCookie(cookies[i]);
        }
        Enumeration names = hreq.getHeaderNames();
        while (names.hasMoreElements()) {
            String name = (String) names.nextElement();
            Enumeration values = hreq.getHeaders(name);
            while (values.hasMoreElements()) {
                String value = (String) values.nextElement();
                saved.addHeader(name, value);
            }
        }
        Enumeration locales = hreq.getLocales();
        while (locales.hasMoreElements()) {
            Locale locale = (Locale) locales.nextElement();
            saved.addLocale(locale);
        }
        Map parameters = hreq.getParameterMap();
        Iterator paramNames = parameters.keySet().iterator();
        while (paramNames.hasNext()) {
            String paramName = (String) paramNames.next();
            String paramValues[] = (String[]) parameters.get(paramName);
            saved.addParameter(paramName, paramValues);
        }
        saved.setMethod(hreq.getMethod());
        saved.setQueryString(hreq.getQueryString());
        saved.setRequestURI(hreq.getRequestURI());

        // Stash the SavedRequest in our session for later use
        session.setNote(Constants.FORM_REQUEST_NOTE, saved);

    }


    /**
     * Return the request URI (with the corresponding query string, if any)
     * from the saved request so that we can redirect to it.
     *
     * @param session Our current session
     */
    //START Apache bug 36136: Refactor the login and error page forward
    //private String savedRequestURL(Session session) {
    protected String savedRequestURL(Session session) {
    //END Apache bug 36136: Refactor the login and error page forward

        SavedRequest saved =
            (SavedRequest) session.getNote(Constants.FORM_REQUEST_NOTE);
        if (saved == null)
            return (null);
        StringBuffer sb = new StringBuffer(saved.getRequestURI());
        if (saved.getQueryString() != null) {
            sb.append('?');
            sb.append(saved.getQueryString());
        }
        return (sb.toString());

    }


    /**
     * Saves the given request URI in the given session
     */
    private void saveRequest(String requestURI, String method,
                             Session session) {

        SavedRequest saved = new SavedRequest();
        saved.setMethod(method);
        saved.setRequestURI(requestURI);
        session.setNote(Constants.FORM_REQUEST_NOTE, saved);
    }

}
