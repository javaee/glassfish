package com.elf.asmworkshop;

import java.util.logging.*;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        System.out.println( "Hello World!" );


        try {
            PrimeProviderDump.dump();
        } catch (Exception ex) {
            Logger.getLogger("foo").log(Level.SEVERE, null, ex);
        }
    }
}
