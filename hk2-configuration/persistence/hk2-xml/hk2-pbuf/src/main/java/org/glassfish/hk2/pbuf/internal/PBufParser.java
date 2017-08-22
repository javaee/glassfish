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
import java.lang.reflect.Array;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.xml.bind.Unmarshaller.Listener;
import javax.xml.namespace.QName;

import org.glassfish.hk2.api.IterableProvider;
import org.glassfish.hk2.pbuf.api.PBufUtilities;
import org.glassfish.hk2.xml.api.XmlHk2ConfigurationBean;
import org.glassfish.hk2.xml.api.XmlRootHandle;
import org.glassfish.hk2.xml.api.XmlService;
import org.glassfish.hk2.xml.internal.ChildDataModel;
import org.glassfish.hk2.xml.internal.ChildDescriptor;
import org.glassfish.hk2.xml.internal.ChildType;
import org.glassfish.hk2.xml.internal.ModelImpl;
import org.glassfish.hk2.xml.internal.ParentedModel;
import org.glassfish.hk2.xml.jaxb.internal.BaseHK2JAXBBean;
import org.glassfish.hk2.xml.spi.Model;
import org.glassfish.hk2.xml.spi.PreGenerationRequirement;
import org.glassfish.hk2.xml.spi.XmlServiceParser;

import com.google.protobuf.CodedOutputStream;
import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.Descriptors;
import com.google.protobuf.Descriptors.FileDescriptor;
import com.google.protobuf.DynamicMessage;

@Singleton
@Named(PBufUtilities.PBUF_SERVICE_NAME)
public class PBufParser implements XmlServiceParser {
    private final HashMap<Class<?>, Descriptors.Descriptor> allProtos = new HashMap<Class<?>, Descriptors.Descriptor>();
    
    @Inject @Named(PBufUtilities.PBUF_SERVICE_NAME)
    private IterableProvider<XmlService> xmlService;

