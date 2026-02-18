package com.export2bd.dto;

import java.util.Map;

public class ImportFailureDto {

    private int row;
    private String id_number;
    private String reason;
    private Map<String, Object> raw;

    public int getRow() { return row; }
    public void setRow(int row) { this.row = row; }

    public String getId_number() { return id_number; }
    public void setId_number(String id_number) { this.id_number = id_number; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public Map<String, Object> getRaw() { return raw; }
    public void setRaw(Map<String, Object> raw) { this.raw = raw; }
}