/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.enterprise.admin.common;

import java.util.Random;

import java.net.ServerSocket;
import java.io.IOException;
import com.sun.enterprise.admin.common.exception.PortInUseException;
import com.sun.enterprise.admin.util.Debug;

// i18n import 
import com.sun.enterprise.admin.util.SOMLocalStringsManager;

/**
A utility class to check the port availability etc.
*/


public class NetUtil
{
	public static final int			kMaxPortNo		= 65535;
	
    /**
		Static method to check whether the port provided is available to connect to.
		Uses ServerSocket for this check.
		@throws PortInUseException
    */
	
    public static void checkPortAvailability(int port) throws PortInUseException
    {
        ServerSocket trialSocket = null;
        try
        {
            trialSocket = new ServerSocket(port);
        }
        catch(IOException ioe)
        {
			String msg = localizedStrMgr.getString( "admin.common.port_in_use", new String( port + "" ) );
            throw new PortInUseException( msg );
        }
        finally
        {
            try
            {
				if (trialSocket != null)
				{
					trialSocket.close();
				}
            }
            catch(Exception e)
            {
                Debug.printStackTrace(e);
            }
        }
    }
	
	/**
		Returns whether the given port number is available at the time of
		call.
	 
		@param portNo integer specifying the port.
	*/
	
	public static boolean isPortAvalable(int portNo)
	{
		boolean available = true;
		
		try
		{
			checkPortAvailability(portNo);
		}
		catch(Exception e)
		{
			available = false;
		}
		return ( available );
	}

    private static Random random = new Random();

	// i18n SOMLocalStringsManager
	private static SOMLocalStringsManager localizedStrMgr =
		SOMLocalStringsManager.getManager( NetUtil.class );

    public static int getFreePort()
    {
        synchronized(NetUtil.class)
        {
            /* scared of using infinite while loop */
            for (int i = 0; i < 1024; i++) 
            {
                int nextInt = random.nextInt(kMaxPortNo);
                if (nextInt <= 1024)
                {
                    continue;
                }
                else if (isPortAvalable(nextInt))
                {
                    return nextInt;
                }
            }
        }
        return 0;
    }
}
