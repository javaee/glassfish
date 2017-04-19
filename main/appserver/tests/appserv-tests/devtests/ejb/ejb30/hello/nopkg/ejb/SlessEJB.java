/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

import javax.ejb.Stateless;

@Stateless
public class SlessEJB implements Sless
{
    public String hello() {
        System.out.println("In SlessEJB:hello()");
        return "hello";
    }

}
