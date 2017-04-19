import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.ExecTask;

/**
 * Extends the Apache Exec task to execute a given command in a separate thread and return control.
 * Supports the same attributes and elements as the exec task. This task is useful for running
 * server programs where you wish the server to be spawned off in a separate thread and execution
 * to proceed without blocking for the server.
 *
 * @author nandkumar.kesavan@sun.com
 * @see <a href="http://ant.apache.org/manual/CoreTasks/exec.html">Exec</a>
 */
public class SpawnTask extends ExecTask implements Runnable{

    /**
     * Run the command in a new thread
     */
    public void execute() throws BuildException {

        //Instantiate a new thread and run the command in this thread.
        Thread taskRunner = new Thread(this);
        taskRunner.start();

    }

    public void run() {

        //Run the parent ExecTask in a separate thread
        super.execute();

    }

}




