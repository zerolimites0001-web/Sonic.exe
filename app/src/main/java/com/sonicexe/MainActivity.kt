package com.sonicexe
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
class MainActivity:AppCompatActivity(){
    var mp:MediaPlayer?=null; var bgMp:MediaPlayer?=null
    val prefs by lazy{getSharedPreferences("sonic",Context.MODE_PRIVATE)}
    fun allowVol():Boolean=prefs.getBoolean("allow_vol",false)
    fun hasAsked():Boolean=prefs.getBoolean("asked",false)
    fun askVol(cb:()->Unit){
        if(hasAsked()){cb();return}
        AlertDialog.Builder(this).setTitle("Controle de volume").setMessage("Permitir que Sonic.exe aumente o volume durante o efeito?").setPositiveButton("Permitir"){_,_->prefs.edit().putBoolean("asked",true).putBoolean("allow_vol",true).apply();cb()}.setNegativeButton("Não permitir"){_,_->prefs.edit().putBoolean("asked",true).putBoolean("allow_vol",false).apply();cb()}.setCancelable(false).show()
    }
    fun maxVol(){
        if(!allowVol()) return
        try{
            val am=getSystemService(Context.AUDIO_SERVICE) as AudioManager
            am.setStreamVolume(AudioManager.STREAM_MUSIC, am.getStreamMaxVolume(AudioManager.STREAM_MUSIC),0)
        }catch(e:Exception){}
    }
    var mp2:MediaPlayer?=null
    fun playAll(){
        try{
            maxVol()
            mp?.release(); mp2?.release()
            mp=MediaPlayer.create(this,R.raw.scream)?.apply{setVolume(1f,1f);isLooping=false;start()}
            mp2=MediaPlayer.create(this,R.raw.laugh)?.apply{setVolume(1f,1f);isLooping=false;start()}
            // bg já tocando
        }catch(e:Exception){}
    }
    fun ensureBg(){
        if(bgMp==null){
            try{ bgMp=MediaPlayer.create(this,R.raw.bg)?.apply{isLooping=true;setVolume(0.6f,0.6f);start()} }catch(e:Exception){}
        }
    }
    fun vibrate(pattern:LongArray){
        try{
            val v=getSystemService(VIBRATOR_SERVICE) as Vibrator
            if(Build.VERSION.SDK_INT>=26) v.vibrate(VibrationEffect.createWaveform(pattern,-1))
            else v.vibrate(pattern, -1)
        }catch(e:Exception){}
    }
    override fun onCreate(b:Bundle?){
        super.onCreate(b); setContentView(R.layout.activity_main)
        val prog=findViewById<ProgressBar>(R.id.prog); val log=findViewById<TextView>(R.id.log)
        // pergunta volume logo no início (só 1 vez)
        if(!hasAsked()) askVol{ ensureBg() } else ensureBg()

        findViewById<Button>(R.id.btnPurge).setOnClickListener{
            askVol{
                ensureBg()
                prog.visibility=ProgressBar.VISIBLE; log.visibility=TextView.VISIBLE; log.text="> START DESTRUCTION...\n"
                var count=0
                val handler=Handler(Looper.getMainLooper())
                val runnable=object:Runnable{
                    override fun run(){
                        if(count>=20){ finishAffinity(); return }
                        count++
                        log.append("Pulse $count/20 - TODOS!\n")
                        maxVol()
                        playAll()
                        vibrate(longArrayOf(0,500,100,500,100,1000))
                        prog.progress = (count*100/20)
                        handler.postDelayed(this, 1800)
                    }
                }
                handler.post(runnable)
            }
        }

        findViewById<Button>(R.id.btnOverlay).setOnClickListener{
            if(!Settings.canDrawOverlays(this)){
                AlertDialog.Builder(this).setTitle("Sobrepor outros apps").setMessage("Permitir que Sonic.exe mostre um efeito flutuante por 5s?").setPositiveButton("Permitir"){_,_-> startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))}.setNegativeButton("Cancelar",null).show(); return@setOnClickListener
            }
            askVol{ startService(Intent(this,OverlayService::class.java)) }
        }
    }
    override fun onDestroy(){ try{mp?.release()}catch(e:Exception){}; try{mp2?.release()}catch(e:Exception){}; try{bgMp?.release()}catch(e:Exception){}; super.onDestroy()}
}
