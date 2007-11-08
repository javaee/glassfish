/*
* The contents of this file are subject to the terms 
* of the Common Development and Distribution License 
* (the License).  You may not use this file except in
* compliance with the License.
* 
* You can obtain a copy of the license at 
* https://glassfish.dev.java.net/public/CDDLv1.0.html or
* glassfish/bootstrap/legal/CDDLv1.0.txt.
* See the License for the specific language governing 
* permissions and limitations under the License.
* 
* When distributing Covered Code, include this CDDL 
* Header Notice in each file and include the License file 
* at glassfish/bootstrap/legal/CDDLv1.0.txt.  
* If applicable, add the following below the CDDL Header, 
* with the fields enclosed by brackets [] replaced by
* you own identifying information: 
* "Portions Copyrighted [year] [name of copyright owner]"
* 
* Copyright 2007 Sun Microsystems, Inc. All rights reserved.
*/

package com.sun.xml.registry.common.tools;

import javax.xml.registry.*;
import javax.xml.registry.infomodel.*;

import com.sun.xml.registry.uddi.*;
import com.sun.xml.registry.uddi.infomodel.*;
import com.sun.xml.registry.common.tools.bindings_v3.*;
import com.sun.xml.registry.common.*;
import com.sun.xml.registry.common.util.*;

import java.io.*;
import java.net.*;
import java.util.*;

import java.util.logging.Logger;
import java.util.logging.Level;

import com.sun.xml.registry.common.util.*;

import java.io.File;
import java.io.IOException;

// JAXP packages
import javax.xml.parsers.*;
import java.util.Properties;

import org.w3c.dom.*;
import org.xml.sax.SAXException;
import org.xml.sax.InputSource;

import javax.xml.parsers.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedExceptionAction;
import java.security.PrivilegedActionException;


//needs major rework
public class JAXRConceptsManager {
    
    Logger logger = (Logger)
    AccessController.doPrivileged(
            new PrivilegedAction() {
        public Object run() {
            return Logger.getLogger(com.sun.xml.registry.common.util.Utility.LOGGING_DOMAIN + ".common");
        }
    });
    
    static Locale US_LOCALE = new Locale("en", "US");
    
    static PredefinedConcepts predefines;
    static PredefinedConcepts naics;
    static PredefinedConcepts iso;
    static PredefinedConcepts unsp;
    static PredefinedConcepts user;
    static Collection definedSchemes;
    ArrayList fileList = new ArrayList();
    HashMap dMap = new HashMap();
    String jaxrFile;
    String naicsFile;
    String isoFile;
    String predefinesDTD;
    HashMap idMap;
    ConnectionImpl connection;
    private static JAXRConceptsManager instance;
    
    //for jaxb
    private JAXBContext jc;
    private ObjectFactory objFactory;
    private Unmarshaller u;
    
    private static String taxonomyPath =
            "resources/";
    
    public static JAXRConceptsManager getInstance(ConnectionImpl connection) {
        
        try {
            if (instance == null)
                instance = new JAXRConceptsManager(connection);
            //maybe not do here but in constructor
            instance.loadTaxonomies();
            definedSchemes = instance.taxonomies2TaxonomyTree();
        } catch (JAXRException ex) {
            System.out.println("Failed to load taxonomies");
            ex.printStackTrace();
        }
        return instance;
    }
    
    private JAXRConceptsManager(ConnectionImpl connection){
        this.connection = connection;
        idMap = new HashMap();
        initJAXBObjectFactory();
    }
    
    private void initJAXBObjectFactory(){
        // create a JAXBContext
        try {
            if (jc == null)
                jc = JAXBContext.newInstance( "com.sun.xml.registry.common.tools.bindings_v3");
        } catch (JAXBException jbe){
            System.out.println("Exiting unable to initial JAXB context");
            //System.exit(1);
        }
        // create an ObjectFactory instance.
        // if the JAXBContext had been created with mutiple pacakge names,
        // we would have to explicitly use the correct package name when
        // creating the ObjectFactory.
        if (objFactory == null)
            objFactory = new ObjectFactory();
        try {
            u = jc.createUnmarshaller();
        } catch (JAXBException jbe){
            //need to log
        }
    }
    
    
    public Collection
            findClassificationSchemeByName(Collection findQualifiers,
            String namePattern) throws JAXRException {
        
        Collection matchingSchemes =
                getClassificationSchemeByName(findQualifiers, namePattern);
        
        return matchingSchemes;
    }
    
    public Concept findConceptByPath(String path)
    throws JAXRException {
        return getConceptsByPath2(path);
    }
    
    public Collection findClassificationSchemes(
            Collection findQualifiers,
            Collection namePatterns,
            Collection classifications,
            Collection externalLinks ) throws JAXRException {
        
        Collection matches =
                doFindClassificationSchemes(findQualifiers, namePatterns);
        return matches;
    }
    
    public Collection getChildConcepts(ClassificationScheme scheme) throws JAXRException{
        if (scheme != null)
            return scheme.getChildrenConcepts();
        return null;
    }
    
