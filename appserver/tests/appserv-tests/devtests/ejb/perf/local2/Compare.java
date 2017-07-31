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

import java.io.*;
import java.util.StringTokenizer;

public class Compare {


    public static void main(String args[]) {


        String file1Name = args[0];
        String file2Name = args[1];

        File file1 = new File(file1Name);
        File file2 = new File(file2Name);
        
        float allResults = 0;
        int totalResults = 0;

        try {

            LineNumberReader reader1 = new LineNumberReader
                (new FileReader(file1));

            LineNumberReader reader2 = new LineNumberReader
                (new FileReader(file2));
            
            String nextLine1 = reader1.readLine();

            while(nextLine1 != null) {
                String nextLine2 = reader2.readLine();
                
                StringTokenizer tokenizer1 = 
                    new StringTokenizer(nextLine1);
                StringTokenizer tokenizer2 = 
                    new StringTokenizer(nextLine2);                
                StringBuffer category = new StringBuffer();
                boolean tx = true;
                int numResults = 0;
                while(tokenizer1.hasMoreTokens()) {
                    String nextToken1 = tokenizer1.nextToken();
                    String nextToken2 = tokenizer2.nextToken();

                    if( Character.isDigit(nextToken1.charAt(0)) ) {
                        numResults++;
                        float float1 = Float.parseFloat(nextToken1);
                        float float2 = Float.parseFloat(nextToken2);
                        float dif = float1 - float2;
                        float pcg =  dif / float1 * 100;

                        allResults += pcg;
                        totalResults++;

                        System.out.printf("%16s   %9.3f   %9.3f   %9.3f   %6.2f  %s\n",
                                          category.toString(), float1, float2, dif, pcg, tx ? "TX" : "NO_TX");
                        //                        System.out.println(category.toString() + "\t" + float1 + "\t" + float2 + "\t" + dif + "\t" + pcg);
                        tx = false;
                    } else {
                        if( nextToken1.indexOf("[exec]") == -1 ) {
                            category.append(nextToken1);
                        }
                    }
                }
                if( numResults == 0 ) {
                    System.out.println(category);
                }

                nextLine1 = reader1.readLine();
                System.out.println();
            } 

            System.out.println("total results = " + totalResults);
            System.out.println("avg pcg dif = " + allResults / totalResults);

        } catch(Exception e) {
            e.printStackTrace();
        }
    }




}
