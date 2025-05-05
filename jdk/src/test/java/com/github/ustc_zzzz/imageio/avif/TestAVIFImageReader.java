package com.github.ustc_zzzz.imageio.avif;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.annotation.Nonnull;
import javax.imageio.ImageIO;
import javax.imageio.stream.FileCacheImageInputStream;
import javax.imageio.stream.FileImageInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class TestAVIFImageReader {
    @TempDir
    Path temp;

    @Test
    void testAVIFException() {
        assertThrows(AVIFException.class, () -> Optional.empty().orElseThrow(() -> new AVIFException("IO error")));
    }

    @Test
    void testStillImage1() throws IOException {
        try (var in = this.getClass().getResourceAsStream("/kodim03_yuv420_8bpc.avif")) {
            assertNotNull(in);
            var stream = ImageIO.createImageInputStream(in);
            assertInstanceOf(FileCacheImageInputStream.class, stream);
            var reader = ImageIO.getImageReaders(stream).next();
            assertInstanceOf(AVIFImageReader.class, reader);
            reader.setInput(stream, false);
            assertEquals(1, reader.getNumImages(true));
            var nativeDecoded = ((AVIFImageReader) reader).getNativeDecoded();
            assertEquals(768, nativeDecoded.width());
            assertEquals(512, nativeDecoded.height());
            assertEquals(8, nativeDecoded.depth());
            assertEquals("YUV420", nativeDecoded.yuvFormat());
            assertFalse(nativeDecoded.alphaPresent());
            assertTrue(nativeDecoded.yuvRangeFull());
            assertEquals(0, nativeDecoded.yuvChromaSamplePosition());
            assertEquals(1, nativeDecoded.colorPrimaries());
            assertEquals(13, nativeDecoded.transferCharacteristics());
            assertEquals(6, nativeDecoded.matrixCoefficients());
            assertArrayEquals(new byte[0], nativeDecoded.icc());
            assertArrayEquals(new byte[0], nativeDecoded.exif());
            assertArrayEquals(new byte[0], nativeDecoded.xmp());
        }
    }

    @Test
    void testStillImage2() throws IOException {
        try (var in = this.getClass().getResourceAsStream("/kodim23_yuv420_8bpc.avif")) {
            assertNotNull(in);
            Files.copy(in, this.temp.resolve("kodim23_yuv420_8bpc.avif"));
        }
        var stream = ImageIO.createImageInputStream(this.temp.resolve("kodim23_yuv420_8bpc.avif").toFile());
        assertInstanceOf(FileImageInputStream.class, stream);
        var reader = ImageIO.getImageReaders(stream).next();
        assertInstanceOf(AVIFImageReader.class, reader);
        reader.setInput(stream, false);
        assertEquals(1, reader.getNumImages(true));
        var nativeDecoded = ((AVIFImageReader) reader).getNativeDecoded();
        assertEquals(768, nativeDecoded.width());
        assertEquals(512, nativeDecoded.height());
        assertEquals(8, nativeDecoded.depth());
        assertEquals("YUV420", nativeDecoded.yuvFormat());
        assertFalse(nativeDecoded.alphaPresent());
        assertTrue(nativeDecoded.yuvRangeFull());
        assertEquals(0, nativeDecoded.yuvChromaSamplePosition());
        assertEquals(1, nativeDecoded.colorPrimaries());
        assertEquals(13, nativeDecoded.transferCharacteristics());
        assertEquals(6, nativeDecoded.matrixCoefficients());
        assertArrayEquals(new byte[0], nativeDecoded.icc());
        assertArrayEquals(new byte[0], nativeDecoded.exif());
        assertArrayEquals(new byte[0], nativeDecoded.xmp());
    }

    @Test
    void testStillImage3() throws IOException {
        try (var in = this.getClass().getResourceAsStream("/cosmos1650_yuv444_10bpc_p3pq.avif")) {
            assertNotNull(in);
            var stream = ImageIO.createImageInputStream(in);
            assertInstanceOf(FileCacheImageInputStream.class, stream);
            var reader = ImageIO.getImageReaders(stream).next();
            assertInstanceOf(AVIFImageReader.class, reader);
            reader.setInput(stream, false);
            assertEquals(1, reader.getNumImages(true));
            var nativeDecoded = ((AVIFImageReader) reader).getNativeDecoded();
            assertEquals(1024, nativeDecoded.width());
            assertEquals(428, nativeDecoded.height());
            assertEquals(10, nativeDecoded.depth());
            assertEquals("YUV444", nativeDecoded.yuvFormat());
            assertFalse(nativeDecoded.alphaPresent());
            assertTrue(nativeDecoded.yuvRangeFull());
            assertEquals(0, nativeDecoded.yuvChromaSamplePosition());
            assertEquals(12, nativeDecoded.colorPrimaries());
            assertEquals(16, nativeDecoded.transferCharacteristics());
            assertEquals(12, nativeDecoded.matrixCoefficients());
            assertArrayEquals(new byte[0], nativeDecoded.icc());
            assertArrayEquals(new byte[0], nativeDecoded.exif());
            assertArrayEquals(new byte[0], nativeDecoded.xmp());
        }
    }

    @Test
    void testStillImage4() throws IOException {
        try (var in = this.getClass().getResourceAsStream("/paris_icc_exif_xmp.avif")) {
            assertNotNull(in);
            var stream = ImageIO.createImageInputStream(in);
            assertInstanceOf(FileCacheImageInputStream.class, stream);
            var reader = ImageIO.getImageReaders(stream).next();
            assertInstanceOf(AVIFImageReader.class, reader);
            reader.setInput(stream, false);
            assertEquals(1, reader.getNumImages(true));
            var nativeDecoded = ((AVIFImageReader) reader).getNativeDecoded();
            assertEquals(403, nativeDecoded.width());
            assertEquals(302, nativeDecoded.height());
            assertEquals(8, nativeDecoded.depth());
            assertEquals("YUV444", nativeDecoded.yuvFormat());
            assertFalse(nativeDecoded.alphaPresent());
            assertTrue(nativeDecoded.yuvRangeFull());
            assertEquals(0, nativeDecoded.yuvChromaSamplePosition());
            assertEquals(2, nativeDecoded.colorPrimaries());
            assertEquals(2, nativeDecoded.transferCharacteristics());
            assertEquals(6, nativeDecoded.matrixCoefficients());
            assertEquals(596, nativeDecoded.icc().length);
            assertEquals(1126, nativeDecoded.exif().length);
            assertEquals(3898, nativeDecoded.xmp().length);
        }
    }

    @Test
    void testAnimatedImage() throws IOException {
        try (var in = this.getClass().getResourceAsStream("/horse_in_motion_yuv420_8bpc.avif")) {
            assertNotNull(in);
            var stream = ImageIO.createImageInputStream(in);
            assertInstanceOf(FileCacheImageInputStream.class, stream);
            var reader = ImageIO.getImageReaders(stream).next();
            assertInstanceOf(AVIFImageReader.class, reader);
            reader.setInput(stream, false);
            assertEquals(11, reader.getNumImages(true));
            var nativeDecoded = ((AVIFImageReader) reader).getNativeDecoded();
            assertEquals(1370, nativeDecoded.width());
            assertEquals(885, nativeDecoded.height());
            assertEquals(8, nativeDecoded.depth());
            assertEquals("YUV420", nativeDecoded.yuvFormat());
            assertFalse(nativeDecoded.alphaPresent());
            assertTrue(nativeDecoded.yuvRangeFull());
            assertEquals(0, nativeDecoded.yuvChromaSamplePosition());
            assertEquals(1, nativeDecoded.colorPrimaries());
            assertEquals(13, nativeDecoded.transferCharacteristics());
            assertEquals(6, nativeDecoded.matrixCoefficients());
            assertArrayEquals(new byte[0], nativeDecoded.icc());
            assertArrayEquals(new byte[0], nativeDecoded.exif());
            assertArrayEquals(new byte[0], nativeDecoded.xmp());
            assertEquals(15L, nativeDecoded.timeScale());
            assertEquals(11L, nativeDecoded.durationInTimescales());
            assertAll(IntStream.range(0, 11).mapToObj(i -> () -> {
                assertDoesNotThrow(() -> reader.read(i));
                var nativeImages = ((AVIFImageReader) reader).getNativeImages();
                assertEquals(i + 1, nativeImages.size());
                var nativeImage = nativeImages.get(i);
                assertEquals(1370, nativeImage.width());
                assertEquals(885, nativeImage.height());
                assertEquals(8, nativeImage.depth());
                assertEquals(i, nativeImage.ptsInTimescales());
                assertEquals(1L, nativeImage.durationInTimescales());
            }));
        }
    }

    @Test
    void testMetadataFormat() {
        var reader = ImageIO.getImageReadersBySuffix("avif").next();
        assertInstanceOf(AVIFImageReader.class, reader);
        var spi = reader.getOriginatingProvider();
        var streamFormat = spi.getStreamMetadataFormat("ustc_zzzz_imageio_avif_stream_1.0");
        var imageFormat = spi.getImageMetadataFormat("ustc_zzzz_imageio_avif_image_1.0");
        assertEquals(AVIFStreamMetadataFormat.getInstance(), streamFormat);
        assertEquals(AVIFImageMetadataFormat.getInstance(), imageFormat);
    }

    @Test
    void testExceptionFromNative1() throws IOException {
        var header = "\0\0\0 ftypavif\0\0\0\0".getBytes();
        var stream = ImageIO.createImageInputStream(new ByteArrayInputStream(header));
        assertInstanceOf(FileCacheImageInputStream.class, stream);
        var reader = ImageIO.getImageReaders(stream).next();
        assertInstanceOf(AVIFImageReader.class, reader);
        reader.setInput(stream, false);
        assertThrows(AVIFException.class, () -> reader.getNumImages(true));
    }

    @Test
    void testExceptionFromNative2() throws IOException {
        var header = "\0\0\0 ftypavif\0\0\0\0avifmif1miafMA1B".getBytes();
        var stream = ImageIO.createImageInputStream(new HeaderOnlyInputStream(header));
        assertInstanceOf(FileCacheImageInputStream.class, stream);
        var reader = ImageIO.getImageReaders(stream).next();
        assertInstanceOf(AVIFImageReader.class, reader);
        reader.setInput(stream, false);
        assertThrows(IOException.class, () -> reader.getNumImages(true));
    }

    static final class HeaderOnlyInputStream extends InputStream {
        private final byte[] header;

        private HeaderOnlyInputStream(byte[] header) {
            this.header = header;
        }

        private int pointer;

        @Override
        public int read() throws IOException {
            if (this.pointer >= this.header.length) {
                throw new IOException("Read limit of 32 bytes exceeded");
            }
            return this.header[this.pointer++] & 0xff;
        }

        @Override
        public int read(@Nonnull byte[] b, int off, int len) throws IOException {
            if (this.pointer + len > this.header.length) {
                throw new IOException("Read limit of 32 bytes exceeded");
            }
            System.arraycopy(this.header, this.pointer, b, off, len);
            this.pointer += len;
            return len;
        }

        @Override
        public void close() {
            this.pointer = this.header.length;
        }
    }
}
