# AVIF ImageIO Native Reader

[![](https://jitpack.io/v/ustc-zzzz/avif-imageio-native-reader.svg)](https://jitpack.io/#ustc-zzzz/avif-imageio-native-reader)

A native JNI binding for avif image format, which supports Java imageio service.

This library requires Java 17 or above.

## Include Dependency

```groovy
// Add it in your root build.gradle at the end of repositories
allprojects {
    repositories {
        maven { url 'https://jitpack.io' }
    }
}
// Add the dependency
dependencies {
    implementation 'com.github.ustc-zzzz:avif-imageio-native-reader:master-SNAPSHOT'
}
```

## Usage

```
$ jshell --class-path avif-imageio-native-reader.jar
|  Welcome to JShell -- Version 17.0.15
|  For an introduction type: /help intro

jshell> import javax.imageio.*

jshell> ImageIO.read(new File("paris_icc_exif_xmp.avif")) // still image
$2 ==> BufferedImage@6aceb1a5: type = 5 ColorModel: #pixelBits = 24 numComponents = 3 color space = java.awt.color.ICC_ColorSpace@2d6d8735 transparency = 1 has alpha = false isAlphaPre = false ByteInterleavedRaster: width = 403 height = 302 #numDataElements 3 dataOff[0] = 2

jshell> "%dx%d".formatted($2.getWidth(), $2.getHeight())
$3 ==> "403x302"

jshell> ImageIO.getImageReadersBySuffix("avif").next()
$4 ==> com.github.ustc_zzzz.imageio.avif.AVIFImageReader@7bb11784

jshell> ImageIO.createImageInputStream(new File("horse_in_motion_yuv420_8bpc.avif")) // animated image
$5 ==> javax.imageio.stream.FileImageInputStream@34033bd0

jshell> $4.setInput($5)

jshell> $4.getNumImages(true) // image count
$7 ==> 11

jshell> $4.read(0) // first image
$8 ==> BufferedImage@17d99928: type = 5 ColorModel: #pixelBits = 24 numComponents = 3 color space = java.awt.color.ICC_ColorSpace@2d6d8735 transparency = 1 has alpha = false isAlphaPre = false ByteInterleavedRaster: width = 1370 height = 885 #numDataElements 3 dataOff[0] = 2

jshell> $4.read(10) // last image
$9 ==> BufferedImage@1ae369b7: type = 5 ColorModel: #pixelBits = 24 numComponents = 3 color space = java.awt.color.ICC_ColorSpace@2d6d8735 transparency = 1 has alpha = false isAlphaPre = false ByteInterleavedRaster: width = 1370 height = 885 #numDataElements 3 dataOff[0] = 2

jshell> $4.getImageMetadata(0) // metadata for current frame
$10 ==> com.github.ustc_zzzz.imageio.avif.AVIFImageMetadata@2b80d80f

jshell> (javax.imageio.metadata.IIOMetadataNode) $10.getAsTree("ustc_zzzz_imageio_avif_image_1.0") // metadata tree
$11 ==> javax.imageio.metadata.IIOMetadataNode@28864e92

jshell> (javax.imageio.metadata.IIOMetadataNode) $11.getElementsByTagName("depth").item(0) // depth node
$12 ==> javax.imageio.metadata.IIOMetadataNode@6833ce2c

jshell> $12.getUserObject() // 8-bit depth image
$13 ==> 8

jshell> (javax.imageio.metadata.IIOMetadataNode) $11.getElementsByTagName("ptsInTimescales").item(0) // ptsInTimescales node
$14 ==> javax.imageio.metadata.IIOMetadataNode@6e3c1e69

jshell> $14.getUserObject() // this image to be shown at tick 0
$15 ==> 0

jshell> (javax.imageio.metadata.IIOMetadataNode) $11.getElementsByTagName("durationInTimescales").item(0) // durationInTimescales node
$16 ==> javax.imageio.metadata.IIOMetadataNode@649d209a

jshell> $16.getUserObject() // this image to be shown for 1 tick
$17 ==> 1

jshell> $4.getStreamMetadata() // metadata for the whole avif image
$18 ==> com.github.ustc_zzzz.imageio.avif.AVIFStreamMetadata@256216b3

jshell> (javax.imageio.metadata.IIOMetadataNode) $18.getAsTree("ustc_zzzz_imageio_avif_stream_1.0") // metadata tree
$19 ==> javax.imageio.metadata.IIOMetadataNode@d7b1517

jshell> (javax.imageio.metadata.IIOMetadataNode) $19.getElementsByTagName("timeScale").item(0) // timeScale node
$20 ==> javax.imageio.metadata.IIOMetadataNode@23223dd8

jshell> $20.getUserObject() // the time scale is 15Hz (one tick for 1/15 second)
$21 ==> 15
```
