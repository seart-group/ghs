package usi.si.seart.analysis;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import usi.si.seart.exception.TerminalExecutionException;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.Map;
import java.util.stream.Collectors;

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

        processBuilder.command("bash", "-c", String.join(" ", args));
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
            throw new TerminalExecutionException("Could not start terminal process", e);
        }
        return this;
    }

    /**
     * Terminates the process.
     *  If the process is not running, does nothing.
     */
    public TerminalExecution stop() {
        if (process == null)
            return this;
        process.destroy();
        return this;
    }

    /**
     * Returns a BufferedReader for reading from the process' normal output.
     * @return the BufferedReader for reading the normal output piped from the process.
     */
    public BufferedReader getStdOut() {
        return new BufferedReader(new InputStreamReader(process.getInputStream()));
    }

    public BufferedReader getStdErr() {
        return new BufferedReader(new InputStreamReader(process.getErrorStream()));
    }

    public boolean isRunning() {
        return process != null && process.isAlive();
    }

    /**
     * Waits for the successful execution of the process.
     * @throws RuntimeException If the command exits with an error or throws any kind of exception.
     */
    public TerminalExecution waitSuccessfulExit() throws TerminalExecutionException {
        if (process == null)
            return this;

        int exitCode = 0;
        BufferedReader stderr;
        try {
            stderr = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            exitCode = process.onExit().get().exitValue();
            if (exitCode != 0) {
                final String stringErr = stderr.lines().collect(Collectors.joining("\n"));
                throw new Exception(
                        "Terminal process returned error code " + exitCode + "\n== stderr:\n" + stringErr + "\n==="
                );
            }
        } catch (Exception e) {
            throw new TerminalExecutionException(
                    "Error occurred while waiting on the successful exit of the terminal process", e
            );
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
    public TerminalExecution setEnv(Map<String, String> env) {
        processBuilder.environment().clear();
        processBuilder.environment().putAll(env);
        return this;
    }
}
