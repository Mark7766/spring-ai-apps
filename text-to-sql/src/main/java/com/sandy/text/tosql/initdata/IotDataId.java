package com.sandy.text.tosql.initdata;
import java.io.Serializable;
import java.time.ZonedDateTime;

public class IotDataId implements Serializable {
    private String name;
    private ZonedDateTime time;

    // Constructors
    public IotDataId() {}

    public IotDataId(String name, ZonedDateTime time) {
        this.name = name;
        this.time = time;
    }

    // Getters, Setters, equals(), and hashCode()
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public ZonedDateTime getTime() { return time; }
    public void setTime(ZonedDateTime time) { this.time = time; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IotDataId that = (IotDataId) o;
        return name.equals(that.name) && time.equals(that.time);
    }

    @Override
    public int hashCode() {
        return 31 * name.hashCode() + time.hashCode();
    }
}