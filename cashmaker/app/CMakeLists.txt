# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.


#             src/main/cpp/art/art_method_replace.cpp
#             src/main/cpp/art/art_method_replace_4_4.cpp
#             src/main/cpp/art/art_method_replace_5_0.cpp
#             src/main/cpp/art/art_method_replace_5_1.cpp
#             src/main/cpp/art/art_method_replace_6_0.cpp
#             src/main/cpp/art/art_method_replace_7_0.cpp
#             src/main/cpp/dalvik/dalvik_method_replace.cpp
#             src/main/cpp/hook.cpp
#             src/main/cpp/andfix.cpp

cmake_minimum_required(VERSION 3.4.1)


INCLUDE_DIRECTORIES(src/main/cpp)
INCLUDE_DIRECTORIES(src/main/cpp/lua)
INCLUDE_DIRECTORIES(src/main/cpp/jsoncpp)
INCLUDE_DIRECTORIES(src/main/cpp/cutils)
INCLUDE_DIRECTORIES(src/main/cpp/android_runtime)
#INCLUDE_DIRECTORIES(src/main/cpp/art)
#INCLUDE_DIRECTORIES(src/main/cpp/dalvik)







file(GLOB lua_src "src/main/cpp/lua/*.cpp")
file(GLOB native_src "src/main/cpp/*.cpp")

add_library( # Sets the name of the library.
             native-lib
             # Sets the library as a shared library.
             SHARED

             # Provides a relative path to your source file(s).
             src/main/cpp/native-lib.cpp
         	 src/main/cpp/lua/lapi.c
             src/main/cpp/lua/lauxlib.c
             src/main/cpp/lua/lbaselib.c
             src/main/cpp/lua/lcode.c
             src/main/cpp/lua/ldblib.c
             src/main/cpp/lua/ldebug.c
             src/main/cpp/lua/ldo.c
             src/main/cpp/lua/ldump.c
             src/main/cpp/lua/lfunc.c
             src/main/cpp/lua/lgc.c
             src/main/cpp/lua/linit.c
             src/main/cpp/lua/liolib.c
             src/main/cpp/lua/llex.c
             src/main/cpp/lua/lmathlib.c
             src/main/cpp/lua/lmem.c
             src/main/cpp/lua/loadlib.c
             src/main/cpp/lua/lobject.c
             src/main/cpp/lua/lopcodes.c
             src/main/cpp/lua/loslib.c
             src/main/cpp/lua/lparser.c
             src/main/cpp/lua/lstate.c
             src/main/cpp/lua/lstring.c
             src/main/cpp/lua/lstrlib.c
             src/main/cpp/lua/ltable.c
             src/main/cpp/lua/ltablib.c
             src/main/cpp/lua/ltm.c
             src/main/cpp/lua/lundump.c
             src/main/cpp/lua/lvm.c
             src/main/cpp/lua/lzio.c
             src/main/cpp/jsoncpp/jsoncpp.cpp
             )



add_library(cutils SHARED IMPORTED)
set_target_properties(cutils  PROPERTIES IMPORTED_LOCATION   ${CMAKE_SOURCE_DIR}/libs/${ANDROID_ABI}/libcutils.so)

# -ldvm -landroid_runtime  -lart
find_library(
              log-lib
              log
              m-lib
              m
              d-lib
              d
              art-lib
              art
              android_runtime-lib
              android_runtime
              )



target_link_libraries( # Specifies the target library.
                       native-lib
                       cutils
                       ${log-lib} ${m-lib} ${d-lib} ${art-lib} ${android_runtime-lib} )