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


package com.sun.persistence.runtime.query.impl;

import antlr.RecognitionException;

import com.sun.org.apache.jdo.pm.MockPersistenceManagerInternal;
import com.sun.persistence.runtime.model.CompanyMappingModel;
import com.sun.persistence.runtime.model.mapping.RuntimeMappingModel;
import com.sun.persistence.runtime.query.EJBQLAST;
import com.sun.persistence.runtime.query.QueryContext;
import com.sun.persistence.runtime.query.QueryInternal;

import junit.framework.Assert;
import junit.framework.TestCase;

public class NavigationIdTest extends TestCase {
    
    private static final RuntimeMappingModel companyMappingModel
        = CompanyMappingModel.getInstance();
    
    protected void setUp() throws Exception {
        super.setUp();
        
        // access Department JDOClass which causes package.jdo to be read
        String DEPT_CLASSNAME = 
            CompanyMappingModel.COMPANY_PACKAGE + "Department";
        companyMappingModel.getJDOModel().getJDOClass(DEPT_CLASSNAME);
    }
    
    public void testNavigationId() {
        String qstr = "select object(c) from Company c, Employee emp, in(c.departments) d "
            + "where emp.department.company = ?1 and emp.firstname = ?2 and emp.department = d";
        
        QueryInternal query = (QueryInternal) QueryFactory.getInstance()
            .createQuery(qstr, new MockPersistenceManagerInternal());

        NavigationIdCompilationMonitor cm = new NavigationIdCompilationMonitor();

        QueryContext qc = new PersistenceQueryContext(
            CompanyMappingModel.getInstance());

        try {
            EJBQLC.getInstance().compile(query, qc, cm);
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            fail("Caught unexpected exception");
        }
    }

    class NavigationIdCompilationMonitor extends CompilationMonitorImpl {        
        public void postSemantic(String qstr, EJBQLAST ast) {
            NavigationIdVisitor v = new NavigationIdVisitor();

            EJBQLTreeWalker tw = new EJBQLTreeWalker();
            tw.init(v);
            tw.setASTFactory(EJBQLASTFactory.getInstance());
            try {
                tw.query((EJBQLASTImpl) ast);
            } catch (RecognitionException e) {
                System.err.println(
                    "Unexpected exception caught in NaviationIdCompilationMonitor.testSemantics:");
                e.printStackTrace(System.err);
            }
        }
    }
    
    class NavigationIdVisitor extends EJBQLVisitorImpl {
        private int count = 0;

        @Override
        public Object leaveCollectionMemberDecl(
                EJBQLAST node, Object path, Object var) {
            EJBQLAST pnode = (EJBQLAST) path;
            if (pnode != null) {
                Assert.assertEquals("c.departments", pnode.getNavigationId());
            }
            EJBQLAST vnode = (EJBQLAST) var;
            if (vnode != null) {
                Assert.assertEquals(vnode.getText(), vnode.getNavigationId());
            }
            return node;           
        }

        @Override
        public Object leaveRangeVarDecl(
                EJBQLAST node, Object schema, Object var) {
            EJBQLAST vnode = (EJBQLAST) var;
            if (vnode != null) {
                Assert.assertEquals(vnode.getText(), vnode.getNavigationId());
            }
            return node;
        }

        @Override
        public Object leaveProjection(
                Object path) {
            EJBQLAST pnode = (EJBQLAST) path;
            if (pnode != null) {
                Assert.assertEquals(pnode.getText(), pnode.getNavigationId());
            }
            return null;
        }

        @Override
        public Object leaveAggregateSelectExprCount(
                EJBQLAST node, Object distinct, Object path) {
            EJBQLAST pnode = (EJBQLAST) path;
            if (pnode != null) {
                Assert.assertEquals(pnode.getText(), pnode.getNavigationId());
            }
            return node;
        }

        @Override
        public Object leavePathExprCMPField(
                EJBQLAST node, Object path, Object field) {
            Assert.assertEquals("emp.firstname", node.getNavigationId());
            return node;
        }
        
        @Override
        public Object leavePathExprCMRField(
                EJBQLAST node, Object path, Object field) {
            switch (count++) {
                case 0:
                    Assert.assertEquals("emp.department", node.getNavigationId());
                    break;
                case 1:
                    Assert.assertEquals("emp.department.company", node.getNavigationId());
                    break;
            }
            return node;
        }    

        @Override
        public Object leavePathExprCollectionCMRField(
                EJBQLAST node, Object path, Object field) {
            Assert.assertEquals("c.departments", node.getNavigationId());
            return node;
        }
        
        @Override
        public Object leavePathExprIdentificationVar(
                EJBQLAST node) {
            Assert.assertEquals(node.getText(), node.getNavigationId());
            return node;
        }

        @Override
        public Object leaveIdentificationVar(
                EJBQLAST node) {
            Assert.assertEquals(node.getText(), node.getNavigationId());
            return node;
        }
    }
}      

