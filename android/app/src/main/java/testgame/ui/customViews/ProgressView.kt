package testgame.ui.customViews

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.example.testgame.R

class ProgressView(context: Context, attrs: AttributeSet) : View(context, attrs) {

//    val player: Player

    override fun draw(canvas: Canvas?) {
        super.draw(canvas)

        val margin = 2f

        val borderPaint = Paint()
        borderPaint.color = ContextCompat.getColor(context, R.color.colorPrimaryDark)
        canvas?.drawRect(x, y, width.toFloat(), height.toFloat(), borderPaint)

        val healthPointPercentage = 0.7
        val healthWidth = ((width - margin * 2) * healthPointPercentage).toFloat()

        val healthPaint = Paint()
        healthPaint.color = ContextCompat.getColor(context, R.color.colorPrimary)

        canvas?.drawRect(x + margin, y + margin, healthWidth, height - margin * 2, healthPaint)
    }
}