/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

/*
 *  TableUtilities.java
 *
 */

package com.sun.jbi.jsf.util;

import com.sun.data.provider.impl.ObjectListDataProvider;
import com.sun.data.provider.FieldKey;
import com.sun.data.provider.RowKey;
import com.sun.webui.jsf.component.TableRowGroup;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;


/**
 *
 * Provides utilities for JBI related tables
 *
 **/

public final class TableUtilities
{

    
	 /**
     * Controls printing of diagnostic messages to the log
     */
	private static Logger sLog = JBILogger.getInstance();


    public final static String KEY_NAME  = "name";
    public final static String KEY_TYPE  = "type";

    /**
     * Returns a list of names and types for the selected rows.
     * @param aGroup <code>TableRowGroup</code> the table data with some rows selected.
     * @return <code>List</code> of <code>Properties</code> objects
     * <p> Each properties object has 'name' and 'type' keys and values.
     */
    public static List getSelectedRowProperties(TableRowGroup aGroup)
    {
        ArrayList result = new ArrayList();

        ObjectListDataProvider dp = (ObjectListDataProvider)
            aGroup.getSourceData();

        if (null != dp)
	    {
		try
		    {
			FieldKey fkName = dp.getFieldKey(KEY_NAME);
			FieldKey fkType = dp.getFieldKey(KEY_TYPE);

			RowKey[] rowKeys = aGroup.getSelectedRowKeys();

			for(int cnt = 0; cnt < rowKeys.length; cnt++)
			    {
				Properties selectedRowProperties =
				    new Properties();


				String compName = (String)
				    dp.getValue(fkName, rowKeys[cnt]);

				selectedRowProperties.setProperty(KEY_NAME, compName);

				String compType = (String)
				    dp.getValue(fkType, rowKeys[cnt]);

				selectedRowProperties.setProperty(KEY_TYPE, compType);

				result.add(selectedRowProperties);
			    }
		    }
		catch (Exception ex)
		    {
			// TBD use logging warning
			sLog.fine("OperationHandlers.getSelectedRowProperties(), caught ex=" + ex);
			ex.printStackTrace(System.err);
		    }
	    }
        else
	    {
		sLog.fine("OperationHandlers.getSelectedRowProperties(), cannot process dp=" + dp); 
	    }

        sLog.fine("OperationHandlers.getSelectedRowProperties(), result=" + result); 
        return result;
    }

}
