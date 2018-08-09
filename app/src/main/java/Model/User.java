package Model;

public class User {

    private String email,password;

    public User() {
    }

    public User(String email, String password, String name, String phone) {

        this.email = email;
        this.password = password;

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

}
