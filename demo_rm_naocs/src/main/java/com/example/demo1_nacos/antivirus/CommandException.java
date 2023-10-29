/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 * <p>
 * https://github.com/keeps/roda
 */
package com.example.demo1_nacos.antivirus;

/**
 * Thrown to indicate that a system command finished with an error code.
 *
 * @author Rui Castro
 */
public class CommandException extends Throwable {

    private static final long serialVersionUID = 1017228066365011437L;

    private int exitCode = 0;

    private String output = null;

    /**
     * Constructs a {@link CommandException} with the given error message.
     *
     * @param message
     *          the error message.
     */
    public CommandException(String message) {
        new RuntimeException(message);
    }

    /**
     * Constructs a {@link CommandException} with the given error message, exit
     * code and output.
     *
     * @param message
     *          the error message.
     * @param exitCode
     *          the command exit code.
     * @param output
     *          the command output.
     */
    public CommandException(String message, int exitCode, String output) {
        new RuntimeException(message);
        this.exitCode = exitCode;
        this.output = output;
    }

    /**
     * Constructs a {@link CommandException} with the given error message and
     * cause exception.
     *
     * @param message
     *          the error message.
     * @param cause
     *          the cause exception.
     */
    public CommandException(String message, Throwable cause) {
      System.out.println(message+"----"+cause);;
    }

    /**
     * @return the exitCode
     */
    public int getExitCode() {
        return exitCode;
    }

    /**
     * @return the output
     */
    public String getOutput() {
        return output;
    }

}
