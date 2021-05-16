package com.gmail.etpr99.jose.moodlenotifier.persistence.daos;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.gmail.etpr99.jose.moodlenotifier.persistence.entities.Course;

import java.util.List;

@Dao
public interface CourseDao {

    @Query("SELECT * FROM course ORDER BY name")
    List<Course> getAll();

    @Query("SELECT * FROM course WHERE shouldBeChecked = 1 ORDER BY name")
    List<Course> getAllChecked();

    @Query("SELECT * FROM course WHERE id = :id LIMIT 1")
    Course findById(int id);

    @Query("SELECT COUNT(1) FROM course WHERE id = :id")
    int contains(int id);

    @Query("SELECT shouldBeChecked FROM course WHERE id = :id")
    boolean shouldPageBeChecked(int id);

    @Query("UPDATE course SET shouldBeChecked = :checked WHERE id = :id")
    void setShouldPageBeChecked(int id, boolean checked);

    @Insert
    void insert(Course course);

    @Insert
    void insertAll(Course... courses);

    @Delete
    void delete(Course course);
}
