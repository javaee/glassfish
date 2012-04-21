/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2008-2010 Oracle and/or its affiliates. All rights reserved.
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

package org.jvnet.hk2.tracing;

import com.sun.hk2.component.ExistingSingletonInhabitant;
import org.jvnet.hk2.component.Inhabitant;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;


/**
 * Utilities for tracing hk2 usage at runtime.
 *
 * @author Jerome Dochez
 */
public class TracingUtilities {

    private final static boolean enabled = Boolean.getBoolean("hk2.module.tracestate");
    
    public static boolean isEnabled() {
        return enabled;
    }

    public static class Node {
        final Inhabitant t;
        final long inception = System.currentTimeMillis();
        long completion;

        final List<Node> children = new ArrayList<Node>();

        public Node(Inhabitant t) {
            this.t = t;
        }

        public void done() {
            completion = System.currentTimeMillis();
        }

        public long elapsed() {
            return completion-inception;
        }

        private void dump(String prefix, PrintStream ps) {
            StringBuffer buffer = new StringBuffer();
            for (int i=0;i<prefix.length();i++) {
                buffer.append("|");
            }
            buffer.append("->");
            buffer.append(" Inhabitant : ").append(t.typeName()).append(" initialized at ").
                    append(inception).append(" took ").append(elapsed());
            ps.println(buffer);
            
            for (Node child : new ArrayList<Node>(children)) {
                child.dump(prefix+"  ", ps);
            }
        }
    }

    public static final Node rootNode = new Node(new ExistingSingletonInhabitant<TracingUtilities>(new TracingUtilities()));

    public static void dump(PrintStream ps) {
        for (Node node : new ArrayList<Node>(rootNode.children))  {
            node.dump("", ps);
        }
    }

    public static void dump(String typeName, PrintStream ps) {
        dump(typeName, rootNode, ps);
    }

    public static void dump(String typeName, Node node, PrintStream ps) {

        for (Node child : new ArrayList<Node>(node.children)) {
            if (child.t.typeName().equals(typeName)) {
                child.dump("", ps);
                return;
            } else {
                dump(typeName, child, ps);
            }
        }
    }
    
}
