package com.transifex.common;

import java.io.File;

import androidx.annotation.NonNull;

/**
 * A helper method that contains the boilerplate code for making sure that a temporary
 * directory does not exist when performing tests. By default the directory is
 * <code>"build/unitTestTempDir"</code>, but can optionally specify a different directory.
 * <p>
 *  The directory is deleted, if it exists, during setup. It is also deleted during tear down.
 */
public class TempDirHelper {

    private final File tempDir;

    public TempDirHelper() {
        tempDir = new File("build" + File.separator + "unitTestTempDir");
    }

    public TempDirHelper(@NonNull File dir) {
        tempDir = dir;
    }

    public void setUp() {
        if (tempDir.exists()) {
            Utils.deleteDirectory(tempDir);
        }
    }

    public void tearDown() {
        if (tempDir.exists()) {
            boolean deleted = Utils.deleteDirectory(tempDir);
            if (!deleted) {
                System.out.println("Could not delete tmp dir after test. Next test may fail.");
            }
        }
    }

    public File getFile() {
        return tempDir;
    }
}
