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

import javax.validation.constraints.*;
import javax.resource.spi.*;

public class MyAdminObject1 implements java.io.Serializable {

    private String resetControl="NORESET";
    private Integer expectedResults;


    private String email ;

    public void setEmail(String email){
       this.email = email;
    }

    @Null
    public String getEmail(){
        return email;
    }

/*
    @Readme : setting a value that violates validation constraint
    This violation should be listed in the set of violated constraints (in server.log) and
    lookup of this resource must fail
 */

    int intValue = -1;
    @Max(value=50)
    public int getIntValue(){
      return intValue;
    }
 
    @ConfigProperty(type=java.lang.Integer.class)
    public void setIntValue(int intValue){
      this.intValue = intValue;
    }
/*
    @Readme : setting a value that violates validation constraint
    This violation should be listed in the set of violated constraints (in server.log) and
    lookup of this resource must fail
 */
    int intValue1 = 55;

    @Max(value=50)
    public int getIntValue1(){
      return intValue1;
    }

    @ConfigProperty(type=java.lang.Integer.class)
    public void setIntValue1(int intValue){
      this.intValue1 = intValue;
    }



    public void setResetControl (String value) {
        resetControl = value;
    }

    public String getResetControl () {
        return resetControl;
    }

    public void setExpectedResults (Integer value) {
        expectedResults = value;
    }

    public Integer getExpectedResults () {
        return expectedResults;
    }

    public void initialize() {
	System.out.println("[MyAdminObject] Initializing the Controls to false:"+resetControl);
        if (resetControl.equals("BEGINNING")) {
            synchronized (Controls.readyLock){
                Controls.done=false;
            }
	   System.out.println("[MyAdminObject] Initialized the Controls to false");
	}
    }

    public boolean done() {
        synchronized (Controls.readyLock){
         return Controls.done;
        }
    }

    public int expectedResults(){
        synchronized (Controls.readyLock){

        return Controls.expectedResults;
        }
    }

    public Object getLockObject(){
        return Controls.readyLock;
    }

}

