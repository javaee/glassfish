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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.xml.bind.Unmarshaller.Listener;
import javax.xml.namespace.QName;

import org.glassfish.hk2.api.IterableProvider;
import org.glassfish.hk2.pbuf.api.PBufUtilities;
import org.glassfish.hk2.utilities.general.GeneralUtilities;
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

import com.google.protobuf.ByteString;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;
import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;

@Singleton
@Named(PBufUtilities.PBUF_SERVICE_NAME)
public class PBufParser implements XmlServiceParser {
    private final HashMap<Class<?>, Descriptors.Descriptor> allProtos = new HashMap<Class<?>, Descriptors.Descriptor>();
    private final Object marshalLock = new Object();
    private final Object unmarshalLock = new Object();
    
    private final WeakHashMap<OutputStream, CodedOutputStream> cosCache = new WeakHashMap<OutputStream, CodedOutputStream>();
    private final WeakHashMap<InputStream, CodedInputStream> cisCache = new WeakHashMap<InputStream, CodedInputStream>();
    
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
    @SuppressWarnings("unchecked")
    @Override
    public <T> T parseRoot(Model rootModel, InputStream input,
            Listener listener) throws Exception {
        try {
            Set<Descriptors.FileDescriptor> protoFiles = new HashSet<Descriptors.FileDescriptor>();
            convertAllModels((ModelImpl) rootModel, protoFiles);
        }
        catch (IOException ioe) {
            throw ioe;
        }
        catch (Exception e) {
            throw new IOException(e);
        }
        
        synchronized (unmarshalLock) {
            CodedInputStream cis = cisCache.get(input);
            if (cis == null) {
                cis = CodedInputStream.newInstance(input);
                cisCache.put(input, cis);
            }
            
            int size = cis.readInt32();
            if (size <= 0) {
                throw new AssertionError("Invalid size of protocol buffer on the wire: " + size);
            }
            
            byte[] rawBytes = cis.readRawBytes(size);
            
            DynamicMessage message = internalUnmarshal((ModelImpl) rootModel, rawBytes);
        
            XmlHk2ConfigurationBean retVal = parseDynamicMessage((ModelImpl) rootModel,
                null,
                message,
                listener);
        
            return (T) retVal;
        }
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
          Set<Descriptors.FileDescriptor> protoFiles = new HashSet<Descriptors.FileDescriptor>();
          convertAllModels(model, protoFiles);
        }
        catch (IOException ioe) {
            throw ioe;
        }
        catch (Exception e) {
            throw new IOException(e);
        }
        
