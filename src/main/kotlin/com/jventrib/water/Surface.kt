package com.jventrib.water

import java.awt.*
import java.awt.event.*
import java.awt.geom.Ellipse2D
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import javax.swing.JPanel
import javax.swing.Timer


class Surface : JPanel(), ActionListener, MouseListener, MouseMotionListener, KeyListener {

    private var dropHeight: Float = 100f
    private val delay = 16
    internal lateinit var timer: Timer

    private val background = ImageIO.read(File("src/main/resources/chiba.jpg"))
    val screenWidth = background.width
    val screenHeight = background.height
    private val img = BufferedImage(screenWidth, screenHeight, BufferedImage.TYPE_INT_RGB)

    private var buffer1 = Array(screenWidth) { FloatArray(screenHeight) }
    private val buffer2 = Array(screenWidth) { FloatArray(screenHeight) }
    private var currentBuffer = buffer1
    private var previousBuffer = buffer2

    private var algo = this::gridA

    private val damping = 128

    init {
        initTimer()
        this.addMouseListener(this)
        this.addMouseMotionListener(this)
    }

    private fun initTimer() {
        timer = Timer(delay, this)
        timer.start()
    }


    private fun doDrawing(g: Graphics) {
        val g2d = g as Graphics2D

        g2d.paint = Color.blue

        for (x in 1..screenWidth - 2) {
            for (y in 1..screenHeight - 2) {
                val newValue = algo(x, y)
                val damped = newValue - newValue / damping
                currentBuffer[x][y] = damped
            }
        }


        for (x in 0 until screenWidth) {
            for (y in 0 until screenHeight) {
                val deltaX = currentBuffer[x][y] - currentBuffer[(x - 1).max(screenWidth)][y]
                val deltaY = currentBuffer[x][y] - currentBuffer[x][(y - 1).max(screenHeight)]
//                img.setRGB(x, y, toRgb((currentBuffer[x][y] + 128 + deltaX * 20).toInt()))
                val color = Color(
                    background.getRGB(
                        (x + deltaX.toInt()).max(screenWidth),
                        (y + deltaY.toInt()).max(screenHeight)
                    )
                )
                val delta = (deltaX.toInt() + deltaY.toInt()).bound(10)
                val red = (color.red + delta).max(256)
                val green = (color.green + delta).max(256)
                val blue = (color.blue + delta).max(256)
                img.setRGB(x, y, Color(red, green, blue).rgb)
            }
        }
        g2d.drawImage(img, 0, 0, null)
        g2d.color = Color.WHITE

        // Swap buffer
        val temp = currentBuffer
        currentBuffer = previousBuffer
        previousBuffer = temp
    }

    private fun gridA(x: Int, y: Int) = (
            +previousBuffer[x - 1][y]
                    + previousBuffer[x + 1][y]
                    + previousBuffer[x][y + 1]
                    + previousBuffer[x][y - 1]
            ) / 2 - currentBuffer[x][y]

    private fun gridB(x: Int, y: Int) = (
            +previousBuffer[x - 1][y]
                    + previousBuffer[x + 1][y]
                    + previousBuffer[x][y + 1]
                    + previousBuffer[x][y - 1]
            ) / 2 - currentBuffer[x][y]

    private fun gridC(x: Int, y: Int) = (
            +previousBuffer[x - 1][y - 1]
                    + previousBuffer[x - 1][y]
                    + previousBuffer[x - 1][y + 1]
                    + previousBuffer[x][y - 1]
                    + previousBuffer[x][y]
                    + previousBuffer[x][y + 1]
                    + previousBuffer[x + 1][y - 1]
                    + previousBuffer[x + 1][y]
                    + previousBuffer[x + 1][y + 1]
            ) / 5 - currentBuffer[x][y]

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        doDrawing(g)
    }

    override fun actionPerformed(e: ActionEvent?) {
        repaint()
    }

    override fun mouseClicked(e: MouseEvent) {

    }

    override fun mousePressed(e: MouseEvent) {
        dropWater(e, 100)
    }

    override fun mouseReleased(e: MouseEvent) {
    }

    override fun mouseEntered(e: MouseEvent) {

    }

    override fun mouseExited(e: MouseEvent) {
    }

    override fun mouseDragged(e: MouseEvent) {
        dropWater(e, 10)
    }

    override fun mouseMoved(e: MouseEvent) {
//        dropWater2(e, 10)

    }

    private fun dropWater2(e: MouseEvent, i: Int) {
        for (x in 0 until screenWidth) {
            for (y in 0 until screenHeight) {
                currentBuffer[x][y] = ((e.x - x) * (e.x - x) * 0.01f + (e.y - y) * (e.y - y) * 0.01f)
            }
        }
    }

    override fun keyTyped(e: KeyEvent) {
    }

    override fun keyPressed(e: KeyEvent) {
        println(e.keyCode)
        dropHeight = if (e.keyCode == 38) 100f else 30f
        if (e.keyCode == 49) algo = this::gridA
        if (e.keyCode == 50) algo = this::gridB
        if (e.keyCode == 51) algo = this::gridC
    }

    override fun keyReleased(e: KeyEvent) {
    }

    private fun dropWater(e: MouseEvent, size: Int) {

        val bufferedImage = fillCircle(size)

        for (x in 0 until size) {
            for (y in 0 until size) {
                val idxX = e.x + x - size / 2
                val idxY = e.y + y - size / 2
                val red = Color(bufferedImage.getRGB(x, y)).red
                if (red > 0) {
                    previousBuffer[idxX.max(screenWidth)][idxY.max(screenHeight)] =
                        red.toFloat() * 2
                }
//                if (bufferedImage.getRGB(x, y) == -1) {
//                    previousBuffer[idxX.trimmed(screenWidth)][idxY.trimmed(screenHeight)] = dropHeight
//                }
            }
        }
    }


    private fun fillCircle(size: Int): BufferedImage {
        val bufferedImage = BufferedImage(size, size, BufferedImage.TYPE_BYTE_GRAY)
        val g2d = bufferedImage.graphics as Graphics2D
//        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
//        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)

        g2d.color = Color.WHITE
        val center = Point(size / 2, size / 2)
        val dist = floatArrayOf(0f, 1f)
        val colors = arrayOf(
            Color(255, 255, 255),
            Color(0, 0, 0)
        )
        val rgp = RadialGradientPaint(center, size.toFloat() / 2, dist, colors)
        g2d.paint = rgp
        g2d.fill(Ellipse2D.Double(0.0, 0.0, size.toDouble(), size.toDouble()))

        return bufferedImage
    }
}

private fun Int.max(max: Int): Int {
    return when {
        this < 0 -> 0
        this >= max -> max - 1
        else -> this
    }
}

private fun Int.bound(max: Int): Int {
    return when {
        this < -max -> -max
        this >= max -> max - 1
        else -> this
    }
}
