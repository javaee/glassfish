package test;

import javax.ejb.EJB;

import test.ejb.stateful.SFHello;

public class Client {
    private static @EJB SFHello sful1; 
    private static @EJB(name="ejb/sfhello") SFHello sful2; 

    public void main(String[] args) {
    }
}
