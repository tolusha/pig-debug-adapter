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

import java.io.IOException;

import com.microsoft.java.debug.core.protocol.Events;
import com.microsoft.java.debug.core.protocol.Requests.LaunchArguments;

public class LaunchWithDebuggingDelegate implements ILaunchDelegate {

    @Override
    public void launch(LaunchArguments launchArguments, IDebugAdapterContext context) throws IOException {
        IDebugSession session = new DebugSession(launchArguments.stopOnEntry, launchArguments.program);
        context.setDebugSession(session);
    }

    @Override
    public void postLaunch(LaunchArguments launchArguments, IDebugAdapterContext context) {
        context.getProtocolServer().sendEvent(new Events.InitializedEvent());
    }

    @Override
    public void preLaunch(LaunchArguments launchArguments, IDebugAdapterContext context) {
        context.setAttached(false);
    }
}
