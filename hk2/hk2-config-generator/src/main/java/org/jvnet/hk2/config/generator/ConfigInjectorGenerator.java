/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2007-2011 Oracle and/or its affiliates. All rights reserved.
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
package org.jvnet.hk2.config.generator;

import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JForEach;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;


import com.sun.tools.xjc.api.util.FilerCodeWriter;
import org.jvnet.hk2.annotations.InhabitantAnnotation;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.MultiMap;
import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.ConfigInjector;
import org.jvnet.hk2.config.ConfigMetadata;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.Dom;
import org.jvnet.hk2.config.InjectionTarget;
import org.jvnet.hk2.config.NoopConfigInjector;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.SimpleElementVisitor6;
import javax.lang.model.util.SimpleTypeVisitor6;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

/**
 * Generates {@link ConfigInjector} implementations for {@link Configured} objects
 * and {@link ConfigBeanProxy} subtypes.
 * 
 * @author Kohsuke Kawaguchi
 */
public class ConfigInjectorGenerator extends SimpleElementVisitor6<Object, Element> {

    private  ProcessingEnvironment env;

    private JCodeModel cm;

    TypeMath math;
    /**
     * Reference to the {@link ConfigBeanProxy} type.
     */
    private TypeElement configBeanProxy;

    public ConfigInjectorGenerator(ProcessingEnvironment env) {
        this.env = env;
        this.math = new TypeMath(env);
        configBeanProxy = env.getElementUtils().getTypeElement(ConfigBeanProxy.class.getName());
    }

    public void process(Element element) {
        cm = new JCodeModel();

        element.accept(this, element);

        try {
            cm.build(new FilerCodeWriter(env.getFiler()));
        } catch (IOException e) {
            throw new Error(e);
        }
        cm = null;
    }

    private void printError(Element element, String message) {
        env.getMessager().printMessage(Diagnostic.Kind.ERROR, message, element);
    }

    /**
     * For each class annotated with {@link Configured}.
     */
    public void visitClassDeclaration(TypeElement clz) {
        if (clz.getKind() == ElementKind.CLASS) {
            try {
                new ClassGenerator(clz, false).generate();
            } catch (JClassAlreadyExistsException e) {
                printError(clz, e.toString());
            }
        } else if (clz.getKind() == ElementKind.INTERFACE) {
            try {
                Types types = env.getTypeUtils();
                if(!types.isSubtype(clz.asType(), configBeanProxy.asType())) {
                    printError(clz, clz.getQualifiedName()+" has @Configured but doesn't extend ConfigBeanProxy");
                } else {
                    new ClassGenerator(clz, true).generate();
                }
            } catch (JClassAlreadyExistsException e) {
                printError(clz, e.toString());
            }
        }
    }



