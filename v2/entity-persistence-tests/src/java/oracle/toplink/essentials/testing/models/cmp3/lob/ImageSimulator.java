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
// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.essentials.testing.models.cmp3.lob;

import java.util.Random;

/**
 * The object simulates a set of byte array, char array and string to create
 */

public class ImageSimulator{
  public static Image generateImageNullLOB() {
    Image imageNullLOB = new Image();
    imageNullLOB.setId(2000);
    imageNullLOB.setPicture(null);
    imageNullLOB.setScript(null);
    imageNullLOB.setAudio(null);
    imageNullLOB.setCommentary(null);
    imageNullLOB.setCustomAttribute1(null);
    imageNullLOB.setCustomAttribute2(null);
 
    return imageNullLOB;
  }

  public static Image generateImage(int blobSize, int clobSize) {
    Image generatedImage = new Image();
    generatedImage.setId(1000);
    generatedImage.setPicture(initObjectByteBase(blobSize));
    generatedImage.setScript(initStringBase(clobSize/100));
    generatedImage.setAudio(initByteBase(blobSize));
    generatedImage.setCommentary(initCharArrayBase(clobSize));
    generatedImage.setCustomAttribute1(new SerializableNonEntity(new Long(Long.MAX_VALUE)));
    generatedImage.setCustomAttribute2(new SerializableNonEntity(new Long(Long.MAX_VALUE)));
   
    return generatedImage;
  }
  
  public static String initStringBase(int cycle){
    StringBuffer base = new StringBuffer();
    for(int count = 0; count <cycle; count++) {
      base.append("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
    }
    return base.toString();
  }

  public static char [] initCharArrayBase(int cycle){
    char [] base = new char[cycle];
    for(int count = 0; count <cycle; count++) {
      base[count] = 'c';
    }
    return base;
  }
  public static byte[] initByteBase(int cycle){
    byte[] pictures = new byte[cycle];
    new Random().nextBytes(pictures);
    return pictures;
  }

  public static Byte[] initObjectByteBase(int cycle){
    byte[] pictures = new byte[cycle];
    new Random().nextBytes(pictures);
	Byte[] pics = new Byte[cycle];
    for (int x = 0; x < cycle; x++) {
		pics[x] = new Byte(pictures[x]);
	}
	return pics;
  }
}