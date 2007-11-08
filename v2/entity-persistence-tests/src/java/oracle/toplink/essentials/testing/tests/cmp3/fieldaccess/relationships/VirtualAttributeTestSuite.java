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


package oracle.toplink.essentials.testing.tests.cmp3.fieldaccess.relationships;

import javax.persistence.EntityManager;

import oracle.toplink.essentials.testing.framework.junit.JUnitTestCase;
import oracle.toplink.essentials.testing.models.cmp3.virtualattribute.*;

import oracle.toplink.essentials.internal.ejb.cmp3.EntityManagerImpl;

import junit.extensions.TestSetup;
import junit.framework.*;

public class VirtualAttributeTestSuite extends JUnitTestCase {

    protected static int id;

    public VirtualAttributeTestSuite() {
        super();
    }
    
    public VirtualAttributeTestSuite(String name) {
        super(name);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTest(new VirtualAttributeTestSuite("testInsertVirtualAttribute")); 
        suite.addTest(new VirtualAttributeTestSuite("testReadVirtualAttribute")); 
        suite.addTest(new VirtualAttributeTestSuite("testUpdateVirtualAttribute")); 
        suite.addTest(new VirtualAttributeTestSuite("testDeleteVirtualAttribute")); 

        return new TestSetup(suite) {
            protected void setUp(){
                new VirtualAttributeTableCreator().replaceTables(JUnitTestCase.getServerSession());
            }

            protected void tearDown() {
                clearCache();
            }
        };
    }
   
    public void setUp () {
        super.setUp();
        clearCache();
    }
    
    public void testInsertVirtualAttribute(){
        EntityManager em = createEntityManager();
        em.getTransaction().begin();
        try{
            OneToOneVirtualAttributeHolder holder = new OneToOneVirtualAttributeHolder();
            VirtualAttribute attribute = new VirtualAttribute();
            attribute.setDescription("virtualAttribute");
            holder.setVirtualAttribute(attribute);
            em.persist(holder);
            em.flush();
            id = holder.getId();
            em.getTransaction().commit();
        }catch (RuntimeException ex){
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            em.close();
            throw ex;
        }
    }
    
    public void testReadVirtualAttribute(){
        OneToOneVirtualAttributeHolder holder = createEntityManager().find(OneToOneVirtualAttributeHolder.class, id);
        assertNotNull("Object with virtual attributes could not be read.", holder);
        assertNotNull("Object held as a virtual attribute was not read with it's owner", holder.getVirtualAttribute());
    }
        
    public void testUpdateVirtualAttribute(){
        OneToOneVirtualAttributeHolder holder = null;
        EntityManager em = createEntityManager();
        em.getTransaction().begin();
        try{
            holder = em.find(OneToOneVirtualAttributeHolder.class, id);
            VirtualAttribute attribute = new VirtualAttribute();
            attribute.setDescription("virtualAttribute2");
            holder.setVirtualAttribute(attribute);
            em.getTransaction().commit();
        }catch (RuntimeException ex){
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            em.close();
            throw ex;
        }
        clearCache();
        holder = em.find(OneToOneVirtualAttributeHolder.class, id);
        em.close();
        assertNotNull("Updated object with virtual attributes could not be read.", holder);
        assertNotNull("Updated object held as a virtual attribute was not read with it's owner", holder.getVirtualAttribute());
        assertTrue("Virtual Attribute Object was not updated.", holder.getVirtualAttribute().getDescription().equals("virtualAttribute2"));
    }
    
    public void testDeleteVirtualAttribute(){
        OneToOneVirtualAttributeHolder holder = null;
        VirtualAttribute attribute = null;
        int attributeId = 0;
        EntityManager em = createEntityManager();
        em.getTransaction().begin();
        try{
            holder = em.find(OneToOneVirtualAttributeHolder.class, id);
    
            attribute = holder.getVirtualAttribute();
            attributeId = attribute.getId();
            holder.setVirtualAttribute(null);
            em.remove(attribute);       
            em.remove(holder);
            em.getTransaction().commit();
        }catch (RuntimeException ex){
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            em.close();
            throw ex;
        }
        holder = em.find(OneToOneVirtualAttributeHolder.class, id);
        assertNull("Object holding virtual attribute was not properly deleted", holder);
        attribute =em.find(VirtualAttribute.class, attributeId);
        assertNull("Virtual Attribute Object was not properly removed.", attribute);
    }
   
    
}
