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

package com.sun.ejte.ccl.filediff;

import java.io.*;

public class ModifyReport{
    public static void main(String args[]){
        ModifyReport mf = new ModifyReport();
        if(args.length != 1){
            mf.usage();
            System.exit(0);
        }            
        String tobeAdded = 
        "<tr> \n"+
        "<td colspan=3 align=center> <font color=RED> <b> Expected Results </b> </font> </td> \n"+
         "</tr>\n"+
        "<tr>\n"+
         "<td> <b><i> Test Location </i></b> </td>\n"+
         "<td> <b><i> Reported Name </i></b> </td>\n"+
         "<td> <b><i> Expected Count[13] </i></b> </td>\n"+
         "</tr>\n"+
         "<tr> \n"+
         "<td>  sqetests/ejb/stateless/converter </td> \n"+
         "<td> converter mainID </td> \n"+
         "<td> 1 </td> \n"+
         "</tr>\n"+
         "<tr> \n"+
         "<td>  sqetests/ejb/stateless/converter </td> \n"+
         "<td> converter Standalone </td> \n"+
         "<td> 1 </td> \n"+
         "</tr>\n"+
         "<tr> \n"+
         /*"<td>  sqetests/transaction/txglobal </td> \n"+
         "<td> txglobal testTxCommit </td> \n"+
         "<td> 2 </td> \n"+
         "</tr>\n"+
         "<tr> \n"+*/
         "<td>  sqetests/ejb/mdb/simple </td> \n"+
         "<td> simple mdb mainID </td> \n"+
         "<td>  1</td>\n"+
         "</tr>\n"+
         "<tr> \n"+
         "<td>  sqetests/ejb/bmp/enroller </td> \n"+
         "<td> enroller bmpID </td> \n"+
         "<td>  1</td>\n"+
         "</tr>\n"+
         "<tr> \n"+
         "<td>  sqetests/ejb/cmp/roster </td> \n"+
         "<td> cmp roster:insertInfoID </td> \n"+
         "<td>  3</td>\n"+
         "</tr>\n"+
         "<tr> \n"+
         "<td>  sqetests/corba </td> \n"+
         "<td> rmiiiop testID </td> \n"+
         "<td>  1</td>\n"+
         "</tr>\n"+
         "<tr> \n"+
         "<td>  sqetests/connector/cci </td> \n"+
         "<td> Connector:cci Connector resource adapter Test status:ID </td> \n"+
         "<td>  1</td>\n"+
         "</tr>\n"+
         "<tr> \n"+
         "<td>  sqetests/jndi/url </td> \n"+
         "<td> HTMLReader TestID </td> \n"+
         "<td> 1</td>\n"+
         "</tr>\n"+
         "<tr> \n"+
         "<td>  sqetests/web/subclassing </td>\n"+
         "<td> WEBCLIENT </td> \n"+
         "<td> 3</td>\n"+
         "</tr>\n";
/*
         "<tr> \n"+
         "<td>  sqetests/transaction/txglobal </td> \n"+
         "<td> </td> \n"+
         "<td> [Total :1]</td>\n"+         
         "</tr>\n"+
         "<tr> \n"+
         "<td>  sqetests/security </td>\n"+
         "<td> </td> \n"+
         "<td> [Total :1]</td>\n"+
         "</tr>\n"+
 */         
        mf.modifyFile(args[0], tobeAdded);
    }

    public void modifyFile(String filename, String tobeAdded){
        String fileline = "";
        String fileContent = "";
        StringBuffer filedata = new StringBuffer();  // to read file into, and to insert data into
        String tag = "</TABLE>";
        int breakPoint = -1 ;
        int c;
        try{       
            File inputfile = new File(filename);
            BufferedReader in = new BufferedReader(new FileReader(inputfile));
	    //System.out.println("Reading original file...");
            while ((fileline = in.readLine()) != null){            
                filedata.append(fileline);
            }            
	    //System.out.println("Finished Reading file. ");
	    //System.out.println("Closing input stream...");
            in.close();
	    //System.out.println("filedata:\n\n"+filedata);
            // find index of first </TABLE> tag
            // insert new string at the index.
	    //System.out.println("breakPoint:"+breakPoint);
            breakPoint=filedata.indexOf(tag);
            if(breakPoint != -1){
                filedata.insert(breakPoint, tobeAdded);
                fileContent = filedata.toString();
		//System.out.println("string inserted");
            } else {
                System.out.println("specified tag: "+tag+" was not found in the report");
            }
            //System.out.println("fileContent:\n\n"+fileContent);
            FileWriter fw = new FileWriter(inputfile);
            fw.write(fileContent);
            fw.close();
            System.out.println("Added Configuration data into report");
        } catch(FileNotFoundException fnfe){
            System.out.println("File is not present. \n"+
                               "Please check the file name and try again");
        } catch(Exception ex){
            ex.printStackTrace();
        }        
        //System.out.println("============================================");
    }
    public void usage(){
        System.out.println("Usage:");
        System.out.println("Modify Report <filename>");
    }
}
