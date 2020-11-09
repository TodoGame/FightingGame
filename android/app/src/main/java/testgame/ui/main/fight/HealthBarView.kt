package testgame.ui.main.fight

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.example.testgame.R


class HealthBarView(context: Context, attrs: AttributeSet) : View(context, attrs) {

//    val player: Player

    override fun draw(canvas: Canvas?) {
        super.draw(canvas)

        val x = 30f
        val y = 30f
        val height = 20f
        val width = 100f
        val margin = 2f
        val borderLeft = x - width / 2
        val borderRight = x + width / 2
        val borderBottom = y
        val borderTop = borderBottom - height

        val borderPaint = Paint()
        val borderColor = ContextCompat.getColor(context, R.color.colorPrimary)
        borderPaint.color = borderColor
        val healthPaint = Paint()
        val healthColor = ContextCompat.getColor(context, R.color.colorAccent)
        healthPaint.setColor(healthColor)

//        val healthPointPercentage = player.currentHealth / player.maxHealth
        val healthPointPercentage = 30

        val healthWidth = width - 2 * margin
        val healthHeight = height - 2 * margin
        val healthLeft = borderLeft + margin
        val healthRight = healthLeft + healthWidth*healthPointPercentage
        val healthBottom = borderBottom - margin
        val healthTop = healthBottom - healthHeight

        canvas?.drawRect(borderLeft, borderTop, borderRight, borderBottom, borderPaint)
        canvas?.drawRect(healthLeft, healthTop, healthRight, healthBottom, healthPaint)
    }
}