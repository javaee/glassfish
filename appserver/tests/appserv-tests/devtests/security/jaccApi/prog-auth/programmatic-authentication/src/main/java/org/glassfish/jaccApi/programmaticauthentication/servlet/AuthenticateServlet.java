package org.glassfish.jaccApi.programmaticauthentication.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.security.auth.Subject;
import javax.security.jacc.PolicyContext;
import javax.security.jacc.PolicyContextException;
import java.security.Principal;
import java.util.stream.Collectors;
import java.util.Set;

@WebServlet(urlPatterns = "/public/authenticate")
public class AuthenticateServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        response.getWriter().write("This is a public servlet \n");
        request.setAttribute("doLogin",true);
        boolean authenticateOutcome = request.authenticate(response);
        String webName;
        if (request.getUserPrincipal() != null) {
            webName = request.getUserPrincipal().getName();
        }
        //get Subject via jacc api
        try {
            Subject subject = (Subject) PolicyContext.getContext("javax.security.auth.Subject.container");
            if (subject != null) {
                response.getWriter().write(subject.toString());
                Set<Principal> principalsSet = subject.getPrincipals();
//                String princiaplsInSubject = "";
                String princiaplsInSubject = principalsSet.stream()
                                                          .map(e -> e.getName())
                                                          .collect(Collectors.joining(", "));
                response.getWriter().write("Principals: " + princiaplsInSubject);
//            response.getWriter().write("Principals in subject are :" + subject.getPrincipals().stream().map(Principal::getName()).collect(Collectors.join(",")));
            }
        }catch (PolicyContextException e){
            response.getWriter().write("ERROR while getting Subject");
            e.printStackTrace(response.getWriter());
        }

    }

}
