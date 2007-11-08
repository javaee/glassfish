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

//
// Copyright 2001 iPlanet/ Sun Microsystems, Inc. All Rights Reserved.
//
// Author : darpan
// Module : Utils
//

package com.sun.enterprise.util.scheduler;

import com.sun.enterprise.util.collection.DListNode;

/**
* Maintains a list of elements in an increasing order. Each element is a 
* TaskData object encapsulating an absolute execute time and the servicable
* task object.
* <BR> <I>$Source: /cvs/glassfish/appserv-commons/src/java/com/sun/enterprise/util/scheduler/TimedTaskList.java,v $</I>
* @author     $Author: tcfujii $
* @version    1.0 $Revision: 1.3 $ $Date: 2005/12/25 04:12:30 $
* @see PeriodicallyServicable
*/
public class TimedTaskList
{
    /** First dummy node. */
    private DListNode first;
    /** Last dummy node. */
    private DListNode last;
    /** Maintains the current size of the list. */    
    private int size = 0;
    
    /**
    * Constructor for TimedTaskList, created a new DList object.
    */
    protected TimedTaskList()
    {
        first = new DListNode(null);
        last = new DListNode(null);
        first.next = last;
        last.prev = first;
        first.prev = last.next = null;
    }

    /**
    * Return the size of the task list.
    * @return 0 if empty, otherwise a number indicating the number of elements.
    */
    protected int size()
    {
        return size;
    }

    /**
    * Remove first task from the ordered list, and return it.
    * @return first task on the queue.
    */
    protected TaskData getFirstTask()
    {
        DListNode node = first;
        node = node.next;
        if(null!=node.next) // not the end
        {
            node.delink();
            --size;
            return (TaskData) node.object;
        }
        return null;
    }

    /**
    * Add a new servicable task into the task list (ordered in increasing order).
    * @param taskObj servicable object to add.
    * @param startingTime wait for how long to start considering this task.
    * @param currentTime current time when executing other tasks.
    * @return boolean true on success, false otherwise.
    */
    protected boolean addTask(PeriodicallyServicable taskObj, int startingTime, long currentTime)
    {
        TaskData task = new TaskData();
        task.obj = taskObj;
        // decrement frequency time as it will be added on insertTask( ) anyway
        task.abs_execute_time = currentTime + startingTime - taskObj.getFrequency();
        return insertTask(task);
    }

    /**
    * Insert task back into the task list (ordered in increasing order).
    * @param task object to execute in an order.
    * @return boolean true on success, false otherwise.
    */
    protected boolean insertTask(TaskData task)
    {
        task.abs_execute_time += ((PeriodicallyServicable)task.obj).getFrequency();
        DListNode addingNode = new DListNode(task);
        DListNode node = first;
        ++size;
        for(int i=0; i<size-1; i++)
        {
            node = node.next;
            long nodeTime = ((TaskData)node.object).abs_execute_time;
            if(nodeTime > task.abs_execute_time)
            {
                node.insertBefore(addingNode);
                return true;
            }
        }
        node.insertAfter(addingNode);
        return true;
      }

    /**
    * Removes the Servicable object from the task list permanently.
    * @param obj servicable object to remove.
    * @return boolean true on success, false otherwise.
    */
    protected boolean removeTask(PeriodicallyServicable obj)
    {
        DListNode node = first;

        for(int i=0; i<size; i++)
        {
            node = node.next;
            PeriodicallyServicable nodeObj = ((TaskData)node.object).obj;

            if(nodeObj.equals(obj))
            {
                node.delink();
                --size;
                return true;
            }
        }
        return false;
    }

    /**
    * Prints information about the list and its contents.
    * @return String 
    */
    public String toString()
    {
        StringBuffer sb = new StringBuffer(100);
        sb.append(" [TimedTaskList: ");
        sb.append( size + " elements: ");
        DListNode node = first;
        while(null!=node.next && null!=node.next.object)
        {
            node = node.next;
            sb.append((int) ( ((TaskData)node.object).abs_execute_time / 1000) );
            sb.append(",");
        }
        sb.append("] ");
        return sb.toString();
    }
}
