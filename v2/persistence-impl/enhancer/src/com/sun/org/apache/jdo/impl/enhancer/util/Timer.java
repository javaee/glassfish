/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the "License").  You may not use this file except 
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt or 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html. 
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * HEADER in each file and include the License file at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt.  If applicable, 
 * add the following below this CDDL HEADER, with the 
 * fields enclosed by brackets "[]" replaced with your 
 * own identifying information: Portions Copyright [yyyy] 
 * [name of copyright owner]
 */

/*
 * Copyright 2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at 
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */


package com.sun.org.apache.jdo.impl.enhancer.util;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import java.text.DecimalFormat;

import java.io.PrintWriter;


/**
 * Utility class for simple performance analysis.
 */
public final class Timer
{
    // a method's timing descriptor
    static private class MethodDescriptor
    {
        final String name;
        int instantiations;
        int calls;
        long self;
        long total;

        MethodDescriptor(String name)
        {
            this.name = name;
        }
    }

    // a method call's timing descriptor
    static private class MethodCall
    {
        final MethodDescriptor method;
        final String message;
        long self;
        long total;

        MethodCall(MethodDescriptor method,
                   String message,
                   long self,
                   long total)
        {
            this.method = method;
            this.message = message;
            this.self = self;
            this.total = total;
        }
    }

    // output device
    PrintWriter out = new PrintWriter(System.out, true);

    // methods
    HashMap methods = new HashMap();

    // method call stack
    private final ArrayList calls = new ArrayList(16);
    
    public Timer()
    {
        this.out = out;
    }

    public Timer(PrintWriter out)
    {
        this.out = out;
    }

    public final synchronized void push(String name)
    {
        push(name, name);
    }
    
    public final synchronized void push(String name, String message)
    {
        // get time
        final long now = System.currentTimeMillis();

        // get a method descriptor
        MethodDescriptor current = (MethodDescriptor)methods.get(name);
        if (current == null) {
            current = new MethodDescriptor(name);
            methods.put(name, current);
        }

        // update method descriptor
        current.calls++;
        current.instantiations++;

        // update method call stack
        calls.add(new MethodCall(current, message, now, now));
    }

    public final synchronized void pop()
    {
        // get time
        final long now = System.currentTimeMillis();

        // update method call stack
        final MethodCall call = (MethodCall)calls.remove(calls.size()-1);

        // get current call's time
        final long currentSelf = now - call.self;
        final long currentTotal = now - call.total;

        // update previous call's self time
        if (calls.size() > 0) {
            final MethodCall previous = (MethodCall)calls.get(calls.size()-1);
            previous.self += currentTotal;
        }

        // update method descriptor
        final MethodDescriptor current = call.method;
        current.self += currentSelf;
        if (--current.instantiations == 0) {
            current.total += currentTotal;
        }

        if (false) {
            out.println("Timer (n,g): " + call.message + " : ("
                        + currentSelf + ", " + currentTotal + ")");
        }
    }

    static private final String pad(String s, int i)
    {
        StringBuffer b = new StringBuffer();
        for (i -= s.length(); i > 0; i--)
            b.append((char)' ');
        b.append(s);
        return b.toString();
    }
    
    public final synchronized void print()
    {
        out.println("Timer : printing accumulated times ...");
        final Object[] calls = methods.values().toArray();

        Arrays.sort(calls,
                    new Comparator() {
                            public int compare(Object o1,
                                               Object o2) {
                                return (int)(((MethodDescriptor)o2).total
                                             - ((MethodDescriptor)o1).total);
                            }
                            public boolean equals(Object obj) {
                                return (compare(this, obj) == 0);
                            }
                        });
        
        out.println("Timer :  total s    self s  #calls  name");
        DecimalFormat nf = new DecimalFormat();
        nf.setMaximumFractionDigits(2);
        nf.setMinimumFractionDigits(2);
        //nf.applyPattern("#,##0.00");
        //out.println("Timer : pattern = " + nf.toPattern());
        for (int i = 0; i < calls.length; i++) {
            final MethodDescriptor current = (MethodDescriptor)calls[i];

            out.println("Timer : "
                        + pad(nf.format(current.total / 1000.0), 8) + "  "
                        + pad(nf.format(current.self / 1000.0), 8) + "  "
                        + pad(String.valueOf(current.calls), 6) + "  "
                        + current.name);
        }
    }
}
