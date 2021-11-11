package com.jventrib.water

import java.awt.EventQueue
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.JFrame
import javax.swing.Timer


class Demo : JFrame() {

    init {
        val surface = Surface()
        add(surface)

        addKeyListener(surface)
        addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent?) {
                val timer: Timer = surface.timer
                timer.stop()
            }
        })

        title = "Water"
        setSize(surface.screenWidth, surface.screenHeight)
        setLocationRelativeTo(null)
        defaultCloseOperation = EXIT_ON_CLOSE
    }



}

fun main() {
    EventQueue.invokeLater(Runnable {
        val ex = Demo()
        ex.setVisible(true)
    })
}