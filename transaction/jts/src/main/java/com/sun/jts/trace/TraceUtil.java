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
 * Copyright 2004-2005 Sun Microsystems, Inc.  All rights reserved.
 * Use is subject to license terms.
 */
package com.sun.jts.trace;

import java.io.*;
import com.sun.jts.CosTransactions.*;
import org.omg.CosTransactions.*;

/**
 * This class is used to set trace properties and print trace statements. The print method does the printing.
 * All the methods are unsynchronized. Since setting of properties doesn't happen simultaneously with print
 * in current usage, this is fine. The tracing should be enabled/disabled by calling 
 * <code> Configuration.enableTrace()/disableTrace()</code>
 * prior to any operation on TraceUtil.
 * It uses TraceRecordFormatter for formatting the trace record. 
 *
 * @author <a href="mailto:kannan.srinivasan@sun.com">Kannan Srinivasan</a>
 * @version 1.0
 */
public class TraceUtil
{
	private static int m_currentTraceLevel = TraceLevel.IAS_JTS_TRACE_TRIVIAL ;
	private static char m_fieldDelimiter = ':';
	private static String m_traceRecordTag = "iAS_JTS_Trace> ";
	private static PrintWriter m_traceWriter = null ;

	static
	{
		m_traceWriter = new PrintWriter(System.out);
	}

 /**
   * Initialises the trace class with given output writer. 
   *
   * @param traceWriter an <code>PrintWriter</code> value
   */
    public static void init(PrintWriter traceWriter)
    {
        setTraceWriter(traceWriter);
    }

  /**
   * Sets the output writer. By default the output writer is set to Stdout.
   *
   * @param traceWriter an <code>PrintWriter</code> value
   */
    public static void setTraceWriter(PrintWriter traceWriter)
    {
	m_traceWriter = traceWriter;
    }

  /**
   * Gets the current output writer.
   *
   * @return an <code>PrintWriter</code> value
   */
    public static PrintWriter getTraceWriter()
    {
        return m_traceWriter;
    }

  /**
   * Gets the current trace level. Returns an integer as per the TraceLevel constants.
   *
   * @return an <code>int</code> value
   */
    public static int getCurrentTraceLevel()
    {
        return m_currentTraceLevel;
    }

  /**
   * Sets the current trace level. The argument is tested for its validity and trace level is set.
   * Else an exception is raised.
   *
   * @param traceLevel an <code>int</code> value
   * @exception InvalidTraceLevelException if an error occurs
   */
    public static void setCurrentTraceLevel(int traceLevel) throws InvalidTraceLevelException
    {
        if(Configuration.traceOn)
        {
            int i;
            boolean traceLevelSet = false;
            for(i = 0; i <= TraceLevel.IAS_JTS_MAX_TRACE_LEVEL; i++)
            {
                if(traceLevel == i)
                {
                    m_currentTraceLevel = traceLevel;
                    traceLevelSet = true;
                    break;
                }
            } 
            if(!traceLevelSet)
                throw new InvalidTraceLevelException();

        }
    }

  /**
   * This method formats and writes the trace record to output writer. The method is called
   * with a tracelevel, which is checked with current trace level and if found equal or higher,
   * the print is carried out. This method takes an PrintWriter also, which is used to write the
   * output. This given outputWriter would override the set outputWriter. The origin object is printed
   * using its toString() method.
   * @param traceLevel an <code>int</code> value
   * @param outWriter an <code>PrintWriter</code> value
   * @param tid an <code>Object</code> value
   * @param origin an <code>Object</code> value
   * @param msg a <code>String</code> value
   */
    public static void print(int traceLevel, PrintWriter outWriter, Object tid, Object origin, String msg) 
    {
            if( traceLevel <= m_currentTraceLevel )
            {
            	String traceRecord = TraceRecordFormatter.createTraceRecord(tid, origin, msg); 
		outWriter.println(traceRecord);
            }
    }

  /**
   * This method formats and writes the trace record to current output writer. The method is 
   * called with a tracelevel, which is checked with current trace level and if found equal 
   * or higher, the print is carried out. This method doesn't take a otid and tries to recover
   * it from current obejct asscociated with this thread
   * @param traceLevel an <code>int</code> value
   * @param origin an <code>Object</code> value
   * @param msg a <code>String</code> value
   */
    public static void print(int traceLevel, Object origin, String msg) 
    {
	try{
		 print(traceLevel,
	          ((com.sun.jts.CosTransactions.TopCoordinator)
		  com.sun.jts.CosTransactions.CurrentTransaction.getCurrent().get_localCoordinator()).get_transaction_name(),
		  origin,
		  msg
		  );
	}catch(Exception e){
    		print(traceLevel,null,origin,msg);
	}
    }

  /**
   * This method formats and writes the trace record to current output writer. The method is called
   * with a tracelevel, which is checked with current trace level and if found equal or higher,
   * the print is carried out. This uses the currently set output writer to write the trace output.
   * @param traceLevel an <code>int</code> value
   * @param tid an <code>Object</code> value
   * @param origin an <code>Object</code> value
   * @param msg a <code>String</code> value
   */
    public static void print(int traceLevel, Object tid, Object origin, String msg) 
    {
    	print(traceLevel,m_traceWriter,tid,origin,msg);
    }

  /**
   * Gets the current field delimiter used in formatting trace record. The default is ':'.
   *
   * @return a <code>char</code> value
   */
    public static char getFieldDelimiter()
    {
        return m_fieldDelimiter;
     }    

  /**
   * Sets the current field delimiter.
   *
   * @param delimiter a <code>char</code> value
   */
    public static void setFieldDelimiter(char delimiter)
    {
        m_fieldDelimiter = delimiter;
    }

  /**
   * Gets the current trace record tag used in formatting of trace record. The default is 
   * 'iAS_JTS_Trace> '.
   *
   * @return a <code>String</code> value
   */
    public static String getTraceRecordTag()
    {
        return m_traceRecordTag;
     }    

  /**
   * Sets the trace record tag.
   *
   * @param traceRecordTag a <code>String</code> value
   */
    public static void setTraceRecordTag(String traceRecordTag)
    {
        m_traceRecordTag = traceRecordTag;
    }
}
