package com.github.brickwall2900.diary.hacks;

import java.io.File;
import java.io.IOException;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URISyntaxException;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.io.File.separatorChar;
public class JavaProcessHelper {
    /**
     * Gets the currently running Java Virutal Machine's executable path
     *
     * @throws NullPointerException if JVM's executable path wasn't found
     * @return the path to the currently running JVM executable
     */
    public static String getJavaVM(boolean hasConsole) {
        // Method 1 :: System.getProperty("java.home")
        String javaPath = System.getProperty("java.home");
        String binPath = separatorChar + "bin" + separatorChar;
        String javaExecutable = hasConsole ? (binPath + "java.exe") : (binPath + "javaw.exe");
        if (javaPath != null)
            return javaPath + javaExecutable;

        // Method 2 :: %JAVA_HOME%
        javaPath = System.getenv("JAVA_HOME");
        if (javaPath != null)
            return javaPath + javaExecutable;

        // Method 3 :: arguments?
        Optional<String[]> args = ProcessHandle.current().info().arguments();
        if (args.isPresent()) {
            javaPath = args.get()[0];
            return javaPath;
        }

        // no JVM can be found. how did this run anyway?
        throw new NullPointerException("what kind of JVM is this?");
    }

    /**
     * @return Returns true if console exists by {@link System#console()}
     */
    public static boolean hasConsole() {
        return System.console() != null;
    }

    /**
     * Tries to find the main class containing the {@code public static void main(String[] args)} signature
     *
     * @throws NullPointerException if the main class wasn't found
     * @return the fully qualified class name for the main class
     */
    public static String getMainClass() {
        // Method 1 :: sun.java.command
        String[] javaArgs = System.getProperty("sun.java.command").split(" ");
        String mainClass = javaArgs[0];
        boolean canReturn;
        try {
            Class<?> cls = Class.forName(mainClass);
            Method signatureMethod = cls.getDeclaredMethod("main", String[].class);
            int modifiers = signatureMethod.getModifiers();
            canReturn = Modifier.isPublic(modifiers) || Modifier.isStatic(modifiers);
        } catch (ReflectiveOperationException e) {
            canReturn = false;
        }
        if (canReturn) return mainClass;

        // Method 2 :: Digging through StackWalker
        StackWalker stackWalker = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);
        List<StackWalker.StackFrame> frames = stackWalker.walk(Stream::toList);
        String name = frames.get(frames.size() - 1).getDeclaringClass().getName();
        if (!name.startsWith("java.lang.Thread")) return name;

        // we failed
        throw new NullPointerException("Main class wasn't found!");
    }

    /**
     * Gets the JAR file of the input class file.
     *
     * @param cls the input class to find the JAR file
     * @throws URISyntaxException if URI somehow wasn't formatted correctly
     * @return the JAR file of the class file
     */
    public static File getCodeSource(Class<?> cls) throws URISyntaxException {
        return new File(cls.getProtectionDomain().getCodeSource().getLocation()
                .toURI());
    }

    /**
     * Creates the new arguments that can be used for relaunching this process
     *
     * @param moreArguments pass only if extra arguments are needed
     * @return a list of arguments
     */
    public static List<String> createArguments(List<String> moreArguments) {
        String determinedMainClass = getMainClass();

        RuntimeMXBean runtime = java.lang.management.ManagementFactory.getRuntimeMXBean();
        List<String> runtimeArgs = runtime.getInputArguments();
        List<String> cmdArgs = new ArrayList<>(runtimeArgs);

        if (moreArguments != null) {
            cmdArgs.addAll(moreArguments);
        }
        cmdArgs.add("-cp");
        cmdArgs.add(System.getProperty("java.class.path"));
        cmdArgs.add(determinedMainClass);
        return cmdArgs;
    }

    /**
     * Converts a list to a string representation of it
     * @param list the input list
     * @param separator separator used to separate items on the list
     * @return a string representation of the list
     */
    public static <T> String toString(List<T> list, String separator) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            T appended = list.get(i);
            if (i < list.size() - 1) {
                builder.append(appended).append(separator);
            } else {
                builder.append(appended);
            }
        }
        return builder.toString();
    }

    /**
     * @param prefix prefix of property to be included
     * @return a map of the properties starting with the prefix
     */
    public static Map<String, String> getPropertiesStartingWith(String prefix) {
        Properties properties = System.getProperties();
        Map<String, String> map = new HashMap<>();
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            String name = (String) entry.getKey();
            if (name.startsWith(prefix)) {
                String value = (String) entry.getValue();
                map.put(name, value);
            }
        }
        return map;
    }

    /**
     * @param args input map of properties
     * @return arguments that have mapped properties into format {@code -Dkey=value}
     */
    public static String[] mapPropertiesToArguments(Map<String, String> args) {
        String[] arguments = new String[args.size()];
        int idx = 0;
        for (Map.Entry<String, String> entry : args.entrySet()) {
            String key = entry.getKey();
            String val = entry.getValue();
            arguments[idx] = String.format("-D%s=%s", key, val);
            idx++;
        }
        return arguments;
    }

    /**
     * Attempts to relaunch the process with the same currently running JVM, and given the arguments
     * @param args command line arguments
     * @return a process
     * @throws IOException if relaunching fails
     */
    public static Process relaunch(List<String> args) throws IOException {
        String jvm = getJavaVM(hasConsole());
        ProcessBuilder processBuilder = new ProcessBuilder();
        List<String> arguments = new ArrayList<>();
        arguments.add(new File(jvm).getAbsolutePath());
        arguments.addAll(args);
        processBuilder.command(arguments);
        return processBuilder.inheritIO().start();
    }

    /**
     * @return the file for the temporary directory set by {@code java.io.tmpdir}
     */
    public static File getTempDirectory() {
        return new File(System.getProperty("java.io.tmpdir"));
    }

    /**
     * Check if a debugger is attached to the current running process.
     * @implNote <a href="https://stackoverflow.com/a/73125047">Copied from here!</a>
     * @return {@code true} if a debugger is attached, {@code false} otherwise.
     */
    public static boolean isDebuggerPresent() {
        // Get ahold of the Java Runtime Environment (JRE) management interface
        RuntimeMXBean runtime = java.lang.management.ManagementFactory.getRuntimeMXBean();

        // Get the command line arguments that we were originally passed in
        List<String> args = runtime.getInputArguments();

        // Check if the Java Debug Wire Protocol (JDWP) agent is used.
        // One of the items might contain something like "-agentlib:jdwp=transport=dt_socket,address=9009,server=y,suspend=n"
        // We're looking for the string "jdwp".
        boolean jdwpPresent = args.toString().contains("jdwp");

        return jdwpPresent;
    }
}
