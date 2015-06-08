/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012-2015 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.examples.http;

import org.glassfish.hk2.api.PerLookup;

/**
 * This receiver will receive a specific kind of HttpRequest
 * where the elements of the request have these types:
 * <OL>
 * <LI>The rank of the request is an integer</LI>
 * <LI>The id of the request is a long</LI>
 * <LI>The requested action is a String</LI>
 * </OL>
 * <p>
 * The getHttpRequest method of this class is annotated with the
 * @author jwells
 *
 */
@PerLookup
public class HttpEventReceiver {
    private int lastRank;
    private long lastId;
    private String lastAction;
    
    /**
     * This method will get called back with the
     * information filled in from the request, either
     * from the Alternate injection resolver or from
     * the system provided three-thirty resolver
     * 
     * @param rank the rank, parameter zero of the HttpRequest (from the alternate)
     * @param id the id, parameter one of the HttpRequest (from the alternate)
     * @param action the action, parameter two of the HttpRequest (from the alternate)
     * @param logger a logger to send interesting messages to
     */
    @AlternateInject
    public void receiveRequest(
            @HttpParameter int rank,
            @HttpParameter(1) long id,
            @HttpParameter(2) String action,
            Logger logger) {
        lastRank = rank;
        lastId = id;
        lastAction = action;
        
        logger.log("I got a message of rank " + lastRank + " and id " + lastId + " and action " + action);
    }

    /**
     * @return the lastRank
     */
    public int getLastRank() {
        return lastRank;
    }

    /**
     * @return the lastId
     */
    public long getLastId() {
        return lastId;
    }

    /**
     * @return the lastAction
     */
    public String getLastAction() {
        return lastAction;
    }
}
