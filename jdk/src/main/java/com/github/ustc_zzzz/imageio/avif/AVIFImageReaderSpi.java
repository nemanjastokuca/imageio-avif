package com.github.ustc_zzzz.imageio.avif;

import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;
import java.io.IOException;
import java.util.Locale;

public final class AVIFImageReaderSpi extends ImageReaderSpi {
    public static final String NAME = "AVIF";
    public static final String SUFFIX = "avif";
    public static final String VERSION = "1.0";
    public static final String VENDOR = "Yanbing Zhao";
    public static final String MIME_TYPE = "image/avif";
    public static final String IMAGE_METADATA_FORMAT_NAME = "ustc_zzzz_imageio_avif_image_1.0";
    public static final String STREAM_METADATA_FORMAT_NAME = "ustc_zzzz_imageio_avif_stream_1.0";

    static final String READER_CLASS = "com.github.ustc_zzzz.imageio.avif.AVIFImageReader";
    static final String IMAGE_FMT_CLASS = "com.github.ustc_zzzz.imageio.avif.AVIFImageMetadataFormat";
    static final String STREAM_FMT_CLASS = "com.github.ustc_zzzz.imageio.avif.AVIFStreamMetadataFormat";
    static final String DESCRIPTION = "Native AVIF image reader, with JNI native mapping created by ustc_zzzz";

    public AVIFImageReaderSpi() {
        super(VENDOR, VERSION, new String[]{SUFFIX, NAME},
                new String[]{SUFFIX}, new String[]{MIME_TYPE},
                READER_CLASS, new Class<?>[]{ImageInputStream.class}, null,
                false, STREAM_METADATA_FORMAT_NAME, STREAM_FMT_CLASS, null, null,
                false, IMAGE_METADATA_FORMAT_NAME, IMAGE_FMT_CLASS, null, null);
    }

    @Override
    public String getDescription(Locale locale) {
        return DESCRIPTION;
    }

    @Override
    public boolean canDecodeInput(Object source) throws IOException {
        if (source instanceof ImageInputStream stream) {
            var head = new byte[16];
            stream.mark();
            stream.readFully(head);
            stream.reset();
            var fileTypeBox = head[4] == 'f' && head[5] == 't' && head[6] == 'y' && head[7] == 'p';
            if (fileTypeBox) {
                return head[8] == 'a' && head[9] == 'v' && head[10] == 'i' && (head[11] == 'f' || head[11] == 's');
            }
        }
        return false;
    }

    @Override
    public ImageReader createReaderInstance(Object extension) {
        return new AVIFImageReader(this);
    }
}
