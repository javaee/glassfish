package test.provider;

import java.io.IOException;
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.message.AuthException;
import javax.security.auth.message.AuthStatus;
import javax.security.auth.message.MessageInfo;
import javax.security.auth.message.MessagePolicy;
import javax.security.auth.message.module.ServerAuthModule;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("rawtypes")
public class TestHttpServletServerAuthModule implements ServerAuthModule {

    /**
     * This is the URI of the action which will trigger a redirect.
     */
    private static final String LOGIN_ACTION_URI = "/samlogin";

    /**
     * This must point to a JSF Facelets page to trigger the bug.
     */
    private static final String REDIRECT_FACES_URI = "/message.xhtml";

    @Override
    public Class<?>[] getSupportedMessageTypes() {
        return new Class[] { HttpServletRequest.class, HttpServletResponse.class };
    }

    @Override
    public void initialize(final MessagePolicy reqPolicy, final MessagePolicy resPolicy, final CallbackHandler cBH,
            final Map opts) throws AuthException {
    }

    @Override
    public void cleanSubject(final MessageInfo messageInfo, final Subject subject) throws AuthException {
        if (subject != null) {
            subject.getPrincipals().clear();
        }
    }

    @Override
    public AuthStatus validateRequest(final MessageInfo messageInfo, final Subject clientSubject, final Subject serviceSubject)
            throws AuthException {
        try {
            final HttpServletRequest request = (HttpServletRequest) messageInfo.getRequestMessage();
            final HttpServletResponse response = (HttpServletResponse) messageInfo.getResponseMessage();

            //XXX
            /*
            if (!isMandatory(messageInfo) && !request.getRequestURI().endsWith(LOGIN_ACTION_URI)) {
                System.out.println(request.getRequestURI() + " Ignoring - Mandatory=" + isMandatory(messageInfo)
                        + " isLoginAction=" + request.getRequestURI().endsWith(LOGIN_ACTION_URI));
            */
            if (!request.getRequestURI().endsWith(LOGIN_ACTION_URI)) {
                return AuthStatus.SUCCESS;
            }

            request.getRequestDispatcher(REDIRECT_FACES_URI).forward(request, response);
            return AuthStatus.SEND_CONTINUE;
        } catch (Throwable e) {
            AuthException authException = new AuthException();
            authException.initCause(e);
            throw authException;
        }
    }

    /*
    protected void redirectToPage(final HttpServletRequest request, final HttpServletResponse response, final String path)
            throws ServletException, IOException {
        if (request.isSecure()) {
            final RequestDispatcher disp = request.getRequestDispatcher(path);
            disp.forward(request, response);
        } else {
            final String encodedRedirectURL = response.encodeRedirectURL(request.getContextPath() + path);
            response.sendRedirect(encodedRedirectURL);
        }
    }
    */

    /*
    private boolean isMandatory(final MessageInfo messageInfo) {
        return Boolean.parseBoolean((String) messageInfo.getMap().get("javax.security.auth.message.MessagePolicy.isMandatory"));
    }
    */

    @Override
    public AuthStatus secureResponse(final MessageInfo messageInfo, final Subject serviceSubject) throws AuthException {
        return AuthStatus.SEND_SUCCESS;
    }

}
