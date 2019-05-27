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
package pig.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.microsoft.java.debug.core.protocol.IProtocolServer;
import com.microsoft.java.debug.core.protocol.JsonUtils;
import com.microsoft.java.debug.core.protocol.Messages;
import com.microsoft.java.debug.core.protocol.Requests.Arguments;
import com.microsoft.java.debug.core.protocol.Requests.Command;

import pig.adapter.handler.ConfigurationDoneRequestHandler;
import pig.adapter.handler.DisconnectRequestHandler;
import pig.adapter.handler.EvaluateRequestHandler;
import pig.adapter.handler.InitializeRequestHandler;
import pig.adapter.handler.LaunchRequestHandler;
import pig.adapter.handler.ScopesRequestHandler;
import pig.adapter.handler.SetBreakpointsRequestHandler;
import pig.adapter.handler.SourceRequestHandler;
import pig.adapter.handler.StackTraceRequestHandler;
import pig.adapter.handler.StepRequestHandler;
import pig.adapter.handler.TerminateRequestHandler;
import pig.adapter.handler.ThreadsRequestHandler;
import pig.adapter.handler.VariablesRequestHandler;

public class DebugAdapter implements IDebugAdapter {
    private static final Logger logger = Logger.getLogger(DebugAdapter.class.getName());

    private IDebugAdapterContext debugContext = null;
    private Map<Command, List<IDebugRequestHandler>> requestHandlersForDebug = null;
    private Map<Command, List<IDebugRequestHandler>> requestHandlersForNoDebug = null;

    /**
     * Constructor.
     */
    public DebugAdapter(IProtocolServer server) {
        this.debugContext = new DebugAdapterContext(server);
        requestHandlersForDebug = new HashMap<>();
        requestHandlersForNoDebug = new HashMap<>();
        initialize();
    }

    @Override
    public CompletableFuture<Messages.Response> dispatchRequest(Messages.Request request) {
        Messages.Response response = new Messages.Response();
        response.request_seq = request.seq;
        response.command = request.command;
        response.success = true;

        Command command = Command.parse(request.command);
        Arguments cmdArgs = JsonUtils.fromJson(request.arguments, command.getArgumentType());

        if (debugContext.isPigServerTerminated() && command != Command.DISCONNECT) {
            return CompletableFuture.completedFuture(response);
        }
        List<IDebugRequestHandler> handlers = this.debugContext.getLaunchMode() == LaunchMode.DEBUG
                ? requestHandlersForDebug.get(command) : requestHandlersForNoDebug.get(command);
        if (handlers != null && !handlers.isEmpty()) {
            CompletableFuture<Messages.Response> future = CompletableFuture.completedFuture(response);
            for (IDebugRequestHandler handler : handlers) {
                future = future.thenCompose((res) -> {
                    return handler.handle(command, cmdArgs, res, debugContext);
                });
            }
            return future;
        } else {
            final String errorMessage = String.format("Unrecognized request: { _request: %s }", request.command);
            logger.log(Level.SEVERE, errorMessage);
            return AdapterUtils.createAsyncErrorResponse(response, ErrorCode.UNRECOGNIZED_REQUEST_FAILURE, errorMessage);
        }
    }

    private void initialize() {
        // Register request handlers.
        // When there are multiple handlers registered for the same request, follow the rule "first register, first execute".
        registerHandler(new InitializeRequestHandler());
        registerHandler(new LaunchRequestHandler());

        // DEBUG node only
        registerHandlerForDebug(new ConfigurationDoneRequestHandler());
        registerHandlerForDebug(new DisconnectRequestHandler());
        registerHandlerForDebug(new TerminateRequestHandler());
        registerHandlerForDebug(new SetBreakpointsRequestHandler());
        registerHandlerForDebug(new SourceRequestHandler());
        registerHandlerForDebug(new ThreadsRequestHandler());
        registerHandlerForDebug(new StepRequestHandler());
        registerHandlerForDebug(new StackTraceRequestHandler());
        registerHandlerForDebug(new ScopesRequestHandler());
        registerHandlerForDebug(new VariablesRequestHandler());
        registerHandlerForDebug(new EvaluateRequestHandler());
    }

    private void registerHandlerForDebug(IDebugRequestHandler handler) {
        registerHandler(requestHandlersForDebug, handler);
    }

    private void registerHandler(IDebugRequestHandler handler) {
        registerHandler(requestHandlersForDebug, handler);
        registerHandler(requestHandlersForNoDebug, handler);
    }

    private void registerHandler(Map<Command, List<IDebugRequestHandler>> requestHandlers, IDebugRequestHandler handler) {
        for (Command command : handler.getTargetCommands()) {
            List<IDebugRequestHandler> handlerList = requestHandlers.get(command);
            if (handlerList == null) {
                handlerList = new ArrayList<>();
                requestHandlers.put(command, handlerList);
            }
            handler.initialize(debugContext);
            handlerList.add(handler);
        }
    }
}
