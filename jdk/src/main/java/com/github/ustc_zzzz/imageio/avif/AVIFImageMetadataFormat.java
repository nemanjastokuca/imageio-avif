package com.github.ustc_zzzz.imageio.avif;

import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadataFormatImpl;

public class AVIFImageMetadataFormat extends IIOMetadataFormatImpl {
    public AVIFImageMetadataFormat() {
        super(AVIFImageReaderSpi.IMAGE_METADATA_FORMAT_NAME, CHILD_POLICY_SOME);

        this.addElement("depth", AVIFImageReaderSpi.IMAGE_METADATA_FORMAT_NAME, CHILD_POLICY_EMPTY);
        this.addObjectValue("depth", int.class, true, 8);

        this.addElement("ptsInTimescales", AVIFImageReaderSpi.IMAGE_METADATA_FORMAT_NAME, CHILD_POLICY_EMPTY);
        this.addObjectValue("ptsInTimescales", long.class, true, 0L);

        this.addElement("durationInTimescales", AVIFImageReaderSpi.IMAGE_METADATA_FORMAT_NAME, CHILD_POLICY_EMPTY);
        this.addObjectValue("durationInTimescales", long.class, true, 0L);
    }

    @Override
    public boolean canNodeAppear(String elementName, ImageTypeSpecifier imageType) {
        return true;
    }
}
