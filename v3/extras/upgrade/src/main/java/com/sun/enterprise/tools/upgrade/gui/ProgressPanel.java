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
import com.sun.enterprise.tools.upgrade.common.*;
import com.sun.enterprise.util.i18n.StringManager;
import java.awt.*;
import java.util.logging.Level;
import javax.swing.*;

public class ProgressPanel extends JPanel {
    
    private JLabel progressLabel;
    private JTextArea resultTextArea;
    private ProgressBar progressBar;
    private JScrollPane jscrollpane;
    
    private StringManager stringManager =
        StringManager.getManager(ProgressPanel.class);
    
    /** Creates a new instance of ProgressPanel */
    public ProgressPanel() {
        initialize();
    }
  
    private void initialize(){
        this.setLayout(new BorderLayout());
        HeaderPanel headerPanel = new HeaderPanel(
            stringManager.getString("upgrade.gui.progresspanel.headerPanel"));
        headerPanel.setInsets(new Insets(12, 10, 12, 10));
        add(headerPanel, BorderLayout.NORTH);
        add(getWizardPanel(), BorderLayout.CENTER);
    }
    
    private JPanel getWizardPanel(){
        JPanel panel = new JPanel(new GridBagLayout());
        JLabel topLabel = new JLabel();
        JLabel textAreaLabel = new JLabel();
        progressLabel = new JLabel();
        progressLabel.setForeground(Color.BLUE);
        progressBar = new ProgressBar();
        resultTextArea = new JTextArea();
        resultTextArea.setFocusable(false);
        resultTextArea.setEditable(false);
        resultTextArea.setLineWrap(true);
        jscrollpane = new JScrollPane(resultTextArea, 20, 30);
        jscrollpane.setAutoscrolls(true);
        resultTextArea.setAutoscrolls(true);

        topLabel.setText(stringManager.getString("upgrade.gui.progresspanel.contentLabel"));
        topLabel.setForeground(Color.BLUE);
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new Insets(0, 10, 10, 0);
        gridBagConstraints.weightx = 1.0;
        panel.add(topLabel, gridBagConstraints);
        
        textAreaLabel.setText(stringManager.getString("upgrade.gui.progresspanel.textAreaText"));
        textAreaLabel.setForeground(Color.BLUE);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new Insets(5, 10, 10, 10);
        gridBagConstraints.weightx = 1.0;
        panel.add(textAreaLabel, gridBagConstraints);
        
        gridBagConstraints = new GridBagConstraints();  
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.insets = new Insets(0, 10, 10, 10);
        gridBagConstraints.weightx = 1.0; gridBagConstraints.weighty = 1.0;
        panel.add(jscrollpane, gridBagConstraints);
        
        progressLabel.setText(stringManager.getString("upgrade.gui.progresspanel.progressLabel"));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new Insets(10, 10, 10, 10);
        gridBagConstraints.weightx = 1.0;
        panel.add(progressLabel, gridBagConstraints);
        
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new Insets(0, 10, 10, 10);
        gridBagConstraints.weightx = 1.0;
        panel.add(progressBar, gridBagConstraints);
        
        return panel;
    }
    public void updateLog(LogMessageEvent evt){
        java.util.logging.LogRecord logRecord = evt.getLogRecord();
        if(logRecord != null){
            if((logRecord.getLevel().equals(Level.SEVERE)) || (logRecord.getLevel().equals(Level.WARNING))){
                //Font origFont = this.resultTextArea.getFont();
                //Font spFont = new Font(this.resultTextArea.getFont().getName(), Font.BOLD, this.resultTextArea.getFont().getSize());
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
            JOptionPane.showMessageDialog(this, stringManager.getString(
                "upgrade.gui.progresspanel.errorProgressMsg"),
                stringManager.getString(
                "upgrade.gui.progresspanel.errorProgressMsgTitle"),
                JOptionPane.ERROR_MESSAGE);
        }
        this.progressBar.setProgress(progressState);         
        if(labelText != null)
            progressLabel.setText(labelText);
    }    
}
