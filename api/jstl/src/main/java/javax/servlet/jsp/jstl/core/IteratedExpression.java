/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 *
 *
 * This file incorporates work covered by the following copyright and
 * permission notice:
 *
 * Copyright 2004 The Apache Software Foundation
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

package javax.servlet.jsp.jstl.core;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Map;
import java.util.StringTokenizer;

import javax.el.ELContext;
import javax.el.ELException;
import javax.el.ValueExpression;

import javax.servlet.jsp.JspTagException;

/**
 * @author Kin-man Chung
 * @version $Id: IteratedExpression.java,v 1.6 2006/11/17 19:48:41 jluehe Exp $
 */
public final class IteratedExpression /*implements Serializable*/ {

    private static final long serialVersionUID = 1L;
    protected final ValueExpression orig;
    protected final String delims;

    private Object base;
    private int index;
    private Iterator iter;

    public IteratedExpression(ValueExpression orig, String delims) {
        this.orig = orig;
        this.delims = delims;
    }

    /**
     * Evaluates the stored ValueExpression and return the indexed item.
     * @param context The ELContext used to evaluate the ValueExpression
     * @param i The index of the item to be retrieved
     */
    public Object getItem(ELContext context, int i) {

        if (base == null) {
            base = orig.getValue(context);
            if (base == null) {
                return null;
            }
            iter = toIterator(base);
            index = 0;
        }
        if (index > i) {
            // Restart from index 0
            iter = toIterator(base);
            index = 0;
        }
        while (iter.hasNext()) {
            Object item = iter.next();
            if (index++ == i) {
                return item;
            }
        }
        return null;
    }

    public ValueExpression getValueExpression() {
        return orig;
    }

    private Iterator toIterator(final Object obj) {

        Iterator iter;
        if (obj instanceof String) {
            iter = toIterator(new StringTokenizer((String)obj, delims));
        }
        else if (obj instanceof Iterator) {
            iter = (Iterator)obj;
        }
        else if (obj instanceof Collection) {
            iter = toIterator(((Collection) obj).iterator());
        }
        else if (obj instanceof Enumeration) {
            iter = toIterator((Enumeration)obj);
        }
        else if (obj instanceof Map) {
            iter = ((Map)obj).entrySet().iterator();
        } else {
            throw new ELException("Don't know how to iterate over supplied "
                                  + "items in forEach");
        }
        return iter;
    }

    private Iterator toIterator(final Enumeration obj) {
        return new Iterator() {
            public boolean hasNext() {
                return obj.hasMoreElements();
            }
            public Object next() {
                return obj.nextElement();
            }
            public void remove() {}
        };
    }
}

