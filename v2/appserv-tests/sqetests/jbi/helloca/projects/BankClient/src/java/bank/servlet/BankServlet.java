/*
 * BankServlet.java
 *
 * Created on February 16, 2007, 12:09 PM
 */

package bank.servlet;

import com.sun.org.apache.xml.internal.security.utils.Base64;
import java.io.*;
import java.net.*;
import java.util.Map;
import javax.annotation.security.DeclareRoles;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.xml.ws.soap.SOAPFaultException;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.WebServiceRef;

/**
 *
 * @author sony
 * @version
 */

@DeclareRoles(value = {"bankcustomer", "bankmanager"})
public class BankServlet extends HttpServlet {

    @WebServiceRef(wsdlLocation = "WEB-INF/wsdl/client/BankService/BankService.wsdl")
    private bankws.BankService service;

    /** Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();      
        String method = request.getParameter("test");
        if (method.equals("createAccount")) {
            out.println("Calling Bank.createAccount()");
            createAccount(out);
        } else if (method.equals("createAccountWrongPrincipal")) {
            out.println("Calling Bank.createAccount()");
            createAccountWrongPrincipal(out);
        } else if (method.equals("debit")) {
            out.println("Calling Bank.debit()");
            debit(out);
        }        
        out.close();
    }
    
    public void createAccount(PrintWriter out) {
        try { // Call Web Service Operation
            bankws.Bank port = service.getBankPort();            
//            Map context = ((BindingProvider) port).getRequestContext();
//            context.put(BindingProvider.USERNAME_PROPERTY, "bar");
//            context.put(BindingProvider.PASSWORD_PROPERTY, "bar123");
            
            int result = port.createAccount("Tom", 2000.00f);
            if (result == 1001)
                out.println("Bank.createAccount:PASS");
            else
                out.println("Bank.createAccount:FAIL");
        } catch (Exception ex) {
            out.println("Bank.createAccount:FAIL");
            ex.printStackTrace(out);
        }
    }
    
    public void debit(PrintWriter out) {
        try { // Call Web Service Operation
            bankws.Bank port = service.getBankPort();            
            double result = port.debit(2001, 550.00);
            if (result == (550.00 + 1000))
                out.println("Bank.debit:PASS");
            else
                out.println("Bank.debit:FAIL");
        } catch (Exception ex) {
            out.println("Bank.debit:FAIL");
            ex.printStackTrace(out);
        }
    }    
    
    public void createAccountWrongPrincipal(PrintWriter out) {
        
        try { // Call Web Service Operation
            bankws.Bank port = service.getBankPort();            
            int result = port.createAccount("Harry", 2000.00f);
            out.println("Bank.createAccount:FAIL");
        } catch (SOAPFaultException sfe) {
            out.println("Got expected SOAPFaultException");
            out.println("Bank.createAccount:PASS");
        } catch (Exception ex) {
            out.println("Bank.createAccount:FAIL");
            ex.printStackTrace(out);
        }
    }
    
    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    }
    
    /** Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    }
    
    /** Returns a short description of the servlet.
     */
    public String getServletInfo() {
        return "Short description";
    }
    // </editor-fold>
}
