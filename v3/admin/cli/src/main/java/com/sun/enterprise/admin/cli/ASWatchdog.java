/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.enterprise.admin.cli;

/**
 * This class has one and only one purpose.  To make it clearer from jps output
 * what the process is doing.
 * I.e. instead of AsadminMain they see ASWatchdog
 * @author bnevins
 */
public class ASWatchdog {
    public static void main(String[] args) {
        AsadminMain.main(args);
    }
}
