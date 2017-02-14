/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.hk2.json.internal;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.json.Json;
import javax.xml.bind.Unmarshaller.Listener;

import org.glassfish.hk2.api.Rank;
import org.glassfish.hk2.json.api.JsonUtilities;
import org.glassfish.hk2.xml.api.XmlRootHandle;
import org.glassfish.hk2.xml.api.XmlService;
import org.glassfish.hk2.xml.internal.ChildDataModel;
import org.glassfish.hk2.xml.internal.ChildDescriptor;
import org.glassfish.hk2.xml.internal.ChildType;
import org.glassfish.hk2.xml.internal.ModelImpl;
import org.glassfish.hk2.xml.internal.ParentedModel;
import org.glassfish.hk2.xml.internal.Utilities;
import org.glassfish.hk2.xml.jaxb.internal.BaseHK2JAXBBean;
import org.glassfish.hk2.xml.spi.Model;
import org.glassfish.hk2.xml.spi.PreGenerationRequirement;
import org.glassfish.hk2.xml.spi.XmlServiceParser;

/**
 * @author jwells
 *
 */
@Singleton
@Named(JsonUtilities.JSON_SERVICE_NAME)
@Rank(-1)
public class JsonParser implements XmlServiceParser {
    private void skipper(javax.json.stream.JsonParser parser) {
        if (!parser.hasNext()) return;
        
        for(;;) {
            javax.json.stream.JsonParser.Event event = parser.next();
            switch (event) {
            case KEY_NAME:
            case VALUE_FALSE:
            case VALUE_TRUE:
            case VALUE_NULL:
            case VALUE_STRING:
            case VALUE_NUMBER:
                // Skipping done
                break;
            case START_OBJECT:
            case START_ARRAY:
                skipper(parser);
                break;
            case END_OBJECT:
            case END_ARRAY:
                return;
            }
        }
    }
    
    @Inject @Named(JsonUtilities.JSON_SERVICE_NAME)
    private Provider<XmlService> xmlService;
    
