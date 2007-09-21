package com.sun.hk2.component;

import org.jvnet.hk2.component.ComponentException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;

/**
 * Scans the stream that follows the <tt>/META-INF/inhabitants/*</tt> format.
 *
 * <p>
 * This class implements {@link Iterable} so that it can be used in for-each loop,
 * but it cannot parse the same stream twice.
 *
 * @author Kohsuke Kawaguchi
 */
public class InhabitantsScanner implements Iterable<KeyValuePairParser> {
    private int lineNumber = 0;
    private final String systemId;
    private final BufferedReader r;
    private final KeyValuePairParser kvpp = new KeyValuePairParser();

    public InhabitantsScanner(InputStream in, String systemId) throws IOException {
        r = new BufferedReader(new InputStreamReader(in,"UTF-8"));
        this.systemId = systemId;
    }

    public String getSystemId() {
        return systemId;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public Iterator<KeyValuePairParser> iterator() {
        return new Iterator<KeyValuePairParser>() {
            private String nextLine;

            public boolean hasNext() {
                fetch();
                return nextLine!=null;
            }

            private void fetch() {
                if(nextLine!=null)  return;

                try {
                    while((nextLine=r.readLine())!=null) {
                        lineNumber++;
                        if(nextLine.startsWith("#"))
                            continue;   // comment

                        return; // found the next line
                    }
                } catch (IOException e) {
                    throw new ComponentException("Failed to parse line " + lineNumber + " of " + systemId,e);
                }
            }

            public KeyValuePairParser next() {
                fetch();
                kvpp.set(nextLine);
                nextLine=null;
                return kvpp;
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }
}
