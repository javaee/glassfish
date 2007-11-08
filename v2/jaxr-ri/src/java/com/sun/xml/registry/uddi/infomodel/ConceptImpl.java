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

package com.sun.xml.registry.uddi.infomodel;

import javax.xml.registry.*;
import javax.xml.registry.infomodel.*;

import com.sun.xml.registry.*;

import java.util.*;
import java.io.Serializable;

/**
 * Implementation of Concept interface. The ConceptImpl has
 * either a concept or classification scheme as its parent,
 * but not both.
 *
 * @author Farrukh S. Najmi
 * @author Kathy Walsh
 */
public class ConceptImpl extends RegistryObjectImpl implements Concept, Serializable {

    transient boolean childrenLoaded = false;
    transient boolean isPredefined = false;

    String value;   
    ConceptImpl parentConcept;
    ClassificationScheme classificationScheme; 
    ArrayList children;

    /**
     * Default constructor
     */
    public ConceptImpl() {
        super();
        children = new ArrayList();
        childrenLoaded = false;
    }
	
    public ConceptImpl(Key key) {
        this();
        this.key = key;
    }
	
    public ConceptImpl(Key key, String description, String name) {
        this(key);
        this.description = new InternationalStringImpl(description);
        this.name = new InternationalStringImpl(name);
    }
	
    public ConceptImpl(RegistryObject parent, String name, String value) throws JAXRException {
        this();
        if (parent instanceof ClassificationScheme) {
            ((ClassificationScheme) parent).addChildConcept(this);
        } else if (parent instanceof Concept){
	    ((Concept) parent).addChildConcept(this);
        }
        this.name = new InternationalStringImpl(name);
        this.value = value;
    }
	
    /**
     * Gets the value (usually a code in a taxonomy)
     * associated with this Concept.
     */
    public String getValue() throws JAXRException {
        if (value == null) {
            getObject();
        }
        return value;
    }
    
    /**
     * Sets the value (usually a code in a taxonomy)
     * associated with this Concept.
     */
    public void setValue(String value) {
        this.value = value;
        setIsModified(true);
    }
  
    /**
     * Add a child Concept
     */
    public void addChildConcept(Concept concept) throws JAXRException {
        if ((concept != null) && (!children.contains(concept))) {
            ((ConceptImpl) concept).setParentConcept(this);
            children.add(concept);
            setIsModified(true);
        }
    }

