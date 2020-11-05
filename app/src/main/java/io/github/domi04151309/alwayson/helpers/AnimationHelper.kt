package io.github.domi04151309.alwayson.helpers

import android.os.Handler
import android.os.Looper
import android.view.View

internal object AnimationHelper {

    private const val FRAME_RATE: Long = 15

    fun animateView(view: View, positionY: Float, duration: Long) {
        var deltaTime: Long
        val startTime = System.currentTimeMillis()
        var oldTime = startTime
        val motionSection = (positionY - view.translationY) * (1 / (duration.toFloat()))
        val animationHandler = Handler(Looper.getMainLooper())
        val animationRunnable = object : Runnable {
            override fun run() {
                deltaTime = System.currentTimeMillis() - oldTime
                oldTime = System.currentTimeMillis()
                view.translationY = view.translationY + motionSection * deltaTime
                if (duration > (oldTime - startTime)) animationHandler.postDelayed(this, 1000 / FRAME_RATE)
            }
        }
        animationHandler.postDelayed(animationRunnable, 1000 / FRAME_RATE)
    }
}