/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
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

package jaxb1.impl.runtime;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.xml.XMLConstants;

import org.xml.sax.SAXException;

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;
import com.sun.xml.bind.marshaller.NamespaceSupport;

/**
 * Implementation of the NamespaceContext2.
 * 
 * This class also provides several utility methods for
 * XMLSerializer-derived classes.
 * 
 * The startElement method and the endElement method need to be called
 * appropriately when used. See javadoc for those methods for details.
 */
public class NamespaceContextImpl implements NamespaceContext2
{
    /**
     * Sequence generator. Used as the last resort to generate
     * unique prefix.
     */
    private int iota = 1;
    
    /**
     * Used to maintain association between prefixes and URIs.
     */
    private final NamespaceSupport nss = new NamespaceSupport();
    
    /**
     * A flag that indicates the current mode of this object.
     */
    private boolean inCollectingMode;
    
    /** Assigns prefixes to URIs. Can be null. */
    private final NamespacePrefixMapper prefixMapper;
    
    /**
     * Used during the collecting mode to sort out the namespace
     * URIs we need for this element.
     * 
     * A map from prefixes to namespace URIs.
     */
    private final Map decls = new HashMap();
    
    private final Map reverseDecls = new HashMap();
    
    
    public NamespaceContextImpl(NamespacePrefixMapper _prefixMapper) {
        this.prefixMapper = _prefixMapper;
        // declare the default namespace binding
        // which are effective because of the way XML1.0 is made
        nss.declarePrefix("","");
        nss.declarePrefix( "xmlns", XMLConstants.XMLNS_ATTRIBUTE_NS_URI );
// this one is taken care of by the NamespaceSupport class by default.
//        nss.declarePrefix( "xml",   XMLConstants.XML_NS_URI );
    }
    
    public final NamespacePrefixMapper getNamespacePrefixMapper() {
        return prefixMapper;
    }
    
//
//
// public methods of MarshallingContext
//
//
    /**
     * @param requirePrefix
     *      true if this is called for attribute name. false otherwise.
     */
    public String declareNamespace( String namespaceUri, String preferedPrefix, boolean requirePrefix ) {
        if( !inCollectingMode ) {
            if( !requirePrefix && nss.getURI("").equals(namespaceUri) )
                return "";  // can use the default prefix. use it whenever we can
            
            // find a valid prefix for this namespace URI
            // ASSERTION: the result is always non-null,
            // since we require all the namespace URIs to be declared while
            // this object is in collection mode.
            if (requirePrefix)
                return nss.getPrefix2(namespaceUri);
            else
                return nss.getPrefix(namespaceUri);
        } else {
            if( requirePrefix && namespaceUri.length()==0 )
                return "";
            
            // collect this new namespace URI
            String prefix = (String)reverseDecls.get(namespaceUri);
            if( prefix!=null ) {
                if( !requirePrefix || prefix.length()!=0 ) {
                    // this namespace URI is already taken care of,
                    // and it satisfies the "requirePrefix" requirement.
                    return prefix;
                } else {
                    // the prefix was already allocated but it's "",
                    // and we specifically need non-empty prefix.
                    
                    // erase the current binding
                    decls.remove(prefix);
                    reverseDecls.remove(namespaceUri);
                }
            }
            
            
            if( namespaceUri.length()==0 ) {
                // the empty namespace URI needs to be bound to the default prefix.
                prefix = "";
            } else {
                // see if this namespace URI is already in-scope
                prefix = nss.getPrefix(namespaceUri);
                if( prefix==null )
                    prefix = (String)reverseDecls.get(namespaceUri);
                
                if( prefix==null ) {
                    // if not, try to allocate a new one.
                    
                    // use prefixMapper if specified. If so, just let the 
                    // prefixMapper decide if it wants to use the suggested prefix.
                    // otherwise our best bet is the suggested prefix.
                    if( prefixMapper!=null )
                        prefix = prefixMapper.getPreferredPrefix(
                            namespaceUri,preferedPrefix,requirePrefix);
                    else
                       prefix = preferedPrefix;

                    if( prefix==null )
                        // if the user don't care, generate one
                        prefix = "ns"+(iota++);
                }
            }

            // ASSERT: prefix!=null
            
            if( requirePrefix && prefix.length()==0 )
                // we can't map it to the default prefix. generate one.
                prefix = "ns"+(iota++);
            
            
            while(true) {
                String existingUri = (String)decls.get(prefix);
                
                if( existingUri==null ) {
                    // this prefix is unoccupied. use it
                    decls.put( prefix, namespaceUri );
                    reverseDecls.put( namespaceUri, prefix );
                    return prefix;
                }
                
                if( existingUri.length()==0 ) {
                    // we have to remap the new namespace URI to a different
                    // prefix because the current association of ""->"" cannot
                    // be changed
                    ;
                } else {
                    // the new one takes precedence. this is necessary
                    // because we might first assign "uri1"->"" and then
                    // later find that ""->"" needs to be added.
                    
                    // so change the existing one
                    decls.put( prefix, namespaceUri );
                    reverseDecls.put( namespaceUri, prefix );
                    
                    namespaceUri = existingUri;
                }
                
                // we need to find a new prefix for URI "namespaceUri"
                // generate a machine-made prefix
                prefix = "ns"+(iota++);
                
                // go back to the loop and reassign
            }
        }
    }
    

