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

package pig.adapter.handler;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.microsoft.java.debug.core.protocol.Messages.Response;
import com.microsoft.java.debug.core.protocol.Requests.Arguments;
import com.microsoft.java.debug.core.protocol.Requests.Command;
import com.microsoft.java.debug.core.protocol.Requests.EvaluateArguments;
import com.microsoft.java.debug.core.protocol.Events;
import com.microsoft.java.debug.core.protocol.Responses;

import org.apache.pig.PigServer;
import org.apache.pig.data.Tuple;

import pig.adapter.AdapterUtils;
import pig.adapter.ErrorCode;
import pig.adapter.IDebugAdapterContext;
import pig.adapter.IDebugRequestHandler;
import pig.adapter.IDebugSession;

public class EvaluateRequestHandler implements IDebugRequestHandler {

    @Override
    public List<Command> getTargetCommands() {
        return Arrays.asList(Command.EVALUATE);
    }

    @Override
    public CompletableFuture<Response> handle(Command command, Arguments arguments, Response response,
            IDebugAdapterContext context) {
        IDebugSession session = context.getDebugSession();
        if (session == null) {
            return AdapterUtils.createAsyncErrorResponse(response, ErrorCode.EMPTY_DEBUG_SESSION,
                    "Empty debug session.");
        }

        return CompletableFuture.supplyAsync(() -> {
            try {

                EvaluateArguments evaluateArgs = (EvaluateArguments) arguments;
                PigServer server = session.getPigServer();

                // evaluate variable
                if (server.getAliasKeySet().contains(evaluateArgs.expression)) {
                    response.body = doEvaluate(server, evaluateArgs.expression);
                    return response;
                }

                server.registerQuery(evaluateArgs.expression);
                response.body = doEvaluate(server, server.getLastRel());

                return response;
            } catch (IOException e) {
                throw AdapterUtils.createCompletionException(
                        String.format("Cannot evaluate because of %s.", e.toString()), ErrorCode.EVALUATE_FAILURE);
            }
        });
    }

    private Object doEvaluate(PigServer server, String alias) throws IOException {
        List<Tuple> value = AdapterUtils.readValue(server, alias);
        Object body = new Responses.EvaluateResponseBody(value.toString(), 0, server.dumpSchema(alias).toString(), 0);
        return body;
    }
}
