/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2009-2017 Oracle and/or its affiliates. All rights reserved.
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
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

@SuppressWarnings({"IOResourceOpenedButNotSafelyClosed"})
public class HtmlReportProducer {
    private XMLEventReader reader;
    private XMLEvent event;
    private final static boolean print = false;
    private Stack<Object> context = new Stack<Object>();
    private StringBuilder buffer = new StringBuilder();
    private File input;
    private ReportHandler handler;
    private List<TestSuite> suites = new ArrayList<TestSuite>();
    private final boolean failOnError;

    public HtmlReportProducer(String inputFile) throws FileNotFoundException {
        this(inputFile, true);
    }
    public HtmlReportProducer(String inputFile, boolean fail) throws FileNotFoundException {
        input = new File(generateValidReport(inputFile));
        // Use the default (non-validating) parser
        handler = new ReportHandler(new File(input.getParentFile(), "test_results.html"));
        failOnError = fail;
    }

    public void produce() throws IOException, XMLStreamException {
        reader = XMLInputFactory.newInstance().createXMLEventReader(new FileReader(input));
        try {
            //noinspection LoopConditionNotUpdatedInsideLoop
            while (nextEvent() != null) {
                if (event.isStartElement()) {
                    getHandler(event).run();
                } else if (event.isEndElement()) {
                    if (getHandler(event) != null && !context.isEmpty()) {
//                        handler.process(pop());
                        Object obj = pop();
                        if (obj instanceof TestSuite) {
                            TestSuite suite = (TestSuite) obj;
                            suites.add(suite);
                        } else if (obj instanceof Test) {
                            pop();
                            ((TestSuite) context.peek()).addTest((Test) obj);
                            tests();
                        } else if (obj instanceof TestCase) {
                            pop();
                            ((Test) context.peek()).addTestCase((TestCase) obj);
                            testcases();
                        }
                    }
                }
            }
            for (TestSuite testSuite : suites) {
                handler.process(testSuite);
            }
            handler.printHtml();
            line();
            format("PASSED", handler.pass);
            format("FAILED", handler.fail);
            format("DID NOT RUN", handler.didNotRun);
            format("TOTAL", handler.testCaseCount);
            line();
            System.out.println(buffer);
            FileWriter resultsWriter = new FileWriter(new File(input.getParentFile(), "count.txt"));
            resultsWriter.write(buffer.toString());
            resultsWriter.flush();
            resultsWriter.close();
            if (failOnError && handler.fail != 0) {
                System.err.println("All Tests NOT passed, so returning FAILED status.");
                System.exit(1);
            }
        } catch(Exception e) {
            System.out.println("HtmlReportProducer.produce: event = " + event);
            System.out.println(
                "HtmlReportProducer.produce: event.getLocation().getLineNumber() = " + event.getLocation()
                    .getLineNumber());
        } finally {
            reader.close();
        }
    }

    private void line() {
        buffer.append("**********************\n");
    }

    private void format(final String result, final int count) {
        buffer.append(String.format("* %-12s %5d *\n", result, count));
    }

    @SuppressWarnings({"unchecked"})
    private void map() {
        final StartElement start = event.asStartElement();
        final String name = start.getName().getLocalPart();
        final Iterator<Attribute> attributes = start.getAttributes();
        String text = null;
        if (attributes.hasNext()) {
            text = attributes.next().getValue();
        } else {
            try {
                final XMLEvent xmlEvent = nextEvent();
                text = xmlEvent.isCharacters() ? xmlEvent.asCharacters().getData() : null;
            } catch (Exception e) {
                handle(e);
            }
        }
        nextEvent();
        set(name, text);
    }

    private void set(final String field, final String value) {
        final Object target = context.peek();
        try {
            final Field f = target.getClass().getDeclaredField(field);
            f.setAccessible(true);
            f.set(target, value);
        } catch (Exception e) {
            print(String.format("%s : %s at %d:%d", target.getClass().getName(), e.getMessage(),
                getLineNumber(), getColumn()));
            e.printStackTrace();
            System.exit(1);
        }
    }

    private XMLEvent nextEvent() {
        try {
            do {
                event = reader.hasNext() ? reader.nextEvent() : null;
            } while (event != null && event instanceof Characters && ((Characters) event).isWhiteSpace());
            if (event != null) {
                print("next event = " + event.toString().trim());
            }
            return event;
        } catch (XMLStreamException e) {
            handle(e);
            return null;
        }
    }

