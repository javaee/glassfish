/*
 * TestServlet.java
 *
 * Created on February 15, 2007, 11:26 PM
 */

package outbound.servlet;

import java.io.*;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.xml.ws.Holder;
import javax.xml.ws.WebServiceRef;
import messages.ejbws.AddressBook;
import messages.ejbws.AddressBookEntry;
import messages.ejbws.MessageEJB;

/**
 *
 * @author sony
 * @version
 */
public class TestServlet extends HttpServlet {

    @WebServiceRef(wsdlLocation = "WEB-INF/wsdl/client/MessageEJBService/MessageEJBService.wsdl")
    private messages.ejbws.MessageEJBService service;
    
    /** Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        String testName = request.getParameter("test");
        out.println("Calling test : " + testName);
        if (testName.equals("testPing"))
            testPing(out);
        else if (testName.equals("testStringOneway"))
            testStringOneway(out);
        else if (testName.equals("testTypes"))
            testTypes(out);
        else if (testName.equals("testParamModes"))
            testParamModes(out);
        out.close();
    }
    
    public void testPing(PrintWriter out) {
        
        try { // Call Web Service Operation
            messages.ejbws.MessageEJB port = service.getMessageEJBPort();
            port.ping();
            out.println("testPing:PASS");
        } catch (Exception ex) {
            out.println("testPing:FAIL");
            ex.printStackTrace(out);
        }
    }
    
    public void testTypes(PrintWriter out) {
        try {
            MessageEJB port = service.getMessageEJBPort();
            boolean result = port.testTypes("Hello", Integer.MIN_VALUE,
                    Double.MAX_VALUE, 
                    new byte[] { Byte.MAX_VALUE, Byte.MIN_VALUE});
            
            if (result)
                out.println("testTypes:PASS");
            else 
                out.println("testTypes:FAIL");
        } catch (Exception ex) {
            out.println("testTypes:FAIL");
            ex.printStackTrace(out);
        }
    }
    
    public void testStringOneway(PrintWriter out) {
        
        try { // Call Web Service Operation
            messages.ejbws.MessageEJB port = service.getMessageEJBPort();
            port.testStringOneway("Hello from callMessagesEJBApp");
            out.println("testStringOneway:PASS");
        } catch (Exception ex) {
            out.println("testStringOneway:FAIL");
            ex.printStackTrace(out);
        }
    }
    
    public void testParamModes(PrintWriter out) {
        
        try { // Call Web Service Operation
            messages.ejbws.MessageEJB port = service.getMessageEJBPort();
            
            Holder<AddressBook> bookHolder = new Holder<AddressBook>();
            
            Holder<AddressBookEntry> entryHolder =
                    new Holder<AddressBookEntry>();
            
            AddressBookEntry entry = new AddressBookEntry();
            entry.setName("Foo Bar");
            entryHolder.value = entry;
            
            String result = port.testParamModes(bookHolder, entryHolder);
            AddressBookEntry resultEntry =
                    bookHolder.value.getAddressBook().get(0);
            
            if (result.equals("Foo Bar") &&
                    entryHolder.value.getName().equals("Foo Bar") &&
                    resultEntry.getName().equals("Foo Bar"))
                out.println("testParamModes:PASS");
            else
                out.println("testParamModes:FAIL");
        } catch (Exception ex) {
            out.println("testParamModes:FAIL");
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
