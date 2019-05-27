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

import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

import com.microsoft.java.debug.core.protocol.Events;
import com.microsoft.java.debug.core.protocol.Events.DebugEvent;
import com.microsoft.java.debug.core.protocol.Events.OutputEvent.Category;

import org.apache.log4j.Logger;
import org.apache.pig.ExecType;
import org.apache.pig.PigServer;
import org.apache.pig.backend.executionengine.ExecException;

public class DebugSession implements IDebugSession {
    private static final Logger logger = Logger.getLogger(DebugSession.class.getName());

    private List<IDebugSessionObserver> observers;
    private PigServer server;
    private boolean stopOnEntry;
    private List<Integer> breakpoints = new CopyOnWriteArrayList<>();
    private AtomicInteger currentLine = new AtomicInteger(1);
    private DataProcessing dataProcessing;
    private IFileContext fileContext;

    public DebugSession(boolean stopOnEntry, String program) throws ExecException {
        this.fileContext = new DebugFileContext();
        this.fileContext.setFile(program);

        this.dataProcessing = new DataProcessing();
        this.server = new PigServer(ExecType.LOCAL);
        this.stopOnEntry = stopOnEntry;
        this.observers = new ArrayList<>();
    }

    @Override
    public void start() {
        this.dataProcessing.acquireLock();
        this.dataProcessing.start();

        if (this.stopOnEntry) {
            this.notify(new Events.StoppedEvent("pause", 1, true));
        } else {
            this.dataProcessing.releaseLock();
        }
    }

    @Override
    public void terminate() {
        this.dataProcessing.interrupt();
        try {
            this.dataProcessing.join();
        } catch (InterruptedException e) {
            logger.error(e.getMessage());
        }
        this.server.shutdown();
        this.observers.clear();
    }

    @Override
    public PigServer getPigServer() {
        return this.server;
    }

    @Override
    public void addObserver(IDebugSessionObserver observer) {
        this.observers.add(observer);
    }

    @Override
    public void removeObserver(IDebugSessionObserver observer) {
        this.observers.remove(observer);
    }

    private void notify(DebugEvent event) {
        observers.forEach(observer -> observer.onEvent(event));
    }

    @Override
    public void setBreakpoints(int[] lines) {
        this.breakpoints.clear();
        for (int i = 0; i < lines.length; i++) {
            this.breakpoints.add(lines[i]);
        }
    }

    @Override
    public void resume() {
        this.dataProcessing.releaseLock();
    }

    private class DataProcessing extends Thread {
        private Semaphore semaphore = new Semaphore(1, true);

        @Override
        public void run() {
            for (;;) {
                try {
                    semaphore.acquire();
                } catch (InterruptedException e) {
                    break;
                }

                String query;
                try {
                    query = fileContext.readLine(currentLine.get());
                } catch (IOException e) {
                    DebugSession.this.notify(new Events.TerminatedEvent());
                    semaphore.release();
                    break;
                }

                if (AdapterUtils.isQuery(query)) {
                    try {
                        server.registerQuery(query);
                    } catch (IOException e) {
                        DebugSession.this.notify(new Events.OutputEvent(Category.stderr, e.getMessage()));
                    }
                }

                currentLine.incrementAndGet();

                if (breakpoints.contains(currentLine.get())) {
                    DebugSession.this.notify(new Events.StoppedEvent("breakpoint", 1, true));
                } else {
                    semaphore.release();
                }
            }
        }

        public void acquireLock() {
            try {
                this.semaphore.acquire();
            } catch (InterruptedException e) {
                logger.error(e.getMessage());
            }
        }

        public void releaseLock() {
            this.semaphore.release();
        }
    }

    @Override
    public IFileContext getFileContext() {
        return this.fileContext;
    }

    @Override
    public int getCurrentLine() {
        return this.currentLine.get();
    }
}