    public String getPrefix( String namespaceUri ) {
        // even through the method name is "getPrefix", we 
        // use this method to declare prefixes if necessary.
        
        // the only time a prefix is required is when we print
        // attribute names, and in those cases we will call
        // declareNamespace method directly. So it's safe to
        // assume that we don't require a prefix in this case.
        return declareNamespace(namespaceUri,null,false);
    }
    
    /**
     * Obtains the namespace URI currently associated to the given prefix.
     * If no namespace URI is associated, return null.
     */
    public String getNamespaceURI( String prefix ) {
        String uri = (String)decls.get(prefix);
        if(uri!=null)       return uri;
        
        return nss.getURI(prefix);
    }
    
    public Iterator getPrefixes( String namespaceUri ) {
        // not particularly efficient implementation.
        Set s = new HashSet();
        
        String prefix = (String)reverseDecls.get(namespaceUri);
        if(prefix!=null)    s.add(prefix);
        
        if( nss.getURI("").equals(namespaceUri) )
            s.add("");
        
        for( Enumeration e=nss.getPrefixes(namespaceUri); e.hasMoreElements(); )
            s.add(e.nextElement());
        
        return s.iterator();
    }

    /**
     * Sets the current bindings aside and starts a new element context.
     * 
     * This method should be called at the beginning of the startElement method
     * of the Serializer implementation.
     */
    public void startElement() {
        nss.pushContext();
        inCollectingMode = true;
    }
    
    /**
     * Reconciles the namespace URI/prefix mapping requests since the
     * last startElement method invocation and finalizes them.
     * 
     * This method must be called after all the necessary namespace URIs 
     * for this element is reported through the declareNamespace method
     * or the getPrefix method.
     */
    public void endNamespaceDecls() {
        if(!decls.isEmpty()) {
            // most of the times decls is empty, so take advantage of it.
            for( Iterator itr=decls.entrySet().iterator(); itr.hasNext(); ) {
                Map.Entry e = (Map.Entry)itr.next();
                String prefix = (String)e.getKey();
                String uri = (String)e.getValue();
                if(!uri.equals(nss.getURI(prefix))) // avoid redundant decls.
                    nss.declarePrefix( prefix, uri );
            }
            decls.clear();
            reverseDecls.clear();
        }
        inCollectingMode = false;
    }
    
    /**
     * Ends the current element context and gets back to the parent context.
     * 
     * This method should be called at the end of the endElement method
     * of derived classes.
     */
    public void endElement() {
        nss.popContext();
    }

    
    
    /** Iterates all newly declared namespace prefixes for this element. */
    public void iterateDeclaredPrefixes( PrefixCallback callback ) throws SAXException {
        for( Enumeration e=nss.getDeclaredPrefixes(); e.hasMoreElements(); ) {
            String p = (String)e.nextElement();
            String uri = nss.getURI(p);
            
            callback.onPrefixMapping( p, uri );
        }
    }
    
    
}
