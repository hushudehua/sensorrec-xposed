cmake_minimum_required(VERSION 3.4.1)
add_library(recorder_native SHARED recorder_native.c)
find_library(log-lib log)
target_link_libraries(recorder_native ${log-lib})
