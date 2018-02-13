/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
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

/*
 * MessageEJB.java
 *
 * Created on February 15, 2007, 9:35 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package jbi.helloca.message;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.ejb.Stateless;
import javax.jws.Oneway;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebParam.Mode;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;
import javax.xml.ws.Holder;
import jbi.helloca.message.types.AddressBook;
import jbi.helloca.message.types.AddressBookEntry;

/**
 *
 * @author sony
 */

@Stateless()
@WebService()
@SOAPBinding(style=Style.RPC)
public class MessageEJB {
    
    @WebMethod
    public void ping() {
        System.out.println("MessageEJB : ping ping ping");
    }
    
    @WebMethod
    @Oneway
    public void testStringOneway(String str) {
        System.out.println("MessageEJB : testStringOneway : " + str);
    }
    
    @WebMethod(operationName="testTypes")
    public boolean testSimpleTypes(String str, int i, double d,
            byte[] array) {
        
        System.out.println("MessageEJB : testSimpleTypes");
        
        byte[] localArray = new byte[] { Byte.MAX_VALUE, Byte.MIN_VALUE};
        
        if (str.equals("Hello") && i == Integer.MIN_VALUE &&
                d == java.lang.Double.MAX_VALUE &&
                Arrays.equals(array, localArray))
            return true;
        
        return false;
    }
    
    @WebMethod
    public String testParamModes(
    @WebParam(name="addressBook", mode=Mode.OUT) Holder<AddressBook> book,
    @WebParam(name="addressBookEntry", mode=Mode.INOUT) Holder<AddressBookEntry> entry) {

        System.out.println("Received AddressBookEntry : " +
                entry.value.name);
        book.value = new AddressBook();
        book.value.addressBook = new ArrayList<AddressBookEntry>();
        book.value.addressBook.add(entry.value);
        return entry.value.name;
    }
}
