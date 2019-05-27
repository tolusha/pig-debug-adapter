/*******************************************************************************
* Copyright (c) 2017-2019 Microsoft Corporation and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*     Microsoft Corporation - initial API and implementation
*******************************************************************************/
package pig.adapter.handler;

import pig.adapter.AdapterUtils;
import pig.adapter.ErrorCode;
import pig.adapter.IDebugAdapterContext;
import pig.adapter.IDebugRequestHandler;
import pig.adapter.IDebugSession;

import com.google.common.collect.Lists;
import com.microsoft.java.debug.core.protocol.Messages.Response;
import com.microsoft.java.debug.core.protocol.Requests.Arguments;
import com.microsoft.java.debug.core.protocol.Requests.Command;
import com.microsoft.java.debug.core.protocol.Requests.VariablesArguments;
import com.microsoft.java.debug.core.protocol.Responses;
import com.microsoft.java.debug.core.protocol.Types;

import org.apache.pig.PigServer;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class VariablesRequestHandler implements IDebugRequestHandler {

    @Override
    public List<Command> getTargetCommands() {
        return Arrays.asList(Command.VARIABLES);
    }

    @Override
    public CompletableFuture<Response> handle(Command command, Arguments arguments, Response response,
            IDebugAdapterContext context) {
        VariablesArguments varArgs = (VariablesArguments) arguments;

        IDebugSession session = context.getDebugSession();
        if (session == null) {
            return AdapterUtils.createAsyncErrorResponse(response, ErrorCode.EMPTY_DEBUG_SESSION,
                    "Empty debug session.");
        }

        PigServer server = session.getPigServer();
        server.getAliasKeySet().forEach(s -> {
            if (s.hashCode() == varArgs.variablesReference) {
                try {
                    String schema = server.dumpSchema(s).toString();
                    String value = AdapterUtils.readValue(server, s).toString();
                    Types.Variable var = new Types.Variable(s, value, schema, 0, s);
                    response.body = new Responses.VariablesResponseBody(Lists.newArrayList(var));
                } catch (IOException e) {
                }
            }
        });


        return CompletableFuture.completedFuture(response);
    }
}
