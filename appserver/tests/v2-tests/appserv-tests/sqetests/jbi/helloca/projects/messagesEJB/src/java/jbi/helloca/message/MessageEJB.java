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
