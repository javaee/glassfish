package org.glassfish.ejb.deployment;

import org.jvnet.hk2.annotations.Service;
import com.sun.enterprise.deployment.AnnotationTypesProvider;

import javax.ejb.MessageDriven;
import javax.ejb.Stateful;
import javax.ejb.Stateless;
import java.lang.annotation.Annotation;

/**
 * Provides the annotation types for the EJB Types
 *
 * @author Jerome Dochez
 */
@Service(name="EJB")
public class EjbAnnotationTypesProvider implements AnnotationTypesProvider {
    public Class<? extends Annotation>[] getAnnotationTypes() {
        return new Class[] {
                MessageDriven.class, Stateful.class, Stateless.class };    }
}
