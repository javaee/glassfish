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
 * Profiler.java
 *
 * Created on September 17, 2001, 12:42 PM
 */

package com.sun.enterprise.tools.common.util.diagnostics;

import java.util.*;
import com.sun.enterprise.tools.common.util.diagnostics.Reporter;
import com.sun.enterprise.tools.common.util.StringUtils;
/**
 *
 * @author  bnevins
 * @version 
 */


public class Profiler 
{
	public Profiler() 
	{
    }

	public void beginItem()
	{
		beginItem("No Description"); // NOI18N
	}
	
	public void beginItem(String desc)
	{
		//if(currItem != null)
			//Reporter.assertIt(currItem.hasEnded());
		
		currItem = new Item(desc);
		items.add(currItem);
	}

	public void endItem()
	{
		Item item = getLastNotEnded();
		
		if(item != null)
			item.end();
	}
	
	public void report()
	{
		
	}
	
	public String toString()
	{
		StringBuffer sb = new StringBuffer(Item.getHeader());
		sb.append("\n\n"); // NOI18N
		
		for(Iterator iter = items.iterator(); iter.hasNext(); )
		{
			Item item = (Item)iter.next();
			sb.append(item.toString());
			sb.append("\n"); // NOI18N
		}
		return sb.toString();
	}
	
	private Item getLastNotEnded()
	{
		int index = items.size();
		
		while(--index >= 0)
		{
			Item item = (Item)items.get(index);
			
			if(!item.hasEnded())
				return item;
		}
		return null;
	}
	
	private static class Item
	{
		Item(String desc)
		{
			title		= desc;
			startTime	= System.currentTimeMillis();
			endTime		= startTime;
			
			if(title.length() > longestTitle)
				longestTitle = title.length();
		}
		
		boolean hasEnded()
		{
			return endTime > startTime;
		}
		
		void end()
		{
			endTime	= System.currentTimeMillis();
		}
		
		public String toString()
		{
			long finish = hasEnded() ? endTime : System.currentTimeMillis();

			String desc = StringUtils.padRight(title, longestTitle + 1);
			String time = StringUtils.padLeft("" + (finish - startTime), 8); // NOI18N

			if(!hasEnded())
				time += "  ** STILL RUNNING **"; // NOI18N
			
			return desc + time;
		}
		
		public static String getHeader()
		{
			return StringUtils.padRight("Description", longestTitle + 1) + StringUtils.padLeft("msec", 8); // NOI18N
		}
		
		String		title;
		long		startTime;
		long		endTime;
		static int	longestTitle = 12;
	}
	
	Item	currItem	= null;;
	List	items		= new ArrayList();

	public static void main(String[] notUsed)
	{
		Profiler p = new Profiler();
		
		try
		{
			p.beginItem("first item"); // NOI18N
			Thread.sleep(3000);
			p.beginItem("second item here dude whoa yowser yowser"); // NOI18N
			Thread.sleep(1500);
			p.endItem();
			p.endItem();
			System.out.println("" + p); // NOI18N
		}
		catch(Exception e)
		{
		}
	}


}
