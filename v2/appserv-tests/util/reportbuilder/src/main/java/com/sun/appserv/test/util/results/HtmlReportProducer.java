package com.sun.appserv.test.util.results;
/*
* Copyright 2004-2009 Sun Microsystems, Inc.  All rights reserved.
* Use is subject to license terms.
*/

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.FileInputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.Date;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

@SuppressWarnings({"IOResourceOpenedButNotSafelyClosed"})
public class HtmlReportProducer {
    private XMLEventReader reader;
    private XMLEvent event;
    private boolean print = false;
    private Stack<Object> context = new Stack<Object>();
    private StringBuilder buffer = new StringBuilder();
    private File input;
    private ReportHandler handler;

    public HtmlReportProducer(String inputFile) throws FileNotFoundException {
        input = new File(generateValidReport(inputFile));
        // Use the default (non-validating) parser
        handler = new ReportHandler(new File(input.getParentFile(), "test_results.html"));
    }

    private void produce() throws IOException, XMLStreamException {
        reader = XMLInputFactory.newInstance().createXMLEventReader(new FileReader(input));
        try {
            nextEvent();
            //noinspection LoopConditionNotUpdatedInsideLoop
            while (reader.hasNext()) {
                if (event.isStartElement()) {
                    final Runnable runnable = getHandler(event);
                    if (runnable != null) {
                        runnable.run();
                    } else {
                        map();
                    }
                } else if (event.isEndElement()) {
                    print("end event = " + event);
                    final Runnable runnable = getHandler(event);
                    if (runnable != null && !context.isEmpty()) {
                        handler.process(pop());
                    }
                }
                nextEvent();
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
            if (handler.fail != 0) {
                System.err.println("All Tests NOT passed, so returning FAILED status.");
                System.exit(1);
            }
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

    private void map() {
        final StartElement start = event.asStartElement();
        final String name = start.getName().getLocalPart();
        final Iterator<Attribute> attributes = start.getAttributes();
        String text = null;
        if (attributes.hasNext()) {
            text = attributes.next().getValue();
            nextEvent();
        } else {
            try {
                final XMLEvent xmlEvent = nextEvent();
                text = xmlEvent.isCharacters() ? xmlEvent.asCharacters().getData() : null;
            } catch (Exception e) {
                handle(e);
            }
        }
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
            event = reader.nextEvent();
            print("next event = " + event);
            return event;
        } catch (XMLStreamException e) {
            handle(e);
            return null;
        }
    }

    private void print(final String s) {
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
        print("popping " + context.peek());
        return context.pop();
    }

    void date() {
        try {
            handler.date = nextEvent().asCharacters().getData();
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
        print("=> suite.tests");
        push(((TestSuite) context.peek()).getTests());
    }

    void test() {
        Test test = new Test();
        ((List) context.peek()).add(test);
        push(test);
    }

    void testcases() {
        print("=> test.testcases");
        push(((Test) context.peek()).getTestCases());
    }

    void testcase() {
        TestCase test = new TestCase();
        ((List) context.peek()).add(test);
        push(test);
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
            print(String.format("No handler for %s at %d:%d", name, getLineNumber(), getColumn()));
        }
        return null;
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
                    new Date(), System.getProperty("os.name"),System.getProperty("os.version"),
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

    public static void main(String[] args) throws XMLStreamException, IOException {
        if (args.length < 1) {
            System.err.println("Please specify the input file name");
            return;
        }
        final HtmlReportProducer producer = new HtmlReportProducer(args[0]);
        producer.produce();
    }
}