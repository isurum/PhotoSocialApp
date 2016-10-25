package uk.co.stableweb.photosocial.model;

/**
 * Created by Isuru on 08/10/2016.
 */

public class Photo {

    private String image;
    private String title;
    private String desc;
    private String display_name;
    private String uid;
    private String profile_image;
    private Long uploaded_time;

    public Photo(){

    }

    public Photo(String image, String title, String desc, String display_name, String uid, String profile_image, Long uploaded_time) {
        this.image = image;
        this.title = title;
        this.desc = desc;
        this.display_name = display_name;
        this.uid = uid;
        this.profile_image = profile_image;
        this.uploaded_time = uploaded_time;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getImage() {

        return image;
    }

    public String getDisplay_name() {
        return display_name;
    }

    public void setDisplay_name(String display_name) {
        this.display_name = display_name;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getProfile_image() {
        return profile_image;
    }

    public void setProfile_image(String profile_image) {
        this.profile_image = profile_image;
    }

    public Long getUploaded_time() {
        return uploaded_time;
    }

    public void setUploaded_time(Long uploaded_time) {
        this.uploaded_time = uploaded_time;
    }
}
