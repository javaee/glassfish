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


package javax.jms;

/** A <CODE>StreamMessage</CODE> object is used to send a stream of primitive
  * types in the Java programming language. It is filled and read sequentially.
  * It inherits from the <CODE>Message</CODE> interface
  * and adds a stream message body. Its methods are based largely on those
  * found in <CODE>java.io.DataInputStream</CODE> and
  * <CODE>java.io.DataOutputStream</CODE>.
  *
  * <P>The primitive types can be read or written explicitly using methods
  * for each type. They may also be read or written generically as objects.
  * For instance, a call to <CODE>StreamMessage.writeInt(6)</CODE> is
  * equivalent to <CODE>StreamMessage.writeObject(new Integer(6))</CODE>.
  * Both forms are provided, because the explicit form is convenient for
  * static programming, and the object form is needed when types are not known
  * at compile time.
  *
  * <P>When the message is first created, and when <CODE>clearBody</CODE>
  * is called, the body of the message is in write-only mode. After the 
  * first call to <CODE>reset</CODE> has been made, the message body is in 
  * read-only mode. 
  * After a message has been sent, the client that sent it can retain and 
  * modify it without affecting the message that has been sent. The same message
  * object can be sent multiple times.
  * When a message has been received, the provider has called 
  * <CODE>reset</CODE> so that the message body is in read-only mode for the client.
  * 
  * <P>If <CODE>clearBody</CODE> is called on a message in read-only mode, 
  * the message body is cleared and the message body is in write-only mode.
  * 
  * <P>If a client attempts to read a message in write-only mode, a 
  * <CODE>MessageNotReadableException</CODE> is thrown.
  * 
  * <P>If a client attempts to write a message in read-only mode, a 
  * <CODE>MessageNotWriteableException</CODE> is thrown.
  *
  * <P><CODE>StreamMessage</CODE> objects support the following conversion 
  * table. The marked cases must be supported. The unmarked cases must throw a 
  * <CODE>JMSException</CODE>. The <CODE>String</CODE>-to-primitive conversions 
  * may throw a runtime exception if the primitive's <CODE>valueOf()</CODE> 
  * method does not accept it as a valid <CODE>String</CODE> representation of 
  * the primitive.
  *
  * <P>A value written as the row type can be read as the column type.
  *
  * <PRE>
  * |        | boolean byte short char int long float double String byte[]
  * |----------------------------------------------------------------------
  * |boolean |    X                                            X
  * |byte    |          X     X         X   X                  X   
  * |short   |                X         X   X                  X   
  * |char    |                     X                           X
  * |int     |                          X   X                  X   
  * |long    |                              X                  X   
  * |float   |                                    X     X      X   
  * |double  |                                          X      X   
  * |String  |    X     X     X         X   X     X     X      X   
  * |byte[]  |                                                        X
  * |----------------------------------------------------------------------
  * </PRE>
  *
  * <P>Attempting to read a null value as a primitive type must be treated
  * as calling the primitive's corresponding <code>valueOf(String)</code> 
  * conversion method with a null value. Since <code>char</code> does not 
  * support a <code>String</code> conversion, attempting to read a null value 
  * as a <code>char</code> must throw a <code>NullPointerException</code>.
  *
  * @version     1.0 - 6 August 1998
  * @author      Mark Hapner
  * @author      Rich Burridge
  *
  * @see         javax.jms.Session#createStreamMessage()
  * @see         javax.jms.BytesMessage
  * @see         javax.jms.MapMessage
  * @see         javax.jms.Message
  * @see         javax.jms.ObjectMessage
  * @see         javax.jms.TextMessage
  */

public interface StreamMessage extends Message {


    /** Reads a <code>boolean</code> from the stream message.
      *
      * @return the <code>boolean</code> value read
      *
      * @exception JMSException if the JMS provider fails to read the message
      *                         due to some internal error.
      * @exception MessageEOFException if unexpected end of message stream has
      *                                been reached.     
      * @exception MessageFormatException if this type conversion is invalid.
      * @exception MessageNotReadableException if the message is in write-only 
      *                                        mode.
      */

    boolean 
    readBoolean() throws JMSException;


