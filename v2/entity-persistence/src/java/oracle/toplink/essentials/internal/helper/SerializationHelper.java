/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * // Copyright (c) 1998, 2007, Oracle. All rights reserved.
 * 
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
package oracle.toplink.essentials.internal.helper;

import oracle.toplink.essentials.exceptions.ValidationException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;

/**
 * <p>Provide common functionalities for serialization of object.
 * </p>
 *
 * <p>This class throws exceptions for invalid <code>null</code> inputs.
 * Each method documents its behaviour in more detail.</p>
 *
 * @author Steven Vo
 * @since OracleAS 10.0.3
 */
public class SerializationHelper {

    /**
     * <p>Deep clone a Serializable object using serialization.
     * @param the serializable object
     * @return the deep cloned object
     * @throws  IOException, ClassNotFoundException
     */
    public static Object clone(Serializable object) throws IOException, ClassNotFoundException {
        return deserialize(serialize(object));
    }

    /**
     * Serialize the object to an OutputStream
     *
     * @param obj  the object to serialize to bytes
     * @param outputStream  the stream to write to, can not be null
     * @throws IOException
     */
    public static void serialize(Serializable obj, OutputStream outputStream) throws IOException {
        if (outputStream == null) {
            throw ValidationException.invalidNullMethodArguments();
        }
        ObjectOutputStream outStream = null;

        try {
            // stream closed in the finally
            outStream = new ObjectOutputStream(outputStream);
            outStream.writeObject(obj);
        } finally {
            try {
                if (outStream != null) {
                    outStream.close();
                }
            } catch (IOException ex) {
                // ignore;
            }
        }
    }

    /**
     * Serialize the object to a byte array
     *
     * @param obj  the object to serialize to bytes
     * @return a byte[] of the obj
     * @throws IOException
     */
    public static byte[] serialize(Serializable obj) throws IOException {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream(512);
        serialize(obj, outStream);
        return outStream.toByteArray();
    }

    /**
     * Deserialze an object from an InputStream
     *
     * @param inputStream  the serialized object input stream, must not be null
     * @return the deserialized object
     * @throws IOException, ClassNotFoundException
     */
    public static Object deserialize(InputStream inputStream) throws IOException, ClassNotFoundException {
        if (inputStream == null) {
            throw new IllegalArgumentException("The inputStream argument cannot be null");
        }
        ObjectInputStream inStream = null;
        try {
            // stream closed in the finally
            inStream = new ObjectInputStream(inputStream);
            return inStream.readObject();

        } finally {
            try {
                if (inStream != null) {
                    inStream.close();
                }
            } catch (IOException ex) {
                // ignore
            }
        }
    }

    /**
     * Deserialize an object from a byte array
     *
     * @param objectBytes  the serialized object, can not be null
     * @return the deserialized object
     * @throws  IOException, ClassNotFoundException
     */
    public static Object deserialize(byte[] objectBytes) throws IOException, ClassNotFoundException {
        if (objectBytes == null) {
            throw ValidationException.invalidNullMethodArguments();
        }
        ByteArrayInputStream inStream = new ByteArrayInputStream(objectBytes);
        return deserialize(inStream);
    }
}
