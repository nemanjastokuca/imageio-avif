package com.github.ustc_zzzz.imageio.avif;

import org.w3c.dom.Node;

import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;

public class AVIFStreamMetadata extends IIOMetadata {
    private final Native.Decoded decoded;

    AVIFStreamMetadata(Native.Decoded decoded) {
        super(false, AVIFImageReaderSpi.STREAM_METADATA_FORMAT_NAME, AVIFImageReaderSpi.STREAM_FMT_CLASS, null, null);
        this.decoded = decoded;
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
        root.appendChild(this.createNode("depth", this.decoded.depth()));
        if (this.decoded.yuvFormat() != null) {
            root.appendChild(this.createNode("yuvFormat", this.decoded.yuvFormat()));
        }
        root.appendChild(this.createNode("yuvRangeFull", this.decoded.yuvRangeFull()));
        if ("YUV420".equals(this.decoded.yuvFormat())) {
            root.appendChild(this.createNode("yuvChromaSamplePosition", this.decoded.yuvChromaSamplePosition()));
        }
        root.appendChild(this.createNode("colorPrimaries", this.decoded.colorPrimaries()));
        root.appendChild(this.createNode("transferCharacteristics", this.decoded.transferCharacteristics()));
        root.appendChild(this.createNode("matrixCoefficients", this.decoded.matrixCoefficients()));
        root.appendChild(this.createNode("icc", this.decoded.icc()));
        root.appendChild(this.createNode("exif", this.decoded.exif()));
        root.appendChild(this.createNode("xmp", this.decoded.xmp()));
        root.appendChild(this.createNode("alphaPresent", this.decoded.alphaPresent()));
        if (this.decoded.alphaPresent()) {
            root.appendChild(this.createNode("alphaPremultiplied", this.decoded.alphaPremultiplied()));
        }
        root.appendChild(this.createNode("timeScale", this.decoded.timeScale()));
        root.appendChild(this.createNode("durationInTimescales", this.decoded.durationInTimescales()));
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
