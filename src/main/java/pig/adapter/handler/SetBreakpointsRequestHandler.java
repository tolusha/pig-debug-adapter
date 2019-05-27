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

import pig.adapter.AdapterUtils;
import pig.adapter.ErrorCode;
import pig.adapter.IDebugAdapterContext;
import pig.adapter.IDebugRequestHandler;
import pig.adapter.IDebugSession;

import com.microsoft.java.debug.core.protocol.Messages.Response;
import com.microsoft.java.debug.core.protocol.Requests.Arguments;
import com.microsoft.java.debug.core.protocol.Requests.Command;
import com.microsoft.java.debug.core.protocol.Requests.SetBreakpointArguments;
import com.microsoft.java.debug.core.protocol.Responses;
import com.microsoft.java.debug.core.protocol.Types.Breakpoint;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class SetBreakpointsRequestHandler implements IDebugRequestHandler {

    private boolean registered = false;

    @Override
    public List<Command> getTargetCommands() {
        return Arrays.asList(Command.SETBREAKPOINTS);
    }

    @Override
    public CompletableFuture<Response> handle(Command command, Arguments arguments, Response response,
            IDebugAdapterContext context) {

        IDebugSession session = context.getDebugSession();
        if (session == null) {
            return AdapterUtils.createAsyncErrorResponse(response, ErrorCode.EMPTY_DEBUG_SESSION,
                    "Empty debug session.");
        }

        SetBreakpointArguments bpArguments = (SetBreakpointArguments) arguments;
        if (bpArguments.source.path.equals(session.getFileContext().getFile())) {
             session.setBreakpoints(Arrays.stream(bpArguments.breakpoints).mapToInt(value -> value.line).toArray());
        }

        List<Breakpoint> breakpoints = new ArrayList<>();
        for (int i = 0; i < bpArguments.lines.length; i++) {
            boolean verified;
            try {
                String line = session.getFileContext().readLine(bpArguments.lines[i]);
                verified = AdapterUtils.isQuery(line);
            } catch (IOException e) {
                verified = false;
            }
            breakpoints.add(new Breakpoint(bpArguments.lines[i], verified, bpArguments.lines[i], ""));
        }

        response.body = new Responses.SetBreakpointsResponseBody(breakpoints);
        return CompletableFuture.completedFuture(response);
    }
}