    Collection stringNames2Namepatterns(Collection patterns) {
        
        Collection namepatterns = new ArrayList();
        Iterator iter = patterns.iterator();
        Namepattern pattern = null;
        while (iter.hasNext()) {
                pattern = objFactory.createNamepattern();
		pattern.setContent(iter.next().toString());
		namepatterns.add(pattern);
        }
        return namepatterns;
    }
    
    private JAXRClassificationScheme classificationScheme2JAXRClassificationScheme(ClassificationScheme scheme)
    throws JAXRException {
        
        if (scheme != null) {
            logger.finest("Scheme is not null");
            String key = scheme.getKey().getId();
            String name = null;
            InternationalString iName = scheme.getName();
            if (iName != null)
                name = iName.getValue();
	    String description = null;
            JAXRClassificationScheme jaxrScheme	= null;
            jaxrScheme = objFactory.createJAXRClassificationScheme();
            jaxrScheme.setId(key);
            jaxrScheme.setName(name);
            if (jaxrScheme != null)
                logger.finest("jaxrScheme is not null in cs2jrcl");
            return jaxrScheme;
        }
        return null;
    }
    
    
    
    Collection jaxrClassificationSchemes2ClassificationSchemes(Collection schemes)
    throws JAXRException {
        
        Collection classificationSchemes = new ArrayList();
        if (schemes != null) {
            
            Iterator iter = schemes.iterator();
            ClassificationSchemeImpl classificationScheme = null;
            while (iter.hasNext()) {
                JAXRClassificationScheme scheme = (JAXRClassificationScheme)iter.next();
                String key = scheme.getId();
                String name = scheme.getName();
                String description = scheme.getDescription();
                classificationScheme =
                        new ClassificationSchemeImpl(new KeyImpl(key));
                classificationScheme.setName(new InternationalStringImpl(US_LOCALE, name));
                classificationScheme.setDescription(new InternationalStringImpl(US_LOCALE, description));
                //set it as predefined and external
                //classificationScheme.setExternal(true);
                classificationScheme.setPredefined(true);
                
                //add it to the collection of classificationSchemes
                idMap.put(classificationScheme.getKey().getId(),
                        classificationScheme);
                classificationSchemes.add(classificationScheme);
            }
        }
        return classificationSchemes;
    }
    
    ClassificationScheme jaxrClassificationScheme2ClassificationScheme(JAXRClassificationScheme scheme)
    throws JAXRException {
        
        ClassificationSchemeImpl classificationScheme = null;
        if (scheme != null) {
            String key = scheme.getId();
            String name = scheme.getName();
            String description = scheme.getDescription();
            classificationScheme = new ClassificationSchemeImpl(new KeyImpl(key));
            classificationScheme.setName(new InternationalStringImpl(US_LOCALE, name));
            classificationScheme.setDescription(new InternationalStringImpl(US_LOCALE, description));
            //set it as predefined and external
            //classificationScheme.setExternal(true);
            classificationScheme.setPredefined(true);
            
            //add it to the collection of classificationSchemes
            idMap.put(classificationScheme.getKey().getId(),
                    classificationScheme);
        }
        return classificationScheme;
    }
    
    
    Collection jaxrConcepts2Concepts(ClassificationScheme scheme,
        Collection jaxrConcepts) throws JAXRException {
        Collection concepts = new ArrayList();
        idMap.clear();
        ClassificationSchemeImpl parentScheme = null;
        ClassificationSchemeImpl classificationScheme =
                (ClassificationSchemeImpl)scheme;
        if (scheme != null) {
            classificationScheme.setPredefined(true);
            
            //add it to the collection of classificationSchemes
            idMap.put(classificationScheme.getKey().getId(),
                    classificationScheme);
            
            if (jaxrConcepts != null) {
                Iterator iter = jaxrConcepts.iterator();
                while (iter.hasNext()) {
                    ConceptImpl concept = null;
                    JAXRConcept jaxrConcept = (JAXRConcept)iter.next();
                    String ckey = jaxrConcept.getId();
                    String cname = jaxrConcept.getName();
                    String cvalue = jaxrConcept.getCode();
                    String parentId = jaxrConcept.getParent();
                    
                    concept = new ConceptImpl();
                    concept.setKey(new KeyImpl(ckey));
                    concept.setIsRetrieved(true);
                    concept.setIsLoaded(true);
                    concept.setName(new InternationalStringImpl(US_LOCALE, cname));
                    concept.setValue(cvalue);
                    concept.setPredefined(true);
                    idMap.put(ckey, concept);
                    
                    Object parent = idMap.get(parentId);
                    if (parent == null) {
                        concepts.add(concept);
                        //logger.finest("Error in file");
                    }
                    if (parent instanceof ClassificationSchemeImpl) {
                        parentScheme =
                                (ClassificationSchemeImpl) parent;
                        //should make sure parent is classificationscheme
                        concept.setClassificationScheme(parentScheme);
                        parentScheme.addChildConcept(concept);
                    } else if (parent instanceof ConceptImpl) {
                        //logger.finest("Found Concept");
                        ConceptImpl parentConcept =
                                (ConceptImpl) parent;
                        concept.setParentConcept(concept);
                        parentConcept.addChildConcept(concept);
                    }
                }
                if (parentScheme != null) {
                    parentScheme.setChildrenLoaded(true);
                    concepts.addAll(parentScheme.getChildrenConcepts());
                }
            }   //maybe should check for null parentScheme?
            
        } //end if
        return concepts;
    } //end method
    