    /**
     * Add a Collection of Concept children. If parameter is
     * null, treat as empty collection.
     */
    public void addChildConcepts(Collection concepts) throws JAXRException {
        if (concepts == null) {
            return;
        }
        Iterator iter = concepts.iterator();
        try {
            while (iter.hasNext()) {
                Concept concept = (Concept) iter.next();
		addChildConcept(concept);
            }
        } catch (ClassCastException cce) {
            throw new UnexpectedObjectException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("ConceptImpl:Objects_in_collection_must_be_concepts"), cce);
        }
    }
    
    /**
     * Remove child concept
     */
    public void removeChildConcept(Concept concept) {
        if (concept != null) {
            children.remove(concept);
            setIsModified(true);
        }
    }
    
    /**
     * Remove a Collection of children Concepts. Treat
     * null parameter as empty collection.
     */
    public void removeChildConcepts(Collection concepts) {
        if (concepts != null) {
            children.removeAll(concepts);
            setIsModified(true);
        }
    }
       
    /**
     * Get number of children
     */
    public int getChildConceptCount() {
        return children.size();
    }

    /**
     * Get all immediate children Concepts
     */
    public Collection getChildrenConcepts() throws JAXRException {
        return (Collection) children.clone();
    }
	
    /**
     * Get all descendant Concepts. This method recurses
     * through all the children concepts.
     */
    public Collection getDescendantConcepts() throws JAXRException {
        ArrayList descendants = new ArrayList(children);
        Iterator iter = children.iterator();
        while (iter.hasNext()) {
            Concept child = (Concept) iter.next();
            if (child.getChildConceptCount() > 0) {
                descendants.addAll(child.getDescendantConcepts());
            }
        }
        return descendants;
    }

    /**
     * Get the parent Concept or null if
     * parent is a ClassificationScheme
     */
    public Concept getParentConcept()  throws JAXRException {
        return parentConcept;
    }
    
    /**
     * Get the parent classification scheme.
     * The method recurses up through parent concepts to get the
     * classification scheme.
     */
    public ClassificationScheme getClassificationScheme()  throws JAXRException{
        if (classificationScheme != null) {
            return classificationScheme;
        }
	return (parentConcept != null ? parentConcept.getClassificationScheme() : null);
    }
	
    /**
     * Internal method to set the ClassificationScheme. This should
     * not be set on a concept that has a concept as a parent.
     */
    public void setClassificationScheme(ClassificationScheme scheme) {
        classificationScheme = scheme;
    }
    
    /**
     * Returns the full path from classification scheme
     * down to the concept. 
     */
    public String getPath() throws JAXRException {
        if (parentConcept == null) {
            return ("/" + classificationScheme.getKey().getId() + "/" + value);
        }
        return ("/" + parentConcept.getPath() + "/" + value);
    }
    
    public RegistryObject getParent() {
        if (parentConcept != null) {
            return parentConcept;
        }
        return classificationScheme;
    }
    
    public boolean isPredefined() {
	return isPredefined;
    }
    
    public void setPredefined(boolean predefined) {
	isPredefined = predefined;
    }
    
    public boolean getChildrenLoaded() throws JAXRException {
	return childrenLoaded;
    }

    
    /**
     * For use by Composite class only
     * Use parent.addChild to set parent in client code
     */
    public void setParentConcept(Concept parent) {
        if (parent instanceof ConceptImpl) {
            this.parentConcept = (ConceptImpl)parent;
        }              
    }
	
    /**
     * Overrides behavior in RegistryObjectImpl to allow adding
     * external links. If an external link already exists, this 
     * method throws UnsupportedCapabilityException. See appendix
     * D of specification.
     */
    public void addExternalLink(ExternalLink link) throws JAXRException {
	if (externalLinks.size() > 0) {
	    throw new UnsupportedCapabilityException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("ConceptImpl:ExternalLink_already_exists,_cannot_add_more."));
	}
	if (link != null) {
	    ExternalLinkImpl externalLink = (ExternalLinkImpl) link;
	    externalLink.addLinkedObject(this);
	    externalLinks.add(externalLink);
            setIsModified(true);
	}
    }
       
    /**
     * Overrides behavior in RegistryObjectImpl to allow adding
     * external links. If an external link already exists or if
     * the collection contains more than one external link, this
     * method throws UnsupportedCapabilityException. See appendix
     * D of specification.
     */
    public void addExternalLinks(Collection links) throws JAXRException {
	if (externalLinks.size() > 0) {
	    throw new UnsupportedCapabilityException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("ConceptImpl:ExternalLink_already_exists,_cannot_add_more."));
	}
	if (links != null) {			
	    if (links.size() > 1) {
		throw new UnsupportedCapabilityException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("ConceptImpl:Cannot_add_more_than_one_ExternalLink"));
	    }
	    Iterator iter = links.iterator();
	    try {
		while (iter.hasNext()) {
		    addExternalLink((ExternalLink) iter.next());
		}	        
	    } catch (ClassCastException e) {
		throw new UnexpectedObjectException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("ConceptImpl:Objects_in_collection_must_be_ExternalLinks"), e);
	    }
	}
    }

    /**
     * Overrides behavior in RegistryObjectImpl to allow adding
     * external links. If an external link already exists or if
     * the collection contains more than one external link, this
     * method throws UnsupportedCapabilityException. See appendix
     * D of specification.
     */
    public void setExternalLinks(Collection links) throws JAXRException {
        if (links == null) {
            externalLinks.clear();
            return;
        }
	if (links.size() > 1) {
	    throw new UnsupportedCapabilityException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("ConceptImpl:Cannot_set_more_than_one_link."));
	}
	externalLinks.clear();
	addExternalLinks(links);
    }
}
