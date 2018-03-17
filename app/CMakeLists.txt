# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

add_library( # Sets the name of the library.
             native-lib

             # Sets the library as a shared library.
             SHARED

             # Provides a relative path to your source file(s).
              src/main/native/XChat.c
             # src/main/cpp/astart.c
              src/main/cpp/adht.c
              src/main/cpp/acache.c
              src/main/cpp/alocal.c
              src/main/cpp/track.c
              src/main/cpp/listen.c
              src/main/cpp/record.c
              src/main/cpp/ad.c
              src/main/cpp/social.c
              src/main/cpp/aip.c
              src/main/cpp/lib/util.c
              src/main/cpp/xchat/xcommon.c
              src/main/cpp/xchat/store.c
              src/main/cpp/xchat/cutil.c
              src/main/cpp/xchat/retransmit.c
              src/main/cpp/xchat/message.c
              src/main/cpp/lib/configfiles.c
              src/main/cpp/lib/allnet_log.c
              src/main/cpp/lib/priority.c
              src/main/cpp/lib/allnet_queue.c
              src/main/cpp/lib/util.c
              src/main/cpp/lib/trace_util.c
              src/main/cpp/lib/ai.c
              src/main/cpp/lib/routing.c
              src/main/cpp/lib/table.c
              src/main/cpp/lib/pqueue.c
              src/main/cpp/lib/app_util.c
              src/main/cpp/lib/mapchar.c
              src/main/cpp/lib/keys.c
              src/main/cpp/lib/pipemsg.c
              src/main/cpp/lib/sha.c
              src/main/cpp/lib/asn1.c
              src/main/cpp/lib/wp_rsa.c
              src/main/cpp/lib/wp_arith.c
              src/main/cpp/lib/crypt_sel.c
              src/main/cpp/lib/cipher.c
              src/main/cpp/lib/stream.c
              src/main/cpp/lib/wp_aes.c
              src/main/cpp/lib/dcache.c
              src/main/cpp/mgmt/traced.c
              src/main/cpp/mgmt/keyd.c
              src/main/cpp/mgmt/trace.c
               )

 include_directories(src/main/cpp)

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
              log-lib

              # Specifies the name of the NDK library that
              # you want CMake to locate.
              log )

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
                       native-lib
                       # Links the target library to the log library
                       # included in the NDK.
                       ${log-lib} )