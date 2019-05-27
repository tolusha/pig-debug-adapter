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

import com.google.common.collect.Lists;
import com.microsoft.java.debug.core.protocol.Messages.Response;
import com.microsoft.java.debug.core.protocol.Requests.Arguments;
import com.microsoft.java.debug.core.protocol.Requests.Command;
import com.microsoft.java.debug.core.protocol.Responses;
import com.microsoft.java.debug.core.protocol.Types;

import pig.adapter.AdapterUtils;
import pig.adapter.ErrorCode;
import pig.adapter.IDebugAdapterContext;
import pig.adapter.IDebugRequestHandler;
import pig.adapter.IDebugSession;
import pig.adapter.IFileContext;

public class StackTraceRequestHandler implements IDebugRequestHandler {

    @Override
    public List<Command> getTargetCommands() {
        return Arrays.asList(Command.STACKTRACE);
    }

    @Override
    public CompletableFuture<Response> handle(Command command, Arguments arguments, Response response,
            IDebugAdapterContext context) {

        IDebugSession session = context.getDebugSession();
        if (session == null) {
            return AdapterUtils.createAsyncErrorResponse(response, ErrorCode.EMPTY_DEBUG_SESSION,
                    "Empty debug session.");
        }

        IFileContext file = session.getFileContext();

        Types.Source source = new Types.Source(file.getName(), file.getFile(), 0);
        Types.StackFrame stackFrame;
        String stackFrameName = null;
        try {
            stackFrameName = file.readLine(session.getCurrentLine());
        } catch (IOException e) {
            stackFrameName = "< no line to debug >";
        }
        stackFrame = new Types.StackFrame(1, stackFrameName, source, session.getCurrentLine(), 0);

        response.body = new Responses.StackTraceResponseBody(Lists.asList(stackFrame, new Types.StackFrame[0]), 1);
        return CompletableFuture.completedFuture(response);
    }
}
