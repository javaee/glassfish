/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2002-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

package com.sun.connector.blackbox;

import javax.resource.spi.BootstrapContext;
import javax.resource.spi.XATerminator;
import javax.resource.spi.work.WorkManager;
import java.io.*;

public class BootstrapHolder {

    private BootstrapContext context;
    private XATerminator xat;
    private WorkManager wm;

    public BootstrapHolder(BootstrapContext context) {
        this.context = context;    
        this.wm = (WorkManager) makeCopyOfObject(context.getWorkManager());
        checkEquality(wm, context.getWorkManager());
        this.xat = (XATerminator) makeCopyOfObject(context.getXATerminator());
    }

    private boolean checkEquality(Object obj_1, Object obj_2) {
        boolean equal = obj_1.equals(obj_2);
        if(equal){
            System.out.println("checkEquality : objects of "+obj_1.getClass().getName() +" , "+obj_2.getClass().getName()+" are equal ");
        }else{
            System.out.println("checkEquality : objects of "+obj_1.getClass().getName() +" , "+obj_2.getClass().getName()+" are not equal ");
        }
        return equal;
    }

    public Object makeCopyOfObject(Object obj) {
        if (obj instanceof Serializable) {
            try {
                // first serialize the object
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(bos);
                oos.writeObject(obj);
                oos.flush();
                byte[] data = bos.toByteArray();
                oos.close();
                bos.close();

                // now deserialize it
                ByteArrayInputStream bis = new ByteArrayInputStream(data);
                ObjectInputStream ois = new ObjectInputStream(bis);


                return ois.readObject();
            } catch (Exception ex) {
                RuntimeException re =
                        new RuntimeException("Cant copy Serializable object of : " + obj.getClass().getName());
                re.initCause(ex);
                throw re;
            }
        } else {
            throw new RuntimeException("Cant copy Serializable object of  : " + obj.getClass().getName());
        }
    }
}
