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

import com.microsoft.java.debug.core.protocol.Messages;
import com.microsoft.java.debug.core.protocol.Requests;
import com.microsoft.java.debug.core.protocol.Types;

import pig.adapter.IDebugAdapterContext;
import pig.adapter.IDebugRequestHandler;

public class InitializeRequestHandler implements IDebugRequestHandler {
    @Override
    public List<Requests.Command> getTargetCommands() {
        return Arrays.asList(Requests.Command.INITIALIZE);
    }

    @Override
    public CompletableFuture<Messages.Response> handle(Requests.Command command, Requests.Arguments argument,
            Messages.Response response, IDebugAdapterContext context) {
        Requests.InitializeArguments initializeArguments = (Requests.InitializeArguments) argument;
        context.setClientLinesStartAt1(initializeArguments.linesStartAt1);
        context.setClientColumnsStartAt1(initializeArguments.columnsStartAt1);
        String pathFormat = initializeArguments.pathFormat;
        if (pathFormat != null) {
            switch (pathFormat) {
            case "uri":
                context.setClientPathsAreUri(true);
                break;
            default:
                context.setClientPathsAreUri(false);
            }
        }
        context.setSupportsRunInTerminalRequest(initializeArguments.supportsRunInTerminalRequest);

        Types.Capabilities caps = new Types.Capabilities();
        caps.supportsConfigurationDoneRequest = true;
        caps.supportTerminateDebuggee = true;
        caps.supportsLogPoints = true;
        caps.supportsEvaluateForHovers = true;
        response.body = caps;
        return CompletableFuture.completedFuture(response);
    }
}
