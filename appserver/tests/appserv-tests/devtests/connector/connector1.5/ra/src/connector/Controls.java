/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package connector;

/**
 *
 * @author	Qingqing Ouyang
 */
public class Controls {
    public static Object readyLock = new Object();
    public static int expectedResults;
    public static boolean done = false;
}
