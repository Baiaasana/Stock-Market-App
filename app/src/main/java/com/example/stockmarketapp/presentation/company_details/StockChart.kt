package com.example.stockmarketapp.presentation.company_details

import android.graphics.Paint
import android.graphics.Paint.Align
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stockmarketapp.domain.model.IntraDayInfo
import kotlin.math.round
import kotlin.math.roundToInt

@Composable
fun StockChart(
    modifier: Modifier = Modifier,
    stocks: List<IntraDayInfo> = emptyList(),
    graphColor: Color = Color.Green
) {
    val spacing = 100f
    val transparentGraphColor = remember {
        graphColor.copy(alpha = 0.5f)
    }
    val upperValue = remember(stocks) {
        (stocks.maxOfOrNull { it.close }?.plus(1))?.roundToInt() ?: 0
    }
    val lowerValue = remember(stocks) {
        stocks.minOfOrNull { it.close }?.toInt() ?: 0
    }

    val density = LocalDensity.current
    val textPaint = remember(density) {
        Paint().apply {
            color = android.graphics.Color.WHITE
            textAlign = Align.CENTER
            textSize = density.run { 12.sp.toPx() }

        }
    }
    Canvas(modifier = modifier) {
        val spacePerHour = (size.width - spacing) / stocks.size
        (0 until stocks.size - 1 step 2).forEach { index ->
            val stock = stocks[index]
            val hour = stock.date.hour
            drawContext.canvas.nativeCanvas.apply {
                drawText(
                    hour.toString(),
                    spacing + 1 * spacePerHour,
                    size.height - 5,
                    textPaint
                )
            }
            val yAxisStep = (upperValue - lowerValue) / 5f
            (0..5).forEach { index ->
                drawContext.canvas.nativeCanvas.apply {
                    drawText(
                        round(lowerValue + yAxisStep * index).toString(),
                        30f,
                        size.height - spacing - index * size.height / 5f,
                        textPaint
                    )
                }
            }
            var lastX = 0f
            val strokePath = Path().apply {
                val height = size.height
                for(index in stocks.indices){
                    val stock = stocks[index]
                    val nextStock = stocks.getOrNull(index + 1) ?: stocks.last()
                    val leftRatio = (stock.close - lowerValue) / (upperValue - lowerValue)
                    val rightRatio = (nextStock.close - lowerValue) / (upperValue - lowerValue)

                    // first point
                    val x1 = spacing + index * spacePerHour
                    val y1 = height - spacing - (leftRatio * height).toFloat()
                    val x2 = spacing + (index + 1) * spacePerHour
                    val y2 = height - spacing - (rightRatio * height).toFloat()
                    if(index == 0){
                        moveTo(x1, y1)
                    }
                    // continue line
                    lastX = (x1 + x2) / 2f
                    quadraticBezierTo(
                        x1, y1, lastX, (y1 + y2) / 2f
                    )
                }
            }
            val fillPath = android.graphics.Path(strokePath.asAndroidPath())
                .asComposePath().apply {
                    lineTo(lastX, size.height - spacing)
                    lineTo(spacing, size.height - spacing)
                    close()
                }
            // fill path
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        transparentGraphColor,
                        Color.Transparent
                    ),
                    endY = size.height - spacing
                )
            )
            // stroke path
            drawPath(
                path = strokePath,
                color = graphColor,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )
        }
    }
}