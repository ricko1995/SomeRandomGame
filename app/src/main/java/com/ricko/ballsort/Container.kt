package com.ricko.ballsort

import android.widget.ImageView
import android.widget.TextView

data class Container(
    var markerView: ImageView? = null,
    val containerView: ImageView,
    val row: Int,
    val column: Int,
    var notClicked: Boolean = true,
    var isLastInRow: Boolean = false,
    var isFinished: Boolean = false,
    var numberOfClicks: Int = 0,
    var isSuccessful: Boolean = false,
    var numberOfClicksView: TextView? = null
) {
}