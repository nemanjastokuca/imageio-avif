package com.github.ustc_zzzz.imageio.avif;

import javax.imageio.stream.ImageInputStream;
import java.awt.image.DataBuffer;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Locale;

final class Native {
    private static final String[] CLASS_NAME = {"com", "github", "ustc_zzzz", "imageio", "avif", "Native"};

    static {
        // check if the class has been relocated
        var className = Native.class.getName();
        if (!Arrays.equals(CLASS_NAME, className.split("\\."))) {
            var msg = "Relocated class (" + String.join(".", CLASS_NAME) + " => " + className + ") not allowed";
            throw new IllegalStateException(msg);
        }
        // get resource path
        var resourcePath = (String) null;
        var libraryName = System.mapLibraryName("avif-imageio-jni");
        var arch = System.getProperty("os.arch").toLowerCase(Locale.ROOT);
        if (arch.contains("amd64") || arch.contains("x86_64")) {
            resourcePath = "/META-INF/native/amd64/" + libraryName;
        }
        if (arch.contains("aarch64") || arch.contains("arm64")) {
            resourcePath = "/META-INF/native/arm64/" + libraryName;
        }
        // copy the compressed the resource to file and load
        try (var input = resourcePath == null ? null : Native.class.getResourceAsStream(resourcePath)) {
            if (input == null) {
                var msg = "Unsupported arch: " + arch + " for current os (" + libraryName + " not found)";
                throw new IllegalStateException(msg);
            }
            // copy
            var dot = libraryName.lastIndexOf('.');
            var suffix = dot >= 0 ? libraryName.substring(dot) : "";
            var prefix = dot >= 0 ? libraryName.substring(0, dot + 1) : libraryName + ".";
            var tmpFile = Files.createTempFile(prefix, suffix);
            Files.copy(input, tmpFile, StandardCopyOption.REPLACE_EXISTING);
            tmpFile.toFile().deleteOnExit();
            // load
            System.load(tmpFile.toAbsolutePath().toString());
        } catch (IOException e) {
            var msg = "Failed to load " + libraryName + " as native library for " + arch;
            throw new IllegalStateException(msg);
        }
    }

    static native Image next(long decoderPtr) throws IOException;

    static native void destroy(long decoderPtr, long memoryPtr);

    static native Decoded create(ImageInputStream imageInput) throws IOException;

    record Image(int width, int height, int depth,
                 long ptsInTimescales, long durationInTimescales,
                 // BGRA if hasAlpha otherwise BGR
                 boolean hasAlpha, DataBuffer pixels, int imageIndex) {
        // nothing here
    }

    record Decoded(int width, int height, int depth,
                   String yuvFormat, boolean yuvRangeFull, int yuvChromaSamplePosition,
                   int colorPrimaries, int transferCharacteristics, int matrixCoefficients,
                   byte[] icc, byte[] exif, byte[] xmp, boolean alphaPresent, boolean alphaPremultiplied,
                   long timeScale, long durationInTimescales, int imageCount, long decoderPtr, long memoryPtr) {
        Runnable cleaner() {
            var memoryPtr = this.memoryPtr;
            var decoderPtr = this.decoderPtr;
            return () -> destroy(decoderPtr, memoryPtr);
        }
    }

    private Native() {
        throw new UnsupportedOperationException("This class is non-instantiable");
    }
}
