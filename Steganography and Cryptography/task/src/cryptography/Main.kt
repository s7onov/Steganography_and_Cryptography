package cryptography

import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.experimental.xor

const val HIDE = "hide"
const val SHOW = "show"
const val EXIT = "exit"

fun main() {
    while (true) {
        println("Task ($HIDE, $SHOW, $EXIT):")
        val input = readln()
        when (input.lowercase()) {
            HIDE -> hide()
            SHOW -> show()
            EXIT -> { println("Bye!"); break }
            else -> println("Wrong task: $input")
        }
    }
}

fun show() {
    println("Input image file:")
    val inputFileName = readln()
    val bImage: BufferedImage?
    try {
        bImage = ImageIO.read(File(inputFileName))
    } catch (ex: Exception) {
        println("Can't read input file!")
        return
    }
    println("Password:")
    val password = readln()

    val array = mutableListOf<Byte>()
    val width = bImage.width
    val height = bImage.height

    var k = 0
    var t = 0
    loop@ for (i in 0 until height) {
        for (j in 0 until width) {
            val c = Color(bImage.getRGB(j, i))
            val x = k % 8
            val bit = c.blue % 2
            t = (t shl 1) + bit
            if (x == 7) {
                array.add(t.toByte());
                //println(" $bit")
                //println(t.toByte())
                t = 0;
            } //else print(" $bit")
            k++
            if (array.joinToString("").contains("003")) break@loop
        }
    }
    println("Message:\n${crypt(array.dropLast(3).toByteArray(), password.toByteArray()).toString(Charsets.UTF_8)}")
}

fun hide() {
    println("Input image file:")
    val inputFileName = readln()
    println("Output image file:")
    val outputFileName = readln()
    // println("Input Image: $inputFileName")
    // println("Output Image: $outputFileName")
    println("Message to hide:")
    val message = readln()
    println("Password:")
    val password = readln()
    val array = crypt(message.encodeToByteArray(), password.toByteArray()) + byteArrayOf(0, 0, 3)
    val bImage: BufferedImage?
    try {
        bImage = ImageIO.read(File(inputFileName))
    } catch (ex: Exception) {
        println("Can't read input file!")
        return
    }

    val width = bImage.width
    val height = bImage.height
    if (array.size * 8 > width * height) {
        println("The input image is not large enough to hold this message.")
        return
    }

    var k = 0
    loop@ for (i in 0 until height) {
        for (j in 0 until width) {
            val c = Color(bImage.getRGB(j, i))
            val x = k % 8
            val bit = (array[k / 8].toInt() shr 7 - x) % 2
            /*if (x == 7) println(" $bit")
            else print(" $bit")*/
            val r = c.red
            val g = c.green
            val b = if (bit == 0) c.blue and 0xfe else c.blue or 0x01
            bImage.setRGB(j, i, Color(r, g, b).rgb)
            k++
            if (k / 8 > array.size - 1) break@loop
        }
    }

    try {
        ImageIO.write(bImage, "png", File(outputFileName))
    } catch (ex: Exception) {
        println("Can't write output file!")
        return
    }
    println("Message saved in $outputFileName image.")
}

fun crypt(array: ByteArray, pass: ByteArray): ByteArray {
    for (i in array.indices)
        array[i] = array[i] xor pass[i % pass.size]
    return array
}

