/*******************************************************************************
* Copyright (c) 2017 Microsoft Corporation and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*     Microsoft Corporation - initial API and implementation
*******************************************************************************/

package com.microsoft.java.debug.core.protocol;

import java.util.Arrays;
import java.util.Map;

import com.google.gson.annotations.SerializedName;

/**
 * The request arguments types defined by VSCode Debug Protocol.
 */
public class Requests {

    public static class ValueFormat {
        public boolean hex;
    }

    public static class Arguments {

    }

    public static class InitializeArguments extends Arguments {
        public String clientID;
        public String adapterID;
        public String pathFormat;
        public boolean linesStartAt1;
        public boolean columnsStartAt1;
        public boolean supportsVariableType;
        public boolean supportsVariablePaging;
        public boolean supportsRunInTerminalRequest;
    }

    public static class StepFilters {
        public String[] classNameFilters = new String[0];
        public boolean skipSynthetics;
        public boolean skipStaticInitializers;
        public boolean skipConstructors;
    }

    public static class LaunchBaseArguments extends Arguments {
    }

    public static enum CONSOLE {
        internalConsole, integratedTerminal, externalTerminal;
    }

    public static enum ShortenApproach {
        @SerializedName("none")
        NONE, @SerializedName("jarmanifest")
        JARMANIFEST, @SerializedName("argfile")
        ARGFILE;
    }

    public static class LaunchArguments extends LaunchBaseArguments {
        public String program = "";
        public boolean stopOnEntry;
    }

    public static class RunInTerminalRequestArguments extends Arguments {
        public String kind; // Supported kind should be "integrated" or "external".
        public String title;
        public String cwd; // required.
        public String[] args; // required.
        public Map<String, String> env;

        private RunInTerminalRequestArguments() {
            // do nothing.
        }

        /**
         * Create a RunInTerminalRequestArguments instance.
         *
         * @param cmds List of command arguments. The first arguments is the command to
         *             run.
         * @param cwd  Working directory of the command.
         * @return the request arguments instance.
         */
        public static RunInTerminalRequestArguments createIntegratedTerminal(String[] cmds, String cwd) {
            RunInTerminalRequestArguments requestArgs = new RunInTerminalRequestArguments();
            requestArgs.args = cmds;
            requestArgs.cwd = cwd;
            requestArgs.kind = "integrated";
            return requestArgs;
        }

        /**
         * Create a RunInTerminalRequestArguments instance.
         *
         * @param cmds  List of command arguments. The first arguments is the command to
         *              run.
         * @param cwd   Working directory of the command.
         * @param env   Environment key-value pairs that are added to the default
         *              environment.
         * @param title Optional title of the terminal.
         * @return the request arguments instance.
         */
        public static RunInTerminalRequestArguments createIntegratedTerminal(String[] cmds, String cwd,
                Map<String, String> env, String title) {
            RunInTerminalRequestArguments requestArgs = createIntegratedTerminal(cmds, cwd);
            requestArgs.env = env;
            requestArgs.title = title;
            return requestArgs;
        }

        /**
         * Create a RunInTerminalRequestArguments instance.
         *
         * @param cmds List of command arguments. The first arguments is the command to
         *             run.
         * @param cwd  Working directory of the command.
         * @return the request arguments instance.
         */
        public static RunInTerminalRequestArguments createExternalTerminal(String[] cmds, String cwd) {
            RunInTerminalRequestArguments requestArgs = new RunInTerminalRequestArguments();
            requestArgs.args = cmds;
            requestArgs.cwd = cwd;
            requestArgs.kind = "external";
            return requestArgs;
        }

        /**
         * Create a RunInTerminalRequestArguments instance.
         *
         * @param cmds  List of command arguments. The first arguments is the command to
         *              run.
         * @param cwd   Working directory of the command.
         * @param env   Environment key-value pairs that are added to the default
         *              environment.
         * @param title Optional title of the terminal.
         * @return the request arguments instance.
         */
        public static RunInTerminalRequestArguments createExternalTerminal(String[] cmds, String cwd,
                Map<String, String> env, String title) {
            RunInTerminalRequestArguments requestArgs = createExternalTerminal(cmds, cwd);
            requestArgs.env = env;
            requestArgs.title = title;
            return requestArgs;
        }
    }

    public static class RestartArguments extends Arguments {

    }

    public static class DisconnectArguments extends Arguments {
        // If client doesn't set terminateDebuggee attribute at the DisconnectRequest,
        // the debugger would choose to terminate debuggee by default.
        public boolean terminateDebuggee = true;
        public boolean restart;
    }

    public static class TerminateArguments extends Arguments {
        public boolean restart;
    }

    public static class ConfigurationDoneArguments extends Arguments {

    }

