package com.github.ustc_zzzz.imageio.avif;

import org.w3c.dom.Node;

import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;

public class AVIFImageMetadata extends IIOMetadata {
    private final Native.Image nativeImage;

    AVIFImageMetadata(Native.Image nativeImage) {
        super(false, AVIFImageReaderSpi.IMAGE_METADATA_FORMAT_NAME,
                AVIFImageReaderSpi.IMAGE_FMT_CLASS,
                null, null);
        this.nativeImage = nativeImage;
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }

    @Override
    public Node getAsTree(String formatName) {
        // custom native format only
        if (!this.nativeMetadataFormatName.equals(formatName)) {
            throw new IllegalArgumentException("Unrecognized format: " + formatName);
        }
        // construct the node
        var root = new IIOMetadataNode(this.nativeMetadataFormatName);
        root.appendChild(this.createNode("Depth", this.nativeImage.depth()));
        root.appendChild(this.createNode("PTSInTimescales", this.nativeImage.ptsInTimescales()));
        root.appendChild(this.createNode("DurationInTimescales", this.nativeImage.durationInTimescales()));
        return root;
    }

    private IIOMetadataNode createNode(String name, Object value) {
        var node = new IIOMetadataNode(name);
        node.setUserObject(value);
        return node;
    }

    @Override
    public void mergeTree(String formatName, Node root) {
        throw new IllegalStateException("Readonly metadata");
    }

    @Override
    public void reset() {
        throw new IllegalStateException("Readonly metadata");
    }
}
