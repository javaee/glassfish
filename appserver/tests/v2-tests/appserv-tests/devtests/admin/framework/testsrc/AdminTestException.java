/*
    Copyright 2003 Sun Microsystems, Inc. All rights reserved.
    This software is the proprietary information of Sun Microsystems, Inc.
    Use is subject to license terms.

    Copyright 2003 Sun Microsystems, Inc. Tous droits r?serv?s.
    Ce logiciel est propriet? de Sun Microsystems, Inc.
    Distribu? par des licences qui en restreignent l'utilisation.

    $Id: AdminTestException.java,v 1.1 2004/05/11 17:51:35 kravtch Exp $
 *   @author: alexkrav
 *
 *   $Log: AdminTestException.java,v $
 *   Revision 1.1  2004/05/11 17:51:35  kravtch
 *   devtest creation
 *
 *   Revision 1.4  2003/06/25 20:03:37  kravtch
 *   1. java file headers modified
 *   2. properties handling api is added
 *   3. fixed bug for xpathes containing special symbols;
 *   4. new testcases added for jdbc-resource
 *   5. introspector modified by not including base classes operations;
 *
 *
*/

package com.sun.enterprise.admin;

public class AdminTestException extends Exception
{
    /**
        Creates new <code>MBeanConfigException</code> without detail message.
    */
    
    public AdminTestException(int iLine, String strLine, String sampleLine)
    {
        super("AdminTest Compare Exception: line=" + iLine + "\n<<" + strLine + ">>\n<<" + sampleLine + ">> {sample}" );
    }

    public AdminTestException()
    {
        super();
    }


    /**
        Constructs an <code>MBeanConfigException</code> with the specified detail message.
        @param msg the detail message.
    */
    public AdminTestException(String msg)
    {
        super(msg);
    }
}