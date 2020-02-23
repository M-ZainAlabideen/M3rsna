package app.m3resna.webservices.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class User {
    @SerializedName("id")
    public int id;

    @SerializedName("fullName")
    public String fullName;

    @SerializedName("mobile")
    public String mobile;

    @SerializedName("email")
    public String email;

    @SerializedName("password")
    public String password;

    @SerializedName("photoUrl")
    public String photoUrl;

    @SerializedName("countryId")
    public int countryId;

    @SerializedName("instagramLink")
    public String instagramLink;

    @SerializedName("about")
    public String about;
}
