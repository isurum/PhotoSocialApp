package uk.co.stableweb.photosocial.model;

/**
 * Created by Isuru on 10/10/2016.
 */

public class User {

    private String name;
    private String image;
    private String display_name;

    public User() {

    }

    public User(String name, String image, String display_name) {

        this.name = name;
        this.image = image;
        this.display_name = display_name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getDisplay_name() {
        return display_name;
    }

    public void setDisplay_name(String display_name) {
        this.display_name = display_name;
    }
}
