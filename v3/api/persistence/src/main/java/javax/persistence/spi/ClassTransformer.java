/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package javax.persistence.spi;

import java.security.ProtectionDomain;
import java.lang.instrument.IllegalClassFormatException;
/**
 * A persistence provider supplies an instance of this
 * interface to the {@link PersistenceUnitInfo#addTransformer 
 * PersistenceUnitInfo.addTransformer}
 * method. The supplied transformer instance will get
 * called to transform entity class files when they are
 * loaded or redefined. The transformation occurs before
 * the class is defined by the JVM.
 *
 * @since Java Persistence 1.0
 */
public interface ClassTransformer {
	/**
	* Invoked when a class is being loaded or redefined.
	* The implementation of this method may transform the
	* supplied class file and return a new replacement class
	* file.
	*
	* @param loader The defining loader of the class to be
	* transformed, may be null if the bootstrap loader
	* @param className The name of the class in the internal form
	* of fully qualified class and interface names
	* @param classBeingRedefined If this is a redefine, the
	* class being redefined, otherwise null
	* @param protectionDomain The protection domain of the
	* class being defined or redefined
	* @param classfileBuffer The input byte buffer in class
	* file format - must not be modified
	* @return A well-formed class file buffer (the result of
	* the transform), or null if no transform is performed
	* @throws IllegalClassFormatException If the input does
	* not represent a well-formed class file
	*/
	byte[] transform(ClassLoader loader, String className, 
		Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) 
        throws IllegalClassFormatException;
}