    /** Reads a <code>byte</code> value from the stream message.
      *
      * @return the next byte from the stream message as a 8-bit
      * <code>byte</code>
      *
      * @exception JMSException if the JMS provider fails to read the message
      *                         due to some internal error.
      * @exception MessageEOFException if unexpected end of message stream has
      *                                been reached.     
      * @exception MessageFormatException if this type conversion is invalid.
      * @exception MessageNotReadableException if the message is in write-only 
      *                                        mode.
      */ 

    byte 
    readByte() throws JMSException;


    /** Reads a 16-bit integer from the stream message.
      *
      * @return a 16-bit integer from the stream message
      *
      * @exception JMSException if the JMS provider fails to read the message
      *                         due to some internal error.
      * @exception MessageEOFException if unexpected end of message stream has
      *                                been reached.     
      * @exception MessageFormatException if this type conversion is invalid.
      * @exception MessageNotReadableException if the message is in write-only 
      *                                        mode.
      */ 

    short 
    readShort() throws JMSException;


    /** Reads a Unicode character value from the stream message.
      *
      * @return a Unicode character from the stream message
      *
      * @exception JMSException if the JMS provider fails to read the message
      *                         due to some internal error.
      * @exception MessageEOFException if unexpected end of message stream has
      *                                been reached.     
      * @exception MessageFormatException if this type conversion is invalid      
      * @exception MessageNotReadableException if the message is in write-only 
      *                                        mode.
      */ 

    char 
    readChar() throws JMSException;


    /** Reads a 32-bit integer from the stream message.
      *
      * @return a 32-bit integer value from the stream message, interpreted
      * as an <code>int</code>
      *
      * @exception JMSException if the JMS provider fails to read the message
      *                         due to some internal error.
      * @exception MessageEOFException if unexpected end of message stream has
      *                                been reached.     
      * @exception MessageFormatException if this type conversion is invalid.
      * @exception MessageNotReadableException if the message is in write-only 
      *                                        mode.
      */ 

    int 
    readInt() throws JMSException;


    /** Reads a 64-bit integer from the stream message.
      *
      * @return a 64-bit integer value from the stream message, interpreted as
      * a <code>long</code>
      *
      * @exception JMSException if the JMS provider fails to read the message
      *                         due to some internal error.
      * @exception MessageEOFException if unexpected end of message stream has
      *                                been reached.     
      * @exception MessageFormatException if this type conversion is invalid.
      * @exception MessageNotReadableException if the message is in write-only 
      *                                        mode.
      */ 

    long 
    readLong() throws JMSException;


    /** Reads a <code>float</code> from the stream message.
      *
      * @return a <code>float</code> value from the stream message
      *
      * @exception JMSException if the JMS provider fails to read the message
      *                         due to some internal error.
      * @exception MessageEOFException if unexpected end of message stream has
      *                                been reached.     
      * @exception MessageFormatException if this type conversion is invalid.
      * @exception MessageNotReadableException if the message is in write-only 
      *                                        mode.
      */ 

    float 
    readFloat() throws JMSException;


    /** Reads a <code>double</code> from the stream message.
      *
      * @return a <code>double</code> value from the stream message
      *
      * @exception JMSException if the JMS provider fails to read the message
      *                         due to some internal error.
      * @exception MessageEOFException if unexpected end of message stream has
      *                                been reached.     
      * @exception MessageFormatException if this type conversion is invalid.
      * @exception MessageNotReadableException if the message is in write-only 
      *                                        mode.
      */ 

    double 
    readDouble() throws JMSException;


    /** Reads a <CODE>String</CODE> from the stream message.
      *
      * @return a Unicode string from the stream message
      *
      * @exception JMSException if the JMS provider fails to read the message
      *                         due to some internal error.
      * @exception MessageEOFException if unexpected end of message stream has
      *                                been reached.     
      * @exception MessageFormatException if this type conversion is invalid.
      * @exception MessageNotReadableException if the message is in write-only 
      *                                        mode.
      */ 

    String 
    readString() throws JMSException;


