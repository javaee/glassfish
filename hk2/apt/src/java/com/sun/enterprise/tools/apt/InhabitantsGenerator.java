package com.sun.enterprise.tools.apt;

import com.sun.hk2.component.InhabitantsFile;
import com.sun.mirror.apt.AnnotationProcessor;
import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.apt.RoundCompleteEvent;
import com.sun.mirror.apt.RoundCompleteListener;
import com.sun.mirror.declaration.AnnotationMirror;
import com.sun.mirror.declaration.AnnotationTypeDeclaration;
import com.sun.mirror.declaration.AnnotationTypeElementDeclaration;
import com.sun.mirror.declaration.AnnotationValue;
import com.sun.mirror.declaration.ClassDeclaration;
import com.sun.mirror.declaration.InterfaceDeclaration;
import com.sun.mirror.declaration.TypeDeclaration;
import com.sun.mirror.type.InterfaceType;
import com.sun.mirror.type.MirroredTypeException;
import com.sun.mirror.type.TypeMirror;
import com.sun.mirror.util.DeclarationVisitor;
import com.sun.mirror.util.DeclarationVisitors;
import com.sun.mirror.util.SimpleDeclarationVisitor;
import org.jvnet.hk2.annotations.Contract;
import org.jvnet.hk2.annotations.ContractProvided;
import org.jvnet.hk2.annotations.Index;
import org.jvnet.hk2.annotations.InhabitantAnnotation;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.InhabitantMetadata;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map;

/**
 * Generates <tt>/META-INF/inhabitants/*</tt>
 * 
 * @author Kohsuke Kawaguchi
 */
public class InhabitantsGenerator implements AnnotationProcessor, RoundCompleteListener {
    private final AnnotationProcessorEnvironment env;
    private final boolean debug;
    private final DescriptorList list;

    private final InhabitantMetadataProcessor inhabitantMetadataProcessor = new InhabitantMetadataProcessor();

    public InhabitantsGenerator(AnnotationProcessorEnvironment env, DescriptorList list) {
        this.env = env;
        debug = env.getOptions().containsKey("-Adebug");
        env.addListener(this);

        this.list = list;
    }


    public void process() {
        // env.getTypeDeclarations() only contain types that are newly introduced in the round
        DeclarationVisitor scanner = DeclarationVisitors.getDeclarationScanner(
            new ListClassVisitor(),
            DeclarationVisitors.NO_OP);
        for (TypeDeclaration typeDecl : env.getTypeDeclarations())
            typeDecl.accept(scanner);
    }

    /**
     * Defer the output till the very end, because otherwise we'll end up writing
     * files multiple times, which APT doesn't like.
     *
     * But APT also doesn't like us creating files after the rounds are completed,
     * so we need a hack to figure out where the output directory is, and use that.
     */
    public void roundComplete(RoundCompleteEvent event) {
        if(!event.getRoundState().finalRound())
            return;

        list.write(env);
    }

    private class ListClassVisitor extends SimpleDeclarationVisitor {
        /**
         * Contract indices found in various methods of this class
         * gets accumulated here.
         */
        private final List<String> indices = new ArrayList<String>();

        /**
         * {@link InterfaceType}s whose contracts are already checked.
         */
        private final Set<InterfaceDeclaration> checkedInterfaces = new HashSet<InterfaceDeclaration>();

        public void visitClassDeclaration(ClassDeclaration d) {
            if (debug) {
                env.getMessager().printNotice("Visiting " + d.getQualifiedName());
            }

            // check if this class has annotation that has InhabitantAnnotation meta-annotation.
            InhabitantAnnotation ia=null;
            AnnotationMirror a=null;
            for (AnnotationMirror am : d.getAnnotationMirrors()) {
                ia = am.getAnnotationType().getDeclaration().getAnnotation(InhabitantAnnotation.class);
                if(ia!=null) {
                    a = am;
                    break;
                }
            }
            if(ia==null) {
                // doesn't have any component annotation.
                // in case annotations are removed from them, update the descriptors
                for (InhabitantsDescriptor id : list.descriptors.values())
                    id.remove(d.getQualifiedName());
                return;
            }

            if (debug) {
                env.getMessager().printNotice("Found component annotation " + a + " on "+d.getQualifiedName());
            }
            
            // update the descriptor
            InhabitantsDescriptor descriptor = list.get(ia.value());
            descriptor.put(d.getQualifiedName(),getInhabitantDeclaration(a,d));
        }

