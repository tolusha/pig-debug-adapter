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

import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.microsoft.java.debug.core.protocol.AbstractProtocolServer;
import com.microsoft.java.debug.core.protocol.Events.DebugEvent;
import com.microsoft.java.debug.core.protocol.Events.StoppedEvent;
import com.microsoft.java.debug.core.protocol.JsonUtils;
import com.microsoft.java.debug.core.protocol.Messages;
import com.microsoft.java.debug.core.protocol.Messages.Response;

import org.apache.log4j.Logger;

/**
 * @author Anatolii Bazko
 */
public class ProtocolServer extends AbstractProtocolServer {
    private static final Logger logger = Logger.getLogger(ProtocolServer.class.getName());

    private IDebugAdapter debugAdapter;

    private Object lock = new Object();
    private boolean isDispatchingRequest = false;
    private ConcurrentLinkedQueue<DebugEvent> eventQueue = new ConcurrentLinkedQueue<>();

    /**
     * Constructs a protocol server instance based on the given input stream and
     * output stream.
     *
     * @param input  the input stream
     * @param output
     */
    public ProtocolServer(InputStream input, OutputStream output) {
        super(input, output);
        debugAdapter = new DebugAdapter(this);
    }

    @Override
    public void sendEvent(DebugEvent event) {
        String json = JsonUtils.toJson(event);
        logger.info("EVENT: " + json);

        // See the two bugs https://github.com/Microsoft/java-debug/issues/134 and
        // https://github.com/Microsoft/vscode/issues/58327,
        // it requires the java-debug to send the StoppedEvent after
        // ContinueResponse/StepResponse is received by DA.
        if (event instanceof StoppedEvent) {
            sendEventLater(event);
        } else {
            super.sendEvent(event);
        }
    }

    /**
     * If the the dispatcher is idle, then send the event to the DA immediately.
     * Else add the new event to an eventQueue first and send them when dispatcher
     * becomes idle again.
     */
    private void sendEventLater(DebugEvent event) {
        synchronized (lock) {
            if (this.isDispatchingRequest) {
                this.eventQueue.offer(event);
            } else {
                super.sendEvent(event);
            }
        }
    }

    @Override
    protected void dispatchRequest(Messages.Request request) {
        String json = JsonUtils.toJson(request);
        logger.info("REQUEST: " + json);

        try {
            synchronized (lock) {
                this.isDispatchingRequest = true;
            }

            debugAdapter.dispatchRequest(request).thenCompose((response) -> {
                CompletableFuture<Void> future = new CompletableFuture<>();
                if (response != null) {
                    sendResponse(response);
                    future.complete(null);
                } else {
                    future.completeExceptionally(
                            new DebugException("The request dispatcher should not return null response.",
                                    ErrorCode.UNKNOWN_FAILURE.getId()));
                }
                return future;
            }).exceptionally((ex) -> {
                Messages.Response response = new Messages.Response(request.seq, request.command);
                if (ex instanceof CompletionException && ex.getCause() != null) {
                    ex = ex.getCause();
                }

                String exceptionMessage = ex.getMessage() != null ? ex.getMessage() : ex.toString();
                ErrorCode errorCode = ex instanceof DebugException
                        ? ErrorCode.parse(((DebugException) ex).getErrorCode())
                        : ErrorCode.UNKNOWN_FAILURE;

                sendResponse(AdapterUtils.setErrorResponse(response, errorCode, exceptionMessage));
                return null;
            }).join();
        } finally {
            synchronized (lock) {
                this.isDispatchingRequest = false;
            }

            while (this.eventQueue.peek() != null) {
                super.sendEvent(this.eventQueue.poll());
            }
        }
    }

    @Override
    public void sendResponse(Response response) {
        String json = JsonUtils.toJson(response);
        logger.info("RESPONSE: " + json);

        super.sendResponse(response);
    }
}
