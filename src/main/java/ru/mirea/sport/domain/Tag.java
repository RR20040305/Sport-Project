package ru.mirea.sport.domain;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "tags")
public class Tag {
    @Id
    private UUID id;

    @Column(unique = true, nullable = false)
    private String name;

    private String color;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
}