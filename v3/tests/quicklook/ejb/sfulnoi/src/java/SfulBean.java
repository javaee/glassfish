package sfulnoi;

import javax.ejb.Stateful;

@Stateful
public class SfulBean {

    private String myId = "unknown";

    public void setId(String id) {
        myId = id;
    }

    public String getId() {
        return myId;
    }

}
