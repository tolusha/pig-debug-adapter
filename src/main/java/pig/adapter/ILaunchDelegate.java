/*******************************************************************************
 * Copyright (c) 2018 Microsoft Corporation and others.
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

import com.microsoft.java.debug.core.protocol.Requests.LaunchArguments;

import pig.adapter.IDebugAdapterContext;

public interface ILaunchDelegate {
    void postLaunch(LaunchArguments launchArguments, IDebugAdapterContext context);

    void preLaunch(LaunchArguments launchArguments, IDebugAdapterContext context);

    void launch(LaunchArguments launchArguments, IDebugAdapterContext context) throws IOException;
}
