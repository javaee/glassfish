/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * // Copyright (c) 2005, 2006, Oracle. All rights reserved.
 * 
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
package oracle.toplink.essentials.internal.weaving;

//ASM imports
import oracle.toplink.libraries.asm.*;

import java.util.Iterator;

/**
 * INTERNAL:
 * 
 * Used by TopLink's weaving feature to adjust methods to make use of ValueHolders that
 * have been inserted by TopLinkClassWeaver.
 * 
 * For FIELD access, changes references to GETFIELD and PUTFIELD to call newly added 
 * convenience methods.
 * 
 * For Property access, modifies the getters and setters to make use of new ValueHolders
 * 
 * Also adds initialization of newly added ValueHolders to constructor.
 * 
 */

public class TopLinkMethodWeaver extends CodeAdapter implements Constants {

	protected TopLinkClassWeaver tcw;
	protected String methodName;
    private String methodDescriptor = null;
    
    // used to determine if we are at the first line of a method
    private boolean methodStarted = false;
    
    // Used to control initialization of valueholders in constructor
    private boolean constructorInitializationDone = false;
	
	public TopLinkMethodWeaver(TopLinkClassWeaver tcw, String methodName, String methodDescriptor,
		CodeVisitor cv) {
		
		super(cv);
		this.tcw = tcw;
		this.methodName = methodName;
		this.methodDescriptor = methodDescriptor;
	}

    /**
     *  INTERNAL:
     *  Change GETFIELD and PUTFIELD for fields that use attribute access to make use of new convenience methods
     *  
     *  A GETFIELD for an attribute named 'variableName' will be replaced by a call to:
     *  
     *  _toplink_getvariableName()
     *  
     *  A PUTFIELD for an attribute named 'variableName' will be replaced by a call to:
     *  
     *  toplink_setvariableName(variableName)
     */
    public void weaveAttributesIfRequired(int opcode, String owner, String name, String desc){
		AttributeDetails attributeDetails = (AttributeDetails)tcw.classDetails.getAttributeDetailsFromClassOrSuperClass(name);	
        if (attributeDetails == null || !attributeDetails.isMappedWithAttributeAccess()){
            super.visitFieldInsn(opcode, owner, name, desc);
            return;
        }
		if (opcode == GETFIELD) {
			if (attributeDetails != null) {
                cv.visitMethodInsn(INVOKEVIRTUAL, tcw.classDetails.getClassName(), "_toplink_get" + name, "()L" + attributeDetails.getReferenceClass().replace('.','/') + ";");
			} else {
				super.visitFieldInsn(opcode, owner, name, desc);
			}
	    }  else if (opcode == PUTFIELD) {
			if (attributeDetails != null) {
                cv.visitMethodInsn(INVOKEVIRTUAL, tcw.classDetails.getClassName(), "_toplink_set" + name, "(L" + attributeDetails.getReferenceClass().replace('.','/') + ";)V");
			} else {
				super.visitFieldInsn(opcode, owner, name, desc);
			}
        }  else {
			super.visitFieldInsn(opcode, owner, name, desc);
	    }    
    }

    /**
     * INTERNAL:
     * Add initialization of new ValueHolders to constuctors.  If a ValueHolder called 'variableName' 
     * has been added, the following line will be added to the constructor.
     * 
     *  _toplink_variableName_vh = new ValueHolder();
     *  _toplink_foo_vh.setIsNewlyWeavedValueHolder(true);
     */
    public void weaveConstructorIfRequired(int opcode, String owner, String name, String desc){
        if (!constructorInitializationDone && ("<init>".equals(methodName)||"<cinit>".equals(methodName))) {
                // look for the superclass initializer and insert the valueholder
            // initialization after it
            if (opcode == INVOKESPECIAL && name.startsWith("<init>")) {                  
                ClassDetails details = tcw.classDetails;
                Iterator attributes = details.getAttributesMap().keySet().iterator();
                while (attributes.hasNext()){
                    String key = (String)attributes.next();
                        
                    AttributeDetails attribute = (AttributeDetails)details.getAttributesMap().get(key);
                    if (attribute.weaveValueHolders() && !attribute.isCollectionMapping() && !attribute.isAttributeOnSuperClass()){
                        super.visitVarInsn(ALOAD, 0);
                        super.visitTypeInsn(NEW, TopLinkClassWeaver.VH_SHORT_SIGNATURE);
                        super.visitInsn(DUP);
                        super.visitMethodInsn(INVOKESPECIAL, TopLinkClassWeaver.VH_SHORT_SIGNATURE, "<init>", "()V");
                        super.visitFieldInsn(PUTFIELD, details.className, "_toplink_" + attribute.attributeName + "_vh", TopLinkClassWeaver.VHI_SIGNATURE);                    
                        
                        super.visitVarInsn(ALOAD, 0);
                        super.visitFieldInsn(GETFIELD, details.className, "_toplink_" + attribute.attributeName + "_vh", TopLinkClassWeaver.VHI_SIGNATURE);
                        super.visitInsn(ICONST_1);
                        super.visitMethodInsn(INVOKEINTERFACE, TopLinkClassWeaver.VHI_SHORT_SIGNATURE, "setIsNewlyWeavedValueHolder", "(Z)V");
                    }
                }
            }
            constructorInitializationDone = true;
        }
    }

