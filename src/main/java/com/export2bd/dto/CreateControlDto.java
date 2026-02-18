package com.export2bd.dto;

public class CreateControlDto {

    private String id_number;
    private String name;
    private String type;
    private String description;

    public String getId_number() { return id_number; }
    public void setId_number(String id_number) { this.id_number = id_number; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}