/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 * <p>
 * https://github.com/keeps/roda
 */
package com.example.demo1_nacos.antivirus;


import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

/**
 * System command utility.
 *
 * @author Rui Castro
 * @author Luis Faria
 */
public class CommandUtility {

    private CommandUtility() {
        // do nothing
    }

    /**
     * Execute the given command line.
     *
     * @param args the command line as a list of arguments.
     * @return a {@link String} with the output of the command.
     * @throws CommandException
     */
    public static String execute(String... args) throws CommandException {
        return execute(true, args);
    }

    /**
     * Execute the given command line.
     *
     * @param args the command line as a list of arguments.
     * @return a {@link String} with the output of the command.
     * @throws CommandException
     */
    public static String execute(boolean withErrorStream, String... args) throws CommandException {
        int exitValue = 0;
        String output;

        try {
            StringBuilder builder = new StringBuilder();
            for (String arg : args) {
                builder.append(arg + " ");
            }

            LogUtil.debug("Executing {}" + builder);

            // create and execute process
            System.out.println("=="+args);
            ProcessBuilder processBuilder = new ProcessBuilder(args);
            processBuilder.redirectErrorStream(withErrorStream);
            Process process = processBuilder.start();

            // Get process output
            InputStream is = process.getInputStream();
            CaptureOutputThread captureOutputThread = new CaptureOutputThread(is);

            synchronized (is) {
                captureOutputThread.start();

                // Wait until the CaptureOutputThread notifies that is finished
                // reading the input stream.
                LogUtil.debug("Waiting until CaptureOutputThread notifies");
                is.wait();
            }

            LogUtil.debug("CaptureOutputThread notified. Getting output...");
            output = captureOutputThread.output;

            // Get process exit value
            exitValue = process.waitFor();

            IOUtils.closeQuietly(is);


            if (exitValue == 0) {
                return output;
            } else {
                throw new CommandException("Command " + Arrays.toString(args) + " terminated with error code " + exitValue,
                        exitValue, output);
            }

        } catch (IOException | InterruptedException e) {
            LogUtil.debug("Error executing command " + Arrays.toString(args), e);

            throw new CommandException("Error executing command " + Arrays.toString(args) + " - " + e.getMessage(), e);
        }
    }

    /**
     * Execute the given command line.
     *
     * @param args the command line as a list of arguments.
     * @return a {@link String} with the output of the command.
     * @throws CommandException
     */
    public static String execute(List<String> args) throws CommandException {
        return execute(args, true);
    }

    public static String execute(List<String> args, boolean withErrorStream) throws CommandException {
        return execute(withErrorStream, args.toArray(new String[args.size()]));
    }

}

class CaptureOutputThread extends Thread {

    InputStream is;
    String output;

    public CaptureOutputThread(InputStream is) {
        this.is = is;
    }

    @Override
    public void run() {
        StringBuilder outputBuffer = new StringBuilder();

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;

            while ((line = reader.readLine()) != null) {
                outputBuffer.append(line + System.lineSeparator());
            }

        } catch (IOException e) {
            LogUtil.error("Exception reading from inputstream", e);
        }

        output = outputBuffer.toString();

        synchronized (is) {
            is.notify();
        }
    }
}
