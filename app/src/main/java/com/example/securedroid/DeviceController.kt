package com.example.securedroid.ui.theme

import android.app.admin.DevicePolicyManager
import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

object DeviceController {

    fun lockDevice(context: Context) {
        try {
            val devicePolicyManager = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            devicePolicyManager.lockNow()
        } catch (e: Exception) {
            e.printStackTrace()
            // Device admin not enabled
        }
    }

    fun ringDevice(context: Context) {
        try {
            // Get vibrator service
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }

            // Vibrate
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(
                    longArrayOf(0, 1000, 500, 1000, 500, 1000),
                    -1
                ))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(longArrayOf(0, 1000, 500, 1000, 500, 1000), -1)
            }

            // Play sound
            playAlarmSound(context)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun playAlarmSound(context: Context) {
        try {
            val mediaPlayer = MediaPlayer()
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

            // Set alarm volume to max
            val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM)
            audioManager.setStreamVolume(AudioManager.STREAM_ALARM, maxVolume, 0)

            // Use default alarm sound
            mediaPlayer.setDataSource(context, android.provider.Settings.System.DEFAULT_ALARM_ALERT_URI)
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM)
            mediaPlayer.isLooping = false
            mediaPlayer.prepare()
            mediaPlayer.start()

            // Stop after 30 seconds
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                if (mediaPlayer.isPlaying) {
                    mediaPlayer.stop()
                    mediaPlayer.release()
                }
            }, 30000)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}