/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.grizzly.comet;

import com.sun.enterprise.web.connector.grizzly.comet.*;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Simple CometChat.
 *
 * @author Jeanfrancois Arcand
 */
public class CometServlet extends HttpServlet{
    
    private String contextPath;
    
    static int firstServlet = -1;
     
    public CometServlet() {
    }

    public void init(ServletConfig config) throws ServletException { 
        super.init(config);
        contextPath = config.getServletContext().getContextPath() + "/chat";
        CometEngine cometEngine = CometEngine.getEngine();
        CometContext context = cometEngine.register(contextPath);    
        context.setExpirationDelay(20 * 1000);
    }
   
    
    public void doGet(HttpServletRequest request, HttpServletResponse response) 
                                        throws ServletException, IOException {
        doPost(request,response);
    }   
    
    
    public void doPost(HttpServletRequest request, HttpServletResponse response) 
                                        throws ServletException, IOException {
        
            String action = request.getParameter("action");
            CometEngine cometEngine = CometEngine.getEngine();
            CometContext cometContext = cometEngine.getCometContext(contextPath);

            if (action != null) {              
                if ("login".equals(action)) {
                    String username = request.getParameter("username");
                    request.getSession(true).setAttribute("username", username);                    
                             
                    if (firstServlet != -1){
                         cometContext.notify("User " + username 
                          + " from " + request.getRemoteAddr()
                          + " is joinning the chat.<br/>",CometEvent.NOTIFY,
                                 firstServlet); 
                    }
                    
                    response.sendRedirect("chat.jsp");
                    return;
                } else if ("post".equals(action)){
                    String username = (String) request.getSession(true)
                        .getAttribute("username");
                    String message = request.getParameter("message");
                    cometContext.notify("[ " + username + " ]  " 
                            + message + "<br/>");
                    response.sendRedirect("post.jsp");   
                    return;
                } else if ("openchat".equals(action)) {
                    response.setContentType("text/html");
                    String username = (String) request.getSession(true)
                        .getAttribute("username");
                    response.getWriter().println("<h2>Welcome " 
                            + username + " </h2>");
                    
                    CometRequestHandler handler = new CometRequestHandler();
                    handler.clientIP = request.getRemoteAddr();
                    handler.attach(response.getWriter());
                    cometContext.addCometHandler(handler);
                    return;
                } else if ("openchat_admin".equals(action)) {
                    response.setContentType("text/html");
                    CometRequestHandler handler = new CometRequestHandler();
                    handler.attach(response.getWriter());
                    if (firstServlet == -1){
                        handler.clientIP = request.getRemoteAddr();
                        firstServlet = cometContext.addCometHandler(handler);
                        response.getWriter().println("<h2>Master Chat Window</h2>");
                    } else {
                        response.getWriter()
                            .println("<h2>Moderator already logged</h2>");
                    }
                    return;
                }
                
            } 
    }

    // --------------------------------------------------------- Async Hook ---/

    public class CometRequestHandler implements CometHandler<PrintWriter>{
        
        private PrintWriter printWriter;
        
        public String clientIP;
        
        public void attach(PrintWriter printWriter){
            this.printWriter = printWriter;
        }
                
                
        public void onEvent(CometEvent event) throws IOException{   
            try{
                
                if (firstServlet != -1 && this.hashCode() != firstServlet){
                     event.getCometContext().notify("User " + clientIP
                      + " is getting a new message.<br/>",CometEvent.NOTIFY,
                             firstServlet); 
                }      
                if ( event.getType() != CometEvent.READ ){
                    printWriter.println(event.attachment());
                    printWriter.flush();
                }
            } catch (Throwable t){
               t.printStackTrace(); 
            }  
        }

        
        public void onInitialize(CometEvent event) throws IOException{  
            printWriter.println("<!doctype html public \"-//w3c//dtd html 4.0 transitional//en\">");
            printWriter.println("<html><head><title>Chat</title></head><body bgcolor=\"#FFFFFF\">");
            printWriter.flush();
        }


        public void onTerminate(CometEvent event) throws IOException{
            onInterrupt(event);
        }


        public void onInterrupt(CometEvent event) throws IOException{
            
            if (this.hashCode() == firstServlet) {
                firstServlet = -1;
            }
            
            printWriter.println("Chat closed<br/>");
            printWriter.println("</body></html>");
            printWriter.flush();
            printWriter.close();
        }        
    }
}
