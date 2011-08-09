/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2011 Oracle and/or its affiliates. All rights reserved.
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
package org.jvnet.hk2.test.runlevel;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.RunLevel;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.Inhabitant;
import org.jvnet.hk2.junit.Hk2Runner;
import org.jvnet.hk2.junit.Hk2Test;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.logging.Logger;

/**
 * Test the @RunLevel annotation
 *
 * @author Jerome Dochez
 */
@RunWith(Hk2Runner.class)
public class RunLevelTest extends Hk2Test {
    @Inject
    Habitat habitat;

    public RunLevel getAnnotation(Class<?> annotated) {
        RunLevel rl = annotated.getAnnotation(RunLevel.class);
        if (rl!=null) {
            return rl;
        }
        for (Annotation a : annotated.getAnnotations()) {
            rl = a.annotationType().getAnnotation(RunLevel.class);
            if (rl!=null) {
                return rl;
            }
        }
        return null;
    }

    @Test
    public void run() {
        HashSet<Inhabitant<?>> annotated = new HashSet<Inhabitant<?>>(habitat.getInhabitantsByContract(RunLevel.class.getName()));
        assertEquals(annotated.toString(), 26, annotated.size());
        for (Inhabitant<?> i : annotated) {
            Logger.getAnonymousLogger().fine(i.typeName() + " is annotated with " + RunLevel.class.getName());

            RunLevel rl = getAnnotation(i.type());
            assertNotNull(rl);
            Logger.getAnonymousLogger().fine("and its level is " + rl.value());
        }

        annotated = new HashSet<Inhabitant<?>>(habitat.getInhabitantsByContract(ARunLevel.class.getName()));
        assertEquals(annotated.toString(), 1, annotated.size());
        for (Inhabitant<?> i : annotated) {
            Logger.getAnonymousLogger().fine(i.typeName() + " is annotated with " + ARunLevel.class.getName());
            assertEquals(SomeOtherServerService.class.getName(), i.typeName());

            RunLevel rl = getAnnotation(i.type());
            assertNotNull(rl);
            assertEquals("level", 50, rl.value());
        } 
    }
}
