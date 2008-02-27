package org.glassfish.admin.runtime.apt;

import com.sun.mirror.apt.*;
import com.sun.mirror.declaration.*;
import java.util.*;

import org.glassfish.admin.runtime.infrastructure.management.MBean;
import org.glassfish.admin.runtime.infrastructure.management.ManagedAttribute;
import org.glassfish.admin.runtime.infrastructure.management.ManagedOperation;

public class RuntimeMgmtAptFactory implements AnnotationProcessorFactory {

    public RuntimeMgmtAptFactory() {}

    public AnnotationProcessor getProcessorFor(
            Set<AnnotationTypeDeclaration> atds,
            AnnotationProcessorEnvironment env
            ) {
        return new RuntimeMgmtAptProcessor(env);
    }

    public Collection<String> supportedAnnotationTypes() {
        Collection<String> rslt = new ArrayList<String>();
        rslt.add(MBean.class.getName());
        rslt.add(ManagedAttribute.class.getName());
        rslt.add(ManagedOperation.class.getName());
        return rslt;
    }

    public Collection<String> supportedOptions() {
        return new ArrayList<String>();
    }
}
