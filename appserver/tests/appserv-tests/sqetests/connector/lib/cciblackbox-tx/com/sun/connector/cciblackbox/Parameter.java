/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package com.sun.connector.cciblackbox;

import java.sql.DatabaseMetaData;
import java.sql.Types;

/**
 * This class stores all the important properties of every parameter of a
 * stored procedure.
 * @author Sheetal Vartak
 */

public class Parameter {

  private String catalog;

  private String schema;

  private String procedureName;

  private String parameterName;

  private short parameterType;

  private short dataType;

  private short scale;

  //the above properties are the only important properties of the parameters

  public Parameter(String catalog, String schema, String procedureName, String parameterName,
      short parameterType, short dataType, short scale) {
    this.catalog = catalog;
    this.schema = schema;
    this.procedureName = procedureName;
    this.parameterName = parameterName;
    this.parameterType = parameterType;
    this.dataType = dataType;
    this.scale = scale;
  }

  public short getScale() {
    return scale;
  }

  public String getCatalog() {
    return catalog;
  }

  public void setCatalog(String catalog) {
    this.catalog = catalog;
  }

  public String getSchema() {
    return schema;
  }

  public void setSchema(String schema) {
    this.schema = schema;
  }

  public String getProcedureName() {
    return procedureName;
  }

  public void setProcedureName(String procedureName) {
    this.procedureName = procedureName;
  }

  public String getParameterName() {
    return parameterName;
  }

  public void setParameterName(String parameterName) {
    this.parameterName = parameterName;
  }

  public short getParameterType() {
    return parameterType;
  }

  public void setParameterType(short parameterType) {
    this.parameterType = parameterType;
  }

  public short getDataType() {
    return dataType;
  }

  public void setDataType(short dataType) {
    this.dataType = dataType;
  }

  public boolean isOutputColumn() {
    return (parameterType == DatabaseMetaData.procedureColumnOut || parameterType == DatabaseMetaData.procedureColumnInOut || parameterType == DatabaseMetaData.procedureColumnReturn);
  }

  public boolean isInputColumn() {
    return (parameterType == DatabaseMetaData.procedureColumnIn || parameterType == DatabaseMetaData.procedureColumnInOut);
  }

  public boolean isDecimalNumeric() {
    return (dataType == Types.NUMERIC || dataType == Types.DECIMAL);
  }
}
