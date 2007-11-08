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

package com.sun.org.apache.jdo.impl.enhancer.jdo;

import com.sun.org.apache.jdo.enhancer.classfile.AnnotaterCallback;
import com.sun.org.apache.jdo.enhancer.classfile.MethodInfo;

/**
 * 
 * A ClassFileBuilder builds the actual PersistenceCapable class by providing 
 *   methods to add, modify fields and methods to an existing class. 
 *  
 * @author kmahesh
 *
 */
public interface ClassFileBuilder {
	
	public void mediateFieldAccess(MethodInfo methodInfo, AnnotaterCallback listener);
	
	public void addJDOReplaceFlags();

	public void addJDOIsPersistentMethod();

	public void addJDOIsTransactionalMethod();

	public void addJDOIsNewMethod();

	public void addJDOIsDeletedMethod();

	public void addJDOIsDirtyMethod();

	public void addJDOMakeDirtyMethod();

	public void addJDOPreSerializeMethod();

	public void addJDOGetPersistenceManagerMethod();

	public void addJDOGetObjectIdMethod();

	public void addJDOGetTransactionalObjectIdMethod();

	public void addJDOReplaceStateManager();

	public void addJDOProvideFieldsMethod();

	public void addJDOReplaceFieldsMethod();

	public void addSunJDOClassForNameMethod();
	
	public void addJDOGetManagedFieldCountMethod();
	
	public void addStaticInitialization();
	
	public void addJDONewInstanceMethod();

	public void addJDONewInstanceOidMethod();
	
	public void addJDOProvideFieldMethod();
	
	public void addJDOReplaceFieldMethod();
	
	public void addJDOCopyFieldMethod();
	
	public void addJDOCopyFieldsMethod();
	
	public void addJDONewObjectIdInstanceMethod();
	
	public void addJDONewObjectIdInstanceStringMethod();
    
    public void addJDONewObjectIdInstanceObjectMethod();
	
	public void addJDOCopyKeyFieldsToObjectIdMethod();
	
	public void addJDOCopyKeyFieldsFromObjectIdMethod();
	
	public void addJDOCopyKeyFieldsToObjectIdOIFSMethod();
	
	public void addJDOCopyKeyFieldsFromObjectIdOIFCMethod();
	
	public void addJDODirectReadAccessMethod(String methodName,
            String methodSig,
            int accessFlags,
            int fieldIndex);
	
	public void addJDOCheckedReadAccessMethod(String methodName,
            String methodSig,
            int accessFlags,
            int fieldIndex);
	
	public void addJDOMediatedReadAccessMethod(String methodName,
            String methodSig,
            int accessFlags,
            int fieldIndex);
	
	public void addJDODirectWriteAccessMethod(String methodName,
            String methodSig,
            int accessFlags,
            int fieldIndex);
	
	public void addJDOCheckedWriteAccessMethod(String methodName,
            String methodSig,
            int accessFlags,
            int fieldIndex);
	
	public void addJDOMediatedWriteAccessMethod(String methodName,
            String methodSig,
            int accessFlags,
            int fieldIndex);

	public void addWriteObjectMethod();

	public void addJDOPreSerializeCall(String name, String sig);
    
    public void annotateForPropertyBasedPersistence();

}
