package com.gmail.etpr99.jose.moodlenotifier.persistence.daos;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.gmail.etpr99.jose.moodlenotifier.persistence.entities.CoursePageHtml;

@Dao
public interface CoursePageHtmlDao {

    @Query("SELECT html FROM coursepagehtml WHERE id = :id LIMIT 1")
    String findHtmlByCourseId(int id);

    @Query("SELECT COUNT(1) FROM coursepagehtml WHERE id = :id")
    int contains(int id);

    @Insert
    void insert(CoursePageHtml coursePageHtml);

    @Delete
    void delete(CoursePageHtml coursePageHtml);
}
