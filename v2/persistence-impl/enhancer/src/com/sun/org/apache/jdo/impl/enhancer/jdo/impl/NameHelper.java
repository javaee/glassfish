/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the "License").  You may not use this file except 
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt or 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html. 
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * HEADER in each file and include the License file at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt.  If applicable, 
 * add the following below this CDDL HEADER, with the 
 * fields enclosed by brackets "[]" replaced with your 
 * own identifying information: Portions Copyright [yyyy] 
 * [name of copyright owner]
 */

package com.sun.org.apache.jdo.impl.enhancer.jdo.impl;


/**
 * Constant definitions for Java and JDO path prefixes.
 */
interface PathConstants
{
    // these constant definitions are used in the initialization of
    // the subsequent interfaces

    String JAVA_LANG_Path = "java/lang/";

    String JDO_Path = "com/sun/persistence/support/";

    String JDO_SPI_Path = JDO_Path + "spi/";
}

/**
 * Helper methods for dealing with JVM naming conventions.
 *
 * Provides the JDO meta information neccessary for byte-code enhancement.
 * <p>
 * <b>Please note: This interface deals with fully qualified names in the
 * JVM notation, that is, with '/' as package separator character&nbsp;
 * (instead of '.').</b>
 * <p>
 * The following convention is used to specify the format of a given name:
 * Something called ...
 * <ul>
 * <li>
 * <i>name</i> represents a non-qualified name (e.g.
 *   <code>JDOPersistenceManager_Name</code>
 *   = "<code>PersistenceManager</code>")</li>
 * <li>
 * <i>type</i> represents a Java-qualified class name (e.g.
 *   <code>JDOPersistenceManager_Path</code>
 *   = '<code>com.sun.javax.jdo.ri.PersistenceManager</code>")</li>
 * <li>
 * <i>path</i> represents a JVM-qualified name (e.g.
 *   <code>JDOPersistenceManager_Path</code>
 *   = '<code>javax/jdo/ri/PersistenceManager</code>")</li>
 * <li>
 * <i>sig</i> (for <i>signature</i>) represents a JVM-qualified type signature
 *   name (e.g. <code>JDOPersistenceManager_Sig</code>
 *   = "<code>Ljavax/jdo/ri/PersistenceManager;</code>")</li>
 * </ul>
 */
public class NameHelper
    implements PathConstants
{
    public static final String sigForPath(String path)
    {
        // assumes reference type
        return "L" + path + ";";
    }

    public static final String pathForSig(String sig)
    {
        // assumes reference type
        return (sig.charAt(0) == '['
                ? sig : sig.substring(1, sig.length() - 1));
    }

    public static final String typeForPath(String path)
    {
        return path.replace('/', '.');
    }

    static final String pathForType(String type)
    {
        return type.replace('.', '/');
    }

    public static final String typeForSig(String sig)
    {
        return typeForPath(pathForSig(sig));
    }

    static final String sigForType(String type)
    {
        return sigForPath(pathForType(type));
    }

    static final String elementSigForSig(String sig)
    {
        return sig.substring(sig.lastIndexOf('[') + 1,
                             sig.length());
    }

    static final String elementPathForSig(String sig)
    {
        return pathForSig(elementSigForSig(sig));
    }

    static final String elementTypeForSig(String sig)
    {
        return typeForSig(elementSigForSig(sig));
    }

    static final String javaLangPathForType(String type)
    {
        return JAVA_LANG_Path + type;
    }

    public static final String constructorName()
    {
        return "<init>";
    }

    public static final String constructorSig()
    {
        return constructorSig("");
    }

    static final String constructorSig(String argSig)
    {
        final String sig = (argSig == null ? "" : argSig);
        return "(" +  sig + ")V";
    }
}
