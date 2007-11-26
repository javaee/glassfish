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
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;
import com.sun.codemodel.JInvocation;
import com.sun.enterprise.tools.apt.ContractFinder;
import com.sun.istack.tools.APTTypeVisitor;
import com.sun.mirror.apt.AnnotationProcessor;
import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.declaration.AnnotationTypeDeclaration;
import com.sun.mirror.declaration.ClassDeclaration;
import com.sun.mirror.declaration.Declaration;
import com.sun.mirror.declaration.FieldDeclaration;
import com.sun.mirror.declaration.MethodDeclaration;
import com.sun.mirror.declaration.TypeDeclaration;
import com.sun.mirror.type.ArrayType;
import com.sun.mirror.type.ClassType;
import com.sun.mirror.type.DeclaredType;
import com.sun.mirror.type.InterfaceType;
import com.sun.mirror.type.PrimitiveType;
import com.sun.mirror.type.TypeMirror;
import com.sun.mirror.type.TypeVariable;
import com.sun.mirror.type.VoidType;
import com.sun.mirror.type.WildcardType;
import com.sun.mirror.util.SimpleDeclarationVisitor;
import com.sun.tools.xjc.api.util.FilerCodeWriter;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.MultiMap;
import org.jvnet.hk2.config.ConfigInjector;
import org.jvnet.hk2.config.ConfigMetadata;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.Dom;
import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.Element;
import org.jvnet.hk2.config.InjectionTarget;
import org.jvnet.hk2.config.ConfigBeanProxy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Generates {@link ConfigInjector} implementations for {@link Configured} objects
 * and {@link ConfigBeanProxy} subtypes.
 * 
 * @author Kohsuke Kawaguchi
 */
public class ConfigInjectorGenerator extends SimpleDeclarationVisitor implements AnnotationProcessor {
    private final AnnotationProcessorEnvironment env;
    private JCodeModel cm;

    final TypeMath math;

    public ConfigInjectorGenerator(AnnotationProcessorEnvironment env) {
        this.env = env;
        this.math = new TypeMath(env);
    }

    public void process() {
        cm = new JCodeModel();

        AnnotationTypeDeclaration ann = (AnnotationTypeDeclaration) env.getTypeDeclaration(Configured.class.getName());
        for(Declaration d : env.getDeclarationsAnnotatedWith(ann))
            d.accept(this);

        try {
            cm.build(new FilerCodeWriter(env.getFiler()));
        } catch (IOException e) {
            throw new Error(e);
        }
        cm = null;
    }

    /**
     * For each class annotated with {@link Configured}.
     */
    public void visitClassDeclaration(ClassDeclaration clz) {
        try {
            new ClassGenerator(clz).generate();
        } catch (JClassAlreadyExistsException e) {
            env.getMessager().printError(clz.getPosition(),e.toString());
        }
    }

    /*package*/ class ClassGenerator {
        final ClassDeclaration clz;
        final JDefinedClass injector;
        final JClass targetType;
        final JAnnotationUse service;
        final JMethod injectMethod,injectAttributeMethod,injectElementMethod;
        final MultiMap<String,String> metadata = new MultiMap<String,String>();
        /**
         * Key property that has {@link Element#key()} or {@link Attribute#key()}
         */
        private Property key=null;


        public ClassGenerator(ClassDeclaration clz) throws JClassAlreadyExistsException {
            this.clz = clz;
            Configured c = clz.getAnnotation(Configured.class);

            String name = clz.getQualifiedName();
            targetType = cm.ref(name);

            // [RESULT]
            // @Service(name='...')
            // @InjectionTarget(target)
            // public class XYZInjector extends ConfigInjector<XYZ>
            injector = cm._class(name+"Injector");
            String elementName = c.name();
            if(c.local()) {
                if(elementName.length()>0) {
                    env.getMessager().printError(clz.getPosition(),"@Configured.local and @Configured.name is mutually exclusive");
                    elementName = ""; // error recovery
                }
            } else {
                if(elementName.length()==0) // infer default
                    elementName = NAME_UTIL.toHyphenated(clz.getSimpleName());
            }

            service = injector.annotate(Service.class).param("name",elementName);
            injector.annotate(InjectionTarget.class).param("value",targetType);
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

            metadata.add(ConfigMetadata.TARGET,name);

            // locate additional contracts for the target.
            for (TypeDeclaration t : ContractFinder.find(clz))
                metadata.add(ConfigMetadata.TARGET_CONTRACTS,t.getQualifiedName());
        }

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
            for (FieldDeclaration f : clz.getFields())
                generate(new Property.Field(f));

            for (MethodDeclaration m : clz.getMethods())
                generate(new Property.Method(m));

            service.param("metadata", metadata.toCommaSeparatedString());
        }

        private void generate(Property p) {
            Attribute a = p.getAnnotation(Attribute.class);
            Element e = p.getAnnotation(Element.class);

            if(a!=null) {
                new AttributeMethodGenerator(p,a).generate();
                if(e!=null)
                    env.getMessager().printError(p.decl().getPosition(),"Cannot have both @Element and @Attribute at the same time");
            } else {
                if(e!=null)
                    new ElementMethodGenerator(p,e).generate();
            }

            // Updates #key with error check.
            if(p.isKey()) {
                if(key!=null) {
                    env.getMessager().printError(p.decl().getPosition(),"Multiple key properties");
                    env.getMessager().printError(key.decl().getPosition(),"Another one is at here");
                }
                key = p;
            }
        }