    /*package*/ class ClassGenerator {
        final TypeElement clz;
        final JDefinedClass injector;
        final JClass targetType;
        final JAnnotationUse service;
        final JMethod injectMethod,injectAttributeMethod,injectElementMethod;
        final MultiMap<String,String> metadata = new MultiMap<String,String>();
        /**
         * Key property that has {@link org.jvnet.hk2.config.Element#key()} or {@link Attribute#key()}
         */
        private Property key=null;

        /**
         * If true, generate a ConfigInjector that extends from {@link NoopConfigInjector}.
         * This is used as the metadata place holder for {@link ConfigBeanProxy}s.
         * <p>
         * If false, generate a real {@link ConfigInjector}.
         *
         * <p>
         * See the test-config test module for this difference in action. 
         */
        private final boolean generateNoopConfigInjector;


        public ClassGenerator(TypeElement clz, boolean generateNoopConfigInjector) throws JClassAlreadyExistsException {
            this.clz = clz;
            this.generateNoopConfigInjector = generateNoopConfigInjector;
            Configured c = clz.getAnnotation(Configured.class);

            String name = clz.toString();
            targetType = cm.ref(name);

            // [RESULT]
            // @Service(name='...')
            // @InjectionTarget(target)
            // public class XYZInjector extends ConfigInjector<XYZ>
            injector = cm._class(name+"Injector");
            String elementName = c.name();
            if(c.local()) {
                if(elementName.length()>0) {
                    printError(clz, "@Configured.local and @Configured.name is mutually exclusive");
                    elementName = ""; // error recovery
                }
            } else {
                if(elementName.length()==0) // infer default
                    elementName = Dom.convertName(clz.getSimpleName().toString());
            }

            service = injector.annotate(Service.class).param("name",elementName);
            injector.annotate(InjectionTarget.class).param("value",targetType);

            Set<String> targetHabitats = new HashSet<String>();
            if (c != null) {
//                for (AnnotationMirror am : clz.getAnnotationMirrors()) {
//                    InhabitantAnnotation ia = am.getAnnotationType().getDeclaration().getAnnotation(InhabitantAnnotation.class);
//                    if (ia != null) {
//                        targetHabitats.add(ia.value());
//                    }
//                }
                //TODO: [Double Check]
                InhabitantAnnotation ia = clz.getAnnotation(InhabitantAnnotation.class);
                if (ia != null) {
                    targetHabitats.add(ia.value());
                }
            }

            if(generateNoopConfigInjector) {
                injector._extends(cm.ref(NoopConfigInjector.class));
                injectAttributeMethod = null;
                injectMethod = null;
                injectElementMethod = null;
            } else {
                injector._extends(cm.ref(ConfigInjector.class).narrow(targetType));

                // [RESULT]
                // public void inject(Dom dom, Property target) { ... }
                injectMethod = injector.method(JMod.PUBLIC, void.class, "inject");
                injectMethod.param(Dom.class, "dom");
                injectMethod.param(targetType, "target");
                injectMethod.body();

                injectAttributeMethod = injector.method(JMod.PUBLIC,void.class,"injectAttribute");
                addReinjectionParam(injectAttributeMethod);

                injectElementMethod = injector.method(JMod.PUBLIC,void.class,"injectElement");
                addReinjectionParam(injectElementMethod);
            }

            metadata.add(ConfigMetadata.TARGET,name);

            // locate additional contracts for the target.
            for (TypeElement t : HK2ContractFinder.find(env.getTypeUtils(), clz))
                metadata.add(ConfigMetadata.TARGET_CONTRACTS, t.getQualifiedName().toString());

            if (targetHabitats.size() > 0) {
                StringBuilder sb = new StringBuilder();
                for (String h : targetHabitats) {
                    sb.append(h).append(";");
                }
                metadata.add(ConfigMetadata.TARGET_HABITATS, sb.toString());
            }
        }

//        private boolean isSubAnnotationOf(String base, AnnotationMirror sub) {
//            if (sub.toString().equals(base)) {
//                return true;
//            }
//
//            for (AnnotationMirror am : sub.getAnnotationType().getDeclaration().getAnnotationMirrors()) {
//                if (isSubAnnotationOf(base, am)) {
//                    return true;
//                }
//            }
//
//            return false;
//        }

        private void addReinjectionParam(JMethod method) {
            method.param(Dom.class,"dom");
            method.param(String.class,"name");
            method.param(targetType, "target");
        }

        /**
         * Visits all annotated fields/methods and
         * generates the body of the {@link ConfigInjector#inject(Dom, Object)} code.
         */
        public void generate() {
            Stack<TypeElement> q = new Stack<TypeElement>();
            Set<TypeElement> visited = new HashSet<TypeElement>();
            q.push(clz);

            while(!q.isEmpty()) {
                TypeElement t = q.pop();
                if(!visited.add(t)) continue;   // been here already

                for (VariableElement f : ElementFilter.fieldsIn(t.getEnclosedElements()))
                    generate(new Property.Field(f));

                for (ExecutableElement m : ElementFilter.methodsIn(t.getEnclosedElements()))
                    generate(new Property.Method(m));


                for (TypeMirror tm : clz.getInterfaces())
                    q.add((TypeElement) env.getTypeUtils().asElement(tm));

                if (t.getKind() == ElementKind.CLASS) {
                    if(t.getSuperclass()!=null)
                        q.add((TypeElement) env.getTypeUtils().asElement(t.getSuperclass()));
                }
            }

            service.param("metadata", metadata.toCommaSeparatedString());
        }

        private void generate(Property p) {
            Attribute a = p.getAnnotation(Attribute.class);
            org.jvnet.hk2.config.Element e = p.getAnnotation(org.jvnet.hk2.config.Element.class);

            if(a!=null) {
                new AttributeMethodGenerator(p,a).generate();
                if(e!=null)
                    printError(p.decl(), "Cannot have both @Element and @Attribute at the same time");
            } else {
                if(e!=null)
                    new ElementMethodGenerator(p, e).generate();
            }

            // Updates #key with error check.
            if(p.isKey()) {
                if(key!=null) {
                    printError(p.decl(), "Multiple key properties");
                    printError(key.decl(), "Another one is at here");
                }
                key = p;
            }
        }

        /**
         * Generates a single injection method, which inject
         * value(s) of a particular element/attribute name.
         */
        private abstract class MethodGenerator {
            final JBlock body;
            final JVar $dom;
            final JVar $target;
            /**
             * Element name or attribute name.
             * A special case is "*" for elements that indicate substitute-by-type.
             */
            final String xmlName;
            final Property p;
            private int iota=1;
            /**
             * Erasure of {@code p.type()}
             */
            final TypeMirror erasure;
            /**
             * If this is a multi-value property, the packer knows how to create a collection value.
             */
            final Packer packer;
            /**
             * The type of individual item. If this is a multi-value property, this is a type of the collection
             * item, otherwise the same as {@link #erasure}.
             */
            final TypeMirror itemType;
            /**
             * Converter for {@link #itemType}.
             */
            /*semi-final*/ Converter conv;

            MethodGenerator(String methodNamePrefix, JMethod reinjectionMethod, Property p, String xmlName) {
                this.xmlName = p.inferName(xmlName);
                this.p = p;

                if(generateNoopConfigInjector) {
                    body = null;
                    $dom = null;
                    $target = null;
                } else {
                    JMethod m = injector.method(JMod.PUBLIC,void.class, methodNamePrefix+p.seedName());
                    $dom = m.param(Dom.class,"dom");
                    $target = m.param(targetType,"target");
                    body = m.body();

                    injectMethod.body().invoke(m).arg($dom).arg($target);

                    reinjectionMethod.body()._if(JExpr.lit(this.xmlName).invoke("equals").arg(JExpr.ref("name")))
                        ._then().invoke(m).arg($dom).arg($target);
                }

                erasure = erasure(p.type());
                packer = createPacker(p.type(),erasure);

                itemType = packer==null ? erasure : erasure(packer.itemType());
            }

            private void assign(JExpression rhs) {
                p.assign($target,body,rhs);
            }

            /**
             * Returns '@xmlName' for attributes and '&lt;xmlName>' for elements.
             */
            protected abstract String xmlTokenName();

            protected void generate() {
                conv = createConverter(itemType);
                conv.addMetadata(xmlTokenName(),itemType);

                if(!isVariableExpansion() && TO_JTYPE.visit(itemType, (Void) null)!=cm.ref(String.class))
                    printError(p.decl(), "variableExpansion=false is only allowed on String");

                if(!generateNoopConfigInjector) {
                    JVar value = var(
                        packer!=null ? cm.ref(List.class).narrow(conv.sourceType()) : conv.sourceType(),getXmlValue());

                    if(!isRequired())
                        body._if(value.eq(JExpr._null()))._then()._return();

                    if(packer!=null)
                        handleMultiValue(value);
                    else
                        assign(conv.as(value,itemType));
                }

                if(p.isKey())
                    addKey();
            }

            /**
             * Returns true if the property must have a value, or if it's optional.
             */
            protected abstract boolean isRequired();

            /**
             * Returns true if the property is a referenec to another element
             */
            protected abstract boolean isReference();

            /**
             * Returns true if the property is a a subject of variable expansion.
             */
            protected abstract boolean isVariableExpansion();

            /**
             * Return true if this property is {@code @FromElement("*")},
             * which means finding a match by types
             */
            protected abstract boolean isAllElementMatch();

            /**
             * Obtains the source value(s) from {@link Dom}.
             */
            protected abstract JExpression getXmlValue();

            private void addKey() {
                metadata.add(ConfigMetadata.KEY, xmlTokenName());
                metadata.add(ConfigMetadata.KEYED_AS, p.decl().asType().toString());
            }

            /**
             * Invokes a method on DOM by adjusting the name for plural.
             */
            final JInvocation invokeDom(String methodName) {
                if(packer!=null)    methodName+='s';
                return $dom.invoke(methodName);
            }
            
            private void handleMultiValue(JVar values) {
                // [RESULT]
                // List<S> values = dom.leafElements("...");
                // <packer init>
                // for( S v : values ) {
                //   <packer set>(<as>(v));
                // }
                //  ... assign ...
                packer.start(values.invoke("size"));
                JForEach forEach = body.forEach(conv.sourceType(), id(), values);
                packer.pack(forEach.body(),conv.as(forEach.var(),packer.itemType()), forEach.var());
                assign(packer.end());
            }

            /**
             * Creates a variable
             */
            protected JVar var(JType t, JExpression init) {
                return body.decl(t,id(),init);
            }

            protected JVar var(Class t, JExpression init) {
                return var(cm.ref(t),init);
            }

            /**
             * Creates an unique id.
             */
            private String id() {
                return "v"+(iota++);
            }

            private Packer createPacker(TypeMirror type, TypeMirror erasure) {
                if(erasure instanceof ArrayType) {
                    // T=X[]
                    return new ArrayPacker((ArrayType)erasure);
                }

                TypeMirror itemType = math.isCollection(type);
                if(itemType!=null) {
                    // T=Collection[]
                    return new ListPacker(type,itemType);
                }

                TypeMirror mapType = TypeMath.baseClassFinder.visit(type, env.getElementUtils().getTypeElement(Map.class.getName()));
                if(mapType!=null) {
                    // T=Map<...>
                    DeclaredType d = (DeclaredType)mapType;
                    Iterator<? extends TypeMirror> itr = d.getTypeArguments().iterator();
                    itr.next();
                    return new MapPacker(itr.next());
                }

                return null;
            }

            abstract class Packer {
                abstract TypeMirror itemType();
                /**
                 * Starts packing.
                 */
                abstract void start(JExpression $valueSize);
                /**
                 * Adds one more item to the pack.
                 */
                abstract void pack(JBlock block, JExpression item, JExpression sourceValue);

                /**
                 * Returns the packed value to be set.
                 */
                abstract JExpression end();
            }

            final class ArrayPacker extends Packer {
                private JVar $array,$index;
                private final JType arrayT;
                private final JType componentT;
                private final ArrayType at;

                public ArrayPacker(ArrayType t) {
                    this.at = t;
                    this.componentT = TO_JTYPE.visit(itemType(),null);
                    this.arrayT = componentT.array();
                }

                TypeMirror itemType() {
                    return at.getComponentType();
                }

                void start(JExpression $valueSize) {
                    // [RESULT]
                    // T[] x = new T[values.size()];
                    $array = var(arrayT, JExpr.newArray(componentT, $valueSize));
                    $index = var(int.class,JExpr.lit(0));
                }

                void pack(JBlock block, JExpression item, JExpression sourceValue) {
                    // [RESULT]
                    // x[i++] = <rhs>;
                    block.assign($array.component($index.incr()),item);
                }

                JExpression end() {
                    return $array;
                }
            }

            final class ListPacker extends Packer {
                private JVar $list;
                private final JClass collectionType,itemType;
                private final TypeMirror itemT;

                public ListPacker(TypeMirror collectionType, TypeMirror itemType) {
                    this.collectionType = TO_JTYPE.visit(collectionType,null).boxify();
                    this.itemType       = TO_JTYPE.visit(itemType,null).boxify();
                    this.itemT = itemType;
                }

                TypeMirror itemType() {
                    return itemT;
                }

                void start(JExpression $valueSize) {
                    // [RESULT]
                    // T x = new ArrayList<T>(values.size());
                    $list = var(collectionType,JExpr._new(implType()).arg($valueSize));
                }

                /**
                 * Figure out the concrete implementation class to be used.
                 */
                JType implType() {
                    if(cm.ref(Set.class).isAssignableFrom(collectionType))
                        return cm.ref(HashSet.class).narrow(itemType);
                    return cm.ref(ArrayList.class).narrow(itemType);
                }

                void pack(JBlock block, JExpression item, JExpression sourceValue) {
                    // [RESULT]
                    // x.add(<rhs>);
                    block.invoke($list,"add").arg(item);
                }

                JExpression end() {
                    return $list;
                }
            }

            final class MapPacker extends Packer {
                private JVar $map;
                private final TypeMirror itemT;

                public MapPacker(TypeMirror itemType) {
                    this.itemT = itemType;
                }

                TypeMirror itemType() {
                    return itemT;
                }

                void start(JExpression $valueSize) {
                    // [RESULT]
                    // T x = new HashMap<T>();
                    $map = var(Map.class,JExpr._new(cm.ref(HashMap.class)).arg($valueSize));
                }

                void pack(JBlock block, JExpression item, JExpression itemDom) {
                    // [RESULT]
                    // x.put(dom.getKey(),<rhs>);
                    block.invoke($map,"put").arg(itemDom.invoke("getKey")).arg(item);
                }

                JExpression end() {
                    return $map;
                }
            }

            private Converter createConverter(TypeMirror itemType) {
                try {
                    // is this a leaf value?
                    math.SIMPLE_VALUE_CONVERTER.visit(itemType, JExpr._null());
                    return new LeafConverter();
                } catch (UnsupportedOperationException e) {
                    // nope
                }

                // try to handle it as a reference
                if (itemType instanceof DeclaredType) {
                    if (!(itemType.getKind() == TypeKind.DECLARED)) {
                        printError(p.decl(), "I only inject interfaces or classes,  "+itemType+" is not one of them");
                        return new NodeConverter();
                    }

                    TypeElement decl = (TypeElement) itemType;
                    Configured cfg = decl.getAnnotation(Configured.class);
                    if(cfg!=null) {
                        // node value
                        if(isReference())
                            return new ReferenceConverter();
                        else
                            return new NodeConverter();
                    }
                }

                if(isAllElementMatch()) {
                    return new NodeByTypeConverter(itemType);
                }

                printError(p.decl(), "I don't know how to inject "+itemType+" from configuration");
                return new NodeConverter(); // error recovery
            }

            /**
             * Encapsulates the source value representation in {@link Dom}.
             */
            abstract class Converter {
                /**
                 * Generates an expression that converts 'rhs'.
                 *
                 * @param targetType
                 *      The expected type of the expression, so that the generated expression
                 *      can contain cast operation if necessary.
                 */
                abstract JExpression as(JExpression rhs, TypeMirror targetType);
                /**
                 * Source value type as returned by {@link Dom}.
                 */
                abstract JClass sourceType();

                /**
                 * True if the XML representation of the source value is a leaf (string value)
                 * as opposed to node (an XML fragment.)
                 */
                abstract boolean isLeaf();
                
                abstract void addMetadata(String key,TypeMirror itemType);

                protected final String makeCollectionIfNecessary(String s) {
                    if(packer!=null)    return "collection:"+s;
                    else                return s;
                }
            }

            class LeafConverter extends Converter {
                JExpression as(JExpression rhs, TypeMirror targetType) {
                    return math.SIMPLE_VALUE_CONVERTER.visit(targetType, rhs);
                }
                JClass sourceType() {
                    return cm.ref(String.class);
                }

                boolean isLeaf() {
                    return true;
                }

                void addMetadata(String key,TypeMirror itemType) {
                    metadata.add(key,makeCollectionIfNecessary("leaf"));
                }
            }

            class NodeConverter extends Converter {
                JExpression as(JExpression rhs, TypeMirror targetType) {
                    return JExpr.cast(TO_JTYPE.visit(targetType,null),rhs.invoke("get"));
                }
                JClass sourceType() {
                    return cm.ref(Dom.class);
                }

                boolean isLeaf() {
                    return false;
                }

                void addMetadata(String key,TypeMirror itemType) {
                    metadata.add(key,makeCollectionIfNecessary(itemType.toString()));
                }
            }

            class NodeByTypeConverter extends Converter {
                final JClass sourceType;

                NodeByTypeConverter(TypeMirror sourceType) {
                    this.sourceType = TO_JTYPE.visit(sourceType,null).boxify();
                }

                JExpression as(JExpression rhs, TypeMirror targetType) {
                    return rhs;
                }
                JClass sourceType() {
                    return sourceType;
                }
                boolean isLeaf() {
                    return false;
                }
                void addMetadata(String key,TypeMirror itemType) {
                    // TODO: we need to indicate that there's open-ended match here
                }
            }

            class ReferenceConverter extends Converter {
                JExpression as(JExpression rhs, TypeMirror targetType) {
                    return JExpr.invoke("reference").arg($dom).arg(rhs).arg(TO_JTYPE.visit(targetType,null).boxify().dotclass());
                }
                JClass sourceType() {
                    return cm.ref(String.class);
                }

                boolean isLeaf() {
                    return true;
                }

                void addMetadata(String key,TypeMirror itemType) {
                    metadata.add(key,makeCollectionIfNecessary("leaf"));
                    metadata.add(key, "reference");
                }
            }
        }


        private final class AttributeMethodGenerator extends MethodGenerator {
            private final Attribute a;

            private AttributeMethodGenerator(Property p, Attribute a) {
                super("attribute_", injectAttributeMethod, p, a.value());
                this.a = a;
            }

            protected String xmlTokenName() {
                return '@'+xmlName;
            }

            protected boolean isRequired() {
                return a.required();
            }

            protected boolean isReference() {
                return a.reference();
            }

            protected boolean isVariableExpansion() {
                return a.variableExpansion();
            }

            protected boolean isAllElementMatch() {
                return false;
            }

            protected boolean hasDefault() {
                boolean noDefaultValue = 
                    a.defaultValue().length() == 1 && a.defaultValue().charAt(0) == '\u0000';
                return (!noDefaultValue);
            }
            /**
             * Generates the injector that reads an attribute and sets the value.
             */
            @Override
            protected void generate() {
                metadata.add(xmlTokenName(),isRequired()?"required":"optional");
                if (this.hasDefault()) {
                    if (a.defaultValue().indexOf(',')!=-1) {
                        metadata.add(xmlTokenName(), '"' + "default:" + a.defaultValue() + '"');
                    } else {
                        metadata.add(xmlTokenName(), "default:" + a.defaultValue());                        
                    }
                }
                String ant = "";
                try {
                    Class c = a.dataType();
                } catch(MirroredTypeException me) { //hack?
                     ant = getCanonicalTypeFrom(me);
                }
                if (ant.length() == 0) { //take it from the return type of method
                    Property.Method m = (Property.Method)p; // Method needn't be Property's inner class
                    String typeReturnedByMethodDecl = m.decl.getReturnType().toString();
                    metadata.add(xmlTokenName(), "datatype:" + typeReturnedByMethodDecl);
                } else {
                    metadata.add(xmlTokenName(), "datatype:" + ant);
                }
                super.generate();
            }

            protected JExpression getXmlValue() {
                if(!isVariableExpansion() && packer!=null) {
                    printError(p.decl(), "collection attribute property is inconsistent with variableExpansion=false");
                }
                return invokeDom(isVariableExpansion()?"attribute":"rawAttribute").arg(xmlName);
            }
        }
        
        private final class ElementMethodGenerator extends MethodGenerator {
            private final org.jvnet.hk2.config.Element e;
            private ElementMethodGenerator(Property p, org.jvnet.hk2.config.Element e) {
                super("element_", injectElementMethod, p, e.value());
                this.e = e;
            }

            protected String xmlTokenName() {
                return '<'+xmlName+'>';
            }

            protected JExpression getXmlValue() {
                String name;
                if(conv.isLeaf()) {
                    if(isVariableExpansion())
                        name = "leafElement";
                    else
                        name = "rawLeafElement";
                } else {
                    assert isVariableExpansion();   // this error is checked earlier.
                    if(xmlName.equals("*")) {
                        return invokeDom("nodeByTypeElement").arg(TO_JTYPE.visit(itemType, (Void) null).boxify().dotclass());
                    } else
                        name = "nodeElement";
                }

                return invokeDom(name).arg(xmlName);
            }

            @Override
            protected void generate() {
                super.generate();
                if (packer==null) {
                    for (AnnotationMirror am : p.decl().getAnnotationMirrors()) {
                        if (!am.toString().contains("hk2"))
                            metadata.add(xmlTokenName(), am.toString());
                    }
                }
            }

            protected boolean isRequired() {
                return e.required();
            }

            protected boolean isReference() {
                return e.reference();
            }

            protected boolean isVariableExpansion() {
                return e.variableExpansion();
            }

            protected boolean isAllElementMatch() {
                return e.value().equals("*");
            }
        }
    }