    /**
     * INTERNAL:
     * Modifies getter and setter methods for attributes using property access
     * 
     * In a getter method for 'attributeName', the following line is added at the beginning of the method
     * 
     *  if (!_toplink_attributeName_vh.isInstantiated()){
     *      setFoo((EntityC)_toplink_attributeName_vh.getValue());
     *  }
     * 
     * In a setter method, for 'attributeName', the following line is added at the beginning of the method
     * 
     *  _toplink_attributeName_vh.setValue(argument);
     *  _toplink_attributeName_vh.setIsCoordinatedWithProperty(true);
     * 
     * TODO: In the end, the call to setValue() should be modified to somehow make use of the result of
     * the getter method.  This behavior has not yet been implemented.
     */
    public void addValueHolderReferencesIfRequired(){
        if (methodStarted){
            return;
        }
        AttributeDetails attributeDetails = (AttributeDetails)tcw.classDetails.getGetterMethodToAttributeDetails().get(methodName);
        if (attributeDetails != null && !attributeDetails.isAttributeOnSuperClass()){
            
            cv.visitVarInsn(ALOAD, 0);
            cv.visitFieldInsn(GETFIELD, tcw.classDetails.getClassName(), "_toplink_" + attributeDetails.getAttributeName() + "_vh", TopLinkClassWeaver.VHI_SIGNATURE);
            cv.visitMethodInsn(INVOKEINTERFACE, TopLinkClassWeaver.VHI_SHORT_SIGNATURE, "isInstantiated", "()Z");
            Label l0 = new Label();
            cv.visitJumpInsn(IFNE, l0);
            
            
            cv.visitVarInsn(ALOAD, 0);
            cv.visitVarInsn(ALOAD, 0);
            cv.visitFieldInsn(GETFIELD, tcw.classDetails.getClassName(), "_toplink_" + attributeDetails.getAttributeName() + "_vh", TopLinkClassWeaver.VHI_SIGNATURE);
            cv.visitMethodInsn(INVOKEINTERFACE, TopLinkClassWeaver.VHI_SHORT_SIGNATURE, "getValue", "()Ljava/lang/Object;");
            cv.visitTypeInsn(CHECKCAST, attributeDetails.getReferenceClass().replace('.','/'));
            cv.visitMethodInsn(INVOKEVIRTUAL, tcw.classDetails.getClassName(), attributeDetails.getSetterMethodName(), "(L" + attributeDetails.getReferenceClass().replace('.','/') + ";)V");

            cv.visitLabel(l0);
            //cv.visitVarInsn(ALOAD, 0);
          //  cv.visitFieldInsn(GETFIELD, tcw.classDetails.getClassName(), "foo", "L" + attributeDetails.getReferenceClass().replace('.','/') + ";");

        
        } else {
            attributeDetails = (AttributeDetails)tcw.classDetails.getSetterMethodToAttributeDetails().get(methodName);
            if (attributeDetails != null){
                cv.visitVarInsn(ALOAD, 0);
                cv.visitFieldInsn(GETFIELD, tcw.classDetails.getClassName(), "_toplink_" + attributeDetails.getAttributeName() + "_vh", TopLinkClassWeaver.VHI_SIGNATURE);
                cv.visitVarInsn(ALOAD, 1);
                cv.visitMethodInsn(INVOKEINTERFACE, TopLinkClassWeaver.VHI_SHORT_SIGNATURE, "setValue", "(Ljava/lang/Object;)V");
                
                cv.visitVarInsn(ALOAD, 0);
                cv.visitFieldInsn(GETFIELD, tcw.classDetails.getClassName(), "_toplink_" + attributeDetails.getAttributeName() + "_vh", TopLinkClassWeaver.VHI_SIGNATURE);
                cv.visitInsn(ICONST_1);
                cv.visitMethodInsn(INVOKEINTERFACE, TopLinkClassWeaver.VHI_SHORT_SIGNATURE, "setIsCoordinatedWithProperty", "(Z)V");
            }
        }
    }

