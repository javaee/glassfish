/**
 * 
 */
package org.glassfish.synchronization.client;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * @author Behrooz Khorashadi
 *
 */
public class ClientConfig {
	public final int id;
	public final String das_url;
	public final int sync_threads;
	public final String logLevel;
	public final boolean verify;
	public final String base_dir;
	public ClientConfig(Element config) {
		NodeList nodelist;
		Element e;
		nodelist = config.getElementsByTagName("id");
		e = (Element)nodelist.item(0);
		id = new Integer(e.getTextContent());
		nodelist = config.getElementsByTagName("syncthreads");
		e = (Element)nodelist.item(0);
		sync_threads = new Integer(e.getTextContent());
		nodelist = config.getElementsByTagName("connectionurl");
		e = (Element)nodelist.item(0);
		das_url = e.getTextContent();
		nodelist = config.getElementsByTagName("loglevel");
		e = (Element)nodelist.item(0);
		logLevel = e.getTextContent();
		nodelist = config.getElementsByTagName("verify");
		e = (Element)nodelist.item(0);
		verify = new Boolean(e.getTextContent());
		nodelist = config.getElementsByTagName("base_dir");
		e = (Element)nodelist.item(0);
		base_dir = e.getTextContent();
	}
}
