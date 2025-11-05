package com.example.securedroid.ui.theme

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class CommandListenerService : Service() {

    private lateinit var database: FirebaseDatabase
    private var commandListener: ValueEventListener? = null
    private var commandRef: DatabaseReference? = null

    override fun onCreate() {
        super.onCreate()
        database = FirebaseDatabase.getInstance()
        startListeningForCommands()
    }

    private fun startListeningForCommands() {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        commandRef = database.reference.child("devices").child(user.uid).child("commands")

        commandListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val command = snapshot.child("action").getValue(String::class.java)
                val timestamp = snapshot.child("timestamp").getValue(Long::class.java) ?: 0L

                // Only execute if command is recent (within last 5 minutes)
                val currentTime = System.currentTimeMillis()
                if (currentTime - timestamp < 5 * 60 * 1000) {
                    when (command) {
                        "lock" -> {
                            DeviceController.lockDevice(this@CommandListenerService)
                            clearCommand()
                        }
                        "ring" -> {
                            DeviceController.ringDevice(this@CommandListenerService)
                            clearCommand()
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                error.toException().printStackTrace()
            }
        }

        commandRef?.addValueEventListener(commandListener!!)
    }

    private fun clearCommand() {
        commandRef?.removeValue()
    }

    override fun onDestroy() {
        super.onDestroy()
        commandListener?.let {
            commandRef?.removeEventListener(it)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}