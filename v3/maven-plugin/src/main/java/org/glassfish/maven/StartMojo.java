//========================================================================
//$Id: JettyStopMojo.java 4005 2008-11-06 22:31:53Z janb $
//Copyright 2000-2004 Mort Bay Consulting Pty. Ltd.
//------------------------------------------------------------------------
//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at 
//http://www.apache.org/licenses/LICENSE-2.0
//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.
//========================================================================

package org.glassfish.maven;

import java.io.OutputStream;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import org.glassfish.api.embedded.*;
/**
 * 
 * @author David Yu
 * 
 * @goal stop
 * @description Stops jetty that is configured with &lt;stopKey&gt; and &lt;stopPort&gt;.
 */

public class StartMojo extends AbstractMojo
{        
    protected String user = "admin", passwordFile = "";
    protected int port = 8080;

    public void execute() throws MojoExecutionException, MojoFailureException 
    {
        Server server = new Server.Builder("First").build();
        server.createPort(port);
        server.start();
    }

}
