/*
 * Copyright 2005 Sun Microsystems, Inc. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Sun Microsystems nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.sun.javaee.blueprints.components.ui.model;
import com.sun.faces.config.beans.ManagedBeanBean;
import com.sun.faces.spi.ManagedBeanFactory;
import com.sun.faces.spi.ManagedBeanFactory.Scope;
import com.sun.faces.spi.ManagedBeanFactoryWrapper;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.ejb.PostConstruct;
import javax.ejb.PreDestroy;
import javax.faces.context.FacesContext;

/**
 *
 * @author edburns
 */
public class LifecycleManagedBeanFactory extends ManagedBeanFactoryWrapper {
    
    private ManagedBeanFactory parent = null;
    
    public static ManagedBeanFactory mostRecentMBF = null;
    
    /** Creates a new instance of NewManagedBeanFactory */
    public LifecycleManagedBeanFactory(ManagedBeanFactory old) {
        this.parent = old;
        mostRecentMBF = this;
    }
    
    public ManagedBeanFactory getWrapped() {
        return parent;
    }
    
    public Object newInstance(FacesContext context) {
        Object result = getWrapped().newInstance(context);
        if (null != result) {
            Class resultClass = result.getClass();
            Method [] methods = resultClass.getMethods();
            PostConstruct postAnno = null;
            PreDestroy preAnno = null;
            List<Object []> scopeDestroyList = null;
            
            for (int i = 0; i < methods.length; i++) {
                String name = methods[i].getName();
                // If the bean wants a call on create
                if (null != (postAnno =
                        methods[i].getAnnotation(PostConstruct.class))) {
                    try {
                        methods[i].invoke(result);
                        
                    } catch (IllegalAccessException accessE) {
                        
                    } catch (IllegalArgumentException argumentE) {
                        
                    } catch (InvocationTargetException ite) {
                        
                    }
                }
                // If the bean wants a call on destroy 
                if (null != (preAnno =
                        methods[i].getAnnotation(PreDestroy.class))) {
                    if (null != (scopeDestroyList =
                            getScopeDestroyList(context))) {
                        Object methodAndInstance[] = { methods[i], result };
                        scopeDestroyList.add(methodAndInstance);
                    }
                }
                
            }
        }
           
        return result;
    }
    
    private List<Object []> getScopeDestroyList(FacesContext context) {
        Scope scope = getWrapped().getScope();
        List result = null;
        Map scopeMap = null;
        
        if (scope == Scope.REQUEST) {
            scopeMap = context.getExternalContext().getRequestMap();
        }
        else if (scope == Scope.SESSION) {
            scopeMap = context.getExternalContext().getSessionMap();
        }
        else if (scope == Scope.APPLICATION) {
            scopeMap = context.getExternalContext().getApplicationMap();
        }
        if (null != scopeMap) {
            if (null == (result = (List) scopeMap.get("components.model"))) {
                result = new ArrayList<Object []>();
                scopeMap.put("components.model", result);
            }
        }
        return result;        
    }
    
}