    ClassificationScheme jaxrConcepts2Concepts2(ClassificationScheme scheme,
            Collection jaxrConcepts) throws JAXRException {
        
        Collection concepts = new ArrayList();
        ClassificationSchemeImpl classificationScheme =
                (ClassificationSchemeImpl)scheme;
        if (scheme != null) {
            //classificationScheme.setExternal(true);
            classificationScheme.setPredefined(true);
            
            //add it to the collection of classificationSchemes
            idMap.put(classificationScheme.getKey().getId(),
                    classificationScheme);
            
            if (jaxrConcepts != null) {
                Iterator iter = jaxrConcepts.iterator();
                while (iter.hasNext()) {
                    
                    JAXRConcept jaxrConcept = (JAXRConcept)iter.next();
                    String ckey = jaxrConcept.getId();
                    String cname = jaxrConcept.getName();
                    String cvalue = jaxrConcept.getCode();
                    String parentId = jaxrConcept.getParent();
                    Collection jaxrChildConcepts = jaxrConcept.getJAXRConcept();
                    
                    ConceptImpl concept = new ConceptImpl();
                    concept.setKey(new KeyImpl(ckey));
                    concept.setIsRetrieved(true);
                    concept.setIsLoaded(true);
                    concept.setName(new InternationalStringImpl(US_LOCALE, cname));
                    concept.setValue(cvalue);
                    concept.setPredefined(true);
                    if ((jaxrChildConcepts != null) && (!jaxrChildConcepts.isEmpty())){
                        
                        concepts =
                                jaxrChildConcepts2ConceptCollection(jaxrChildConcepts, concept);
                    }
                    if (concepts != null) {
                        Iterator citer = concepts.iterator();
                        while (citer.hasNext()){
                            Concept aconcept = (Concept)citer.next();
                            classificationScheme.addChildConcept(aconcept);
                        }
                    }
                }
            }   //maybe should check for null parentScheme?
        } //end if
        return classificationScheme;
    } //end method
    
    Collection jaxrChildConcepts2ConceptCollection(Collection jaxrConcepts,
            ConceptImpl parentConcept) throws JAXRException {
        Collection concepts = new ArrayList();
        Iterator jiter = jaxrConcepts.iterator();
        while (jiter.hasNext()) {
            JAXRConcept jconcept = (JAXRConcept)jiter.next();
            String name = jconcept.getName();
            String parent = jconcept.getParent();
            String id = jconcept.getId();
            String value = jconcept.getCode();
            Collection childConcepts = jconcept.getJAXRConcept();
            
            //do concept here
            ConceptImpl concept = new ConceptImpl();
            concept.setKey(new KeyImpl(id));
            concept.setIsRetrieved(true);
            concept.setIsLoaded(true);
            concept.setName(new InternationalStringImpl(US_LOCALE, name));
            concept.setParentConcept(parentConcept);
            concept.setValue(value);
            if (childConcepts != null) {
                //do recursion here
                Collection childconcepts =
                        jaxrChildConcepts2ConceptCollection(childConcepts, concept);
            }
            
            if (concept != null) {
                concepts.add(concept);
                parentConcept.addChildConcept(concept);
            }
        }
        return concepts;
    }
    
    Collection keysFromJAXRObjects(Collection jaxrObjects) {
        
        Collection keys = new ArrayList();
        Iterator iter = jaxrObjects.iterator();
        while (iter.hasNext()) {
            Object jaxrObject = iter.next();
            if (jaxrObject instanceof JAXRClassificationScheme) {
                KeyImpl key =
                        new KeyImpl( ((JAXRClassificationScheme)jaxrObject).getId());
                keys.add(key);
            } else {
                KeyImpl key =
                        new KeyImpl( ((JAXRConcept)jaxrObject).getParent());
                //let's hope it's a JAXRConcept
                keys.add(key);
            }
        }
        return keys;
    }
    
    DocumentBuilder createDocumentBuilder() {
        
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        
        dbf.setNamespaceAware(false);
        dbf.setValidating(false);
        
        try {
            return dbf.newDocumentBuilder();
        } catch (ParserConfigurationException pce) {
            IllegalArgumentException iae = new IllegalArgumentException(pce.getMessage());
            iae.initCause(pce);
            throw iae;
        }
    }
    
    Collection getAllClassificationSchemes(){
        
        Collection defines = dMap.values();
        Collection allSchemes = new ArrayList();
        Iterator diter = defines.iterator();
        List schemes = null;
        while (diter.hasNext()) {
            PredefinedConcepts pConcepts =
                    (PredefinedConcepts)diter.next();
            
            schemes =
                    pConcepts.getJAXRClassificationScheme();
            Iterator iter = schemes.iterator();
        }
        return schemes;
    }
    
