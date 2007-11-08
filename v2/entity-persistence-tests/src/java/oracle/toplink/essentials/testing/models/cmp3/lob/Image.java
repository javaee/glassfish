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
import java.io.Serializable;
import javax.persistence.*;

@Entity
@Table(name="CMP3_IMAGE")
@SecondaryTable(name="CMP3_CLIP")
@PrimaryKeyJoinColumn(name="ID", referencedColumnName="ID")

public class Image implements Serializable {
    private int id;
    private byte[] audio;
    private char[] commentary;
    private Byte[] picture;
    private String script;
    private SerializableNonEntity customAttribute1;
    private SerializableNonEntity customAttribute2;

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Lob @Column(table="CMP3_CLIP")
    public byte[] getAudio(){
        return audio;
    }

    @Lob @Column(table="CMP3_CLIP")
    public char[] getCommentary()    {
        return commentary;
    }
  
    @Lob
    public SerializableNonEntity getCustomAttribute1() {
        return customAttribute1;
    }
  

    public SerializableNonEntity getCustomAttribute2() {
        return customAttribute2;
    }

    @Id
    public int getId(){
        return id;
    }

    @Lob
    public Byte[] getPicture(){
        return picture;
    }

    @Lob
    public String getScript()    {
        return script;
    }

    public void setAudio(byte[] audio)    {
        this.audio = audio;
    }

    public void setCommentary(char[] commentary)    {
        this.commentary = commentary;
    }
  
    public void setCustomAttribute1(SerializableNonEntity customAttribute) {
        this.customAttribute1= customAttribute;
    }
  
    public void setCustomAttribute2(SerializableNonEntity customAttribute) {
        this.customAttribute2=customAttribute;
    }

    public void setId(int id)    {
        this.id = id;
    }

    public void setPicture(Byte[] picture)    {
        this.picture = picture;
    }

    public void setScript(String script)    {
        this.script = script;
    }
}