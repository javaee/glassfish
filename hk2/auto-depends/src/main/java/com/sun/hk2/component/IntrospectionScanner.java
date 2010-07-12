package com.sun.hk2.component;

import org.glassfish.hk2.classmodel.reflect.*;
import org.jvnet.hk2.annotations.Contract;
import org.jvnet.hk2.annotations.InhabitantAnnotation;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.MultiMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Inhabitant scanner based on introspection information rather than statically
 * generated inhabitant file
 */
public class IntrospectionScanner implements Iterable<InhabitantParser> {

    final Iterator<AnnotatedElement> inhabitantAnnotations;
    Iterator<AnnotatedElement> current;

    public IntrospectionScanner(ParsingContext context) {
        AnnotationType am = context.getTypes().getBy(AnnotationType.class, InhabitantAnnotation.class.getName());
        if (am==null) {                                  
            inhabitantAnnotations = (new ArrayList<AnnotatedElement>()).iterator();
        } else {
            Collection<AnnotatedElement> ccc = am.allAnnotatedTypes();
            inhabitantAnnotations = ccc.iterator();
        }
        fetch();
    }

    /*
     * puts current in the first non empty iterator.
     */
    private void fetch() {
        if (!inhabitantAnnotations.hasNext()) {
            current = (new ArrayList<AnnotatedElement>()).iterator();
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
