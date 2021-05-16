package com.gmail.etpr99.jose.moodlenotifier.persistence;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.gmail.etpr99.jose.moodlenotifier.persistence.daos.CourseDao;
import com.gmail.etpr99.jose.moodlenotifier.persistence.entities.Course;
import com.gmail.etpr99.jose.moodlenotifier.persistence.entities.CoursePageHtml;
import com.gmail.etpr99.jose.moodlenotifier.persistence.daos.CoursePageHtmlDao;

@Database(entities = {Course.class, CoursePageHtml.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract CourseDao courseDao();
    public abstract CoursePageHtmlDao coursePageHtmlDao();
}
