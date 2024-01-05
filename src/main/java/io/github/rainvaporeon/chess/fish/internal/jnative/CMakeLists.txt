cmake_minimum_required(VERSION 3.26)
project(jnative C)

set(CMAKE_C_STANDARD 11)

include_directories(.)

add_executable(jnative
        com_spiritlight_chess_fish_internal_jnative_MagicBitboard.h
        com_spiritlight_chess_fish_internal_jnative_MagicBitboard.cpp)
