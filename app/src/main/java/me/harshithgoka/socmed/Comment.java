package me.harshithgoka.socmed;

public class Comment {
    String uid;
    String name;
    int commentid;
    String text;
    String timestamp;

    Comment(String uid, String name, int commentid, String text, String timestamp) {
        this.uid = uid;
        this.name = name;
        this.commentid = commentid;
        this.text = text;
        this.timestamp = timestamp;
    }
}
