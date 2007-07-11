package org.jvnet.hk2.config.generator;

import com.envoisolutions.sxc.builder.Builder;
import com.envoisolutions.sxc.builder.ElementParserBuilder;
import com.envoisolutions.sxc.builder.ParserBuilder;
import com.envoisolutions.sxc.builder.impl.BuilderImpl;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JVar;
import com.sun.mirror.apt.AnnotationProcessor;
import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.declaration.AnnotationTypeDeclaration;
import com.sun.mirror.declaration.ClassDeclaration;
import com.sun.mirror.declaration.Declaration;
import com.sun.mirror.declaration.FieldDeclaration;
import com.sun.mirror.declaration.MethodDeclaration;
import com.sun.mirror.type.ClassType;
import com.sun.mirror.type.PrimitiveType;
import com.sun.mirror.type.TypeMirror;
import com.sun.mirror.util.SimpleDeclarationVisitor;
import com.sun.mirror.util.SimpleTypeVisitor;
import com.sun.mirror.util.SourcePosition;
import com.sun.tools.xjc.api.util.FilerCodeWriter;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.FromAttribute;
import org.jvnet.hk2.config.FromElement;
import org.jvnet.hk2.config.ReaderEx;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author Kohsuke Kawaguchi
 */
public class AnnotationProcessorImpl extends SimpleDeclarationVisitor implements AnnotationProcessor {
    private final AnnotationProcessorEnvironment env;

    public AnnotationProcessorImpl(AnnotationProcessorEnvironment env) {
        this.env = env;
    }

    public void process() {
        AnnotationTypeDeclaration ann = (AnnotationTypeDeclaration) env.getTypeDeclaration(Configured.class.getName());
        for(Declaration d : env.getDeclarationsAnnotatedWith(ann))
            d.accept(this);
    }

    /**
     * For each class annotated with {@link Configured}.
     */
    public void visitClassDeclaration(ClassDeclaration clz) {
        Configured c = clz.getAnnotation(Configured.class);

        String name = clz.getQualifiedName();

        Builder builder = new BuilderImpl(name+"Reader",null,null);
        builder.setReaderBaseClass(ReaderEx.class);
        JCodeModel cm = builder.getCodeModel();
        ElementParserBuilder root = builder.getParserBuilder();

        ElementParserBuilder b = root.expectElement(new QName(c.name()));
        JClass target = cm.ref(name);
        JVar $target = b.getBody().decl(target, "result", JExpr._new(target));
        b.getBody().add(JExpr._this().invoke("inject").arg(b.getXSR()).arg($target));
        b.getBody()._return($target);

        Set<Property> props = new LinkedHashSet<Property>();

        for (FieldDeclaration f : clz.getFields()) {
            Property p = Property.createField(env,f);
            if(handleAttribute(p,b,$target)|handleElement(p,b,$target))
                props.add(p);
        }

        for (MethodDeclaration m : clz.getMethods()) {
            Property p = new Property.Method(m);
            if(handleAttribute(p,b,$target)|handleElement(p,b,$target))
                props.add(p);
        }

        for (Property p : props) {
            p.pre(cm,b.getBody().getBlock());
            p.post(cm,b.getTailBlock());
        }

        try {
            builder.write(new FilerCodeWriter(env.getFiler()));
        } catch (IOException e) {
            throw new Error(e);
        }
    }

    private boolean handleElement(Property p, ElementParserBuilder b, JVar $target) {
        FromElement a = p.getAnnotation(FromElement.class);
        if(a==null)     return false; // not an attribute

        ElementParserBuilder child = b.expectElement(new QName(p.inferName(a.value())));

        // try to handle it as a reference
        TypeMirror t = p.type();
        if(t instanceof ClassType) {
            ClassDeclaration decl = ((ClassType) t).getDeclaration();
            Configured cfg = decl.getAnnotation(Configured.class);
            if(cfg!=null) {
                // load from structure
                JCodeModel cm = child.getCodeModel();
                p.assign(child.passParentVariable($target),
                    child.getBody().getBlock(),
                    // the read1 method is always the one that loads the object
                    JExpr.cast(cm.ref(decl.getQualifiedName()),
                        JExpr._new(cm.ref(decl.getQualifiedName()+"Reader")).
                            arg(JExpr.ref("context")).invoke("read1").arg(child.getXSR()).arg(JExpr.ref("properties"))
                        ));
                return true;
            }
        }

        handleSimpleValue(child, $target, p);
        return true;
    }

    /**
     * Generates the handler that reads an attribute and sets the value.
     */
    private boolean handleAttribute(Property p, ElementParserBuilder b, JVar $target) {
        FromAttribute a = p.getAnnotation(FromAttribute.class);
        if(a==null)     return false; // not an attribute

        String name = p.inferName(a.value());
        handleSimpleValue(b.expectAttribute(new QName(name)), $target, p);
        return true;
    }

    /**
     * Completes the handler that reads a simple text value.
     */
    private void handleSimpleValue(ParserBuilder pb, JVar $target, Property p) {
        $target = pb.passParentVariable($target);
        p.assign($target, pb.getBody().getBlock(), pb.as(toSimpleClass(p.type(),p.decl().getPosition())));
    }


    /**
     * Determines the Class object from the given type
     */
    private Class<?> toSimpleClass(TypeMirror type, final SourcePosition position) {
        final Class[] r = new Class[1];
        env.getTypeUtils().getErasure(type).accept(new SimpleTypeVisitor() {
            public void visitPrimitiveType(PrimitiveType p) {
                switch (p.getKind()) {
                case BOOLEAN:       r[0]=boolean.class;break;
                case BYTE:          r[0]=byte.class;break;
                case CHAR:          r[0]=char.class;break;
                case DOUBLE:        r[0]=double.class;break;
                case FLOAT:         r[0]=float.class;break;
                case INT:           r[0]=int.class;break;
                case LONG:          r[0]=long.class;break;
                case SHORT:         r[0]=short.class;break;
                }
            }

            public void visitClassType(ClassType classType) {
                String name = classType.getDeclaration().getQualifiedName();
                try {
                    r[0] = Class.forName(name);
                } catch (ClassNotFoundException e) {
                    env.getMessager().printError(position,"Not a valid type for attribute "+name);
                }
            }
        });

        // TODO: improve error handling when r[0]==null
        return r[0];
    }
}
