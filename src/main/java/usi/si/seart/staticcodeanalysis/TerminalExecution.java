package usi.si.seart.staticcodeanalysis;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import usi.si.seart.exception.TerminalExecutionException;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

/**
 * A useful wrapper for managing the lifecycle of terminal processes.
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TerminalExecution {

    Process process;

    ProcessBuilder processBuilder;


    /**
     * Prepares a new terminal command.
     * @param cwd The Current Working Directory in which the command will be invoked.
     * @param args the arguments of the command. Will get joined together (eg {"ls", "-la"} )
     */
    public TerminalExecution(@Nullable Path cwd, @NotNull String... args) {
        processBuilder = new ProcessBuilder();

        if (cwd != null)
            processBuilder.directory(cwd.toFile());

        switch (System.getProperty("os.name")) {
            case "windows":
                processBuilder.command("cmd.exe", "/c", String.join(" ", args));
                break;
            default:
                processBuilder.command("bash", "-c", String.join(" ", args));
                break;
        }
    }


    /**
     * Invokes the command in the shell.
     *  If a process was already invoked and is still running, it first stops it.
     * @throws TerminalExecutionException Thrown if an error occurred when invoking the command.
     */
    public TerminalExecution start() throws TerminalExecutionException {
        if (isRunning())
            stop();
        try {
            process = processBuilder.start();
        } catch (IOException e) {
            throw new TerminalExecutionException("Could not start terminal process",e);
        }
        return this;
    }

    /**
     * Terminates the process.
     *  If the process is not running, does nothing.
     */
    public TerminalExecution stop() {
        if(process == null)
            return this;
        process.destroy();
        return this;
    }

    /**
     * Returns a BufferedReader for reading from the process' normal output.
     * @return the BufferedReader for reading the normal output piped from the process.
     */
    public BufferedReader getStdout() {
        return new BufferedReader(new InputStreamReader(process.getInputStream()));
    }

    public boolean isRunning() {
        return process != null && process.isAlive();
    }

    /**
     * Waits for the process to end.
     *  Method returns without error regardless of the process terminating abnormally, or if the waiting thread is interrupted.
     */
    public TerminalExecution waitEnd() {
        if (process == null)
            return this;

        try {
            process.onExit().get();
        } catch (ExecutionException | CancellationException e) {

        } finally {
            return this;
        }
    }

    /**
     * Waits for the successful execution of the process.
     * @throws RuntimeException If the command exits with an error or throws any kind of exception.
     */
    public TerminalExecution waitSuccessfulExit() throws TerminalExecutionException {
        if (process == null)
            return this;

        int exitCode = 0;
        try {
            exitCode = process.onExit().get().exitValue();
            if (exitCode != 0) {
                throw new Exception("Terminal process returned error code "+exitCode);
            }
        } catch (Exception e) {
            throw new TerminalExecutionException("Error occurred while waiting on the successful exit of the terminal process", e);
        }

        return this;
    }


    /**
     * Adds a shell environment variable.
     *  Overwrites value if key already in keyset.
     * @param key the environment var identifier.
     * @param value the value.
     */
    public TerminalExecution setEnv(String key, String value) {
        processBuilder.environment().put(key, value);

        return this;
    }

    /**
     * Clears all previously-set shell environment variables and adds all the ones passed in input.
     * @param env the new set of shell environment variables.
     */
    public TerminalExecution setEnv(Map<String,String> env) {
        processBuilder.environment().clear();
        processBuilder.environment().putAll(env);
        return this;
    }
}
