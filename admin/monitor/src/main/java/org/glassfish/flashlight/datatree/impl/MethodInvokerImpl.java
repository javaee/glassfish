/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.flashlight.datatree.impl;

import org.glassfish.flashlight.datatree.MethodInvoker;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
/**
 *
 * @author Harpreet Singh
 */
public class MethodInvokerImpl extends AbstractTreeNode implements MethodInvoker {
    Method method;
    Object methodInstance;

    public void setMethod (Method m){
        method = m;
    }
    public Method getMethod (){
        return method;
    }
    
    public void setInstance (Object i){
        methodInstance = i;
    }
    
    public Object getInstance (){
    
        return methodInstance;
    }
    
    @Override
    // TBD Put Logger calls.
    public Object getValue (){
        Object retValue = null;
        try {
            if (method == null){
                throw new RuntimeException ("Flashlight:MethodInvoker: method, " +
                        "is null - cannot be null.");
            }
            if (methodInstance == null)
                 throw new RuntimeException ("Flashlight:MethodInvoker: object, " +
                        " instance is null - cannot be null.");
                
            if (super.isEnabled())
                retValue = method.invoke(methodInstance, null);
        } catch (IllegalAccessException ex) {
            
            // Logger.getLogger(MethodInvokerImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalArgumentException ex) {
            // Logger.getLogger(MethodInvokerImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvocationTargetException ex) {
            // Logger.getLogger(MethodInvokerImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return retValue;
    }

}
