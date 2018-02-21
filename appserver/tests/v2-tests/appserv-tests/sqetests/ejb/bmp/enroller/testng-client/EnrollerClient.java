/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2001-2018 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package com.sun.s1peqe.ejb.bmp.enroller.client;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import java.util.*;
import com.sun.s1peqe.ejb.bmp.enroller.ejb.*;

import org.testng.Assert;
import org.testng.annotations.Configuration;
import org.testng.annotations.Test;


public class EnrollerClient {

    public static void main(String[] args) {
        org.testng.TestNG testng = new org.testng.TestNG();
        testng.setTestClasses(
            new Class[] { com.sun.s1peqe.ejb.bmp.enroller.client.EnrollerClient.class } );
        testng.run();
    }

    StudentHome studentHome = null;
    CourseHome courseHome = null;
    EnrollerHome enrollerHome = null;

    @Configuration(beforeTestClass = true)
    public void initialize() throws Exception {
        InitialContext ic = new InitialContext();

        studentHome = (StudentHome)PortableRemoteObject.narrow(
            ic.lookup("java:comp/env/ejb/SimpleStudent"), StudentHome.class);

        System.out.println("Narrowed StudentHome !!");

        courseHome = (CourseHome)PortableRemoteObject.narrow(
            ic.lookup("java:comp/env/ejb/SimpleCourse"), CourseHome.class);
        System.out.println("Narrowed CourseHome !!");

        enrollerHome = (EnrollerHome)PortableRemoteObject.narrow(
            ic.lookup("java:comp/env/ejb/SimpleEnroller"), EnrollerHome.class);

        System.out.println("Narrowed EnrollerHome !!");
    }

    @Test
    public void test1() throws Exception {

        Student denise = studentHome.create("823", "Denise Smith");

        Course power = courseHome.create("220", "Power J2EE Programming");

        Enroller enroller = enrollerHome.create();
        enroller.enroll("823", "220");
        enroller.enroll("823", "333");
        enroller.enroll("823", "777");
        enroller.enroll("456", "777");
        enroller.enroll("388", "777");

        System.out.println(denise.getName() + ":");
        ArrayList courses = denise.getCourseIds();
        Iterator i = courses.iterator();
        while (i.hasNext()) {
            String courseId = (String)i.next();
            Course course = courseHome.findByPrimaryKey(courseId);
            System.out.println(courseId + " " + course.getName());
        }

        System.out.println();

        Course intro = courseHome.findByPrimaryKey("777");
        System.out.println(intro.getName() + ":");
        courses = intro.getStudentIds();
        i = courses.iterator();
        while (i.hasNext()) {
            String studentId = (String)i.next();
            Student student = studentHome.findByPrimaryKey(studentId);
            System.out.println(studentId + " " + student.getName());
        }
    }
}
