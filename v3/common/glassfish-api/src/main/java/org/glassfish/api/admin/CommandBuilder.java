package org.glassfish.api.admin;

import org.glassfish.api.Param;
import org.glassfish.api.ActionReport;
import com.sun.hk2.component.InjectionResolver;

import java.util.Properties;
import java.util.logging.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: dochez
 * Date: Apr 20, 2009
 * Time: 4:23:04 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class CommandBuilder {

    final public String commandName;
    final private CommandRunner owner;

    CommandBuilder(String name, CommandRunner owner) {
        this.commandName = name;
        this.owner = owner;
    }

    public Properties  paramsAsProperties;
    public Object      delegate;
    public Payload.Inbound inbound;
    public Payload.Outbound outbound;

    public abstract CommandBuilder setResolver(InjectionResolver<Param> resolver);

    public CommandBuilder setParameters(Properties props) {
        paramsAsProperties = props;
        return this;
    }

    public CommandBuilder setParameters(Object delegate) {
        this.delegate = delegate;
        return this;
    }

    public CommandBuilder setInbound(Payload.Inbound inbound) {
        this.inbound = inbound;
        return this;
    }

    public CommandBuilder setOutbound(Payload.Outbound outbound) {
        this.outbound = outbound;
        return this;
    }

    public void execute(ActionReport report, Logger logger) {
        owner.doCommand(this, report, logger);
    }


}
