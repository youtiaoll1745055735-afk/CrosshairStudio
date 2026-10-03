package com.crosshair.studio.core

import androidx.compose.ui.graphics.Color
import kotlinx.serialization.json.Json

val AppJson: Json = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
}

fun Long.toComposeColor(): Color = Color(this and 0xFFFFFFFFL)

fun Long.toHexString(): String =
    "%02X%02X%02X".format((this shr 16) and 0xFF, (this shr 8) and 0xFF, this and 0xFF)

fun Long.red(): Int = ((this shr 16) and 0xFF).toInt()
fun Long.green(): Int = ((this shr 8) and 0xFF).toInt()
fun Long.blue(): Int = (this and 0xFF).toInt()

fun argbLong(a: Int, r: Int, g: Int, b: Int): Long =
    ((a.toLong() and 0xFF) shl 24) or ((r.toLong() and 0xFF) shl 16) or
        ((g.toLong() and 0xFF) shl 8) or (b.toLong() and 0xFF)

fun parseHex(s: String): Long? {
    val t = s.trim().removePrefix("#")
    if (t.length != 6) return null
    val v = t.toLongOrNull(16) ?: return null
    return argbLong(0xFF, ((v shr 16) and 0xFF).toInt(), ((v shr 8) and 0xFF).toInt(), (v and 0xFF).toInt())
}
