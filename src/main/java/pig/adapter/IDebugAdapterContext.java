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

import java.nio.charset.Charset;
import java.util.Set;

import com.microsoft.java.debug.core.protocol.IProtocolServer;
import com.microsoft.java.debug.core.protocol.Requests.StepFilters;

import org.apache.pig.PigServer;

public interface IDebugAdapterContext {
    IProtocolServer getProtocolServer();

    void setDebugSession(IDebugSession session);

    IDebugSession getDebugSession();

    boolean isDebuggerLinesStartAt1();

    void setDebuggerLinesStartAt1(boolean debuggerLinesStartAt1);

    boolean isDebuggerPathsAreUri();

    void setDebuggerPathsAreUri(boolean debuggerPathsAreUri);

    boolean isClientLinesStartAt1();

    void setClientLinesStartAt1(boolean clientLinesStartAt1);

    boolean isClientColumnsStartAt1();

    void setClientColumnsStartAt1(boolean clientColumnsStartAt1);

    boolean isClientPathsAreUri();

    void setClientPathsAreUri(boolean clientPathsAreUri);

    void setSupportsRunInTerminalRequest(boolean supportsRunInTerminalRequest);

    boolean supportsRunInTerminalRequest();

    boolean isAttached();

    void setAttached(boolean attached);

    void setDebuggeeEncoding(Charset encoding);

    Charset getDebuggeeEncoding();

    void setPigServerTerminated();

    boolean isPigServerTerminated();

    void setStepFilters(StepFilters stepFilters);

    StepFilters getStepFilters();

    LaunchMode getLaunchMode();

    void setLaunchMode(LaunchMode launchMode);
}
