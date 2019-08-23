package org.jboss.as.test.integration.ejb.access.log;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;

/**
 * Incrementally returns added lines
 */
class ServerLog {
    private RandomAccessFile pointer;

    public ServerLog(Path file, long offset) throws IOException {
        pointer = new RandomAccessFile(file.toFile(), "r");
        pointer.seek(offset);
    }

    /**
     * Incrementally returns added lines
     * @return lines added since last read
     * @throws IOException
     */
    public String readNext() throws IOException {
        System.out.println("offset BEFORE " + pointer.getFilePointer());
        StringBuilder content = new StringBuilder();
        String line;
        while ((line = pointer.readLine()) != null) {
            content.append(line);
            content.append("\n");
        }
        System.out.println("offset AFTER " + pointer.getFilePointer());
        return content.toString();
    }

    public long getOffset() throws IOException {
        return pointer.getFilePointer();
    }
}
