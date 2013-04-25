package ejb32.intrfaces;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
/*
    StlesEJB1 exposes no-interface view, local interface St6, remote interface St7.
    St5 isn't business interface
 */

@Stateless
@LocalBean
public class StlesEJB1 implements St5, St6, St7{
    @Override
    public String st5() throws Exception {
        return "StlesEJB1.st5";
    }

    @Override
    public String st6() throws Exception {
        return "StlesEJB1.st6";
    }

    @Override
    public String st7() {
        return "StlesEJB1.st7";
    }
}
