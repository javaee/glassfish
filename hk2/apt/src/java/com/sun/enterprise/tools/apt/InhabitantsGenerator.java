/*
 * 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2007-2008 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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
package com.sun.enterprise.tools.apt;

import com.sun.hk2.component.InhabitantsFile;
import com.sun.hk2.component.CompanionSeed;
import static com.sun.hk2.component.InhabitantsFile.COMPANION_CLASS_METADATA_KEY;
import static com.sun.hk2.component.InhabitantsFile.INDEX_KEY;
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
import com.sun.mirror.declaration.Declaration;
import com.sun.mirror.type.InterfaceType;
import com.sun.mirror.type.MirroredTypeException;
import com.sun.mirror.type.TypeMirror;
import com.sun.mirror.type.ClassType;
import com.sun.mirror.util.DeclarationVisitor;
import com.sun.mirror.util.DeclarationVisitors;
import com.sun.mirror.util.SimpleDeclarationVisitor;
import org.jvnet.hk2.annotations.Contract;
import org.jvnet.hk2.annotations.ContractProvided;
import org.jvnet.hk2.annotations.Index;
import org.jvnet.hk2.annotations.InhabitantAnnotation;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.InhabitantMetadata;
import org.jvnet.hk2.annotations.CompanionOf;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.lang.annotation.Annotation;

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

        private AnnotationMirror find(Declaration d, Class<? extends Annotation> annType) {
            for(AnnotationMirror am : d.getAnnotationMirrors()) {
                if(am.getAnnotationType().getDeclaration().getQualifiedName().equals(annType.getName()))
                    return am;
            }
            return null;
        }

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

            // check for contract annotations
            for(AnnotationMirror am : d.getAnnotationMirrors()) {
                AnnotationTypeDeclaration atd = am.getAnnotationType().getDeclaration();
                Contract c = atd.getAnnotation(Contract.class);
                if(c!=null) {
                    // this is a contract annotation
                    enforceContractLevelScope(atd,d);
                    addIndex(atd.getQualifiedName(), geIndexValue(am));
                }

                // check for meta-annotations
                for(AnnotationMirror mam : atd.getAnnotationMirrors()) {
                    AnnotationTypeDeclaration matd = mam.getAnnotationType().getDeclaration();
                    Contract mc = matd.getAnnotation(Contract.class);
                    if(mc!=null)
                        // meta-contract annotation
                        addIndex(matd.getQualifiedName(), geIndexValue(mam));
                }
            }

            StringBuilder buf = new StringBuilder();
            buf.append(InhabitantsFile.CLASS_KEY).append('=').append(d.getQualifiedName());
            for (String contract : indices)
                addMetadata(buf, INDEX_KEY, contract);

            findInhabitantMetadata(d, buf);

            // for seed, capture the metadata for the actual companion object
            CompanionSeed seed = d.getAnnotation(CompanionSeed.class);
            if(seed!=null) {
                try {
                    seed.companion();
                } catch (MirroredTypeException e) {
                    ClassDeclaration companion = ((ClassType) e.getTypeMirror()).getDeclaration();
                    addMetadata(buf, COMPANION_CLASS_METADATA_KEY,
                        quote(getInhabitantDeclaration(find(companion, CompanionOf.class),companion)));
                }
            }

            // TODO: should be deprecated and replaced with InhabitantMetadata
            String metadata = getStringValue(a,"metadata");
            if(metadata!=null && metadata.length()>0)
                buf.append(',').append(metadata);

            return buf.toString();
        }

        private void addMetadata(StringBuilder buf, String key, String value) {
            buf.append(',').append(key).append('=').append(value);
        }

        private String quote(String s) {
            StringBuilder buf = new StringBuilder();
            buf.append('"');
            for( int i=0; i<s.length(); i++ ) {
                char ch=s.charAt(i);
                switch(ch) {
                case '\\':
                    buf.append("\\\\");
                    break;
                case '"':
                    buf.append("\\\"");
                    break;
                default:
                    buf.append(ch);
                }
            }
            buf.append('"');
            return buf.toString();
        }

        /**
         * Locates all {@link InhabitantMetadata} and add to the metadata.
         */
        private void findInhabitantMetadata(ClassDeclaration d, StringBuilder buf) {
            for (Map.Entry<String, String> e : inhabitantMetadataProcessor.process(d).entrySet())
                buf.append(',').append(e.getKey()).append('=').append(e.getValue());
        }

        /**
         * Finds the value of the annotation element annotated with {@link Index}.
         */
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
