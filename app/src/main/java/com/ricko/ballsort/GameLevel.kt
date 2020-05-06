package com.ricko.ballsort

data class GameLevel(
    var levelNumber: Int = 0,
    val containersInRow: Int,
    val numberOfContainers: Int,
    val score: Int,
    var passedWithScore: Int? = null,
    var isUnlocked: Boolean = false
) {
}