package by.liauko.siarhei.cl.activity

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Bundle
import android.os.CountDownTimer
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import by.liauko.siarhei.cl.R


class LaunchScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launch_screen)

        object : CountDownTimer(2500, 1000) {
            override fun onTick(millisUntilFinished: Long) {}
            override fun onFinish() {
                findViewById<CoordinatorLayout>(R.id.launch_screen).animate().alpha(0.0f).setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        super.onAnimationEnd(animation)
                        finish()
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    }
                })
            }
        }.start()
    }

    override fun onBackPressed() {
        finishAffinity()
    }
}