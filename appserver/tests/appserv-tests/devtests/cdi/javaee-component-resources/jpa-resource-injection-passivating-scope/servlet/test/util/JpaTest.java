/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2017 Oracle and/or its affiliates. All rights reserved.
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

package test.util;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.transaction.UserTransaction;

import test.entity.Department;
import test.entity.Employee;

public class JpaTest {

    private EntityManager em;
    private UserTransaction utx;

    private static Department deptRef[] = new Department[2];
    private static Employee empRef[] = new Employee[5];

    public JpaTest() {
    }

    public JpaTest(EntityManager em, UserTransaction utx) {
        this.em = em;
        this.utx = utx;
    }

    public boolean lazyLoadingInit() {
        boolean status = false;
        System.out.println("-----lazeLoadingInit()---------");
        try {
            deptRef[0] = new Department(1, "Engineering");
            deptRef[1] = new Department(2, "Marketing");
            utx.begin();
            em.joinTransaction();
            for (int i = 0; i < 2; i++) {
                em.persist(deptRef[i]);
            }
            utx.commit();

            empRef[0] = new Employee(1, "Alan", "Frechette", deptRef[0]);
            empRef[1] = new Employee(2, "Arthur", "Wesley", deptRef[0]);
            empRef[2] = new Employee(3, "Abe", "White", deptRef[0]);
            empRef[3] = new Employee(4, "Paul", "Hinz", deptRef[1]);
            empRef[4] = new Employee(5, "Carla", "Calrson", deptRef[1]);
            utx.begin();
            em.joinTransaction();
            for (int i = 0; i < 5; i++) {
                em.persist(empRef[i]);
            }
            utx.commit();
            status = true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        System.out.println("-----status = " + status + "---------");
        return status;
    }

    public boolean lazyLoadingByFind(int employeeID) {

        boolean status = true;
        System.out.println("------------lazyLoadingAfterFind -----------");
        System.out.println("employeeID = " + employeeID);
        Employee emp = em.find(Employee.class, employeeID);

        System.out.println("found: emp.id=" + emp.getId());

        try {
            // 1. get Department before loading
            Department deptBL = emp.getDepartmentNoWeaving();
            System.out.println("1. before loading: deptBL=" + deptBL);
            String deptNameBL = null;
            if (deptBL != null) {
                deptNameBL = deptBL.getName();
                System.out.println("deptNameBL=" + deptNameBL);
            }
            // assert deptBL == null;
            if (deptBL != null) {
                status = false;
            }

            // 2. loading
            String deptName = emp.getDepartment().getName();
            System.out.println("2. loading, deptName = " + deptName);

            // 3. get Department after loading
            Department deptAL = emp.getDepartmentNoWeaving();
            System.out.println("3. after loading: deptAL=" + deptAL);
            String deptNameAL = deptAL.getName();
            System.out.println("deptNameAL=" + deptNameAL);
            // assert deptAL != null
            // assert deptAL.getName == deptName;
            if (deptAL == null || deptNameAL != deptName) {
                status = false;
            }
        } catch (Exception ex) {
            status = false;
            ex.printStackTrace();
        }

        System.out.println("-----status = " + status + "---------");
        return status;
    }

    public boolean lazyLoadingByQuery(String fName) {

        boolean status = true;
        System.out.println("------------lazyLoadingByQuery -----------");
        System.out.println("fName = " + fName);
        Query query = em.createQuery(
                "SELECT e FROM Employee e WHERE e.firstName like :firstName")
                .setParameter("firstName", fName);
        ;
        Employee emp = (Employee) query.getSingleResult();

        System.out.println("queried: emp.firstName=" + emp.getFirstName());

        try {
            // 1. get Department before loading
            Department deptBL = emp.getDepartmentNoWeaving();
            System.out.println("1. before loading: deptBL=" + deptBL);
            String deptNameBL = null;
            if (deptBL != null) {
                deptNameBL = deptBL.getName();
                System.out.println("deptNameBL=" + deptNameBL);
            }
            // assert deptBL == null;
            if (deptBL != null) {
                status = false;
            }

            // 2. loading
            String deptName = emp.getDepartment().getName();
            System.out.println("2. loading, deptName = " + deptName);

            // 3. get Department after loading
            Department deptAL = emp.getDepartmentNoWeaving();
            System.out.println("3. after loading: deptAL=" + deptAL);
            String deptNameAL = deptAL.getName();
            System.out.println("deptNameAL=" + deptNameAL);
            // assert deptAL != null
            // assert deptAL.getName == deptName;
            if (deptAL == null || deptNameAL != deptName) {
                status = false;
            }
        } catch (Exception ex) {
            status = false;
            ex.printStackTrace();
        }
        System.out.println("-----status = " + status + "---------");
        return status;
    }

}
