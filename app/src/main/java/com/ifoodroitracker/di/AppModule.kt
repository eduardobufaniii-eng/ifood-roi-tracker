package com.ifoodroitracker.di

import android.content.Context
import com.ifoodroitracker.data.db.AppDatabase
import com.ifoodroitracker.data.prefs.PreferencesRepository
import com.ifoodroitracker.data.repository.DeliveryRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@dagger.hilt.android.qualifiers.ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getInstance(context)
    }

    @Provides
    @Singleton
    fun providePreferencesRepository(@dagger.hilt.android.qualifiers.ApplicationContext context: Context): PreferencesRepository {
        return PreferencesRepository(context)
    }

    @Provides
    @Singleton
    fun provideDeliveryRepository(
        database: AppDatabase,
        prefs: PreferencesRepository
    ): DeliveryRepository {
        return DeliveryRepository(database.deliveryOfferDao(), prefs)
    }
}