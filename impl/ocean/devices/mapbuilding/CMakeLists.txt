# Copyright (c) Meta Platforms, Inc. and affiliates.
#
# This source code is licensed under the MIT license found in the
# LICENSE file in the root directory of this source tree.

cmake_minimum_required(VERSION 3.26)

add_subdirectory(jni)

if (MACOS OR ANDROID OR IOS OR WIN32)

    set(OCEAN_TARGET_NAME "ocean_devices_mapbuilding")

    # Source files
    file(GLOB OCEAN_TARGET_HEADER_FILES "${CMAKE_CURRENT_LIST_DIR}/*.h")
    file(GLOB OCEAN_TARGET_SOURCE_FILES "${CMAKE_CURRENT_LIST_DIR}/*.cpp")

    # Target definition
    add_library(${OCEAN_TARGET_NAME} ${OCEAN_TARGET_SOURCE_FILES} ${OCEAN_TARGET_HEADER_FILES})

    target_include_directories(${OCEAN_TARGET_NAME} PUBLIC "${OCEAN_IMPL_DIR}")

    target_compile_definitions(${OCEAN_TARGET_NAME} PUBLIC ${OCEAN_PREPROCESSOR_FLAGS})

    if (BUILD_SHARED_LIBS)
        target_compile_definitions(${OCEAN_TARGET_NAME} PRIVATE "-DUSE_OCEAN_DEVICES_MAPBUILDING_EXPORT")
    endif()

    target_compile_options(${OCEAN_TARGET_NAME} PUBLIC "${OCEAN_COMPILER_FLAGS}")

    if (NOT WIN32)
        target_compile_options(${OCEAN_TARGET_NAME} PRIVATE "-fexceptions")
    endif()

    # Dependencies
    target_link_libraries(${OCEAN_TARGET_NAME}
        PUBLIC
            ocean_base
            ocean_cv
            ocean_devices
            ocean_geometry
            ocean_io
            ocean_math
            ocean_media
            ocean_tracking_mapbuilding
    )

    # Installation
    install(TARGETS ${OCEAN_TARGET_NAME}
            DESTINATION ${CMAKE_INSTALL_LIBDIR}
            COMPONENT lib
    )

    install(FILES ${OCEAN_TARGET_HEADER_FILES}
            DESTINATION ${CMAKE_INSTALL_PREFIX}/include/ocean/devices/mapbuilding
            COMPONENT include
    )

endif()
