/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.hk2.hk2Config.xml.test.listeners;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;

import org.glassfish.hk2.xml.hk2Config.test.beans.Phylum;

/**
 * @author jwells
 *
 */
public class DaveHatingListener implements VetoableChangeListener {

    /* (non-Javadoc)
     * @see java.beans.VetoableChangeListener#vetoableChange(java.beans.PropertyChangeEvent)
     */
    @Override
    public void vetoableChange(PropertyChangeEvent evt)
            throws PropertyVetoException {
        if (evt.getSource() == null) return;
        if (!(evt.getSource() instanceof Phylum)) return;
        
        if ((evt.getNewValue() != null) && (evt.getNewValue() instanceof String)) {
            String newValue = (String) evt.getNewValue();
            if (ListenersTest.DAVE_NAME.equals(newValue)) {
                throw new PropertyVetoException(ListenersTest.EXPECTED_MESSAGE, evt);
            }
            if (ListenersTest.CAROL_NAME.equals(newValue)) {
                // In this case we throw something OTHER than PVE
                throw new IllegalStateException(ListenersTest.EXPECTED_MESSAGE2);
            }
            
            return;
        }
        
        if ((evt.getOldValue() == null) && (evt.getNewValue() != null) &&
                (evt.getNewValue() instanceof Phylum)) {
            // What?!? Trying to add a Dave? How dare you!
            Phylum newValue = (Phylum) evt.getNewValue();
            if (newValue.getName() == null) {
                throw new PropertyVetoException("Cannot add a Phylum with no name", evt);
            }
            if (ListenersTest.DAVE_NAME.equals(newValue.getName())) {
                throw new PropertyVetoException(ListenersTest.EXPECTED_MESSAGE, evt);
            }
        }
        
    }

}
