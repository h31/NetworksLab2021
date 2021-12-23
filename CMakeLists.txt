cmake_minimum_required(VERSION 3.21)

set(TARGET_NAME simple_server)
project(SimpleChatServer)

add_compile_options(-Wall -ggdb)
aux_source_directory(./src SOURCES)
add_executable(${TARGET_NAME} ${SOURCES})
