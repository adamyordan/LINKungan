package garbagecollector.linkungan;

/**
 * Created by Jefly on 7/6/2015.
 * Representasi dari user
 */
public class User {
    public String id;
    public String email;
    public String password;
    public String firstName;
    public String lastName;
    public String status;
    //TODO: profile picture, alamat
    public User(String id, String firstName, String lastName, String email, String password, String status){
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.status = status;
    }

}
