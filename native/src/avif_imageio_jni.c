#include <jni.h>
#include <stddef.h>
#include <string.h>
#include <avif/avif.h>

#define JAVA_IO_BATCH_SIZE 8192
#define JAVA_ARRAY_GRARANTEE_SIZE 0x7FFFFFF7

#define IMAGEIO_INPUT "javax/imageio/stream/ImageInputStream"
#define IMAGEIO_INPUT_LENGTH "length", "()J"
#define IMAGEIO_INPUT_READ "read", "([B)I"
#define IMAGEIO_INPUT_READ_FULLY "readFully", "([B)V"

#define AWT_DATA_BUFFER_BYTE "java/awt/image/DataBufferByte"
#define AWT_DATA_BUFFER_BYTE_INIT "<init>", "([BI)V"
#define AWT_DATA_BUFFER_USHORT "java/awt/image/DataBufferUShort"
#define AWT_DATA_BUFFER_USHORT_INIT "<init>", "([SI)V"

#define AVIF_IMAGEIO_EX "com/github/ustc_zzzz/imageio/avif/AVIFException"
#define AVIF_IMAGEIO_DECODED "com/github/ustc_zzzz/imageio/avif/Native$Decoded"
#define AVIF_IMAGEIO_DECODED_INIT "<init>", "(IIILjava/lang/String;ZIIII[B[B[BZZJJIJJ)V"
#define AVIF_IMAGEIO_IMAGE "com/github/ustc_zzzz/imageio/avif/Native$Image"
#define AVIF_IMAGEIO_IMAGE_INIT "<init>", "(IIIJJZLjava/awt/image/DataBuffer;I)V"

#define AVIF_CHECK_OK(RESULT)     \
    do                            \
    {                             \
        avifResult _r = (RESULT); \
        if (_r != AVIF_RESULT_OK) \
        {                         \
            return _r;            \
        }                         \
    } while (AVIF_FALSE)

#define AVIF_CHECK_PTR(OBJ, RESULT) \
    do                              \
    {                               \
        void *_o = (OBJ);           \
        avifResult _r = (RESULT);   \
        if (!_o)                    \
        {                           \
            return _r;              \
        }                           \
    } while (AVIF_FALSE)

#define AVIF_CHECK_JNI_EX(ENV, RESULT) \
    do                                 \
    {                                  \
        JNIEnv *_e = (ENV);            \
        avifResult _r = (RESULT);      \
        if ((*_e)->ExceptionCheck(_e)) \
        {                              \
            return _r;                 \
        }                              \
    } while (AVIF_FALSE)

#define AVIF_THROW(ENV, RESULT)                                \
    do                                                         \
    {                                                          \
        JNIEnv *_e = (ENV);                                    \
        avifResult _r = (RESULT);                              \
        if (!(*_e)->ExceptionCheck(_e))                        \
        {                                                      \
            jclass _c = (*_e)->FindClass(_e, AVIF_IMAGEIO_EX); \
            (*_e)->ThrowNew(_e, _c, avifResultToString(_r));   \
        }                                                      \
    } while (AVIF_FALSE)

/**
 * Reads data from a Java ImageInputStream into an avifRWData buffer.
 * The memory buffer may be larger than the data read.
 * If the stream length is small and known, reads in one shot.
 * Otherwise, data is read in fixed-size chunks until EOF.
 * Note: *readSize may be less than memory->size if fewer bytes were available.
 *
 * @param env              input JNI environment pointer.
 * @param imageInputStream input Java ImageInputStream object.
 * @param memory           output avifRWData for receiving bytes.
 * @param readSize         output number of bytes read (always <= memory->size).
 * @return AVIF_RESULT_OK on success, or an error code on failure.
 */