    public void visitInsn (final int opcode) {
        addValueHolderReferencesIfRequired();
        methodStarted = true;
        super.visitInsn(opcode);
    }

    public void visitIntInsn (final int opcode, final int operand) {
        addValueHolderReferencesIfRequired();
        methodStarted = true;
        cv.visitIntInsn(opcode, operand);
    }

    public void visitVarInsn (final int opcode, final int var) {
        addValueHolderReferencesIfRequired();
        methodStarted = true;
        cv.visitVarInsn(opcode, var);
    }

    public void visitTypeInsn (final int opcode, final String desc) {
        addValueHolderReferencesIfRequired();
        methodStarted = true;
        cv.visitTypeInsn(opcode, desc);
    }

    public void visitFieldInsn (final int opcode, final String owner, final String name, final String desc){
        addValueHolderReferencesIfRequired();
        methodStarted = true;
        weaveAttributesIfRequired(opcode, owner, name, desc);
    }

    public void visitMethodInsn (final int opcode, final String owner, final String name, final String desc){
        addValueHolderReferencesIfRequired();
        methodStarted = true;
        super.visitMethodInsn(opcode, owner, name, desc);     
        weaveConstructorIfRequired(opcode, owner, name, desc);
    }

    public void visitJumpInsn (final int opcode, final Label label) {
        addValueHolderReferencesIfRequired();
        methodStarted = true;
        cv.visitJumpInsn(opcode, label);
    }

    public void visitLabel (final Label label) {
        cv.visitLabel(label);
    }

    public void visitLdcInsn (final Object cst) {
        addValueHolderReferencesIfRequired();
        methodStarted = true;
        cv.visitLdcInsn(cst);
    }

    public void visitIincInsn (final int var, final int increment) {
        addValueHolderReferencesIfRequired();
        methodStarted = true;
        cv.visitIincInsn(var, increment);
    }

    public void visitTableSwitchInsn (final int min, final int max, final Label dflt, final Label labels[]){
        addValueHolderReferencesIfRequired();
        methodStarted = true;
        cv.visitTableSwitchInsn(min, max, dflt, labels);
    }

    public void visitLookupSwitchInsn (final Label dflt, final int keys[], final Label labels[]){
        addValueHolderReferencesIfRequired();
        methodStarted = true;
        cv.visitLookupSwitchInsn(dflt, keys, labels);
    }

    public void visitMultiANewArrayInsn (final String desc, final int dims) {
        addValueHolderReferencesIfRequired();
        methodStarted = true;
        cv.visitMultiANewArrayInsn(desc, dims);
    }

    public void visitTryCatchBlock (final Label start, final Label end,final Label handler, final String type){
        addValueHolderReferencesIfRequired();
        methodStarted = true;
        cv.visitTryCatchBlock(start, end, handler, type);
    }

    public void visitMaxs (final int maxStack, final int maxLocals) {
        addValueHolderReferencesIfRequired();
        methodStarted = true;
        cv.visitMaxs(0, 0);
    }

    public void visitLocalVariable (final String name, final String desc, final Label start, final Label end, final int index){
        addValueHolderReferencesIfRequired();
        methodStarted = true;
        cv.visitLocalVariable(name, desc, start, end, index);
    }

    public void visitLineNumber (final int line, final Label start) {
        cv.visitLineNumber(line, start);
    }

    public void visitAttribute (final Attribute attr) {
        addValueHolderReferencesIfRequired();
        methodStarted = true;
        cv.visitAttribute(attr);
    }


	// helper methods
	protected AttributeDetails weaveValueHolders(ClassDetails startingDetails,
		String fieldName) {
		
		if (startingDetails == null) {
			return null;
		} else {
			AttributeDetails attributeDetails =	(AttributeDetails)startingDetails.getAttributesMap().get(fieldName);
			if (attributeDetails != null && attributeDetails.weaveValueHolders()) {
				return attributeDetails;
			} else {
				return weaveValueHolders(startingDetails.getSuperClassDetails(),
					fieldName);
			}
		}
	}
}