    Collection getClassificationSchemeByName(Collection findQualifiers, String name)
    throws JAXRException {
        
        ClassificationScheme jscheme = null;
        Collection allMatches = new ArrayList();
        Iterator iter = definedSchemes.iterator();
        while (iter.hasNext()) {
            jscheme = (ClassificationScheme)iter.next();
            String pname = jscheme.getName().getValue(US_LOCALE);
            
            String uname = name.toUpperCase();
            
            if ( (name.indexOf(pname) != -1) ||
                    (pname.indexOf(name) != -1) ||
                    (pname.equalsIgnoreCase(name)) ){
                allMatches.add(jscheme);
                return allMatches;
            }
            
            if ( (uname.indexOf(pname) != -1) ||
                    (pname.indexOf(uname) != -1) ||
                    (pname.equalsIgnoreCase(uname)) ){
                allMatches.add(jscheme);
                return allMatches;
            }
            
            
            
            char escapeChar = '\\';
            if (name.indexOf("%") == -1) {
                String mname = name + "%";
                if (matchPattern(mname, pname, escapeChar)){
                    allMatches.add(jscheme);
                    continue;
                }
                mname = "%" + name;
                if (matchPattern(mname, pname, escapeChar)){
                    allMatches.add(jscheme);
                    continue;
                }
                mname = "%" + name + "%";
                if (matchPattern(mname, pname, escapeChar)){
                    allMatches.add(jscheme);
                    continue;
                }
                mname = uname + "%";
                if (matchPattern(mname, pname, escapeChar)){
                    allMatches.add(jscheme);
                    continue;
                }
                mname = "%" + uname;
                if (matchPattern(mname, pname, escapeChar)){
                    allMatches.add(jscheme);
                    continue;
                }
                mname = "%" + uname + "%";
                if (matchPattern(mname, pname, escapeChar)){
                    allMatches.add(jscheme);
                    continue;
                }
            } else {
                if (matchPattern(name, pname, escapeChar)){
                    allMatches.add(jscheme);
                    continue;
                }
                if (matchPattern(uname, pname, escapeChar)){
                    allMatches.add(jscheme);
                }
            }
            
        }
        return allMatches;
    }
    
    public ClassificationScheme getClassificationSchemeById(String id)
    throws JAXRException {
        
        Iterator iter = definedSchemes.iterator();
        while (iter.hasNext()) {
            ClassificationScheme scheme = (ClassificationScheme)iter.next();
            String cid = scheme.getKey().getId();
            if ( cid.equalsIgnoreCase(id) ){
                return scheme;
            }
        }
        return null;
    }
    
    Concept getConceptById(Collection childConcepts, String id)
    throws JAXRException {
        
        Iterator iter = childConcepts.iterator();
        while (iter.hasNext()) {
            Concept concept = (Concept)iter.next();
            if (concept.getKey().getId().equalsIgnoreCase(id))
                return concept;
            else {
                Collection children = concept.getChildrenConcepts();
                Concept found = null;
                if (children != null)
                    found = getConceptById(children, id);
                if (found != null)
                    return found;
            }
        }
        return null;
    }
    
    
    public Concept getConceptById(String id) throws JAXRException{
        logger.finest("Id is " + id);
        if (id == null)
            return null;
        Iterator iter = definedSchemes.iterator();
        while (iter.hasNext()) {
            ClassificationScheme scheme = (ClassificationScheme)iter.next();
            Collection children = scheme.getChildrenConcepts();
            if (children != null) {
                Iterator citer = children.iterator();
                while (citer.hasNext()) {
                    Concept concept = (Concept) citer.next();
                    if (concept.getKey().getId().equalsIgnoreCase(id))
                        return concept;
                    else {
                        Collection conceptChildren = concept.getChildrenConcepts();
                        Concept found = null;
                        if (conceptChildren != null)
                            found = getConceptById(conceptChildren, id);
                        if (found != null)
                            return found;
                    }
                }
            }
        }
        return null;
    }
    
    
    JAXRClassificationScheme getClassificationSchemeForConcept(JAXRConcept concept){
        
        String cid = concept.getParent();
        Collection schemes = getAllClassificationSchemes();
        Iterator iter = schemes.iterator();
        while (iter.hasNext()) {
            JAXRClassificationScheme jscheme =
                    (JAXRClassificationScheme)iter.next();
            if (cid.equals(jscheme.getId()) ){	//found a match
                return jscheme;
            }
        }
        return null;
    }
    
    
    boolean hasChildren(Collection concepts, String id) {
        Iterator citer = concepts.iterator();
        while (citer.hasNext()) {
            JAXRConcept pconcept = (JAXRConcept)citer.next();
            String parent = pconcept.getParent();
            if (parent.equals(id) ){
                return true;
            }
        }
        return false;
    }
    
    boolean hasChildrenConcepts(JAXRClassificationScheme scheme) {
        
        Collection concepts = scheme.getJAXRConcept();
        if (concepts != null) {
            if (concepts.size() > 0) {
                return true;
            }
            return false;
        }
        return false;
    }
    
