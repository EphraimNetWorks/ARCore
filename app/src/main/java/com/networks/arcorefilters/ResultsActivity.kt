package com.networks.arcorefilters

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.VideoView
import com.bumptech.glide.Glide
import java.io.File

class ResultsActivity : AppCompatActivity() {

    private val filePath by lazy {
        intent.getStringExtra(EXTRA_PATH)?:""
    }

    private val isResultImage by lazy {
        intent.getBooleanExtra(EXTRA_TYPE,true)
    }

    private val resultsImageView by lazy{
        findViewById<ImageView>(R.id.results_image_view)
    }

    private val resultsVideoView by lazy{
        findViewById<VideoView>(R.id.results_video_view)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_results)

        if (isResultImage){
            Glide.with(resultsImageView)
                    .load(File(filePath))
                    .fitCenter()
                    .into(resultsImageView)
        }else{
            initializePlayer()
        }

    }

    override fun onPause() {
        super.onPause()
        pausePlayer()
    }

    override fun onStop() {
        super.onStop()
        releasePlayer()
    }

    private fun initializePlayer(){
        resultsVideoView.setVideoPath(filePath)
        resultsVideoView.start()
    }

    private fun releasePlayer(){
        resultsVideoView.stopPlayback()
    }

    private fun pausePlayer(){
        resultsVideoView.pause()
    }

    companion object{
        private const val EXTRA_PATH = "path"
        private const val EXTRA_TYPE = "type"
        fun newInstance(context: Context, resultsFilePath:String, isImage: Boolean): Intent {
            return Intent(context, ResultsActivity::class.java).apply {
                putExtra(EXTRA_PATH, resultsFilePath)
                putExtra(EXTRA_TYPE, isImage)
            }
        }
    }

}