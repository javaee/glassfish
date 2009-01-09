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
package testing;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;

import javax.xml.parsers.ParserConfigurationException;

import org.glassfish.synchronization.client.ClientConfig;
import org.glassfish.synchronization.client.SyncClient;
import org.glassfish.synchronization.util.ClientXMLConfigParser;
import org.glassfish.synchronization.util.StaticSyncInfo;
import org.xml.sax.SAXException;

/**
 * @author behrooz
 * 
 */
public class StartClients {
	private static int num_clients = 1;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
        String filepath = "C:\\Users\\Behrooz\\GlassFish\\client_config.xml";
		List<ClientConfig> configs = null;
		try {
			configs = 
				ClientXMLConfigParser.parseClientXML(new File(filepath));
			SyncClient client;
			for(ClientConfig conf : configs) {
				client = new SyncClient(conf);
				Thread th = new Thread(client);
				th.start();
			}
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			System.exit(-1);
		} catch (SAXException e) {
			e.printStackTrace();
			System.exit(-1);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
//		String dasAddr = null;
//		String configPath = null;
//		if (args.length > 0) {
//			int clients;
//			try {
//				clients = Integer.parseInt(args[0]);
//				if (clients > 0) {
//					num_clients = clients;
//				}
//			} catch (Exception e) {
//			}
//		}
//		if (args.length > 1) {
//			dasAddr = args[1];
//		}
//		if (args.length > 2)
//			configPath = args[2];
//		// Random rng = new Random(1);
//		SyncClient client;
//		File config;
//		if (configPath != null)
//			config = new File(configPath);
//		else
//			config = new File("sync_config.txt");
//		for (int i = 0; i < num_clients; i++) {
//			if (dasAddr != null)
//				client = new SyncClient(i, dasAddr, config);
//			else
//				client = new SyncClient(i, config);
//			Thread th = new Thread(client);
//			th.start();
//		}
	}
}
