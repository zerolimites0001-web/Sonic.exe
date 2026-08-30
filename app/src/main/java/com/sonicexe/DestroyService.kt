package com.sonicexe
import android.app.*
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.*
import androidx.core.app.NotificationCompat
class DestroyService:Service(){
    var bg:MediaPlayer?=null; var mp:MediaPlayer?=null; var mp2:MediaPlayer?=null
    var count=0; val h=Handler(Looper.getMainLooper())
    override fun onBind(i:Intent?)=null
    override fun onCreate(){
        super.onCreate()
        try{
            val ch="sonic"
            if(Build.VERSION.SDK_INT>=26){ val nm=getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager; nm.createNotificationChannel(NotificationChannel(ch,"Sonic",NotificationManager.IMPORTANCE_LOW)) }
            startForeground(1, NotificationCompat.Builder(this,ch).setContentTitle("Sonic.exe").setContentText("Destruction em andamento...").setSmallIcon(R.drawable.float_icon).build())
        }catch(e:Exception){}
        val prefs=getSharedPreferences("sonic",Context.MODE_PRIVATE)
        if(prefs.getBoolean("allow_vol",false)){(getSystemService(Context.AUDIO_SERVICE) as AudioManager).setStreamVolume(AudioManager.STREAM_MUSIC, (getSystemService(Context.AUDIO_SERVICE) as AudioManager).getStreamMaxVolume(AudioManager.STREAM_MUSIC),0)}
        bg=MediaPlayer.create(this,R.raw.bg)?.apply{isLooping=true;setVolume(0.6f,0.6f);start()}
        loop()
    }
    fun loop(){
        if(count>=20){ stopSelf(); stopService(Intent(this,OverlayService::class.java)); return }
        count++
        try{
            val am=getSystemService(Context.AUDIO_SERVICE) as AudioManager
            if(getSharedPreferences("sonic",Context.MODE_PRIVATE).getBoolean("allow_vol",false)) am.setStreamVolume(AudioManager.STREAM_MUSIC, am.getStreamMaxVolume(AudioManager.STREAM_MUSIC),0)
            mp?.release(); mp2?.release()
            mp=MediaPlayer.create(this,R.raw.scream)?.apply{setVolume(1f,1f);start()}
            mp2=MediaPlayer.create(this,R.raw.laugh)?.apply{setVolume(1f,1f);start()}
            val v=getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if(Build.VERSION.SDK_INT>=26) v.vibrate(VibrationEffect.createWaveform(longArrayOf(0,500,100,500,100,1000),-1)) else v.vibrate(longArrayOf(0,500,100,500,100,1000),0)
        }catch(e:Exception){}
        h.postDelayed({loop()},1800)
    }
    override fun onDestroy(){ try{bg?.release()}catch(e:Exception){}; try{mp?.release()}catch(e:Exception){}; try{mp2?.release()}catch(e:Exception){}; try{(getSystemService(Context.VIBRATOR_SERVICE) as Vibrator).cancel()}catch(e:Exception){}; super.onDestroy()}
}
