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
              src/main/native/CHelper.c
              src/main/cfiles/astart.c
              src/main/cfiles/adht.c
              src/main/cfiles/acache.c
              src/main/cfiles/alocal.c
              src/main/cfiles/track.c
              src/main/cfiles/listen.c
              src/main/cfiles/record.c
              src/main/cfiles/ad.c
              src/main/cfiles/social.c
              src/main/cfiles/aip.c
              src/main/cfiles/lib/util.c
              src/main/cfiles/xchat/xcommon.c
              src/main/cfiles/xchat/store.c
              src/main/cfiles/xchat/cutil.c
              src/main/cfiles/xchat/retransmit.c
              src/main/cfiles/xchat/message.c
              src/main/cfiles/lib/configfiles.c
              src/main/cfiles/lib/allnet_log.c
              src/main/cfiles/lib/priority.c
              src/main/cfiles/lib/allnet_queue.c
              src/main/cfiles/lib/util.c
              src/main/cfiles/lib/trace_util.c
              src/main/cfiles/lib/ai.c
              src/main/cfiles/lib/routing.c
              src/main/cfiles/lib/table.c
              src/main/cfiles/lib/pqueue.c
              src/main/cfiles/lib/app_util.c
              src/main/cfiles/lib/mapchar.c
              src/main/cfiles/lib/keys.c
              src/main/cfiles/lib/pipemsg.c
              src/main/cfiles/lib/sha.c
              src/main/cfiles/lib/asn1.c
              src/main/cfiles/lib/wp_rsa.c
              src/main/cfiles/lib/wp_arith.c
              src/main/cfiles/lib/crypt_sel.c
              src/main/cfiles/lib/cipher.c
              src/main/cfiles/lib/stream.c
              src/main/cfiles/lib/wp_aes.c
              src/main/cfiles/lib/dcache.c
              src/main/cfiles/mgmt/traced.c
              src/main/cfiles/mgmt/keyd.c
              src/main/cfiles/mgmt/trace.c
               )

 include_directories(src/main/cfiles)

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