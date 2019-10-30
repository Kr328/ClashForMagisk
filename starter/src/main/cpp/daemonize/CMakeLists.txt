cmake_minimum_required(VERSION 3.0)

add_executable(daemonize main.c)
set_target_properties(daemonize
        PROPERTIES OUTPUT_NAME libdaemonize.so)