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

package org.glassfish.flashlight.provider;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.glassfish.flashlight.client.ProbeClientInvoker;
import org.glassfish.flashlight.client.ProbeHandle;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.List;
import java.util.ArrayList;
import org.glassfish.api.monitoring.ProbeInfo;
import org.glassfish.flashlight.FlashlightUtils;
import org.glassfish.flashlight.impl.client.DTraceClientInvoker;

public class FlashlightProbe
        implements ProbeHandle, ProbeInfo{

    public FlashlightProbe(int id, Class providerClazz, String moduleProviderName,
    		String moduleName, String probeProviderName, String probeName,
                 String[] probeParamNames, Class[] paramTypes, boolean self, boolean hidden) {
        this.id = id;
        this.providerClazz = providerClazz;
        this.moduleProviderName = moduleProviderName;
        this.moduleName = moduleName;
        this.probeProviderName = probeProviderName;
        this.probeName = probeName;
        this.probeDesc = moduleProviderName + ":" + moduleName + ":" +
                probeProviderName + ":" + probeName;
        this.hasSelf = self;
        this.hidden = hidden;
        
        if (self) {
            if (isMethodStatic())
                throw new RuntimeException("Monitoring: Cannot define \"self\" on a static method - " + probeDesc);
            // Fill in the first slot of ParamNames with @Self and paramTypes with the providerClass type
            this.probeParamNames = new String[probeParamNames.length+1];
            this.paramTypes = new Class[paramTypes.length+1];
            this.probeParamNames[0] = SELF;
            this.paramTypes[0] = providerClazz;
            for (int index = 0; index < probeParamNames.length; index++) {
                this.probeParamNames[index+1] = probeParamNames[index];
                this.paramTypes[index+1] = paramTypes[index];
            }
        } else {
            this.probeParamNames = probeParamNames;
            this.paramTypes = paramTypes;
        }

    }

    private boolean isMethodStatic() {
        boolean isMethodStatic = false;
        try {
            int modifier = getProviderClazz().getDeclaredMethod(getProviderJavaMethodName(),
                                                                    getParamTypes()).getModifiers();
            return Modifier.isStatic(modifier);
        } catch (Exception e) {
            return false;
        }
    }

    public synchronized boolean addInvoker(ProbeClientInvoker invoker) {
    	boolean isFirst = invokerList.size() == 0;
        invokerList.add(invoker);
        listenerEnabled.set(true);
        
        return isFirst;
    }

    public synchronized boolean removeInvoker(ProbeClientInvoker invoker) {
        invokerList.remove(invoker);
        listenerEnabled.set(invokerList.size() > 0);
        
        return listenerEnabled.get();
    }

    public void fireProbe(Object[] params) {
        if(listenerEnabled.get()) {
            for (ProbeClientInvoker invoker : invokerList)
                invoker.invoke(params);
        }
    }

    public boolean isEnabled() {
        return listenerEnabled.get();
    }

    public int getId() {
        return id;
    }

    public String getModuleProviderName() {
		return moduleProviderName;
	}

    public String getModuleName() {
        return moduleProviderName;
    }

	public String getProbeProviderName() {
        return probeProviderName;
    }

    public String getProbeName() {
        return probeName;
    }

    public String[] getProbeParamNames() {
        return probeParamNames;
    }

    public Class[] getParamTypes() {
        return paramTypes;
    }

    public String getProviderJavaMethodName() {
        return providerJavaMethodName;
    }

    public void setProviderJavaMethodName(String providerJavaMethodName) {
        this.providerJavaMethodName = providerJavaMethodName;
    }

    public String getProbeDesc() {
        return probeDesc;
    }

    public Class getProviderClazz() {
		return providerClazz;
	}

	public String toString() {
        StringBuilder sbldr = new StringBuilder(moduleProviderName + ":" + moduleName
        		+ ":" + probeProviderName + ":" + probeName);
        sbldr.append(" ").append(providerJavaMethodName).append("(");
        String delim = "";
        for (Class c : paramTypes) {
            sbldr.append(delim).append((c == null) ? " " : c.getName());
            delim = ", ";
        }
        sbldr.append(")");

        return sbldr.toString();
    }

    public void setDTraceProviderImpl(Object impl) {
        dtraceProviderImpl = impl;
    }

    public Object getDTraceProviderImpl() {
        return dtraceProviderImpl;
    }

    public Method getDTraceMethod() {
        return dtraceMethod;
    }

    public void setDTraceMethod(Method m) {
        dtraceMethod = m;
    }

    public boolean hasSelf() {
        return hasSelf;
    }

    public boolean isHidden() {
        return hidden;
    }

    public static String SELF = "@SELF";
    private int id;
    private Class providerClazz;
    private String moduleProviderName;
    private String moduleName;
    private String probeName;
    private String probeProviderName;
    private String[] probeParamNames;
    private Class[] paramTypes;
    private List<ProbeClientInvoker> invokerList = new ArrayList(2);
    private String providerJavaMethodName;
    private AtomicBoolean listenerEnabled = new AtomicBoolean(false);
    private String probeDesc;
    private Object  dtraceProviderImpl;
    private Method  dtraceMethod;
    private boolean hasSelf;
    private boolean hidden;
}
