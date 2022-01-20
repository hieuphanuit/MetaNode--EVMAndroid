# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.10.2)

# Declares and names the project.

project("native")
set(CMAKE_CXX_STANDARD 17)
# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.


file(GLOB KECCAK_SOURCES
        3rdparty/keccak/*.c
        )

add_subdirectory(3rdparty)
# # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # #
# Common variables
# # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # #
set(EEVM_INCLUDE_DIRS
        ${CMAKE_CURRENT_SOURCE_DIR}/3rdparty
        ${CMAKE_CURRENT_SOURCE_DIR}/include
)

set(EEVM_CORE_SRCS
        src/disassembler.cpp
        src/processor.cpp
        src/stack.cpp
        src/transaction.cpp
        src/util.cpp
        )

set(EEVM_SIMPLE_SRCS
        src/simple/simpleaccount.cpp
        src/simple/simpleglobalstate.cpp
        src/simple/simplestorage.cpp
        )


add_library( # Sets the name of the library.
        native

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        native-lib.cpp
        ${EEVM_SIMPLE_SRCS}
)
target_include_directories(native PRIVATE
        ${EEVM_INCLUDE_DIRS}
)

add_library( # Sets the name of the library.
        eevm

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        ${EEVM_CORE_SRCS}
        ${KECCAK_SOURCES}
)
target_include_directories(eevm PRIVATE
        ${EEVM_INCLUDE_DIRS}
)
# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log)

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
        eevm

        # Links the target library to the log library
        # included in the NDK.
        ${log-lib})

target_link_libraries(eevm
        intx::intx
)

target_link_libraries(native eevm)