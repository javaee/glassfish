/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sun.grizzly.comet.CometContext;
import com.sun.grizzly.comet.CometEngine;
import com.sun.grizzly.comet.CometEvent;
import com.sun.grizzly.comet.CometHandler;


public class CometEchoServlet extends HttpServlet {
    private String contextPath;

    public class ChatListnerHandler implements CometHandler<PrintWriter> {

        private PrintWriter writer;

        public void attach(PrintWriter writer) {
            this.writer = writer;
        }

        public void onEvent(CometEvent event) throws IOException {
            if (event.getType() == CometEvent.NOTIFY) {
                String output = (String) event.attachment();

                writer.println(output);
                writer.flush();
            }
        }

        public void onInitialize(CometEvent event) throws IOException {
        }

        public void onInterrupt(CometEvent event) throws IOException {
            removeThisFromContext();
        }

        public void onTerminate(CometEvent event) throws IOException {
            removeThisFromContext();
        }

        private void removeThisFromContext() {
            writer.close();

            CometContext context = CometEngine.getEngine().getCometContext(contextPath);
            context.removeCometHandler(this);
        }
    }


    @Override
    public void init(ServletConfig config) throws ServletException {
        contextPath = config.getServletContext().getContextPath() + "/echo";

        CometContext context = CometEngine.getEngine().register(contextPath);
        context.setExpirationDelay(5 * 60 * 1000);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        res.setContentType("text/html");
        res.setHeader("Cache-Control", "private");
        res.setHeader("Pragma", "no-cache");
        
        PrintWriter writer = res.getWriter();
        ChatListnerHandler handler = new ChatListnerHandler();
        handler.attach(writer);

        CometContext context = CometEngine.getEngine().register(contextPath);
        context.addCometHandler(handler);
        writer.println("OK");
        writer.flush();
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        res.setContentType("text/plain");
        res.setHeader("Cache-Control", "private");
        res.setHeader("Pragma", "no-cache");

        req.setCharacterEncoding("UTF-8");
        String message = req.getParameter("msg");

        CometContext context = CometEngine.getEngine().register(contextPath);
        context.notify(message);
        PrintWriter writer = res.getWriter();
        writer.println("OK");
        writer.flush();
    }
}
