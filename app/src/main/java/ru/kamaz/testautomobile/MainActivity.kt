package ru.kamaz.testautomobile

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    var can: ru.kamaz.can.api.CanMessenger ?= null
    var work = false
    var objects = mutableListOf<androidx.constraintlayout.widget.ConstraintLayout>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        getSupportActionBar()?.hide()
        bindService(createExplicitIntent(),sercon, Context.BIND_AUTO_CREATE)
        objects.add(findViewById(R.id.view1))
        objects.add(findViewById(R.id.view2))
        objects.add(findViewById(R.id.view3))
        objects.add(findViewById(R.id.view4))
        objects.add(findViewById(R.id.view5))
        objects.add(findViewById(R.id.view6))

        for(i in 0..5){
            objects[i].findViewById<ProgressBar>(R.id.progressBar).visibility = View.INVISIBLE
            when(i){
                0->{
                    objects[i].findViewById<TextView>(R.id.name).setText("N двиг., об.мин")
                    objects[i].findViewById<TextView>(R.id.type).setText("(обороты в двигателе от 0 до 2500)")
                }
                1->{
                    objects[i].findViewById<TextView>(R.id.name).setText("% ППТ")
                    objects[i].findViewById<TextView>(R.id.type).setText("(процент нажатия на педаль подачи топлива от 0 до 100%)")
                }
                2->{
                    objects[i].findViewById<TextView>(R.id.name).setText("% ПТ")
                    objects[i].findViewById<TextView>(R.id.type).setText("(процент нажатия на педаль тормоза от 0 до 100%)")
                }
                3->{
                    objects[i].findViewById<TextView>(R.id.name).setText("Т ожд, °C")
                    objects[i].findViewById<TextView>(R.id.type).setText("(температура охлаждения жидкости в двигателе)")
                }
                4->{
                    objects[i].findViewById<TextView>(R.id.name).setText("Т м, °C")
                    objects[i].findViewById<TextView>(R.id.type).setText("(температура масла в двигателе)")
                }
                5->{
                    objects[i].findViewById<TextView>(R.id.name).setText("Р нд,")
                    objects[i].findViewById<TextView>(R.id.type).setText("(давление наддува в двиг.)")
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        objects[i].findViewById<ProgressBar>(R.id.progressBar).min = 0
                        objects[i].findViewById<ProgressBar>(R.id.progressBar).max = 500
                    }
                }
            }
        }

        objects[5].findViewById<ProgressBar>(R.id.progressBar).visibility = View.VISIBLE

    }



    override fun onStart() {
        work = true
        coroutine()
        super.onStart()
    }

    override fun onStop() {
        work = false
        super.onStop()
    }

    fun coroutine(){
        CoroutineScope(Dispatchers.Main).launch {
            while (work){
                for(i in 0..5){
                    objects[0].findViewById<TextView>(R.id.num).setText(String.format("%.1f", can?.receive(0x0CF00400,4,1,16,0.125f,0f)?.data))
                    objects[1].findViewById<TextView>(R.id.num).setText(can?.receive(0x0CF00300,2,1,8,0.4f,0f)?.data!!.toInt().toString())
                    objects[2].findViewById<TextView>(R.id.num).setText(can?.receive(0x18F0010B,2,1,8,0.4f,0f)?.data!!.toInt().toString())
                    objects[3].findViewById<TextView>(R.id.num).setText(can?.receive(0x18FEEE00,1,1,8,1f,-40f)?.data!!.toInt().toString())
                    objects[4].findViewById<TextView>(R.id.num).setText(can?.receive(0x18FEEE00,3,1,16,0.03125f,-237f)?.data!!.toInt().toString())
                    objects[5].findViewById<TextView>(R.id.num).setText(can?.receive(0x18FEF600,2,1,8,2f,0f)?.data!!.toInt().toString())
                    objects[5].findViewById<ProgressBar>(R.id.progressBar).progress = can?.receive(0x18FEF600,2,1,8,2f,0f)?.data?.toInt() ?: 0
                }
                delay(300)
            }
        }
    }

    fun engineRotation(){
        objects[0].findViewById<TextView>(R.id.name)
    }




    private fun createExplicitIntent(): Intent {
        val intent = Intent("ru.kamaz.can.service.aidl.REMOTE_CONNECTION")
        val services = packageManager.queryIntentServices(intent, 0)
        if (services.isEmpty()) {
            throw IllegalStateException("Приложение-сервер не установлено")
        }
        return Intent(intent).apply {
            val resolveInfo = services[0]
            val packageName = resolveInfo.serviceInfo.packageName
            val className = resolveInfo.serviceInfo.name
            component = ComponentName(packageName, className)
        }
    }
    var sercon = object : ServiceConnection {
        override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
            Log.i("qwer", "start")
            can = ru.kamaz.can.api.CanMessenger.Stub.asInterface(p1)
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            Log.i("qwer", "end")
        }

    }
}