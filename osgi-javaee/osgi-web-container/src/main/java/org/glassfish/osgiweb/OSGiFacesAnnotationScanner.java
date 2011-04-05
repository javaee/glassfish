/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2009-2010 Oracle and/or its affiliates. All rights reserved.
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


package org.glassfish.osgiweb;

import com.sun.faces.spi.AnnotationProvider;
import org.glassfish.hk2.classmodel.reflect.*;

import javax.faces.bean.ManagedBean;
import javax.faces.component.FacesComponent;
import javax.faces.component.behavior.FacesBehavior;
import javax.faces.convert.FacesConverter;
import javax.faces.event.NamedEvent;
import javax.faces.render.FacesBehaviorRenderer;
import javax.faces.render.FacesRenderer;
import javax.faces.validator.FacesValidator;
import javax.servlet.ServletContext;
import java.lang.annotation.Annotation;
import java.net.URI;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class OSGiFacesAnnotationScanner extends AnnotationProvider {
    private static Logger logger = Logger.getLogger(OSGiFacesAnnotationScanner.class.getPackage().getName());

    /**
     * Creates a new <code>AnnotationScanner</code> instance.
     * <p/>
     * This is a much needed constructor as mojarra initializes using this constructor.
     *
     * @param sc the <code>ServletContext</code> for the application to be
     *           scanned
     */
    public OSGiFacesAnnotationScanner(ServletContext sc) {
        super(sc);
    }

    @Override
    public Map<Class<? extends Annotation>, Set<Class<?>>> getAnnotatedClasses(Set<URI> uris) {
        Map<Class<? extends Annotation>, Set<Class<?>>> result =
                (Map<Class<? extends Annotation>, Set<Class<?>>>) sc.getAttribute(Constants.FACES_ANNOTATED_CLASSES);
        sc.setAttribute(Constants.FACES_ANNOTATED_CLASSES, null); // clear it
        if (result == null) {
            logger.warning("Faces annotation parsing has not taken place");
            result = Collections.emptyMap();
        }
        return result;
    }

    /* package */

    static Map<Class<? extends Annotation>, Set<Class<? extends Object>>> scan(Collection<URI> uris, Types types, ClassLoader cl) {
        // can't use ServletContext here, because it is not yet available as this method is called
        // from WebModuleDecorator which is called when WebModule is being created.
        // hence this is a static method.
        Map<Class<? extends Annotation>, Set<Class<? extends Object>>> result =
                new HashMap<Class<? extends Annotation>, Set<Class<? extends Object>>>();
        Class<? extends Annotation>[] annotations = getAnnotationTypes();
        if (annotations == null) return result;
        int total = 0;
        for (Class<? extends Annotation> annotationType : annotations) {
            Type type = types.getBy(annotationType.getName());
            if (type instanceof AnnotationType) {
                Collection<AnnotatedElement> elements = ((AnnotationType) type).allAnnotatedTypes();
                for (AnnotatedElement element : elements) {
                    Type t = (element instanceof Member ? ((Member) element).getDeclaringType() : (Type) element);
                    if (t.wasDefinedIn(uris)) {
                        Set<Class<? extends Object>> classes = result.get(annotationType);
                        if (classes == null) {
                            classes = new HashSet<Class<? extends Object>>();
                            result.put(annotationType, classes);
                        }
                        try {
                            final Class<?> aClass = cl.loadClass(t.getName());
                            logger.info(aClass + " contains " + annotationType);
                            total++;
                            classes.add(aClass);
                        } catch (ClassNotFoundException e) {
                            logger.log(Level.WARNING, "Not able to load " + t.getName(), e);
                        }
                    }
                }
            }
        }
        logger.info("total number of classes with faces annotation = " + total); // TODO(Sahoo): change to finer
        return result;
    }

    private static Class<Annotation>[] getAnnotationTypes() {
        HashSet<Class<? extends Annotation>> annotationInstances =
                new HashSet<Class<? extends Annotation>>(8, 1.0f);
        Collections.addAll(annotationInstances,
                FacesComponent.class,
                FacesConverter.class,
                FacesValidator.class,
                FacesRenderer.class,
                ManagedBean.class,
                NamedEvent.class,
                FacesBehavior.class,
                FacesBehaviorRenderer.class);
        return annotationInstances.toArray(new Class[0]);
    }

}
