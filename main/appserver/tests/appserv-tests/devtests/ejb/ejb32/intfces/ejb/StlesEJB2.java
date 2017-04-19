package ejb32.intrfaces;

import javax.ejb.Local;
import javax.ejb.Stateless;
/*
    StlesEJB2 exposes remote interface St7. St3 isn't business interface
 */

@Stateless
@Local
public class StlesEJB2 implements St3, St7 {
    @Override
    public String st3() throws Exception {
        return "StlesEJB2.st3";
    }

    @Override
    public String st7() throws Exception {
        return "StlesEJB2.st7";
    }
}