    private void parseObject(ModelImpl currentModel, BaseHK2JAXBBean target, BaseHK2JAXBBean parent, Listener listener,
            javax.json.stream.JsonParser parser) {
        try {
            listener.beforeUnmarshal(target, parent);
        }
        catch (RuntimeException re) {
            throw re;
        }
        catch (Throwable th) {
            // TODO: Log
            throw new RuntimeException(th);
        }
        
        if (!parser.hasNext()) {
            throw new IllegalStateException("Expectin an end token from Json parser");
        }
        
        boolean getNextEvent = true;
        do {
            javax.json.stream.JsonParser.Event event = parser.next();
            switch(event) {
            case END_OBJECT:
                try {
                    listener.afterUnmarshal(target, parent);
                }
                catch (RuntimeException re) {
                    throw re;
                }
                catch (Throwable th) {
                    // TODO: Log
                    throw new RuntimeException(th);
                }
                
                getNextEvent = false;
                break;
            case KEY_NAME:
                String keyName = parser.getString();
                ChildDescriptor descriptor = currentModel.getChildDescriptor(keyName);
                if (descriptor == null) {
                    skipper(parser);
                }
                else {
                    ParentedModel parentedModel = descriptor.getParentedModel();
                    if (parentedModel == null) {
                        ChildDataModel childDataModel = descriptor.getChildDataModel();
                    
                        javax.json.stream.JsonParser.Event attributeEvent = parser.next();
                        switch (attributeEvent) {
                        case VALUE_STRING:
                            target._setProperty(keyName, parser.getString());
                            break;
                        case VALUE_NUMBER:
                            if (parser.isIntegralNumber()) {
                                target._setProperty(keyName, new Integer(parser.getInt()));
                            }
                            else {
                                target._setProperty(keyName, new Long(parser.getLong()));
                            }
                            break;
                        case VALUE_NULL:
                            target._setProperty(keyName, null);
                            break;
                        case VALUE_TRUE:
                            target._setProperty(keyName, Boolean.TRUE);
                            break;
                        case VALUE_FALSE:
                            target._setProperty(keyName, Boolean.FALSE);
                            break;
                        default:
                            throw new IllegalStateException("Uknown value type: " + attributeEvent + " for " + childDataModel + " for " + currentModel);
                        }
                    }
                    else {
                        ModelImpl childModel = parentedModel.getChildModel();
                    
                        javax.json.stream.JsonParser.Event childTypeEvent = parser.next();
                    
                        if (javax.json.stream.JsonParser.Event.START_ARRAY.equals(childTypeEvent)) {
                            List<BaseHK2JAXBBean> myList = new LinkedList<BaseHK2JAXBBean>();
                        
                            for (;;) {
                                javax.json.stream.JsonParser.Event arrayEvent = parser.next();
                                if (javax.json.stream.JsonParser.Event.END_ARRAY.equals(arrayEvent)) {
                                    // Finished loop!
                                    break;
                                }
                            
                                if (!javax.json.stream.JsonParser.Event.START_OBJECT.equals(arrayEvent)) {
                                    throw new AssertionError("Do not know how to handle this case inside an array expecting an object" + arrayEvent);
                                }
                            
                                BaseHK2JAXBBean oneChild = Utilities.createBean(childModel.getProxyAsClass());
                            
                                parseObject(childModel, oneChild, target, listener, parser);
                            
                                myList.add(oneChild);
                            }
                        
                            if (ChildType.LIST.equals(parentedModel.getChildType())) {
                                target._setProperty(keyName, myList);
                            }
                            else if (ChildType.ARRAY.equals(parentedModel.getChildType())) {
                                Object array = Array.newInstance(childModel.getOriginalInterfaceAsClass(), myList.size());
                            
                                int lcv = 0;
                                for (BaseHK2JAXBBean bean : myList) {
                                    Array.set(array, lcv, bean);
                                    lcv++;
                                }
                            
                                target._setProperty(keyName, array);
                            }
                            else {
                                throw new AssertionError("The model says DIRECT but I got an ARRAY start so bombing quite badly");
                            }
                        
                        }
                        else if (javax.json.stream.JsonParser.Event.START_OBJECT.equals(childTypeEvent)) {
                            if (!ChildType.DIRECT.equals(parentedModel.getChildType())) {
                                throw new AssertionError("The model says " + parentedModel.getChildType() + " but I got an START_OBJECT start so bombing quite badly");
                            }
                        
                            BaseHK2JAXBBean oneChild = Utilities.createBean(childModel.getProxyAsClass());
                        
                            parseObject(childModel, oneChild, target, listener, parser);
                        
                            target._setProperty(keyName, oneChild);
                        }
                        else {
                            throw new IllegalStateException("Unknown start of child event: " + event);
                        }
                    }
                }
                break;
            default:
                throw new IllegalArgumentException("Unkown event: " + event);
            }
        }
        while (getNextEvent);
         
        
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.xml.spi.XmlServiceParser#parseRoot(org.glassfish.hk2.xml.spi.Model, java.net.URI, javax.xml.bind.Unmarshaller.Listener)
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> T parseRoot(Model rootModel, URI location, Listener listener)
            throws Exception {
        javax.json.stream.JsonParser parser = Json.createParser(location.toURL().openStream());
        
        try {
            if (!parser.hasNext()) {
                T root = (T) Utilities.createBean(rootModel.getProxyAsClass());
                
                listener.beforeUnmarshal(root, null);
                listener.afterUnmarshal(root, null);
                
                return root;
            }
            
            javax.json.stream.JsonParser.Event event = parser.next();
            if (!javax.json.stream.JsonParser.Event.START_OBJECT.equals(event)) {
                throw new AssertionError("Unknown start of JSON object: " + event);
            }
            
            BaseHK2JAXBBean root = Utilities.createBean(rootModel.getProxyAsClass());
            
            parseObject((ModelImpl) rootModel, root, null, listener, parser);
            
            return (T) root;
        }
        finally {
            parser.close();
        }
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.xml.spi.XmlServiceParser#getPreGenerationRequirement()
     */
    @Override
    public PreGenerationRequirement getPreGenerationRequirement() {
        return PreGenerationRequirement.LAZY_PREGENERATION;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.xml.spi.XmlServiceParser#marshall(java.io.OutputStream, org.glassfish.hk2.xml.api.XmlRootHandle)
     */
    @Override
    public <T> void marshal(OutputStream outputStream, XmlRootHandle<T> root)
            throws IOException {
        throw new AssertionError("marshal not yet implemented");
    }

}
