/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 *  Copyright 2010 Sun Microsystems, Inc. All rights reserved.
 *
 *  The contents of this file are subject to the terms of either the GNU
 *  General Public License Version 2 only ("GPL") or the Common Development
 *  and Distribution License("CDDL") (collectively, the "License").  You
 *  may not use this file except in compliance with the License. You can obtain
 *  a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 *  or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 *  language governing permissions and limitations under the License.
 *
 *  When distributing the software, include this License Header Notice in each
 *  file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 *  Sun designates this particular file as subject to the "Classpath" exception
 *  as provided by Sun in the GPL Version 2 section of the License file that
 *  accompanied this code.  If applicable, add the following below the License
 *  Header, with the fields enclosed by brackets [] replaced by your own
 *  identifying information: "Portions Copyrighted [year]
 *  [name of copyright owner]"
 *
 *  Contributor(s):
 *
 *  If you wish your version of this file to be governed by only the CDDL or
 *  only the GPL Version 2, indicate your decision by adding "[Contributor]
 *  elects to include this software in this distribution under the [CDDL or GPL
 *  Version 2] license."  If you don't indicate a single choice of license, a
 *  recipient has the option to distribute your version of this file under
 *  either the CDDL, the GPL Version 2 or to extend the choice of license to
 *  its licensees as provided above.  However, if you add GPL Version 2 code
 *  and therefore, elected the GPL Version 2 license, then the option applies
 *  only if the new code is made subject to such option by the copyright
 *  holder.
 */
package com.sun.hk2.component;

import org.glassfish.hk2.classmodel.reflect.*;
import org.jvnet.hk2.annotations.Contract;
import org.jvnet.hk2.annotations.InhabitantAnnotation;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.MultiMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Inhabitant scanner based on introspection information rather than statically
 * generated inhabitant file
 */
public class InhabitantIntrospectionScanner implements Iterable<InhabitantParser> {

    final Iterator<AnnotatedElement> inhabitantAnnotations;
    Iterator<AnnotatedElement> current;

    @SuppressWarnings("unchecked")
    public InhabitantIntrospectionScanner(ParsingContext context) {
        Types types = context.getTypes();
        AnnotationType am = types.getBy(AnnotationType.class, InhabitantAnnotation.class.getName());
        if (am==null) {                                  
            inhabitantAnnotations = Collections.EMPTY_LIST.iterator();
        } else {
            Collection<AnnotatedElement> ccc = am.allAnnotatedTypes();
            inhabitantAnnotations = ccc.iterator();
        }
        fetch();
    }

    /*
     * puts current in the first non empty iterator.
     */
    @SuppressWarnings("unchecked")
    private void fetch() {
        if (!inhabitantAnnotations.hasNext()) {
            current = Collections.EMPTY_LIST.iterator();
            return;
        }
        do {
            AnnotationType am = AnnotationType.class.cast(inhabitantAnnotations.next());
            current = am.allAnnotatedTypes().iterator();
        } while (!current.hasNext() && inhabitantAnnotations.hasNext());
    }

    public boolean isContract(AnnotatedElement type) {
        // must be annotated with @Contract
        return type.getAnnotation(Contract.class.getName())!=null;
    }

    public void findClassContracts(ClassModel cm, List<InterfaceModel> interfaces) {
        for (InterfaceModel im : cm.getInterfaces()) {
            if (isContract(im)) {
                interfaces.add(im);
            }
        }
        findContractsFromAnnotations(cm, interfaces);
    }

    public void findInterfaceContracts(InterfaceModel im, List<InterfaceModel> interfaces) {
        if (im.getParent()!=null) {
            findInterfaceContracts(im.getParent(), interfaces);
        }
        if (isContract(im)) {
            interfaces.add(im);
        }
        findContractsFromAnnotations(im, interfaces);
    }

    public void findContractsFromAnnotations(AnnotatedElement ae, List<InterfaceModel> interfaces) {

        for (AnnotationModel am : ae.getAnnotations()) {
            findInterfaceContracts(am.getType(), interfaces);
        }
    }

    public void findContracts(ClassModel cm, List<InterfaceModel> interfaces) {
        for (InterfaceModel im : cm.getInterfaces()) {
            if (isContract(im)) {
                interfaces.add(im);
            }
        }
        findContractsFromAnnotations(cm, interfaces);
    }

    public Iterator<InhabitantParser> iterator() {
        return new Iterator<InhabitantParser>() {
            public boolean hasNext() {
                return current.hasNext();
            }

            public InhabitantParser next() {
                final AnnotatedElement ae = current.next();
                InhabitantParser ip = new InhabitantParser() {
                    public Iterable<String> getIndexes() {
                        if (ae instanceof ClassModel) {
                            final ClassModel cm = (ClassModel) ae;

                            final List<InterfaceModel> implInterfaces = new ArrayList<InterfaceModel>();
                            findContracts(cm, implInterfaces);

                            final Iterator<InterfaceModel> interfaces = implInterfaces.iterator();
                            return new Iterable<String>() {
                                public Iterator<String> iterator() {
                                    return new Iterator<String>() {
                                        public boolean hasNext() {
                                            return interfaces.hasNext();
                                        }

                                        public String next() {
                                            final AnnotationModel am = cm.getAnnotation(Service.class.getName());
                                            String contract = interfaces.next().getName();
                                            String name = (String) am.getValues().get("name");
                                            if (name==null || name.isEmpty()) {
                                                return contract;
                                            } else {
                                                return contract+":"+name;
                                            }
                                        }

                                        public void remove() {
                                            throw new UnsupportedOperationException();
                                        }
                                    };
                                }
                            };

                        }
                        // this should be an error...
                        return (new ArrayList<String>());
                    }

                    public String getImplName() {
                        return ae.getName();
                    }

                    public void setImplName(String name) {
                        throw new UnsupportedOperationException();
                    }

                    public String getLine() {
                        return null;  //To change body of implemented methods use File | Settings | File Templates.
                    }

                    public void rewind() {
                        //To change body of implemented methods use File | Settings | File Templates.
                    }

                    public MultiMap<String, String> getMetaData() {
                        return new MultiMap<String, String>();
                    }
                };
                if (!current.hasNext()) {
                    fetch();
                }
                return ip;
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }
}
