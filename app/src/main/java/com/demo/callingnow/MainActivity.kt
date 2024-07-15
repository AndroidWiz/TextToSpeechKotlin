package com.demo.callingnow

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.NumberPicker
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.demo.callingnow.databinding.ActivityMainBinding
import com.google.android.material.slider.Slider
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import nl.marc_apps.tts.TextToSpeechEngine
import nl.marc_apps.tts.TextToSpeechFactory
import nl.marc_apps.tts.TextToSpeechInstance
import nl.marc_apps.tts.experimental.ExperimentalVoiceApi
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    var ttsInstance: TextToSpeechInstance? = null
    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

//        val numberPicker by lazy { findViewById<NumberPicker>(R.id.number_picker) }
        val speedSlider by lazy { findViewById<Slider>(R.id.speed_slider) }
        val text by lazy { findViewById<TextInputEditText>(R.id.et_alert) }
        val sliderTv by lazy { findViewById<TextView>(R.id.slider_tv) }
//        var speed by lazy { 55f }
        var speed: Float = 0.55f

//        numberPicker.minValue = 35
//        numberPicker.maxValue = 55
//
//        numberPicker.setOnValueChangedListener { _, _, newValue ->
//            Log.d("numberPicker", "newValue: ${newValue}f")
//            speed = newValue.toFloat()
//            Log.d("speed", "speed: $speed")
//        }

        speedSlider.addOnChangeListener { _, value, _ ->
            speed = value
            Log.d("speedSlider", "Speed: $speed")
            sliderTv.text = speed.toFloat().toString()
        }

        /*// init tts
        lifecycleScope.launch {
            initTextToSpeech(this@MainActivity, locale = Locale.US, speed = speed)
        }*/

        // US
        binding.btnUS.setOnClickListener {
            lifecycleScope.launch {
                initTextToSpeech(this@MainActivity, locale = Locale("en", "US"), speed = speed)
            }
            playAlert(context = this@MainActivity, text = text.text.toString())
        }

        binding.btnUK.setOnClickListener {
            lifecycleScope.launch {
                initTextToSpeech(this@MainActivity, locale = Locale("en", "GB"), speed = speed)
            }
            playAlert(context = this@MainActivity, text = text.text.toString())
        }
        binding.btnIndia.setOnClickListener {
            lifecycleScope.launch {
                initTextToSpeech(this@MainActivity, locale = Locale("en", "IN"), speed = speed)
            }
            playAlert(context = this@MainActivity, text = text.text.toString())
        }
        binding.btnAustralia.setOnClickListener {
            lifecycleScope.launch {
                initTextToSpeech(this@MainActivity, locale = Locale("en", "AU"), speed = speed)
            }
            playAlert(context = this@MainActivity, text = text.text.toString())
        }
    }

    fun playAlert(context: Context, text: String) {
        context.let {
            if (text.isNotEmpty()) {
                lifecycleScope.launch {
                    delay(1000)
                    ttsInstance?.say(
                        text,
                        clearQueue = false
                    )
                }
            }
        }
    }

    @OptIn(ExperimentalVoiceApi::class)
    suspend fun initTextToSpeech(context: Context, locale: Locale, speed: Float) {
        val textToSpeechFactory =
            TextToSpeechFactory(context.applicationContext, TextToSpeechEngine.Google)

        ttsInstance = textToSpeechFactory.createOrNull()

        context.let {
            lifecycleScope.launch {
                ttsInstance?.isSynthesizing?.collect {
                    Log.d(TAG, "initTextToSpeech: $it")
                    Toast.makeText(context, "TTS isSynthesizing is $it", Toast.LENGTH_SHORT).show()
                }
            }

            lifecycleScope.launch {
                ttsInstance?.isWarmingUp?.collect {
                    Log.d(TAG, "initTextToSpeech: $it")
                    Toast.makeText(context, "TTS isWarmingUp is $it", Toast.LENGTH_SHORT).show()
                }
            }
            ttsInstance?.rate = speed
            ttsInstance?.volume = 100

            // set language
            val desiredLocale = locale
            val selectedVoice = ttsInstance?.voices?.find { it.locale == desiredLocale }

            if (selectedVoice != null) {
                ttsInstance?.currentVoice = selectedVoice
                Log.d(TAG, "Selected voice: $selectedVoice")
            } else {
                Log.d(TAG, "No matching voice found for locale: $desiredLocale")
            }
            Log.d(TAG, "ttsLanguage-language: ${ttsInstance?.language}")
            Log.d(TAG, "ttsLanguage-currentVoice: ${ttsInstance?.currentVoice}")
            ttsInstance?.voices?.forEach { voice ->
                Log.d(TAG, "ttsLanguage-voice: $voice")
            }
        }
    }

    override fun onDestroy() {
        ttsInstance?.close()
        super.onDestroy()
    }
}