package mx.edu.utng.firebase.Entity;

/**
 * Created by Catherine on 2/28/2018.
 */

public class User {
    private String name;
    private String email;

    public User() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
