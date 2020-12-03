package me.mateus.desafiowebservices.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import me.mateus.desafiowebservices.R
import kotlin.concurrent.thread

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        thread(start = true) {
            Thread.sleep(1500)
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}