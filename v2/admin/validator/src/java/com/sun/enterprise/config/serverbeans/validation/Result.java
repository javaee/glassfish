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
package com.sun.enterprise.config.serverbeans.validation;

import java.util.*;
import com.sun.enterprise.util.LocalStringManagerImpl;

/**
    <b>NOT THREAD SAFE (mutable variables not protected)</b>
 */
public class Result {

    public static final int PASSED = 0;
    public static final int FAILED = 1;
    public static final int WARNING = 2;
    public static final int NOT_APPLICABLE = 3;
    public static final int NOT_RUN = 4;
    public static final int NOT_IMPLEMENTED = 5;
    private int status = NOT_RUN;
    private String componentName;
    private String operationPrintName;
    private String assertion;
    private String testName;
    private final Vector errorDetails =  new Vector();
    private final Vector goodDetails =  new Vector();
    private final Vector warningDetails =  new Vector();
    private final Vector naDetails =  new Vector();

    /**
     * Result Constructor
     *
     */
    public Result(){
    }


    /**
     * Store passed info
     *
     * @param detail Details of passed test
     */
    public void passed(String detail){
	setStatus(PASSED);
	addGoodDetails(detail);
    }

    /**
     * Store warning info 
     *
     * @param detail Details of warning test
     */
    public void warning(String detail){
	setStatus(WARNING);
	addWarningDetails(detail);
    }

    /**
     * Store Not Applicable info
     *
     * @param detail Details of not applicable test
     */
    public void notApplicable(String detail){
	setStatus(NOT_APPLICABLE);
	addNaDetails(detail);
    }

    /**
     * Store Failed info
     *
     * @param detail Details of failed test
     */
    public void failed(String detail){
	setStatus(FAILED);
	addErrorDetails(detail);
    }

    /**
     * Retrieve Not Applicable details
     *
     * @return <code>Vector</code> not applicable details
     */
    public Vector getNaDetails(){
	return naDetails;
    }

    /**
     * Retrieve Warning details
     *
     * @return <code>Vector</code> warning details
     */
    public Vector getWarningDetails(){
	return warningDetails;
    }

    /**
     * Set Not Applicable details
     *
     * @param s not applicable details
     */
    public void addNaDetails(String s){
	naDetails.addElement(s);
    }

    /**
     * Retrieve Good details
     *
     * @return <code>Vector</code> good details
     */
    public Vector getGoodDetails(){
	return goodDetails;
    }

    /**
     * Fill in Good details 
     *
     * @param s good detail string
     */
    public void addGoodDetails(String s){
	goodDetails.addElement(s);
    }

    /**
     * Fill in Warning details
     *
     * @param s warning detail string
     */
    public void addWarningDetails(String s){
	warningDetails.addElement(s);
    }

    /**
     * Retrieve Error details
     *
     * @return <code>Vector</code> error details
     */
    public Vector getErrorDetails(){
	return errorDetails;
    }

    /**
     * Retrieve Error details
     *
     * @return <code>Vector</code> error details
     */
    public String getErrorDetailsAsString(){
        String str = "";
	for(int i=0; i<errorDetails.size(); i++)
        {
            if(i>0)
                str += "\n";
            str += errorDetails.get(i);
        }
        //add rejection string at the end
        if(errorDetails.size()>0 && 
           operationPrintName!=null &&
           operationPrintName.length()>0)
        {
            str += "\n";
            str += StringManagerHelper.getLocalStringsManager().getLocalString(
                      "operation_reject_msg","{0} has been rejected.",
                      new Object[] {operationPrintName});
                    
        }
        return str;
    }

    /**
     * Fill in Error details
     *
     * @param s  error detail string
     */
    public void addErrorDetails(String s){
	errorDetails.addElement(s);
    }

    /**
     * Fill in Error details
     *
     * @param s  error detail string
     */
    public void addErrorDetails(int index, String s){
	errorDetails.add(index, s);
    }

    /**
     * Retrieve test result status
     *
     * @return <code>int</code> test result status
     */
    public int getStatus(){
	return status;
    }

    /**
     * Set test result status
     *
     * @param s test result status
     */
    public void setStatus(int s){
	status = s;
    }

    /**
     * Retrieve assertion
     *
     * @return <code>String</code> assertion string
     */
    public String getAssertion(){
	return assertion;
    }

    /**
     * Set assertion 
     *
     * @param s assertion string
     */
    public void setAssertion(String s){
	assertion = s;
    }

    /**
     * Retrieve component/module name
     *
     * @return <code>String</code> component/module name
     */
    public String getComponentName(){
	return componentName;
    }

    /**
     * Set component/module name
     *
     * @param s component/module name
     */
    public void setComponentName(String s){
	componentName = s;
    }

    /**
     * Retrieve config operation name
     *
     * @return <code>String</code> component/module name
     */
    public String getOperationPrintName(){
	return operationPrintName;
    }

    /**
     * Set component/module name
     *
     * @param s component/module name
     */
    public void setOperationPrintName(String s){
	operationPrintName = s;
    }

    /**
     * Retrieve test name
     *
     * @return <code>String</code> test name
     */
    public String getTestName(){
	return testName;
    }

    /**
     * Set test name
     *
     * @param s test name
     */
    public void setTestName(String s){
	testName = s;
    }

} // Result class
