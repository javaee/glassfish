/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.s1asdev.ejb.ejb30.ee.remote_sfsb;

import javax.ejb.Stateful;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
	
@Stateful
public class RemoteSfulEJB
	implements  RemoteSful {

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public String sayNamaste() {
	return "Remote Hello";
    }

}