avifResult avifJNI2RWData(
    JNIEnv *env, jobject imageInputStream, avifRWData *memory, jlong *readSize)
{
    // image io class
    jclass imageCls = (*env)->FindClass(env, IMAGEIO_INPUT);

    // read size
    jmethodID lengthFunc = (*env)->GetMethodID(env, imageCls, IMAGEIO_INPUT_LENGTH);
    jlong length = (*env)->CallLongMethod(env, imageInputStream, lengthFunc);
    AVIF_CHECK_JNI_EX(env, AVIF_RESULT_INTERNAL_ERROR);

    // copy data only once from image input if available
    if (length >= 0 && length <= JAVA_ARRAY_GRARANTEE_SIZE)
    {
        jmethodID readFunc = (*env)->GetMethodID(env, imageCls, IMAGEIO_INPUT_READ_FULLY);
        // realloc only once
        AVIF_CHECK_OK(avifRWDataRealloc(memory, (size_t)length));
        // construct a java byte array
        jbyteArray buffer = (*env)->NewByteArray(env, (jsize)length);
        AVIF_CHECK_PTR(buffer, AVIF_RESULT_IO_ERROR);
        // copy to the java byte array
        (*env)->CallVoidMethod(env, imageInputStream, readFunc, buffer);
        AVIF_CHECK_JNI_EX(env, AVIF_RESULT_INTERNAL_ERROR);
        // copy to rw data
        void *dest = memory->data;
        (*env)->GetByteArrayRegion(env, buffer, 0, (jsize)length, dest);
        *readSize = length;
        return AVIF_RESULT_OK;
    }
    // range that byte array should be copied more than once
    jmethodID readFunc = (*env)->GetMethodID(env, imageCls, IMAGEIO_INPUT_READ);
    // realloc first
    size_t initAllocSize = length < 0 ? JAVA_IO_BATCH_SIZE * 2 : (size_t)length;
    AVIF_CHECK_OK(avifRWDataRealloc(memory, initAllocSize));
    // construct a fixed size java byte array
    jbyteArray buffer = (*env)->NewByteArray(env, JAVA_IO_BATCH_SIZE);
    AVIF_CHECK_PTR(buffer, AVIF_RESULT_OUT_OF_MEMORY);
    // copy to the java byte array sequencially
    jlong confirmed = 0;
    while (AVIF_TRUE)
    {
        // let's try to copy
        jint copied = (*env)->CallIntMethod(env, imageInputStream, readFunc, buffer);
        AVIF_CHECK_JNI_EX(env, AVIF_RESULT_INTERNAL_ERROR);
        if (copied < 0)
        {
            // eof
            break;
        }
        // realloc if needed
        jlong newConfirmed = confirmed + copied;
        if (newConfirmed > memory->size)
        {
            AVIF_CHECK_OK(avifRWDataRealloc(memory, memory->size * 3 / 2));
        }
        // copy to rw data
        void *dest = memory->data + confirmed;
        (*env)->GetByteArrayRegion(env, buffer, 0, (jsize)copied, dest);
        // confirm
        confirmed = newConfirmed;
    }
    *readSize = confirmed;
    return AVIF_RESULT_OK;
}

/**
 * Copies the bytes from the avifRWData buffer to a new Java array.
 *
 * @param env    input JNI environment pointer.
 * @param rwData input avifRWData containing bytes to copy.
 * @param array  output Java byte array receiving copied bytes.
 * @return AVIF_RESULT_OK on success, or an error code on failure.
 */
avifResult avifRWData2JNI(
    JNIEnv *env, avifRWData rwData, jbyteArray *array)
{
    jbyteArray result = (*env)->NewByteArray(env, rwData.size);
    AVIF_CHECK_PTR(result, AVIF_RESULT_IO_ERROR);
    const void *data = rwData.data;
    (*env)->SetByteArrayRegion(env, result, 0, rwData.size, data);
    *array = result;
    return AVIF_RESULT_OK;
}

/**
 * Converts an avifPixelFormat enum to a Java string.
 * If the format is unknown, sets output str to NULL.
 *
 * @param env    input JNI environment pointer.
 * @param format input avifPixelFormat value.
 * @param str    output jstring receiving Java string or NULL if unknown.
 * @return AVIF_RESULT_OK on success.
 */
avifResult avifPixelFormat2JNI(
    JNIEnv *env, avifPixelFormat format, jstring *str)
{
    jstring result = NULL;
    const char *outName = avifPixelFormatToString(format);
    *str = !strcmp(outName, "Unknown") ? NULL : (*env)->NewStringUTF(env, outName);
    return AVIF_RESULT_OK;
}

/**
 * Builds a Java Native.Decoded from an avifDecoder.
 * Parses AVIF data and extracts avif metadata fields.
 *
 * @param env      input JNI environment pointer.
 * @param decoder  input avifDecoder instance.
 * @param memory   input avifRWData with encoded bytes.
 * @param readSize input number of bytes read into avifRWData.
 * @param obj      output jobject receiving Native.Decoded record.
 * @return AVIF_RESULT_OK on success, or an error code on failure.
 */
