package testgame.ui.main.clicker

import android.view.animation.Interpolator
import kotlin.math.cos
import kotlin.math.pow

internal class BounceInterpolator(private val mAmplitude: Double, private val mFrequency: Double) : Interpolator {
    override fun getInterpolation(time: Float): Float {
        return (-1 * Math.E.pow(-time / mAmplitude) *
                cos(mFrequency * time) + 1).toFloat()
    }

}