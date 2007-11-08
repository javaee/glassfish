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

//J2SE imports
import java.util.*;

//ASM imports
import oracle.toplink.libraries.asm.*;
import oracle.toplink.libraries.asm.attrs.RuntimeVisibleAnnotations;
import oracle.toplink.libraries.asm.attrs.Annotation;

/**
 * INTERNAL:
 * Weaves classes to allow them to support TopLink indirection.
 * Classes are weaved to add a variable of type ValueHolderInterface for each attribute
 * that uses indirection.  In addition, access methods are added for the new variable.
 * Also, triggers the process of weaving the methods of the class.  
 * @see oracle.toplink.essentials.internal.weaving.TopLinkMethodWeaver
 */

public class TopLinkClassWeaver extends ClassAdapter implements Constants {

    public static final String VHI_CLASSNAME =
        "oracle.toplink.essentials.indirection.WeavedAttributeValueHolderInterface";
    public static final String VH_SHORT_SIGNATURE =
        "oracle/toplink/essentials/indirection/ValueHolder";
    public static final String VHI_SHORT_SIGNATURE =
        "oracle/toplink/essentials/indirection/WeavedAttributeValueHolderInterface";
    public static final String VHI_SIGNATURE =
        "L" + VHI_SHORT_SIGNATURE +";";
    public static final String TW_SHORT_SIGNATURE =
        "oracle/toplink/essentials/internal/weaving/TopLinkWeaved";
    
    protected ClassDetails classDetails;
    public boolean alreadyWeaved = false;
    public boolean weavedVH = false;

    public TopLinkClassWeaver(ClassWriter classWriter, ClassDetails classDetails) {
        super(classWriter);
        this.classDetails = classDetails;
    }

    /**
    *  INTERNAL:
    *  Add a variable of type ValueHolderInterface to the class.  When this method has been run, the
    *  class will contain a variable declaration similar to the following:
    *  
    *  private ValueHolderInterface _toplink_variableName_vh;
    */
    public void addValueHolder(AttributeDetails attributeDetails){
        String attribute = attributeDetails.getAttributeName();
        RuntimeVisibleAnnotations annotations = null;
        if (attributeDetails.getGetterMethodName() == null || attributeDetails.getGetterMethodName().equals("")){
            annotations = getTransientAnnotation();
        }
        weavedVH = true;  
        cv.visitField(ACC_PRIVATE, "_toplink_" + attribute + "_vh", VHI_SIGNATURE, null, annotations);
    }

