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

package ejb32.intrfaces;

import javax.ejb.*;
import javax.annotation.*;

/*
    SingletonBean exposes remote interfaces St1 and St2
 */
@Remote
@Singleton
public class SingletonBean implements St1, St2 {

    @EJB(lookup = "java:module/StflEJB1!ejb32.intrfaces.St3")
    St3 st3;
    @EJB(lookup = "java:app/ejb32-intrfaces-ejb2/StflEJB1!ejb32.intrfaces.St4")
    St4 st4;
    @EJB(lookup = "java:global/ejb32-intrfacesApp/ejb32-intrfaces-ejb2/StlesEJB2!ejb32.intrfaces.St7")
    St7 st7;

    @Resource
    SessionContext ctx;

    // expectation: StflEJB1.st3.StflEJB1.st4.SingletonBean.st1
    public String st1() throws Exception {
        return st3.st3() + "." + st4.st4() + "." + "SingletonBean.st1";
    }

    // expectation: StlesEJB2.st7.SingletonBean.st2
    public String st2() throws Exception {
        try {
            ctx.lookup("java:module/StlesEJB2!ejb32.intrfaces.St5");
        } catch (Exception e) {
            return st7.st7() + "." + "SingletonBean.st2";
        }
        throw new IllegalStateException("Error occurred for StlesEJB2!");
    }
}
