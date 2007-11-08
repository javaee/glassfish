/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the "License").  You may not use this file except 
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt or 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html. 
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * HEADER in each file and include the License file at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt.  If applicable, 
 * add the following below this CDDL HEADER, with the 
 * fields enclosed by brackets "[]" replaced with your 
 * own identifying information: Portions Copyright [yyyy] 
 * [name of copyright owner]
 */
// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.essentials.testing.models.cmp3.datetime;

import java.sql.Time;

import java.sql.Timestamp;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Vector;

import oracle.toplink.essentials.sessions.Session;
import oracle.toplink.essentials.sessions.UnitOfWork;
import oracle.toplink.essentials.testing.models.cmp3.advanced.Address;
import oracle.toplink.essentials.tools.schemaframework.PopulationManager;

public class DateTimePopulator {
    public DateTimePopulator() {
    }

    public void persistExample(Session session)
    {        
        Vector allObjects = new Vector();   
        allObjects.add(example1());
        allObjects.add(example2());
        allObjects.add(example3());
        allObjects.add(example4());
        
        UnitOfWork unitOfWork = session.acquireUnitOfWork();        
        unitOfWork.registerAllObjects(allObjects);
        unitOfWork.commit();        
    }
    
    public DateTime example1() {
        GregorianCalendar cal = new GregorianCalendar(); 
        cal.set(2001, 6, 1, 3, 45, 32);
        cal.set(Calendar.MILLISECOND, 87);
        
        return buildAttributes(cal);
    }

    public DateTime example2() {
        GregorianCalendar cal = new GregorianCalendar(); 
        cal.set(1901, 11, 31, 23, 59, 59);
        cal.set(Calendar.MILLISECOND, 999);
        
        return buildAttributes(cal);
    }

    public DateTime example3() {
        Calendar cal = Calendar.getInstance(); 
        
        return buildAttributes(cal);
    }

    public DateTime example4() {
        GregorianCalendar cal = new GregorianCalendar(); 
        cal.set(1999, 0, 1, 23, 45, 32);
        cal.set(Calendar.MILLISECOND, 234);
        
        return buildAttributes(cal);
    }

    public DateTime buildAttributes(Calendar cal) {
        DateTime dateTime = new DateTime();
        long time = cal.getTime().getTime();;
        
        dateTime.setDate(new java.sql.Date(time));        
        dateTime.setTime(new Time(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND)));        
        dateTime.setTimestamp(new Timestamp(time));
        dateTime.setUtilDate(new Date(time));
        dateTime.setCalendar(cal);
        
        return dateTime;        
    }
}
