/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 *
 */

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

    public Map getRegisteredAnnotations() {
        return annotations;
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