        synchronized (marshalLock) {
            DynamicMessage dynamicMessage = internalMarshal(rootBean);
            int size = dynamicMessage.getSerializedSize();
            
            CodedOutputStream cos = cosCache.get(outputStream);
            if (cos == null) {
                cos = CodedOutputStream.newInstance(outputStream);
                cosCache.put(outputStream, cos);
            }
            
            try {
              cos.writeInt32NoTag(size);
              dynamicMessage.writeTo(cos);
            }
            finally {
                cos.flush();
            }
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
            String protoPart = PBUtilities.camelCaseToUnderscore(localPart);
            
            Descriptors.FieldDescriptor fieldDescriptor = descriptor.findFieldByName(protoPart);
            if (fieldDescriptor == null) {
                throw new IOException("Unknown field " + protoPart + " in " + bean);
            }
            
            ChildDataModel childDataModel = childDescriptor.getChildDataModel();
            if (childDataModel != null) {
                boolean fieldSet = message.hasField(fieldDescriptor);
                if (!fieldSet) {
                    continue;
                }
                
                Object value = message.getField(fieldDescriptor);
                
                value = convertFieldForUnmarshal(value, childDataModel);
                
                bean._setProperty(qname, value);
            }
            else {
                ParentedModel parentedNode = childDescriptor.getParentedModel();
                
                if (ChildType.DIRECT.equals(parentedNode.getChildType())) {
                    // Cannot call hasField on repeated fields in pbuf
                    boolean fieldSet = message.hasField(fieldDescriptor);
                    if (!fieldSet) {
                        continue;
                    }
                }
                
                Object value = message.getField(fieldDescriptor);
                if (value == null) {
                    continue;
                }
                
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
    
    private DynamicMessage internalUnmarshal(ModelImpl model, byte[] bytes) throws Exception {
        Class<?> originalAsClass = model.getOriginalInterfaceAsClass();
        String originalInterface = model.getOriginalInterface();
        String protoName = getSimpleName(originalInterface);
        
        Descriptors.Descriptor descriptor;
        synchronized (allProtos) {
            descriptor = allProtos.get(originalAsClass);
        }
        if (descriptor == null) {
            throw new IOException("Unknown model: " + originalInterface + " with protoName=" + protoName);
        }
        
        DynamicMessage retVal = DynamicMessage.parseFrom(descriptor, bytes);
        return retVal;
    }
    
    
    
    @SuppressWarnings("unchecked")
    private <T>  DynamicMessage internalMarshal(XmlHk2ConfigurationBean bean) throws IOException {
        Map<String, Object> blm = bean._getBeanLikeMap();
        ModelImpl model = bean._getModel();
        
        Class<?> originalAsClass = model.getOriginalInterfaceAsClass();
        String originalInterface = model.getOriginalInterface();
        String protoName = getSimpleName(originalInterface);
        
        Descriptors.Descriptor descriptor;
        synchronized (allProtos) {
            descriptor = allProtos.get(originalAsClass);
        }
        
        if (descriptor == null) {
            throw new IOException("Unknown model: " + originalInterface + " with protoName=" + protoName);
        }
        
        DynamicMessage.Builder retValBuilder = DynamicMessage.newBuilder(descriptor);
        
        for (Map.Entry<QName, ChildDescriptor> allEntry : model.getAllChildrenDescriptors().entrySet()) {
            QName qname = allEntry.getKey();
            ChildDescriptor childDescriptor = allEntry.getValue();
            
            String localPart = qname.getLocalPart();
            String protoPart = PBUtilities.camelCaseToUnderscore(localPart);
            
            Descriptors.FieldDescriptor fieldDescriptor = descriptor.findFieldByName(protoPart);
            if (fieldDescriptor == null) {
                throw new IOException("Unknown field " + protoPart + " in " + bean);
            }
            
            ChildDataModel childDataModel = childDescriptor.getChildDataModel();
            if (childDataModel != null) {
                if (!bean._isSet(localPart)) {
                    continue;
                }
                
                Object value = blm.get(localPart);
                Class<?> childType = childDataModel.getChildTypeAsClass();
                Object convertedValue = convertFieldForMarshal(value, childType);
              
                if (convertedValue != null) {
                    retValBuilder.setField(fieldDescriptor, convertedValue);
                }
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
                        
                        for (XmlHk2ConfigurationBean childBean : asList) {
                            DynamicMessage subMessage = internalMarshal(childBean);
                            
                            retValBuilder.addRepeatedField(fieldDescriptor, subMessage);
                        }
                    }
                    break;
                case ARRAY:
                    Object arrayValue = blm.get(localPart);
                    if (arrayValue != null) {
                        int count = Array.getLength(arrayValue);
                        
                        if (count <= 0) {
                            continue;
                        }
                        
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
    
    private void convertAllModels(ModelImpl model, Set<Descriptors.FileDescriptor> protoFiles) throws Exception {
        synchronized (allProtos) {
            Class<?> modelClass = model.getOriginalInterfaceAsClass();
            Descriptors.Descriptor dd = allProtos.get(modelClass);
            if (dd != null) {
                protoFiles.add(dd.getFile());
                return;
            }
        
            for (ParentedModel pModel : model.getAllChildren()) {
                convertAllModels(pModel.getChildModel(), protoFiles);
            }
        
            dd = allProtos.get(modelClass);
            if (dd != null) {
                protoFiles.add(dd.getFile());
                return;
            }
        
            Descriptors.Descriptor converted = convertModelToDescriptor(model, protoFiles);
        
            protoFiles.add(converted.getFile());
        
            allProtos.put(modelClass, converted);
        }
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
        if (childClass.equals(boolean.class) || childClass.equals(Boolean.class)) {
            return DescriptorProtos.FieldDescriptorProto.Type.TYPE_BOOL;
        }
        if (childClass.equals(double.class) || childClass.equals(Double.class)) {
            return DescriptorProtos.FieldDescriptorProto.Type.TYPE_DOUBLE;
        }
        if (childClass.equals(float.class) || childClass.equals(Float.class)) {
            return DescriptorProtos.FieldDescriptorProto.Type.TYPE_FLOAT;
        }
        if (childClass.equals(byte.class) || childClass.equals(Byte.class)) {
            return DescriptorProtos.FieldDescriptorProto.Type.TYPE_BYTES;
        }
        if (childClass.equals(char.class) || childClass.equals(Character.class)) {
            return DescriptorProtos.FieldDescriptorProto.Type.TYPE_STRING;
        }
        if (childClass.equals(short.class) || childClass.equals(Short.class)) {
            return DescriptorProtos.FieldDescriptorProto.Type.TYPE_INT32;
        }
        
        throw new AssertionError("Unknown type to convert " + childClass.getName());
    }
    
    private static String getSimpleName(String dotDelimitedName) {
        int index = dotDelimitedName.lastIndexOf('.');
        if (index < 0) return dotDelimitedName;
        
        return dotDelimitedName.substring(index + 1);
    }
    
    private static String getPackageName(String dotDelimitedName) {
        int index = dotDelimitedName.lastIndexOf('.');
        if (index < 0) return null;
        
        return dotDelimitedName.substring(0, index);
    }
    
    private static String getProtoNameFromModel(ModelImpl mi) {
        String originalInterface = mi.getOriginalInterface();
        String protoName = getSimpleName(originalInterface);
        return protoName;
    }
    
    private static String getPackageNameFromModel(ModelImpl mi) {
        String originalInterface = mi.getOriginalInterface();
        String packageName = getPackageName(originalInterface);
        return packageName;
    }
    
    private static Descriptors.Descriptor convertModelToDescriptor(ModelImpl model, Set<Descriptors.FileDescriptor> knownFiles) throws Exception {
        Map<QName, ChildDescriptor> allChildren = model.getAllChildrenDescriptors();
        
        String protoName = getProtoNameFromModel(model);
        String packageName = getPackageNameFromModel(model);
        
        DescriptorProtos.DescriptorProto.Builder builder = DescriptorProtos.DescriptorProto.newBuilder();
        builder.setName(protoName);
        
        Class<?> originalInterface = model.getOriginalInterfaceAsClass();
        
        int oneOfNumber = 0;
        Map<String, Integer> oneOfToIndexMap = new HashMap<String, Integer>();
        String currentOneOf = null;
        
        int number = 1;
        for(Map.Entry<QName, ChildDescriptor> entry : allChildren.entrySet()) {
            QName entryKey = entry.getKey();
            String localPart = entryKey.getLocalPart();
            ChildDescriptor childDescriptor = entry.getValue();
            
            String protoPart = PBUtilities.camelCaseToUnderscore(localPart);
            
            DescriptorProtos.FieldDescriptorProto.Builder fBuilder =
                    DescriptorProtos.FieldDescriptorProto.newBuilder().setName(protoPart);
            fBuilder.setNumber(number);
            number++;
            
            ChildDataModel dataModel = childDescriptor.getChildDataModel();
            if (dataModel != null) {
                fBuilder.setLabel(DescriptorProtos.FieldDescriptorProto.Label.LABEL_OPTIONAL);
                
                Class<?> dataType = dataModel.getChildTypeAsClass();
                String originalMethodName = dataModel.getOriginalMethodName();
                
                String oneOfValue = PBUtilities.getOneOf(originalInterface, originalMethodName, dataType);
                if (GeneralUtilities.safeEquals(oneOfValue, currentOneOf)) {
                    if (oneOfValue != null) {
                        int oneOfDeclIndex = oneOfToIndexMap.get(oneOfValue);
                        
                        fBuilder.setOneofIndex(oneOfDeclIndex);
                    }
                }
                else {
                    if (oneOfValue != null) {
                        DescriptorProtos.OneofDescriptorProto.Builder oneOfBuilder = DescriptorProtos.OneofDescriptorProto.newBuilder();
                        oneOfBuilder.setName(oneOfValue);
                        
                        int oneOfIndex = oneOfNumber++;
                        builder.addOneofDecl(oneOfIndex, oneOfBuilder.build());
                        
                        oneOfToIndexMap.put(oneOfValue, oneOfIndex);

                        fBuilder.setOneofIndex(oneOfIndex);
                    }
                    
                    currentOneOf = oneOfValue;
                }
                
                if (dataModel.getDefaultAsString() != null) {
                    fBuilder.setDefaultValue(dataModel.getDefaultAsString());
                }
            
                DescriptorProtos.FieldDescriptorProto.Type fieldType = convertChildDataModelToType(dataModel);
                fBuilder.setType(fieldType);
            }
            else {
                ParentedModel pm = childDescriptor.getParentedModel();
                
                // Set the type
                fBuilder.setType(DescriptorProtos.FieldDescriptorProto.Type.TYPE_MESSAGE);
                
                Class<?> childDataType = pm.getChildModel().getOriginalInterfaceAsClass();
                String originalMethodName = pm.getOriginalMethodName();
                
                String oneOfValue = PBUtilities.getOneOf(originalInterface, originalMethodName, childDataType);
                if (GeneralUtilities.safeEquals(oneOfValue, currentOneOf)) {
                    if (oneOfValue != null) {
                        int oneOfDeclIndex = oneOfToIndexMap.get(oneOfValue);
                        
                        fBuilder.setOneofIndex(oneOfDeclIndex);
                    }
                }
                else {
                    if (oneOfValue != null) {
                        DescriptorProtos.OneofDescriptorProto.Builder oneOfBuilder = DescriptorProtos.OneofDescriptorProto.newBuilder();
                        oneOfBuilder.setName(oneOfValue);
                        
                        int oneOfIndex = oneOfNumber++;
                        builder.addOneofDecl(oneOfIndex, oneOfBuilder.build());
                        
                        oneOfToIndexMap.put(oneOfValue, oneOfIndex);

                        fBuilder.setOneofIndex(oneOfIndex);
                    }
                        
                    currentOneOf = oneOfValue;
                }
                
                ModelImpl childModel = pm.getChildModel();
                String childTypeName = childModel.getOriginalInterface();
                
                fBuilder.setTypeName("." + childTypeName);
                
                ChildType childType = pm.getChildType();
                if (childType.equals(ChildType.ARRAY) || childType.equals(ChildType.LIST)) {
                    fBuilder.setLabel(DescriptorProtos.FieldDescriptorProto.Label.LABEL_REPEATED);
                }
                else {
                    fBuilder.setLabel(DescriptorProtos.FieldDescriptorProto.Label.LABEL_OPTIONAL);
                }
            }
            
            builder.addField(fBuilder.build());
            
        }
        
        DescriptorProtos.DescriptorProto proto = builder.build();
        
        DescriptorProtos.FileDescriptorProto.Builder fileBuilder = DescriptorProtos.FileDescriptorProto.newBuilder();
        fileBuilder.addMessageType(proto);
        if (packageName != null) {
          fileBuilder.setPackage(packageName);
        }
        
        DescriptorProtos.FileDescriptorProto fProto = fileBuilder.build();
        
        Descriptors.FileDescriptor fDesc = Descriptors.FileDescriptor.buildFrom(fProto,
                knownFiles.toArray(new Descriptors.FileDescriptor[knownFiles.size()]));
        
        Descriptors.Descriptor fD = fDesc.findMessageTypeByName(protoName);
        
        return fD;
    }
    
    private static Object convertFieldForMarshal(Object field, Class<?> expectedType) {
        if (field == null) {
            if (String.class.equals(expectedType)) {
                return new String("");
            }
            
            return null;
        }
        
        if (field instanceof Short) {
            Short s = (Short) field;
            return new Integer(s.intValue());
        }
        if (field instanceof Character) {
            Character c = (Character) field;
            return new String(c.toString());
        }
        if (field instanceof Byte) {
            Byte b = (Byte) field;
            byte retVal[] = new byte[1];
            retVal[0] = b.byteValue();
            return retVal;
        }
        
        return field;
    }
    
    private static Object convertFieldForUnmarshal(Object field, ChildDataModel expected) {
        if (field == null) return null;
        
        Class<?> expectedType = expected.getChildTypeAsClass();
        
        if (expectedType.equals(short.class) || expectedType.equals(Short.class)) {
            Integer i = (Integer) field;
            return i.shortValue();
        }
        
        if (expectedType.equals(char.class) || expectedType.equals(Character.class)) {
            String s = (String) field;
            return s.charAt(0);
        }
        
        if (expectedType.equals(byte.class) || expectedType.equals(Byte.class)) {
            ByteString b = (ByteString) field;
            return b.byteAt(0);
        }
        
        if (String.class.equals(expectedType) && ((String) field).isEmpty()) {
            // PBuf returns empty string for null.  There is no way to
            // tell the difference, so we are just converting empty
            // string back null
            return null;
        }
        
        return field;
    }
    
    @Override
    public String toString() {
        return "PBufParser(" + System.identityHashCode(this) + ")";
    }
}
