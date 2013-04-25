package ejb32.intrfaces;

import javax.ejb.Local;
import javax.ejb.Stateful;
/*
    StflEJB2 exposes local interfaces St4 and St6. St5 isn't business interface
 */

@Stateful
@Local({St4.class})
public class StflEJB2 implements St4, St5, St6 {

    @Override
    public String st4() {
        return "StflEJB2.st4";
    }

    @Override
    public String st5() throws Exception {
        return "StflEJB2.st5";
    }

    @Override
    public String st6() throws Exception {
        return "StflEJB2.st6";
    }
}
