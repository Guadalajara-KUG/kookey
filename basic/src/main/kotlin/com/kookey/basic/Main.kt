package com.kookey.basic

import com.kookey.formal.Ensures
import com.kookey.formal.Requires

fun main() {
    println("Normal add: " + add(2, 3))
    println("Wrapper add: " + addWithContracts(2, 3))

    try {
        println("Wrapper add failing: " + addWithContracts(-1, 3))
    } catch (e: Exception) {
        println("Caught expected exception: ${e.message}")
    }
}

@Requires("x > 0", "y > 0")
@Ensures("result == x + y")
fun add(
    x: Int,
    y: Int,
): Int = x + y
