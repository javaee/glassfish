/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * // Copyright (c) 1998, 2007, Oracle. All rights reserved.
 * 
 * Portions Copyright 2007 Markus KARG (markus-karg@users.sourceforge.net)
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
package oracle.toplink.essentials.platform.database;

import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Hashtable;

import oracle.toplink.essentials.exceptions.ValidationException;
import oracle.toplink.essentials.expressions.ExpressionOperator;
import oracle.toplink.essentials.internal.databaseaccess.DatabasePlatform;
import oracle.toplink.essentials.internal.databaseaccess.FieldTypeDefinition;
import oracle.toplink.essentials.internal.helper.ClassConstants;
import oracle.toplink.essentials.internal.helper.DatabaseTable;
import oracle.toplink.essentials.queryframework.ValueReadQuery;

/**
 * Provides SQL Anywhere specific behaviour.
 * 
 * @author Markus KARG (markus-karg@users.sourceforge.net)
 */
@SuppressWarnings("serial")
public final class SQLAnyWherePlatform extends DatabasePlatform {

	private static final ExpressionOperator createCurrentDateOperator() {
		return ExpressionOperator.simpleFunctionNoParentheses(ExpressionOperator.CurrentDate, "CURRENT DATE");
	}

	private static final ExpressionOperator createCurrentTimeOperator() {
		return ExpressionOperator.simpleFunctionNoParentheses(ExpressionOperator.CurrentTime, "CURRENT TIME");
	}

	private static final ExpressionOperator createLocate2Operator() {
		return ExpressionOperator.simpleThreeArgumentFunction(ExpressionOperator.Locate2, "LOCATE");
	}

	private static final ExpressionOperator createLocateOperator() {
		return ExpressionOperator.simpleTwoArgumentFunction(ExpressionOperator.Locate, "LOCATE");
	}

	@Override
	protected final Hashtable buildFieldTypes() {
		final Hashtable<Class, FieldTypeDefinition> fieldTypeMapping = new Hashtable<Class, FieldTypeDefinition>();
		fieldTypeMapping.put(Boolean.class, new FieldTypeDefinition("BIT", false));
		fieldTypeMapping.put(Integer.class, new FieldTypeDefinition("INTEGER", false));
		fieldTypeMapping.put(Long.class, new FieldTypeDefinition("BIGINT", false));
		fieldTypeMapping.put(Float.class, new FieldTypeDefinition("REAL", false));
		fieldTypeMapping.put(Double.class, new FieldTypeDefinition("DOUBLE", false));
		fieldTypeMapping.put(Short.class, new FieldTypeDefinition("SMALLINT", false));
		fieldTypeMapping.put(Byte.class, new FieldTypeDefinition("SMALLINT", false));
		fieldTypeMapping.put(BigInteger.class, new FieldTypeDefinition("BIGINT", false));
		fieldTypeMapping.put(BigDecimal.class, new FieldTypeDefinition("DOUBLE", false));
		fieldTypeMapping.put(Number.class, new FieldTypeDefinition("DOUBLE", false));
		fieldTypeMapping.put(String.class, new FieldTypeDefinition("VARCHAR"));
		fieldTypeMapping.put(Character.class, new FieldTypeDefinition("CHAR"));
		fieldTypeMapping.put(Byte[].class, new FieldTypeDefinition("LONG BINARY", false));
		fieldTypeMapping.put(Character[].class, new FieldTypeDefinition("LONG VARCHAR", false));
		fieldTypeMapping.put(byte[].class, new FieldTypeDefinition("LONG BINARY", false));
		fieldTypeMapping.put(char[].class, new FieldTypeDefinition("LONG VARCHAR", false));
		fieldTypeMapping.put(Blob.class, new FieldTypeDefinition("LONG BINARY",	false));
		fieldTypeMapping.put(Clob.class, new FieldTypeDefinition("LONG VARCHAR", false));
		fieldTypeMapping.put(Date.class, new FieldTypeDefinition("DATE", false));
		fieldTypeMapping.put(Time.class, new FieldTypeDefinition("TIME", false));
		fieldTypeMapping.put(Timestamp.class, new FieldTypeDefinition("TIMESTAMP", false));
		return fieldTypeMapping;
	}

	@Override
    public final ValueReadQuery buildSelectQueryForIdentity() {
		return new ValueReadQuery("SELECT @@identity");
	}

	@Override
	protected final String getCreateTempTableSqlPrefix() {
		return "DECLARE TEMPORARY TABLE ";
	}

	@Override
	public final int getJDBCType(final Class javaType) {
		return javaType == ClassConstants.BLOB ? Types.LONGVARBINARY :  javaType == ClassConstants.CLOB ? Types.LONGVARCHAR : super.getJDBCType(javaType);
	}

	@Override
	public final int getMaxFieldNameSize() {
		return 128;
	}

	@Override
	public final DatabaseTable getTempTableForTable(final DatabaseTable table) {
		return new DatabaseTable("$" + table.getName(), table.getTableQualifier());
	}

	@Override
	protected final void initializePlatformOperators() {
		super.initializePlatformOperators();
		this.addOperator(SQLAnyWherePlatform.createLocateOperator());
		this.addOperator(SQLAnyWherePlatform.createLocate2Operator());
		this.addOperator(SQLAnyWherePlatform.createCurrentDateOperator());
		this.addOperator(SQLAnyWherePlatform.createCurrentTimeOperator());
		this.addOperator(ExpressionOperator.charLength());
	}

	@Override
	public final boolean isSQLAnywhere() {
		return true;
	}

	@Override
	public final void printFieldIdentityClause(final Writer writer) throws ValidationException {
		try {
			writer.write(" DEFAULT AUTOINCREMENT");
		} catch (final IOException ioException) {
			throw ValidationException.fileError(ioException);
		}
	}

	@Override
	public final void printFieldNullClause(final Writer writer) throws ValidationException {
		try {
			writer.write(" NULL");
		} catch (final IOException ioException) {
			throw ValidationException.fileError(ioException);
		}
	}

	@Override
	public final boolean supportsLocalTempTables() {
		return true;
	}

	@Override
	public final boolean supportsIdentity() {
		return true;
	}

	@Override
	public final boolean supportsStoredFunctions() {
		return true;
	}

}
