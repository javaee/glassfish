/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package justbean;

public class Log implements java.io.Serializable 
{
  public static void
  log (String message)
  {
    System.out.println(message);
  }
}
