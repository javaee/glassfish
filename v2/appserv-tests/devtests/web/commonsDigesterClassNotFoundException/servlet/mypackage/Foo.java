package mypackage;

import java.util.Iterator;

public class Foo {

    private String name;
    private Bar bar;

    public void addBar(Bar bar) {
        // no-op
    }

    public Bar findBar(int id) {
        return null;
    }

    public Iterator getBars() {
        return null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
