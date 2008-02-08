package com.sun.enterprise.v3.server;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.util.StreamReaderDelegate;

/**
 * {@link XMLStreamReader} wrapper that cuts off sub-trees.
 * @author Kohsuke Kawaguchi
 */
abstract class XMLStreamReaderFilter extends StreamReaderDelegate {
    XMLStreamReaderFilter(XMLStreamReader reader) {
        super(reader);
    }

    XMLStreamReaderFilter() {
    }

    public int next() throws XMLStreamException {
        while(true) {
            int r = super.next();
            if(r!=START_ELEMENT || !filterOut())
                return r;
            skipTree();
        }
    }

    public int nextTag() throws XMLStreamException {
        while(true) {
            int r = super.nextTag();
            if(r!=START_ELEMENT || !filterOut())
                return r;
            skipTree();
        }
    }

    /**
     * Skips a whole subtree, and return with the cursor pointing to the end element
     * of the skipped subtree.
     */
    private void skipTree() throws XMLStreamException {
        int depth=1;
        while(depth>0) {
            int r = super.nextTag();
            if(r==START_ELEMENT)    depth++;
            else                    depth--;
        }
    }

    /**
     * Called when the parser is at the start element state, to decide if we are to skip the current element
     * or not.
     */
    abstract boolean filterOut() throws XMLStreamException;
}
