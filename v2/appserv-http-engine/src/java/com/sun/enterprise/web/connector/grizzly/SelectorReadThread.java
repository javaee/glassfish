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
package com.sun.enterprise.web.connector.grizzly;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.Selector;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;


/**
 * Specialized <code>SelectorThread</code> that only handle OP_READ.
 *
 * @author Scott Oaks
 * @author Jean-Francois Arcand
 */
public class SelectorReadThread extends SelectorThread 
        implements MultiSelectorThread{

    /**
     * List of <code>Channel<code> to process.
     */
    ArrayList<SocketChannel> channels = new ArrayList<SocketChannel>();


    /**
     * Int used to differenciate thsi instance
     */
    public static int countName;

    
    /**
     * Add a <code>Channel</code> to be processed by this
     * <code>Selector</code>
     */
    public synchronized void addChannel(SocketChannel channel) 
            throws IOException, ClosedChannelException {
        channels.add(channel);
        getSelector().wakeup();
    }


    /**
     * Register all <code>Channel</code> with an OP_READ opeation.
     */
    private synchronized void registerNewChannels() throws IOException {
        int size = channels.size();
        for (int i = 0; i < size; i++) {
            SocketChannel sc = channels.get(i);
            sc.configureBlocking(false);
            try {
                SelectionKey readKey = 
                        sc.register(getSelector(), SelectionKey.OP_READ);
                setSocketOptions(((SocketChannel)readKey.channel()).socket());
            } catch (ClosedChannelException cce) {
            }
        }
        channels.clear();
    }

    
    /**
     * Initialize this <code>SelectorThread</code>
     */
    public void initEndpoint() throws IOException, InstantiationException { 
        setName("SelectorReaderThread-" + getPort());
        initAlgorithm();
    }
    
    
    /**
     * Start and wait for incoming connection
     */
    public void startEndpoint() throws IOException, InstantiationException {
        setRunning(true);
        while (isRunning()) {
            try{
                if ( getSelector() == null ){
                    setSelector(Selector.open());
                }              
                
                registerNewChannels();
                doSelect();
            } catch (Throwable t){
                logger.log(Level.FINE,"selectorThread.errorOnRequest",t);
            }
        }
    }


    /**
     * Return a <code>ReadTask</code> configured to use this instance.
     */
    public ReadTask getReadTask(SelectionKey key) throws IOException{
        ReadTask task = super.getReadTask(key);
        task.setSelectorThread(this);
        return task;
    }

    
    /**
     * Provides the count of request threads that are currently
     * being processed by the container
     *
     * @return Count of requests 
     */
    public int getCurrentBusyProcessorThreads() {
        return (getProcessorPipeline().getCurrentThreadsBusy());
    }
    
}
