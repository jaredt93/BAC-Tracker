package uncc.itis5280.bacapp.screens.bac;

public class Reading {
    Float bac;
    String date;

    public Reading() {
        //empty
    }

    public Reading(Float bac, String date) {
        this.bac = bac;
        this.date = date;
    }

    public Float getBac() {
        return bac;
    }

    public void setBac(Float bac) {
        this.bac = bac;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
