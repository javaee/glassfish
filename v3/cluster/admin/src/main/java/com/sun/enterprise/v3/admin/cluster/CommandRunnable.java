/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.enterprise.v3.admin.cluster;

import java.util.concurrent.BlockingQueue;

import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.CommandRunner.CommandInvocation;

/**
 * This class wraps a CommandInvocation so that it can be run via a
 * thread pool. On construction you pass it the CommandInvocation
 * to execute as well as a response queue and the ActionReport
 * that was set on the CommandInvocation. When the run() method
 * is called the CommandInvocation is executed (which sets its results
 * in the ActionReport) and then it adds itself to the response queue
 * where it can be picked up and the ActionReport inspected for the results.
 *
 * @author dipol
 */
public class CommandRunnable implements Runnable {

    BlockingQueue<CommandRunnable> responseQueue = null;
    String name = "";
    CommandInvocation ci = null;
    ActionReport report = null;

    private CommandRunnable() {
    }

    /**
     * Construct a CommandRunnable. This class wraps a CommandInvocation
     * so that it can be executed via a thread pool.
     *
     * @param ci        A CommandInvocation containing the command you want
     *                  to run.
     * @param report    The ActionReport you used with the CommandInvocation
     * @param q         A blocking queue that this class will add itself to
     *                  when its run method has completed.
     *
     * After dispatching this class to a thread pool the caller can block
     * on the response queue where it will dequeue CommandRunnables and then
     * use the getActionReport() method to retrieve the results.
     */
    public CommandRunnable(CommandInvocation ci, ActionReport report,
            BlockingQueue<CommandRunnable> q) {
        this.responseQueue = q;
        this.report = report;
        this.ci = ci;
    }

    @Override
    public void run() {
        ci.execute();
        if (responseQueue != null) {
            responseQueue.add(this);
        }
    }

    /**
     * Set a name on the runnable. The name is not interpreted to mean
     * anything so the caller can use it for whatever it likes.
     *
     * @param s The name
     */
    public void setName(String s) {
        this.name = s;
    }

    /**
     * Get the name that was previously set.
     *
     * @return  A name that was previously set or null if no name was set.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the CommandInvocation that was passed on the constructor
     *
     * @return  The CommandInvocation that was passed on the constructor
     */
    public CommandInvocation getCommandInvocation() {
        return ci;
    }

    /**
     * Returns the ActionReport that was passed on the constructor.
     *
     * @return  the ActionReport that was passed on the constructor.
     */
    public ActionReport getActionReport() {
        return report;
    }

    @Override
    public String toString() {
        if (name == null) {
            return "null";
        } else {
            return name;
        }
    }
}