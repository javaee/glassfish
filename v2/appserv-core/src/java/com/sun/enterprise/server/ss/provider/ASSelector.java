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
package com.sun.enterprise.server.ss.provider;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.nio.channels.spi.AbstractSelector;
import java.nio.channels.spi.SelectorProvider;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.enterprise.server.ss.spi.ASSocketFacadeUtils;
import com.sun.enterprise.server.ss.util.ASSet;
import com.sun.enterprise.server.ss.util.ASWrapperCreator;
import com.sun.logging.LogDomains;

/**
 * Selector implementation of the quick appserver startup implementation.
 * JDK's NIO implementation only recognises its own channel implementation.
 * It doesnt accept any other implementation for select registrations.
 * This implementation make sure that JDK's NIO implementation gets only 
 * the JDK's channels.
 */
public class ASSelector extends AbstractSelector implements ASWrapperCreator{
    private static final Logger logger = LogDomains.getLogger(LogDomains.CORE_LOGGER);
    private AbstractSelector sel = null;

    private boolean wakenup = false;
    private boolean wrapSelectionKeys = false;
    private int port = 0;

    ASSelector(AbstractSelector sel, SelectorProvider provider) {
        super(provider);
        this.sel = sel;
    }

    protected void implCloseSelector() throws IOException {
        if ( logger.isLoggable(Level.FINER) ) {
            logger.log(Level.FINER, "Selector is getting closed :" + sel);
        }
        sel.close();
    }

    protected SelectionKey register(AbstractSelectableChannel ac,
				    int ops, Object obj) { 
        try {
            if (ac instanceof ASChannel) {
                ASChannel asc = (ASChannel) ac;
                SelectableChannel sc = asc.getActualChannel();

                if (ac instanceof ASServerSocketChannel && 
                    ops == SelectionKey.OP_ACCEPT) {
                    this.port = ((ASServerSocketChannel)ac).getPortNumber();
                    wrapSelectionKeys = true;
                }
                return sc.register(sel, ops, obj);
            } else {
                if ( logger.isLoggable(Level.FINEST) ) {
	            logger.log(java.util.logging.Level.FINEST, 
                    "In ASSelector.register channel class = " 
                    + ac.getClass().getName(), new Exception());
                }
                return ac.register(sel, ops, obj);
            }
        } catch (ClosedChannelException ce) {
            throw new RuntimeException (ce);
        }
    }

    public java.util.Set keys() {
        if (wrapSelectionKeys && 
            ASSocketFacadeUtils.getASSocketService().isServerStartingUp(port)) {
            return new ASSet(sel.keys(), this);
        } else {
            return sel.keys();
        }
    }

    public java.util.Set selectedKeys() {
        if (wrapSelectionKeys && 
            ASSocketFacadeUtils.getASSocketService().isServerStartingUp(port)) {
            return new ASSet(sel.selectedKeys(), this);
        } else {
            return sel.selectedKeys();
        }
 
    }

    public Selector wakeup() {
        wakenup = true;
        return sel.wakeup();
    }

    public int select(long l) throws IOException {
        return sel.select(l);
    }

    public int selectNow() throws IOException {
        return sel.selectNow();
    }

    public int select() throws IOException {
        return sel.select();
    }

    boolean wakenUp() {
        boolean result = wakenup;
        wakenup = false;
        return result;
    }

    public Object wrapIfNecessary(Object next) {
       SelectionKey selKey = (SelectionKey) next;
        if ((selKey.interestOps() & SelectionKey.OP_ACCEPT) != 0) {
            ASServerSocketChannel channel = new ASServerSocketChannel(
            (ServerSocketChannel) selKey.channel(), this.sel.provider());
            SelectionKey key = new ASSelectionKey(channel, selKey, this);
            key.attach(selKey.attachment());
            return key;
        } else {
            return selKey;
        }
    }

    public boolean equals(Object obj) {
        if (obj == this.sel) return true;

        if (obj instanceof Selector) {
            return ((Selector) obj).equals(this.sel);
        }
        return false;
    }

    public int hashCode() {
        return this.sel.hashCode();
    }

    public Selector getSelector() {
        return sel;
    }

}
