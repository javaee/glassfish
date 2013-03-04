/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package connector;

import java.util.*;

/**
 * collection of messages.
 *
 * @author	Qingqing Ouyang
 */
public class Messages {

    private static Hashtable messages = new Hashtable();

    public static void sendMessage (String destName, String message) {
        if (messages.get(destName) != null) {
            ((ArrayList) messages.get(destName)).add(message);
        } else {
            ArrayList list = new ArrayList();
            list.add(message);
            messages.put(destName, list);
        }
        System.out.println("sendMessage. message at foo is " + Messages.hasMessages("Foo"));
    }

    public static boolean hasMessages (String destName) {
        return messages.get(destName) != null;
    }

    public static ArrayList getMessages (String destName) {
        return (ArrayList) messages.get(destName);
    }

    public static void main (String[] args) {
       if (args.length != 3) {
           System.exit(1);
       }

       String command  = args[0];
       String destName = args[1];
       String message  = args[2];
   
       if (!"add".equals(command)) {
           System.exit(1);
       }

       sendMessage(destName, message);
       System.out.println("Message : " + message + " sent to " + destName);
    }
}
