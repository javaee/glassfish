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
package org.glassfish.config.support;

import org.jvnet.hk2.config.ConfigView;
import org.jvnet.hk2.config.ConfigBeanProxy;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * View that translate configured attributes containing properties like ${foo.bar}
 * into system properties values.
 *
 * @author Jerome Dochez
 */
public class TranslatedConfigView implements ConfigView {

    final static Pattern p = Pattern.compile("([^\\$]*)\\$\\{([^\\}]*)\\}([^\\$]*)");

    public static Object getTranslatedValue(Object value) {
        if (value!=null && value instanceof String) {
            String stringValue = value.toString();
            if (stringValue.indexOf('$')==-1) {
                return value;
            }
            Matcher m = p.matcher(stringValue);
            while (m.find()) {
                String newValue = System.getProperty(m.group(2).trim());
                if (newValue!=null) {
                    stringValue = m.replaceFirst(m.quoteReplacement(m.group(1)+
                            System.getProperty(m.group(2).trim())+m.group(3)));
                    m.reset(stringValue);
                }
            }
            return stringValue;
        }
        return value;
    }

    final ConfigView masterView;

    TranslatedConfigView(ConfigView master) {
        this.masterView = master;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return getTranslatedValue(masterView.invoke(proxy, method, args));
    }

    public ConfigView getMasterView() {
        return masterView;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setMasterView(ConfigView view) {
        // immutable implementation
    }

    public <T extends ConfigBeanProxy> Class<T> getProxyType() {
        return masterView.getProxyType();
    }

    public <T extends ConfigBeanProxy> T getProxy(Class<T> proxyType) {
        return proxyType.cast(Proxy.newProxyInstance(proxyType.getClassLoader(), new Class[]{proxyType},
                 this));
    }
}