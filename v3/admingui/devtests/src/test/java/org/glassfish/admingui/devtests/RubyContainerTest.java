package org.glassfish.admingui.devtests;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by IntelliJ IDEA.
 * User: jasonlee
 * Date: Mar 11, 2010
 * Time: 2:46:08 PM
 * To change this template use File | Settings | File Templates.
 */
public class RubyContainerTest extends BaseSeleniumTestClass {
    private static final String TRIGGER_RUBY_CONTAINER = "Container to host Ruby web applications such as Ruby on Rails, Merb, Sinatra or any Rack based Ruby application.";

    @Test
    public void testRubyContainer() {
        final String initialPoolSize = Integer.toString(generateRandomNumber(10));
        final String minPoolSize = Integer.toString(generateRandomNumber(10));
        final String maxPoolSize = Integer.toString(generateRandomNumber(10));

        clickAndWait("treeForm:tree:configuration:jruby:jruby_link", TRIGGER_RUBY_CONTAINER);
        selenium.type("form1:propertySheet:propertySectionTextField:jruntime:jruntime", initialPoolSize);
        selenium.type("form1:propertySheet:propertySectionTextField:jruntime-mim:jruntime-mim", minPoolSize);
        selenium.type("form1:propertySheet:propertySectionTextField:jruntime-max:jruntime-max", maxPoolSize);
        String button = "form1:propertyContentPage:topButtons:newButton";
        if (!selenium.isElementPresent(button)) {
            button = "form1:propertyContentPage:topButtons:saveButton";
        }
        clickAndWait(button, MSG_NEW_VALUES_SAVED);
        clickAndWait("treeForm:tree:ct", "Please Register");

        clickAndWait("treeForm:tree:configuration:jruby:jruby_link", TRIGGER_RUBY_CONTAINER);
        assertEquals(initialPoolSize, selenium.getValue("form1:propertySheet:propertySectionTextField:jruntime:jruntime"));
        assertEquals(minPoolSize, selenium.getValue("form1:propertySheet:propertySectionTextField:jruntime-mim:jruntime-mim"));
        assertEquals(maxPoolSize, selenium.getValue("form1:propertySheet:propertySectionTextField:jruntime-max:jruntime-max"));
    }
}
