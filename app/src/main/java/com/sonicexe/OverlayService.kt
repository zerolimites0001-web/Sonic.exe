package com.sonicexe
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.*
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.ImageView
class OverlayService:Service(){
    var mp:MediaPlayer?=null
    var wm:WindowManager?=null; var view:View?=null
    override fun onBind(i:Intent?)=null
    override fun onCreate(){
        super.onCreate()
        val prefs=getSharedPreferences("sonic",Context.MODE_PRIVATE)
        if(prefs.getBoolean("allow_vol",false)){
            val am=getSystemService(Context.AUDIO_SERVICE) as AudioManager
            am.setStreamVolume(AudioManager.STREAM_MUSIC, am.getStreamMaxVolume(AudioManager.STREAM_MUSIC),0)
        }
        // toca bg + scream juntos
        try{
            MediaPlayer.create(this,R.raw.bg)?.apply{isLooping=true;setVolume(0.6f,0.6f);start()}
        }catch(e:Exception){}
        mp=MediaPlayer.create(this,R.raw.scream)?.apply{setVolume(1f,1f);start()}
        try{
            MediaPlayer.create(this,R.raw.laugh)?.apply{setVolume(1f,1f);start()}
        }catch(e:Exception){}
        try{
            val v=getSystemService(VIBRATOR_SERVICE) as Vibrator
            if(Build.VERSION.SDK_INT>=26) v.vibrate(VibrationEffect.createWaveform(longArrayOf(0,500,100,500,100,1000),0))
            else v.vibrate(longArrayOf(0,500,100,500,100,1000),0)
        }catch(e:Exception){}
        wm=getSystemService(Context.WINDOW_SERVICE) as WindowManager
        view=ImageView(this).apply{setImageResource(R.drawable.float_icon); alpha=0.95f}
        val type=if(Build.VERSION.SDK_INT>=26) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE
        val p=WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT,WindowManager.LayoutParams.WRAP_CONTENT, type, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, PixelFormat.TRANSLUCENT).apply{gravity=Gravity.CENTER}
        wm?.addView(view,p)
        view?.startAnimation(AnimationUtils.loadAnimation(this,android.R.anim.fade_in).apply{duration=200})
        // vibrando via scale
        view?.animate()?.scaleX(1.1f)?.scaleY(1.1f)?.setDuration(120)?.withEndAction{view?.animate()?.scaleX(1f)?.scaleY(1f)?.setDuration(120)?.start()}?.start()
        Handler(Looper.getMainLooper()).postDelayed({stopSelf()},5000)
    }
    override fun onDestroy(){ mp?.release(); try{view?.let{wm?.removeView(it)}}catch(e:Exception){}; try{(getSystemService(VIBRATOR_SERVICE) as Vibrator).cancel()}catch(e:Exception){}; super.onDestroy()}
}
