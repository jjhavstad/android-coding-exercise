package com.groundspeak.rove

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.groundspeak.rove.databinding.ActivityDoneBinding

class DoneActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDoneBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDoneBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.textView.text = intent.getStringExtra(EXTRA_TARGET_MESSAGE)
        binding.buttonDone.setOnClickListener {
            startActivity(
                PrimerActivity.createIntent(this).also {
                    it.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
            )
        }
    }

    companion object {
        val EXTRA_TARGET_MESSAGE = "DoneActivity.TARGET_MESSAGE"

        fun createIntent(context: Context, targetMessage: String): Intent {
            return Intent(context, DoneActivity::class.java).also {
                it.putExtra(EXTRA_TARGET_MESSAGE, targetMessage)
            }
        }
    }
}
