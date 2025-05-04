cmake_minimum_required(VERSION 3.24)

# set cmake toolchain
set(CMAKE_SYSTEM_NAME Windows)
set(CMAKE_SYSTEM_PROCESSOR x86_64)

# set clang as compiler
set(CMAKE_C_COMPILER x86_64-w64-mingw32-clang)
set(CMAKE_CXX_COMPILER x86_64-w64-mingw32-clang++)
set(CMAKE_BUILD_TYPE=MinSizeRel)

# set mingw library path
execute_process(
  COMMAND x86_64-w64-mingw32-clang -print-search-dirs
  COMMAND grep libraries:
  COMMAND sed "s/^libraries: =//"
  OUTPUT_VARIABLE AVIF_IMAGEIO_MINGW_LIBRARY_PATHS
  OUTPUT_STRIP_TRAILING_WHITESPACE
)
string(REGEX REPLACE ":+" ", " CMAKE_FIND_ROOT_PATH "${AVIF_IMAGEIO_MINGW_LIBRARY_PATHS}")
message(STATUS "MinGW library paths: ${CMAKE_FIND_ROOT_PATH}")

# set the compilers
set(CMAKE_C_COMPILER_TARGET x86_64-w64-mingw32)
set(CMAKE_CXX_COMPILER_TARGET x86_64-w64-mingw32)

# adjust how cmake searches for programs
set(CMAKE_FIND_ROOT_PATH_MODE_PROGRAM NEVER)
set(CMAKE_FIND_ROOT_PATH_MODE_LIBRARY ONLY)
set(CMAKE_FIND_ROOT_PATH_MODE_INCLUDE ONLY)

# set the linker
set(CMAKE_C_FLAGS_INIT "-static")
set(CMAKE_CXX_FLAGS_INIT "-static")
