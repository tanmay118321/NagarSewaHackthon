package com.deepdefender.nagarsewahackthon.Admin;

public class Complaint {

    private String description;
    private String address;
    private String issues;
    private String status;
    private String imageBase64;
    private long timestamp;
    private String docId;

    public Complaint() {}

    public String getDescription() { return description; }
    public String getAddress() { return address; }
    public String getIssues() { return issues; }
    public String getStatus() { return status; }
    public String getImageBase64() { return imageBase64; }
    public long getTimestamp() { return timestamp; }
    public String getDocId() { return docId; }

    public void setDocId(String docId) { this.docId = docId; }

    // 🔧 ADD THESE
    public void setStatus(String status) { this.status = status; }
    public void setIssues(String issues) { this.issues = issues; }
    public void setAddress(String address) { this.address = address; }
}
