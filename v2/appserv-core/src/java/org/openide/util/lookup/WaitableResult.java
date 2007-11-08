/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.openide.util.lookup;

import java.util.Collection;
import org.openide.util.Lookup;


/** A special subclass of lookup that is able to wait before queries.
 *
 * @author  Jaroslav Tulach
 */
abstract class WaitableResult<T> extends Lookup.Result<T> {
    /** Used by proxy results to synchronize before lookup.
     */
    protected abstract void beforeLookup(Lookup.Template t);

    /** Needed to group notification of outside the package listeners
     * after all AbstractLookup and ProxyLookups have been updated.
     * @param evAndListeners LookupEvent, LookupListener, LookupEvent, LookupListener, etc.
     */
    protected abstract void collectFires(Collection<Object> evAndListeners);
     
}
