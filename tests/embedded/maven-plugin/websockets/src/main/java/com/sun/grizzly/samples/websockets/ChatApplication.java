/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package com.sun.grizzly.samples.websockets;

import com.sun.grizzly.tcp.Request;
import com.sun.grizzly.websockets.DataFrame;
import com.sun.grizzly.websockets.NetworkHandler;
import com.sun.grizzly.websockets.WebSocket;
import com.sun.grizzly.websockets.WebSocketApplication;
import com.sun.grizzly.websockets.WebSocketListener;

import java.io.IOException;

public class ChatApplication extends WebSocketApplication {
    @Override
    public boolean isApplicationRequest(Request request) {
        return request.requestURI().equals("/chat");
    }

    @Override
    public WebSocket createSocket(WebSocketListener... listeners) throws IOException {
        return new ChatWebSocket();
    }

    public void onMessage(WebSocket socket, DataFrame frame) throws IOException {
        final String data = frame.getTextPayload();
        if (data.startsWith("login:")) {
            login((ChatWebSocket) socket, frame);
        } else {
            broadcast(((ChatWebSocket) socket).getUser() + " : " + data);
        }
    }

    private void broadcast(String text) throws IOException {
        WebSocketsServlet.logger.info("Broadcasting : " + text);
        for (WebSocket webSocket : getWebSockets()) {
            try {
                webSocket.send(text);
            } catch (IOException e) {
                e.printStackTrace();
                WebSocketsServlet.logger.info("Removing chat client: " + e.getMessage());
                webSocket.close();
            }
        }

    }

    private void login(ChatWebSocket socket, DataFrame frame) throws IOException {
        if (socket.getUser() == null) {
            WebSocketsServlet.logger.info("ChatApplication.login");
            socket.setUser(frame.getTextPayload().split(":")[1].trim());
            broadcast(socket.getUser() + " has joined the chat.");
        }
    }
}
