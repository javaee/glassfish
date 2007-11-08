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
 * JWhichUI.java
 *
 * Created on December 2, 2000, 3:09 PM
 */

package com.sun.enterprise.util.diagnostics;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;

/**
 *
 * @author  administrator
 * @version 
 */

class JWhichUI extends JFrame implements ActionListener
{  
	public JWhichUI()
	{  
		addButtonPanel();
		addTextPanel();

		setTitle(title);
		setSize(900, 300);

		addWindowListener(new WindowAdapter()
		{  
			public void windowClosing(WindowEvent e)
			{  
				System.exit(0);
			}
		} );

		show();
	}

	////////////////////////////////////////////////////////
	
	public void pr(String s)
	{
		//textArea.setText(textArea.getText() + s + "\n");//NOI18N
		textArea.append(s + "\n");//NOI18N
	}

	//////////////////////////////////////////////////////////////
	
	private void addButtonPanel()
	{
		JPanel panel = new JPanel();
		searchButton = new JButton("Search");//NOI18N
		panel.add(searchButton);
		searchButton.addActionListener(this);
		Dimension d = searchButton.getPreferredSize();
		d.setSize(450, d.getHeight());
		searchString = new JTextField();
		searchString.setPreferredSize(d);
		panel.add(searchString);
		searchString.addActionListener(this);
		
		getContentPane().add(panel, "South");//NOI18N
	}
	
	//////////////////////////////////////////////////////////////
	
	private void addTextPanel()
	{
		textArea	= new JTextArea(800, 50);
		scrollPane	= new JScrollPane(textArea);
		getContentPane().add(scrollPane, "Center");//NOI18N
	}
	
	//////////////////////////////////////////////////////////////
	
	public void actionPerformed(ActionEvent evt)
	{  
		Object source = evt.getSource();
	
		if(source == searchButton)
		{
			String what = searchString.getText();
			
			if(what == null || what.length() <= 0)
				return;
			
			JWhich jw = new JWhich(what);
			pr(jw.getResult());
		}
	}

	//////////////////////////////////////////////////////////////

	public static void main(String[] args)
	{
		JWhichUI jwui = new JWhichUI();
		jwui.show();
	}
	
	//////////////////////////////////////////////////////////////

	private					JButton		searchButton;   
	private					JTextField	searchString;
	private					JTextArea	textArea;
	private					JScrollPane	scrollPane;
	private final static	String		title		= "JWhich -- Class Finder";//NOI18N
}

