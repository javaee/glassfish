package ejb32.intrfaces;

import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;

/*
    St1 can not be both local and remote
 */
@Stateless
@Local({St1.class, St2.class})
@Remote({St1.class, St3.class})
public class NegEJB1 {
    public String st1() throws Exception {
        return "NegEJB1.st1";
    }

    public String st2() throws Exception {
        return "NegEJB1.st2";
    }

    public String st3() throws Exception {
        return "NegEJB1.st3";
    }
}
