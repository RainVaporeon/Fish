cmake_minimum_required(VERSION 3.26)
project(jnative CXX)

set(CMAKE_C_STANDARD 17)

include_directories(.)

add_executable(jnative
        magic-bits/include/magic_bits.hpp
        io_github_rainvaporeon_chess_fish_internal_jnative_NativeMagicBoard.cpp
        io_github_rainvaporeon_chess_fish_internal_jnative_NativeMagicBoard.h)
