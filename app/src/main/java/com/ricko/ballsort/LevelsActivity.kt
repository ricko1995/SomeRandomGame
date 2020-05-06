package com.ricko.ballsort

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.WindowManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_levels.*
import kotlinx.android.synthetic.main.level_card_layout.*
import java.lang.reflect.Type
import kotlin.math.floor

class LevelsActivity : AppCompatActivity(), RecyclerAdapter.LevelClicksInterface {

    private lateinit var recyclerAdapter: RecyclerAdapter
    private lateinit var gridLayoutManager: GridLayoutManager
    private val displayMetrics = DisplayMetrics()

    companion object {
        var gameLevels: ArrayList<GameLevel> = ArrayList()
        const val REQUEST_CODE_FOR_ACTIVITY_RESULT = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_levels)

        windowManager.defaultDisplay.getMetrics(displayMetrics)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }

        LevelsSingleObject.setActivity(this)
        gameLevels = LevelsSingleObject.getGameLevels()

        val cardWidth = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            88f,
            resources.displayMetrics
        ) //80dp is card width and margin of 4dp on each side
        val spanCount = floor(resources.displayMetrics.widthPixels.toFloat() / cardWidth).toInt()

        gridLayoutManager =
            GridLayoutManager(applicationContext, spanCount, LinearLayoutManager.VERTICAL, false)
        recyclerAdapter = RecyclerAdapter(gameLevels, this)
        levelsRecyclerView.layoutManager = gridLayoutManager
        levelsRecyclerView.adapter = recyclerAdapter
    }

    override fun onLevelClick(gameLevel: GameLevel) {
        val intent = Intent()
        intent.putExtra("level", Gson().toJson(gameLevel))
        setResult(RESULT_OK, intent)
        finish()
    }
}
