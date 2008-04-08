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

/**
 * @version 1.01 June 6, 2000
 * @author Byron Nevins
 */
package com.sun.enterprise.util.diagnostics;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;
//Bug 4677074 begin
import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.LogDomains;
//Bug 4677074 end

class ReporterFrame extends JFrame implements ActionListener
{  
//Bug 4677074 begin
    static Logger _logger=LogDomains.getLogger(LogDomains.UTIL_LOGGER);
//Bug 4677074 end
	public void pr(String s)
	{
		//textArea.setText(textArea.getText() + s + "\n");//NOI18N
		textArea.append(s + "\n");//NOI18N
	}

	//////////////////////////////////////////////////////////////
	
	ReporterFrame(String title)
	{  
		addButtonPanel();
		addTextPanel();

		setTitle(titleRoot + " -- " + title);//NOI18N
		setSize(900, 300);
		if(standAlone)
		{
			addWindowListener(new WindowAdapter()
			{  
				public void windowClosing(WindowEvent e)
				{  
					System.exit(0);
				}
			} );
			
			for(int i = 0; i < 1000; i++)
				textArea.append("This is line# " + i + "\n");//NOI18N
			
			show();
		}
	}
	////////////////////////////////////////////////////////
	
	private void addButtonPanel()
	{
		JPanel panel = new JPanel();
		wrapButton = new JButton("Wrap");//NOI18N
		panel.add(wrapButton);
		wrapButton.addActionListener(this);

		noWrapButton = new JButton("No wrap");//NOI18N
		panel.add(noWrapButton);
		noWrapButton.addActionListener(this);

		getContentPane().add(panel, "South");//NOI18N
	}
	
	//////////////////////////////////////////////////////////////
	
	private void addTextPanel()
	{
		//textArea	= new JTextPane();
		textArea	= new JTextArea(800, 200);
		scrollPane	= new JScrollPane(textArea);
		getContentPane().add(scrollPane, "Center");//NOI18N
	}
	
	//////////////////////////////////////////////////////////////
	
	public void actionPerformed(ActionEvent evt)
	{  
		Object source = evt.getSource();
	
		if(source == wrapButton)
		{  
			textArea.setLineWrap(true);
			scrollPane.validate();
		}
		else if(source == noWrapButton)
		{  
			textArea.setLineWrap(false);
			scrollPane.validate();
		}

		
		
		/* trying to make it auto-scroll!!!
		//JViewport vp = textArea.getViewport();
		JScrollBar vert = scrollPane.getVerticalScrollBar();

//Bug 4677074		System.out.println("*** vertSB.getMaximum(): " + vert.getMaximum());
//Bug 4677074		System.out.println("*** vertSB.getValue(): " + vert.getValue());
//Bug 4677074		System.out.println("*** getRows(): " + textArea.getRows());
		//System.out.println("*** getRowHeight(): " + textArea.getRowHeight());
//Bug 4677074		System.out.println("*** getLineCount(): " + textArea.getLineCount());
//Bug 4677074 begin
		_logger.log(Level.FINE,"*** vertSB.getMaximum(): " + vert.getMaximum());
		_logger.log(Level.FINE,"*** vertSB.getValue(): " + vert.getValue());
		_logger.log(Level.FINE,"*** getRows(): " + textArea.getRows());
		// _logger.log(Level.FINE,"*** getRowHeight(): " + textArea.getRowHeight());
		_logger.log(Level.FINE,"*** getLineCount(): " + textArea.getLineCount());
//Bug 4677074 end

		final int numLines = textArea.getLineCount();
		int endOffset;
		try
		{
			endOffset= textArea.getLineEndOffset(numLines - 1);
//Bug 4677074			System.out.println("***** numLines:  " + numLines + "  endOffset: " + endOffset);
//Bug 4677074 begin
			_logger.log(Level.FINE,"***** numLines:  " + numLines + "  endOffset: " + endOffset);
//Bug 4677074 end
		}
		catch(BadLocationException e)
		{
//Bug 4677074			System.out.println("***** Exception: " + e);
//Bug 4677074 begin
			_logger.log(Level.WARNING,"iplanet_util.badlocation_exception",e);
//Bug 4677074 end
		}
		*/
	}
	
	//////////////////////////////////////////////////////////////

	
	static void setStandAlone()
	{
//Bug 4677074		System.err.println("setStandAlone() here!!!");//NOI18N
//Bug 4677074 begin
		_logger.log(Level.FINE,"setStandAlone() here!!!");
//Bug 4677074 end
		standAlone = true;
	}
	
	//////////////////////////////////////////////////////////////
	
	private					JButton		wrapButton;   
	private					JButton		noWrapButton;
	//private					JTextPane	textArea;
	private					JTextArea	textArea;
	private					JScrollPane	scrollPane;
	private final static	String		titleRoot		= "iPlanet Reporter";//NOI18N
	private static			boolean		standAlone		= false;
}

