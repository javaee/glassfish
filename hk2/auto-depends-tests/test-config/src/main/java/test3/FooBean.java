/*
 * 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2007-2008 Sun Microsystems, Inc. All rights reserved.
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
package test3;

import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.Element;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Kohsuke Kawaguchi
 */
@Configured(name="domain",symbolSpace=HttpListener.class)
public class FooBean {
    public Exception e;

    @Attribute
    public int httpPort;

    public String bar;
    
    public List<String> jvmOptions = new ArrayList<String>();

    @Element("property")
    public Map<String,Property> properties = new HashMap<String, Property>();

    @Element("http-listener")
    public Map<String,HttpListener> httpListeners = new HashMap<String,HttpListener>();

    @Element("virtual-server")
    public List<VirtualServer> virtualServers = new ArrayList<VirtualServer>();

    @Element("*")
    public List<Object> all = new ArrayList<Object>();

    public FooBean() {
        e = new Exception();
    }

    @Element
    public void setBar(String bar) {
        this.bar = bar;
    }

    @Element
    public void setJvmOptions(List<String> opts) {
        this.jvmOptions.addAll(opts);
    }

    public <T> T find(Class<T> t) {
        for (Object o : all) {
            if(t.isInstance(o))
                return t.cast(o);
        }
        return null;
    }
}
