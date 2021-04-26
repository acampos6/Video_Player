package com.example.video_player

import android.app.Activity
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException
import java.lang.IllegalStateException


class MainActivity : AppCompatActivity(), SurfaceHolder.Callback, SeekBar.OnSeekBarChangeListener,
    MediaPlayer.OnPreparedListener{
    @FunctionalInterface
    interface Runnable

    private val player = MediaPlayer()
    private lateinit var runnable: java.lang.Runnable
    private var handler = Handler(Looper.getMainLooper())
    private lateinit var videoSourceFileUri: Uri


    val SEC = 1000

    //Use to get timing
    private val MediaPlayer.seconds: Int
        get() {
            return this.duration / SEC
        }
    private val MediaPlayer.currentSeconds: Int
        get() {
            return this.currentPosition / SEC
        }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //To Keep screen one while playing video
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        player.setOnPreparedListener(this)
        video_View.holder.addCallback(this)
        seek_bar.setOnSeekBarChangeListener(this)
        play_button.isEnabled = false
        pause_button.isEnabled = false
        stop_button.isEnabled = false

        play_button.setOnClickListener {
            player.start()
        }
        pause_button.setOnClickListener {
            if(player.isPlaying) {
                player.pause()
            }
        }
        stop_button.setOnClickListener {
            if(player.isPlaying) {
                player.stop()
                player.reset()
                try {
                    player.prepare()
                }catch (e: IllegalStateException){
                    e.printStackTrace()
                }catch (e: IOException){
                    e.printStackTrace()
                }
                player.seekTo(0)
            }
            else{
                player.stop()
                player.reset()
                player.seekTo(0)
            }

        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.app_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    //Selecting the video to be displayed
    override fun surfaceCreated(surfaceHolder: SurfaceHolder) {
        player.apply {
            setDataSource(applicationContext, videoSourceFileUri)
            setDisplay(surfaceHolder)
            prepareAsync()
        }
    }


    override fun onPrepared(mP: MediaPlayer?) {
        initializeSeekBar()
        updateSeekBar()
    }
    /*Formatting time for video*/
    private fun timeString(seconds: Int): String {
        return String.format("%02d:%02d",
            (seconds / 3600 * 60 + ((seconds % 3600) / 60)), (seconds % 60)
        )
    }

    private fun updateSeekBar() {
        runnable = Runnable {

            text_progress.text = timeString(player.currentSeconds)
            seek_bar.progress = player.currentSeconds
            handler.postDelayed(runnable, SEC.toLong())

        }
        handler.postDelayed(runnable, SEC.toLong())

    }
    // Creating SeekerBar Initialization
    private fun initializeSeekBar() {
        seek_bar.max = player.seconds
        text_progress.text = "00:00"
        text_total_progress.text = timeString(player.seconds)
        progress_bar.visibility = View.GONE
        play_button.isEnabled = true
        pause_button.isEnabled = true
        stop_button.isEnabled = true
    }

    override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
        if (p2)
            player.seekTo(p1 * SEC)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        return when (item?.itemId){
            R.id.dir ->{
                val intent = Intent()
                intent.type = "video/*"
                intent.action = Intent.ACTION_GET_CONTENT
                startActivityForResult(
                    Intent.createChooser(
                        intent,
                        "Select Video")
                    , 123)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 123) {
                videoSourceFileUri = data?.data!!
                video_View.holder.addCallback(this)
            }
        }
    }

    override fun onDestroy(){
        handler.removeCallbacks(runnable)
        player.release()
        super.onDestroy()

    }

    override fun onStartTrackingTouch(p0: SeekBar?) {
    }

    override fun onStopTrackingTouch(p0: SeekBar?) {
    }

    override fun surfaceChanged(p0: SurfaceHolder?, p1: Int, p2: Int, p3: Int) {
    }

    override fun surfaceDestroyed(p0: SurfaceHolder?) {
    }
}