    /**
     * INTERNAL:
     * Add a get method for the newly added valueholder.  Adds a method of the following form:
     * 
     *  public WeavedAttributeValueHolderInterface _toplink_getfoo_vh(){
     *      if (_toplink_foo_vh.isCoordinatedWithProperty() || _toplink_foo_vh.isNewlyWeavedValueHolder()){
     *          EntityC object = getFoo();
     *          if (object != _toplink_foo_vh.getValue()){
     *              setFoo(object);
     *          }
     *      }
     *      return _toplink_foo_vh;
     *  }
     */
    public void addGetterMethodForValueHolder(ClassDetails classDetails, AttributeDetails attributeDetails){
        String attribute = attributeDetails.getAttributeName();
        String className = classDetails.getClassName();
        // Create a getter method for the new valueholder
        CodeVisitor cv_get_VH = cv.visitMethod(ACC_PUBLIC, "_toplink_get" + attribute + "_vh", "()" + VHI_SIGNATURE, null, null);
        cv_get_VH.visitVarInsn(ALOAD, 0);
        
        // if (_toplink_foo_vh.isCoordinatedWithProperty() || _toplink_foo_vh.isNewlyWeavedValueHolder()){
        cv_get_VH.visitFieldInsn(GETFIELD, classDetails.getClassName(), "_toplink_" + attribute + "_vh", VHI_SIGNATURE);
        cv_get_VH.visitMethodInsn(INVOKEINTERFACE, VHI_SHORT_SIGNATURE, "isCoordinatedWithProperty", "()Z");       
        Label l0 = new Label();
        cv_get_VH.visitJumpInsn(IFNE, l0);
        cv_get_VH.visitVarInsn(ALOAD, 0);
        cv_get_VH.visitFieldInsn(GETFIELD, classDetails.getClassName(), "_toplink_" + attribute + "_vh", VHI_SIGNATURE);
        cv_get_VH.visitMethodInsn(INVOKEINTERFACE, VHI_SHORT_SIGNATURE, "isNewlyWeavedValueHolder", "()Z");
        Label l1 = new Label();
        cv_get_VH.visitJumpInsn(IFEQ, l1);
        cv_get_VH.visitLabel(l0);
        cv_get_VH.visitVarInsn(ALOAD, 0);    
        
        // EntityC object = getFoo();
        if (attributeDetails.getGetterMethodName() != null){
            cv_get_VH.visitMethodInsn(INVOKEVIRTUAL, classDetails.getClassName(), attributeDetails.getGetterMethodName(), "()L" + attributeDetails.getReferenceClass().replace('.','/') + ";");    
        } else {
            cv_get_VH.visitMethodInsn(INVOKEVIRTUAL, classDetails.getClassName(), "_toplink_get" + attributeDetails.attributeName, "()L" + attributeDetails.getReferenceClass().replace('.','/') + ";");                
        }
        cv_get_VH.visitVarInsn(ASTORE, 1);

        // if (object != _toplink_foo_vh.getValue()){
        cv_get_VH.visitVarInsn(ALOAD, 1);
        cv_get_VH.visitVarInsn(ALOAD, 0);
        cv_get_VH.visitFieldInsn(GETFIELD, classDetails.getClassName(), "_toplink_" + attribute + "_vh", VHI_SIGNATURE);
        cv_get_VH.visitMethodInsn(INVOKEINTERFACE, VHI_SHORT_SIGNATURE, "getValue", "()Ljava/lang/Object;");
        cv_get_VH.visitJumpInsn(IF_ACMPEQ, l1);
        
        // setFoo(object);
        cv_get_VH.visitVarInsn(ALOAD, 0);
        cv_get_VH.visitVarInsn(ALOAD, 1);
        if (attributeDetails.getSetterMethodName() != null){
            cv_get_VH.visitMethodInsn(INVOKEVIRTUAL, classDetails.getClassName(), attributeDetails.getSetterMethodName(), "(L" + attributeDetails.getReferenceClass().replace('.','/') + ";)V");
        } else {
            cv_get_VH.visitMethodInsn(INVOKEVIRTUAL, classDetails.getClassName(), "_toplink_set" + attributeDetails.getAttributeName(), "(L" + attributeDetails.getReferenceClass().replace('.','/') + ";)V");
        }
        
        // }
        cv_get_VH.visitLabel(l1); 
        
        // return _toplink_foo_vh;
        cv_get_VH.visitVarInsn(ALOAD, 0);
        cv_get_VH.visitFieldInsn(GETFIELD, className, "_toplink_" + attribute + "_vh", VHI_SIGNATURE);
        cv_get_VH.visitInsn(ARETURN);

        cv_get_VH.visitMaxs(0, 0);
    }
    
    /**
     * INTERNAL:
     * Add a set method for the newly added ValueHolder.  Adds a method of this form:
     * 
     *  public void _toplink_setfoo_vh(WeavedAttributeValueHolderInterface valueholderinterface){
     *      _toplink_foo_vh = valueholderinterface;
     *      if (valueholderinterface.isInstantiated()){
     *          Object object = getFoo();
     *          Object value = valueholderinterface.getValue();
     *              if (object != value){
     *                  setFoo((EntityC)value);
     *              }  
     *      }
     *  }
     */
    public void addSetterMethodForValueHolder(ClassDetails classDetails, AttributeDetails attributeDetails){
        String attribute = attributeDetails.getAttributeName();
        String className = classDetails.getClassName();
        // create a setter method for the new valueholder
        CodeVisitor cv_set_value = cv.visitMethod(ACC_PUBLIC, "_toplink_set" + attribute + "_vh", "(" + VHI_SIGNATURE + ")V", null, null);                                 
        
        // _toplink_foo_vh = valueholderinterface;
        cv_set_value.visitVarInsn(ALOAD, 0);
        cv_set_value.visitVarInsn(ALOAD, 1);
        cv_set_value.visitFieldInsn(PUTFIELD, className, "_toplink_" + attribute + "_vh", VHI_SIGNATURE);    
        
        // if (valueholderinterface.isInstantiated()){
        cv_set_value.visitVarInsn(ALOAD, 1);
        cv_set_value.visitMethodInsn(INVOKEINTERFACE, VHI_SHORT_SIGNATURE, "isInstantiated", "()Z");
        Label l0 = new Label();
        cv_set_value.visitJumpInsn(IFEQ, l0);
        
        // Object object = getFoo();
        cv_set_value.visitVarInsn(ALOAD, 0);            
         if (attributeDetails.getGetterMethodName() != null){
            cv_set_value.visitMethodInsn(INVOKEVIRTUAL, className, attributeDetails.getGetterMethodName(), "()L" + attributeDetails.getReferenceClass().replace('.','/') + ";");    
        } else {
            cv_set_value.visitMethodInsn(INVOKEVIRTUAL, className, "_toplink_get" + attributeDetails.attributeName, "()L" + attributeDetails.getReferenceClass().replace('.','/') + ";");                
        }
        cv_set_value.visitVarInsn(ASTORE, 2);
        
        // Object value = valueholderinterface.getValue();
        cv_set_value.visitVarInsn(ALOAD, 1);
        cv_set_value.visitMethodInsn(INVOKEINTERFACE, VHI_SHORT_SIGNATURE, "getValue", "()Ljava/lang/Object;");
        cv_set_value.visitVarInsn(ASTORE, 3);       
        
        // if (object != value){
        cv_set_value.visitVarInsn(ALOAD, 2);
        cv_set_value.visitVarInsn(ALOAD, 3);
        cv_set_value.visitJumpInsn(IF_ACMPEQ, l0);
        
        // setFoo((EntityC)value);
        cv_set_value.visitVarInsn(ALOAD, 0);
        cv_set_value.visitVarInsn(ALOAD, 3);
        cv_set_value.visitTypeInsn(CHECKCAST, attributeDetails.getReferenceClass().replace('.','/'));
         if (attributeDetails.getSetterMethodName() != null){
            cv_set_value.visitMethodInsn(INVOKEVIRTUAL, className, attributeDetails.getSetterMethodName(), "(L" + attributeDetails.getReferenceClass().replace('.','/') + ";)V");
        } else {
            cv_set_value.visitMethodInsn(INVOKEVIRTUAL, className, "_toplink_set" + attributeDetails.getAttributeName(), "(L" + attributeDetails.getReferenceClass().replace('.','/') + ";)V");
        }

        // }
        cv_set_value.visitLabel(l0);
        
        
        cv_set_value.visitInsn(RETURN);
        cv_set_value.visitMaxs(0, 0);
        
    }
    
