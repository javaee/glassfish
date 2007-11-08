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

package com.sun.org.apache.jdo.impl.enhancer.classfile.asm;

import org.objectweb.asm.Type;

import com.sun.org.apache.jdo.enhancer.classfile.TypeHelper;

public class TypeHelperImpl
        implements TypeHelper
{

    public String getReturnTypeAsJavaString(String methodDescriptor) {
        return typeDescriptorAsJavaString(Type.getReturnType(methodDescriptor)
                .toString());
    }

    public String[] getParamTypesAsJavaString(String methodDescriptor) {
        Type[] types = Type.getArgumentTypes(methodDescriptor);
        String[] javaTypes = new String[types.length];
        int index = 0;
        for (Type type : types) {
            javaTypes[index] = typeDescriptorAsJavaString(type.toString());
        }
        return javaTypes;
    }

    public String typeDescriptorAsJavaString(String desc) {
        int index = 0;
        int sz = desc.length();

        String primType = null;
        String objectType = null;
        boolean typeFound = false;

        int dimension = 0;
        for (index = 0; (!typeFound) && (index < sz); index++) {
            char c = desc.charAt(index);
            if (c == '[') {
                dimension++;
            } else {
                switch (c) {
                case 'B':
                    primType = "byte";
                    typeFound = true;
                    break;
                case 'C':
                    primType = "char";
                    typeFound = true;
                    break;
                case 'D':
                    primType = "double";
                    typeFound = true;
                    break;
                case 'F':
                    primType = "float";
                    typeFound = true;
                    break;
                case 'I':
                    primType = "int";
                    typeFound = true;
                    break;
                case 'J':
                    primType = "long";
                    typeFound = true;
                    break;
                case 'L':
                    objectType = desc.substring(index + 1, sz - 1).replace('/',
                            '.');
                    typeFound = true;
                    break;
                case 'S':
                    primType = "short";
                    typeFound = true;
                    break;
                case 'Z':
                    primType = "boolean";
                    typeFound = true;
                    break;
                case 'V':
                    primType = "void";
                    typeFound = true;
                    break;                    
                default:
                    throw new IllegalArgumentException("Inalid char : " + c
                            + " at index: " + index);
                }
            }
        }

        if (!typeFound) {
            throw new IllegalArgumentException("Inalid descriptor: " + desc);
        }
        StringBuilder sbldr = new StringBuilder();
        sbldr.append((primType != null) ? primType : objectType);

        for (int i = 0; i < dimension; i++) {
            sbldr.append("[]");
        }

        return sbldr.toString();
    }

}
