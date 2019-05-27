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

import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

public class DebugFileContext implements IFileContext, Closeable {

    private String file;
    private URI uri;
    private List<String> lines;

    @Override
    public void setFile(String file) {
        this.file = file;
        try {
            this.uri = new URI(AdapterUtils.convertPath(this.file, AdapterUtils.isUri(this.file), true));
            this.lines = this.readLines();
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getFile() {
        return this.file;
    }

    @Override
    public String readLine(int lineNumber) throws IOException {
        if (lineNumber > this.lines.size()) {
            throw new EOFException("No such line " + lineNumber);
        }

        return lines.get(lineNumber - 1);
    }

    @Override
    public void close() throws IOException {
        this.lines.clear();
    }

    private List<String> readLines() throws IOException, URISyntaxException {
        Path filePath = Paths.get(this.uri);
        if (filePath == null) {
            throw new IOException(this.file + " can't be converted to URI");
        }
        return Lists.newCopyOnWriteArrayList(Files.readAllLines(filePath));
    }

    @Override
    public String getContent() {
        return Joiner.on("\n").join(lines);
    }

    @Override
    public String getName() {
        return Paths.get(this.uri).getFileName().toString();
    }

    @Override
    public String getUri() {
        return this.uri.getPath();
    }
}