        /**
         * Generates a single injection method, which injects
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

                JMethod m = injector.method(JMod.PUBLIC,void.class, methodNamePrefix+p.seedName());
                $dom = m.param(Dom.class,"dom");
                $target = m.param(targetType,"target");
                body = m.body();

                injectMethod.body().invoke(m).arg($dom).arg($target);

                reinjectionMethod.body()._if(JExpr.lit(this.xmlName).invoke("equals").arg(JExpr.ref("name")))
                    ._then().invoke(m).arg($dom).arg($target);

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

                if(!isVariableExpansion() && TO_JTYPE.apply(itemType,null)!=cm.ref(String.class))
                    env.getMessager().printError(p.decl().getPosition(),"variableExpansion=false is only allowed on String");

                JVar value = var(
                    packer!=null ? cm.ref(List.class).narrow(conv.sourceType()) : conv.sourceType(),getXmlValue());

                if(!isRequired())
                    body._if(value.eq(JExpr._null()))._then()._return();

                if(packer!=null)
                    handleMultiValue(value);
                else
                    assign(conv.as(value,itemType));

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
                metadata.add(ConfigMetadata.KEYED_AS,p.decl().getDeclaringType().getQualifiedName());
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

                TypeMirror mapType = TypeMath.baseClassFinder.apply(type, env.getTypeDeclaration(Map.class.getName()));
                if(mapType!=null) {
                    // T=Map<...>
                    DeclaredType d = (DeclaredType)mapType;
                    Iterator<TypeMirror> itr = d.getActualTypeArguments().iterator();
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
                    this.componentT = TO_JTYPE.apply(itemType(),null);
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
                    this.collectionType = TO_JTYPE.apply(collectionType,null).boxify();
                    this.itemType       = TO_JTYPE.apply(itemType,null).boxify();
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
                    math.SIMPLE_VALUE_CONVERTER.apply(itemType, JExpr._null());
                    return new LeafConverter();
                } catch (UnsupportedOperationException e) {
                    // nope
                }

                // try to handle it as a reference
                if(itemType instanceof ClassType) {
                    ClassDeclaration decl = ((ClassType)itemType).getDeclaration();
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

                env.getMessager().printError(p.decl().getPosition(),
                    "I don't know how to inject "+itemType+" from configuration");
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
                    return math.SIMPLE_VALUE_CONVERTER.apply(targetType, rhs);
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
                    return JExpr.cast(TO_JTYPE.apply(targetType,null),rhs.invoke("get"));
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
                    this.sourceType = TO_JTYPE.apply(sourceType,null).boxify();
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
                    return JExpr.invoke("reference").arg($dom).arg(rhs).arg(TO_JTYPE.apply(targetType,null).boxify().dotclass());
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

            /**
             * Generates the injector that reads an attribute and sets the value.
             */
            protected void generate() {
                metadata.add(xmlTokenName(),isRequired()?"required":"optional");
                super.generate();
            }

            protected JExpression getXmlValue() {
                if(!isVariableExpansion() && packer!=null) {
                    env.getMessager().printError(p.decl().getPosition(),
                        "collection attribute property is inconsistent with variableExpansion=false");
                }
                return invokeDom(isVariableExpansion()?"attribute":"rawAttribute").arg(xmlName);
            }
        }

        private final class ElementMethodGenerator extends MethodGenerator {
            private final Element e;
            private ElementMethodGenerator(Property p, Element e) {
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
                        return invokeDom("nodeByTypeElement").arg(TO_JTYPE.apply(itemType,null).boxify().dotclass());
                    } else
                        name = "nodeElement";
                }

                return invokeDom(name).arg(xmlName);
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

    private TypeMirror erasure(TypeMirror type) {
        return env.getTypeUtils().getErasure(type);
    }

    /**
     * Takes {@link TypeMirror} and returns the corresponding {@link JType}.
     */
    final APTTypeVisitor<JType,Void> TO_JTYPE = new APTTypeVisitor<JType,Void>() {
        protected JType onPrimitiveType(PrimitiveType type, Void param) {
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

        protected JType onArrayType(ArrayType type, Void param) {
            return apply(type.getComponentType(),null).array();
        }

        protected JType onClassType(ClassType type, Void param) {
            // TODO: generics support
            return cm.ref(type.getDeclaration().getQualifiedName());
        }

        protected JType onInterfaceType(InterfaceType type, Void param) {
            // TODO: generics support
            return cm.ref(type.getDeclaration().getQualifiedName());
        }

        protected JType onTypeVariable(TypeVariable type, Void param) {
            throw new UnsupportedOperationException();
        }

        protected JType onVoidType(VoidType type, Void param) {
            return cm.VOID;
        }

        protected JType onWildcard(WildcardType type, Void param) {
            throw new UnsupportedOperationException();
        }
    };

    private static final NameUtil NAME_UTIL = new NameUtil();
}
