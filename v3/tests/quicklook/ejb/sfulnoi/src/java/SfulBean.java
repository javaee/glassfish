package sfulnoi;

import javax.ejb.Stateful;

@Stateful
public class SfulBean {
    private String name = "foo";

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
