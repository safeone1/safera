package com.ids.model;



import java.util.List;

public class ApiResponse {
    private String success;
    private String error;
    private List<Integer> data;

    // Getters and setters
    public String getSuccess() {
        return success;
    }

    public String getError() {
        return error;
    }

    public void setSuccess(String success) {
        this.success = success;
    }

    public List<Integer> getData() {
        return data;
    }

    public void setData(List<Integer> data) {
        this.data = data;
    }
}
