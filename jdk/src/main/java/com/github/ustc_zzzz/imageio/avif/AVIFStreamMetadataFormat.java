package com.github.ustc_zzzz.imageio.avif;

import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadataFormatImpl;

public class AVIFStreamMetadataFormat extends IIOMetadataFormatImpl {
    public AVIFStreamMetadataFormat() {
        super(AVIFImageReaderSpi.STREAM_METADATA_FORMAT_NAME, CHILD_POLICY_SOME);

        this.addElement("yuvFormat", AVIFImageReaderSpi.STREAM_METADATA_FORMAT_NAME, CHILD_POLICY_EMPTY);
        this.addObjectValue("yuvFormat", String.class, false, null);

        this.addElement("yuvRangeFull", AVIFImageReaderSpi.STREAM_METADATA_FORMAT_NAME, CHILD_POLICY_EMPTY);
        this.addObjectValue("yuvRangeFull", boolean.class, true, true);

        this.addElement("yuvChromaSamplePosition", AVIFImageReaderSpi.STREAM_METADATA_FORMAT_NAME, CHILD_POLICY_EMPTY);
        this.addObjectValue("yuvChromaSamplePosition", int.class, false, 0);

        this.addElement("colorPrimaries", AVIFImageReaderSpi.STREAM_METADATA_FORMAT_NAME, CHILD_POLICY_EMPTY);
        this.addObjectValue("colorPrimaries", int.class, true, 0);

        this.addElement("transferCharacteristics", AVIFImageReaderSpi.STREAM_METADATA_FORMAT_NAME, CHILD_POLICY_EMPTY);
        this.addObjectValue("transferCharacteristics", int.class, true, 0);

        this.addElement("matrixCoefficients", AVIFImageReaderSpi.STREAM_METADATA_FORMAT_NAME, CHILD_POLICY_EMPTY);
        this.addObjectValue("matrixCoefficients", int.class, true, 0);

        this.addElement("icc", AVIFImageReaderSpi.STREAM_METADATA_FORMAT_NAME, CHILD_POLICY_EMPTY);
        this.addObjectValue("icc", byte.class, 0, Integer.MAX_VALUE);

        this.addElement("exif", AVIFImageReaderSpi.STREAM_METADATA_FORMAT_NAME, CHILD_POLICY_EMPTY);
        this.addObjectValue("exif", byte.class, 0, Integer.MAX_VALUE);

        this.addElement("xmp", AVIFImageReaderSpi.STREAM_METADATA_FORMAT_NAME, CHILD_POLICY_EMPTY);
        this.addObjectValue("xmp", byte.class, 0, Integer.MAX_VALUE);

        this.addElement("alphaPresent", AVIFImageReaderSpi.STREAM_METADATA_FORMAT_NAME, CHILD_POLICY_EMPTY);
        this.addObjectValue("alphaPresent", boolean.class, true, false);

        this.addElement("alphaPremultiplied", AVIFImageReaderSpi.STREAM_METADATA_FORMAT_NAME, CHILD_POLICY_EMPTY);
        this.addObjectValue("alphaPremultiplied", boolean.class, false, false);

        this.addElement("timeScale", AVIFImageReaderSpi.STREAM_METADATA_FORMAT_NAME, CHILD_POLICY_EMPTY);
        this.addObjectValue("timeScale", long.class, true, 0L);

        this.addElement("durationInTimescales", AVIFImageReaderSpi.STREAM_METADATA_FORMAT_NAME, CHILD_POLICY_EMPTY);
        this.addObjectValue("durationInTimescales", long.class, true, 0L);
    }

    @Override
    public boolean canNodeAppear(String elementName, ImageTypeSpecifier imageType) {
        return true;
    }
}
