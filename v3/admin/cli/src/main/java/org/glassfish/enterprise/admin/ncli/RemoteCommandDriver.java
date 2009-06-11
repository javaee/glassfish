package org.glassfish.enterprise.admin.ncli;

import org.glassfish.enterprise.admin.ncli.metadata.CommandDesc;
import org.glassfish.enterprise.admin.ncli.comm.TargetServer;

import java.util.Arrays;


/**  The main class that is designed to work on arguments that are presented on a <i> command line </i>.
 *   It is designed for remote commands as of now.
 * @author &#2325;&#2375;&#2342;&#2366;&#2352 (km@dev.java.net)
 * @since  GlassFish v3 (September 2009)
 */
public class RemoteCommandDriver {
    private final String[] args;
    private final Parser parser;
    private final CommandMetadataCache cache;
    
    public RemoteCommandDriver(String[] args) {
        this.args = args;
        parser = new Parser(args);
        cache  = new CommandMetadataCache();
    }

    public static void main(String... args) throws ParserException, GenericCommandException {
        new RemoteCommandDriver(args).run();
    }

    // ALL Private
    
    private void run() throws ParserException, GenericCommandException {
        FirstPassResult fpr = parser.firstPass();
        String cmdName      = fpr.getCommandName();
        TargetServer ts     = fpr.getTargetServer();
        debug(fpr);
        CommandDesc desc    = cache.get(cmdName, ts);

        if (desc == null)
            desc = getCommandMetadata(cmdName, fpr.getTargetServer()); //goes to server

        SecondPassResult spr = parser.secondPass(desc, fpr.getCommandArguments());
        NewCommand command = spr.getFinalCommand();
        //at this point, there are no syntax errors, server is running and command is fully formed
        //now, only command execution errors can occur
        assert command != null : "Command is null!";
        CommandExecutionResult er = command.execute(ts);
        cache.put(cmdName, ts, desc);
    }

    private static void debug(FirstPassResult fpr) {
        //TODO remove this method
        out("First Pass is Successful");
        out("****Command uses NEW Syntax? " + !fpr.usesDeprecatedSyntax());
        out("Explicit/Default asadmin program options:");
        for (Option op : fpr.getProgramOptions()) {
            out("name: " + op.getName());
            out("effective value: " + op.getEffectiveValue());
        }
        out("CmdName: " + fpr.getCommandName());
        out("Command Options/Operands:" + Arrays.toString(fpr.getCommandArguments()));

    }

    private static void out(String s) {
        System.out.println(s);
    }

    private CommandDesc getCommandMetadata(String cmdName, TargetServer from) {
        //TODO
        return null;
    }
}