    /**
     * INTERNAL:
     * Adds a convenience method used to replace a PUTFIELD when field access is used. The method follows
     * the following form:
     *     
     *     public void _toplink_setvariableName(<VariableClas> item) {
     *          _toplink_variableName_vh.setValue(item);
     *          this.item = item;
     *      }
     * 
     */
    public void addSetterMethodForFieldAccess(ClassDetails classDetails, AttributeDetails attributeDetails){
        String attribute = attributeDetails.getAttributeName();
        CodeVisitor cv_set = cv.visitMethod(ACC_PUBLIC, "_toplink_set" + attribute, "(L" + attributeDetails.getReferenceClass().replace('.','/') + ";)V", null, null);
        cv_set.visitVarInsn(ALOAD, 0);
        cv_set.visitFieldInsn(GETFIELD, classDetails.getClassName(), "_toplink_" + attribute + "_vh", TopLinkClassWeaver.VHI_SIGNATURE);
        cv_set.visitVarInsn(ALOAD, 1);
        cv_set.visitMethodInsn(INVOKEINTERFACE, TopLinkClassWeaver.VHI_SHORT_SIGNATURE, "setValue", "(Ljava/lang/Object;)V");
        cv_set.visitVarInsn(ALOAD, 0);
        cv_set.visitVarInsn(ALOAD, 1);
        cv_set.visitFieldInsn(PUTFIELD, classDetails.getClassName(), attribute, "L" + attributeDetails.getReferenceClass().replace('.','/') + ";");
        cv_set.visitInsn(RETURN);
        cv_set.visitMaxs(0 ,0);
    }

    /**
     * INTERNAL:
     * Adds a convenience method used to replace a GETFIELD when field access is used. The method follows
     * the following form:
     * 
     *      public <VariableClass> _toplink_getvariableName(){
     *          this.item = (<VariableClass>)_toplink_variableName_vh.getValue();
     *          return this.item;
     *      }
     * 
     */
    public void addGetterMethodForFieldAccess(ClassDetails classDetails, AttributeDetails attributeDetails){
        String attribute = attributeDetails.getAttributeName();

        CodeVisitor cv_get = cv.visitMethod(ACC_PUBLIC, "_toplink_get" + attribute, "()L" + attributeDetails.getReferenceClass().replace('.','/') + ";", null, null);
        cv_get.visitVarInsn(ALOAD, 0);
        cv_get.visitVarInsn(ALOAD, 0);
        cv_get.visitFieldInsn(GETFIELD, classDetails.getClassName(), "_toplink_" + attribute + "_vh", TopLinkClassWeaver.VHI_SIGNATURE);
        cv_get.visitMethodInsn(INVOKEINTERFACE, TopLinkClassWeaver.VHI_SHORT_SIGNATURE, "getValue", "()Ljava/lang/Object;");
        cv_get.visitTypeInsn(CHECKCAST, attributeDetails.getReferenceClass().replace('.','/'));
        cv_get.visitFieldInsn(PUTFIELD, classDetails.getClassName(), attribute, "L" + attributeDetails.getReferenceClass().replace('.','/') + ";");
        cv_get.visitVarInsn(ALOAD, 0);
        cv_get.visitFieldInsn(GETFIELD, classDetails.getClassName(), attribute, "L" + attributeDetails.getReferenceClass().replace('.','/') + ";");
        cv_get.visitInsn(ARETURN);
        cv_get.visitMaxs(0, 0);
    }
   
