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
 
package oracle.toplink.essentials.testing.models.cmp3.inheritance;

import java.util.Vector;

import oracle.toplink.essentials.sessions.Session;
import oracle.toplink.essentials.sessions.UnitOfWork;
import oracle.toplink.essentials.tools.schemaframework.PopulationManager;

/**
 * <p><b>Purpose</b>: To build and populate the database for example and testing purposes.
 * This population routine is fairly complex and makes use of the population manager to
 * resolve interrated objects as the employee objects are an interconnection graph of objects.
 *
 * This is not the recomended way to create new objects in your application,
 * this is just the easiest way to create interconnected new example objects from code.
 * Normally in your application the objects will be defined as part of a transactional and user interactive process.
 */
public class InheritancePopulator {
    protected PopulationManager populationManager;

    public InheritancePopulator() {
        this.populationManager = PopulationManager.getDefaultManager();

    }

    /**
     * Call all of the example methods in this system to guarantee that all our objects
     * are registered in the population manager
     */
    public void buildExamples() {
        // First ensure that no preivous examples are hanging around.
        PopulationManager.getDefaultManager().getRegisteredObjects().remove(Person.class);

        PopulationManager.getDefaultManager().registerObject(Person.class, InheritanceModelExamples.personExample1(), "e1");
        PopulationManager.getDefaultManager().registerObject(Person.class, InheritanceModelExamples.personExample2(), "e2");
        PopulationManager.getDefaultManager().registerObject(Person.class, InheritanceModelExamples.personExample3(), "e3");
        PopulationManager.getDefaultManager().registerObject(Person.class, InheritanceModelExamples.personExample4(), "e4");
        PopulationManager.getDefaultManager().registerObject(Person.class, InheritanceModelExamples.personExample5(), "e5");
        PopulationManager.getDefaultManager().registerObject(Person.class, InheritanceModelExamples.personExample6(), "e6");

        PopulationManager.getDefaultManager().registerObject(AAA.class, InheritanceModelExamples.aaaExample1(), "a1");
        PopulationManager.getDefaultManager().registerObject(AAA.class, InheritanceModelExamples.bbbExample1(), "b1");
        PopulationManager.getDefaultManager().registerObject(AAA.class, InheritanceModelExamples.cccExample1(), "c1");
        PopulationManager.getDefaultManager().registerObject(AAA.class, InheritanceModelExamples.cccExample1(), "c2");
        
        PopulationManager.getDefaultManager().registerObject(Company.class, InheritanceModelExamples.companyExample1(), "co1");
        PopulationManager.getDefaultManager().registerObject(Company.class, InheritanceModelExamples.companyExample2(), "co2");
        PopulationManager.getDefaultManager().registerObject(Company.class, InheritanceModelExamples.companyExample3(), "co3");
    }
    
    
    public void persistExample(Session session)
    {        
        Vector allObjects = new Vector();        
        UnitOfWork unitOfWork = session.acquireUnitOfWork();        
        PopulationManager.getDefaultManager().addAllObjectsForClass(Person.class, allObjects);
        PopulationManager.getDefaultManager().addAllObjectsForClass(AAA.class, allObjects);
        PopulationManager.getDefaultManager().addAllObjectsForClass(Company.class, allObjects);
        unitOfWork.registerAllObjects(allObjects);
        unitOfWork.commit();
        
    }
    protected boolean containsObject(Class domainClass, String identifier) {
        return populationManager.containsObject(domainClass, identifier);
    }


}
