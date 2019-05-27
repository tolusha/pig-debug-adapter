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

import com.microsoft.java.debug.core.protocol.IProtocolServer;
import com.microsoft.java.debug.core.protocol.Requests.StepFilters;

public class DebugAdapterContext implements IDebugAdapterContext {
    private IProtocolServer server;

    private IDebugSession session;
    private boolean debuggerLinesStartAt1 = true;
    private boolean debuggerPathsAreUri = true;
    private boolean clientLinesStartAt1 = true;
    private boolean clientColumnsStartAt1 = true;
    private boolean clientPathsAreUri = false;
    private boolean supportsRunInTerminalRequest;
    private boolean isAttached = false;
    private Charset debuggeeEncoding;
    private transient boolean pigServerTerminated;
    private LaunchMode launchMode = LaunchMode.DEBUG;
    private StepFilters stepFilters;

    public DebugAdapterContext(IProtocolServer server) {
        this.server = server;
    }

    @Override
    public IProtocolServer getProtocolServer() {
        return server;
    }

    @Override
    public boolean isDebuggerLinesStartAt1() {
        return debuggerLinesStartAt1;
    }

    @Override
    public void setDebuggerLinesStartAt1(boolean debuggerLinesStartAt1) {
        this.debuggerLinesStartAt1 = debuggerLinesStartAt1;
    }

    @Override
    public boolean isDebuggerPathsAreUri() {
        return debuggerPathsAreUri;
    }

    @Override
    public void setDebuggerPathsAreUri(boolean debuggerPathsAreUri) {
        this.debuggerPathsAreUri = debuggerPathsAreUri;
    }

    @Override
    public boolean isClientLinesStartAt1() {
        return clientLinesStartAt1;
    }

    @Override
    public void setClientLinesStartAt1(boolean clientLinesStartAt1) {
        this.clientLinesStartAt1 = clientLinesStartAt1;
    }

    public boolean isClientColumnsStartAt1() {
        return clientColumnsStartAt1;
    }

    public void setClientColumnsStartAt1(boolean clientColumnsStartAt1) {
        this.clientColumnsStartAt1 = clientColumnsStartAt1;
    }

    @Override
    public boolean isClientPathsAreUri() {
        return clientPathsAreUri;
    }

    @Override
    public void setClientPathsAreUri(boolean clientPathsAreUri) {
        this.clientPathsAreUri = clientPathsAreUri;
    }

    @Override
    public void setSupportsRunInTerminalRequest(boolean supportsRunInTerminalRequest) {
        this.supportsRunInTerminalRequest = supportsRunInTerminalRequest;
    }

    @Override
    public boolean supportsRunInTerminalRequest() {
        return supportsRunInTerminalRequest;
    }

    @Override
    public boolean isAttached() {
        return isAttached;
    }

    @Override
    public void setAttached(boolean attached) {
        isAttached = attached;
    }

    @Override
    public void setDebuggeeEncoding(Charset encoding) {
        debuggeeEncoding = encoding;
    }

    @Override
    public Charset getDebuggeeEncoding() {
        return debuggeeEncoding;
    }

    @Override
    public void setPigServerTerminated() {
        pigServerTerminated = true;
    }

    @Override
    public boolean isPigServerTerminated() {
        return pigServerTerminated;
    }

    @Override
    public void setStepFilters(StepFilters stepFilters) {
        this.stepFilters = stepFilters;
    }

    @Override
    public StepFilters getStepFilters() {
        return stepFilters;
    }

    @Override
    public LaunchMode getLaunchMode() {
        return launchMode;
    }

    @Override
    public void setLaunchMode(LaunchMode launchMode) {
        this.launchMode = launchMode;
    }

    @Override
    public void setDebugSession(IDebugSession session) {
        this.session = session;
    }

    @Override
    public IDebugSession getDebugSession() {
        return this.session;
    }
}
