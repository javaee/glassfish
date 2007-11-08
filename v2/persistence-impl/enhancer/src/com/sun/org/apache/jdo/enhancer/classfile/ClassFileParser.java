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

import java.io.InputStream;
import java.io.IOException;

/**
 * ClassFileParser parses a given class and calls the appropriate
 * ClassFileParserEventListener methods.
 * 
 * @see ClassFileParserEventListener
 * @see ClassInfo
 * @author kmahesh
 */
public interface ClassFileParser {

    public boolean registerClassFileParserEventListener(
            ClassFileParserEventListener listener);

    public boolean removeClassFileParserEventListener(
            ClassFileParserEventListener listener);

    public void parse(InputStream is) throws IOException;

    public void parse(ClassInfo classInfo)
        throws IOException;

}
