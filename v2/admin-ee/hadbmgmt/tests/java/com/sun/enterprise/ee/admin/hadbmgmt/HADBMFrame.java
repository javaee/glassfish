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
 * HADBMFrame.java
 *
 * Created on May 19, 2004, 2:27 PM
 */

package com.sun.enterprise.ee.admin.hadbmgmt;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import javax.swing.event.*;

/**
 *
 * @author  bnevins
 */
public class HADBMFrame extends JFrame implements DocumentListener, ActionListener
{
	public HADBMFrame(String[] args)
	{
		this.args = args;
		persist = new SimplePersistence(this, false);
		setTitle("Phony HADBM");
		setSize(600, 400);
		//System.out.println("LayoutManager: " + getLayout().getClass().getName());
		
		addWindowListener(new WindowAdapter()
		{
			public void windowClosing(WindowEvent e)
			{
				System.exit(0);
			}
		});
		
		createWidgets();
		Container pane = getContentPane();
		pane.add(savePanel, "North");
		pane.add(textPanel, "Center");
		pane.add(returnPanel, "South");
		setText();
		loadProps();
	}
	
	public void actionPerformed(java.awt.event.ActionEvent actionEvent)
	{
		Object source = actionEvent.getSource();
		
		if(source == succeed)
		{
			storeProps();
			System.exit(0);
		}
		if(source == fail)
		{
			storeProps();
			System.exit(1);
		}
		if(source == save)
		{
			doSave();
		}
	}

	public void changedUpdate(javax.swing.event.DocumentEvent documentEvent)
	{
	}	
	
	public void insertUpdate(javax.swing.event.DocumentEvent documentEvent)
	{
	}
	
	public void removeUpdate(javax.swing.event.DocumentEvent documentEvent)
	{
	}

	private void createWidgets()
	{
		succeed		= new JButton("Return Success");
		fail		= new JButton("Return Failure");
		save		= new JButton("Save to File");
		
		succeed.addActionListener(this);
		fail.addActionListener(this);
		save.addActionListener(this);

		fileField	= new JTextField(40);
		returnPanel = new JPanel();
		savePanel	= new JPanel();
		commandLineArea	= new JTextArea(30, 50);
		textPanel	= new JScrollPane(commandLineArea);
		returnPanel.add(succeed);
		returnPanel.add(fail);
		savePanel.add(fileField);
		savePanel.add(save);
	}
	private void doSave()
	{
		String filename = fileField.getText();
		
		if(filename == null || filename.length() <= 0)
		{
			JOptionPane.showMessageDialog(this, "Filename is empty!", "Error", JOptionPane.INFORMATION_MESSAGE); 	
			return;
		}

		//System.out.println("FILENAME: " + filename);
		try
		{
			FileWriter w = new FileWriter(filename, true);
			w.write(commandLineArea.getText());
			w.write("--------------------------------" + SEP);
			w.close();
		}
		catch(IOException ioe)
		{
			String msg = "Got an IOException trying to save to " + filename + " --\n" + ioe;
			JOptionPane.showMessageDialog(this, msg, "IOException", JOptionPane.INFORMATION_MESSAGE); 	
		}
		
	}
	
	private void setText()
	{
		for(int i = 0; i < args.length; i++)
		{
			commandLineArea.append(args[i]);
			commandLineArea.append(SEP);
		}
	}
	
	
	///////////////////////////////////////////////////////////////////////////
	
	private void loadProps()
	{
		getProp("filename",		fileField);
	}
	
	///////////////////////////////////////////////////////////////////////////
	
	private void storeProps()
	{
		persist.clear();
		setProp("filename",			fileField.getText());
		persist.store();
	}
	
	///////////////////////////////////////////////////////////////////////////
	
	private void setProp(String key, String value)
	{
		if(ok(key) && ok(value))
			persist.setProperty(key, value);
	}
	
	///////////////////////////////////////////////////////////////////////////
	
	private void getProp(String key, JTextField field)
	{
		if(ok(key))
		{
			String value = persist.getProperty(key);
			
			if(ok(value))
				field.setText(value);
		}
	}
	
	private boolean ok(String s)
	{
		return s != null && s.length() > 0;
	}
	
	JButton succeed, fail, save;
	JPanel returnPanel, savePanel;
	JScrollPane textPanel;
	JTextField	fileField;
	JTextArea	commandLineArea;
	String[]	args;
	private	SimplePersistence persist;
	static final String	SEP = System.getProperty("line.separator");
	
}