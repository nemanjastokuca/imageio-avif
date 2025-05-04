cmake_minimum_required(VERSION 3.24)

project(avif-imageio-jni LANGUAGES C VERSION 1.0)

# Global static linking
set(AVIF_CODEC_AOM NONE)
set(AVIF_CODEC_DAV1D LOCAL)
set(AVIF_CODEC_LIBGAV1 NONE)
set(AVIF_CODEC_RAV1E NONE)
set(AVIF_CODEC_SVT NONE)
set(AVIF_LIBYUV NONE)
set(AVIF_LIBSHARPYUV NONE)
set(AVIF_JPEG NONE)
set(AVIF_ZLIBPNG NONE)
set(CMAKE_POSITION_INDEPENDENT_CODE ON)

# Add source code for library
add_library(avif-imageio-jni SHARED src/avif_imageio_jni.c)
target_compile_options(avif-imageio-jni PRIVATE -fPIC)

# Add OSX dependency
if(APPLE)
  if(NOT DEFINED CMAKE_OSX_SYSROOT)
    message(FATAL_ERROR Cmake did not find any CMAKE_OSX_SYSROOT, please recheck your osx sdk root path)
  else()
    message(STATUS "Using apple ${CMAKE_SYSTEM_PROCESSOR} (sdk path: ${CMAKE_OSX_SYSROOT})")
  endif()
endif()

# Add JNI dependency
include(FetchContent)
set(FETCHCONTENT_QUIET OFF)
if(WIN32)
  FetchContent_Declare(jni-win
    URL https://aka.ms/download-jdk/microsoft-jdk-17.0.15-windows-x64.zip
    URL_HASH SHA256=4731e8b69287b26ff0f87cbd47c4aa6dec7581664f57047bad00e47d99bb4abd)
  FetchContent_MakeAvailable(jni-win)
  set(JNI_INCLUDE_DIRS
    ${FETCHCONTENT_BASE_DIR}/jni-win-src/include
    ${FETCHCONTENT_BASE_DIR}/jni-win-src/include/win32)
else()
  include(FindJNI)
endif()

# Add dav1d dependency and inject for avif
FetchContent_Declare(dav1d
  GIT_REPOSITORY https://code.videolan.org/videolan/dav1d.git
  GIT_TAG 1.5.1)
FetchContent_MakeAvailable(dav1d)
set(AVIF_CODEC_DAV1D_SOURCE_DIR "${FETCHCONTENT_BASE_DIR}/dav1d-src")

# Add avif dependency
FetchContent_Declare(libavif
  GIT_REPOSITORY https://github.com/AOMediaCodec/libavif.git
  GIT_TAG v1.2.1
  PATCH_COMMAND git apply --index ${CMAKE_CURRENT_SOURCE_DIR}/patches/libavif.cmake.patch
  UPDATE_COMMAND git reset --hard v1.2.1)
FetchContent_MakeAvailable(libavif)

# Link and target options
target_include_directories(avif-imageio-jni PRIVATE ${JNI_INCLUDE_DIRS})
target_link_libraries(avif-imageio-jni PRIVATE avif)
if(WIN32)
  set_target_properties(avif-imageio-jni PROPERTIES PREFIX "")
endif()
