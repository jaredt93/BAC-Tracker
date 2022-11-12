package uncc.itis5280.bacapp.screens.bac;

public class Reading {
    Float bac;
    int id;

    public Reading() {
        //empty
    }

    public Reading(Float bac, int id) {
        this.bac = bac;
        this.id = id;
    }

    public Float getBac() {
        return bac;
    }

    public void setBac(Float bac) {
        this.bac = bac;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