    Concept getConceptsByPath2(String xpath)
    throws JAXRException {
        String anyPath = "*"; //any path element in the path
        String anyDescendant = "//";
        String delim = "/";
        boolean escaped = false;
        String  tok = null;
        ArrayList tokens = new ArrayList();
        if (xpath != null) {
            StringTokenizer st = new StringTokenizer(xpath, delim, true);
            //Parse string into a Collection of tokens since we will need to peek forward as we
            //scan tokens
            int k = 1;
            while (st.hasMoreTokens()) {
                tok = st.nextToken();
                tokens.add(tok);
            }
        } else throw new JAXRException(ResourceBundle.getBundle("com/sun/xml/registry/common/LocalStrings").getString("JAXRConceptsManager:Path_is_null"));
        Concept concept = null;
        int i = 0;
        int numtoks = tokens.size();
        if (i < numtoks) {
            
            String delimOne = (String)tokens.get(i);
            i++;
            if (delimOne.equals("/")) {
                //continue - malformed XPATH Request
                String classificationId =  (String)tokens.get(i);
                i++;
                ClassificationScheme scheme =
                        getClassificationSchemeById(classificationId);
                if (scheme == null)
                    return null;
                logger.finest("scheme.getName " + scheme.getName().getValue());
                Collection children = scheme.getChildrenConcepts();
                while (i < numtoks) {
                    String delimn = (String)tokens.get(i);
                    i++;
                    if (delimn.equals("/")) {
                        if (i < numtoks) {
                            String value = (String)tokens.get(i);
                            i++;
                            if (children != null) {
                                concept = getConceptByValue(children , value);
                                if (concept != null) {
                                    logger.finest("FirstConcept with value" + concept.getValue());
                                    children = concept.getChildrenConcepts();
                                    if (children == null) {
                                        logger.finest("children are null for " + concept.getValue());
                                    }
                                }
                            }
                        }
                    }
                }
                return concept;
            }
        }
        return null;
    }
    
    Concept getConceptByValue(Collection childConcepts, String value)
    throws JAXRException {
        
        Iterator iter = childConcepts.iterator();
        while (iter.hasNext()) {
            Concept concept = (Concept)iter.next();
            String cvalue = concept.getValue();
            if (cvalue != null) {
                if (cvalue.equalsIgnoreCase(value))
                    return concept;
            }
        }
        return null;
    }
    
    Concept findConceptByValue(RegistryObject parent, String value)
    throws JAXRException {
        Collection children = null;
        if (parent instanceof ClassificationScheme)
            children = ((ClassificationScheme)parent).getChildrenConcepts();
        else if (parent instanceof Concept)
            children = ((Concept)parent).getChildrenConcepts();
        
        Concept concept = null;
        if (children != null) {
            Iterator citer = children.iterator();
            while (citer.hasNext()) {
                concept = (Concept)citer.next();
                logger.finest("Concept in find by value" + concept.getValue());
                String cvalue = concept.getValue();
                if (cvalue != null) {
                    if (cvalue.equalsIgnoreCase(value)) {
                        return concept;
                    }
                }
            }
        } else logger.finest("Children are null");
        return null;
    }
    
    Collection getConceptsByPath(String xpath, char escapeChar)
    throws javax.xml.parsers.ParserConfigurationException {
        
        String escapeCharStr = String.valueOf(escapeChar);
        String anyPath = "*"; //any path element in the path
        String anyDescendant = "//";
        String delim = "/";
        String delims = delim + escapeCharStr;
        boolean escaped = false;
        String  tok = null;
        ArrayList tokens = new ArrayList();
        if (xpath != null) {
            StringTokenizer st = new StringTokenizer(xpath, delims, true);
            //Parse string into a Collection of tokens since we will need to peek forward as we
            //scan tokens
            int k = 1;
            while (st.hasMoreTokens()) {
                tok = st.nextToken();
                tokens.add(tok);
            }
        }
        
        Collection concepts = new ArrayList();
        //logger.finest("In getConceptsByPath");
        PredefinedConcepts predefines = (PredefinedConcepts)dMap.get(naics);
        Element el = null;
        
        int j = 0;
        String uuid = null;
        tok = (String)tokens.get(j);
        Element firstConcept = null;
        while (j <= tokens.size()) {
            String nexttok = null;
            if (tok.equals(delim)) {
                j++;
                if (j >= tokens.size()) break;
                nexttok = (String)tokens.get(j);
                if (nexttok.equals(delim)) {
                    //this is a wildcard meaning any descendant
                    j++;
                    if (j >= tokens.size()) break;
                    tok = (String)tokens.get(j);
                    if (tok.indexOf("uuid:") != -1)
                        el = findElementByUUID( tok, el);
                    else {
                        if (firstConcept == null)
                            firstConcept = el;
                        el = findElementByCode(tok, firstConcept);
                    }
                    //get next token
                    j++;
                    if (j >= tokens.size()) break;
                    tok = (String)tokens.get(j);
                } else if (nexttok.equals(anyPath)) {
                    if (nexttok.indexOf("uuid:") != -1)
                        el = findElementByUUID( nexttok, el);
                    else {
                        if (firstConcept == null)
                            firstConcept = el;
                        el = findElementByCode(nexttok, firstConcept);
                    }
                    //get next token
                    j++;
                    if (j >= tokens.size()) break;
                    tok = (String)tokens.get(j);
                } else {
                    if (nexttok.indexOf("uuid:") != -1)
                        el = findElementByUUID( nexttok, el);
                    else {
                        if (firstConcept == null)
                            firstConcept = el;
                        el = findElementByCode(nexttok, firstConcept);
                    }
                    //get next token
                    j++;
                    if (j >= tokens.size()) break;
                    tok = (String)tokens.get(j);
                }
            } else break;
        }
        if (el != null) {
            JAXRConcept jconcept = null;
            String tagName = el.getTagName();
            
            if (tagName.equals("JAXRConcept")) {
                
                if (jconcept == null)
                    logger.warning(ResourceBundle.getBundle("com/sun/xml/registry/common/LocalStrings").getString("JAXRConceptsManager:jconcept_is_null"));
                concepts.add(jconcept);
                return concepts;
            }
        }
        return null;
    }
    
