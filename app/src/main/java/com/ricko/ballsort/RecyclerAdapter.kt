package com.ricko.ballsort

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.level_card_layout.view.*

class RecyclerAdapter(
    private val gameLevels: ArrayList<GameLevel>,
    private val levelClicksInterface: LevelClicksInterface
) :
    RecyclerView.Adapter<RecyclerAdapter.LevelItemHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LevelItemHolder {
        return LevelItemHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.level_card_layout, parent, false)
        )
    }

    override fun getItemCount() = gameLevels.size

    override fun onBindViewHolder(holder: LevelItemHolder, position: Int) {
        holder.bind(gameLevels[position], levelClicksInterface)
    }

    class LevelItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val levelNumberTxt = itemView.levelNumberTxt
        private val levelCard = itemView.levelCard
        private val ratingBar = itemView.ratingBar

        fun bind(gameLevel: GameLevel, levelClicksInterface: LevelClicksInterface) {
            val txt = if (gameLevel.isUnlocked) (gameLevel.levelNumber + 1).toString() else "\uD83D\uDD12"
            levelNumberTxt.text = txt
            val isRatingBarVisible = if (gameLevel.isUnlocked) VISIBLE else GONE
            ratingBar.visibility = isRatingBarVisible
            ratingBar.rating = if (gameLevel.passedWithScore != null) gameLevel.passedWithScore!!.toFloat()
                .div(gameLevel.score.toFloat()) * 5f else 0f

            levelCard.setOnClickListener {
                if (gameLevel.isUnlocked) levelClicksInterface.onLevelClick(gameLevel)
                else it.startAnimation(
                    AnimationUtils.loadAnimation(levelClicksInterface as Activity, R.anim.shake)
                )
            }
        }
    }

    interface LevelClicksInterface {
        fun onLevelClick(gameLevel: GameLevel)
    }
}