    /** Reads a byte array field from the stream message into the 
      * specified <CODE>byte[]</CODE> object (the read buffer). 
      * 
      * <P>To read the field value, <CODE>readBytes</CODE> should be 
      * successively called 
      * until it returns a value less than the length of the read buffer.
      * The value of the bytes in the buffer following the last byte 
      * read is undefined.
      * 
      * <P>If <CODE>readBytes</CODE> returns a value equal to the length of the 
      * buffer, a subsequent <CODE>readBytes</CODE> call must be made. If there 
      * are no more bytes to be read, this call returns -1.
      * 
      * <P>If the byte array field value is null, <CODE>readBytes</CODE> 
      * returns -1.
      *
      * <P>If the byte array field value is empty, <CODE>readBytes</CODE> 
      * returns 0.
      * 
      * <P>Once the first <CODE>readBytes</CODE> call on a <CODE>byte[]</CODE>
      * field value has been made,
      * the full value of the field must be read before it is valid to read 
      * the next field. An attempt to read the next field before that has 
      * been done will throw a <CODE>MessageFormatException</CODE>.
      * 
      * <P>To read the byte field value into a new <CODE>byte[]</CODE> object, 
      * use the <CODE>readObject</CODE> method.
      *
      * @param value the buffer into which the data is read
      *
      * @return the total number of bytes read into the buffer, or -1 if 
      * there is no more data because the end of the byte field has been 
      * reached
      *
      * @exception JMSException if the JMS provider fails to read the message
      *                         due to some internal error.
      * @exception MessageEOFException if unexpected end of message stream has
      *                                been reached.     
      * @exception MessageFormatException if this type conversion is invalid.
      * @exception MessageNotReadableException if the message is in write-only 
      *                                        mode.
      * 
      * @see #readObject()
      */ 

    int
    readBytes(byte[] value) throws JMSException;


    /** Reads an object from the stream message.
      *
      * <P>This method can be used to return, in objectified format,
      * an object in the Java programming language ("Java object") that has 
      * been written to the stream with the equivalent
      * <CODE>writeObject</CODE> method call, or its equivalent primitive
      * <CODE>write<I>type</I></CODE> method.
      *  
      * <P>Note that byte values are returned as <CODE>byte[]</CODE>, not 
      * <CODE>Byte[]</CODE>.
      *
      * <P>An attempt to call <CODE>readObject</CODE> to read a byte field 
      * value into a new <CODE>byte[]</CODE> object before the full value of the
      * byte field has been read will throw a 
      * <CODE>MessageFormatException</CODE>.
      *
      * @return a Java object from the stream message, in objectified
      * format (for example, if the object was written as an <CODE>int</CODE>, 
      * an <CODE>Integer</CODE> is returned)
      *
      * @exception JMSException if the JMS provider fails to read the message
      *                         due to some internal error.
      * @exception MessageEOFException if unexpected end of message stream has
      *                                been reached.     
      * @exception MessageFormatException if this type conversion is invalid.
      * @exception MessageNotReadableException if the message is in write-only 
      *                                        mode.
      * 
      * @see #readBytes(byte[] value)
      */ 

    Object 
    readObject() throws JMSException;



    /** Writes a <code>boolean</code> to the stream message.
      * The value <code>true</code> is written as the value 
      * <code>(byte)1</code>; the value <code>false</code> is written as 
      * the value <code>(byte)0</code>.
      *
      * @param value the <code>boolean</code> value to be written
      *
      * @exception JMSException if the JMS provider fails to write the message
      *                         due to some internal error.
      * @exception MessageNotWriteableException if the message is in read-only 
      *                                         mode.
      */

    void 
    writeBoolean(boolean value) 
			throws JMSException;


    /** Writes a <code>byte</code> to the stream message.
      *
      * @param value the <code>byte</code> value to be written
      *
      * @exception JMSException if the JMS provider fails to write the message
      *                         due to some internal error.
      * @exception MessageNotWriteableException if the message is in read-only 
      *                                         mode.
      */ 

    void 
    writeByte(byte value) throws JMSException;


    /** Writes a <code>short</code> to the stream message.
      *
      * @param value the <code>short</code> value to be written
      *
      * @exception JMSException if the JMS provider fails to write the message
      *                         due to some internal error.
      * @exception MessageNotWriteableException if the message is in read-only 
      *                                         mode.
      */ 

    void 
    writeShort(short value) throws JMSException;


