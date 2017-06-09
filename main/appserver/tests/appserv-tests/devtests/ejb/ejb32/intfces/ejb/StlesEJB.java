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

import javax.annotation.Resource;
import javax.ejb.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;

/*
    StlesEJB exposes remote interface St3 and St4. St5 isn't business interface
 */
@Remote({St3.class, St4.class})
@Stateless
public class StlesEJB implements St3, St4, St5 {
    @EJB(lookup = "java:module/SingletonBean1!ejb32.intrfaces.St6")
    St6 st6;

    @EJB(lookup = "java:app/ejb32-intrfaces-ejb2/StflEJB2!ejb32.intrfaces.St4")
    St4 st4_stflEJB2;
    @EJB(lookup = "java:module/StflEJB2!ejb32.intrfaces.St6")
    St6 st6_stflEJB2;

    @Resource
    SessionContext ctx;

    // expectation: SingletonBean1.st6.StlesEJB.st3
    public String st3() throws Exception {
        try {
            ctx.lookup("java:module/SingletonBean1!ejb32.intrfaces.St5");
        } catch (Exception e) {
            e.printStackTrace();
            return st6.st6() + "." + "StlesEJB.st3";
        }
        throw new IllegalStateException("Error occurred for SingletonBean1!");
    }

    // expectation: StflEJB2.st4.StflEJB2.st6.StlesEJB.st4
    public String st4() throws Exception {
        try {
            ctx.lookup("java:module/StflEJB2!ejb32.intrfaces.St5");
        } catch (Exception e) {
            e.printStackTrace();
            return st4_stflEJB2.st4() + "." + st6_stflEJB2.st6() + "." + "StlesEJB.st4";
        }
        throw new IllegalStateException("Error occurred for StflEJB2!");
    }


    @Override
    public String st5() throws Exception {
        return "StlesEJB.st5";
    }
}
