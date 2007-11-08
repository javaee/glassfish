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
 * Helper methods for dealing with JDO naming conventions.
 */
public class JDONameHelper
    extends NameHelper
    implements PathConstants, VMConstants
{
    static final String jdoPathForType(String type)
    {
        return JDO_Path + type;
    }

    public static final String jdoSPIPathForType(String type)
    {
        return JDO_SPI_Path + type;
    }

    public static final String getJDO_PC_jdoCopyField_Sig(String classPath) 
    {
        return "(" + sigForPath(classPath) + "I)V";
    }

    static private final int ACCPublicPrivateProtected
        = (ACCPublic | ACCPrivate | ACCProtected);

    static private final int ACCStaticFinal
        = (ACCStatic | ACCFinal);
    
    static final String getJDO_PC_jdoAccessor_Name(String fieldName) 
    {
        return "jdoGet" + fieldName;
    }

    static final String getJDO_PC_jdoAccessor_Sig(String instanceClassPath,
                                                  String fieldSig)
    {
        return "(" + sigForPath(instanceClassPath) + ")" + fieldSig;
    }

    static final int getJDO_PC_jdoAccessor_Mods(int fieldMods)
    {
        return (ACCStaticFinal | (fieldMods & ACCPublicPrivateProtected));
    }
    
    static final String getJDO_PC_jdoMutator_Name(String fieldName) 
    {
        return "jdoSet" + fieldName;
    }

    static final String getJDO_PC_jdoMutator_Sig(String instanceClassPath,
                                                 String fieldSig)
    {
        return "(" + sigForPath(instanceClassPath) + fieldSig + ")V";
    }

    static final int getJDO_PC_jdoMutator_Mods(int fieldMods)
    {
        return (ACCStaticFinal | (fieldMods & ACCPublicPrivateProtected));
    }
}
