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
package org.glassfish.hk2.pbuf.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.xml.bind.Unmarshaller.Listener;
import javax.xml.namespace.QName;

import org.glassfish.hk2.pbuf.api.PBufUtilities;
import org.glassfish.hk2.xml.api.XmlHk2ConfigurationBean;
import org.glassfish.hk2.xml.api.XmlRootHandle;
import org.glassfish.hk2.xml.internal.ChildDataModel;
import org.glassfish.hk2.xml.internal.ModelImpl;
import org.glassfish.hk2.xml.internal.ParentedModel;
import org.glassfish.hk2.xml.spi.Model;
import org.glassfish.hk2.xml.spi.PreGenerationRequirement;
import org.glassfish.hk2.xml.spi.XmlServiceParser;

import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.Descriptors;

@Singleton
@Named(PBufUtilities.PBUF_SERVICE_NAME)
public class PBufParser implements XmlServiceParser {
    private final HashMap<Class<?>, Descriptors.Descriptor> allProtos = new HashMap<Class<?>, Descriptors.Descriptor>();

    /* (non-Javadoc)
     * @see org.glassfish.hk2.xml.spi.XmlServiceParser#parseRoot(org.glassfish.hk2.xml.spi.Model, java.net.URI, javax.xml.bind.Unmarshaller.Listener)
     */
    @Override
    public <T> T parseRoot(Model rootModel, URI location, Listener listener)
            throws Exception {
        throw new AssertionError("not yet implemented");
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.xml.spi.XmlServiceParser#parseRoot(org.glassfish.hk2.xml.spi.Model, java.io.InputStream, javax.xml.bind.Unmarshaller.Listener)
     */
    @Override
    public <T> T parseRoot(Model rootModel, InputStream input,
            Listener listener) throws Exception {
        throw new AssertionError("not yet implemented");
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.xml.spi.XmlServiceParser#getPreGenerationRequirement()
     */
    @Override
    public PreGenerationRequirement getPreGenerationRequirement() {
        return PreGenerationRequirement.MUST_PREGENERATE;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.xml.spi.XmlServiceParser#marshal(java.io.OutputStream, org.glassfish.hk2.xml.api.XmlRootHandle)
     */
    @Override
    public <T> void marshal(OutputStream outputStream, XmlRootHandle<T> root)
            throws IOException {
        T rootObject = root.getRoot();
        if (rootObject == null) return;
        
        XmlHk2ConfigurationBean rootBean = (XmlHk2ConfigurationBean) rootObject;
        ModelImpl model = rootBean._getModel();
        
        convertAllModels(model);
        
        throw new AssertionError("not yet implemented");
    }
    
    private synchronized void convertAllModels(ModelImpl model) {
        Class<?> modelClass = model.getOriginalInterfaceAsClass();
        if (allProtos.containsKey(modelClass)) return;
        
        for (ParentedModel pModel : model.getAllChildren()) {
            convertAllModels(pModel.getChildModel());
        }
        
        if (allProtos.containsKey(modelClass)) return;
        
        Descriptors.Descriptor converted = convertModelToDescriptor(model);
        
        allProtos.put(modelClass, converted);
    }
    
    private static DescriptorProtos.FieldDescriptorProto.Type convertChildDataModelToType(ChildDataModel cdm) {
        Class<?> childClass = cdm.getChildTypeAsClass();
        
        if (childClass.equals(String.class)) {
            return DescriptorProtos.FieldDescriptorProto.Type.TYPE_STRING;
        }
        if (childClass.equals(int.class) || childClass.equals(Integer.class)) {
            return DescriptorProtos.FieldDescriptorProto.Type.TYPE_INT32;
        }
        
        throw new AssertionError("Unknown type to convert " + childClass.getName());
    }
    
    private static Descriptors.Descriptor convertModelToDescriptor(ModelImpl model) {
        Map<QName, ChildDataModel> nonChildren = model.getNonChildProperties();
        
        DescriptorProtos.DescriptorProto.Builder builder = DescriptorProtos.DescriptorProto.newBuilder();
        for(Map.Entry<QName, ChildDataModel> entry : nonChildren.entrySet()) {
            QName qname = entry.getKey();
            ChildDataModel dataModel = entry.getValue();
            
            String localPart = entry.getKey().getLocalPart();
            
            DescriptorProtos.FieldDescriptorProto.Builder fBuilder =
                    DescriptorProtos.FieldDescriptorProto.newBuilder().setName(localPart);
            
            if (entry.getValue().getDefaultAsString() != null) {
                fBuilder.setDefaultValue(entry.getValue().getDefaultAsString());
            }
            
            DescriptorProtos.FieldDescriptorProto.Type fieldType = convertChildDataModelToType(dataModel);
            fBuilder.setType(fieldType);
            
            builder.addField(fBuilder.build());
        }
        
        DescriptorProtos.DescriptorProto proto = builder.build();
        
        DescriptorProtos.FileDescriptorProto.Builder fileBuilder = DescriptorProtos.FileDescriptorProto.newBuilder();
        fileBuilder.addMessageType(proto);
        
        DescriptorProtos.FileDescriptorProto fProto = fileBuilder.build();
        
        System.out.println("JRW(10) froto=\n" + fProto.toString());
        
        throw new AssertionError("not yet implemented");
        
    }
}