    private void print(final Object s) {
        if (print) {
            System.out.println(s);
        }
    }

    private void handle(final Exception e) {
        throw new RuntimeException(String.format("(%d:%d): %s", getLineNumber(), getColumn(), e.getMessage()), e);
    }

    private int getLineNumber() {
        return event.getLocation().getLineNumber();
    }

    private int getColumn() {
        return event.getLocation().getColumnNumber();
    }

    void configuration() {
        handler.config = new Configuration();
        push(handler.config);
    }

    private void push(final Object o) {
        print("pushing " + o);
        context.push(o);
    }

    private Object pop() {
        Object top = context.pop();
        print("popping " + top);
//        ListIterator<Object> it = context.listIterator(context.size());
//        while(it.hasPrevious()) {
//            print("on stack: " + it.previous());
//        }
        return top;
    }

    void date() {
        try {
            handler.date = nextEvent().asCharacters().getData();
            print("reading date " + handler.date);
            push(handler.date);
        } catch (Exception e) {
            handle(e);
        }
    }

    void report() {
        push("=> reports");
    }

    void testsuites() {
        push("=> testsuites");
    }

    void testsuite() {
        push(new TestSuite());
    }

    void tests() {
        push("=> suite.tests");
    }

    void test() {
        push(new Test());
    }

    void testcases() {
        push("=> test.testcases");
    }

    void testcase() {
        push(new TestCase());
    }

    private Runnable getHandler(final XMLEvent event) {
        final String name = getEventName(event);
        try {
            final Method method = getClass().getDeclaredMethod(name);
            return new Runnable() {
                @Override
                public void run() {
                    try {
                        method.invoke(HtmlReportProducer.this);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e.getMessage(), e);
                    } catch (Exception e) {
                        throw new RuntimeException(e.getMessage(), e);
                    }
                }
            };
        } catch (NoSuchMethodException e) {
            return new Runnable() {
                @Override
                public void run() {
                    map();
                }
            };

        }
    }

    private String getEventName(final XMLEvent evt) {
        if (evt.isStartElement()) {
            final StartElement start = evt.asStartElement();
            return start.getName().getLocalPart();
        } else if (evt.isEndElement()) {
            final EndElement end = evt.asEndElement();
            return end.getName().getLocalPart();
        } else {
            return null;
        }
    }

    public String generateValidReport(final String notQuiteXml) {
        if (!notQuiteXml.endsWith("Valid.xml")) {
            String oFileName;
            try {
                final String resultFile = new File(notQuiteXml).getAbsolutePath();
                if (resultFile.lastIndexOf(".") > 0) {
                    oFileName = resultFile.substring(0, resultFile.lastIndexOf(".")) + "Valid.xml";
                } else {
                    oFileName = resultFile + "Valid.xml";
                }
                FileOutputStream fout = new FileOutputStream(oFileName);
                FileInputStream fin = new FileInputStream(notQuiteXml);
                try {
                    String machineName;
                    try {
                        InputStream in = Runtime.getRuntime().exec("uname -n").getInputStream();
                        byte[] bytes = new byte[200];
                        in.read(bytes);
                        machineName = new String(bytes).trim();
                    } catch (Exception me) {
                        machineName = "unavailable";
                    }
                    String extraXML = String.format("<report>"
                        + "<date>%s</date>"
                        + "<configuration>"
                        + "<os>%s %s</os>"
                        + "<jdkVersion>%s</jdkVersion>"
                        + "<machineName>%s</machineName>"
                        + "</configuration>"
                        + "<testsuites>",
                        new Date(), System.getProperty("os.name"), System.getProperty("os.version"),
                        System.getProperty("java.version"), machineName);
                    fout.write(extraXML.getBytes());
                    byte[] bytes = new byte[49152];
                    int read;
                    while ((read = fin.read(bytes)) != -1) {
                        fout.write(bytes, 0, read);
                    }
                    fout.write("</testsuites>\n</report>\n".getBytes());
                    fout.flush();
                } finally {
                    fout.close();
                    fin.close();
                }
            } catch (Exception e) {
                System.out.println("ERROR : " + e);
                throw new RuntimeException(e.getMessage(), e);
            }
            return oFileName;
        }
        return notQuiteXml;
    }

    public static void main(String[] args) throws XMLStreamException, IOException {
        if (args.length < 1) {
            System.err.println("Please specify the input file name");
            return;
        }
        final HtmlReportProducer producer = new HtmlReportProducer(args[0]);
        producer.produce();
    }
}
