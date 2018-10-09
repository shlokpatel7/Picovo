package com.example.shlokpatel.picovo;
import java.util.Date;

public class UserPost extends UserPostId{
    String user_id,desc,image_url,thumb;

    Date timestamp;

    public UserPost() {
    }

    public UserPost(String user_id, String desc, String image_url, String thumb, Date timestamp) {
        this.user_id = user_id;
        this.desc = desc;
        this.image_url = image_url;
        this.thumb = thumb;
        this.timestamp = timestamp;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public String getThumb() {
        return thumb;
    }

    public void setThumb(String thumb) {
        this.thumb = thumb;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
