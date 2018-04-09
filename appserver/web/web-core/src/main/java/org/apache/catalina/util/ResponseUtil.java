/*
 * Copyright (c) 1997-2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright 2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.catalina.util;

import javax.servlet.ServletOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Reader;

public final class ResponseUtil {

    /**
     * Copies the contents of the specified input stream to the specified
     * output stream.
     *
     * @param istream The input stream to read from
     * @param ostream The output stream to write to
     *
     * @return Exception that occurred during processing, or null
     */
    public static IOException copy(InputStream istream,
                                   ServletOutputStream ostream) {

        IOException exception = null;
        byte buffer[] = new byte[2048];
        int len;
        while (true) {
            try {
                len = istream.read(buffer);
                if (len == -1)
                    break;
                ostream.write(buffer, 0, len);
            } catch (IOException e) {
                exception = e;
                len = -1;
                break;
            }
        }
        return exception;

    }


    /**
     * Copies the contents of the specified input stream to the specified
     * output stream.
     *
     * @param reader The reader to read from
     * @param writer The writer to write to
     *
     * @return Exception that occurred during processing, or null
     */
    public static IOException copy(Reader reader, PrintWriter writer) {

        IOException exception = null;
        char buffer[] = new char[2048];
        int len;
        while (true) {
            try {
                len = reader.read(buffer);
                if (len == -1)
                    break;
                writer.write(buffer, 0, len);
            } catch (IOException e) {
                exception = e;
                len = -1;
                break;
            }
        }
        return exception;

    }
}
