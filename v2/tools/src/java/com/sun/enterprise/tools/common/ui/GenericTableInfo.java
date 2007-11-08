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
 * GenericTableInfo.java
 *
 * Created on February 21, 2001, 1:18 AM
 */

package com.sun.enterprise.tools.common.ui;
import com.sun.enterprise.tools.common.util.diagnostics.Reporter;

/**
 *
 * @author  bnevins
 * @version 
 */
public class GenericTableInfo
{
	public GenericTableInfo(int nc)
	{
		numCols = nc;
		Reporter.assertIt(nc > 0); //NOI18N
		
		columnNames = new String[numCols];
		isEditable  = new boolean[numCols];
		
		for(int i = 0; i < numCols; i++)
		{
			columnNames[i] = "Column " + i;//NOI18N	// just to have a default...
			isEditable[i] = true;	// by default -- all cells are editable...
		}
	}
	
	////////////////////////////////////////////////////////////
	
	public GenericTableInfo(int nr, int nc)
	{
		numCols = nc;
		numRows = nr;
		// no columns -- bad!  no rows -- OK!
		Reporter.assertIt(nc > 0); //NOI18N
		Reporter.assertIt(nr >= 0); //NOI18N
		
		data = new String[numCols][numRows];
		columnNames = new String[numCols];
		isEditable  = new boolean[numCols];
		
		for(int i = 0; i < numCols; i++)
		{
			columnNames[i] = "Column " + i;//NOI18N	// just to have a default...
			isEditable[i] = true;	// by default -- all cells are editable...
		}
	}
	
	/////////////////////////////////////////////////

	public void setColumnName(int col, String name)
	{
		checkColumnNumber(col);
		columnNames[col] = name;
	}
	
	/////////////////////////////////////////////////

	public String getColumnName(int col)
	{
		checkColumnNumber(col);
		return columnNames[col];
	}
	
	/////////////////////////////////////////////////

	public void setString(int row, int col, String name)
	{
		checkColumnNumber(col);
		checkRowNumber(row);
		
		data[col][row] = name;
	}
	
	/////////////////////////////////////////////////

	public String getString(int row, int col)
	{
		checkColumnNumber(col);
		checkRowNumber(row);
		//System.out.println("getString[col=" + col + "][r=" + row + "]: " + data[col][row]);//NOI18N
		return data[col][row];
	}
	
	/////////////////////////////////////////////////

	public int getColumnCount()	
	{
		return numCols;
	}
	
	/////////////////////////////////////////////////

	public int getRowCount()	
	{
		return numRows;
	}
	
	//////////////////////////////////////////////////////////////
	
	public void setColumnReadOnly(int c)
	{ 
		checkColumnNumber(c);
		isEditable[c] = false;
	}
	
	//////////////////////////////////////////////////////////////
	
	public boolean isColumnEditable(int c)
	{ 
	///	System.out.println("isColEditable for Column " + c + " --- " + isEditable[c]);//NOI18N
		checkColumnNumber(c);
		return isEditable[c];
	}
	
	/////////////////////////////////////////////////

	public String toString()
	{
		String s = "";//NOI18N
		
		for(int c = 0; c < numCols; c++)
		{
			s += "Column Name " + c + ":  " + columnNames[c] + "\n";//NOI18N
		}
		
		for(int r = 0; r < numRows; r++)
		{
			for(int c = 0; c < numCols; c++)
			{
				s += "row " + r + ", col " + c + ":  " + data[c][r] + "\n";//NOI18N
			}
		}
		return s;
	}
	
	/////////////////////////////////////////////////

	private void checkColumnNumber(int col)
	{
		if(col < 0 || col >= numCols)
			throw new  IllegalArgumentException("column number must be between 0 and " + (numCols - 1) + " -- attempted to use non-existant column # " + col);//NOI18N
	}
	
	/////////////////////////////////////////////////

	private void checkRowNumber(int row)
	{
		if(row < 0 || row >= numRows)
			throw new  IllegalArgumentException("Row number must be between 0 and " + (numRows - 1) + " -- attempted to use non-existant row # " + row);//NOI18N
	}
	
	/////////////////////////////////////////////////
	
	private int 		numCols		= 0;
	private int 		numRows		= 0;
	private	String[][]	data 		= null;
	private String[]	columnNames	= null;
	private boolean[]	isEditable	= null;
	
	/////////////////////////////////////////////////

	public static void main(String[] args)
	{
		GenericTableInfo gti = new GenericTableInfo(2, 3);
		gti.setColumnName(0, "Col 0 here!");//NOI18N		
		gti.setColumnName(1, "Col 1 here!");//NOI18N
		
		for(int r = 0; r < 2; r++)
		{
			for(int c = 0; c < 3; c++)
			{
				gti.setString(r, c, "c" + c + "r" + r);//NOI18N
			}
		}
		System.out.println("" + gti);//NOI18N
	}
}
