/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
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

package connector;

import javax.resource.spi.ConfigProperty;

//@README : test to make sure that administed objects defined in ra.xml (no @AdministeredObject annotation)
// are considered for @ConfigProperty annotation

public class MyAdminObject_1 implements java.io.Serializable {

    @ConfigProperty(
            defaultValue = "NORESET",
            type = java.lang.String.class
    )
    //@README : we are setting default value to NORESET only in annotation.
    //getter (getResetControl) will throw exception if it is not NORESET
    private String resetControl;
    private Integer expectedResults;

    public void setResetControl (String value) {
        resetControl = value;
    }

    public String getResetControl () {
        if(resetControl == null || !resetControl.equals("NORESET")){
            throw new RuntimeException("reset control not initialized, should have been initialized via annotation");
        }
        return resetControl;
    }

    @ConfigProperty(
            type = java.lang.Integer.class,
            defaultValue = "88"
    )
    //@README : we are setting default value to 88 only in annotation.
    //getter (getExpectedResults) will throw exception if it is not 88
    public void setExpectedResults (Integer value) {
        expectedResults = value;
    }

    public Integer getExpectedResults () {
        if(expectedResults != 88){
            throw new RuntimeException("expected results not initialized, should have been initialized via annotation");
        }
        return expectedResults;
    }
}
