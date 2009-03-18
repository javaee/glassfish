/*
 * Use of this J2EE Connectors Sample Source Code file is governed by
 * the following modified BSD license:
 *
 * Copyright 2002 Sun Microsystems, Inc. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * -Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * -Redistribution in binary form must reproduct the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * Neither the name of Sun Microsystems, Inc. or the names of
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * This software is provided "AS IS," without a warranty of any kind.
 * ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES,
 * INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN AND
 * ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES OR LIABILITIES
 * SUFFERED BY LICENSEE AS A RESULT OF  OR RELATING TO USE, MODIFICATION
 * OR DISTRIBUTION OF THE SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL
 * SUN OR ITS LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA,
 * OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR
 * PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY OF
 * LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE SOFTWARE,
 * EVEN IF SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 * You acknowledge that Software is not designed, licensed or intended
 * for use in the design, construction, operation or maintenance of any
 * nuclear facility.
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
