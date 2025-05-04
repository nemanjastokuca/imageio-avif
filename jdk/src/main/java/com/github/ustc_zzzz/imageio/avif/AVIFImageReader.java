package com.github.ustc_zzzz.imageio.avif;

import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ComponentColorModel;
import java.awt.image.Raster;
import java.io.IOException;
import java.lang.ref.Cleaner;
import java.util.*;
import java.util.List;
import java.util.stream.Stream;

public class AVIFImageReader extends ImageReader {
    private static final Cleaner cleaner = Cleaner.create();

    private ImageInputStream stream;
    private Native.Decoded nativeDecoded;
    private IOException lastException;
    private List<Native.Image> nativeImages;

    AVIFImageReader(ImageReaderSpi spi) {
        super(spi);
    }

    Native.Decoded getNativeDecoded() {
        return this.nativeDecoded;
    }

    List<Native.Image> getNativeImages() {
        return this.nativeImages;
    }

    private void ensureInitialized() throws IOException {
        var imageInput = this.stream;
        if (imageInput == null) {
            throw new IllegalStateException("No stream available!");
        }
        if (this.nativeDecoded == null) {
            if (this.lastException != null) {
                throw new IOException("Error on the previous initialization", this.lastException);
            }
            try {
                if (imageInput.getStreamPosition() != 0L) {
                    if (this.isSeekForwardOnly()) {
                        throw new IOException("seek forward only");
                    }
                    imageInput.seek(0L);
                }
                var decoded = Native.create(imageInput);
                cleaner.register(this, decoded.cleaner());
                this.nativeDecoded = decoded;
                this.nativeImages = new ArrayList<>(decoded.imageCount());
            } catch (IOException e) {
                this.lastException = e;
                throw e;
            }
        }
    }

    private void ensureImage(int imageIndex) throws IOException {
        this.ensureInitialized();
        var readCount = this.nativeImages.size();
        if (imageIndex >= readCount) {
            if (this.lastException != null) {
                throw new IOException("Error on parsing image " + readCount, this.lastException);
            }
            try {
                while (imageIndex >= readCount) {
                    this.nativeImages.add(Native.next(this.nativeDecoded.decoderPtr()));
                    readCount = this.nativeImages.size();
                }
            } catch (IOException e) {
                this.lastException = e;
                throw e;
            }
        }
    }

    @Override
    public void setInput(Object input, boolean seekForwardOnly, boolean ignoreMetadata) {
        super.setInput(input, seekForwardOnly, ignoreMetadata);
        if (input == null) {
            this.stream = null;
            return;
        }
        if (input instanceof ImageInputStream inputStream) {
            this.stream = inputStream;
            return;
        }
        throw new IllegalArgumentException("input not an ImageInputStream!");
    }

    @Override
    public int getNumImages(boolean allowSearch) throws IOException {
        this.ensureInitialized();
        return this.nativeDecoded.imageCount();
    }

    @Override
    public int getWidth(int imageIndex) throws IOException {
        this.ensureImage(imageIndex);
        return this.nativeImages.get(imageIndex).width();
    }

    @Override
    public int getHeight(int imageIndex) throws IOException {
        this.ensureImage(imageIndex);
        return this.nativeImages.get(imageIndex).height();
    }

    @Override
    public Iterator<ImageTypeSpecifier> getImageTypes(int imageIndex) throws IOException {
        this.ensureImage(imageIndex);
        var type = BufferedImage.TYPE_CUSTOM;
        var nativeImage = this.nativeImages.get(imageIndex);
        if (nativeImage.depth() == 8) {
            type = nativeImage.hasAlpha() ? BufferedImage.TYPE_4BYTE_ABGR : BufferedImage.TYPE_3BYTE_BGR;
        }
        return Stream.of(ImageTypeSpecifier.createFromBufferedImageType(type)).iterator();
    }

    @Override
    public IIOMetadata getStreamMetadata() throws IOException {
        this.ensureInitialized();
        return new AVIFStreamMetadata(this.nativeDecoded);
    }

    @Override
    public IIOMetadata getImageMetadata(int imageIndex) throws IOException {
        this.ensureImage(imageIndex);
        return new AVIFImageMetadata(this.nativeImages.get(imageIndex));
    }

    @Override
    public BufferedImage read(int imageIndex, ImageReadParam param) throws IOException {
        this.ensureImage(imageIndex);
        // get image
        var image = this.nativeImages.get(imageIndex);
        // color profile
        var srgb = ColorSpace.getInstance(ColorSpace.CS_sRGB);
        // color model bits
        var bits = new int[image.hasAlpha() ? 4 : 3];
        Arrays.fill(bits, image.depth());
        // color model
        var model = new ComponentColorModel(
                srgb, bits, image.hasAlpha(), false,
                Transparency.TRANSLUCENT, image.pixels().getDataType());
        // color raster
        var raster = Raster.createInterleavedRaster(
                image.pixels(), image.width(), image.height(), bits.length * image.width(),
                bits.length, image.hasAlpha() ? new int[]{3, 2, 1, 0} : new int[]{2, 1, 0}, null);
        // buffered image now
        return new BufferedImage(model, raster, false, new Hashtable<>());
    }
}
