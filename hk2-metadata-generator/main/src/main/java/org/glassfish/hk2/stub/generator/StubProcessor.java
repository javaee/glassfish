/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.hk2.stub.generator;

import java.io.IOException;
import java.io.Writer;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.JavaFileObject;
import javax.tools.Diagnostic.Kind;

import org.glassfish.hk2.api.MultiException;
import org.glassfish.hk2.metadata.generator.ServiceUtilities;

/**
 * @author jwells
 *
 */
@SupportedAnnotationTypes("org.glassfish.hk2.utilities.Stub")
public class StubProcessor extends AbstractProcessor {
    /**
     * Gets rid of warnings and this code should work with all source versions
     */
    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }

    /* (non-Javadoc)
     * @see javax.annotation.processing.AbstractProcessor#process(java.util.Set, javax.annotation.processing.RoundEnvironment)
     */
    @Override
    public boolean process(Set<? extends TypeElement> annotations,
            RoundEnvironment roundEnv) {
        MultiException me = null;
        
        for (TypeElement annotation : annotations) {
            Set<? extends Element> clazzes = roundEnv.getElementsAnnotatedWith(annotation);
            
            for (Element clazzElement : clazzes) {
                if (!(clazzElement instanceof TypeElement)) continue;
                
                TypeElement clazz = (TypeElement) clazzElement;
                
                try {
                    writeStub(clazz);
                }
                catch (IOException ioe) {
                    if (me == null) {
                        me = new MultiException(ioe);
                    }
                    else {
                        me.addError(ioe);
                    }
                    
                }
            }
            
        }
        
        if (me != null) {
            processingEnv.getMessager().printMessage(Kind.ERROR, me.getMessage());
            me.printStackTrace();
            return true;
        }
        
        return true;
    }
    
    private void writeStub(TypeElement clazz) throws IOException {
        Elements elementUtils = processingEnv.getElementUtils();
        
        Set<ExecutableElement> abstractMethods = new LinkedHashSet<ExecutableElement>();
        List<? extends Element> enclosedElements = elementUtils.getAllMembers(clazz);
        for (Element enclosedElement : enclosedElements) {
            if (!ElementKind.METHOD.equals(enclosedElement.getKind())) continue;
            
            Set<Modifier> modifiers = enclosedElement.getModifiers();
            if (!modifiers.contains(Modifier.ABSTRACT)) continue;
            
            ExecutableElement executableMethod = (ExecutableElement) enclosedElement;
            
            abstractMethods.add(executableMethod);
        }
        
        writeJavaFile(clazz, abstractMethods);
    }
    
    private void writeJavaFile(TypeElement clazz, Set<ExecutableElement> abstractMethods) throws IOException {
        Elements elementUtils = processingEnv.getElementUtils();
        
        PackageElement packageElement = elementUtils.getPackageOf(clazz);
        String packageName = ServiceUtilities.nameToString(packageElement.getQualifiedName());
        String clazzSimpleName = ServiceUtilities.nameToString(clazz.getSimpleName());
        
        String stubClazzName = clazzSimpleName + "_hk2Stub";
        
        String fileName = stubClazzName;
        
        Filer filer = processingEnv.getFiler();
        
        JavaFileObject jfo = filer.createSourceFile(fileName, clazz);
        
        Writer writer = jfo.openWriter();
        try {
            writer.append("package " + packageName + ";\n\n");
            
            writer.append("import javax.annotation.Generated;\n");
            writer.append("import org.jvnet.hk2.annotations.Service;\n\n");
            
            writer.append("@Service @Generated(\"org.glassfish.hk2.stub.generator.StubProcessor\")\n");
            writer.append("public class " + stubClazzName + " extends " + clazzSimpleName + "{\n");
            
            for (ExecutableElement abstractMethod : abstractMethods) {
                writeAbstractMethod(abstractMethod, writer); 
            }
            
            writer.append("}\n");
        }
        finally {
            writer.close();
        }
        
        
        
    }
    
    private void writeAbstractMethod(ExecutableElement abstractMethod, Writer writer) throws IOException {
        Set<Modifier> modifiers = abstractMethod.getModifiers();
        
        writer.append("    ");
        
        if (modifiers.contains(Modifier.PUBLIC)) {
            writer.append("public ");
        }
        else if (modifiers.contains(Modifier.PROTECTED)) {
            writer.append("protected ");
        }
        
        TypeMirror returnType = abstractMethod.getReturnType();
        TypeMirrorOutputs returnOutputs = typeMirrorToString(returnType, false);
        
        writer.append(returnOutputs.leftHandSide + " " + abstractMethod.getSimpleName() + "(");
        
        List<? extends VariableElement> parameterElements = abstractMethod.getParameters();
        int numParams = parameterElements.size();
        int lcv = 0;
        
        for (VariableElement variable : parameterElements) {
            TypeMirror variableAsType = variable.asType();
            
            boolean varArgs = abstractMethod.isVarArgs() && ((lcv + 1) == numParams);
            
            TypeMirrorOutputs paramOutputs = typeMirrorToString(variableAsType, varArgs);
            if (lcv > 0) {
                writer.append(", ");
            }
            
            writer.append(paramOutputs.leftHandSide);
            
            if (varArgs) {
                writer.append("...");
            }
            
            writer.append(" p" + lcv);
            lcv++;
        }
        
        writer.append(") {\n        return " + returnOutputs.body + ";\n    }\n\n");
    }
    
    private TypeMirrorOutputs typeMirrorToString(TypeMirror mirror, boolean varArg) throws IOException {
        Types typeUtils = processingEnv.getTypeUtils();
        
        TypeKind returnKind = mirror.getKind();
        
        switch (returnKind) {
        case ARRAY:
            return new TypeMirrorOutputs(arrayTypeToString((ArrayType) mirror, varArg), "null");
        case VOID:
            return new TypeMirrorOutputs("void", "");
        case BOOLEAN:
            return new TypeMirrorOutputs("boolean", "true");
        case BYTE:
            return new TypeMirrorOutputs("byte", "0");
        case CHAR:
            return new TypeMirrorOutputs("char", "0");
        case DOUBLE:
            return new TypeMirrorOutputs("double", "(double) 0.0");
        case FLOAT:
            return new TypeMirrorOutputs("float", "(float) 0.0");
        case INT:
            return new TypeMirrorOutputs("int", "0");
        case LONG:
            return new TypeMirrorOutputs("long", "0");
        case SHORT:
            return new TypeMirrorOutputs("short", "0");
        case DECLARED:
            TypeElement element = (TypeElement) typeUtils.asElement(mirror);
            return new TypeMirrorOutputs(ServiceUtilities.nameToString(element.getQualifiedName()), "null"); 
        default:
            throw new IOException("Unknown kind: " + returnKind);
        }
        
    }
    
    private String arrayTypeToString(ArrayType arrayType, boolean varArgs) throws IOException {
        int numBraces = (varArgs) ? 0 : 1 ;
        
        TypeMirror arrayOfType = arrayType.getComponentType();
        while (arrayOfType instanceof ArrayType) {
            numBraces++;
            
            arrayOfType = ((ArrayType) arrayOfType).getComponentType();
        }
        
        TypeMirrorOutputs underlyingType = typeMirrorToString(arrayOfType, false);
        
        StringBuffer sb = new StringBuffer(underlyingType.leftHandSide);
        for (int lcv = 0; lcv < numBraces; lcv++) {
            sb.append("[]");
        }
        
        return sb.toString();
        
    }
    
    private static class TypeMirrorOutputs {
        private final String leftHandSide;
        private final String body;
        
        private TypeMirrorOutputs(String leftHandSide, String body) {
            this.leftHandSide = leftHandSide;
            this.body = body;
        }
    }
    
    

}
