package test;

import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.security.DeclareRoles;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import test.ejb.stateful.SFHello;

@DeclareRoles({"staff"})
public class ServletTest {
    private @EJB SFHello sful1; 
    private @EJB(name="ejb/sfhello") SFHello sful2; 

    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
    }

    @PostConstruct
    private void preconstruct() {
    }

    @PreDestroy
    private void predestroy() {
    }
}
