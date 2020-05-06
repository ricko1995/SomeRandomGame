package com.ricko.ballsort

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.ViewPropertyAnimator
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintSet
import com.google.android.material.imageview.ShapeableImageView
import com.google.gson.Gson
import com.ricko.ballsort.LevelsActivity.Companion.REQUEST_CODE_FOR_ACTIVITY_RESULT
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    private val containers: ArrayList<Container> = ArrayList()
    private val displayMetrics = DisplayMetrics()
    private var maxScore = 0
    private var myScore = 0

    private var isReverseActive = false
    private lateinit var currentLevel: GameLevel

    private var numberOfContainers = 0
    private var containersInRow = 0
    private var animationDuration = 0L

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_FOR_ACTIVITY_RESULT) {
            if (resultCode == RESULT_OK) {
                currentLevel = Gson().fromJson(data!!.getStringExtra("level"), GameLevel::class.java)
                createLevelLayout()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        windowManager.defaultDisplay.getMetrics(displayMetrics)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }

        LevelsSingleObject.setActivity(this)

        showLevelsBtn.setOnClickListener {
            val intent = Intent(this, LevelsActivity::class.java)
            startActivityForResult(intent, REQUEST_CODE_FOR_ACTIVITY_RESULT)
        }

        currentLevel = LevelsSingleObject.getLastUnlockedLevel()
        createLevelLayout()

        nextLevelBtn.setOnClickListener {
            if (!isReverseActive) {
                currentLevel = LevelsSingleObject.getNextLevel(currentLevel)
                createLevelLayout()
            }
        }

        reverseBtn.setOnClickListener {
            isReverseActive = !isReverseActive
            reverseBtn.text = if (reverseBtn.text == "Reverse") "Play" else "Reverse"
            if (isReverseActive) {
                nextLevelBtn.visibility = GONE
                for (container in containers) {
                    if (container.markerView != null) {
                        container.markerView!!.startAnimation(
                            AnimationUtils.loadAnimation(
                                this,
                                R.anim.blink
                            )
                        )
                        if (container.numberOfClicksView != null) {
                            container.numberOfClicksView!!.startAnimation(
                                AnimationUtils.loadAnimation(
                                    this,
                                    R.anim.blink
                                )
                            )
                        }
                        container.markerView!!.setOnClickListener {
                            container.markerView!!.clearAnimation()
                            if (container.numberOfClicksView != null) {
                                container.numberOfClicksView!!.clearAnimation()
                            }
                            container.markerView!!.setOnClickListener(null)

                            container.containerView.animate().translationX(0f).withEndAction {
                                container.isFinished = false
                                container.isLastInRow = true
                                container.isSuccessful = false
                                container.numberOfClicks = 0
                                constraintLayout.removeView(container.markerView)
                                constraintLayout.removeView(container.numberOfClicksView)
                                container.numberOfClicksView = null
                                container.markerView = null
                                container.notClicked = true
                                setScore()
                                setLastInRow()

                            }

                        }
                    }
                }
            } else {
                if (isLevelOver()) nextLevelBtn.visibility = VISIBLE
                for (container in containers) {
                    if (container.markerView != null) {
                        container.markerView!!.clearAnimation()
                        if (container.numberOfClicksView != null) {
                            container.numberOfClicksView!!.clearAnimation()
                        }
                        container.markerView!!.setOnClickListener(null)
                    }
                }
            }
        }

    }

    private fun createLevelLayout() {
        if (isReverseActive) return
        nextLevelBtn.visibility = GONE
        if (constraintLayout.childCount > 0) {
            constraintLayout.removeAllViews()
            containers.clear()
        }
        maxScore = currentLevel.score
        myScore = 0

        numberOfContainers = currentLevel.numberOfContainers
        containersInRow =
            if (currentLevel.containersInRow < currentLevel.numberOfContainers) currentLevel.containersInRow else currentLevel.numberOfContainers
        animationDuration = 1000L    //TODO specific level generation
        showLevelsBtn.text = ("Level " + (currentLevel.levelNumber + 1).toString())
        generateLevel()
    }

    private fun generateLevel() {

        val numberOfRows = (numberOfContainers - 1) / containersInRow + 1
        for (i in 0 until numberOfContainers) {

            createContainerView(i, numberOfRows)
            addContainersToLayout(i)

            containers[i].containerView.setOnClickListener {

                if (containers[i].isLastInRow && !isReverseActive) {
                    if (containers[i].notClicked) {
                        containers[i].numberOfClicks++
                        val prevRot = it.rotation
                        it.animate().rotation(if (prevRot < 20f) 360f else 0f)
                            .translationXBy(displayMetrics.widthPixels.toFloat() / containersInRow.toFloat() + 1)
                            .withEndAction {
                                containers[i].isFinished = true
                                if (it.x >= displayMetrics.widthPixels - it.layoutParams.width / 2) {
                                    containers[i].isSuccessful = true
                                    addMarker(
                                        containers[i],
                                        R.drawable.ic_check_green_24dp
                                    )
                                    it.animate()
                                        .translationXBy(displayMetrics.widthPixels.toFloat())
                                        .duration = 500
                                } else {
                                    addMarker(
                                        containers[i],
                                        R.drawable.ic_close_red_24dp
                                    )
                                    it.animate()
                                        .translationXBy(displayMetrics.widthPixels.toFloat())
                                        .duration = 500
                                }
                                containers[i].notClicked = false
                                if (isLevelOver()) {
                                    openLevelOverDialog()
                                }
                                setLastInRow()
                            }.duration = animationDuration
                    }
                } else it.startAnimation(AnimationUtils.loadAnimation(this, R.anim.shake))
            }
        }

        setScore()
    }

    private fun createContainerView(loopIndex: Int, numberOfRows: Int) {

        val displayHeight = displayMetrics.heightPixels - 300
        val containerView = ShapeableImageView(this)
        containerView.setImageResource(R.drawable.ic_container_black_24dp)
        val containerColorType = TypedValue()
        theme.resolveAttribute(R.attr.colorOnSurface, containerColorType, true)
        containerView.setColorFilter(containerColorType.data)
        containerView.id = View.generateViewId()
        val containerWidth =
            displayMetrics.widthPixels / if (containersInRow < numberOfContainers) containersInRow else numberOfContainers
        val containerHeight =
            if (displayHeight > containerWidth * numberOfRows * 5 / 2) containerWidth * 5 / 2
            else displayHeight / numberOfRows


        containerView.layoutParams = ViewGroup.LayoutParams(containerWidth, containerHeight)
        containerView.scaleType = ImageView.ScaleType.FIT_XY
        containers.add(
            Container(
                containerView = containerView,
                column = loopIndex % containersInRow,
                row = loopIndex / containersInRow,
                isLastInRow = (loopIndex % containersInRow == containersInRow - 1 || loopIndex == numberOfContainers - 1)
            )
        )
    }

    private fun addContainersToLayout(loopIndex: Int) {

        constraintLayout.addView(containers[loopIndex].containerView, loopIndex)
        val constraintSet = ConstraintSet()
        constraintSet.clone(constraintLayout)
        if (containers[loopIndex].row == 0 && containers[loopIndex].column == 0) {
            constraintSet.connect(
                containers[loopIndex].containerView.id,
                ConstraintSet.START,
                ConstraintSet.PARENT_ID,
                ConstraintSet.START
            )
            constraintSet.connect(
                containers[loopIndex].containerView.id,
                ConstraintSet.TOP,
                ConstraintSet.PARENT_ID,
                ConstraintSet.TOP,
                50
            )
        } else if (containers[loopIndex].row == 0) {
            constraintSet.connect(
                containers[loopIndex].containerView.id,
                ConstraintSet.START,
                containers[loopIndex - 1].containerView.id,
                ConstraintSet.END
            )
            constraintSet.connect(
                containers[loopIndex].containerView.id,
                ConstraintSet.TOP,
                containers[loopIndex - 1].containerView.id,
                ConstraintSet.TOP
            )
        } else if (containers[loopIndex].column == 0) {
            constraintSet.connect(
                containers[loopIndex].containerView.id,
                ConstraintSet.START,
                ConstraintSet.PARENT_ID,
                ConstraintSet.START
            )
            constraintSet.connect(
                containers[loopIndex].containerView.id,
                ConstraintSet.TOP,
                containers[loopIndex - 1].containerView.id,
                ConstraintSet.BOTTOM
            )
        } else {
            constraintSet.connect(
                containers[loopIndex].containerView.id,
                ConstraintSet.START,
                containers[loopIndex - 1].containerView.id,
                ConstraintSet.END
            )
            constraintSet.connect(
                containers[loopIndex].containerView.id,
                ConstraintSet.TOP,
                containers[loopIndex - 1].containerView.id,
                ConstraintSet.TOP
            )
        }
        if (loopIndex == numberOfContainers - 1) {
            constraintSet.connect(
                containers[containers.size - 1].containerView.id,
                ConstraintSet.BOTTOM,
                ConstraintSet.PARENT_ID,
                ConstraintSet.BOTTOM,
                50
            )
        }


        constraintSet.applyTo(constraintLayout)

    }

    private fun addMarker(container: Container, markerType: Int) {
        val markerView = ImageView(this)
        val constraintSet = ConstraintSet()

        markerView.setImageResource(markerType)
        markerView.id = View.generateViewId()
        markerView.elevation = -10f
        val markerSize =
            if (container.containerView.layoutParams.height < container.containerView.layoutParams.width)
                container.containerView.layoutParams.height / 2
            else container.containerView.layoutParams.width / 2
        markerView.layoutParams = ViewGroup.LayoutParams(
            markerSize,
            markerSize
        )
        markerView.scaleType = ImageView.ScaleType.FIT_XY
        if (container.numberOfClicks > containersInRow - container.column) markerView.setColorFilter(
            Color.rgb(255, 165, 0)
        )
        container.markerView = markerView

        constraintLayout.addView(container.markerView!!)
        constraintSet.clone(constraintLayout)
        constraintSet.connect(
            container.markerView!!.id,
            ConstraintSet.START,
            container.containerView.id,
            ConstraintSet.START
        )
        constraintSet.connect(
            container.markerView!!.id,
            ConstraintSet.END,
            container.containerView.id,
            ConstraintSet.END
        )
        constraintSet.connect(
            container.markerView!!.id,
            ConstraintSet.TOP,
            container.containerView.id,
            ConstraintSet.TOP
        )
        constraintSet.connect(
            container.markerView!!.id,
            ConstraintSet.BOTTOM,
            container.containerView.id,
            ConstraintSet.BOTTOM
        )
        constraintSet.applyTo(constraintLayout)

        if (container.isSuccessful) {
            val numberOfClicks = TextView(this)
            numberOfClicks.text = container.numberOfClicks.toString()
            numberOfClicks.id = View.generateViewId()
            container.numberOfClicksView = numberOfClicks
            constraintLayout.addView(container.numberOfClicksView)
            constraintSet.clone(constraintLayout)
            constraintSet.connect(
                container.numberOfClicksView!!.id,
                ConstraintSet.START,
                container.containerView.id,
                ConstraintSet.START
            )
            constraintSet.connect(
                container.numberOfClicksView!!.id,
                ConstraintSet.END,
                container.containerView.id,
                ConstraintSet.END
            )
            constraintSet.connect(
                container.numberOfClicksView!!.id,
                ConstraintSet.TOP,
                container.containerView.id,
                ConstraintSet.TOP
            )
            constraintSet.connect(
                container.numberOfClicksView!!.id,
                ConstraintSet.BOTTOM,
                container.containerView.id,
                ConstraintSet.BOTTOM,
                60
            )
            constraintSet.applyTo(constraintLayout)
        }
    }

    private fun setLastInRow() {
        val groups = containers.iterator().asSequence().groupBy { it.row }
        for (row in groups) {
            var clickable = true
            val sorted = row.value.sortedByDescending { it.column }
            for (element in sorted) {
                if (!clickable) element.isLastInRow = false
                else if (!element.isFinished) {
                    clickable = false
                    element.isLastInRow = true
                }

            }
        }
    }

    private fun setScore() {
        var score = 0
        var count = 0
        containers.forEach {
            if (it.isFinished && it.isSuccessful) {
                count++
                score += (containersInRow - it.column) * (containersInRow - it.column) / it.numberOfClicks
            }
        }
        myScore = score
        scoreView.text = ("$score/$maxScore")
    }

    private fun isLevelOver(): Boolean {
        setScore()
        var isLevelOver = true
        containers.forEach { container ->
            if (!container.isFinished) {
                isLevelOver = false
                return@forEach
            }
        }
        return isLevelOver
    }

    private fun openLevelOverDialog() {
        currentLevel.passedWithScore = myScore
        LevelsSingleObject.saveLevel(currentLevel)
        toastMessage("You finished with score: " + currentLevel.passedWithScore.toString()) //TODO("Not yet implemented")
        if (!isReverseActive) nextLevelBtn.visibility = VISIBLE
    }

    private fun toastMessage(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }
}
