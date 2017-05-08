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
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
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

package com.sun.appserv.test.util.results;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

@SuppressWarnings({"IOResourceOpenedButNotSafelyClosed"})
public class ResultsProducer {
    private XMLStreamReader parser;
    private int count = 0;
    private int pass = 0;
    private int fail = 0;
    private int didNotRun = 0;
    private boolean done = false;
    private StringBuilder buffer;
    private File input;

    public ResultsProducer(String inputFile) throws XMLStreamException, FileNotFoundException {
        input = new File(inputFile);
        parser = XMLInputFactory.newInstance().createXMLStreamReader(new FileInputStream(input));
    }

    private void produce() throws XMLStreamException, IOException {
        buffer = new StringBuilder();
        while (hasNext()) {
            readTestCase();
        }
        parser.close();
        line();
        format("PASSED", pass);
        format("FAILED", fail);
        format("DID NOT RUN", didNotRun);
        format("TOTAL", count);
        line();
        System.out.println(buffer);
        FileWriter writer = new FileWriter(new File(input.getParentFile(), "count.txt"));
        writer.write(buffer.toString());
        writer.flush();
        writer.close();
        if (fail != 0) {
            System.err.println("All Tests NOT passed, so returning UNSUCCESS status.");
            System.exit(1);
        }

    }

    private void line() {
        buffer.append("**********************\n");
    }

    private void format(final String result, final int count) {
        buffer.append(String.format("* %-12s %5d *\n", result, count));
    }

    private void readTestCase() throws XMLStreamException {
        skipNonStartElements();
        if (hasNext()) {
            if ("testcase".equals(parser.getLocalName())) {
                process(new TestCase(read("name"),
                    read("status", "value")));
            } else {
                next();
                readTestCase();
            }
        }
    }

    private void process(final TestCase test) {
        count++;
        if(ReporterConstants.PASS.equals(test.getStatus())) {
            pass++;
        } else if(ReporterConstants.FAIL.equals(test.getStatus())) {
            fail++;
        } else if(ReporterConstants.DID_NOT_RUN.equals(test.getStatus())) {
            didNotRun++;
        }
    }

    private String read(final String name) throws XMLStreamException {
        skipTo(name);
        return hasNext() ? parser.getElementText().trim() : null;
    }

    private String read(final String name, final String attr) throws XMLStreamException {
        skipTo(name);
        return hasNext() ? parser.getAttributeValue(null, attr).trim() : null;
    }

    private void skipTo(String name) throws XMLStreamException {
        while (hasNext() && !name.equals(parser.getLocalName())) {
            skipNonStartElements();
        }
    }

    private void skipNonStartElements() throws XMLStreamException {
        while (hasNext() && next() != XMLStreamConstants.START_ELEMENT) {
        }
    }

    private boolean hasNext() {
        return !done && parser.getLocation().getLineNumber() >= 0;
    }

    private int next() throws XMLStreamException {
        final int event = parser.next();
        if (event == XMLStreamConstants.END_DOCUMENT) {
            parser.close();
            done = true;
        }
        return event;
    }

    public static void main(String[] args) throws XMLStreamException, IOException {
        if (args.length < 1) {
            System.err.println("Please specify the input file name");
            return;
        }
        final ResultsProducer producer = new ResultsProducer(args[0]);
        producer.produce();
    }
}