    /** Writes a <code>char</code> to the stream message.
      *
      * @param value the <code>char</code> value to be written
      *
      * @exception JMSException if the JMS provider fails to write the message
      *                         due to some internal error.
      * @exception MessageNotWriteableException if the message is in read-only 
      *                                         mode.
      */ 

    void 
    writeChar(char value) throws JMSException;


    /** Writes an <code>int</code> to the stream message.
      *
      * @param value the <code>int</code> value to be written
      *
      * @exception JMSException if the JMS provider fails to write the message
      *                         due to some internal error.
      * @exception MessageNotWriteableException if the message is in read-only 
      *                                         mode.
      */ 

    void 
    writeInt(int value) throws JMSException;


    /** Writes a <code>long</code> to the stream message.
      *
      * @param value the <code>long</code> value to be written
      *
      * @exception JMSException if the JMS provider fails to write the message
      *                         due to some internal error.
      * @exception MessageNotWriteableException if the message is in read-only 
      *                                         mode.
      */ 

    void 
    writeLong(long value) throws JMSException;


    /** Writes a <code>float</code> to the stream message.
      *
      * @param value the <code>float</code> value to be written
      *
      * @exception JMSException if the JMS provider fails to write the message
      *                         due to some internal error.
      * @exception MessageNotWriteableException if the message is in read-only 
      *                                         mode.
      */ 

    void 
    writeFloat(float value) throws JMSException;


    /** Writes a <code>double</code> to the stream message.
      *
      * @param value the <code>double</code> value to be written
      *
      * @exception JMSException if the JMS provider fails to write the message
      *                         due to some internal error.
      * @exception MessageNotWriteableException if the message is in read-only 
      *                                         mode.
      */ 

    void 
    writeDouble(double value) throws JMSException;


    /** Writes a <code>String</code> to the stream message.
      *
      * @param value the <code>String</code> value to be written
      *
      * @exception JMSException if the JMS provider fails to write the message
      *                         due to some internal error.
      * @exception MessageNotWriteableException if the message is in read-only 
      *                                         mode.
      */ 

    void 
    writeString(String value) throws JMSException;


    /** Writes a byte array field to the stream message.
      *
      * <P>The byte array <code>value</code> is written to the message
      * as a byte array field. Consecutively written byte array fields are 
      * treated as two distinct fields when the fields are read.
      * 
      * @param value the byte array value to be written
      *
      * @exception JMSException if the JMS provider fails to write the message
      *                         due to some internal error.
      * @exception MessageNotWriteableException if the message is in read-only 
      *                                         mode.
      */ 

    void
    writeBytes(byte[] value) throws JMSException;


    /** Writes a portion of a byte array as a byte array field to the stream 
      * message.
      *  
      * <P>The a portion of the byte array <code>value</code> is written to the
      * message as a byte array field. Consecutively written byte 
      * array fields are treated as two distinct fields when the fields are 
      * read.
      *
      * @param value the byte array value to be written
      * @param offset the initial offset within the byte array
      * @param length the number of bytes to use
      *
      * @exception JMSException if the JMS provider fails to write the message
      *                         due to some internal error.
      * @exception MessageNotWriteableException if the message is in read-only 
      *                                         mode.
      */ 
 
    void
    writeBytes(byte[] value, int offset, int length) 
			throws JMSException;


    /** Writes an object to the stream message.
      *
      * <P>This method works only for the objectified primitive
      * object types (<code>Integer</code>, <code>Double</code>, 
      * <code>Long</code>&nbsp;...), <code>String</code> objects, and byte 
      * arrays.
      *
      * @param value the Java object to be written
      *
      * @exception JMSException if the JMS provider fails to write the message
      *                         due to some internal error.
      * @exception MessageFormatException if the object is invalid.
      * @exception MessageNotWriteableException if the message is in read-only 
      *                                         mode.
      */ 

    void 
    writeObject(Object value) throws JMSException;


    /** Puts the message body in read-only mode and repositions the stream
      * to the beginning.
      *  
      * @exception JMSException if the JMS provider fails to reset the message
      *                         due to some internal error.
      * @exception MessageFormatException if the message has an invalid
      *                                   format.
      */ 
 
    void
    reset() throws JMSException;
}
