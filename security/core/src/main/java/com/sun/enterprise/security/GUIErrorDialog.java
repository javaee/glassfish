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
 * GUIErrorDialog.java
 * An error dialog box used for FailedLogin
 *
 * @author Harpreet Singh
 * @version 
 */

package com.sun.enterprise.security;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class GUIErrorDialog extends javax.swing.JDialog {
    String message;
    /** Creates new form GUIErrorDialog */
    public GUIErrorDialog (String message){
	super (new JFrame (), true);
	this.message = message;
	initComponents ();
	pack ();
    }
    /** This method is called from within the constructor to
     * initialize the form.
     */
    private void initComponents() {
        okButton = new javax.swing.JButton();
        errorLbl = new javax.swing.JLabel();
	okButton.setAlignmentX (CENTER_ALIGNMENT);
	errorLbl.setAlignmentX (CENTER_ALIGNMENT);
	getContentPane().setLayout (new javax.swing.BoxLayout (getContentPane (),BoxLayout.Y_AXIS));
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        }
        );
        
        okButton.setLabel("OK");
        okButton.setActionCommand("okButton");
        okButton.setText("OK");
        okButton.addActionListener (new ActionListener (){
		public void actionPerformed (ActionEvent e){
		    dispose ();
		}
	    });
	super.addWindowListener (new WindowAdapter (){
		public void windowClosing (WindowEvent we){
		    dispose ();
		}
	    });
        errorLbl.setText("Error : "+message);
	getContentPane().add (errorLbl);
	getContentPane().add (okButton);
    }

    /** Closes the dialog */
    private void closeDialog(java.awt.event.WindowEvent evt) {
        setVisible (false);
        dispose ();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton okButton;
    private javax.swing.JLabel errorLbl;
    // End of variables declaration//GEN-END:variables

}
