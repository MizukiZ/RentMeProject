package Model;

public class Post {

    private String id,title,description,image,location,category,user_id;
    private Double cost;
    boolean isRented;

    public Post() {
    }

    public Post(String id, String title, String description, String image, String location, String category, String user_id, Double cost, boolean isRented) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.image = image;
        this.location = location;
        this.category = category;
        this.user_id = user_id;
        this.cost = cost;
        this.isRented = isRented;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public Double getCost() {
        return cost;
    }

    public void setCost(Double cost) {
        this.cost = cost;
    }

    public boolean isRented() {
        return isRented;
    }

    public void setRented(boolean rented) {
        isRented = rented;
    }
}
