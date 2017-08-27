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

import java.beans.Introspector;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.glassfish.hk2.api.MultiException;
import org.glassfish.hk2.xml.internal.Generator;
import org.glassfish.hk2.xml.internal.Utilities;

/**
 * @author jwells
 *
 */
@SupportedAnnotationTypes("org.glassfish.hk2.pbuf.api.annotations.GenerateProto")
public class PBufGeneratorProcessor extends AbstractProcessor {
    /**
     * Gets rid of warnings and this code should work with all source versions
     */
    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }
    
    @Override
    public boolean process(Set<? extends TypeElement> annotations,
            RoundEnvironment roundEnv) {
        Filer filer = processingEnv.getFiler();
        Elements elements = processingEnv.getElementUtils();
        
        for (TypeElement annotation : annotations) {
            Set<? extends Element> clazzes = roundEnv.getElementsAnnotatedWith(annotation);
            
            for (Element clazzElement : clazzes) {
                if (!(clazzElement instanceof TypeElement)) continue;
                
                TypeElement clazz = (TypeElement) clazzElement;
                ElementKind cKind = clazz.getKind();
                if (!ElementKind.INTERFACE.equals(cKind)) continue;
                
                Writer writer = openWriter(clazz, elements, filer);
                try {
                    buildHeader(writer, clazz, elements);
                    
                    List<? extends Element> members = elements.getAllMembers(clazz);
                    handleMethods(writer, clazz, members, elements);
                }
                finally {
                    try {
                        writer.close();
                    }
                    catch (IOException ioe) {
                        throw new MultiException(ioe);
                    }
                }
            }
        }
        
        return true;
    }
    
    private static void writeBlankLine(Writer writer) throws IOException {
        writer.write("\n");
    }
    
    /**
     * Actually four spaces
     * 
     * @param writer
     * @throws IOException
     */
    private static void writeTab(Writer writer) throws IOException {
        writer.write("    ");
    }
    
    private Writer openWriter(TypeElement clazz, Elements elements, Filer filer) {
        PackageElement clazzPackage = elements.getPackageOf(clazz);
        String clazzPackageName = Utilities.convertNameToString(clazzPackage.getQualifiedName());
        String clazzSimpleName = Utilities.convertNameToString(clazz.getSimpleName());
        clazzSimpleName = clazzSimpleName + ".proto";
        
        try {
            FileObject fileObject = filer.createResource(StandardLocation.CLASS_OUTPUT,
                clazzPackageName,
                clazzSimpleName,
                clazz);
            
            return fileObject.openWriter();
        }
        catch (IOException ioe) {
            // TODO: How should errors actually be handled?
            throw new MultiException(ioe);
        }
    }
    
    private Writer buildHeader(Writer writer, TypeElement clazz, Elements elements) {
        PackageElement clazzPackage = elements.getPackageOf(clazz);
        String clazzPackageName = Utilities.convertNameToString(clazzPackage.getQualifiedName());
        String name = Utilities.convertNameToString(clazz.getQualifiedName());
        
        name = name.replace('.', '/');
        
        try {
            writer.write("// Generated by PBufGeneratorProcessor\n");
            writer.write("// Source File: " + name + ".java\n");
            writeBlankLine(writer);
            
            writer.write("syntax = \"proto3\";\n");
            writeBlankLine(writer);
            
            if (clazzPackageName != null && !clazzPackageName.isEmpty()) {
                writer.write("package " + clazzPackageName + ";\n\n");
            }
            
            return writer;
        }
        catch (IOException ioe) {
            // TODO: How should errors actually be handled?
            throw new MultiException(ioe);
        }
    }
    
    private void handleMethods(Writer writer, TypeElement clazz, List<? extends Element> allMethods, Elements elements) {
        String clazzSimpleName = Utilities.convertNameToString(clazz.getSimpleName());
        
        try {
            List<XmlElementInfo> infos = doImports(writer, clazz, allMethods, elements);
            
            writer.write("message " + clazzSimpleName + " {\n");
            
            int number = 1;
            for (XmlElementInfo info : infos) {
                boolean incrementCount = handleMethod(writer, info, number);
                if (incrementCount) {
                    number++;
                }
            }
            
            writer.write("}\n");
            writeBlankLine(writer);
        }
        catch (IOException ioe) {
            throw new MultiException(ioe);
        }
    }
    
    private boolean handleMethod(Writer writer, XmlElementInfo info, int number) throws IOException {
        writeTab(writer);
        
        if (info.getChildInfo() == null) {
            if (info.isRequired()) {
                writer.write("required ");
            }
            
            writer.write(info.getType() + " " + info.getName() + " = " + number);
        }
        else {
            if (info.getChildInfo().isRepeated()) {
                writer.write("repeated ");
            }
            
            writer.write(info.getChildInfo().getName() + " " + info.getName() + " = " + number);
        }
        
        writer.write(";\n");
        
        return true;
    }
    
    private static ExecutableElement getFieldFromAnnotation(TypeElement annotation, String field) {
        List<? extends Element> elements = annotation.getEnclosedElements();
        for (Element element : elements) {
            if (!(element instanceof ExecutableElement)) {
                continue;
            }
            
            ExecutableElement ee = (ExecutableElement) element;
            String executableName = Utilities.convertNameToString(ee.getSimpleName());
            
            if (field.equals(executableName)) {
                return ee;
            }
        }
        
        return null;
    }
    
    private static String getPBufType(ExecutableElement method, TypeMirror mirror) {
        TypeKind kind = mirror.getKind();
        
        switch(kind) {
        case BOOLEAN:
            return "bool";
        case BYTE:
        case SHORT:
        case INT:
            return "int32";
        case LONG:
            return "int64";
        case CHAR:
            return "string";
        case FLOAT:
            return "float";
        case DOUBLE:
            return "double";
        case DECLARED:
        case ARRAY:
            // TODO: Really?
            return "string";
        default:
            throw new AssertionError("Unknown getter has a unknown return type: " + kind + " for method " + method +
                    " and mirror " + mirror);
        }
    }
    
    private static String getPBufType(ExecutableElement method) {
        String methodName = Utilities.convertNameToString(method.getSimpleName());
        
        if (methodName.startsWith("set")) {
            List<? extends VariableElement> parameters = method.getParameters();
            if (parameters.size() != 1) {
                throw new AssertionError("Unknown setter has more than one input parameter: " + methodName);
            }
            
            TypeMirror asType = parameters.get(0).asType();
            
            String retVal = getPBufType(method, asType);
            return retVal;
        }
        
        if (methodName.startsWith("get")) {
            TypeMirror type = method.getReturnType();
            
            if (type == null) {
                throw new AssertionError("Unknown getter does not have a return type: " + methodName);
            }
            
            String retVal = getPBufType(method, type);
            return retVal;
        }
        
        throw new AssertionError("Cannot determing type as XmlElement not on a get or set method " + methodName);
        
    }
    
    private static XmlElementInfo getXmlElementName(ExecutableElement method, Elements elements) {
        List<? extends AnnotationMirror> annotationMirrors = elements.getAllAnnotationMirrors(method);
        for (AnnotationMirror mirror : annotationMirrors) {
            DeclaredType mirrorType = mirror.getAnnotationType();
            Element e = mirrorType.asElement();
            if (!(e instanceof TypeElement)) {
                continue;
            }
            TypeElement te = (TypeElement) e;
            
            String mirrorTypeName = Utilities.convertNameToString(te.getQualifiedName());
            if (mirrorTypeName.equals(XmlElement.class.getName()) || mirrorTypeName.equals(XmlAttribute.class.getName())) {
                String type = getPBufType(method);
                
                // It has the XmlElement on it, now get the actual name
                Map<? extends ExecutableElement,? extends AnnotationValue> values =
                        elements.getElementValuesWithDefaults(mirror);
                
                ExecutableElement foundNameExecutable = getFieldFromAnnotation(te, "name");
                ExecutableElement foundRequiredExecutable = getFieldFromAnnotation(te, "required");
                
                AnnotationValue av = values.get(foundNameExecutable);
                if (av == null) {
                     throw new AssertionError("The value of name must never be null");
                }
                
                String nameValue = null;
                    
                String sValue = (String) av.getValue();
                if (Generator.JAXB_DEFAULT_STRING.equals(sValue)) {
                    String methodName = Utilities.convertNameToString(method.getSimpleName());
                    
                    nameValue = methodName.substring(3);
                    nameValue = Introspector.decapitalize(nameValue);
                }
                else {
                    nameValue = sValue;
                }
                
                av = values.get(foundRequiredExecutable);
                boolean requiredValue = (Boolean) av.getValue();
                
                String methodName = Utilities.convertNameToString(method.getSimpleName());
                String sortField = methodName.substring(3);
                sortField = Introspector.decapitalize(sortField);
                
                return new XmlElementInfo(nameValue, requiredValue, type, sortField);
            }
        }
        
        return null;
    }
    
    private static ChildInfo getChildName(ExecutableElement method) {
        TypeMirror tm = method.getReturnType();
        TypeKind kind = tm.getKind();
        
        if (TypeKind.ARRAY.equals(kind)) {
            ArrayType at = (ArrayType) tm;
            
            TypeMirror componentType = at.getComponentType();
            TypeKind componentKind = componentType.getKind();
            
            if (TypeKind.DECLARED.equals(componentKind)) {
                DeclaredType dt = (DeclaredType) componentType;
                Element asElement = dt.asElement();
                
                ElementKind elementKind = asElement.getKind();
                if (ElementKind.INTERFACE.equals(elementKind)) {
                    TypeElement interfaceElement = (TypeElement) asElement;
                    
                    return new ChildInfo(Utilities.convertNameToString(interfaceElement.getQualifiedName()), true);
                }
            }
        }
        else if (TypeKind.DECLARED.equals(kind)) {
            DeclaredType dt = (DeclaredType) tm;
            Element asElement = dt.asElement();
            ElementKind elementKind = asElement.getKind();
            
            if (ElementKind.INTERFACE.equals(elementKind)) {
                TypeElement possibleList = (TypeElement) asElement;
                String possibleListType = Utilities.convertNameToString(possibleList.getQualifiedName());
                
                if (List.class.getName().equals(possibleListType)) {
                    // It is a list, now need the type parameter
                    List<? extends TypeMirror> typeArguments = dt.getTypeArguments();
                    if (typeArguments == null || typeArguments.isEmpty()) {
                        return null;
                    }
                    
                    TypeMirror firstArgument = typeArguments.get(0);
                    TypeKind firstTypeKind = firstArgument.getKind();
                    
                    if (TypeKind.DECLARED.equals(firstTypeKind)) {
                        DeclaredType firstDT = (DeclaredType) firstArgument;
                        Element firstElement = firstDT.asElement();
                        ElementKind firstElementKind = firstElement.getKind();
                        
                        if (!ElementKind.INTERFACE.equals(firstElementKind)) {
                            return null;
                        }
                        
                        TypeElement firstElementType = (TypeElement) firstElement;
                        return new ChildInfo(Utilities.convertNameToString(firstElementType.getQualifiedName()), true);
                    }
                }
                else {
                    // It's a direct child
                    return new ChildInfo(possibleListType, false);
                }
            }
        }
        
        return null;
    }
    
    private List<XmlElementInfo> doImports(Writer writer,
            TypeElement clazz,
            List<? extends Element> allMethods,
            Elements elements) throws IOException {
        String clazzName = Utilities.convertNameToString(clazz.getQualifiedName());
        
        boolean atLeastOne = false;
        
        List<XmlElementInfo> retVal = new ArrayList<XmlElementInfo>(allMethods.size());
        for (Element element : allMethods) {
            if (!(element instanceof ExecutableElement)) {
                continue;
            }
            
            ExecutableElement method = (ExecutableElement) element;
            XmlElementInfo xmlElementName = getXmlElementName(method, elements);
            if (xmlElementName == null) {
                continue;
            }
            
            retVal.add(xmlElementName);
            
            ChildInfo childName = getChildName(method);
            if (childName == null) {
                continue;
            }
            
            xmlElementName.setChildInfo(childName);
            
            String importName = childName.getName().replace('.', '/') + ".proto";
            
            writer.write("import \"" + importName + "\";\n");
            atLeastOne = true;
        }
        
        if (atLeastOne) {
            writeBlankLine(writer);
        }
        
        XmlType xmlType = clazz.getAnnotation(XmlType.class);
        if (xmlType == null) {
            return retVal;
        }
        
        String propOrder[] = xmlType.propOrder();
        
        ElementOrderer comparator = new ElementOrderer(propOrder, clazzName);
        
        TreeSet<XmlElementInfo> sortedMap = new TreeSet<XmlElementInfo>(comparator);
        
        sortedMap.addAll(retVal);
        
        List<XmlElementInfo> sortedRetVal = new ArrayList<XmlElementInfo>(retVal.size());
        
        sortedRetVal.addAll(sortedMap);
        
        return sortedRetVal;
    }
    
    private final static class XmlElementInfo {
        private final String name;
        private final boolean required;
        private final String type;
        private ChildInfo childInfo;
        private final String sortField;
        
        private XmlElementInfo(String name, boolean required, String type, String sortField) {
            this.name = name;
            this.required = required;
            this.type = type;
            this.sortField = sortField;
        }
        
        private String getName() {
            return name;
        }
        
        private boolean isRequired() {
            return required;
        }
        
        private String getType() {
            return type;
        }
        
        private void setChildInfo(ChildInfo info) {
            this.childInfo = info;
        }
        
        private ChildInfo getChildInfo() {
            return childInfo;
        }
        
        private String getSortField() {
            return sortField;
        }
        
        @Override
        public int hashCode() {
            return sortField.hashCode();
        }
        
        @Override
        public boolean equals(Object o) {
            if (o == null) return false;
            if (!(o instanceof XmlElementInfo)) {
                return false;
            }
            
            XmlElementInfo other = (XmlElementInfo) o;
            
            return other.sortField.equals(sortField);
        }
    }
    
    private final static class ChildInfo {
        private final String name;
        private final boolean repeated;
        
        private ChildInfo(String name, boolean repeated) {
            this.name = name;
            this.repeated = repeated;
        }
        
        private String getName() {
            return name;
        }
        
        private boolean isRepeated() {
            return repeated;
        }
    }
    
    private final static class ElementOrderer implements Comparator<XmlElementInfo> {
        private final Map<String, Integer> sorter = new HashMap<String, Integer>();
        private final String clazzName;
        
        private ElementOrderer(String order[], String clazzName) {
            this.clazzName = clazzName;
            
            for (int lcv = 0; lcv < order.length; lcv++) {
                sorter.put(order[lcv], lcv);
            }
        }

        /* (non-Javadoc)
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        @Override
        public int compare(XmlElementInfo o1, XmlElementInfo o2) {
            String o1SortField = o1.getSortField();
            String o2SortField = o2.getSortField();
            
            if (!sorter.containsKey(o1SortField)) {
                throw new AssertionError("XmlType must contain all XmlElement and XmlAttribute names, the name " + o1SortField +
                        " was not found in XmlType on " + clazzName);
            }
            
            if (!sorter.containsKey(o2SortField)) {
                throw new AssertionError("XmlType must contain all XmlElement and XmlAttribute names, the name " + o1SortField +
                        " was not found in XmlType on " + clazzName);
            }
            
            int o1Spot = sorter.get(o1SortField);
            int o2Spot = sorter.get(o2SortField);
            
            return o1Spot - o2Spot;
        }
    }

}