avifResult avifDecoder2JNI(
    JNIEnv *env, avifDecoder *decoder, avifRWData *memory, jlong readSize, jobject *obj)
{
    // read to decoder from memory
    AVIF_CHECK_OK(avifDecoderSetIOMemory(decoder, memory->data, readSize));

    // parse decoder
    AVIF_CHECK_OK(avifDecoderParse(decoder));

    // find record class
    jclass decodedCls = (*env)->FindClass(env, AVIF_IMAGEIO_DECODED);
    AVIF_CHECK_PTR(decodedCls, AVIF_RESULT_INTERNAL_ERROR);

    // find record constructor
    jmethodID initFunc = (*env)->GetMethodID(env, decodedCls, AVIF_IMAGEIO_DECODED_INIT);
    AVIF_CHECK_PTR(initFunc, AVIF_RESULT_INTERNAL_ERROR);

    // things from image
    avifImage *img = decoder->image;

    // yuv format
    jstring yuvStr;
    AVIF_CHECK_OK(avifPixelFormat2JNI(env, img->yuvFormat, &yuvStr));

    // icc & exif & xmp
    jobject icc, exif, xmp;
    AVIF_CHECK_OK(avifRWData2JNI(env, img->icc, &icc));
    AVIF_CHECK_OK(avifRWData2JNI(env, img->exif, &exif));
    AVIF_CHECK_OK(avifRWData2JNI(env, img->xmp, &xmp));

    // time related
    uint64_t ts = decoder->timescale;
    uint64_t durationInTs = decoder->durationInTimescales;
    avifBool tsJavaCompatible = ts <= INT64_MAX && durationInTs <= INT64_MAX;
    AVIF_CHECK_PTR(tsJavaCompatible ? decoder : NULL, AVIF_RESULT_INVALID_ARGUMENT);

    // construct record and return
    *obj = (*env)->NewObject(
        env, decodedCls, initFunc,
        img->width, img->height, img->depth,
        yuvStr, img->yuvRange == AVIF_RANGE_FULL, img->yuvChromaSamplePosition,
        img->colorPrimaries, img->transferCharacteristics, img->matrixCoefficients,
        icc, exif, xmp, decoder->alphaPresent, img->alphaPremultiplied,
        ts, durationInTs, decoder->imageCount, (jlong)decoder, (jlong)memory);
    return AVIF_RESULT_OK;
}

/**
 * Builds a Java Native.Image from an avifImage and Java DataBuffer.
 *
 * @param env        input JNI environment pointer.
 * @param image      input avifImage holding pixel metadata.
 * @param hasAlpha   input boolean indicating alpha presence.
 * @param dataBuffer input Java DataBuffer wrapping pixel array.
 * @param obj        output jobject receiving Native.Image record.
 * @return AVIF_RESULT_OK on success, or an error code on failure.
 */
avifResult avifImage2JNI(
    JNIEnv *env, avifImage *img, avifImageTiming *timing,
    avifBool hasAlpha, jobject dataBuffer, jobject *obj)
{
    // find record class
    jclass imageCls = (*env)->FindClass(env, AVIF_IMAGEIO_IMAGE);
    AVIF_CHECK_PTR(img, AVIF_RESULT_INTERNAL_ERROR);

    // find record constructor
    jmethodID initFunc = (*env)->GetMethodID(env, imageCls, AVIF_IMAGEIO_IMAGE_INIT);
    AVIF_CHECK_PTR(initFunc, AVIF_RESULT_INTERNAL_ERROR);

    // time related
    jlong ptsInTs = timing->ptsInTimescales;
    jlong durationInTs = timing->durationInTimescales;
    avifBool tsJavaCompatible = ptsInTs <= INT64_MAX && durationInTs <= INT64_MAX;
    AVIF_CHECK_PTR(tsJavaCompatible ? img : NULL, AVIF_RESULT_INVALID_ARGUMENT);

    // construct record and return
    *obj = (*env)->NewObject(
        env, imageCls, initFunc,
        img->width, img->height, img->depth,
        ptsInTs, durationInTs,
        (jboolean)hasAlpha, dataBuffer);
    return AVIF_RESULT_OK;
}

/**
 * Releases Java array elements allocated for avifRGBImage pixels.
 *
 * @see avifRGBImageAllocJNIArray
 *
 * @param env       input JNI environment pointer.
 * @param rgb       input avifRGBImage with pixel data allocated by JNI.
 * @param dataArray input Java array holding pixel data.
 */