        /**
         * Computes the metadata line for the given class declaration. 
         *
         * @param a
         *      The annotation which is meta-annotated with {@link InhabitantAnnotation}.
         *      Used to extract the index.
         */
        public String getInhabitantDeclaration(AnnotationMirror a, ClassDeclaration d) {
            indices.clear();
            checkedInterfaces.clear();

            // check for Contract supertypes.
            String name = geIndexValue(a);
            for (TypeDeclaration t : ContractFinder.find(d)) {
                enforceContractLevelScope(t,d);
                addIndex(t.getQualifiedName(),name);
            }

            // check for meta annotations
            for(AnnotationMirror am : d.getAnnotationMirrors()) {
                AnnotationTypeDeclaration atd = am.getAnnotationType().getDeclaration();
                Contract c = atd.getAnnotation(Contract.class);
                if(c!=null) {
                    // this is a contract annotation
                    enforceContractLevelScope(atd,d);
                    addIndex(atd.getQualifiedName(), geIndexValue(am));
                }
            }

            StringBuilder buf = new StringBuilder();
            buf.append(InhabitantsFile.CLASS_KEY).append('=').append(d.getQualifiedName());
            for (String contract : indices)
                buf.append(',').append(InhabitantsFile.INDEX_KEY).append('=').append(contract);

            findInhabitantMetadata(d, buf);

            // TODO: should be deprecated and replaced with InhabitantMetadata
            String metadata = getStringValue(a,"metadata");
            if(metadata!=null && metadata.length()>0)
                buf.append(',').append(metadata);

            return buf.toString();
        }

        /**
         * Locates all {@link InhabitantMetadata} and add to the metadata.
         */
        private void findInhabitantMetadata(ClassDeclaration d, StringBuilder buf) {
            for (Map.Entry<String, String> e : inhabitantMetadataProcessor.process(d).entrySet())
                buf.append(',').append(e.getKey()).append('=').append(e.getValue());
        }

        private String geIndexValue(AnnotationMirror a) {
            AnnotationTypeDeclaration decl = a.getAnnotationType().getDeclaration();
            for(AnnotationTypeElementDeclaration e : decl.getMethods()) {
                if(e.getAnnotation(Index.class)!=null) {
                    AnnotationValue v = a.getElementValues().get(e);
                    if(v!=null) // explicitly given
                        return v.getValue().toString();
                    else // defaulted
                        return e.getDefaultValue().getValue().toString();
                }
            }
            return null;
        }

        /**
         * Given a use of annotation like <tt>@abc(def="ghi",jkl="mno")</tt> and a property name 'def',
         * obtain the value of the annotation (in this example "ghi")
         */
        private String getStringValue(AnnotationMirror a, String name) {
            AnnotationTypeDeclaration decl = a.getAnnotationType().getDeclaration();
            for(AnnotationTypeElementDeclaration e : decl.getMethods()) {
                if(e.getSimpleName().equals(name)) {
                    AnnotationValue v = a.getElementValues().get(e);
                    if(v!=null) // explicitly given
                        return v.getValue().toString();
                    else // defaulted
                        return e.getDefaultValue().getValue().toString();
                }
            }
            return null;
        }


        private void addIndex(String primary, String secondary) {
            if(secondary==null || secondary.length()==0)
                indices.add(primary);    // unnamed
            else
                indices.add(primary+':'+secondary); // named
        }

        /**
         * Records the contract&lt;->service relationship.
         *
         * @param ctrct
         *      The contract type declaration. Null in case of {@link ContractProvided}.
         * @param impl
         *      Implementation class.
         */
        private void enforceContractLevelScope(TypeDeclaration ctrct, ClassDeclaration impl) {
            // if @Scoped is on the contract, that means we are forcing a certain scope type.
            Scoped s = ctrct.getAnnotation(Scoped.class);
            if(s==null)     return;

            try {
                s.value();
                throw new AssertionError();
            } catch (MirroredTypeException e) {
                TypeMirror forcedScope = e.getTypeMirror();

                s = impl.getAnnotation(Scoped.class);
                if(s!=null) {
                    try {
                        s.value();
                        throw new AssertionError();
                    } catch (MirroredTypeException f) {
                        if(forcedScope.equals(f.getTypeMirror()))
                            return; // forced scope and the actual scope are consistent
                    }
                }

                env.getMessager().printError(impl.getPosition(),"@Scoped("+forcedScope+") is required because of the contract "+ctrct.getQualifiedName());
            }
        }
    }
}
