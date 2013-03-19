package test.beans;

/**
 * @author <a href="mailto:phil.zampino@oracle.com">Phil Zampino</a>
 */
public class FooImpl implements Foo {

    @Override
    public String value() {
        return getClass().getName();
    }

}
