cmake_minimum_required(VERSION 3.0)

add_executable(setuidgid main.c)
set_target_properties(setuidgid
        PROPERTIES OUTPUT_NAME libsetuidgid.so)