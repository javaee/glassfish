package ejb32.intrfaces;


import javax.ejb.Local;
import javax.ejb.Stateful;

/*
    St7 can not be both local and remote
 */
@Stateful
@Local({St1.class, St7.class})
public class NegEJB2 implements St7 {

    public String st1() throws Exception {
        return "NegEJB2.st1";
    }

    public String st7() throws Exception {
        return "NegEJB2.st7";
    }
}
