package com.sun.enterprise.v3.server;

import org.objectweb.asm.*;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import org.glassfish.api.container.Sniffer;
import org.glassfish.deployment.common.AnnotationScanner;

public class SnifferAnnotationScanner extends AnnotationScanner {

    Map<String, List<SnifferStatus>> annotations = new HashMap<String, List<SnifferStatus>>();

    public void register(Sniffer sniffer, Class[] annotationClasses) {
        SnifferStatus stat = new SnifferStatus(sniffer);
        if (annotationClasses!=null) {
            for (Class annClass : annotationClasses) {
                List<SnifferStatus> statList = 
                    annotations.get(Type.getDescriptor(annClass));
                if (statList == null) {
                    statList = new ArrayList<SnifferStatus>();        
                    annotations.put(Type.getDescriptor(annClass), statList);
                }
                statList.add(stat);
            }
        }
    }

    public List<Sniffer> getApplicableSniffers() {
        List<Sniffer> appSniffers = new ArrayList<Sniffer>();
        for (String annotationName : annotations.keySet()) {
            List<SnifferStatus> statList = annotations.get(annotationName);
            for (SnifferStatus stat : statList) {
                if (!appSniffers.contains(stat.sniffer) && stat.found) {
                    appSniffers.add(stat.sniffer);
                }
            }
        }
        return appSniffers;
    }

    public AnnotationVisitor visitAnnotation(String s, boolean b) {
        List<SnifferStatus> statusList = annotations.get(s);
        if (statusList != null) {
            for (SnifferStatus status : statusList) {
                status.found = true;
            }
        }
        return null;
    }

    private static final class SnifferStatus {
        Sniffer sniffer;
        boolean found;

        SnifferStatus(Sniffer sniffer) {
            this.sniffer = sniffer;
        }
    }
}
