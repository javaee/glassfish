package org.jvnet.hk2.config.generator;

/**
 * @author Kohsuke Kawaguchi
 */
final class StringUtil {
    static String join(Iterable<String> items, String separator) {
        StringBuilder buf = new StringBuilder();
        for (String item : items) {
            if(buf.length()>0)  buf.append(separator);
            buf.append(item);
        }
        return buf.toString();
    }
}
