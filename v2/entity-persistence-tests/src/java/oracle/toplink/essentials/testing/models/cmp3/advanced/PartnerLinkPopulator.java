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
 
package oracle.toplink.essentials.testing.models.cmp3.advanced;

import java.util.Vector;

import oracle.toplink.essentials.sessions.DatabaseSession;
import oracle.toplink.essentials.sessions.Session;
import oracle.toplink.essentials.sessions.UnitOfWork;
import oracle.toplink.essentials.tools.schemaframework.PopulationManager;

/**
 * <p><b>Purpose</b>: To build and populate the database for example and
 * testing purposes. This population routine is fairly complex and makes use
 * of the population manager to resolve interrated objects as the employee
 * objects are an interconnection graph of objects. 
 *
 * This is not the recomended way to create new objects in your application, 
 * this is just the easiest way to create interconnected new example objects
 * from code. Normally in your application the objects will be defined as part
 * of a transactional and user interactive process. 
 */
public class PartnerLinkPopulator {

    protected PopulationManager populationManager;

    public PartnerLinkPopulator() {
        this.populationManager = PopulationManager.getDefaultManager();
    }

    public Man manExample1() {
        if (containsObject(Man.class, "0001")) {
            return (Man)getObject(Man.class, "0001");
        }

        Man man = new Man();
        man.setFirstName("Bob");
        man.setLastName("Smith");
        registerObject(Man.class, man, "0001");

        return man;
    }

    public Man manExample2() {
        if (containsObject(Man.class, "0002")) {
            return (Man)getObject(Man.class, "0002");
        }

        Man man = new Man();
        man.setFirstName("Jim-bob");
        man.setLastName("Jefferson");
        registerObject(Man.class, man, "0002");

        return man;
    }

    public Woman womanExample1() {
        if (containsObject(Woman.class, "0001")) {
            return (Woman)getObject(Woman.class, "0001");
        }

        Woman woman = new Woman();
        woman.setFirstName("Jill");
        woman.setLastName("May");
        registerObject(Woman.class, woman, "0001");

        return woman;
    }

    public PartnerLink partnerLinkExample1() {
        if (containsObject(PartnerLink.class, "0001")) {
            return (PartnerLink)getObject(PartnerLink.class, "0001");
        }

        PartnerLink partnerLink = new PartnerLink();
        partnerLink.setMan(manExample1());
        partnerLink.setWoman(womanExample1());
        registerObject(PartnerLink.class, partnerLink, "0001");

        return partnerLink;
    }

    /**
     * Call all of the example methods in this system to guarantee that all
     * our objects are registered in the population manager.
     */
    public void buildExamples() {
        // First ensure that no preivous examples are hanging around.
        PopulationManager.getDefaultManager().getRegisteredObjects().remove(Man.class);
        PopulationManager.getDefaultManager().getRegisteredObjects().remove(Woman.class);
        PopulationManager.getDefaultManager().getRegisteredObjects().remove(PartnerLink.class);

        manExample1();
        womanExample1();
        partnerLinkExample1();
        manExample2();
    }
    
    public void persistExample(Session session) {        
        Vector allObjects = new Vector();        
        UnitOfWork unitOfWork = session.acquireUnitOfWork();        
        PopulationManager.getDefaultManager().addAllObjectsForClass(Man.class, allObjects);
        PopulationManager.getDefaultManager().addAllObjectsForClass(Woman.class, allObjects);
        PopulationManager.getDefaultManager().addAllObjectsForClass(PartnerLink.class, allObjects);
        unitOfWork.registerAllObjects(allObjects);
        unitOfWork.commit();
    }
    
    protected void registerObject(Class domainClass, Object domainObject, String identifier) {
        populationManager.registerObject(domainClass, domainObject, identifier);
    }

    protected void registerObject(Object domainObject, String identifier) {
        populationManager.registerObject(domainObject, identifier);
    }

    protected boolean containsObject(Class domainClass, String identifier) {
        return populationManager.containsObject(domainClass, identifier);
    }

    protected Vector getAllObjects() {
        return populationManager.getAllObjects();
    }

    public Vector getAllObjectsForClass(Class domainClass) {
        return populationManager.getAllObjectsForClass(domainClass);
    }

    protected Object getObject(Class domainClass, String identifier) {
        return populationManager.getObject(domainClass, identifier);
    }

}
