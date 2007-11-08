package testmbeans;

/*
import javax.management.*;
import java.io.*;
import javax.swing.JFrame;
import java.awt.BorderLayout;
import java.awt.Label;
import java.net.URL;
*/

public class BadCtor implements BadCtorMBean
{
    public BadCtor()
    {
        int x = 0;
        int y = 5 / x;
        //throw new NullPointerException("NPE Here!!");
    }
}
