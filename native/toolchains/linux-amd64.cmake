cmake_minimum_required(VERSION 3.24)

# set cmake toolchain
set(CMAKE_SYSTEM_NAME Linux)
set(CMAKE_SYSTEM_PROCESSOR x86_64)

# set clang as compiler
set(CMAKE_C_COMPILER clang)
set(CMAKE_CXX_COMPILER clang++)
set(CMAKE_BUILD_TYPE=MinSizeRel)
