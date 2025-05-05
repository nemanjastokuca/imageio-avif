package com.github.ustc_zzzz.imageio.avif;

import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadataFormat;
import javax.imageio.metadata.IIOMetadataFormatImpl;

public class AVIFImageMetadataFormat extends IIOMetadataFormatImpl {
    private AVIFImageMetadataFormat() {
        super(AVIFImageReaderSpi.IMAGE_METADATA_FORMAT_NAME, CHILD_POLICY_SOME);

        this.addElement("Depth", AVIFImageReaderSpi.IMAGE_METADATA_FORMAT_NAME, CHILD_POLICY_EMPTY);
        this.addObjectValue("Depth", int.class, true, 8);

        this.addElement("PTSInTimescales", AVIFImageReaderSpi.IMAGE_METADATA_FORMAT_NAME, CHILD_POLICY_EMPTY);
        this.addObjectValue("PTSInTimescales", long.class, true, 0L);

        this.addElement("DurationInTimescales", AVIFImageReaderSpi.IMAGE_METADATA_FORMAT_NAME, CHILD_POLICY_EMPTY);
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
        private static final AVIFImageMetadataFormat INSTANCE = new AVIFImageMetadataFormat();

        private Instance() {
            throw new UnsupportedOperationException("This class is non-instantiable");
        }
    }
}
