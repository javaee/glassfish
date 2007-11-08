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

package oracle.toplink.essentials.testing.tests.cmp3.lob;

import java.lang.Integer;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;

import junit.framework.*;
import junit.extensions.TestSetup;
import oracle.toplink.essentials.internal.ejb.cmp3.EJBQueryImpl;
import oracle.toplink.essentials.sessions.DatabaseSession;
import oracle.toplink.essentials.testing.models.cmp3.lob.Image;
import oracle.toplink.essentials.testing.models.cmp3.lob.ImageSimulator;
import oracle.toplink.essentials.testing.models.cmp3.lob.LobTableCreator;
import oracle.toplink.essentials.testing.framework.junit.JUnitTestCase;
 
/**
 * JUnit test case(s) for the TopLink EntityMappingsXMLProcessor.
 */
public class LobJUnitTestCase extends JUnitTestCase {

    private static Integer imageId;
    private static Image originalImage;
    
    public LobJUnitTestCase() {
        super();
    }
    
    public LobJUnitTestCase(String name) {
        super(name);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite("Lob Model");
        suite.addTest(new LobJUnitTestCase("testCreate"));
        suite.addTest(new LobJUnitTestCase("testRead"));
        suite.addTest(new LobJUnitTestCase("testUpdate"));
        suite.addTest(new LobJUnitTestCase("testDelete"));
        
        return new TestSetup(suite) {
            
            protected void setUp(){               
                DatabaseSession session = JUnitTestCase.getServerSession();   
                new LobTableCreator().replaceTables(session);
            }
        
            protected void tearDown() {
                clearCache();
            }
        };
    }
    
    public void testCreate() {
      EntityManager em = createEntityManager();
      em.getTransaction().begin();
      try {
        Image image = ImageSimulator.generateImage(1000, 800);
        originalImage = image;
        em.persist(image);
        imageId = image.getId();
        em.getTransaction().commit();    
      } catch (RuntimeException e) {
        if (em.getTransaction().isActive()){
          em.getTransaction().rollback();
        }
        em.close();
        throw e;
      }
    }
    
    public void testDelete() {
      EntityManager em = createEntityManager();
      em.getTransaction().begin();
      try {
        em.remove(em.find(Image.class, imageId));
        em.getTransaction().commit();
      } catch (RuntimeException e) {
        if (em.getTransaction().isActive()){
          em.getTransaction().rollback();
        }
        em.close();
        throw e;
      }
      assertTrue("Error deleting Image",em.find(Image.class, imageId)==null);
    }

    public void testRead() {
      EntityManager em = createEntityManager();
      Image image = em.find(Image.class, imageId);
      assertTrue(image.getId()==imageId);
      assertTrue(image.getAudio()==originalImage.getAudio());
      assertTrue(image.getCommentary()==originalImage.getCommentary());
      assertTrue(image.getPicture()==originalImage.getPicture());
      assertTrue(image.getScript()==originalImage.getScript());
    }

    public void testUpdate() {
      EntityManager em = createEntityManager();
      em.getTransaction().begin();
      try {
        Image image = em.find(Image.class, imageId);
        image.setAudio(null);
        image.setCommentary(null);
        image.setPicture(null);
        image.setScript(null);
        em.merge(image);
        em.getTransaction().commit();    
      } catch (RuntimeException e) {
        if (em.getTransaction().isActive()){
          em.getTransaction().rollback();
        }
        em.close();
        throw e;
      }
      Image image = em.find(Image.class, imageId);
      assertNull(image.getAudio());
      assertNull(image.getCommentary());
      assertNull(image.getPicture());
      assertNull(image.getScript());
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(LobJUnitTestCase.suite());
    }
}
