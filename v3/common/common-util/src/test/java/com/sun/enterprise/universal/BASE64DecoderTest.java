/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.enterprise.universal;

import java.io.IOException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author bnevins
 */
public class BASE64DecoderTest {
    /* 
     * make sure the Universal base64 works
     */
    @Test
    public void testEncodeDecode() throws IOException{
        GFBase64Encoder encoder = new GFBase64Encoder();
        GFBase64Decoder decoder = new GFBase64Decoder();
        
        for(String s : ss) {
            byte[] stringAsByteBuf = s.getBytes();
            String enc = encoder.encode(stringAsByteBuf);
            assertFalse(enc.equals(s));
            byte[] decodedByteBuf = decoder.decodeBuffer(enc);
            String dec = new String(decodedByteBuf);
            assertEquals(dec, s);
        }
    }
    
    /* make sure the Universal base64 results match sun.misc
     */
    @Test
    public void testEncodeDecodeAgainstSun() throws IOException{
        com.sun.enterprise.universal.GFBase64Encoder gfEncoder = 
                new com.sun.enterprise.universal.GFBase64Encoder();
        com.sun.enterprise.universal.GFBase64Decoder gfDecoder = 
                new com.sun.enterprise.universal.GFBase64Decoder();
        sun.misc.BASE64Decoder sunDecoder = new sun.misc.BASE64Decoder();
        sun.misc.BASE64Encoder sunEncoder = new sun.misc.BASE64Encoder();
        
        for(String s : ss) {
            byte[] stringAsByteBuf = s.getBytes();
            String gfEnc = gfEncoder.encode(stringAsByteBuf);
            String sunEnc = sunEncoder.encode(stringAsByteBuf);
            
            assertEquals(gfEnc, sunEnc);
            
            byte[] gfDecodedByteBuf = gfDecoder.decodeBuffer(gfEnc);
            byte[] sunDecodedByteBuf = sunDecoder.decodeBuffer(sunEnc);
            
            assertTrue(gfDecodedByteBuf.length == sunDecodedByteBuf.length);
            
            for(int i = 0; i < gfDecodedByteBuf.length; i++)
                assertEquals(gfDecodedByteBuf[i], sunDecodedByteBuf[i]);

            String gfDec = new String(gfDecodedByteBuf);
            String sunDec = new String(sunDecodedByteBuf);
            assertEquals(gfDec, s);
            assertEquals(gfDec, sunDec);
        }
    }
    
    private static final String[] ss = new String[]
    {
        "foo", "QQ234bbVVc", "\n\n\r\f\n"
    };
}
