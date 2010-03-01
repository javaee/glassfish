/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author bnevins
 */
import java.io.*;
import java.util.*;

/**
 *
 * @author bnevins
 */
public class OfflineConfig {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            new OfflineConfig();
        }
        catch (Exception e) {
            System.out.println(e);
        }
    }

    OfflineConfig() {
        if(console == null)
            throw new RuntimeException("You must have a console attached!");

        System.out.println("****  Welcome to Offline Configuration ***\n");
        clusterName = console.readLine("Enter the cluster name: ");
        System.out.print("\n\n");

        getHosts();

        if(hosts.size() < 1)
            throw new RuntimeException("You must define at least one node!");

        dump();
    }

    private void getHosts() {
        System.out.println("Enter as many Hosts as you need.  For each host give the name and" +
                " the desired number of instances like so:\nhostname ##\n" +
                "Hit enter on a blank line to generate the script\n\n");

        while(true) {
            String s = "Hostname ## ?";
            String hostInfo = console.readLine(s);

            // did they enter something?
            if(hostInfo == null || hostInfo.length() <= 0)
                return;

            // parse
            String[] items = hostInfo.split(" ");

            if(items == null || items.length != 2 || !ok(items[0]) || !ok(items[1])) {
                // no big deal
                System.out.println("Ignoring badly formatted command. Example: host1 6");
                continue;
            }
            Host host = new Host();
            host.hostname = items[0];
            host.numInstances = Integer.parseInt(items[1]);
            hosts.add(host);
        }
    }

    private void dump() {
        System.out.printf("%s create-cluster %s %s\n", "asadmin", LONG_ARGS, clusterName);

        for(Host host : hosts) {
            // create NA config
            System.out.printf("%s create-node-agent-config %s %s\n", "asadmin", LONG_ARGS, host.hostname);

            for(int i = 1; i <= host.numInstances; i++) {
                System.out.printf("%s %s --cluster %s --nodeagent %s  server-%s-%d\n",
                    "asadmin",
                    "create-instance",
                    clusterName,
                    host.hostname,
                    host.hostname,
                    i);
            }
        }
    }

    private static boolean ok(String s) { return s != null && s.length() > 0; }


    private List<Host> hosts = new LinkedList<Host>();
    private int numNodes;
    private static final Console console = System.console();
    private static final String LONG_ARGS = " --user admin --passwordfile pass --port 4848 --host localhost --secure=false ";
    private String clusterName ;

    static class Host {
        String  hostname;
        int     numInstances;
    }
}

/***  REFERENCE AREA
 *
create-cluster           --user admin --passwordfile pass --port 4848 --host localhost --secure=false cluster1
create-node-agent-config --user admin --passwordfile pass --port 4848 --host localhost --secure=false q
create-node-agent-config --user admin --passwordfile pass --port 4848 --host localhost --secure=false q
create-node-agent-config --user admin --passwordfile pass --port 4848 --host localhost --secure=false q
create-node-agent-config --user admin --passwordfile pass --port 4848 --host localhost --secure=false q
create-instance --nodeagent q --cluster cluster1 server-q-1
create-instance --nodeagent q --cluster cluster1 server-q-2
create-instance --nodeagent q --cluster cluster1 server-q-1
create-instance --nodeagent q --cluster cluster1 server-q-2
create-instance --nodeagent q --cluster cluster1 server-q-3
create-instance --nodeagent q --cluster cluster1 server-q-1
create-instance --nodeagent q --cluster cluster1 server-q-2
create-instance --nodeagent q --cluster cluster1 server-q-3

 */


