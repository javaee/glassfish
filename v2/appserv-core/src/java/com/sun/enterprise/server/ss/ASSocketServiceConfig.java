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
package com.sun.enterprise.server.ss;

import java.net.SocketAddress;
import java.net.InetSocketAddress;
import com.sun.enterprise.config.ConfigBean;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.ServerBeansFactory;
import com.sun.enterprise.server.ServerContext;

/**
 * Helper class representing configuration
 * of one Socket Service. 
 *
 * @see ASSocketService
 */
class ASSocketServiceConfig {

    private ConfigBean config = null;
    private String addressTag = null;
    private String portTag = null;
    private int port ;
    private int backlog = 0 ;
    private String address = null;
    private SocketAddress sAddress = null;
    private boolean startSelector = true;

    ASSocketServiceConfig(ConfigBean config) {
        this.config = config;
    }

    void setAddressTag(String addressTag) {
        this.addressTag = addressTag;
    }

    String getAddressTag() {
        return this.addressTag;
    }

    void setPortTag(String  portTag) {
        this.portTag = portTag;
    }

    void setBacklog(int backlog) {
        this.backlog = backlog;
    }

    int getBacklog() {
        if (this.backlog < 0) {
            return 0;
        } else {
            return this.backlog;
        }
    }

    String getPortTag() {
        return this.portTag;
    }

    void setStartSelector(boolean flag) {
        this.startSelector = flag;
    }

    boolean getStartSelector() {
        return this.startSelector;
    }


    int getPort() {
        return this.port;
    }

    String getAddress() {
        if ("any".equals(this.address) || "ANY".equals(this.address)
            || "INADDR_ANY".equals(this.address)) {
            return null;
        } else {
            return this.address;
        }
    }

    SocketAddress getSocketAddress() {
        return this.sAddress;
    }

    void init() {
        this.port = Integer.parseInt(config.getAttributeValue(getPortTag()));

        if (getAddressTag() != null) {
            this.address = config.getAttributeValue(getAddressTag());
        }

        String address = getAddress();
        if (address != null) {
            this.sAddress = new InetSocketAddress(address, getPort());
        } else {
            this.sAddress = new InetSocketAddress(getPort());
        }
    }

    public String toString() {
        return getAddress() + ":" + getPort();
    }
}
