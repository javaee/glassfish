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

/**
 * <p>Exposes the current status of
 * an iteration.  JSTL provides a mechanism for LoopTags to
 * return information about the current index of the iteration and
 * convenience methods to determine whether or not the current round is
 * either the first or last in the iteration.  It also lets authors
 * use the status object to obtain information about the iteration range,
 * step, and current object.</p>
 *
 * <p>Environments that require more status can extend this interface.</p>
 *
 * @author Shawn Bayern
 */

public interface LoopTagStatus {

    /**
     * Retrieves the current item in the iteration.  Behaves
     * idempotently; calling getCurrent() repeatedly should return the same
     * Object until the iteration is advanced.  (Specifically, calling
     * getCurrent() does <b>not</b> advance the iteration.)
     *
     * @return the current item as an object
     */
    public Object getCurrent();

    /**
     * Retrieves the index of the current round of the iteration.  If
     * iteration is being performed over a subset of an underlying
     * array, java.lang.Collection, or other type, the index returned
     * is absolute with respect to the underlying collection.  Indices
     * are 0-based.
     *
     * @return the 0-based index of the current round of the iteration
     */
    public int getIndex();

    /**
     * <p>Retrieves the "count" of the current round of the iteration.  The
     * count is a relative, 1-based sequence number identifying the
     * current "round" of iteration (in context with all rounds the
     * current iteration will perform).</p>
     *
     * <p>As an example, an iteration with begin = 5, end = 15, and step =
     * 5 produces the counts 1, 2, and 3 in that order.</p>
     *
     * @return the 1-based count of the current round of the iteration
     */
    public int getCount();

    /**
     * Returns information about whether the current round of the
     * iteration is the first one.  This current round may be the 'first'
     * even when getIndex() != 0, for 'index' refers to the absolute
     * index of the current 'item' in the context of its underlying
     * collection.  It is always that case that a true result from
     * isFirst() implies getCount() == 1.
     * 
     * @return <tt>true</tt> if the current round is the first in the
     * iteration, <tt>false</tt> otherwise.
     */
    public boolean isFirst();

    /**
     * Returns information about whether the current round of the
     * iteration is the last one.  As with isFirst(), subsetting is
     * taken into account.  isLast() doesn't necessarily refer to the
     * status of the underlying Iterator; it refers to whether or not
     * the current round will be the final round of iteration for the
     * tag associated with this LoopTagStatus.
     * 
     * @return <tt>true</tt> if the current round is the last in the
     * iteration, <tt>false</tt> otherwise.
     */
    public boolean isLast();

    /**
     * Returns the value of the 'begin' attribute for the associated tag,
     * or null if no 'begin' attribute was specified.
     *
     * @return the 'begin' value for the associated tag, or null
     * if no 'begin' attribute was specified
     */
    public Integer getBegin();

    /**
     * Returns the value of the 'end' attribute for the associated tag,
     * or null if no 'end' attribute was specified.
     *
     * @return the 'end' value for the associated tag, or null
     * if no 'end' attribute was specified
     */
    public Integer getEnd();

    /**
     * Returns the value of the 'step' attribute for the associated tag,
     * or null if no 'step' attribute was specified.
     *
     * @return the 'step' value for the associated tag, or null
     * if no 'step' attribute was specified
     */
    public Integer getStep();

}
