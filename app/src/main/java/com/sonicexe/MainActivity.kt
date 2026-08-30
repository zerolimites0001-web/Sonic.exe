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
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
class MainActivity:AppCompatActivity(){
    var mp:MediaPlayer?=null
    val prefs by lazy{getSharedPreferences("sonic",Context.MODE_PRIVATE)}
    fun allowVol():Boolean=prefs.getBoolean("allow_vol",false)
    fun hasAsked():Boolean=prefs.getBoolean("asked",false)
    fun askVol(cb:()->Unit){
        if(hasAsked()){cb();return}
        AlertDialog.Builder(this).setTitle("Controle de volume").setMessage("Permitir que Sonic.exe controle o volume de mídia durante os efeitos?").setPositiveButton("Permitir"){_,_->prefs.edit().putBoolean("asked",true).putBoolean("allow_vol",true).apply();cb()}.setNegativeButton("Não permitir"){_,_->prefs.edit().putBoolean("asked",true).putBoolean("allow_vol",false).apply();cb()}.show()
    }
    var bgMp:MediaPlayer?=null
    fun play(id:Int, loop:Boolean=false){
        try{
            if(allowVol()){(getSystemService(Context.AUDIO_SERVICE) as AudioManager).setStreamVolume(AudioManager.STREAM_MUSIC, (getSystemService(Context.AUDIO_SERVICE) as AudioManager).getStreamMaxVolume(AudioManager.STREAM_MUSIC),0)}
            mp?.release(); mp=MediaPlayer.create(this,id)?.apply{isLooping=loop;setVolume(1f,1f);start()}
        }catch(e:Exception){}
    }
    fun ensureBg(){ if(bgMp==null){ bgMp=MediaPlayer.create(this,R.raw.bg)?.apply{isLooping=true;setVolume(0.6f,0.6f);start()} } }
    fun vibrate(pattern:LongArray){
        try{(getSystemService(VIBRATOR_SERVICE) as Vibrator).vibrate(VibrationEffect.createWaveform(pattern,-1))}catch(e:Exception){}
    }
    override fun onCreate(b:Bundle?){
        super.onCreate(b); setContentView(R.layout.activity_main)
        val prog=findViewById<ProgressBar>(R.id.prog); val log=findViewById<TextView>(R.id.log)
        fun repeat20(action:()->Unit){ var c=0; fun loop(){ if(c>=20){finishAffinity(); return}; c++; action(); Handler(Looper.getMainLooper()).postDelayed({loop()},2200)}; loop() }
        findViewById<Button>(R.id.btnPurge).setOnClickListener{
            askVol{
                ensureBg(); prog.visibility=ProgressBar.VISIBLE; log.visibility=TextView.VISIBLE
                repeat20{
                    log.text="> SYSTEM PURGE [${'$'}{20-log.text.lines().size}]...\n"
                    play(R.raw.scream); vibrate(longArrayOf(0,300,80,500,80,500))
                    var p=0; prog.progress=0; val h=Handler(Looper.getMainLooper()); val r=object:Runnable{override fun run(){p+=20; prog.progress=p; if(p<100) h.postDelayed(this,120)}}
                    h.post(r)
                }
            }
        }
        findViewById<Button>(R.id.btnVoid).setOnClickListener{
            askVol{
                ensureBg(); prog.visibility=ProgressBar.VISIBLE; log.visibility=TextView.VISIBLE
                repeat20{
                    log.text="> VOID ARCHIVE loop...\n"
                    play(R.raw.laugh); vibrate(longArrayOf(0,200,80,200,80,900))
                    var p=0; prog.progress=0; val h=Handler(Looper.getMainLooper()); val r=object:Runnable{override fun run(){p+=20; prog.progress=p; if(p<100) h.postDelayed(this,120)}}
                    h.post(r)
                }
            }
        }
        findViewById<Button>(R.id.btnOverlay).setOnClickListener{
            if(!Settings.canDrawOverlays(this)){ AlertDialog.Builder(this).setTitle("Sobrepor outros apps").setMessage("Permitir que Sonic.exe mostre um efeito flutuante por 5s?").setPositiveButton("Permitir"){_,_-> startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))}.setNegativeButton("Cancelar",null).show(); return@setOnClickListener}
            askVol{ startService(Intent(this,OverlayService::class.java)) }
        }
        // bg sempre tocando
        ensureBg()
    }
    override fun onDestroy(){mp?.release(); bgMp?.release(); super.onDestroy()}
}