    /* (non-Javadoc)
     * @see org.glassfish.hk2.xml.spi.XmlServiceParser#parseRoot(org.glassfish.hk2.xml.spi.Model, java.net.URI, javax.xml.bind.Unmarshaller.Listener)
     */
    @Override
    public <T> T parseRoot(Model rootModel, URI location, Listener listener)
            throws Exception {
        InputStream is = location.toURL().openStream();
        try {
            return parseRoot(rootModel, is, listener);
        }
        finally {
            is.close();
        }
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.xml.spi.XmlServiceParser#parseRoot(org.glassfish.hk2.xml.spi.Model, java.io.InputStream, javax.xml.bind.Unmarshaller.Listener)
     */
    @Override
    public <T> T parseRoot(Model rootModel, InputStream input,
            Listener listener) throws Exception {
        try {
            List<Descriptors.FileDescriptor> protoFiles = new LinkedList<Descriptors.FileDescriptor>();
            convertAllModels((ModelImpl) rootModel, protoFiles);
        }
        catch (IOException ioe) {
            throw ioe;
        }
        catch (Exception e) {
            throw new IOException(e);
        }
        
        DynamicMessage message = internalUnmarshal((ModelImpl) rootModel, input);
        
        XmlHk2ConfigurationBean retVal = parseDynamicMessage((ModelImpl) rootModel,
                null,
                message,
                listener);
        
        return (T) retVal;
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
        
        try {
          List<Descriptors.FileDescriptor> protoFiles = new LinkedList<Descriptors.FileDescriptor>();
          convertAllModels(model, protoFiles);
        }
        catch (IOException ioe) {
            throw ioe;
        }
        catch (Exception e) {
            throw new IOException(e);
        }
        
        DynamicMessage dynamicMessage = internalMarshal(rootBean);
        CodedOutputStream cos = CodedOutputStream.newInstance(outputStream);
        
        try {
          dynamicMessage.writeTo(cos);
        }
        finally {
            cos.flush();
        }
    }
    
    private XmlHk2ConfigurationBean parseDynamicMessage(ModelImpl model,
            XmlHk2ConfigurationBean parent,
            DynamicMessage message,
            Listener listener) throws IOException {
        BaseHK2JAXBBean bean = (BaseHK2JAXBBean) xmlService.get().createBean(model.getOriginalInterfaceAsClass());
        
        Descriptors.Descriptor descriptor = message.getDescriptorForType();
        
        listener.beforeUnmarshal(bean, parent);
        
        for(Map.Entry<QName, ChildDescriptor> entry : model.getAllChildrenDescriptors().entrySet()) {
            QName qname = entry.getKey();
            ChildDescriptor childDescriptor = entry.getValue();
            
            String localPart = qname.getLocalPart();
            Descriptors.FieldDescriptor fieldDescriptor = descriptor.findFieldByName(localPart);
            if (fieldDescriptor == null) {
                throw new IOException("Unknown field " + localPart + " in " + bean);
            }
            
            Object value = message.getField(fieldDescriptor);
            if (value == null) continue;
            
            ChildDataModel childDataModel = childDescriptor.getChildDataModel();
            if (childDataModel != null) {
                bean._setProperty(qname, value);
            }
            else {
                ParentedModel parentedNode = childDescriptor.getParentedModel();
                
                DynamicMessage dynamicChild = null;
                XmlHk2ConfigurationBean child = null;
                int repeatedFieldCount = 0;
                
                switch (parentedNode.getChildType()) {
                case DIRECT:
                    if (!(value instanceof DynamicMessage)) {
                        throw new AssertionError("Do not know how to handle a non-dynamic direct message " + value);
                    }
                    dynamicChild = (DynamicMessage) value;
                    
                    child = parseDynamicMessage(parentedNode.getChildModel(),
                            bean,
                            dynamicChild,
                            listener);
                    
                    bean._setProperty(qname, child);
                    break;
                case LIST:
                    repeatedFieldCount = message.getRepeatedFieldCount(fieldDescriptor);
                    ArrayList<XmlHk2ConfigurationBean> list = new ArrayList<XmlHk2ConfigurationBean>(repeatedFieldCount);
                    
                    for (int lcv = 0; lcv < repeatedFieldCount; lcv++) {
                        Object childBean = message.getRepeatedField(fieldDescriptor, lcv);
                        if (!(childBean instanceof DynamicMessage)) {
                            throw new AssertionError("Do not know how to handle a non-dynamic list message " + childBean);
                        }
                        dynamicChild = (DynamicMessage) childBean;
                        
                        child = parseDynamicMessage(parentedNode.getChildModel(),
                                bean,
                                dynamicChild,
                                listener);
                        
                        list.add(child);
                    }
                    
                    bean._setProperty(qname, list);
                    break;
                case ARRAY:
                    ModelImpl childModel = parentedNode.getChildModel();
                    repeatedFieldCount = message.getRepeatedFieldCount(fieldDescriptor);
                    Object array = Array.newInstance(childModel.getOriginalInterfaceAsClass(), repeatedFieldCount);
                    
                    for (int lcv = 0; lcv < repeatedFieldCount; lcv++) {
                        Object childBean = message.getRepeatedField(fieldDescriptor, lcv);
                        if (!(childBean instanceof DynamicMessage)) {
                            throw new AssertionError("Do not know how to handle a non-dynamic array message " + childBean);
                        }
                        dynamicChild = (DynamicMessage) childBean;
                        
                        child = parseDynamicMessage(parentedNode.getChildModel(),
                                bean,
                                dynamicChild,
                                listener);
                        
                        Array.set(array, lcv, child);
                    }
                    
                    bean._setProperty(qname, array);
                    break;
                default:
                    throw new IOException("Unknown child type: " + parentedNode.getChildType());
                }
            }
        }
        
        listener.afterUnmarshal(bean, parent);
        
        return bean;
    }
    
    private synchronized DynamicMessage internalUnmarshal(ModelImpl model, InputStream is) throws Exception {
        Class<?> originalAsClass = model.getOriginalInterfaceAsClass();
        String originalInterface = model.getOriginalInterface();
        String protoName = getSimpleName(originalInterface);
        
        Descriptors.Descriptor descriptor = allProtos.get(originalAsClass);
        if (descriptor == null) {
            throw new IOException("Unknown model: " + originalInterface + " with protoName=" + protoName);
        }
        
        DynamicMessage retVal = DynamicMessage.parseFrom(descriptor, is);
        return retVal;
    }
    
    private synchronized <T>  DynamicMessage internalMarshal(XmlHk2ConfigurationBean bean) throws IOException {
        Map<String, Object> blm = bean._getBeanLikeMap();
        ModelImpl model = bean._getModel();
        
        Class<?> originalAsClass = model.getOriginalInterfaceAsClass();
        String originalInterface = model.getOriginalInterface();
        String protoName = getSimpleName(originalInterface);
        
        Descriptors.Descriptor descriptor = allProtos.get(originalAsClass);
        if (descriptor == null) {
            throw new IOException("Unknown model: " + originalInterface + " with protoName=" + protoName);
        }
        
        DynamicMessage.Builder retValBuilder = DynamicMessage.newBuilder(descriptor);
        
        for (Map.Entry<QName, ChildDescriptor> allEntry : model.getAllChildrenDescriptors().entrySet()) {
            QName qname = allEntry.getKey();
            ChildDescriptor childDescriptor = allEntry.getValue();
            
            String localPart = qname.getLocalPart();
            Descriptors.FieldDescriptor fieldDescriptor = descriptor.findFieldByName(localPart);
            if (fieldDescriptor == null) {
                throw new IOException("Unknown field " + localPart + " in " + bean);
            }
            
            ChildDataModel childDataModel = childDescriptor.getChildDataModel();
            if (childDataModel != null) {
              Object value = blm.get(localPart);
              retValBuilder.setField(fieldDescriptor, value);
            }
            else {
                ParentedModel parentedModel = childDescriptor.getParentedModel();
                
                switch(parentedModel.getChildType()) {
                case DIRECT:
                    Object directValue = blm.get(localPart);
                    if (directValue != null) {
                        DynamicMessage subMessage = internalMarshal((XmlHk2ConfigurationBean) directValue);
                        
                        retValBuilder.setField(fieldDescriptor, subMessage);
                    }
                    break;
                case LIST:
                    Object listValue = blm.get(localPart);
                    if (listValue != null) {
                        List<XmlHk2ConfigurationBean> asList = (List<XmlHk2ConfigurationBean>) listValue;
                        
                        for (XmlHk2ConfigurationBean child : asList) {
                            DynamicMessage subMessage = internalMarshal(child);
                            
                            retValBuilder.addRepeatedField(fieldDescriptor, subMessage);
                        }
                    }
                    break;
                case ARRAY:
                    Object arrayValue = blm.get(localPart);
                    if (arrayValue != null) {
                        int count = Array.getLength(arrayValue);
                        
                        for (int lcv = 0; lcv < count; lcv++) {
                            XmlHk2ConfigurationBean child = (XmlHk2ConfigurationBean) Array.get(arrayValue, lcv);
                            
                            DynamicMessage subMessage = internalMarshal(child);
                            
                            retValBuilder.addRepeatedField(fieldDescriptor, subMessage);
                        }
                    }
                    break;
                default:
                    throw new AssertionError("Unknown child type: " + parentedModel.getChildType());
                    
                }
                
            }
            
        }
        
        return retValBuilder.build();
    }
    
    private synchronized void convertAllModels(ModelImpl model, List<Descriptors.FileDescriptor> protoFiles) throws Exception {
        Class<?> modelClass = model.getOriginalInterfaceAsClass();
        if (allProtos.containsKey(modelClass)) return;
        
        for (ParentedModel pModel : model.getAllChildren()) {
            convertAllModels(pModel.getChildModel(), protoFiles);
        }
        
        if (allProtos.containsKey(modelClass)) return;
        
        Descriptors.Descriptor converted = convertModelToDescriptor(model, protoFiles);
        
        protoFiles.add(converted.getFile());
        
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
        if (childClass.equals(long.class) || childClass.equals(Long.class)) {
            return DescriptorProtos.FieldDescriptorProto.Type.TYPE_INT64;
        }
        
        throw new AssertionError("Unknown type to convert " + childClass.getName());
    }
    
    private static String getSimpleName(String dotDelimitedName) {
        int index = dotDelimitedName.lastIndexOf('.');
        if (index < 0) return dotDelimitedName;
        
        return dotDelimitedName.substring(index + 1);
    }
    
    private static String getProtoNameFromModel(ModelImpl mi) {
        String originalInterface = mi.getOriginalInterface();
        String protoName = getSimpleName(originalInterface);
        return protoName;
    }
    
    private static Descriptors.Descriptor convertModelToDescriptor(ModelImpl model, List<Descriptors.FileDescriptor> knownFiles) throws Exception {
        Map<QName, ChildDescriptor> allChildren = model.getAllChildrenDescriptors();
        
        String protoName = getProtoNameFromModel(model);
        
        DescriptorProtos.DescriptorProto.Builder builder = DescriptorProtos.DescriptorProto.newBuilder();
        builder.setName(protoName);
        
        int number = 1;
        for(Map.Entry<QName, ChildDescriptor> entry : allChildren.entrySet()) {
            String localPart = entry.getKey().getLocalPart();
            ChildDescriptor childDescriptor = entry.getValue();
            
            DescriptorProtos.FieldDescriptorProto.Builder fBuilder =
                    DescriptorProtos.FieldDescriptorProto.newBuilder().setName(localPart);
            fBuilder.setNumber(number);
            number++;
            
            ChildDataModel dataModel = childDescriptor.getChildDataModel();
            if (dataModel != null) {
              if (dataModel.getDefaultAsString() != null) {
                  fBuilder.setDefaultValue(dataModel.getDefaultAsString());
              }
            
              DescriptorProtos.FieldDescriptorProto.Type fieldType = convertChildDataModelToType(dataModel);
              fBuilder.setType(fieldType);
            }
            else {
                ParentedModel pm = childDescriptor.getParentedModel();
                
                ModelImpl childModel = pm.getChildModel();
                String childTypeName = getProtoNameFromModel(childModel);
                
                fBuilder.setTypeName(childTypeName);
                
                ChildType childType = pm.getChildType();
                if (childType.equals(ChildType.ARRAY) || childType.equals(ChildType.LIST)) {
                    fBuilder.setLabel(DescriptorProtos.FieldDescriptorProto.Label.LABEL_REPEATED);
                }
            }
            
            builder.addField(fBuilder.build());
        }
        
        DescriptorProtos.DescriptorProto proto = builder.build();
        
        DescriptorProtos.FileDescriptorProto.Builder fileBuilder = DescriptorProtos.FileDescriptorProto.newBuilder();
        fileBuilder.addMessageType(proto);
        
        DescriptorProtos.FileDescriptorProto fProto = fileBuilder.build();
        
        Descriptors.FileDescriptor fDesc = Descriptors.FileDescriptor.buildFrom(fProto,
                knownFiles.toArray(new Descriptors.FileDescriptor[knownFiles.size()]));
        
        Descriptors.Descriptor fD = fDesc.findMessageTypeByName(protoName);
        
        return fD;
    }
}
