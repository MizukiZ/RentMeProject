package Model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class User implements Serializable {


    private String id,userName,email,password,image,bio;
    private Map<String, Double> location;


    public User() {
    }

    public User(String id, String userName, String email, String password, String image, String bio, Map<String, Double> location) {
        this.id = id;
        this.userName = userName;
        this.email = email;
        this.password = password;
        this.image = image;
        this.bio = bio;
        this.location = location;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public Map<String, Double> getLocation() {
        return location;
    }

    public void setLocation(Map<String, Double> location) {
        this.location = location;
    }

    public HashMap<String, Object> toHashData(){

        HashMap<String, Object> data = new HashMap<String, Object>();
        data.put("id", getId());
        data.put("userName", getUserName());
        data.put("email",getEmail());
        data.put("password", getPassword());
        data.put("image",getImage());
        data.put("bio",getBio());
        data.put("location",getLocation());

        return data;
    }
}
