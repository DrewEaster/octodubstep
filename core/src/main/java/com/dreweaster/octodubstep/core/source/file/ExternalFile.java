package com.dreweaster.octodubstep.core.source.file;

import java.io.IOException;
import java.io.InputStream;

/**
 */
public interface ExternalFile {

    InputStream getInputStream() throws IOException;

    String getPath();
}
