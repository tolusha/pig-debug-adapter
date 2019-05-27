/*******************************************************************************
* Copyright (c) 2018 Microsoft Corporation and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*     Microsoft Corporation - initial API and implementation
*******************************************************************************/

package pig.adapter.handler;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.microsoft.java.debug.core.protocol.Events;
import com.microsoft.java.debug.core.protocol.Events.TerminatedEvent;
import com.microsoft.java.debug.core.protocol.Messages.Response;
import com.microsoft.java.debug.core.protocol.Requests.Arguments;
import com.microsoft.java.debug.core.protocol.Requests.Command;
import com.microsoft.java.debug.core.protocol.Requests.LaunchArguments;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.pig.PigServer;
import org.apache.pig.data.Tuple;

import pig.adapter.AdapterUtils;
import pig.adapter.DebugException;
import pig.adapter.ErrorCode;
import pig.adapter.IDebugAdapterContext;
import pig.adapter.IDebugRequestHandler;
import pig.adapter.IDebugSession;
import pig.adapter.ILaunchDelegate;
import pig.adapter.LaunchMode;
import pig.adapter.LaunchWithDebuggingDelegate;

public class LaunchRequestHandler implements IDebugRequestHandler {
    protected static final Logger logger = Logger.getLogger(LaunchRequestHandler.class.getName());
    protected ILaunchDelegate activeLaunchHandler;

    @Override
    public List<Command> getTargetCommands() {
        return Arrays.asList(Command.LAUNCH);
    }

    @Override
    public CompletableFuture<Response> handle(Command command, Arguments arguments, Response response,
            IDebugAdapterContext context) {
        activeLaunchHandler = new LaunchWithDebuggingDelegate();
        return handleLaunchCommand(arguments, response, context);
    }

    protected CompletableFuture<Response> handleLaunchCommand(Arguments arguments, Response response,
            IDebugAdapterContext context) {
        LaunchArguments launchArguments = (LaunchArguments) arguments;

        if (StringUtils.isBlank(launchArguments.program)) {
            throw AdapterUtils.createCompletionException(
                    "Failed to launch debuggee. Missing program options in launch configuration.",
                    ErrorCode.ARGUMENT_MISSING);
        }
        context.setLaunchMode(LaunchMode.DEBUG);

        activeLaunchHandler.preLaunch(launchArguments, context);

        return launch(launchArguments, response, context).thenCompose(res -> {
            if (res.success) {
                IDebugSession debugSession = context.getDebugSession();

                debugSession.addObserver(event -> {
                    if (event instanceof TerminatedEvent) {
                        PigServer server = debugSession.getPigServer();
                        String alias = server.getLastRel();
                        try {
                            List<Tuple> value = AdapterUtils.readValue(server, alias);
                            context.getProtocolServer()
                                    .sendEvent(Events.OutputEvent.createConsoleOutput(value.toString()));
                        } catch (IOException e) {
                            logger.error(e.getMessage(), e);
                        }
                    }
                    context.getProtocolServer().sendEvent(event);
                });
                activeLaunchHandler.postLaunch(launchArguments, context);

                debugSession.start();
            }
            return CompletableFuture.completedFuture(res);
        });
    }

    protected CompletableFuture<Response> launch(LaunchArguments launchArguments, Response response,
            IDebugAdapterContext context) {

        CompletableFuture<Response> resultFuture = new CompletableFuture<>();
        try {
            activeLaunchHandler.launch(launchArguments, context);
            resultFuture.complete(response);
        } catch (IOException e) {
            resultFuture.completeExceptionally(
                    new DebugException(String.format("Failed to start Pig Server. Reason: %s", e.toString()),
                            ErrorCode.LAUNCH_FAILURE.getId()));
        }

        return resultFuture;
    }
}