    Element findElementByUUID(String uuid, Element el) {
        
        NodeList nlist1 = ((Element)el).getElementsByTagName("JAXRClassificationScheme");
        for (int i = 0; i < nlist1.getLength(); i++) {
            Node n = nlist1.item(i);
            String pname = ((Element)n).getTagName();
            String id = ((Element)n).getAttribute("id");
            if (id.equals(uuid)) {
                String name = ((Element)n).getAttribute("name");
                return (Element)n;
            }
        }
        return null;
    }
    
    Element findElementByCode(String code, Element el) {
        
        NodeList nlist1 = ((Element)el).getElementsByTagName("JAXRConcept");
        for (int i = 0; i < nlist1.getLength(); i++) {
            Node n = nlist1.item(i);
            String pname = ((Element)n).getTagName();
            String ecode = ((Element)n).getAttribute("code");
            if (ecode.equals(code)) {
                String name = ((Element)n).getAttribute("name");
                return (Element)n;
            }
        }
        return null;
    }
    
    
    
    /**
     * Used to determine selector match in a SQL LIKE experssion as in
     * <\p>
     * str LIKE patternStr [ESCAPE escapeChar]
     * <\p>
     *
     * @param patternStr The pattern used in SQL LIKE statement
     * @param str The string being compared with patternStr in SQL LIKE statement
     * @param escapeChar The escape character used to treat wildcards '_' and '%' as normal
     *
     * @return
     *
     * @see
     */
    boolean matchPattern(String patternStr, String str, char escapeChar) {
        boolean matched = false;
        String escapeCharStr = String.valueOf(escapeChar);
        String wildCards = "_%";
        String delims = wildCards + escapeCharStr;
        boolean escaped = false;
        int     index = 0;
        String  tok = null;
        
        // can remove this for logging and rely on log level
        boolean debug = false;
        
        try {
            
            if (str != null) {
                StringTokenizer st = new StringTokenizer(patternStr, delims, true);
                //Parse string into a Collection of tokens since we will need to peek forward as we
                //scan tokens
                ArrayList tokens = new ArrayList();
                int k = 1;
                while (st.hasMoreTokens()) {
                    tok = st.nextToken();
                    if (debug) {
                        logger.finest(k++ + " matchPattern Token=" + tok);
                    }
                    tokens.add(tok);
                }
                matched = true;
                
                //Iterate over tokens list and match each token with str
                int numTokens = tokens.size();
                for (int i=0; i<numTokens; i++) {
                    tok = (String)tokens.get(i);
                    // Token can be a delimeter or actual token
                    if (tok.equals(escapeCharStr) && (!escaped)) {
                        //Remember that the next character in patterStr must be treated literally
                        escaped = true;
                    } else if (tok.equals("%") && (! escaped)) {
                        if (i == (numTokens - 1) ) {
                            // wildcard is last character in pattern,
                            // match entire string.
                            index = str.length();
                        } else if (i != numTokens-1) {    //There are more tokens. If not then we have a match
                            //Now scan forward
                            int _cnt = 0; //count of '_' delimeters encountered
                            ++i;
                            for (; i<numTokens; i++) {
                                tok = (String)tokens.get(i);
                                if (tok.equals(escapeCharStr) && (!escaped)) {
                                    //Remember that the next character in patterStr must be treated literally
                                    escaped = true;
                                } else if (tok.equals("%") && (! escaped)) {
                                    //% followed by % is same as %
                                } else if (tok.equals("_") && (! escaped)) {
                                    ++_cnt;
                                } else {
                                    //This is the nextNonDelimTok
                                    int oldIndex = index;
                                    
                                    if (i == (numTokens -1)) {
                                        
                                        // Not a general purpose fix for
                                        // wildcard matching bug.
                                        // At least handle case when
                                        // only one wildcard in pattern
                                        // that has a group of characters
                                        // trailing it.
                                        if (str.endsWith(tok)) {
                                            index = str.length() - tok.length();
                                        } else {
                                            matched = false;
                                            if (debug) {
                                                logger.finest("no matched5 for token: '" + tok + "'");
                                            }
                                        }
                                    } else {
                                        index = str.indexOf(tok, index);
                                    }
                                    
                                    if (index < 0) {
                                        matched = false;
                                        if (debug) {
                                            logger.finest("no matched1 for token: '" + tok + "'");
                                        }
                                    } else {
                                        //Make sure that we have _cnt charecters between old index and new index
                                        if (index - oldIndex >= _cnt) {
                                            index += tok.length();
                                            if (debug) {
                                                logger.finest("matched1: " + str.substring(0, index));
                                            }
                                        } else {
                                            matched = false;
                                            if (debug) {
                                                logger.finest("no matched 2 for token: '" + tok + "'");
                                            }
                                        }
                                    }
                                    escaped = false;
                                    break;
                                }
                            }
                        }
                    } else if (tok.equals("_") && (!escaped)) {
                        index++;
                        if (debug) {
                            logger.finest("matched2: " + str.substring(0, index));
                        }
                    } else {
                        //Compare token read with corresponding string
                        int tokLen = tok.length();
                        
                        if (debug) {
                            logger.finest(index + " " + tokLen);
                        }
                        if (index + tokLen <= str.length()) {
                            String  subStr = null;
                            
                            try {
                                subStr = str.substring(index, index + tokLen);
                            } catch (StringIndexOutOfBoundsException e) {
                                matched = false;
                                break;
                            }
                            
                            if (!subStr.equalsIgnoreCase(tok)) {
                                matched = false;
                                if (debug) {
                                    logger.finest("no matched3 for token: '" + tok + "'");
                                }
                                
                                break;
                            } else {
                                index = index + tok.length();
                                if (debug) {
                                    logger.finest("matched3: " + str.substring(0, index));
                                }
                            }
                        } else {
                            matched = false;
                            if (debug) {
                                logger.finest("no matched4 for token: '" + tok + "'");
                            }
                            
                            break;
                        }
                        escaped = false;
                    }
                }
            }
            if (matched && index != str.length()) {
                if (debug) {
                    logger.finest("no match5(remainder): " + str.substring(index, str.length()));
                }
                matched = false;
            }
            if (debug) {
                logger.finest("JAXR:matchPattern patternStr = \'" +
                        patternStr + "\' str = \'" +
                        str + "\' matched = " + matched);
            }
        } catch (StringIndexOutOfBoundsException e) {
            matched = false;
            if (debug) {
                e.printStackTrace();
                logger.finest("HANDLED OUTOFBOUNDS JAXR:matchPattern patternStr = \'" +
                        patternStr + "\' str = \'" +
                        str + "\' matched = " + matched);
                
            }
        } finally {
            return matched;
        }
    }
    
