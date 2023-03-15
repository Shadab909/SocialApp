package com.example.socialapp.model;

import java.util.List;

public class MyUsers {
    private String docId;
    private List<String> userIdList;

    public MyUsers(){

    }

    public MyUsers(String docId, List<String> userIdList) {
        this.docId = docId;
        this.userIdList = userIdList;
    }

    public String getDocId() {
        return docId;
    }

    public List<String> getUserIdList() {
        return userIdList;
    }

    public void setUserIdList(List<String> userIdList) {
        this.userIdList = userIdList;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }


}
