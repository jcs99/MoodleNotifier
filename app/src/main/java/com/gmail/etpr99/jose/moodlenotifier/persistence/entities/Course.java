package com.gmail.etpr99.jose.moodlenotifier.persistence.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Objects;

@Entity
public class Course implements Serializable {

    private static final long serialVersionUID = -53978439643798532L;

    @PrimaryKey
    private int id;
    private String name;
    private boolean shouldBeChecked;

    public Course() { }

    public Course(int id, String name, boolean shouldBeChecked) {
        this.id = id;
        this.name = name;
        this.shouldBeChecked = shouldBeChecked;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isShouldBeChecked() {
        return shouldBeChecked;
    }

    public void setShouldBeChecked(boolean shouldBeChecked) {
        this.shouldBeChecked = shouldBeChecked;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, shouldBeChecked);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Course that = (Course) o;
        return id == that.id && name.equals(that.name) && shouldBeChecked == that.shouldBeChecked;
    }

    @Override
    @NotNull
    public String toString() {
        return "Course{" +
            "id=" + id +
            ", name='" + name + '\'' +
            ", shouldBeChecked=" + shouldBeChecked +
            '}';
    }
}
