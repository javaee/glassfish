package org.glassfish.hk2.xml.internal.alt;

import org.glassfish.hk2.xml.internal.MethodType;

public interface MethodInformationI {

    /**
     * @return the originalMethod
     */
    public AltMethod getOriginalMethod();

    /**
     * @return the methodType
     */
    public MethodType getMethodType();

    /**
     * @return the getterSetterType
     */
    public AltClass getGetterSetterType();

    /**
     * @return the representedProperty
     */
    public String getRepresentedProperty();

    /**
     * @return the defaultValue
     */
    public String getDefaultValue();

    /**
     * @return the baseChildType
     */
    public AltClass getBaseChildType();

    /**
     * @return the key
     */
    public boolean isKey();

    /**
     * @return the isList
     */
    public boolean isList();

    /**
     * @return the isArray
     */
    public boolean isArray();

    public boolean isReference();

    public String getDecapitalizedMethodProperty();

    public boolean isElement();

}