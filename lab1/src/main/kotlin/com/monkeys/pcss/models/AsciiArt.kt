package com.monkeys.pcss.models

import java.io.File
import javax.imageio.ImageIO
import kotlin.math.abs
import kotlin.math.roundToInt

class AsciiArt {

    fun getAsciiArt(imageFile: File): String {
        val image = ImageIO.read(imageFile)
        val res = StringBuilder()
        val step = getStep(image.width, image.height)
        for (y in 0 until image.height step step.first) {
            val sb = StringBuilder()
            for (x in 0 until image.width step step.second) {
                val color = image.getRGB(x, y)
                val colorCode = ((abs(color).toDouble() / (Int.MAX_VALUE / 100)) * 255).roundToInt().toShort()
                sb.append("${colorCodeIntoChar(colorCode)}")
            }
            if (sb.toString().trim().isNotEmpty()) {
                res.append(sb.toString().replace("\t", " ").replace("\n", " ")).append("\n")
            }
        }
        return res.toString()
    }

    private fun colorCodeIntoChar(colorCode: Short): Char {
        return when (colorCode) {
            in 0..25 -> '~'
            in 26..50 -> '#'
            in 51..76 -> '$'
            in 77..102 -> '!'
            in 103..127 -> '%'
            in 128..152 -> '&'
            in 153..178 -> '*'
            in 179..204 -> '@'
            in 205..230 -> ' '
            in 231..255 -> '+'
            else -> '`'
        }
    }

    private fun getStep(width: Int, height: Int): Pair<Int, Int> {
        val heightWanna = 16
        val widthWanna = 64
        return ((height / heightWanna) to (width / widthWanna))
    }
}