package com.example.musicplayer


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent


import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlin.system.exitProcess


class NotificationReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            ApplicationClass.PREVIOUS -> prevNextsong(increment = false,context = context!!)
            ApplicationClass.PLAY -> if(PlayerActivity.isPlaying) pauseMusic() else playMusic()
            ApplicationClass.NEXT -> prevNextsong(increment = true,context = context!!)
            ApplicationClass.EXIT -> {
                PlayerActivity.musicService!!.stopForeground(true)
                PlayerActivity.musicService!!.mediaPlayer!!.release()
                PlayerActivity.musicService = null
                exitProcess(1)
            }
        }

    }

    private fun playMusic(){
        PlayerActivity.isPlaying = true
        PlayerActivity.musicService!!.mediaPlayer!!.start()
        PlayerActivity.musicService!!.showNotification(R.drawable.pause_icon)
        PlayerActivity.binding.playpausebtnPA.setIconResource(R.drawable.pause_icon)
    }

    private fun pauseMusic(){
        PlayerActivity.isPlaying = false
        PlayerActivity.musicService!!.mediaPlayer!!.pause()
        PlayerActivity.musicService!!.showNotification(R.drawable.play_icon)
        PlayerActivity.binding.playpausebtnPA.setIconResource(R.drawable.play_icon)
    }

    private  fun prevNextsong(increment: Boolean, context: Context?){
        setsongPosition(increment = increment)
        PlayerActivity.musicService!!.createmediaplayer()


        if (context != null) {
            Glide.with(context)
                .load(PlayerActivity.musicListPA[PlayerActivity.songPosition].arturi)
                .apply(RequestOptions().placeholder(R.drawable.sanju).centerCrop())
                .into(PlayerActivity.binding.songImagePA)
        }
        PlayerActivity.binding.songNamePA.text= PlayerActivity.musicListPA[PlayerActivity.songPosition].title
        playMusic()
    }

}

