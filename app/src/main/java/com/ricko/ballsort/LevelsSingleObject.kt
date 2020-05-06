package com.ricko.ballsort

import android.app.Activity
import android.content.Context.MODE_PRIVATE
import com.google.gson.Gson
import kotlin.math.ceil
import kotlin.math.floor

object LevelsSingleObject {
    private const val maxNumberOfContainers = 81
    private const val maxContainersInRow = 9
    private const val startWith = 1
    private lateinit var activity: Activity

    fun setActivity(act: Activity) {
        activity = act
    }

    private fun createLevelsUsingMath(): ArrayList<GameLevel> {
        val containersInRowArr = IntArray(maxContainersInRow)
        val numberOfContainersArr = IntArray(maxNumberOfContainers)
        val gl: ArrayList<GameLevel> = ArrayList()
        for (c in containersInRowArr.withIndex()) {
            containersInRowArr[c.index] = c.index + startWith
        }
        for (c in numberOfContainersArr.withIndex()) {
            numberOfContainersArr[c.index] = c.index + startWith
        }
        numberOfContainersArr.forEach { noOfCont ->
            containersInRowArr.forEach { contInRow ->
                val numberOfWholeRows =
                    (if (noOfCont.toFloat() / contInRow.toFloat() < 1) ceil(noOfCont.toFloat() / contInRow.toFloat()) else floor(
                        noOfCont.toFloat() / contInRow.toFloat()
                    )).toInt()
                val numberOfContainersInPartialRow = noOfCont % contInRow
                val scoreOfWholeRows = (contInRow * (contInRow + 1) / 2 * numberOfWholeRows)
                val scoreOfPartialRows =
                    contInRow * (contInRow + 1) / 2 - (contInRow - numberOfContainersInPartialRow) * (contInRow - numberOfContainersInPartialRow + 1) / 2
                val score = scoreOfWholeRows + scoreOfPartialRows
                val temp = floor((noOfCont.toFloat() - 1f) / contInRow.toFloat() + 1f)
                if (noOfCont >= contInRow && temp < 10 && contInRow > 1) {
                    gl.add(GameLevel(0, contInRow, noOfCont, score))
                }
            }
        }
        gl.sortBy { it.score }
        var lvlNo = -1
        gl.forEach {
            lvlNo++
            it.levelNumber = lvlNo
        }

        return gl
    }

    private fun getLevelsFromStorage(): ArrayList<GameLevel> {
        val gl: ArrayList<GameLevel> = ArrayList()
        for (i in 0 until createLevelsUsingMath().size) {
            val sp = activity.getSharedPreferences("levels", MODE_PRIVATE).getString("$i", null)
            if (sp != null) {
                val level = Gson().fromJson(sp, GameLevel::class.java)
                gl.add(level)
            } else break
        }
        return gl
    }

    private fun mergeLevels(
        createdLevels: ArrayList<GameLevel>,
        levelsFromStorage: ArrayList<GameLevel>
    ): ArrayList<GameLevel> {
        if (levelsFromStorage.size > 0) {
            for (levelFromStorage in levelsFromStorage.withIndex()) {
                createdLevels[levelFromStorage.index] = levelFromStorage.value
            }
        }
        return createdLevels
    }

    fun getGameLevels(): ArrayList<GameLevel> {
        val gl = mergeLevels(createLevelsUsingMath(), getLevelsFromStorage())
        gl[0].isUnlocked = true
        return gl
    }

    fun getNextLevel(currentLevel: GameLevel? = null): GameLevel {
        return if (currentLevel == null) getGameLevels()[0]
        else getGameLevels()[getGameLevels().indexOf(currentLevel) + 1]
    }

    fun saveLevel(gameLevel: GameLevel) {
        activity.getSharedPreferences("levels", MODE_PRIVATE).edit()
            .putString(gameLevel.levelNumber.toString(), Gson().toJson(gameLevel)).apply()
        val nextLevel = getGameLevels()[getGameLevels().indexOf(gameLevel) + 1]
        nextLevel.isUnlocked = true
        activity.getSharedPreferences("levels", MODE_PRIVATE).edit()
            .putString(nextLevel.levelNumber.toString(), Gson().toJson(nextLevel)).apply()
    }

    fun getLastUnlockedLevel(): GameLevel {
        return if (getLevelsFromStorage().size > 0) getLevelsFromStorage()[getLevelsFromStorage().lastIndex] else getNextLevel()
    }
}