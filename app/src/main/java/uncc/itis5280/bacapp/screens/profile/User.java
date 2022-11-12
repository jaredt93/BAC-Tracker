package uncc.itis5280.bacapp.screens.profile;

import java.io.Serializable;
import java.util.ArrayList;

import uncc.itis5280.bacapp.screens.bac.Reading;

public class User implements Serializable {
    public static String FEMALE = "FEMALE";
    public static String MALE = "MALE";

    String id;
    String email;
    String password;
    String firstName;
    String lastName;
    String city;
    String gender;
    ArrayList<Reading> readingHistory;

    public User() {
        // empty constructor
    }

    public User(String id, String email, String firstName, String lastName, String city, String gender, ArrayList<Reading> readingHistory) {
        this.id = id;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.city = city;
        this.gender = gender;
        this.readingHistory = readingHistory;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
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

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public ArrayList<Reading> getReadingHistory() {
        return readingHistory;
    }

    public void setReadingHistory(ArrayList<Reading> readingHistory) {
        this.readingHistory = readingHistory;
    }
}

