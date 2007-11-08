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

package com.sun.enterprise.cli.commands;

import java.util.Iterator;
import java.util.Vector;
import java.util.Hashtable;
import java.util.Collections;
import java.util.Enumeration;
import java.lang.Character;

import com.sun.enterprise.cli.framework.*;


/**
 *    CompileCLICommands.java
 *    This class will generate a html file containing all the
 *    commands available in asadmin.
 *    @version  $Revision: 1.3 $
 */
public class CompileCLICommands 
{
    public static void main(String[] args) 
    {
        try 
        {
            CLIDescriptorsReader cdr = CLIDescriptorsReader.getInstance();
            ValidCommandsList vcl = cdr.getCommandsList();
            Iterator commands = vcl.getCommands();
            printHTMLHeader();

            Hashtable ht = new Hashtable();
            while (commands.hasNext())
            {
                ValidCommand command = (ValidCommand)commands.next();
                    //System.out.println("command = " + command.getName());
                ht.put(command.getName(), command.getUsageText());
            }
            
            Vector v = new Vector(ht.keySet());
                //sort commands in alphabetical order
            Collections.sort(v);

            printHTMLCommands(v);
            printHTMLUsageText(v, ht);

                /*
            for (Enumeration e = v.elements(); e.hasMoreElements();) {
                String commandName = (String)e.nextElement();
                System.out.println("command = " + commandName + " usage-text = " + (String)ht.get(commandName));
            }
                */
            
            printHTMLFooter();
        }
        catch (Exception e) 
        {
            e.printStackTrace();
        }
    }

    private static int printCommands(Vector commands, int ii, char ch)
    {
        System.out.println("<a name='" + ch + "'/>");
        System.out.println("<B>" + Character.toUpperCase(ch) + "</B><br>");
        while ( ii<commands.size() &&
                ((String)commands.get(ii)).charAt(0) == ch)
        {
            System.out.print("<a href='#");
            System.out.println((String)commands.get(ii) + "'>" +
                               (String)commands.get(ii++) + "</a> <br>");
        }
        System.out.println("<a href='#top'>top</a> <br><br>");
        return ii;
    }
    

    public static void printHTMLCommands(Vector commands)
    {
        for (int ii=0; ii+1<commands.size();)
        {
            String commandName = (String)commands.get(ii);
            
            if (commandName.charAt(0) == 'a') 
                ii = printCommands(commands, ii, 'a');
            else if (commandName.charAt(0) == 'b') 
                ii = printCommands(commands, ii, 'b');
            else if (commandName.charAt(0) == 'c') 
                ii = printCommands(commands, ii, 'c');
            else if (commandName.charAt(0) == 'd') 
                ii = printCommands(commands, ii, 'd');
            else if (commandName.charAt(0) == 'e') 
                ii = printCommands(commands, ii, 'e');
            else if (commandName.charAt(0) == 'f') 
                ii = printCommands(commands, ii, 'f');
            else if (commandName.charAt(0) == 'g') 
                ii = printCommands(commands, ii, 'g');
            else if (commandName.charAt(0) == 'h') 
                ii = printCommands(commands, ii, 'h');
            else if (commandName.charAt(0) == 'i') 
                ii = printCommands(commands, ii, 'i');
            else if (commandName.charAt(0) == 'j') 
                ii = printCommands(commands, ii, 'j');
            else if (commandName.charAt(0) == 'k') 
                ii = printCommands(commands, ii, 'k');
            else if (commandName.charAt(0) == 'l') 
                ii = printCommands(commands, ii, 'l');
            else if (commandName.charAt(0) == 'm') 
                ii = printCommands(commands, ii, 'm');
            else if (commandName.charAt(0) == 'n') 
                ii = printCommands(commands, ii, 'n');
            else if (commandName.charAt(0) == 'o') 
                ii = printCommands(commands, ii, 'o');
            else if (commandName.charAt(0) == 'p') 
                ii = printCommands(commands, ii, 'p');
            else if (commandName.charAt(0) == 'q') 
                ii = printCommands(commands, ii, 'q');
            else if (commandName.charAt(0) == 'r') 
                ii = printCommands(commands, ii, 'r');
            else if (commandName.charAt(0) == 's') 
                ii = printCommands(commands, ii, 's');
            else if (commandName.charAt(0) == 't') 
                ii = printCommands(commands, ii, 't');
            else if (commandName.charAt(0) == 'u') 
                ii = printCommands(commands, ii, 'u');
            else if (commandName.charAt(0) == 'v') 
                ii = printCommands(commands, ii, 'v');
            else if (commandName.charAt(0) == 'w') 
                ii = printCommands(commands, ii, 'w');
            else if (commandName.charAt(0) == 'x') 
                ii = printCommands(commands, ii, 'x');
            else if (commandName.charAt(0) == 'y') 
                ii = printCommands(commands, ii, 'y');
            else if (commandName.charAt(0) == 'z') 
                ii = printCommands(commands, ii, 'z');
            
        }
    }


    private static int printUsageText(Vector commands, Hashtable ht,
                                      int ii, char ch)
    {
        while ( ii<commands.size() &&
                ((String)commands.get(ii)).charAt(0) == ch)
        {
            final String commandName = (String)commands.get(ii++);
            System.out.println("<a name='"+commandName+"'/>");
            System.out.println((String)ht.get(commandName) + " <br><br>");
        }
        return ii;
    }
    


