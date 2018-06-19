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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.CallableStatement;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.util.ArrayList;
import java.util.List;

import javax.resource.ResourceException;
import javax.resource.cci.IndexedRecord;
import javax.resource.cci.Interaction;
import javax.resource.cci.InteractionSpec;
import javax.resource.cci.Record;
import javax.resource.cci.ResourceWarning;

/**
 * This implementation class enables a component to execute EIS functions.
 * @author Sheetal Vartak
 */
public class CciInteraction implements Interaction {

  protected javax.resource.cci.Connection connection;

  protected CallableStatement csmt;

  public CciInteraction(javax.resource.cci.Connection con) {
    connection = con;
  }

  public javax.resource.cci.Connection getConnection() {
    return connection;
  }

  public void close() throws ResourceException {
    connection = null;

  }

  public boolean execute(InteractionSpec ispec, Record input, Record output)
      throws ResourceException {

    if (ispec == null || (!(ispec instanceof CciInteractionSpec))) {
      throw new ResourceException("Invalid interaction spec");
    }

    String procName = ((CciInteractionSpec) ispec).getFunctionName();
    String schema = ((CciInteractionSpec) ispec).getSchema();
    String catalog = ((CciInteractionSpec) ispec).getCatalog();
    output = exec(procName, schema, catalog, input, output);
    if (output != null) {
      return true;
    } else {
      return false;
    }
  }

  /**
  * This method does the following:
  * 1> using the DatabaseMetadata class, gets the parameters that are IN,OUT
  * or INOUT for the stored procedure.
  * 2>create the callablestatement withthe right JDBC syntax
  * e.g. {? = call proc_name(?,?)}
  * {call proc_name(?)}
  * {? = call proc_name()}
  * 3> execute the statement and return the output in an IndexedRecord object
  */
  Record exec(String procName, String schema, String catalog, Record input, Record output)
      throws ResourceException {
    try {
      java.sql.Connection conn = ((CciConnection) connection).getManagedConnection()
          .getJdbcConnection();
      DatabaseMetaData metadata = conn.getMetaData();
      if (!metadata.supportsCatalogsInProcedureCalls()) {
        catalog = "";
      }
      if (!metadata.supportsSchemasInProcedureCalls()) {
        schema = "";
      }

      ResultSet procNames = metadata.getProcedures(catalog, schema, procName);
      int procFound = 0;
      while (procNames.next()) {
        procFound++;
      }
      procNames.close();
      if (procFound == 0) {
        throw new ResourceException(
            "Cannot find procedure " + procName + ". Please check catalog, schema and function name.");
      }

      ResultSet rs = metadata.getProcedureColumns(catalog, schema, procName, null);
      List parameterList = new ArrayList();
      boolean function = false;
      while (rs.next()) {
        if ((rs.getShort(5) == DatabaseMetaData.procedureColumnReturn) && (!((rs.getString(7))
            .equals("void")))) {
          function = true;
        }
        if (rs.getString(7).equals("void")) {
          continue; // skip extra info from Cloudscape
        }
        parameterList.add(new Parameter(rs.getString(1), rs.getString(2), rs.getString(3), rs
            .getString(4), rs.getShort(5), rs.getShort(6), rs.getShort(10)));
      }
      rs.close();

      int paramCount = parameterList.size();
      if (function) {
        paramCount -= 1;
      }
      //if the procedure is parameterless, paramCount = 0
      procName += "(";
      for (int i = 0; i < paramCount; i++) {
        if (i == 0) {
          procName += "?";
        } else {
          procName += ",?";
        }
      }
      procName += ")";
      String schemaAddOn = "";
      if (schema != null && !schema.equals("")) {
        schemaAddOn = schema + ".";
      }
      if (function) {
        procName = "? = call " + schemaAddOn + procName;
      } else {
        procName = "call " + schemaAddOn + procName;
      }
      //System.out.println("procName.."+procName);
      CallableStatement cstmt = conn.prepareCall("{" + procName + "}");

      //get all IN parameters and register all OUT parameters
      int count = parameterList.size();
      int recCount = 0;
      IndexedRecord iRec = null;

      for (int i = 0; i < count; i++) {
        Parameter parameter = (Parameter) parameterList.get(i);
        if (parameter.isInputColumn()) {
          if (iRec == null) {
            if (input instanceof IndexedRecord) {
              iRec = (IndexedRecord) input;
            } else {
              throw new ResourceException("Invalid input record");
            }
          }
          //get value from input record
          cstmt.setObject(i + 1, iRec.get(recCount));
          recCount++;
        }
      }

      IndexedRecord oRec = null;
      for (int i = 0; i < count; i++) {
        Parameter parameter = (Parameter) parameterList.get(i);
        if (parameter.isOutputColumn()) {
          if (oRec == null) {
            if (output instanceof IndexedRecord) {
              oRec = (IndexedRecord) output;
            } else {
              throw new ResourceException("Invalid output record");
            }
          }
          if (parameter.isDecimalNumeric()) {
            cstmt.registerOutParameter(i + 1, parameter.getDataType(), parameter.getScale());
          } else {
            cstmt.registerOutParameter(i + 1, parameter.getDataType());
          }
        }
      }
      cstmt.execute();

      Class[] parameters = new Class[]
      { int.class };
      //get the right getXXX() from Mapping.java for the output
      Mapping map = new Mapping();
      for (int i = 0; i < count; i++) {
        Parameter parameter = (Parameter) parameterList.get(i);
        if (parameter.isOutputColumn()) {
          String ans = (String) map.get(new Integer(parameter.getDataType()));
          Method method = cstmt.getClass().getMethod(ans, parameters);
          Object[] obj = new Object[]
          { new Integer(i + 1) };
          Object o = method.invoke(cstmt, obj);
          if (output instanceof IndexedRecord) {
            oRec = (IndexedRecord) output;
            oRec.add(o);
            //System.out.println("output..."+o.toString());
          }
        }
      }
      cstmt.close();
      return oRec;
      //  conn.close(); 
    }
    catch (SQLException ex) {
      throw new ResourceException(ex.getMessage());
    }
    catch (NoSuchMethodException ex) {
      throw new ResourceException(ex.getMessage());
    }
    catch (IllegalAccessException ex) {
      throw new ResourceException(ex.getMessage());
    }
    catch (InvocationTargetException ex) {
      throw new ResourceException(ex.getMessage());
    }
  }

  public Record execute(InteractionSpec ispec, Record input) throws ResourceException {

    if (ispec == null || (!(ispec instanceof CciInteractionSpec))) {
      throw new ResourceException("Invalid interaction spec");
    }

    String procName = ((CciInteractionSpec) ispec).getFunctionName();
    String schema = ((CciInteractionSpec) ispec).getSchema();
    String catalog = ((CciInteractionSpec) ispec).getCatalog();
    IndexedRecord output = new CciIndexedRecord();
    return exec(procName, schema, catalog, input, output);
  }

  public ResourceWarning getWarnings() throws ResourceException {
    ResourceWarning resWarning = null;
    try {
      java.sql.Connection con = ((CciConnection) connection).getManagedConnection()
          .getJdbcConnection();
      SQLWarning sql = con.getWarnings();
      resWarning = new ResourceWarning(sql.getMessage());
    }
    catch (SQLException e) {
      throw new ResourceException(e.getMessage());
    }
    return resWarning;
  }

  public void clearWarnings() throws ResourceException {
    try {
      java.sql.Connection con = ((CciConnection) connection).getManagedConnection()
          .getJdbcConnection();
      con.clearWarnings();
    }
    catch (SQLException e) {
      throw new ResourceException(e.getMessage());
    }
  }

}
