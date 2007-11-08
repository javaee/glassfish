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

/**
 * Defines the operations that are available for ClassFileBuilder.
 * 
 * @author Mahesh Kannan
 *
 */
public interface ClassFileAnalyzer {
	
	public int getAnnotatedFieldCount();
	
	public int[] getAnnotatedFieldFlags();
	
	public int[] getAnnotatedFieldMods();
	
    public String[] getAnnotatedFieldNames();
	
	public String[] getAnnotatedFieldSigs();

	public boolean isAugmentableAsRoot();
	
	public boolean hasStaticInitializer();
	
	public int getKeyFieldCount();
	
	public String getKeyClassName();
	
	public int[] getKeyFieldIndexes();

    public int getManagedFieldCount();
	
	public String getPCRootClassName();
	
	public String getPCSuperClassName();
	
	public String getPCKeyOwnerClassName();
	
	public String getPCSuperKeyOwnerClassName();
    
    public boolean isPropertyBasedPersistence();
    
}