    Collection doFindClassificationSchemes(Collection findQualifiers, Collection namepatterns)
    throws JAXRException {
        
        Collection matchingSchemes = null;
        Iterator niter = namepatterns.iterator();
        while (niter.hasNext()) {
            String name = (String)niter.next();
            
            matchingSchemes =
                    getClassificationSchemeByName(findQualifiers, name);
        }
        return matchingSchemes;
    }
    
    void loadTaxonomies() throws JAXRException {
        
        String naics = taxonomyPath + "naics.xml";
        String iso3166 = taxonomyPath + "iso3166.xml";
        String unspsc = taxonomyPath + "unspsc.xml";
        String jaxrconcepts = taxonomyPath + "jaxrconcepts.xml";
        
        try {
            fileList.add(naics);
            fileList.add(iso3166);
            fileList.add(unspsc);
            fileList.add(jaxrconcepts);
            
            String userDefined = connection.getUserDefinedTaxonomy();
            if (userDefined != null) {
                
                // parse filenames and add to list
                if (logger.isLoggable(Level.FINEST)) {
                    logger.finest("Parsing user defined taxonomy filenames");
                }
                
                // remove spaces
                String space = " ";
                StringTokenizer spaceTokenizer =
                        new StringTokenizer(userDefined, space);
                StringBuffer udBuffer = new StringBuffer();
                while (spaceTokenizer.hasMoreElements()) {
                    udBuffer.append(spaceTokenizer.nextToken());
                }
                userDefined = udBuffer.toString();
                logger.finest(userDefined);
                StringTokenizer tokenizer = new StringTokenizer(userDefined, "|");
                while (tokenizer.hasMoreElements()) {
                    final String filename = tokenizer.nextToken();
                    if (!fileList.contains(filename)) {
                        if (logger.isLoggable(Level.FINEST)) {
                            logger.finest("Adding filename to list to load: " + filename);
                        }
                        fileList.add(filename);
                    }
                }
            }
            
            Iterator iter = fileList.iterator();
            while(iter.hasNext()) {
                final String filename = (String)iter.next();
                if (logger.isLoggable(Level.FINEST)) {
                    logger.finest("Filename is " + filename);
                }
                InputStream is =   (InputStream)
                AccessController.doPrivileged(
                        new PrivilegedAction() {
                    public Object run() {
                        return this.getClass().getResourceAsStream(filename);
                    }
                });
                if (is == null) {
                    logger.finest("Could not load input stream. Try file lookup.");
                    try {
                        is = (InputStream)
                        AccessController.doPrivileged(
                                new PrivilegedAction() {
                            public Object run() {
                                try {
                                    return new FileInputStream(filename);
                                } catch (FileNotFoundException fnfe) {
                                    fnfe.printStackTrace();
                                }
                                return null;
                            }
                        });
                    } catch (Throwable t) {
                        
                        // cannot load files in some cases
                        logger.log(Level.FINEST, t.getMessage(), t);
                    }
                }
                if (is == null) {
                    logger.warning(ResourceBundle.getBundle("com/sun/xml/registry/common/LocalStrings").getString("JAXRConceptsManager:Could_not_load_file:_") + filename);
                } else {
                    
                    PredefinedConcepts predefines =
                            (PredefinedConcepts)u.unmarshal( is );
                    List schemes =
                            predefines.getJAXRClassificationScheme();
                    if (schemes.size() > 0) {
                        for (int i = 0; i < schemes.size(); i++) {
                            JAXRClassificationScheme scheme =
                                    (JAXRClassificationScheme)schemes.get(i);
                            dMap.put(filename + i, scheme);
                            logger.finest("Got Scheme Success" + filename + " ");
                        }
                        logger.finest("Got Scheme Success");
                        
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new JAXRException(ResourceBundle.getBundle("com/sun/xml/registry/common/LocalStrings").getString("JAXRConceptsManager:Cannot_initialize:_") + e.getMessage(), e);
        }
    }
    
    
    //let transfor the readin files to ClassificationSchemes and Concepts
    Collection taxonomies2TaxonomyTree() throws JAXRException {
        Collection taxonomies = dMap.values();
        Collection schemes = new ArrayList();
        Iterator titer = taxonomies.iterator();
        
        while (titer.hasNext()){
            JAXRClassificationScheme jaxrScheme = (JAXRClassificationScheme) titer.next();
            Collection jaxrConcepts = jaxrScheme.getJAXRConcept();
            ClassificationScheme scheme = jaxrClassificationScheme2ClassificationScheme(jaxrScheme);
            if (scheme.getName().getValue(US_LOCALE).indexOf("unspsc") != -1) {
                scheme = jaxrConcepts2Concepts2(scheme, jaxrConcepts);
            } else {
                Collection concepts =
                        jaxrConcepts2Concepts(scheme, jaxrConcepts);
            }
            schemes.add(scheme);
        }
        return schemes;
    }
    
    void taxonomyTree2TaxonomyFile() throws JAXRException {
        
        if (definedSchemes != null) {
            logger.finest("Have DefinedSchemes");
            Iterator diter = definedSchemes.iterator();
            while (diter.hasNext()) {
                
                ClassificationScheme scheme = (ClassificationScheme)diter.next();
                JAXRClassificationScheme jaxrScheme =
                        scheme2JAXRSchemeTree(scheme);
                //now write 2 file
            }
        }
    }
    
    JAXRClassificationScheme scheme2JAXRSchemeTree(ClassificationScheme scheme)
    throws JAXRException {
        
        if (scheme != null) {
            JAXRClassificationScheme jaxrScheme =
                    classificationScheme2JAXRClassificationScheme(scheme);
            Collection children = scheme.getChildrenConcepts();
            Collection jaxrConcepts =
                    concepts2JAXRConceptsTree(jaxrScheme.getId(),children);
            if (jaxrConcepts != null) {
                jaxrScheme.getJAXRConcept().addAll(jaxrConcepts);
            }
            return jaxrScheme;
        }
        return null;
    }
    
    Collection concepts2JAXRConceptsTree(String parentId, Collection childConcepts)
    throws JAXRException {
        
        Collection jaxrConcepts = null;
        if (childConcepts != null){
            jaxrConcepts = new ArrayList();
            Iterator citer = childConcepts.iterator();
            while (citer.hasNext()) {
                Concept concept = (Concept)citer.next();
                String id = concept.getKey().getId();
                String name = concept.getName().getValue();
                String value = null;
                try {
                    value = concept.getValue();
                } catch (Exception ex) {
                    value = "";
                }
                Collection children = concept.getChildrenConcepts();
                JAXRConcept jconcept = null; //todo: new JAXRConcept();
                jconcept.setName(name);
                jconcept.setId(id);
                jconcept.setCode(value);
                jconcept.setParent(parentId);
                
                Collection jconcepts = null;
                
                if (children != null) {
                    jconcepts = concepts2JAXRConceptsTree(id, children);
                    if ((jconcepts != null) && (jconcepts.size() > 0)){
                        jconcept.getJAXRConcept().addAll(jconcepts);
                    }
                }
                if (jconcept != null){
                    jaxrConcepts.add(jconcept);
                }
            }
        }
        return jaxrConcepts;
    }
    
}
