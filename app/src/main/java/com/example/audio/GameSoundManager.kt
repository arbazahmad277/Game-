package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.sin
import kotlin.random.Random

class GameSoundManager(context: Context) {
  private val appContext = context.applicationContext
  private val scope = CoroutineScope(Dispatchers.Default + Job())

  private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    val vibratorManager = appContext.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
    vibratorManager?.defaultVibrator
  } else {
    @Suppress("DEPRECATION")
    appContext.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
  }

  var isSoundEnabled: Boolean = true
  var isVibrationEnabled: Boolean = true

  private val sampleRate = 22050
  private val toneCache = ConcurrentHashMap<String, ByteArray>()

  init {
    // Pre-generate standard audio buffers in background
    scope.launch {
      toneCache["tick"] = generateSineTone(880f, 40)
      toneCache["alarm"] = generateWarbleTone(600f, 1200f, 250)
      toneCache["radar"] = generateSineTone(1400f, 90)
      toneCache["beep"] = generateSineTone(1050f, 60)
      toneCache["gunshot"] = generateNoisePulse(120)
      toneCache["explosion"] = generateExplosionSound(450)
      toneCache["nitro"] = generateNitroSound(600)
      toneCache["wire_cut"] = generateSineTone(520f, 80)
      toneCache["victory"] = generateVictorySound()
    }
  }

  fun playSound(key: String, volume: Float = 0.8f) {
    if (!isSoundEnabled) return
    scope.launch {
      val buffer = toneCache[key] ?: when (key) {
        "engine" -> generateEngineSound(140f, 150)
        "engine_fast" -> generateEngineSound(280f, 150)
        else -> generateSineTone(800f, 60)
      }
      playPcmBuffer(buffer, volume)
    }
  }

  fun playEngineHum(speedRatio: Float) {
    if (!isSoundEnabled) return
    val baseFreq = 80f + (speedRatio.coerceIn(0f, 1f) * 240f)
    scope.launch {
      val buffer = generateEngineSound(baseFreq, 120)
      playPcmBuffer(buffer, 0.4f + (speedRatio * 0.4f))
    }
  }

  fun playBombTick(secondsLeft: Int) {
    if (!isSoundEnabled && !isVibrationEnabled) return
    val freq = if (secondsLeft <= 10) 1400f else 880f
    val duration = if (secondsLeft <= 10) 30 else 50
    scope.launch {
      if (isSoundEnabled) {
        val buf = generateSineTone(freq, duration)
        playPcmBuffer(buf, 0.9f)
      }
    }
    if (secondsLeft <= 15) {
      vibrate(if (secondsLeft <= 5) 80 else 40)
    } else {
      vibrate(20)
    }
  }

  fun playNitroBoost() {
    playSound("nitro", 1.0f)
    vibratePattern(longArrayOf(0, 100, 50, 150))
  }

  fun playGunshot() {
    playSound("gunshot", 0.7f)
    vibrate(25)
  }

  fun playExplosion() {
    playSound("explosion", 1.0f)
    vibratePattern(longArrayOf(0, 80, 40, 200))
  }

  fun playRadarPing() {
    playSound("radar", 0.6f)
    vibrate(15)
  }

  fun playAlert() {
    playSound("alarm", 0.9f)
    vibratePattern(longArrayOf(0, 120, 80, 120))
  }

  fun playWireCut() {
    playSound("wire_cut", 0.8f)
    vibrate(35)
  }

  fun playSuccess() {
    playSound("victory", 1.0f)
    vibratePattern(longArrayOf(0, 100, 100, 250))
  }

  fun vibrate(durationMs: Long) {
    if (!isVibrationEnabled || vibrator == null || !vibrator.hasVibrator()) return
    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrator.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
      } else {
        @Suppress("DEPRECATION")
        vibrator.vibrate(durationMs)
      }
    } catch (_: Exception) {}
  }

  private fun vibratePattern(timings: LongArray) {
    if (!isVibrationEnabled || vibrator == null || !vibrator.hasVibrator()) return
    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrator.vibrate(VibrationEffect.createWaveform(timings, -1))
      } else {
        @Suppress("DEPRECATION")
        vibrator.vibrate(timings, -1)
      }
    } catch (_: Exception) {}
  }

  private fun playPcmBuffer(buffer: ByteArray, volume: Float) {
    var track: AudioTrack? = null
    try {
      val minBufSize = AudioTrack.getMinBufferSize(
        sampleRate,
        AudioFormat.CHANNEL_OUT_MONO,
        AudioFormat.ENCODING_PCM_16BIT
      )
      val finalSize = maxOf(minBufSize, buffer.size)
      track = AudioTrack.Builder()
        .setAudioAttributes(
          AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        )
        .setAudioFormat(
          AudioFormat.Builder()
            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
            .setSampleRate(sampleRate)
            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
            .build()
        )
        .setBufferSizeInBytes(finalSize)
        .setTransferMode(AudioTrack.MODE_STATIC)
        .build()

      track.setVolume(volume.coerceIn(0f, 1f))
      track.write(buffer, 0, buffer.size)
      track.play()

      val playTimeMs = (buffer.size * 1000L) / (sampleRate * 2)
      Thread.sleep(playTimeMs + 20)
    } catch (_: Exception) {
    } finally {
      try {
        track?.stop()
        track?.release()
      } catch (_: Exception) {}
    }
  }

  private fun generateSineTone(freq: Float, durationMs: Int): ByteArray {
    val totalSamples = (sampleRate * durationMs) / 1000
    val bytes = ByteArray(totalSamples * 2)
    for (i in 0 until totalSamples) {
      val t = i.toDouble() / sampleRate
      val envelope = if (i < totalSamples * 0.1) i / (totalSamples * 0.1) else (1.0 - (i / totalSamples.toDouble()))
      val sample = (sin(2.0 * Math.PI * freq * t) * envelope * 28000.0).toInt().coerceIn(-32768, 32767).toShort()
      bytes[i * 2] = (sample.toInt() and 0xFF).toByte()
      bytes[i * 2 + 1] = ((sample.toInt() shr 8) and 0xFF).toByte()
    }
    return bytes
  }

  private fun generateWarbleTone(freq1: Float, freq2: Float, durationMs: Int): ByteArray {
    val totalSamples = (sampleRate * durationMs) / 1000
    val bytes = ByteArray(totalSamples * 2)
    for (i in 0 until totalSamples) {
      val t = i.toDouble() / sampleRate
      val currentFreq = freq1 + (freq2 - freq1) * ((i % (sampleRate / 8)).toFloat() / (sampleRate / 8))
      val sample = (sin(2.0 * Math.PI * currentFreq * t) * 24000.0).toInt().coerceIn(-32768, 32767).toShort()
      bytes[i * 2] = (sample.toInt() and 0xFF).toByte()
      bytes[i * 2 + 1] = ((sample.toInt() shr 8) and 0xFF).toByte()
    }
    return bytes
  }

  private fun generateNoisePulse(durationMs: Int): ByteArray {
    val totalSamples = (sampleRate * durationMs) / 1000
    val bytes = ByteArray(totalSamples * 2)
    val random = Random(42)
    for (i in 0 until totalSamples) {
      val env = 1.0 - (i.toDouble() / totalSamples)
      val sample = ((random.nextDouble() * 2.0 - 1.0) * env * 30000.0).toInt().coerceIn(-32768, 32767).toShort()
      bytes[i * 2] = (sample.toInt() and 0xFF).toByte()
      bytes[i * 2 + 1] = ((sample.toInt() shr 8) and 0xFF).toByte()
    }
    return bytes
  }

  private fun generateExplosionSound(durationMs: Int): ByteArray {
    val totalSamples = (sampleRate * durationMs) / 1000
    val bytes = ByteArray(totalSamples * 2)
    val random = Random(1337)
    var lastLowPass = 0.0
    for (i in 0 until totalSamples) {
      val env = (1.0 - (i.toDouble() / totalSamples))
      val raw = random.nextDouble() * 2.0 - 1.0
      lastLowPass += (raw - lastLowPass) * 0.12 // Low-pass filter for deep boom
      val sample = (lastLowPass * env * 32000.0).toInt().coerceIn(-32768, 32767).toShort()
      bytes[i * 2] = (sample.toInt() and 0xFF).toByte()
      bytes[i * 2 + 1] = ((sample.toInt() shr 8) and 0xFF).toByte()
    }
    return bytes
  }

  private fun generateNitroSound(durationMs: Int): ByteArray {
    val totalSamples = (sampleRate * durationMs) / 1000
    val bytes = ByteArray(totalSamples * 2)
    val random = Random(999)
    for (i in 0 until totalSamples) {
      val t = i.toDouble() / sampleRate
      val env = sin((i.toDouble() / totalSamples) * Math.PI)
      val roar = sin(2.0 * Math.PI * (180.0 + i * 0.1) * t) * 0.4
      val hiss = (random.nextDouble() * 2.0 - 1.0) * 0.6
      val sample = ((roar + hiss) * env * 28000.0).toInt().coerceIn(-32768, 32767).toShort()
      bytes[i * 2] = (sample.toInt() and 0xFF).toByte()
      bytes[i * 2 + 1] = ((sample.toInt() shr 8) and 0xFF).toByte()
    }
    return bytes
  }

  private fun generateEngineSound(freq: Float, durationMs: Int): ByteArray {
    val totalSamples = (sampleRate * durationMs) / 1000
    val bytes = ByteArray(totalSamples * 2)
    for (i in 0 until totalSamples) {
      val t = i.toDouble() / sampleRate
      // Harmonic engine sawtooth blend
      val wave1 = sin(2.0 * Math.PI * freq * t)
      val wave2 = sin(2.0 * Math.PI * (freq * 2) * t) * 0.5
      val wave3 = sin(2.0 * Math.PI * (freq * 0.5) * t) * 0.3
      val sample = ((wave1 + wave2 + wave3) / 1.8 * 22000.0).toInt().coerceIn(-32768, 32767).toShort()
      bytes[i * 2] = (sample.toInt() and 0xFF).toByte()
      bytes[i * 2 + 1] = ((sample.toInt() shr 8) and 0xFF).toByte()
    }
    return bytes
  }

  private fun generateVictorySound(): ByteArray {
    val durationMs = 600
    val totalSamples = (sampleRate * durationMs) / 1000
    val bytes = ByteArray(totalSamples * 2)
    val notes = floatArrayOf(523.25f, 659.25f, 783.99f, 1046.50f)
    for (i in 0 until totalSamples) {
      val noteIndex = (i.toFloat() / totalSamples * notes.size).toInt().coerceIn(0, notes.size - 1)
      val freq = notes[noteIndex]
      val t = i.toDouble() / sampleRate
      val noteFrac = (i % (totalSamples / notes.size)).toDouble() / (totalSamples / notes.size)
      val env = 1.0 - noteFrac * 0.4
      val sample = (sin(2.0 * Math.PI * freq * t) * env * 26000.0).toInt().coerceIn(-32768, 32767).toShort()
      bytes[i * 2] = (sample.toInt() and 0xFF).toByte()
      bytes[i * 2 + 1] = ((sample.toInt() shr 8) and 0xFF).toByte()
    }
    return bytes
  }
}
