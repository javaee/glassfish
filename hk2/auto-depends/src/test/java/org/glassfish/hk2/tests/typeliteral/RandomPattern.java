package org.glassfish.hk2.tests.typeliteral;

/**
 * Created by IntelliJ IDEA.
 * User: dochez
 * Date: 6/27/11
 * Time: 9:02 PM
 * To change this template use File | Settings | File Templates.
 */
public class RandomPattern implements Pattern {
    @Override
    public String patternTest() {
        return "random";
    }
}
