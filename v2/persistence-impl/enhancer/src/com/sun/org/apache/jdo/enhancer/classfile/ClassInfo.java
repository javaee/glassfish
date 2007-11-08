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

package com.sun.org.apache.jdo.enhancer.classfile;

import java.util.Collection;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.IOException;

/**
 * ClassInfo provides the necessary information about classes.
 * 
 * @author Mahesh Kannan
 */
public interface ClassInfo extends MemberInfo {

    public boolean isClass();

    public boolean isInterface();

    public String getSuperClassName();

    public Collection<FieldInfo> fields();

    public Collection<MethodInfo> methods();

    public Collection<String> interfaces();

    public void addInterface(String name);

    public void addField(int access, String name, String type, String sig);

    public MethodInfo findMethod(String name, String desc, String sig);

    public MethodInfo removeMethod(String name, String desc, String sig);

    public FieldInfo findField(String name);

    public boolean removeMethod(MethodInfo methInfo);

    public void write(OutputStream os) throws IOException;

    public void dump(PrintWriter pw);

    public void print(PrintWriter pw, int indent);

    public void summarize(PrintWriter pw, int indent);
}
