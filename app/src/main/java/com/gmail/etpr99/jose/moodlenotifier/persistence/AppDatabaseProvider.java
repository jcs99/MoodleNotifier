package com.gmail.etpr99.jose.moodlenotifier.persistence;

import android.content.Context;

import androidx.room.Room;

import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

@Module
public class AppDatabaseProvider {

    @Singleton
    @Provides
    public AppDatabase provideDbInstance(Context context) {
        return Room.databaseBuilder(context, AppDatabase.class, "courses")
                .allowMainThreadQueries()
                .build();
    }
}
