/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.glassfish.admingui.plugins.jsf;

import com.sun.faces.config.AnnotationScanner;
import com.sun.faces.spi.AnnotationProvider;
import java.lang.annotation.Annotation;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.bean.ManagedBean;
import javax.faces.component.FacesComponent;
import javax.faces.component.behavior.FacesBehavior;
import javax.faces.convert.FacesConverter;
import javax.faces.event.NamedEvent;
import javax.faces.render.FacesBehaviorRenderer;
import javax.faces.render.FacesRenderer;
import javax.faces.validator.FacesValidator;
import javax.servlet.ServletContext;
import org.glassfish.admingui.plugins.PluginService;

/**
 *
 * @author jdlee
 */
public class PluginAnnotationScanner extends AnnotationScanner {
    private AnnotationProvider parent;
    private final HashSet<Class<? extends Annotation>> annotations = new HashSet<Class<? extends Annotation>>() {{
        add(FacesBehavior.class);
        add(FacesBehaviorRenderer.class);
        add(FacesComponent.class);
        add(FacesConverter.class);
        add(FacesValidator.class);
        add(FacesRenderer.class);
        add(ManagedBean.class);
        add(NamedEvent.class);
    }};
    
    
    public PluginAnnotationScanner(ServletContext sc, AnnotationProvider parent) {
        super(sc);
        this.parent = parent;
    }

    @Override
    public Map<Class<? extends Annotation>, Set<Class<?>>> getAnnotatedClasses(Set<URI> set) {
        final Map<Class<? extends Annotation>, Set<Class<?>>> classes = new HashMap<Class<? extends Annotation>, Set<Class<?>>>();
        final Map<Class<? extends Annotation>, Set<Class<?>>> pluginClasses = getClasses();
        
        classes.putAll(parent.getAnnotatedClasses(set));
        
        for (Map.Entry<Class<? extends Annotation>, Set<Class<?>>> entry : pluginClasses.entrySet()) {
            Set<Class<?>> annotatedClassSet = classes.get(entry.getKey());
            if (annotatedClassSet == null) {
                annotatedClassSet = new HashSet<Class<?>>();
                classes.put(entry.getKey(), annotatedClassSet);
            }
            
            annotatedClassSet.addAll(entry.getValue());
        }
        

        return classes;
    }

    protected void processEnumeration(Enumeration<URL> e) {
        System.out.println("****************************************************");
        while (e.hasMoreElements()) {
            URL url = e.nextElement();
            System.out.println("**** resource = " + url.toString());
        }
    }

    public Map<Class<? extends Annotation>, Set<Class<?>>> getClasses() {
        PluginService ps = PluginUtil.getPluginService();
        
        Map<Class<? extends Annotation>, Set<Class<?>>> classes = new HashMap<Class<? extends Annotation>, Set<Class<?>>>();
        for (String className : ps.getClassNames()) {
            try {
                Class clazz = Class.forName(className);
                for (Class<? extends Annotation> a : annotations) {
                    if (clazz.getAnnotation(a) != null) {
                        Set<Class<?>> set = classes.get(a);
                        if (set == null) {
                            set = new HashSet<Class<?>>();
                            classes.put(a, set);
                        }
                        set.add(clazz);
                    }

                }
            } catch (ClassNotFoundException ex) {
//                Logger.getLogger(PluginService.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return Collections.unmodifiableMap(classes);
    }
}
