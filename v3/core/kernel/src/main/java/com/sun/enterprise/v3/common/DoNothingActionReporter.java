/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.enterprise.v3.common;

import java.io.IOException;
import java.io.OutputStream;

/**
 * PlainTextActionReporter is being used as a fake ActionReporter when one is
 * required.  It is confusing since PTAR does special things.
 * THis one does exactly what it advertises doing in its name!
 * @author Byron Nevins
 */
public class DoNothingActionReporter extends ActionReporter{

    @Override
    public void writeReport(OutputStream os) throws IOException {
    }
}
