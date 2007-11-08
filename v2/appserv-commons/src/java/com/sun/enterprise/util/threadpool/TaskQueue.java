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

//NOTE: Tabs are used instead of spaces for indentation. 
//  Make sure that your editor does not replace tabs with spaces. 
//  Set the tab length using your favourite editor to your 
//  visual preference.

/*
 * $RCSfile: TaskQueue.java,v $    
 *
 * Copyright 2000-2001 by iPlanet/Sun Microsystems, Inc.,
 * 901 San Antonio Road, Palo Alto, California, 94303, U.S.A.
 * All rights reserved.
 * 
 * This software is the confidential and proprietary information
 * of iPlanet/Sun Microsystems, Inc. ("Confidential Information").
 * You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license 
 * agreement you entered into with iPlanet/Sun Microsystems.
 */

/** 
 * <BR> <I>$Source: /cvs/glassfish/appserv-commons/src/java/com/sun/enterprise/util/threadpool/TaskQueue.java,v $</I> 
 * @author     $Author: tcfujii $ 
 * @version    $Revision: 1.3 $ $Date: 2005/12/25 04:12:33 $ 
 */
 
package com.sun.enterprise.util.threadpool;

import com.sun.enterprise.util.collection.BlockingQueue;
import com.sun.enterprise.util.collection.TooManyTasksException;
import com.sun.enterprise.util.collection.QueueClosedException;
import java.util.ArrayList;

/**
 * TaskQueue provides the user the ability to queue his objects without
 * worring about the usage of the TaskQueue by the ThreadPool. The 
 * responsibility of the Task creation is now delegated to the TaskFactory.
 * TaskFactory will create or pool the task objects that wrap the users
 * object. The tasks need to implement the Serviceable interface
 */
public class TaskQueue extends BlockingQueue
{
	
	private TaskFactory taskFactory;
	
	/**
	 * Create a TaskQueue that has an infinite queuelength with the
	 *	specified timeout.
	 * @param The maximum time remove() will block.
	 * @see remove()
	 */
	public TaskQueue(TaskFactory factory) {
		super();
		taskFactory = factory;
	}
	
	/**
	 * Create a TaskQueue that has the specified queue limit with the
	 *	specified timeout.
	 * @param The maximum time remove() will block.
	 * @param The queue length after which TooManyTasksException is thrown.
	 * @see remove()
	 */
	public TaskQueue(int queueLimit, TaskFactory factory) {
		super(queueLimit);
		taskFactory = factory;
	}
	
	/** 
	 * Add to the head of the queue. Probably a high priority job?
	 */
	public void addFirst(Object o)
		throws TooManyTasksException, QueueClosedException
	{
		super.addFirst(taskFactory.createTask(o));
	}

	/** 
	 * Add to the head of the queue. Probably a high priority job?
	 */
	public void addFirst(Servicable ser)
		throws TooManyTasksException, QueueClosedException
	{
		super.addFirst(ser);
	}
	
	/** 
	 * Add to the tail of the queue. 
	 */
	public void addLast(Object o)
		throws TooManyTasksException, QueueClosedException
	{
		super.addLast(taskFactory.createTask(o));
	}
	
	/** 
	 * Add to the tail of the queue. 
	 */
	public void addLast(Servicable ser)
		throws TooManyTasksException, QueueClosedException
	{
		super.addLast(ser);
	}
	
	/** 
	 * Add the job at the specified position. Probably based on priority?
	 */
	public void add(int index, Object object)
		throws TooManyTasksException, QueueClosedException
	{
		super.add(index, taskFactory.createTask(object));
	}
	
	public void addAll(ArrayList arrayList)
		throws TooManyTasksException, QueueClosedException
	{
		for (int i=0; i < arrayList.size(); i++) {
			Object o = taskFactory.createTask(arrayList.get(i));
			arrayList.set(i, o);
		}
		super.addAll(arrayList);
	}
	
    /**
     * Hook to enable factory to deleteTask. The deleteTask
     * can potentially return the object to some pool
     */
	public void destroyTask(Object o)
	{
		taskFactory.deleteTask(o);
	}
}
