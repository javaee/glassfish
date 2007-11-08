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


package com.sun.persistence.runtime.sqlstore.sql.select.impl;

import com.sun.org.apache.jdo.pm.PersistenceManagerInternal;
import com.sun.org.apache.jdo.model.java.JavaType;
import com.sun.persistence.runtime.model.mapping.RuntimeMappingField;

import java.sql.ResultSet;
import java.util.Map;
import java.util.HashMap;

/**
 * ResultElement for fetching a state field
 * @author Mitesh Meswani
 */
public class StateFieldResultElement extends ResultElementImpl {
    /**
     * The field that this result element represents
     */
    private RuntimeMappingField field;

    /**
     * The column that this result element represents
     */
    protected ResultColumnElement resultColumn;

    public StateFieldResultElement(ResultColumnElement resultColumn,
            String sqlText, RuntimeMappingField field) {
        super(sqlText);
        this.field = field;
        this.resultColumn = resultColumn;
    }

    /**
     * @inheritDoc
     */
    public Object getResult(PersistenceManagerInternal pm, ResultSet rs) {
        return resultColumn.getObjectResult(rs);
    }

    /**
     * Gets JDO field number for the underlying field
     * @return JDO field number for the underlying field
     */
    public int getJDOFieldNumber() {
        return field.getJDOField().getFieldNumber();
    }

    private static final String JAVA_LANG_OBJECT = "java.lang.Object"; //NOI18N
    static Map<String, FieldTranscriber> transcribers = new HashMap<String, FieldTranscriber>();
    static {
        transcribers.put("java.lang.Boolean",     new BooleanFieldTranscriber());  //NOI18N
        transcribers.put("java.lang.Byte",        new ByteFieldTranscriber());     //NOI18N
        transcribers.put("java.lang.Short",       new ShortFieldTranscriber());    //NOI18N
        transcribers.put("java.lang.Integer",     new IntegerFieldTranscriber());  //NOI18N
        transcribers.put("java.lang.Long",        new LongFieldTranscriber());     //NOI18N
        transcribers.put("java.lang.Character",   new CharacterFieldTranscriber());//NOI18N
        transcribers.put("java.lang.Float",       new FloatFieldTranscriber());    //NOI18N
        transcribers.put("java.lang.Double",      new DoubleFieldTranscriber());   //NOI18N
        transcribers.put(JAVA_LANG_OBJECT,        new ObjectFieldTranscriber());   //NOI18N
    }

    /* ---------------- Helper methods  for double dispatch ---------------- */
    /**
     * Gets the result for this field as a boolean from the given ResultSet
     * @param rs The ResultSet from which the result set should be fetched
     * @return Result for this field as a boolean from the given ResultSet Any
     *         SqlException encountered are thrown wrapped in a
     *         JDODataStoreException
     */
    public boolean getBooleanResult(ResultSet rs) {
        return resultColumn.getBooleanResult(rs);
    }

    /**
     * Gets the result for this field as a char from the given ResultSet
     * @param rs The ResultSet from which the result set should be fetched
     * @return Result for this field as a char from the given ResultSet Any
     *         SqlException encountered are thrown wrapped in a
     *         JDODataStoreException
     */
    public char getCharResult(ResultSet rs) {
        return resultColumn.getCharResult(rs);
    }

    /**
     * Gets the result for this field as a byte from the given ResultSet
     * @param rs The ResultSet from which the result set should be fetched
     * @return Result for this field as a byte from the given ResultSet Any
     *         SqlException encountered are thrown wrapped in a
     *         JDODataStoreException
     */
    public byte getByteResult(ResultSet rs) {
        return resultColumn.getByteResult(rs);
    }

    /**
     * Gets the result for this field as a short from the given ResultSet
     * @param rs The ResultSet from which the result set should be fetched
     * @return Result for this field as a short from the given ResultSet Any
     *         SqlException encountered are thrown wrapped in a
     *         JDODataStoreException
     */
    public short getShortResult(ResultSet rs) {
        return resultColumn.getShortResult(rs);
    }

    /**
     * Gets the result for this field as a int from the given ResultSet
     * @param rs The ResultSet from which the result set should be fetched
     * @return Result for this field as a int from the given ResultSet Any
     *         SqlException encountered are thrown wrapped in a
     *         JDODataStoreException
     */
    public int getIntResult(ResultSet rs) {
        return resultColumn.getIntResult(rs);
    }

