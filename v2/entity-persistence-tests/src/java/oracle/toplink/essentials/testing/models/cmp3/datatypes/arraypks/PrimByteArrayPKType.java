/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the "License").  You may not use this file except 
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt or 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html. 
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * HEADER in each file and include the License file at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt.  If applicable, 
 * add the following below this CDDL HEADER, with the 
 * fields enclosed by brackets "[]" replaced with your 
 * own identifying information: Portions Copyright [yyyy] 
 * [name of copyright owner]
 */
// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.essentials.testing.models.cmp3.datatypes.arraypks;

import java.util.UUID;
import javax.persistence.*;

@Entity
@Table(name = "CMP3_PBYTEARRAYPK_TYPE")
public class PrimByteArrayPKType implements java.io.Serializable{
    public PrimByteArrayPKType() {
    }

    private byte[] id;

    public PrimByteArrayPKType(byte[] primitiveByteArrayData)
    {
        this.id = primitiveByteArrayData;
    }

    @Id
    public byte[] getId()
    {
        return id;
    }

    public void setId(byte[] id)
    {
        this.id= id;
    }

    private static final int UUID_LENGTH = 0x10;
    private static int BITSPERLONG = 0x40;
    private static int BITSPERBYTE = 0x8;
    
    public void createRandomId() {
        UUID uuid = UUID.randomUUID();
        id = getBytes(uuid);
    }
    
    public static byte[] getBytes(UUID u) {
        byte [] raw = new byte [UUID_LENGTH];
        long msb = u.getMostSignificantBits();
        long lsb = u.getLeastSignificantBits();
        
        /*
         * Convert 2 longs to 16 bytes. 
         */
        int i = 0;
        for (int sh = BITSPERLONG - BITSPERBYTE; sh >= 0; sh -= BITSPERBYTE) {
            raw [i++] = (byte) (msb >> sh);
        }        
        for (int sh = BITSPERLONG - BITSPERBYTE; sh >= 0; sh -= BITSPERBYTE) {
            raw [i++] = (byte) (lsb >> sh);
        } 
        return raw;
    }

}