    private RuntimeVisibleAnnotations getTransientAnnotation(){
        RuntimeVisibleAnnotations attrs = new RuntimeVisibleAnnotations();
        Annotation transientAnnotation = new Annotation("Ljavax/persistence/Transient;");
        attrs.annotations.add(transientAnnotation);
        return attrs;
    }
    
    // add ChangeTracker, TopLinkWeaved interfaces
    public void visit(int version, int access, String name, String superName,
        String[] interfaces, String sourceFile) {

        // prevent 'double' weaving: scan for TopLinkWeaved interface
        for (int i = 0; i < interfaces.length; i++) {
            String s = interfaces[i];
            if (TW_SHORT_SIGNATURE.equals(s)) {
                alreadyWeaved = true;
                break;
            }
        }
        
        if (!alreadyWeaved) {
            int len = 1 + interfaces.length;
            String[] newInterfaces = new String[len];
            System.arraycopy(interfaces, 0, newInterfaces, 0, interfaces.length);
			// add 'marker' oracle.toplink.essentials.internal.weaving.TopLinkWeaved interface
            newInterfaces[interfaces.length] = TW_SHORT_SIGNATURE;
            super.visit(version, access, name, superName, newInterfaces,
                sourceFile);
        } else {
            super.visit(version, access, name, superName, interfaces, sourceFile);
        }

    }

    /**
     *  INTERNAL:
     *  Construct a TopLinkMethodWeaver and allow it to process the method.
     */
    public CodeVisitor visitMethod(int access, String methodName, String desc,
            String[] exceptions, Attribute attrs) {

        if (!alreadyWeaved) {
            return new TopLinkMethodWeaver(this, methodName, desc, cv.visitMethod(access, methodName, desc, exceptions, attrs));
        } else {
            return super.visitMethod(access, methodName, desc, exceptions, attrs);
        }
    }

    public void visitAttribute(Attribute attr) {
        if (!alreadyWeaved) {
            cv.visitAttribute(attr);
        } else {
            super.visitAttribute(attr);
        }
    }

    public void visitEnd() {
        if (!alreadyWeaved) {
            // add implementation of weavedValueHolders() method in the TopLinkWeaved interface
            // this method will return true if we have weaved and false otherwise.
            CodeVisitor cv_weavedValueHolders = cv.visitMethod(ACC_PUBLIC,
                "weavedValueHolders", "()Z", null, null);
            if (classDetails.weavedValueHolders()) {
                cv_weavedValueHolders.visitInsn(ICONST_1);
            } else {
                cv_weavedValueHolders.visitInsn(ICONST_0);
            }
            cv_weavedValueHolders.visitInsn(IRETURN);
            cv_weavedValueHolders.visitMaxs(0, 0);
        }
        
        // for each attribute we need to check what methods and variables to add
        for (Iterator i = classDetails.getAttributesMap().values().iterator();i.hasNext();) {
            AttributeDetails attributeDetails = (AttributeDetails)i.next();
            if (attributeDetails.weaveValueHolders()) {
                
                // we will add valueholders and methods to classes that have not already been weaved
                // and classes that actually contain the attribute we are processing
                // an attribute could be in the classDetails but not actually in the class
                // if it is owned by a MappedSuperClass
                if (!alreadyWeaved && !attributeDetails.isAttributeOnSuperClass()){
                    addValueHolder(attributeDetails);
                    addGetterMethodForValueHolder(classDetails, attributeDetails);
                    addSetterMethodForValueHolder(classDetails, attributeDetails);
                    
                    // We only add the following convenience method if we are using attribute access.
                    if (attributeDetails.getMapping().usesIndirection() && attributeDetails.isMappedWithAttributeAccess()){
                        addSetterMethodForFieldAccess(classDetails, attributeDetails);
                        addGetterMethodForFieldAccess(classDetails, attributeDetails);
                    }
                }
 
            }
        }
        super.visitEnd();
    }
}
