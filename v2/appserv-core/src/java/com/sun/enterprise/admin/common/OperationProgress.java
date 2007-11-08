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

package com.sun.enterprise.admin.common;

//JDK imports
import java.io.Serializable;

/**
	A class that denotes the Progress of an operation carried out earlier.
*/

public class OperationProgress implements Serializable
{
	/* javac 1.3 generated serialVersionUID */
	public static final long serialVersionUID	= -5562129808433683519L;
	
	private boolean			mIsFinished			= false;
	private String			mMessage			= null;
	private int				mPercentage			= 0;
	
	public OperationProgress()
	{
		mIsFinished = false;
		mPercentage = 0;
		mMessage	= "STARTED"; //i18n ?
	}
	/** 
		Creates new OperationProgress 
	*/
    public OperationProgress (int percentage, String message)
	{
		if (percentage < 0 || percentage > 100)
		{
			throw new IllegalArgumentException();
		}
		if (percentage == 100)
		{
			mIsFinished = true;
		}
		mPercentage = percentage;
		mMessage	= message;
    }
	
	/**
		Returns the percentage indicating amount of work done.
	 
		@return int x such that 0 <= x <= 100.
	*/
	
	public int getPercentage()
	{
		return ( mPercentage );
	}
	
	/**
		Returns a boolean indicating whether the work is finished.
	*/
	
	public boolean isFinished()
	{
		return ( mIsFinished );
	}
	
	/*
		Returns a String representing the message associated with this
		stage of the entire work.
	 
		@return String indicating phase of work.
	*/
	public String getMessage()
	{
		return ( mMessage );
	}
	
	/**
		Sets the percentage to given value. The value may not be greater than
		100 or less than 0. If the value is 100, the internal state of this
		Object will be set to finished.
		
		@param percentage integer representing the percentage of work done.
	*/
	
	public void setPercentage(int percentage)
	{
		if (percentage < 0 || percentage > 100)
		{
			throw new IllegalArgumentException();
		}
		mPercentage = percentage;
		if (percentage == 100)
		{
			mIsFinished = true;
		}
	}
	
	/**
		Sets the state to finished or unfinished. If the parameter is true,
		the internal state of the Object will be modified to show the percentage
		as 100.
		@param boolean representing whether work is done.
	*/
	public void setIsFinished(boolean finished)
	{
		mIsFinished = finished;
		if (finished)
		{
			setPercentage(100);
		}
	}
}