    private static String getCanonicalTypeFrom(MirroredTypeException me) {
        TypeMirror tm = me.getTypeMirror();
        if (tm instanceof DeclaredType) {
            DeclaredType dec = (DeclaredType) tm;
            String qn = dec.toString(); //TODO: Was dec.getDeclaration().getQualifiedName()
            return ( qn );
        }
        return "";  //ok?
    }
    private TypeMirror erasure(TypeMirror type) {
        return env.getTypeUtils().erasure(type);
    }

    /**
     * Takes {@link TypeMirror} and returns the corresponding {@link JType}.
     */
    final SimpleTypeVisitor6<JType,Void> TO_JTYPE = new SimpleTypeVisitor6<JType, Void>() {

        @Override
        public JType visitPrimitive(PrimitiveType type, Void param) {
            switch (type.getKind()) {
            case BOOLEAN:   return cm.BOOLEAN;
            case BYTE:      return cm.BYTE;
            case CHAR:      return cm.CHAR;
            case DOUBLE:    return cm.DOUBLE;
            case FLOAT:     return cm.FLOAT;
            case INT:       return cm.INT;
            case LONG:      return cm.LONG;
            case SHORT:     return cm.SHORT;
            }
            throw new AssertionError();
        }

        @Override
        public JType visitArray(ArrayType type, Void param) {
            return type.accept(this, null).array();
        }

        @Override
        public JType visitDeclared(DeclaredType type, Void param) {
            // TODO: generics support
            return cm.ref(type.toString()); //TODO: Double Check. Was cm.ref(type.getDeclaration().getQualifiedName())
        }

        @Override
        public JType visitNoType(NoType type, Void param) {
            return cm.VOID;
        }

        protected JType defaultAction(TypeMirror type, Void param) {
            throw new UnsupportedOperationException();
        }
    };
}
