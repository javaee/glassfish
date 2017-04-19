package ejb32.intrfaces;
/*
    StflEJB1 exposes local interfaces St4 & St3
 */

import javax.ejb.Stateful;

@Stateful
public class StflEJB1 implements St3, St4 {
    @Override
    public String st4() {
        return "StflEJB1.st4";
    }

    @Override
    public String st3() throws Exception {
        return "StflEJB1.st3";
    }
}
