/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * // Copyright (c) 1998, 2007, Oracle. All rights reserved.
 * 
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

package oracle.toplink.essentials.internal.weaving;

import java.io.IOException;
import java.io.Writer;
import java.lang.instrument.IllegalClassFormatException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipException;

import javax.persistence.spi.ClassTransformer;
import javax.persistence.spi.PersistenceUnitInfo;

import oracle.toplink.essentials.ejb.cmp3.persistence.Archive;
import oracle.toplink.essentials.ejb.cmp3.persistence.ArchiveFactoryImpl;
import oracle.toplink.essentials.ejb.cmp3.persistence.PersistenceUnitProcessor;
import oracle.toplink.essentials.ejb.cmp3.persistence.SEPersistenceUnitInfo;
import oracle.toplink.essentials.exceptions.PersistenceUnitLoadingException;
import oracle.toplink.essentials.exceptions.StaticWeaveException;
import oracle.toplink.essentials.internal.ejb.cmp3.metadata.MetadataProcessor;
import oracle.toplink.essentials.internal.helper.EJB30ConversionManager;
import oracle.toplink.essentials.logging.DefaultSessionLog;
import oracle.toplink.essentials.logging.SessionLog;
import oracle.toplink.essentials.sessions.DatabaseLogin;
import oracle.toplink.essentials.sessions.Project;
import oracle.toplink.essentials.threetier.ServerSession;

/**
* <p>
* <b>Description</b>: This class provides the implementation of class transformer by leveraging on the following existing APIs,
* <ul>
* <li> PersistenceUnitProcessor.processORMetadata() - get class descriptor.
* <li> PersistenceUnitProcessor.buildEntityList() - get entity classes lsit.
* <li> TransformerFactory.createTransformerAndModifyProject - get class transformer.
* </ul>
* <p>
* <b>Responsibilities</b>:
* <ul>
* <li> Create the classtransformer for each persistence unit individually and store them into the list.
* <li> Provide class transfom method to perform weaving function.
* </ul>
* 
**/

public class StaticWeaveClassTransformer {
    private ArrayList<ClassTransformer> classTransformers;
    private Writer logWriter;
    private int logLevel = SessionLog.OFF;    
    private ClassLoader aClassLoader;
    
    /**
     * Constructs an instance of StaticWeaveClassTransformer
     * @param inputArchiveURL
     * @param aclassloader
     * @throws Exception
     */
    public StaticWeaveClassTransformer(URL inputArchiveURL,ClassLoader aclassloader) throws Exception {
        this(inputArchiveURL,aclassloader,null,SessionLog.OFF);
    }
    
    /**
     * Constructs an instance of StaticWeaveClassTransformer
     * @param inputArchiveURL
     * @param aclassloader
     * @param log
     * @param loglevel
     * @throws Exception
     */
    public StaticWeaveClassTransformer(URL inputArchiveURL,ClassLoader aclassloader, Writer logWriter, int loglevel) throws URISyntaxException,IOException {
        this.aClassLoader = aclassloader;
        this.logWriter=logWriter;
        this.logLevel=loglevel;
        buildClassTransformers(inputArchiveURL,aclassloader);
    }


    /**
     * INTERNAL:
     * The method performs weaving function on the given class.
     * @param originalClassName
     * @param originalClass
     * @param originalClassBytes
     * @return the converted(woven) class
     * @throws Exception
     */
    public byte[] transform(String originalClassName, Class originalClass, byte[] originalClassBytes)throws IllegalClassFormatException{
        byte[] newClassBytes = null;
        for(ClassTransformer transformer : classTransformers){
            newClassBytes=transformer.transform(aClassLoader, originalClassName,originalClass, null, originalClassBytes);
            if(newClassBytes!=null) {
                break;
            };
        }
        return newClassBytes;
    }

    /**
     * INTERNAL:
     * The method creates classtransformer list corresponding to each persistence unit. 
     * @param inputArchiveURL
     * @param aclassloader
     * @throws Exception
     */
    private void buildClassTransformers(URL inputArchiveURL,ClassLoader aclassloader) throws URISyntaxException,IOException{ 
        if(classTransformers!=null) {
            return ;
        } else{
            classTransformers = new ArrayList<ClassTransformer>();
        }
        Archive archive =null;
        try{
           archive = (new ArchiveFactoryImpl()).createArchive(inputArchiveURL);
        }catch(ZipException e){
            throw StaticWeaveException.exceptionOpeningArchive(inputArchiveURL,e);
        }
            
        List<SEPersistenceUnitInfo> persistenceUnitsList = 
        PersistenceUnitProcessor.processPersistenceArchive(archive, aclassloader);
        if(persistenceUnitsList==null){
            throw PersistenceUnitLoadingException.couldNotGetUnitInfoFromUrl(inputArchiveURL);
        }
        Iterator<SEPersistenceUnitInfo> persistenceUnitsIterator = persistenceUnitsList.iterator();
        while (persistenceUnitsIterator.hasNext()){
            SEPersistenceUnitInfo unitInfo = (SEPersistenceUnitInfo)persistenceUnitsIterator.next();
            unitInfo.setNewTempClassLoader(aclassloader);
            //build class transformer.
            ClassTransformer transformer = buildTransformer(unitInfo,this.logWriter,this.logLevel);
            classTransformers.add(transformer);
        }
    }
    
    /**
     * INTERNAL:
     * This method builds the classtransformer for the specified perisistence unit.
     * @param unitInfo
     * @param logWriter 
     * @param logLevel
     * @return a ClassTransformer
     */
        
    private ClassTransformer buildTransformer(PersistenceUnitInfo unitInfo, Writer logWriter, int logLevel) {
        //persistenceUnitInfo = unitInfo;
        ClassLoader privateClassLoader = unitInfo.getNewTempClassLoader();

        // create server session (it should be done before initializing ServerPlatform)
        ServerSession session = new ServerSession(new Project(new DatabaseLogin()));
        session.setLogLevel(logLevel);
        if(logWriter!=null){
            ((DefaultSessionLog)session.getSessionLog()).setWriter(logWriter);
         }
        
        session.getPlatform().setConversionManager(new EJB30ConversionManager());

        // Create an instance of MetadataProcessor for specified persistence unit info
        MetadataProcessor processor = new MetadataProcessor(unitInfo, session, privateClassLoader, true);
        // Process the Object/relational metadata from XML and annotations.
        PersistenceUnitProcessor.processORMetadata(processor,privateClassLoader, session, false);

        //Collection entities = buildEntityList(persistenceUnitInfo, privateClassLoader);
        Collection entities = PersistenceUnitProcessor.buildEntityList(processor,privateClassLoader);

        // The transformer is capable of altering domain classes to handle a LAZY hint for OneToOne mappings.  It will only
        // be returned if we we are mean to process these mappings
        return TransformerFactory.createTransformerAndModifyProject(session, entities, privateClassLoader);
    }
}
