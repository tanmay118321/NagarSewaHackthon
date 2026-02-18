package com.deepdefender.nagarsewahackthon.Admin;

public class Complaint {
    private String title;
    private String subtitle; // Also used for ID like #CMP-1024
    private String status;   // e.g., "NEW" or "URGENT"

    public Complaint(String title, String subtitle, String status) {
        this.title = title;
        this.subtitle = subtitle;
        this.status = status;
    }

    // Getters
    public String getTitle() { return title; }
    public String getSubtitle() { return subtitle; }
    public String getStatus() { return status; }
}
