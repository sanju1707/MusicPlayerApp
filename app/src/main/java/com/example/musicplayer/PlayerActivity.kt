package com.example.musicplayer

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.media.MediaPlayer
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.widget.SeekBar
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.musicplayer.databinding.ActivityPlayerBinding


class PlayerActivity : AppCompatActivity() , ServiceConnection,MediaPlayer.OnCompletionListener {
 // we can access the campanion object under any class
    companion object {
        lateinit var musicListPA: ArrayList<Music>
        var songPosition: Int = 0
        var isPlaying: Boolean = false
        var musicService: MusicService? = null
        @SuppressLint("StaticFieldLeak")
        lateinit var binding: ActivityPlayerBinding
        var repeat: Boolean = false
    }





    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // for starting service
        val intent = Intent(this,MusicService::class.java)
        bindService(intent,this, BIND_AUTO_CREATE)
        startService(intent)

        initializeLayout()
        binding.backbtnPA.setOnClickListener { finish() }
        binding.playpausebtnPA.setOnClickListener{
            if(isPlaying) pauseMusic()
            else playMusic()
        }
        binding.previousbtnPA.setOnClickListener {
            prevNextSong(increment = false)
        }

        binding.nextbtnPA.setOnClickListener {
            prevNextSong(increment = true)
        }

        binding.seekbarPA.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if(fromUser) {
                    musicService!!.mediaPlayer!!.seekTo(progress)
                    musicService!!.showNotification(if(isPlaying) R.drawable.pause_icon else R.drawable.play_icon)
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit
            override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
        })
        binding.repeatbtnPA.setOnClickListener{
            if(!repeat){
                repeat = true
                binding.repeatbtnPA.setColorFilter(ContextCompat.getColor(this, android.R.color.holo_red_dark))
            }
            else{
                repeat = false
                binding.repeatbtnPA.setColorFilter(ContextCompat.getColor(this, android.R.color.white))
            }

        }
    }
    private fun setlayout(){
        Glide.with(this)
            .load(musicListPA[songPosition].arturi)
            .apply(RequestOptions().placeholder(R.drawable.sanju).centerCrop())
            .into(binding.songImagePA)
        binding.songNamePA.text= musicListPA[songPosition].title
        if (repeat) binding.repeatbtnPA.setColorFilter(ContextCompat.getColor(this, android.R.color.holo_red_dark))
    }

    private fun initializeLayout(){
        songPosition=intent.getIntExtra("index",0)
        when(intent.getStringExtra("class")){
            "MusicAdapter" ->{
                musicListPA= ArrayList()
                musicListPA.addAll(MainActivity.MusicListMA)
                setlayout()



            }
            "MainActivity"->{
                musicListPA = ArrayList()
                musicListPA.addAll(MainActivity.MusicListMA)
                musicListPA.shuffle()
                setlayout()

            }


        }
    }
       private fun createmediaplayer(){
        try{
            if(musicService!!.mediaPlayer == null) musicService!!.mediaPlayer = MediaPlayer()
            musicService!!.mediaPlayer!!.reset()
            musicService!!.mediaPlayer!!.setDataSource(musicListPA[songPosition].path)
            musicService!!.mediaPlayer!!.prepare()
            musicService!!.mediaPlayer!!.start()
            isPlaying = true
            binding.playpausebtnPA.setIconResource(R.drawable.pause_icon)
            musicService!!.showNotification(R.drawable.pause_icon)
            binding.tvseekbarstart.text = formatDuration(musicService!!.mediaPlayer!!.currentPosition.toLong())
            binding.tvseekbarend.text = formatDuration(musicService!!.mediaPlayer!!.duration.toLong())
            binding.seekbarPA.progress = 0
            binding.seekbarPA.max = musicService!!.mediaPlayer!!.duration
            musicService!!.mediaPlayer!!.setOnCompletionListener(this)

        }catch (e:Exception){return}

    }


    private fun playMusic(){
        binding.playpausebtnPA.setIconResource(R.drawable.pause_icon)
        musicService!!.showNotification(R.drawable.pause_icon)
        isPlaying = true
        musicService!!.mediaPlayer!!.start()
    }
    private fun pauseMusic(){
        binding.playpausebtnPA.setIconResource(R.drawable.play_icon)
        musicService!!.showNotification(R.drawable.play_icon)
        isPlaying = false
        musicService!!.mediaPlayer!!.pause()
    }

    private fun prevNextSong(increment:Boolean){
        if(increment)
        {
            setsongPosition(increment=true)
            setlayout()
            createmediaplayer()
        }
        else
        {
            setsongPosition(increment=false)
            setlayout()
            createmediaplayer()
        }
    }


    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        val binder = service as MusicService.MyBinder
        musicService = binder.currentService()
        createmediaplayer()
        musicService!!.seekBarSetup()



    }

    override fun onServiceDisconnected(name: ComponentName?) {
        musicService = null
    }

    override fun onCompletion(mp: MediaPlayer?) {
        setsongPosition(increment = true)
        createmediaplayer()
        try {
            setlayout()
        }catch (e:Exception){return}
    }

}