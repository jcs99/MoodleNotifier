package com.gmail.etpr99.jose.moodlenotifier.persistence.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Objects;

@Entity
public class CoursePageHtml implements Serializable {

    private static final long serialVersionUID = 980426825932859L;

    @PrimaryKey
    private int id;
    private String html;

    public CoursePageHtml() { }

    public CoursePageHtml(int id, String html) {
        this.id = id;
        this.html = html;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getHtml() {
        return html;
    }

    public void setHtml(String html) {
        this.html = html;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, html);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CoursePageHtml that = (CoursePageHtml) o;
        return id == that.id && html.equals(that.html);
    }

    @Override
    @NotNull
    public String toString() {
        return "CoursePageHtml{" +
            "id = " + id +
            ", html = '" + html +
            '}';
    }
}
