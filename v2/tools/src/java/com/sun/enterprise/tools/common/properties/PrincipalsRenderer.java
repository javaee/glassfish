/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 */

/*
 * PrincipalsRenderer.java
 *
 * Created on March 19, 2002, 11:56 AM
 */

package com.sun.enterprise.tools.common.properties;

import javax.swing.*;
import java.util.*;
import java.text.*;

/**
 *
 * @author  shirleyc
 * @version 
 */
public class PrincipalsRenderer extends JLabel implements javax.swing.table.TableCellRenderer {
    
    private static java.util.ResourceBundle bundle =
        java.util.ResourceBundle.getBundle("com.sun.enterprise.tools.common.properties.Bundle"); //NOI18N
    
    public PrincipalsRenderer() {
    }

    public java.awt.Component getTableCellRendererComponent(javax.swing.JTable jTable, java.lang.Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if (value == null) {
            setText(MessageFormat.format(bundle.getString("PLURAL_PRIN_CAPTION"), new Object[] {"0"}));  //NOI18N
        }
        else if (value instanceof Vector) {
            if (((Vector)value).size() == 1)
                setText(bundle.getString("SINGLE_PRIN_CAPTION"));   //NOI18N
            else
                setText(MessageFormat.format(bundle.getString("PLURAL_PRIN_CAPTION"), new Object[] {new Integer(((Vector)value).size())}));   //NOI18N
        }
        else 
            System.out.println(value);            
        return this;
    }
}
