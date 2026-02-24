package io.slatedb;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Loads the slatedb_c native library, extracting it from the JAR if bundled,
 * or falling back to {@code System.loadLibrary} for development workflows.
 */
public final class NativeLoader {
    private static volatile boolean loaded;

    private NativeLoader() {
    }

    public static synchronized void load() {
        if (loaded) {
            return;
        }

        String resource = nativeResourcePath();
        String libFileName = resource.substring(resource.lastIndexOf('/') + 1);
        InputStream in = NativeLoader.class.getResourceAsStream(resource);
        if (in != null) {
            loadFromResource(in, libFileName);
        } else {
            // Dev/test fallback: rely on -Djava.library.path
            System.loadLibrary("slatedb_c");
        }

        loaded = true;
    }

    private static String nativeResourcePath() {
        String os = System.getProperty("os.name", "").toLowerCase();
        String arch = System.getProperty("os.arch", "").toLowerCase();

        String osName;
        String libName;
        if (os.contains("linux")) {
            osName = "linux";
            libName = "libslatedb_c.so";
        } else if (os.contains("mac") || os.contains("darwin")) {
            osName = "osx";
            libName = "libslatedb_c.dylib";
        } else {
            osName = os.replaceAll("\\s+", "");
            libName = "libslatedb_c.so";
        }

        String archName;
        if (arch.equals("amd64") || arch.equals("x86_64")) {
            archName = "x86_64";
        } else if (arch.equals("aarch64") || arch.equals("arm64")) {
            archName = "aarch64";
        } else {
            archName = arch;
        }

        return "/META-INF/native/" + osName + "-" + archName + "/" + libName;
    }

    private static void loadFromResource(InputStream in, String libFileName) {
        try {
            Path tmpDir = Files.createTempDirectory("slatedb-native");
            tmpDir.toFile().deleteOnExit();

            Path libFile = tmpDir.resolve(libFileName);
            Files.copy(in, libFile, StandardCopyOption.REPLACE_EXISTING);
            libFile.toFile().deleteOnExit();

            System.load(libFile.toAbsolutePath().toString());
        } catch (IOException e) {
            throw new UnsatisfiedLinkError("Failed to extract native library: " + e.getMessage());
        } finally {
            try {
                in.close();
            } catch (IOException ignored) {
            }
        }
    }
}
