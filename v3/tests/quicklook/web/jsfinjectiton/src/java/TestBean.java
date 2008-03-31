package jsfinjection;
import javax.annotation.*;
import javax.sql.DataSource;

public class TestBean {

    @Resource(name="entry")
    private String entry;

    @Resource(name="jdbc/__default")
    private DataSource ds;

    public String getEntry() {
        return entry;
    }

    public int getNumber() {
        int tout = -3000;
        if (ds != null) {
            try {
                tout = ds.getLoginTimeout();
            } catch(Exception ex) {
                ex.printStackTrace();
                tout = -1000;
            }
        }
        return tout;
    }

}