    public static void printHTMLUsageText(Vector commands, Hashtable ht)
    {
        System.out.println("<a name='usagetext'/>");
        System.out.println("<br><B>Usage Text</B><br><br>");

        for (int ii=0; ii+1<commands.size();)
        {
            String commandName = (String)commands.get(ii);
            if (commandName.charAt(0) == 'a') 
                ii= printUsageText(commands, ht, ii, 'a');
            else if (commandName.charAt(0) == 'b')
                ii =printUsageText(commands, ht, ii, 'b');
            else if (commandName.charAt(0) == 'c')
                ii =printUsageText(commands, ht, ii, 'c');
            else if (commandName.charAt(0) == 'd')
                ii =printUsageText(commands, ht, ii, 'd');
            else if (commandName.charAt(0) == 'e')
                ii =printUsageText(commands, ht, ii, 'e');
            else if (commandName.charAt(0) == 'f')
                ii =printUsageText(commands, ht, ii, 'f');
            else if (commandName.charAt(0) == 'g')
                ii =printUsageText(commands, ht, ii, 'g');
            else if (commandName.charAt(0) == 'h')
                ii =printUsageText(commands, ht, ii, 'h');
            else if (commandName.charAt(0) == 'i')
                ii =printUsageText(commands, ht, ii, 'i');
            else if (commandName.charAt(0) == 'j')
                ii =printUsageText(commands, ht, ii, 'j');
            else if (commandName.charAt(0) == 'k')
                ii =printUsageText(commands, ht, ii, 'k');
            else if (commandName.charAt(0) == 'l')
                ii =printUsageText(commands, ht, ii, 'l');
            else if (commandName.charAt(0) == 'm')
                ii =printUsageText(commands, ht, ii, 'm');
            else if (commandName.charAt(0) == 'n')
                ii =printUsageText(commands, ht, ii, 'n');
            else if (commandName.charAt(0) == 'o')
                ii =printUsageText(commands, ht, ii, 'o');
            else if (commandName.charAt(0) == 'p')
                ii =printUsageText(commands, ht, ii, 'p');
            else if (commandName.charAt(0) == 'q')
                ii =printUsageText(commands, ht, ii, 'q');
            else if (commandName.charAt(0) == 'r')
                ii =printUsageText(commands, ht, ii, 'r');
            else if (commandName.charAt(0) == 's')
                ii =printUsageText(commands, ht, ii, 's');
            else if (commandName.charAt(0) == 't')
                ii =printUsageText(commands, ht, ii, 't');
            else if (commandName.charAt(0) == 'u')
                ii =printUsageText(commands, ht, ii, 'u');
            else if (commandName.charAt(0) == 'v')
                ii =printUsageText(commands, ht, ii, 'v');
            else if (commandName.charAt(0) == 'w')
                ii =printUsageText(commands, ht, ii, 'w');
            else if (commandName.charAt(0) == 'x')
                ii =printUsageText(commands, ht, ii, 'x');
            else if (commandName.charAt(0) == 'y')
                ii =printUsageText(commands, ht, ii, 'y');
            else if (commandName.charAt(0) == 'z')
                ii =printUsageText(commands, ht, ii, 'z');
        }
        System.out.println("<a href='#top'>top</a> <br><br>");
}
    
    

    public static void printHTMLHeader()
    {
        System.out.println("<html>");
        System.out.println("<h2 align=center>asadmin commands list</h2>");
        System.out.println("<p align=center>");
        System.out.println("<a href='http://appserver.red.iplanet.com/apollo/cli/s1as8ee_cli_commands.html'>8.1 EE CLI Commands -- Dev Page</a> </p>");
        System.out.println("<a name='top'/>");
        System.out.println("[<a href='#a'>a</a>],");
        System.out.println("[<a href='#b'>b</a>],");
        System.out.println("[<a href='#c'>c</a>],");
        System.out.println("[<a href='#d'>d</a>],");
        System.out.println("[<a href='#e'>e</a>],");
        System.out.println("[<a href='#f'>f</a>],");
        System.out.println("[<a href='#g'>g</a>],");
        System.out.println("[<a href='#h'>h</a>],");
        System.out.println("[<a href='#i'>i</a>],");
        System.out.println("[<a href='#j'>j</a>],");
        System.out.println("[<a href='#k'>k</a>],");
        System.out.println("[<a href='#l'>l</a>],");
        System.out.println("[<a href='#m'>m</a>],");
        System.out.println("[<a href='#n'>n</a>],");
        System.out.println("[<a href='#o'>o</a>],");
        System.out.println("[<a href='#p'>p</a>],");
        System.out.println("[<a href='#q'>q</a>],");
        System.out.println("[<a href='#r'>r</a>],");
        System.out.println("[<a href='#s'>s</a>],");
        System.out.println("[<a href='#t'>t</a>],");
        System.out.println("[<a href='#u'>u</a>],");
        System.out.println("[<a href='#v'>v</a>],");
        System.out.println("[<a href='#w'>w</a>],");
        System.out.println("[<a href='#x'>x</a>],");
        System.out.println("[<a href='#y'>y</a>],");
        System.out.println("[<a href='#z'>z</a>],");
        System.out.println("[<a href='#usagetext'>Usage Text</a>] <br><br>");
    }

    public static void printHTMLFooter()
    {
        System.out.println("</html>");
    }
    

 
}
