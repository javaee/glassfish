/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package statelesshello;

import java.rmi.RemoteException;

public class StatelesshelloException extends Exception {
    
    public StatelesshelloException(String str) {
	super(str);
    }
    
}
