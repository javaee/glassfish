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

package com.sun.enterprise.admin.common;

import java.util.*;
import java.io.*;

import com.sun.enterprise.v3.server.V3Environment;

// i18n import
import com.sun.enterprise.admin.util.SOMLocalStringsManager;

public class InitConfFileBean
{
    public final static String INITCONF_SECURITY_ATTRIBUTE = "Security";
    public final static String INITCONF_VALUE_ON = "on";
    public final static String INITCONF_VALUE_OFF = "off";

    private List storage ;
    private List init_storage ;
    private int position;
    private int init_position;
    private Hashtable index;
    private Hashtable init_index;
    private String mag_file;

    // i18n SOMLocalStringsManager
    private static SOMLocalStringsManager localizedStrMgr =
            SOMLocalStringsManager.getManager( InitConfFileBean.class );
    
    public InitConfFileBean()
    {
        storage = Collections.synchronizedList( new LinkedList());
        init_storage = Collections.synchronizedList( new LinkedList());
        index = new Hashtable();
        init_index = new Hashtable();
        position=0;
        init_position = 0;
    }
    
    public void initialise(String instanceName, boolean bBackupFile) throws IOException
    {
        String confFile;
        confFile = com.sun.enterprise.v3.server.Globals.getGlobals().getDefaultHabitat().getComponent(V3Environment.class).getInitFilePath();
        initialize(confFile);
    }
    
    
    private void initialize(String confFile) throws IOException
    {       
        mag_file = confFile;
        storage = Collections.synchronizedList( new LinkedList());
        index = new Hashtable();
        position=0;
        init_storage = Collections.synchronizedList( new LinkedList());
        init_index = new Hashtable();
        init_position=0;
        
        readConfig(confFile);
    }
    
    public void readConfig(String fileName) throws IOException
    {
        File inputFile = new File(fileName) ;
        BufferedReader in = new BufferedReader( new FileReader(inputFile));
        String inLine = null;
        int spaceIndex=0;
        while ( (inLine = in.readLine() )!= null )
        {
            if ( inLine.length() > 2 )
            {
                spaceIndex = inLine.indexOf(" ");
                if ((spaceIndex == -1) && !(inLine.startsWith("Init"))) {
					String msg = localizedStrMgr.getString( "admin.common.wrong_filename_format" );
                    throw new IOException( msg );
				}
                String key;
                if (!inLine.startsWith("Init"))
                    key=inLine.substring(0,spaceIndex);
                else
                    key="Init";
                
                if (!key.equals("Init"))
                {
                    Hashtable temp = new Hashtable();
                    temp.put(key,  inLine.substring(spaceIndex +1 ));
                    synchronized(storage)
                    {
                        storage.add(temp);
                    }
                    index.put(key,new Integer(position));
                    position++;
                }
                else
                {
                    String tempString = "" ;
                    if (spaceIndex != -1)
                        tempString = inLine.substring(spaceIndex + 1);
                    Hashtable temp = new Hashtable();
                    while (inLine != null)
                    {
                        in.mark(500);
                        inLine = in.readLine();
                        if (inLine != null)
                        {
                            if ((inLine.startsWith("\t")) ||
                            (inLine.startsWith(" ")))
                            {
                                tempString += inLine;
                            }
                            else
                            {
                                in.reset();
                                break;
                            }
                        }
                    }
                    temp.put(key, tempString);
                    synchronized(init_storage)
                    {
                        init_storage.add(temp);
                    }
                    init_index.put(key,new Integer(position));
                    init_position++;
                }
                
            }
        }
        
    }
    
    public void writeConfig(String fileName) throws IOException
    {
        PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(fileName)));
        synchronized( storage)
        {
            ListIterator storageIter = storage.listIterator(0);
            while(storageIter.hasNext() )
            {
                Hashtable temp_store = ( Hashtable) (storageIter.next());
                Enumeration e = temp_store.keys();
                while ( e.hasMoreElements() )
                {
                    String temp_key =(String ) e.nextElement();
                    String temp_val = ( String ) temp_store.get(temp_key) ;
                    String temp_value= temp_key+" "+temp_val;
                    
                    if (temp_val != null && !temp_val.trim().equals(""))
                        out.println(temp_value);
                }
                out.flush();
            }
            out.println();
        }
        synchronized( init_storage)
        {
            ListIterator storageIter = init_storage.listIterator(0);
            while(storageIter.hasNext() )
            {
                Hashtable temp_store = ( Hashtable) (storageIter.next());
                Enumeration e = temp_store.keys();
                while ( e.hasMoreElements() )
                {
                    String temp_key =(String ) e.nextElement();
                    String temp_val = (String ) temp_store.get(temp_key) ;
                    String temp_value= temp_key+" "+temp_val;
                    
                    if (temp_val != null && !temp_val.trim().equals(""))
                        out.println(temp_value);
                    //out.println(temp_val);
                }
                out.flush();
            }
        }
        out.close();
    }
    
    public void dump() throws IOException
    {
        writeConfig(mag_file);
    }
    
    private int searchIndex( String varName )
    {
        int indexHash = -1;
        
        Object o = index.get(varName);
        if (o != null)
            indexHash = (( Integer)(index.get(varName))).intValue();
        return indexHash;
    }
    private synchronized void remake_index(Hashtable table )
    {
        int in_size = 0;
        Enumeration e = table.keys();
        while(e.hasMoreElements() )
        {
            table.put(e.nextElement(),new Integer(in_size));
            in_size++;
        }
    }
    private synchronized void syncIndex(String varName , int mode )
    {
        switch (mode )
        {
            case 0 :
                if ( index.containsKey(varName ) )
                {
                    index.remove(varName);
                    this.remake_index(index);
                }
                break;
                
            default :
                if ( !index.containsKey(varName ) )
                {
                    index.put(varName,new Integer(storage.size() - 1));
                }
                break;
        }
    }
    public String get_mag_var( String varName )
    {
        int iterIndex = this.searchIndex(varName);
        String retVal= "";
        if (iterIndex != -1)
            synchronized(storage)
            {
                retVal =( String )   (( Hashtable) (storage.get(iterIndex))).get(varName);
            }
            return retVal;
    }
    public void set_mag_var( String varName , String value )
    {
        int iterIndex = this.searchIndex(varName);
        Hashtable temp = new Hashtable();
        temp.put(varName,value);
        if (iterIndex != -1)
        {
            synchronized(storage)
            {
                storage.set(iterIndex,temp);
            }
        }
        else
            synchronized(storage)
            {
                storage.add(temp);
                this.syncIndex(varName , 1);
            }
    }
    
    public static void main( String[] args ) throws IOException
    {
    /*
    MagObj testObj = new MagObj();
    testObj.readConfig("magnus.conf", "sRoot");
    testObj.set_mag_var("Security","On");
    testObj.writeConfig("magnus.conf");
     */
    }
    
    public String isSelected(String varName, String val)
    {
        String value = this.get_mag_var(varName);
        if (value.equals(val))
            return "SELECTED";
        else
            return "";
    }
}


