/*
 * 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2007-2010 Sun Microsystems, Inc. All rights reserved.
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
package org.jvnet.hk2.test.runlevel;

import java.util.concurrent.TimeUnit;

import org.glassfish.hk2.AsyncPostConstruct;
import org.jvnet.hk2.annotations.RunLevel;
import org.jvnet.hk2.annotations.Service;

/**
 * For testing async service types
 */
@RunLevel(value=5, runLevelScope=Object.class)
@Service
public class AsyncFakeService1 implements AsyncPostConstruct {

    public long start;

    public static long waitFor = 0;
    public static boolean waited;
    
    @Override
    public void postConstruct() {
        start = System.currentTimeMillis();
    }

    @Override
    public boolean isDone() {
        long now = System.currentTimeMillis();
        boolean b = (now - start) >= waitFor;
//        if (b) {
//            System.out.println(getClass().getSimpleName() + " is done already");
//        }
        return b;
    }

    @Override
    public void waitForDone() {
        long now = System.currentTimeMillis();
        long waitTime = (now - start) + waitFor;
        if (waitTime > 0) {
            if (false) {
                try {
                    Thread.sleep(waitTime);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            } else {
                // simulate done
                waitFor = 0L;
            }
        }

        System.out.println("waited for " + (System.currentTimeMillis() - now));
    }

    @Override
    public boolean waitForDone(long timeout, TimeUnit unit) {
        AsyncFakeService1.waited = true;
        
        long now = System.currentTimeMillis();
        long waitTime = (now - start) + waitFor;
//        System.out.println("waiting for " + waitTime + "; now = " + now + "; start = " + start + "; wait= " + waitFor);
        waitTime = Math.max(waitTime, unit.toMillis(timeout));
        if (waitTime > 0) {
            if (false) {
                try {
                    Thread.sleep(waitTime);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            } else {
                // simulate done
                waitFor = 0L;
            }
        }

//        System.out.println("waited for " + (System.currentTimeMillis() - now));

        return isDone();
    }
    
}
