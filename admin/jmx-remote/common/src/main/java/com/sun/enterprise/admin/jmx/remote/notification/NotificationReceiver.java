/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * Header Notice in each file and include the License file 
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.  
 * If applicable, add the following below the CDDL Header, 
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */

package com.sun.enterprise.admin.jmx.remote.notification;

import java.io.IOException;
import java.io.EOFException;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.ObjectInputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.nio.channels.ClosedChannelException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.rmi.ConnectException;
import java.rmi.ConnectIOException;
import java.net.URLConnection;
import java.net.HttpURLConnection;
import java.util.logging.Logger;

import com.sun.enterprise.admin.jmx.remote.notification.NotificationWrapper;
import com.sun.enterprise.admin.jmx.remote.comm.HttpConnectorAddress;
import com.sun.enterprise.admin.jmx.remote.DefaultConfiguration;

/**
 * This is the notification receiver thread.
 * This thread makes the connection to the server-side and starts to receive
 * the notifications.
 * During a JMXConnector.close call, this thread would send a close message to the
 * server-side and would exit.
 */
class NotificationReceiver implements Runnable {
    private ClientNotificationManager mgr = null;

    private String notifMgrUri        = null;
    private HttpConnectorAddress ad   = null;
    private URLConnection mConnection = null;
    private ObjectInputStream objIn   = null;
    private InputStream   in          = null;
    private boolean       exit        = false;
    private boolean       connected   = false;
    private boolean       timedout    = false;
    private int           nReconnected= 0;
    private Thread        receiveThr  = null;

    private static final Logger logger = Logger.getLogger(
        DefaultConfiguration.JMXCONNECTOR_LOGGER);/*, 
        DefaultConfiguration.LOGGER_RESOURCE_BUNDLE_NAME );*/

    public NotificationReceiver(HttpConnectorAddress ad, ClientNotificationManager mgr)
            throws IOException {
        this.mgr = mgr;
        this.ad = ad;
        this.notifMgrUri = getNotifMgrURI();
        connect();
        receiveThr = new Thread(this);
        receiveThr.start();
    }

    private String getNotifMgrURI() {
        String uri = ad.getPath();
        if (uri == null || uri.trim().length() == 0)
            uri = DefaultConfiguration.DEFAULT_SERVLET_CONTEXT_ROOT;
        uri = uri +
              DefaultConfiguration.NOTIF_MGR_PATHINFO +
              "?" + DefaultConfiguration.NOTIF_ID_PARAM +
              "=" + mgr.getId();
        return uri;
   }

    private void connect() throws IOException {
        if (connected)
            return;
        connect(null, false);
    }

    private void connect(String cmd, boolean disconnect) throws IOException {
		try{
            String uri = notifMgrUri;
            if (cmd != null)
                uri = uri + "&" +
                      DefaultConfiguration.NOTIF_CMD_PARAM +
                      "=" + DefaultConfiguration.NOTIF_CMD_CLOSE;
            System.setProperty("sun.net.client.defaultConnectTimeout",
                               Integer.toString(DefaultConfiguration.NOTIF_CONNECT_TIMEOUT)); // XXX: For Sun JDK only
            URLConnection conn = ad.openConnection(uri);
//            conn.setConnectTimeout(2000); // XXX: For jdk 1.5
            InputStream inStream = conn.getInputStream();
            if (!disconnect) {
                mConnection = conn;
                in = inStream;
                connected = true;
                nReconnected = 0;
            } else {
                disconnect(conn, inStream);
            }
		} catch (IOException ioe){
		ioe.printStackTrace();
            throw (ioe);
		}
    }

    /**
     * Reinitialized the connection, if the connection is dropped.
     * This method is invoked from ClientNotificationManager every time the client
     * invokes a method on MBeanServerConnection.
     */
    public boolean reinit() throws IOException {
        if (connected)
            return true;

        timedout = false;
        try {
            connect();
        } catch (IOException ioe) {
            timedout = true;
            throw ioe;
        }
        nReconnected = 0;
        receiveThr = new Thread(this);
        receiveThr.start();
        return true;
    }

    /**
     * Returns if the receiver has timedout while trying to receive
     * notitifications from the server.
     * Timedout -- If a socket timeout happens while trying to connect
     *             If the server is not reachable 3 times consequtively during thread loop.
     *             If not able to connect when reinit is called.
     */
    public boolean hasTimedout() {
        return timedout;
    }

    /**
     * The notification receiver thread loop.
     * This loop, if client has not called JMXConnector.close, will
     * try to read a notification message from the connection and notify
     * the ClientNotificationManager to dispatch the notification.
     */
    public void run() {
        while (!isExiting()) {
            try {
                connect();
                readNotification();
            } catch (IOException ioe) {
                if (isExiting())
                    break;
                ioe.printStackTrace();
                if (ioe instanceof SocketTimeoutException) {
                    timedout = true;
                    break;
                }
                if (isDisconnected(ioe)) {
                    connected = false;
                    nReconnected++;
                    if (nReconnected > 3) {
                        timedout = true;
                        break;
                    }
                    continue;
                } else if (isExiting()) {
                    break;
                }
            }
        }
    }

    private boolean isDisconnected(IOException ioe) {
        if (ioe instanceof ClosedChannelException ||
            ioe instanceof SocketException ||
            ioe instanceof ConnectException ||
            ioe instanceof ConnectIOException ||
            ioe instanceof EOFException)
            return true;
        return false;
    }

    private void disconnect() throws IOException {
        disconnect(mConnection, in);
    }

    private void disconnect(URLConnection conn, InputStream in) throws IOException {
        if (conn instanceof HttpURLConnection) {
            ((HttpURLConnection)conn).disconnect();
        } else
            in.close();
    }

    public void exit() throws Exception {
        exit = true;
        sendCloseMessage();
        disconnect();
        receiveThr.join();
    }

    private void sendCloseMessage() throws IOException {
        connect("close", true);
    }

    private boolean isExiting() {
        return exit;
    }

    private void readNotification() throws IOException {
        Object obj = null;
        try {
            objIn = new ObjectInputStream(in);
            obj = objIn.readObject();
        } catch (IOException ioe) {
            String msg = ioe.getMessage();
            if (msg != null && msg.indexOf("EOF") != -1)
                throw (new EOFException(msg));
            throw ioe;
        } catch (ClassNotFoundException notfound) {
            // Ignore; Unknown Object ???
            return;
        }

        NotificationWrapper wrapr = (NotificationWrapper) obj;
        if (wrapr.getType() == NotificationWrapper.WAIT) {
            return;
        }

        mgr.raiseEvent(wrapr);
    }
}
