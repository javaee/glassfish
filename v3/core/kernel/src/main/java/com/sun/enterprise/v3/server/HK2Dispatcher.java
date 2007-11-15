/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * Header Notice in each file and include the License file 
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.  
 * If applicable, add the following below the CDDL Header, 
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */

package com.sun.enterprise.v3.server;

import com.sun.grizzly.tcp.Adapter;
import com.sun.grizzly.tcp.Request;
import com.sun.grizzly.tcp.Response;

import java.lang.reflect.Field;
import java.lang.ref.WeakReference;
import java.util.Set;
import java.util.HashSet;


public class HK2Dispatcher {

    Field threadLocalsField = null;
    Field tableField = null;
    Field hashCode = null;
    Field value;

    private void init() {
        try {
            threadLocalsField = Thread.class.getDeclaredField("threadLocals");
            threadLocalsField.setAccessible(true);
            Class c = Class.forName("java.lang.ThreadLocal$ThreadLocalMap");
            tableField = c.getDeclaredField("table");
            tableField.setAccessible(true);
            hashCode = ThreadLocal.class.getDeclaredField("threadLocalHashCode");
            hashCode.setAccessible(true);
            c = Class.forName("java.lang.ThreadLocal$ThreadLocalMap$Entry");
            value = c.getDeclaredField("value");
            value.setAccessible(true);

        } catch(NoSuchFieldException e) {
            e.printStackTrace();
            
        } catch(ClassNotFoundException e) {
            e.printStackTrace();
            
        }
    }


    public void dispath(Adapter adapter, ClassLoader cl, Request req, Response res) {

        // save the thread local entries.
        Thread thread = Thread.currentThread();
/*        if (threadLocalsField==null) {
            init();
        }
        Set<Integer> entries = new HashSet();
        if (threadLocalsField!=null) {

            try {

                Object threadLocals = threadLocalsField.get(thread);
                WeakReference<ThreadLocal>[] table = (WeakReference<ThreadLocal>[]) tableField.get(threadLocals);
                int len = table.length;
                for (int j = 0; j < len; j++) {
                    WeakReference<ThreadLocal> e = table[j];
                    if (e != null) {
                        entries.add(hashCode.getInt(e.get()));
                        //System.out.println("Hashcode = " + hashCode.get(e.get()));
                        //System.out.println("Value = " + value.get(e));
                    }
                }
            } catch(IllegalAccessException e) {

            }
*/
            ClassLoader currentCL = thread.getContextClassLoader();
            try {
                if (cl==null) {
                    cl = adapter.getClass().getClassLoader();
                }
                Thread.currentThread().setContextClassLoader(cl);
                // wrap Request to intercept set/getNote
                adapter.service(req, res);
            } catch(Exception e) {
                // log.
                // swallows...

            } finally {
                thread.setContextClassLoader(currentCL);
            }
/*
            // same thing again...
            try {

                Object threadLocals = threadLocalsField.get(thread);
                WeakReference<ThreadLocal>[] table = (WeakReference<ThreadLocal>[]) tableField.get(threadLocals);
                int len = table.length;
                for (int j = 0; j < len; j++) {
                    WeakReference<ThreadLocal> e = table[j];
                    if (e != null) {
                        if (!entries.contains(hashCode.getInt(e.get()))) {
                            //System.out.println("Added Thread local Hashcode = " + hashCode.get(e.get()));
                            //System.out.println("Value = " + value.get(e));
                        }
                    }
                }
            } catch(IllegalAccessException e) {

            }            


        } else {
            // no thread local protection available

        }
*/
    }
}
