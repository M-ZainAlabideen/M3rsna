package app.m3resna.webservices.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class Company {
    @SerializedName("about")
     public String about;

    @SerializedName("countryId")
    public int countryId;

    @SerializedName("email")
    public String email;

    @SerializedName("photoUrl")
    public String photoUrl;

    @SerializedName("fullName")
    public String fullName;

    @SerializedName("instagramLink")
    public String instagramLink;

    @SerializedName("products")
    public ArrayList<Product> products;
}
