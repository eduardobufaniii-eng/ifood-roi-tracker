package com.ifoodroitracker

import android.app.Application
import android.content.Context
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.android.EntryPointAccessors
import com.ifoodroitracker.data.db.AppDatabase
import com.ifoodroitracker.data.prefs.PreferencesRepository
import com.ifoodroitracker.data.repository.DeliveryRepository
import javax.inject.Inject

@HiltAndroidApp
class IfoodRoiApplication : Application() {

    // Entry points para serviços que não suportam injeção direta
    @Inject
    lateinit var preferencesRepository: PreferencesRepository

    @Inject
    lateinit var deliveryRepository: DeliveryRepository

    @Inject
    lateinit var appDatabase: AppDatabase

    override fun onCreate() {
        super.onCreate()
        // Hilt injeta automaticamente
    }

    companion object {
        fun getPreferencesRepository(context: Context): PreferencesRepository {
            val app = context.applicationContext as IfoodRoiApplication
            return app.preferencesRepository
        }

        fun getDeliveryRepository(context: Context): DeliveryRepository {
            val app = context.applicationContext as IfoodRoiApplication
            return app.deliveryRepository
        }

        fun getAppDatabase(context: Context): AppDatabase {
            val app = context.applicationContext as IfoodRoiApplication
            return app.appDatabase
        }
    }
}