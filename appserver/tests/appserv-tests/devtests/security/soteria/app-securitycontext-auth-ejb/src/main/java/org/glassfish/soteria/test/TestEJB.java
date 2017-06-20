package org.glassfish.soteria.test;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.PermitAll;
import javax.annotation.Resource;
import javax.ejb.EJBContext;
import javax.annotation.security.RolesAllowed;
import org.glassfish.soteria.SecurityContextImpl;
import javax.security.enterprise.SecurityContext;
import java.security.Principal;
import java.util.Set;
@Stateless
@DeclareRoles({ "foo" , "bar", "kaz"})
public class TestEJB {

    @Inject
    private SecurityContext securityContext;

    @Resource
    private EJBContext ejbContext;

    public Principal getUserPrincipalFromEJBContext() {
        try {
            return ejbContext.getCallerPrincipal();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean isCallerInRoleFromEJBContext(String role) {
        try {
            return ejbContext.isCallerInRole(role);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    public Principal getUserPrincipalFromSecContext() {
        return securityContext.getCallerPrincipal();
    }

    public boolean isCallerInRoleFromSecContext(String role) {
        return securityContext.isCallerInRole(role);
    }

    public Set<String> getAllDeclaredCallerRoles() {
        return ((SecurityContextImpl)securityContext).getAllDeclaredCallerRoles();
    }


}
