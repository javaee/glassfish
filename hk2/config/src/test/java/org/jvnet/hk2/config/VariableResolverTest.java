package org.jvnet.hk2.config;

import junit.framework.TestCase;

/**
 * @author Kohsuke Kawaguchi
 */
public class VariableResolverTest extends TestCase {
    public void test1() throws TranslationException {
        VariableResolver v = new VariableResolver() {
            protected String getVariableValue(String varName) {
                if(varName.equals(varName.toUpperCase()))
                    return varName.toLowerCase();
                return null;
            }
        };

        assertEquals("foobarzot",v.translate("${FOO}bar${ZOT}"));
        assertEquals("$",v.translate("$"));
        assertEquals("$",v.translate("$$"));
        assertEquals("$$",v.translate("$$$$"));
        assertEquals("${ESCAPE}",v.translate("$${ESCAPE}"));
        assertEquals("xyz",v.translate("${X}${Y}${Z}"));
    }
}
