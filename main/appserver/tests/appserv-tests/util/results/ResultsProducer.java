/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2004-2017 Oracle and/or its affiliates. All rights reserved.
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

import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.channels.FileChannel;

import java.util.StringTokenizer;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.regex.Matcher;

public class ResultsProducer {

    private static Charset charset = Charset.forName("ISO-8859-15");
    private static CharsetDecoder decoder = charset.newDecoder();
    private static Pattern linePattern = Pattern.compile(".*\r?\n");
    private static Pattern pattern;

    public static void main(String[] args) {
        try {
            if (args.length < 1) {
                System.err.println("Usage: java ResultProducer file");
                return;
            }
             
            FileOutputStream outputStream = new FileOutputStream(args[2]);
            FileChannel channel = outputStream.getChannel();
            CharBuffer buffer = CharBuffer.allocate(8192);
            buffer.put("\n************************\n");
            CharBuffer charBuffer = loadFile(new File(args[0]));
            StringTokenizer tokens = new StringTokenizer("pass,fail",",");
            String token;
            int count = 0;
            int pass = 0;
            int fail = 0;
            while( tokens.hasMoreElements() ){
                token = tokens.nextToken(); 
                compile(token);

                count = countOccurance(charBuffer);
                buffer.put(token.toUpperCase() + "ED=   "  + count + "\n");
                buffer.put("------------  =========\n");
                if (token.equals("pass")){
                    pass = count;
                } else {
                    fail = count;
                }
            } 
            buffer.put("DID NOT RUN=   " 
                     +  (new Integer(args[1]).intValue() - (pass + fail))+ "\n");
            buffer.put("------------  =========\n");
            buffer.put("Total Expected=" + args[1]);
            buffer.put("\n************************\n");
            buffer.flip();
            System.out.println(buffer.toString());
            channel.write(ByteBuffer.wrap(buffer.toString().getBytes()));

            if( pass != (new Integer(args[1]).intValue()) )
            {
               System.err.println("All Tests NOT passed, so returning UNSUCCESS status.");
               System.exit(1);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    

    private static void compile(String pat) {
        try {
            pattern = Pattern.compile(pat);
        } catch (PatternSyntaxException x) {
            System.err.println(x.getMessage());
            System.exit(1);
        }
    }


    private static int countOccurance(CharBuffer cb) {
        Matcher lm = linePattern.matcher(cb);
        Matcher pm = null;			
        int count = 0;
        while (lm.find()) {
            CharSequence cs = lm.group(); 	
            if (pm == null)
                pm = pattern.matcher(cs);
            else
                pm.reset(cs);

            if (pm.find()){
                count++;
            }
            if (lm.end() == cb.limit())
            break;
        }
        return count;
    }

    
    private static CharBuffer loadFile(File f) throws IOException {
        FileInputStream fis = new FileInputStream(f);
        FileChannel fc = fis.getChannel();

        int sz = (int)fc.size();
        MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, sz);
        CharBuffer cb = decoder.decode(bb);
        fc.close();
        return cb;
    }

}
