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
package org.openide.util;

import java.util.*;


/** General listener for changes in lookup.
 *
 * @author  Jaroslav Tulach
 */
public interface LookupListener extends EventListener {
    /** A change in lookup occured. Please note that this method
     * should never block since it might be called from lookup implementation
     * internal threads. If you block here you are in risk that the thread
     * you wait for might in turn to wait for the lookup internal thread to
     * finish its work.
     * @param ev event describing the change
     */
    public void resultChanged(LookupEvent ev);
}
