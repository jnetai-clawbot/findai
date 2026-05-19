package com.jnetaol.findai

import android.app.Application
import com.jnetaol.findai.data.db.AppDatabase
import com.jnetaol.findai.logger.DebugLogger

class FindAIApp : Application() {

    lateinit var database: AppDatabase
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        DebugLogger.i("FindAIApp", "FA-001 Application started")
        database = AppDatabase.getInstance(this)
    }

    companion object {
        lateinit var instance: FindAIApp
            private set
    }
}
