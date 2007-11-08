/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * Portions Copyright Apache Software Foundation.
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

package org.apache.taglibs.standard.lang.jstl;

import java.util.List;
import java.util.Map;

/**
 *
 * <p>An expression representing one or more unary operators on a
 * value
 * 
 * @author Nathan Abramson - Art Technology Group
 * @author Shawn Bayern
 * @version $Change: 181177 $$DateTime: 2001/06/26 08:45:09 $$Author: kchung $
 **/

public class UnaryOperatorExpression
  extends Expression
{
  //-------------------------------------
  // Properties
  //-------------------------------------
  // property operator

  UnaryOperator mOperator;
  public UnaryOperator getOperator ()
  { return mOperator; }
  public void setOperator (UnaryOperator pOperator)
  { mOperator = pOperator; }

  //-------------------------------------
  // property operators

  List mOperators;
  public List getOperators ()
  { return mOperators; }
  public void setOperators (List pOperators)
  { mOperators = pOperators; }

  //-------------------------------------
  // property expression

  Expression mExpression;
  public Expression getExpression ()
  { return mExpression; }
  public void setExpression (Expression pExpression)
  { mExpression = pExpression; }

  //-------------------------------------
  /**
   *
   * Constructor
   **/
  public UnaryOperatorExpression (UnaryOperator pOperator,
				  List pOperators,
				  Expression pExpression)
  {
    mOperator = pOperator;
    mOperators = pOperators;
    mExpression = pExpression;
  }

  //-------------------------------------
  // Expression methods
  //-------------------------------------
  /**
   *
   * Returns the expression in the expression language syntax
   **/
  public String getExpressionString ()
  {
    StringBuffer buf = new StringBuffer ();
    buf.append ("(");
    if (mOperator != null) {
      buf.append (mOperator.getOperatorSymbol ());
      buf.append (" ");
    }
    else {
      for (int i = 0; i < mOperators.size (); i++) {
	UnaryOperator operator = (UnaryOperator) mOperators.get (i);
	buf.append (operator.getOperatorSymbol ());
	buf.append (" ");
      }
    }
    buf.append (mExpression.getExpressionString ());
    buf.append (")");
    return buf.toString ();
  }

  //-------------------------------------
  /**
   *
   * Evaluates to the literal value
   **/
  public Object evaluate (Object pContext,
			  VariableResolver pResolver,
			  Map functions,
			  String defaultPrefix,
			  Logger pLogger)
    throws ELException
  {
    Object value = mExpression.evaluate (pContext, pResolver, functions,
					 defaultPrefix, pLogger);
    if (mOperator != null) {
      value = mOperator.apply (value, pContext, pLogger);
    }
    else {
      for (int i = mOperators.size () - 1; i >= 0; i--) {
	UnaryOperator operator = (UnaryOperator) mOperators.get (i);
	value = operator.apply (value, pContext, pLogger);
      }
    }
    return value;
  }

  //-------------------------------------
}
