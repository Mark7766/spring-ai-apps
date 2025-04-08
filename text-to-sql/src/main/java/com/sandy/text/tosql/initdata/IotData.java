package com.sandy.text.tosql.initdata;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Entity
@Table(name = "iotdata")
@IdClass(IotDataId.class) // Composite key class
public class IotData {
    @Id
    @Column(name = "name", length = 50, nullable = false)
    private String name;

    @Id
    @Column(name = "time", nullable = false)
    private ZonedDateTime time;

    @Column(name = "value", nullable = false, precision = 10, scale = 2)
    private BigDecimal value;

    // Constructors
    public IotData() {}

    public IotData(String name, ZonedDateTime time, BigDecimal value) {
        this.name = name;
        this.time = time;
        this.value = value;
    }

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public ZonedDateTime getTime() { return time; }
    public void setTime(ZonedDateTime time) { this.time = time; }
    public BigDecimal getValue() { return value; }
    public void setValue(BigDecimal value) { this.value = value; }
}