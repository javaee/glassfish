package com.sun.enterprise.tools.apt;

import com.sun.hk2.component.CompanionSeed;
import static com.sun.hk2.component.InhabitantsFile.COMPANION_CLASS_KEY;
import com.sun.mirror.apt.AnnotationProcessor;
import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.declaration.AnnotationTypeDeclaration;
import com.sun.mirror.declaration.ClassDeclaration;
import com.sun.mirror.declaration.Declaration;
import com.sun.mirror.util.SimpleDeclarationVisitor;
import com.sun.mirror.type.MirroredTypeException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.tools.xjc.api.util.FilerCodeWriter;
import org.jvnet.hk2.annotations.CompanionOf;

import java.io.IOException;

/**
 * Generates {@link CompanionSeed} classes for each {@link CompanionOf} components.
 * @author Kohsuke Kawaguchi
 */
public class CompanionSeedGenerator extends SimpleDeclarationVisitor implements AnnotationProcessor {
    private final AnnotationProcessorEnvironment env;
    private JCodeModel cm;

    public CompanionSeedGenerator(AnnotationProcessorEnvironment env) {
        this.env = env;
    }

    public void process() {
        cm = new JCodeModel();

        AnnotationTypeDeclaration ann = (AnnotationTypeDeclaration) env.getTypeDeclaration(CompanionOf.class.getName());
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
     * For each class annotated with {@link CompanionOf}.
     */
    public void visitClassDeclaration(ClassDeclaration clz) {
        try {
            CompanionOf companionOf = clz.getAnnotation(CompanionOf.class);
            String lead;
            try {
                companionOf.value();
                throw new AssertionError();
            } catch (MirroredTypeException e) {
                lead = e.getTypeMirror().toString();
            }

            JDefinedClass seed = cm._class(clz.getQualifiedName()+"Seed");
            JAnnotationUse a = seed.annotate(CompanionSeed.class);

            a.param("lead",cm.ref(lead));
            a.param("metadata",COMPANION_CLASS_KEY+'='+clz.getQualifiedName());
        } catch (JClassAlreadyExistsException e) {
            env.getMessager().printError(clz.getPosition(),e.toString());
        }
    }
}