void avifRGBImageFreeJNIArray(
    JNIEnv *env, avifRGBImage *rgb, jarray *dataArray)
{
    // depth more than 8: short, otherwise: byte
    if (rgb->depth > 8)
    {
        void *pixelRegion = rgb->pixels;
        (*env)->ReleaseShortArrayElements(env, (jshortArray)*dataArray, pixelRegion, 0);
        rgb->rowBytes = 0;
        rgb->pixels = NULL;
    }
    else
    {
        void *pixelRegion = rgb->pixels;
        (*env)->ReleaseByteArrayElements(env, (jbyteArray)*dataArray, pixelRegion, 0);
        rgb->rowBytes = 0;
        rgb->pixels = NULL;
    }
}

/**
 * Allocates Java array and wrap it into DataBuffer for avifRGBImage pixels.
 *
 * @see avifRGBImageFreeJNIArray
 *
 * @param env        input JNI environment pointer.
 * @param rgb        input avifRGBImage to initialize.
 * @param dataArray  output jarray receiving pixel array allocated by JNI.
 * @param dataBuffer output jobject receiving DataBuffer wrapping the array.
 * @return AVIF_RESULT_OK on success, or an error code on failure.
 */
avifResult avifRGBImageAllocJNIArray(
    JNIEnv *env, avifRGBImage *rgb, jarray *dataArray, jobject *dataBuffer)
{
    // check if width is acceptable for java
    uint32_t pixelChannels = avifRGBFormatChannelCount(rgb->format);
    uint32_t maxWidth = (uint32_t)JAVA_ARRAY_GRARANTEE_SIZE / pixelChannels;
    AVIF_CHECK_PTR(rgb->width > maxWidth ? NULL : rgb, AVIF_RESULT_INVALID_ARGUMENT);

    // check if height is acceptable for java
    uint32_t rowChannels = rgb->width * pixelChannels;
    uint32_t maxHeight = (uint32_t)JAVA_ARRAY_GRARANTEE_SIZE / rowChannels;
    AVIF_CHECK_PTR(rgb->height > maxHeight ? NULL : rgb, AVIF_RESULT_INVALID_ARGUMENT);
    uint32_t channels = rgb->height * rowChannels;

    // depth more than 8: short, otherwise: byte
    if (rgb->depth > 8)
    {
        // construct short array
        jobject shortArray = (*env)->NewShortArray(env, channels);
        AVIF_CHECK_PTR(shortArray, AVIF_RESULT_OUT_OF_MEMORY);

        // find buffer class
        jclass bufferCls = (*env)->FindClass(env, AWT_DATA_BUFFER_USHORT);
        AVIF_CHECK_PTR(bufferCls, AVIF_RESULT_INTERNAL_ERROR);

        // find record constructor
        jmethodID initFunc = (*env)->GetMethodID(env, bufferCls, AWT_DATA_BUFFER_USHORT_INIT);
        AVIF_CHECK_PTR(initFunc, AVIF_RESULT_INTERNAL_ERROR);

        // construct data buffer
        *dataBuffer = (*env)->NewObject(env, bufferCls, initFunc, shortArray, channels);
        *dataArray = shortArray;

        // get short pointer and return
        void *pixelRegion = (*env)->GetShortArrayElements(env, shortArray, NULL);
        rgb->rowBytes = rowChannels * 2;
        rgb->pixels = pixelRegion;
        return AVIF_RESULT_OK;
    }
    else
    {
        // construct byte array
        jobject byteArray = (*env)->NewByteArray(env, channels);
        AVIF_CHECK_PTR(byteArray, AVIF_RESULT_OUT_OF_MEMORY);

        // find buffer class
        jclass bufferCls = (*env)->FindClass(env, AWT_DATA_BUFFER_BYTE);
        AVIF_CHECK_PTR(bufferCls, AVIF_RESULT_INTERNAL_ERROR);

        // find record constructor
        jmethodID initFunc = (*env)->GetMethodID(env, bufferCls, AWT_DATA_BUFFER_BYTE_INIT);
        AVIF_CHECK_PTR(initFunc, AVIF_RESULT_INTERNAL_ERROR);

        // construct data buffer
        *dataBuffer = (*env)->NewObject(env, bufferCls, initFunc, byteArray, channels);
        *dataArray = byteArray;

        // get byte pointer and return
        void *pixelRegion = (*env)->GetByteArrayElements(env, byteArray, NULL);
        rgb->rowBytes = rowChannels;
        rgb->pixels = pixelRegion;
        return AVIF_RESULT_OK;
    }
}

/**
 * JNI bridge for com.github.ustc_zzzz.imageio.avif.Native.next(long).
 */
