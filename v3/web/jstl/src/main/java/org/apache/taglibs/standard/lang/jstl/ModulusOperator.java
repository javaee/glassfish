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

/**
 *
 * <p>The implementation of the modulus operator
 * 
 * @author Nathan Abramson - Art Technology Group
 * @version $Change: 181177 $$DateTime: 2001/06/26 08:45:09 $$Author: kchung $
 **/

public class ModulusOperator
  extends BinaryOperator
{
  //-------------------------------------
  // Singleton
  //-------------------------------------

  public static final ModulusOperator SINGLETON =
    new ModulusOperator ();

  //-------------------------------------
  /**
   *
   * Constructor
   **/
  public ModulusOperator ()
  {
  }

  //-------------------------------------
  // Expression methods
  //-------------------------------------
  /**
   *
   * Returns the symbol representing the operator
   **/
  public String getOperatorSymbol ()
  {
    return "%";
  }

  //-------------------------------------
  /**
   *
   * Applies the operator to the given value
   **/
  public Object apply (Object pLeft,
		       Object pRight,
		       Object pContext,
		       Logger pLogger)
    throws ELException
  {
    if (pLeft == null &&
	pRight == null) {
      if (pLogger.isLoggingWarning ()) {
	pLogger.logWarning
	  (Constants.ARITH_OP_NULL,
	   getOperatorSymbol ());
      }
      return PrimitiveObjects.getInteger (0);
    }

    if ((pLeft != null &&
	 (Coercions.isFloatingPointType (pLeft) ||
	  Coercions.isFloatingPointString (pLeft))) ||
	(pRight != null &&
	 (Coercions.isFloatingPointType (pRight) ||
	  Coercions.isFloatingPointString (pRight)))) {
      double left =
	Coercions.coerceToPrimitiveNumber (pLeft, Double.class, pLogger).
	doubleValue ();
      double right =
	Coercions.coerceToPrimitiveNumber (pRight, Double.class, pLogger).
	doubleValue ();

      try {
	return PrimitiveObjects.getDouble (left % right);
      }
      catch (Exception exc) {
	if (pLogger.isLoggingError ()) {
	  pLogger.logError
	    (Constants.ARITH_ERROR,
	     getOperatorSymbol (),
	     "" + left,
	     "" + right);
	}
	return PrimitiveObjects.getInteger (0);
      }
    }
    else {
      long left =
	Coercions.coerceToPrimitiveNumber (pLeft, Long.class, pLogger).
	longValue ();
      long right =
	Coercions.coerceToPrimitiveNumber (pRight, Long.class, pLogger).
	longValue ();

      try {
	return PrimitiveObjects.getLong (left % right);
      }
      catch (Exception exc) {
	if (pLogger.isLoggingError ()) {
	  pLogger.logError
	    (Constants.ARITH_ERROR,
	     getOperatorSymbol (),
	     "" + left,
	     "" + right);
	}
	return PrimitiveObjects.getInteger (0);
      }
    }
  }

  //-------------------------------------
}
