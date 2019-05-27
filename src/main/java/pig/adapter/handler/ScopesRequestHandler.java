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

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import com.microsoft.java.debug.core.protocol.Messages.Response;
import com.microsoft.java.debug.core.protocol.Requests.Arguments;
import com.microsoft.java.debug.core.protocol.Requests.Command;
import com.microsoft.java.debug.core.protocol.Responses;
import com.microsoft.java.debug.core.protocol.Types;
import com.microsoft.java.debug.core.protocol.Types.Scope;

import org.apache.pig.PigServer;

import pig.adapter.AdapterUtils;
import pig.adapter.ErrorCode;
import pig.adapter.IDebugAdapterContext;
import pig.adapter.IDebugRequestHandler;
import pig.adapter.IDebugSession;

public class ScopesRequestHandler implements IDebugRequestHandler {

    @Override
    public List<Command> getTargetCommands() {
        return Arrays.asList(Command.SCOPES);
    }

    @Override
    public CompletableFuture<Response> handle(Command command, Arguments arguments, Response response,
            IDebugAdapterContext context) {
        IDebugSession session = context.getDebugSession();
        if (session == null) {
            return AdapterUtils.createAsyncErrorResponse(response, ErrorCode.EMPTY_DEBUG_SESSION,
                    "Empty debug session.");
        }

        PigServer server = session.getPigServer();
        List<Types.Scope> scopes = server.getAliasKeySet().stream().filter(alias -> {
            try {
                server.dumpSchema(alias);
                return true;
            } catch (Exception e) {
                return false;
            }
        }).map(alias -> new Scope(alias, alias.hashCode(), true)).collect(Collectors.toList());

        response.body = new Responses.ScopesResponseBody(scopes);
        return CompletableFuture.completedFuture(response);
    }
}
