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

/*
 * FormattedWriter.java
 *
 * Created on November 14, 2001, 5:50 PM
 */


package com.sun.persistence.utility.generator.io;

import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

/**
 * @author raccah
 */
class FormattedWriter {
    static private final String lineSeparator = System.getProperty(
            "line.separator"); //NOI18N
    static private final String indent = "    ";	// NOI18N

    private StringBuffer _buffer;
    private int _initialIndents = 0;

    /**
     * Creates new FormattedWriter
     */
    FormattedWriter() {
    }

    private StringBuffer getBuffer() {
        if (_buffer == null) {
            _buffer = new StringBuffer();
        }

        return _buffer;
    }

    /**
     * Returns a string representation of the FormattedWriter.
     * @return The string representation of the internal StringBuffer used by
     *         this object.
     */
    public String toString() {
        return getBuffer().toString();
    }

    void writeComments(final String[] comments) {
        final int n = (comments != null ? comments.length : 0);

        for (int i = 0; i < n; i++) {
            final String s = comments[i];

            writeln("// " + (s != null ? s : ""));	// NOI18N
        }
    }

    private void _write(final int indents, final String s) {
        final StringBuffer buffer = getBuffer();

        if (!s.equals(lineSeparator)) {
            for (int i = 0; i < indents; i++) {
                buffer.append(indent);
            }
        }

        buffer.append(s);
    }

    void write(final int indents, final String s) {
        _write(indents + _initialIndents, s);
    }

    void write(final String s) {
        _write(0, s);
    }

    void writeln(final int indents, final String s) {
        if (_initialIndents > 0) {
            _write(_initialIndents, "");	// NOI18N
        }

        _write(indents, s + lineSeparator);
    }

    void writeln(final String s) {
        writeln(0, s);
    }

    void writeln() {
        writeln(0, "");			// NOI18N
    }

    void writeList(final int indents, final List list,
            final boolean addSeparator) {
        if ((list != null) && (list.size() > 0)) {
            Iterator iterator = list.iterator();

            while (iterator.hasNext()) {
                indent(indents, iterator.next().toString());

                if (addSeparator) {
                    writeln();
                }
            }

            if (!addSeparator) {
                writeln();
            }
        }
    }

    void writeList(final int indents, final List list) {
        writeList(indents, list, false);
    }

    void writeList(final List list) {
        writeList(0, list);
    }

    private void indent(final int indents, final String s) {
        if (s.indexOf(lineSeparator) != -1) {
            StringTokenizer tokenizer = new StringTokenizer(
                    s, lineSeparator, true);

            while (tokenizer.hasMoreTokens()) {
                write(indents, tokenizer.nextToken());
            }
        } else {
            write(indents, s);
        }
    }
}