    public static class SetBreakpointArguments extends Arguments {
        public Types.Source source;
        public int[] lines = new int[0];
        public Types.SourceBreakpoint[] breakpoints = new Types.SourceBreakpoint[0];
        public boolean sourceModified = false;
    }

    public static class StackTraceArguments extends Arguments {
        public long threadId;
        public int startFrame;
        public int levels;
    }

    public static class SetFunctionBreakpointsArguments extends Arguments {
        public Types.FunctionBreakpoint[] breakpoints;
    }

    public static class SetExceptionBreakpointsArguments extends Arguments {
        public String[] filters = new String[0];
    }

    public static class ExceptionInfoArguments extends Arguments {
        public long threadId;
    }

    public static class ThreadsArguments extends Arguments {

    }

    public static class ContinueArguments extends Arguments {
        public long threadId;
    }

    public static class StepArguments extends Arguments {
        public long threadId;
    }

    public static class NextArguments extends StepArguments {

    }

    public static class StepInArguments extends StepArguments {
        public int targetId;
    }

    public static class StepOutArguments extends StepArguments {

    }

    public static class PauseArguments extends Arguments {
        public long threadId;
    }

    public static class ScopesArguments extends Arguments {
        public int frameId;
    }

    public static class VariablesArguments extends Arguments {
        public int variablesReference = -1;
        public String filter;
        public int start;
        public int count;
        public ValueFormat format;
    }

    public static class SetVariableArguments extends Arguments {
        public int variablesReference;
        public String name;
        public String value;
        public ValueFormat format;
    }

    public static class SourceArguments extends Arguments {
        public int sourceReference;
    }

    public static class EvaluateArguments extends Arguments {
        public String expression;
        public int frameId;
        public String context;
        public ValueFormat format;
    }

    public static class RedefineClassesArguments extends Arguments {

    }

    public static class RestartFrameArguments extends Arguments {
        public int frameId;
    }

    public static class CompletionsArguments extends Arguments {
        public int frameId;
        public String text;
        public int line;
        public int column;
    }

    public static enum Command {
        INITIALIZE("initialize", InitializeArguments.class), LAUNCH("launch", LaunchArguments.class),
        DISCONNECT("disconnect", DisconnectArguments.class),
        TERMINATE("terminate", TerminateArguments.class),
        CONFIGURATIONDONE("configurationDone", ConfigurationDoneArguments.class), NEXT("next", NextArguments.class),
        CONTINUE("continue", ContinueArguments.class), STEPIN("stepIn", StepInArguments.class),
        STEPOUT("stepOut", StepOutArguments.class), PAUSE("pause", PauseArguments.class),
        STACKTRACE("stackTrace", StackTraceArguments.class), RESTARTFRAME("restartFrame", RestartFrameArguments.class),
        SCOPES("scopes", ScopesArguments.class), VARIABLES("variables", VariablesArguments.class),
        SETVARIABLE("setVariable", SetVariableArguments.class), SOURCE("source", SourceArguments.class),
        THREADS("threads", ThreadsArguments.class), SETBREAKPOINTS("setBreakpoints", SetBreakpointArguments.class),
        SETEXCEPTIONBREAKPOINTS("setExceptionBreakpoints", SetExceptionBreakpointsArguments.class),
        SETFUNCTIONBREAKPOINTS("setFunctionBreakpoints", SetFunctionBreakpointsArguments.class),
        EVALUATE("evaluate", EvaluateArguments.class), COMPLETIONS("completions", CompletionsArguments.class),
        RUNINTERMINAL("runInTerminal", RunInTerminalRequestArguments.class),
        REDEFINECLASSES("redefineClasses", RedefineClassesArguments.class),
        EXCEPTIONINFO("exceptionInfo", ExceptionInfoArguments.class), UNSUPPORTED("", Arguments.class);

        private String command;
        private Class<? extends Arguments> argumentType;

        Command(String command, Class<? extends Arguments> argumentType) {
            this.command = command;
            this.argumentType = argumentType;
        }

        public String getName() {
            return this.command;
        }

        @Override
        public String toString() {
            return this.command;
        }

        public Class<? extends Arguments> getArgumentType() {
            return this.argumentType;
        }

        /**
         * Get the corresponding Command type by the command name. If the command is not
         * defined in the enum type, return UNSUPPORTED.
         *
         * @param command the command name
         * @return the Command type
         */
        public static Command parse(String command) {
            Command[] found = Arrays.stream(Command.values()).filter(cmd -> {
                return cmd.toString().equals(command);
            }).toArray(Command[]::new);

            if (found.length > 0) {
                return found[0];
            }
            return UNSUPPORTED;
        }
    }
}
