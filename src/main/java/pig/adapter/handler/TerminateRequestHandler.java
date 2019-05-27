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

import com.microsoft.java.debug.core.protocol.Messages.Response;
import com.microsoft.java.debug.core.protocol.Requests.Arguments;
import com.microsoft.java.debug.core.protocol.Requests.Command;

import pig.adapter.IDebugAdapterContext;
import pig.adapter.IDebugSession;

public class TerminateRequestHandler extends InitializeRequestHandler {

    @Override
    public List<Command> getTargetCommands() {
        return Arrays.asList(Command.TERMINATE);
    }

    @Override
    public CompletableFuture<Response> handle(Command command, Arguments arguments, Response response,
            IDebugAdapterContext context) {

        IDebugSession session = context.getDebugSession();
        if (session != null) {
            session.terminate();
        }
        return CompletableFuture.completedFuture(response);
    }
}
