/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the "License").  You may not use this file except 
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt or 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html. 
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * HEADER in each file and include the License file at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt.  If applicable, 
 * add the following below this CDDL HEADER, with the 
 * fields enclosed by brackets "[]" replaced with your 
 * own identifying information: Portions Copyright [yyyy] 
 * [name of copyright owner]
 */
package org.glassfish.grizzly.async.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.glassfish.grizzly.async.javamail.JavaMailAsyncFilterHandler;
import org.glassfish.grizzly.async.javamail.JavaMailAsyncFilterEvent;
import org.glassfish.grizzly.async.javamail.JavaMailAsyncFilter;

/**
 * Simple servlet that use Grizzly Asynchronous Request Processing to fecth
 * mail from an email account (Gmail, Yahoo, etc.). The Servlet will be interupted 
 * until new mails are available in the Gmail account.
 *
 * @author Jeanfrancois Arcand
 */
public class EmailNotifierServlet extends HttpServlet 
                                    implements JavaMailAsyncFilterHandler{
     
    /**
     * The username for the account 
     */
    private String username;
    
    
    /**
     * The password for the account 
     */
    private String password;   
    
    
    /**
     * The mail server
     */
    private String mailServer;   
    
    
    /**
     * The  port the mail server is listeningt 
     */
    private String mailServerPort;
    
    
    /**
     * The current Gmail message list.
     */
    private Message[] messages;
    
    
    public EmailNotifierServlet() {
    }
    
    
    /**
     * Init this Servlet by registering it with the 
     * <code>JavaMailAsyncFilter</code>.
     */
    public void init(ServletConfig config) throws ServletException { 
        username = config.getInitParameter("username");
        password = config.getInitParameter("password");
        mailServer = config.getInitParameter("mailServer");  
        mailServerPort = config.getInitParameter("mailServerPort");  
        
        JavaMailAsyncFilter.register(
                config.getInitParameter("contextPath"), this);
    }
   
    
    /**
     * The <code>doGet</code> will execute only when the Grizzly 
     * <code>JavaMailAsyncFilter</code> determines this servlet can execute. 
     * The <code>JavaMailAsyncFilter</code> will allow the execution of this 
     * Servlet only if a new email message arrive in the remote email account.
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response) 
                                        throws ServletException, IOException {
        
        PrintWriter out = response.getWriter();
        try{
            
            if ( messages != null) {           
                out.println("<html>");
                    out.println("<HEAD><TITLE>Async JavaMail Servlet</TITLE></HEAD>");
                out.println("<BODY bgcolor=\"#ccccff\">");
                out.print("<center><font face=\"Arial,Helvetica\" ");
                out.println("font size=\"+3\"><b>");
                out.println("You have " + messages.length + " messages in folder "
                        + "inbox</b></font></center><p>");

                for (int i=0; i < messages.length; i++){
                    try {
                        Message msg = messages[i];
                        displayMessageHeaders(msg, out);

                        try{
                            Object o = msg.getContent();

                            out.println("<pre>");
                            out.println((String)o);
                            out.println("</pre>");
                        } catch (NullPointerException ex){
                            ; //If the message isn't having body
                        }
                     } catch (MessagingException mex) {
                        out.println(mex.toString());
                    }
                }
            }
  
            out.println("</BODY></html>");
            out.close();
        } catch (Throwable t){
           t.printStackTrace(); 
        }
    }

    // --------------------------------------------------------- Async Hook ---/
    
    /**
     * The <code>JavaMailAsyncFilter</code> will invoke that method after
     * fetching the email from the remote account. This method will return 
     * <code>true</code> if the messages retrived aren't new or no new message
     * has been found. Returning <code>true</code> means the Servlet shouldn't
     * yet be executed. Returning <code>false</code> will fire the execution
     * of this Servlet. 
     * @paramc event The JavaMailAsyncFilterEvent notification from 
     *         JavaMailAsyncFilter 
     */
    public boolean handleEvent(JavaMailAsyncFilterEvent event) {
        messages = event.getMessages();

        if ( messages == null || messages.length == 0){
            return true;
        } else {
            return false;
        }      
    }

    
    public String getUserName() {
       return username;
    }

    
    public String getPassword() {
        return password;
    }

    
    public String getMailServer() {
        return mailServer;
    }

    
    public String getMailServerPort() {
        return mailServerPort;
    }
    
    // -------------------------------------------------------- JavaMail ----/
    
    private String getDisplayAddress(Address a) {
        String pers = null;
        String addr = null;
        if (a instanceof InternetAddress &&
            ((pers = ((InternetAddress)a).getPersonal()) != null)) {

            addr = pers + "  "+"&lt;"+((InternetAddress)a).getAddress()+"&gt;";
        } else
            addr = a.toString();

        return addr;
    }

    
    private void displayMessageHeaders(Message msg, PrintWriter out)
        throws IOException {

        try {
            out.println("<b>Date:</b> " + msg.getSentDate() + "<br>");

            Address[] fr = msg.getFrom();
            if (fr != null) {
                boolean tf = true;
                out.print("<b>From:</b> ");
                for (int i = 0; i < fr.length; i++) {
                    out.print(((tf) ? " " : ", ") + getDisplayAddress(fr[i]));
                    tf = false;
                }
                out.println("<br>");
            }

            Address[] to = msg.getRecipients(Message.RecipientType.TO);
            if (to != null) {
                boolean tf = true;
                out.print("<b>To:</b> ");
                for (int i = 0; i < to.length; i++) {
                    out.print(((tf) ? " " : ", ") + getDisplayAddress(to[i]));
                    tf = false;
                }
                out.println("<br>");
            }

            Address[] cc = msg.getRecipients(Message.RecipientType.CC);
            if (cc != null) {
                boolean cf = true;
                out.print("<b>CC:</b> ");
                for (int i = 0; i < cc.length; i++) {
                    out.print(((cf) ? " " : ", ") + getDisplayAddress(cc[i]));
            cf = false;
            }
                    out.println("<br>");
                }

            out.print("<b>Subject:</b> " +
                  ((msg.getSubject() !=null) ? msg.getSubject() : "") +
                  "<br>");

        } catch (MessagingException mex) {
            out.println(msg.toString());
        }   
    }

}