JNIEXPORT jobject JNICALL
Java_com_github_ustc_1zzzz_imageio_avif_Native_next(
    JNIEnv *env, jclass staticCls, jlong decoderHandle)
{
    // decoder
    avifDecoder *decoder = (avifDecoder *)decoderHandle;

    // next image (raise error if it reaches the end)
    avifResult result = avifDecoderNextImage(decoder);
    if (result != AVIF_RESULT_OK)
    {
        AVIF_THROW(env, result);
        return NULL;
    }

    // rgb image (java imageio likes bgr and bgra)
    avifRGBImage rgbImage;
    avifImage *img = decoder->image;
    avifRGBImageSetDefaults(&rgbImage, img);
    avifBool discardAlpha = !img->alphaPlane;
    rgbImage.format = discardAlpha ? AVIF_RGB_FORMAT_BGR : AVIF_RGB_FORMAT_BGRA;
    rgbImage.avoidLibYUV = AVIF_TRUE;

    // allocate pixel bytes
    jobject dataArray, dataBuffer;
    result = avifRGBImageAllocJNIArray(env, &rgbImage, &dataArray, &dataBuffer);
    if (result != AVIF_RESULT_OK)
    {
        AVIF_THROW(env, result);
        return NULL;
    }

    // reformat the image to rgb
    result = avifImageYUVToRGB(img, &rgbImage);
    if (result != AVIF_RESULT_OK)
    {
        avifRGBImageFreeJNIArray(env, &rgbImage, &dataArray);
        AVIF_THROW(env, result);
        return NULL;
    }

    // construct the rgb result
    jobject obj;
    avifImageTiming *timing = &decoder->imageTiming;
    result = avifImage2JNI(env, img, timing, !discardAlpha, dataBuffer, &obj);
    if (result != AVIF_RESULT_OK)
    {
        avifRGBImageFreeJNIArray(env, &rgbImage, &dataArray);
        AVIF_THROW(env, result);
        return NULL;
    }

    // release the pixel bytes and return
    avifRGBImageFreeJNIArray(env, &rgbImage, &dataArray);
    return obj;
}

/**
 * JNI bridge for com.github.ustc_zzzz.imageio.avif.Native.destroy(long, long).
 * This method releases the avifDecoder and the avifRWData allocated previously.
 *
 * @warning This method won't be automatically called by Java GC. Use it with Java cleaner.
 *
 * @see Java_com_github_ustc_1zzzz_imageio_avif_Native_create
 */
JNIEXPORT void JNICALL
Java_com_github_ustc_1zzzz_imageio_avif_Native_destroy(
    JNIEnv *env, jclass staticCls, jlong decoderHandle, jlong memoryHandle)
{
    // decoder and data
    avifDecoder *decoder = (avifDecoder *)decoderHandle;
    avifRWData *memory = (avifRWData *)memoryHandle;

    // destroy decoder and data
    avifDecoderDestroy(decoder);
    avifRWDataFree(memory);
    avifFree(memory);
}

/**
 * JNI bridge for com.github.ustc_zzzz.imageio.avif.Native.create(ImageInputStream).
 * This method allocated an avifDecoder and an avifRWData and read their metadata.
 *
 * @warning there are resources that will not be released by Java GC. Free them manually.
 *
 * @see Java_com_github_ustc_1zzzz_imageio_avif_Native_destroy
 */
JNIEXPORT jobject JNICALL
Java_com_github_ustc_1zzzz_imageio_avif_Native_create(
    JNIEnv *env, jclass staticCls, jobject imageInputStream)
{
    // create rw data
    avifRWData *memory = avifAlloc(sizeof(avifRWData));
    if (!memory)
    {
        AVIF_THROW(env, AVIF_RESULT_OUT_OF_MEMORY);
        return NULL;
    }
    memory->size = 0;
    memory->data = NULL;

    // copy data
    jlong readSize;
    avifResult result = avifJNI2RWData(env, imageInputStream, memory, &readSize);
    if (result != AVIF_RESULT_OK)
    {
        avifRWDataFree(memory);
        avifFree(memory);
        AVIF_THROW(env, result);
        return NULL;
    }

    // decoder
    avifDecoder *decoder = avifDecoderCreate();
    if (!decoder)
    {
        avifRWDataFree(memory);
        avifFree(memory);
        AVIF_THROW(env, AVIF_RESULT_OUT_OF_MEMORY);
        return NULL;
    }

    // construct the parsed result
    jobject obj;
    result = avifDecoder2JNI(env, decoder, memory, readSize, &obj);
    if (result != AVIF_RESULT_OK)
    {
        avifDecoderDestroy(decoder);
        avifRWDataFree(memory);
        avifFree(memory);
        AVIF_THROW(env, result);
        return NULL;
    }
    return obj;
}
