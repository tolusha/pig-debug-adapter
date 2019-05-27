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

import org.apache.pig.PigServer;

public interface IDebugSession extends IDebugSessionObservable {
    void start();

    void terminate();

    void resume();

    PigServer getPigServer();

    void setBreakpoints(int[] lines);

    IFileContext getFileContext();

    int getCurrentLine();
}
