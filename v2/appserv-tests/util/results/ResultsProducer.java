/*
 * Copyright 2004-2005 Sun Microsystems, Inc.  All rights reserved.
 * Use is subject to license terms.
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
