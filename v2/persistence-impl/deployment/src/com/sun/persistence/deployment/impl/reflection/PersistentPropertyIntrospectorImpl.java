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


package com.sun.persistence.deployment.impl.reflection;

import com.sun.org.apache.jdo.util.I18NHelper;
import com.sun.persistence.api.deployment.AccessType;
import com.sun.persistence.deployment.impl.PersistentPropertyIntrospector;
import com.sun.persistence.deployment.impl.LogHelperDeployment;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;

/**
 * JavaModel does not know which field or property is persistent capable.
 * Instead this class has that intelligence.
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class PersistentPropertyIntrospectorImpl
        implements PersistentPropertyIntrospector {

    private static final I18NHelper i18NHelper = I18NHelper.getInstance(
            LogHelperDeployment.class);

    private JavaModelImpl javaModel;

    public PersistentPropertyIntrospectorImpl(JavaModelImpl javaModel) {
        this.javaModel = javaModel;
    }

    public FieldOrPropertyImpl[] getPCProperties(
            Object javaType,
            AccessType accessType)
            throws Exception {
        if (accessType == AccessType.FIELD) {
            return getPCFields(javaType);
        } else {
            return getPCGetters(javaType);
        }
    }

    private FieldOrPropertyImpl[] getPCGetters(Object javaType)
            throws Exception {
        ArrayList<FieldOrPropertyImpl> pcProps = new ArrayList<FieldOrPropertyImpl>();
        String typeName = javaModel.getName(javaType);
        for (FieldOrPropertyImpl p : javaModel.getProperties(typeName,
                AccessType.PROPERTY)) {
            Method method = Method.class.cast(p.getUnderlyingObject());
            if (method.isAnnotationPresent(javax.persistence.Transient.class)) {
                LogHelperDeployment.getLogger().fine(i18NHelper.msg(
                        "MSG_SkippingMethodWithTransientAnnotation", // NOI18N
                        p.getName()));
                continue;
            }
            pcProps.add(p);
        }
        return pcProps.toArray(new FieldOrPropertyImpl[0]);
    }

    private FieldOrPropertyImpl[] getPCFields(Object javaType)
            throws Exception {
        ArrayList<FieldOrPropertyImpl> pcFields =
                new ArrayList<FieldOrPropertyImpl>();
        String typeName = javaModel.getName(javaType);
        for (FieldOrPropertyImpl f : javaModel.getProperties(typeName,
                AccessType.FIELD)) {
            Field field = Field.class.cast(f.getUnderlyingObject());
            if (Modifier.isTransient(field.getModifiers())) {
                LogHelperDeployment.getLogger().fine(i18NHelper.msg(
                        "MSG_SkippingTransientField", // NOI18N
                        f.getName()));
                continue;
            }
            if (field.isAnnotationPresent(javax.persistence.Transient.class)) {
                LogHelperDeployment.getLogger().fine(i18NHelper.msg(
                        "MSG_SkippingFieldWithTransientAnnotation", // NOI18N
                        f.getName()));
                continue;
            }
            pcFields.add(f);
        }
        return pcFields.toArray(new FieldOrPropertyImpl[0]);
    }

    public FieldOrPropertyImpl getPCProperty(
            Object javaType,
            AccessType accessType,
            String fieldOrPropertyName)
            throws Exception {
        for (FieldOrPropertyImpl f : getPCProperties(javaType, accessType)) {
            if (fieldOrPropertyName.equals(f.getName())) {
                return f;
            }
        }
        return null;
    }
}