    /**
     * Gets the result for this field as a long from the given ResultSet
     * @param rs The ResultSet from which the result set should be fetched
     * @return Result for this field as a long from the given ResultSet Any
     *         SqlException encountered are thrown wrapped in a
     *         JDODataStoreException
     */
    public long getLongResult(ResultSet rs) {
        return resultColumn.getLongResult(rs);
    }

    /**
     * Gets the result for this field as a float from the given ResultSet
     * @param rs The ResultSet from which the result set should be fetched
     * @return Result for this field as a float from the given ResultSet Any
     *         SqlException encountered are thrown wrapped in a
     *         JDODataStoreException
     */
    public float getFloatResult(ResultSet rs) {
        return resultColumn.getFloatResult(rs);
    }

    /**
     * Gets the result for this field as a double from the given ResultSet
     * @param rs The ResultSet from which the result set should be fetched
     * @return Result for this field as a double from the given ResultSet Any
     *         SqlException encountered are thrown wrapped in a
     *         JDODataStoreException
     */
    public double getDoubleResult(ResultSet rs) {
        return resultColumn.getDoubleResult(rs);
    }

    /**
     * Gets the result for this field as a String from the given ResultSet
     * @param rs The ResultSet from which the result should be fetched
     * @return Result for this field as a String from the given ResultSet Any
     *         SqlException encountered are thrown wrapped in a
     *         JDODataStoreException
     */
    public String getStringResult(ResultSet rs) {
        return resultColumn.getStringResult(rs);
    }

    /**
     * Gets the result for this field as a Object from the given ResultSet
     * @param rs The ResultSet from which the result should be fetched
     * @return Result for this field as a Object from the given ResultSet Any
     *         SqlException encountered are thrown wrapped in a
     *         JDODataStoreException
     */
    public Object getObjectResult(ResultSet rs) {
        JavaType javaType = field.getJDOField().getJavaField().getType();
        String javaTypeName = javaType.isWrapperClass()
                ? javaType.getName() : JAVA_LANG_OBJECT;
        //TODO: This lookup needs to go into RuntimeMappingField#getTranscriber()
        FieldTranscriber transcriber = transcribers.get(javaTypeName);
        assert transcriber != null : "Model code for isWrapperClass() and" //NOI18N
                        + "the transcribers map is out of sync";           //NOI18N
        return transcriber.getResult(rs, resultColumn);
    }

    //--Transcriber to fetch primitive wrappers
    private static class BooleanFieldTranscriber implements FieldTranscriber {
        public Object getResult(ResultSet rs, ResultColumnElement resultColumn) {
            return Boolean.valueOf(resultColumn.getBooleanResult(rs) );
        }
    }
    private static class ByteFieldTranscriber implements FieldTranscriber {
        public Object getResult(ResultSet rs, ResultColumnElement resultColumn) {
            return Byte.valueOf(resultColumn.getByteResult(rs) );
        }
    }
    private static class ShortFieldTranscriber implements FieldTranscriber {
        public Object getResult(ResultSet rs, ResultColumnElement resultColumn) {
            return Short.valueOf(resultColumn.getShortResult(rs) );
        }
    }

    private static class IntegerFieldTranscriber implements FieldTranscriber {
        public Object getResult(ResultSet rs, ResultColumnElement resultColumn) {
            return Integer.valueOf(resultColumn.getIntResult(rs) );
        }
    }

    private static class LongFieldTranscriber implements FieldTranscriber {
        public Object getResult(ResultSet rs, ResultColumnElement resultColumn) {
            return Long.valueOf(resultColumn.getLongResult(rs) );
        }
    }

    private static class CharacterFieldTranscriber implements FieldTranscriber {
        public Object getResult(ResultSet rs, ResultColumnElement resultColumn) {
            return Character.valueOf(resultColumn.getCharResult(rs) );
        }
    }

    private static class FloatFieldTranscriber implements FieldTranscriber {
        public Object getResult(ResultSet rs, ResultColumnElement resultColumn) {
            return Float.valueOf(resultColumn.getFloatResult(rs) );
        }
    }

    private static class DoubleFieldTranscriber implements FieldTranscriber {
        public Object getResult(ResultSet rs, ResultColumnElement resultColumn) {
            return Double.valueOf(resultColumn.getDoubleResult(rs) );
        }
    }

    private static class ObjectFieldTranscriber implements FieldTranscriber {
        public Object getResult(ResultSet rs, ResultColumnElement resultColumn) {
            return resultColumn.getObjectResult(rs);
        }
    }


}
