package com.github.ustc_zzzz.imageio.avif;

import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadataFormat;
import javax.imageio.metadata.IIOMetadataFormatImpl;

public class AVIFStreamMetadataFormat extends IIOMetadataFormatImpl {
    private AVIFStreamMetadataFormat() {
        super(AVIFImageReaderSpi.STREAM_METADATA_FORMAT_NAME, CHILD_POLICY_SOME);

        this.addElement("YUVFormat", AVIFImageReaderSpi.STREAM_METADATA_FORMAT_NAME, CHILD_POLICY_EMPTY);
        this.addObjectValue("YUVFormat", String.class, false, null);

        this.addElement("YUVRangeFull", AVIFImageReaderSpi.STREAM_METADATA_FORMAT_NAME, CHILD_POLICY_EMPTY);
        this.addObjectValue("YUVRangeFull", boolean.class, true, true);

        this.addElement("YUVChromaSamplePosition", AVIFImageReaderSpi.STREAM_METADATA_FORMAT_NAME, CHILD_POLICY_EMPTY);
        this.addObjectValue("YUVChromaSamplePosition", int.class, false, 0);

        this.addElement("ColorPrimaries", AVIFImageReaderSpi.STREAM_METADATA_FORMAT_NAME, CHILD_POLICY_EMPTY);
        this.addObjectValue("ColorPrimaries", int.class, true, 0);

        this.addElement("TransferCharacteristics", AVIFImageReaderSpi.STREAM_METADATA_FORMAT_NAME, CHILD_POLICY_EMPTY);
        this.addObjectValue("TransferCharacteristics", int.class, true, 0);

        this.addElement("MatrixCoefficients", AVIFImageReaderSpi.STREAM_METADATA_FORMAT_NAME, CHILD_POLICY_EMPTY);
        this.addObjectValue("MatrixCoefficients", int.class, true, 0);

        this.addElement("ICC", AVIFImageReaderSpi.STREAM_METADATA_FORMAT_NAME, CHILD_POLICY_EMPTY);
        this.addObjectValue("ICC", byte.class, 0, Integer.MAX_VALUE);

        this.addElement("EXIF", AVIFImageReaderSpi.STREAM_METADATA_FORMAT_NAME, CHILD_POLICY_EMPTY);
        this.addObjectValue("EXIF", byte.class, 0, Integer.MAX_VALUE);

        this.addElement("XMP", AVIFImageReaderSpi.STREAM_METADATA_FORMAT_NAME, CHILD_POLICY_EMPTY);
        this.addObjectValue("XMP", byte.class, 0, Integer.MAX_VALUE);

        this.addElement("AlphaPresent", AVIFImageReaderSpi.STREAM_METADATA_FORMAT_NAME, CHILD_POLICY_EMPTY);
        this.addObjectValue("AlphaPresent", boolean.class, true, false);

        this.addElement("AlphaPremultiplied", AVIFImageReaderSpi.STREAM_METADATA_FORMAT_NAME, CHILD_POLICY_EMPTY);
        this.addObjectValue("AlphaPremultiplied", boolean.class, false, false);

        this.addElement("TimeScale", AVIFImageReaderSpi.STREAM_METADATA_FORMAT_NAME, CHILD_POLICY_EMPTY);
        this.addObjectValue("TimeScale", long.class, true, 0L);

        this.addElement("DurationInTimescales", AVIFImageReaderSpi.STREAM_METADATA_FORMAT_NAME, CHILD_POLICY_EMPTY);
        this.addObjectValue("DurationInTimescales", long.class, true, 0L);
    }

    @Override
    public boolean canNodeAppear(String elementName, ImageTypeSpecifier imageType) {
        return true;
    }

    public static IIOMetadataFormat getInstance() {
        return Instance.INSTANCE;
    }

    private static final class Instance {
        private static final AVIFStreamMetadataFormat INSTANCE = new AVIFStreamMetadataFormat();

        private Instance() {
            throw new UnsupportedOperationException("This class is non-instantiable");
        }
    }
}
