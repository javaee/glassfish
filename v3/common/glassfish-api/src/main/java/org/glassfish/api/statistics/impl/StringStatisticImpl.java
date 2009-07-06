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

package org.glassfish.api.statistics.impl;
import org.glassfish.api.statistics.StringStatistic;
import java.util.Map;
import java.lang.reflect.*;

/** 
 * @author Sreenivas Munnangi
 */
public class StringStatisticImpl extends StatisticImpl
    implements StringStatistic, InvocationHandler {
    
    private String str = null;

    private StringStatistic ss = (StringStatistic) Proxy.newProxyInstance(
            StringStatistic.class.getClassLoader(),
            new Class[] { StringStatistic.class },
            this);

    public StringStatisticImpl(String str, String name, String unit, 
                              String desc, long sampleTime, long startTime) {
        super(name, unit, desc, startTime, sampleTime);
        this.str = str;
    }
    
    public StringStatisticImpl(String name, String unit, String desc) {
        super(name, unit, desc);
    }
    
    public synchronized StringStatistic getStatistic() {
        return ss;
    }

    public synchronized Map getStaticAsMap() {
        Map m = super.getStaticAsMap();
        m.put("name-current", getCurrent());
        return m;
    }

    public String toString() {
        return super.toString() + NEWLINE + "Current-value: " + getCurrent();
    }

    public String getCurrent() {
        return str;
    }

    public void setCurrent(String str) {
        this.str = str;
    }

    // todo: equals implementation
    public Object invoke(Object proxy, Method m, Object[] args) throws Throwable {
        Object result;
        try {
            result = m.invoke(this, args);
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        } catch (Exception e) {
            throw new RuntimeException("unexpected invocation exception: " +
                       e.getMessage());
        } finally {
        }
        return result;
    }
}
