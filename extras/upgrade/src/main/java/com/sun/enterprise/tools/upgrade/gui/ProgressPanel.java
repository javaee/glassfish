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
 * ProgressPanel.java
 *
 * Created on September 3, 2003, 3:14 PM
 */

package com.sun.enterprise.tools.upgrade.gui;

/**
 *
 * @author  prakash
 */
import javax.swing.*;
import java.awt.*;
import com.sun.enterprise.tools.upgrade.common.*;
import java.util.logging.*;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.tools.upgrade.logging.*;

public class ProgressPanel extends javax.swing.JPanel {
    
    private FlowLabel flowProgressLabel;
    private JTextArea resultTextArea;
    private ProgressBar progressBar;
    private JScrollPane jscrollpane;
    
    //////private StringManager stringManager = StringManager.getManager("com.sun.enterprise.tools.upgrade.gui");
    private StringManager stringManager = StringManager.getManager(ProgressPanel.class);
    private Logger logger = com.sun.enterprise.tools.upgrade.common.CommonInfoModel.getDefaultLogger();    
    
    /** Creates a new instance of ProgressPanel */
    public ProgressPanel() {
        initialize();
    }
  
    private void initialize(){
        this.setLayout(new BorderLayout());
        HeaderPanel headerPanel = new HeaderPanel(stringManager.getString("upgrade.gui.progresspanel.headerPanel"));
        headerPanel.setInsets(new java.awt.Insets(12, 10, 12, 10));
        add(headerPanel, "North");
        add(getWizardPanel(), "Center");
        
    }  
    private JPanel getWizardPanel(){
        JPanel panel = new JPanel(new GridBagLayout());
        FlowLabel flowTopLabel = new FlowLabel();
        FlowLabel flowTextAreaLabel = new FlowLabel();
        flowProgressLabel = new FlowLabel();
        progressBar = new ProgressBar();
        resultTextArea = new JTextArea(){
            public boolean isFocusTraversable()
            {
                return false;
            }
        };
        resultTextArea.setEditable(false);
        resultTextArea.setLineWrap(true);
        jscrollpane = new JScrollPane(resultTextArea, 20, 30);
        jscrollpane.setAutoscrolls(true);
        resultTextArea.setAutoscrolls(true);
        
        flowTopLabel.setText(stringManager.getString("upgrade.gui.progresspanel.flowContentLabel"));
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 10, 0);
        gridBagConstraints.weightx = 1.0;
        panel.add(flowTopLabel, gridBagConstraints);
        
        flowTextAreaLabel.setText(stringManager.getString("upgrade.gui.progresspanel.textAreaText"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 10);
        gridBagConstraints.weightx = 1.0;
        panel.add(flowTextAreaLabel, gridBagConstraints);
        
        gridBagConstraints = new java.awt.GridBagConstraints();  
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 10, 10);
        gridBagConstraints.weightx = 1.0; gridBagConstraints.weighty = 1.0;
        panel.add(jscrollpane, gridBagConstraints);
        
        flowProgressLabel.setText(stringManager.getString("upgrade.gui.progresspanel.progressLabel"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 10);
        gridBagConstraints.weightx = 1.0;
        panel.add(flowProgressLabel, gridBagConstraints);
        
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 10, 10);
        gridBagConstraints.weightx = 1.0;
        panel.add(progressBar, gridBagConstraints);
        
        return panel;
    }
    public void updateLog(LogMessageEvent evt){
        java.util.logging.LogRecord logRecord = evt.getLogRecord();
        if(logRecord != null){
            if((logRecord.getLevel().equals(Level.SEVERE)) || (logRecord.getLevel().equals(Level.WARNING))){
                //java.awt.Font origFont = this.resultTextArea.getFont();
                //java.awt.Font spFont = new java.awt.Font(this.resultTextArea.getFont().getName(), java.awt.Font.BOLD, this.resultTextArea.getFont().getSize());
                //this.resultTextArea.setFont(spFont);
                this.resultTextArea.append(logRecord.getMessage());
                this.resultTextArea.revalidate();
                //jscrollpane.getVerticalScrollBar().setValue(jscrollpane.getVerticalScrollBar().getMaximum());
                //jscrollpane.getVerticalScrollBar().setVisibleAmount(jscrollpane.getVerticalScrollBar().getMaximum());
                //this.resultTextArea.setFont(origFont);
            }else{
                this.resultTextArea.append(logRecord.getMessage());
            }
            this.resultTextArea.append("\n");            
        }else{
            this.resultTextArea.append(evt.getMessage());
        }
        this.resultTextArea.revalidate();
        jscrollpane.getVerticalScrollBar().setValue(jscrollpane.getVerticalScrollBar().getMaximum());
        jscrollpane.getVerticalScrollBar().setVisibleAmount(jscrollpane.getVerticalScrollBar().getMaximum());
    }
    public void updateProgress(UpgradeUpdateEvent evt){
        int progressState = evt.getProgressState();
        String labelText = null;
        if(evt.getProgressState() == 100){
                labelText = stringManager.getString("upgrade.gui.progresspanel.progressLabel.DONE");
        }
        if(evt.getProgressState() == -1){
            progressState = 0;
            labelText = stringManager.getString("upgrade.gui.progresspanel.progressLabel.ERROR");
            javax.swing.JOptionPane.showMessageDialog(this, stringManager.getString("upgrade.gui.progresspanel.errorProgressMsg"),
						      stringManager.getString("upgrade.gui.progresspanel.errorProgressMsgTitle"),
						      javax.swing.JOptionPane.ERROR_MESSAGE); 
        }
        this.progressBar.setProgress(progressState);         
        if(labelText != null)
            flowProgressLabel.setText(labelText);
    }    
}
