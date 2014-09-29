package com.dreweaster.octodubstep.core.source.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 */
public final class JavaIOExternalFile implements ExternalFile {

    private File file;

    public JavaIOExternalFile(File file) {
        this.file = file;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new FileInputStream(file);
    }

    @Override
    public String getPath() {
        return file.getAbsolutePath();
    }
}
