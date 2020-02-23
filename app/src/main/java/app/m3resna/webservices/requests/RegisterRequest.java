package app.m3resna.webservices.requests;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class RegisterRequest {
    @SerializedName("id")
     public int id;

    @SerializedName("fullname")
    public String fullName;

    @SerializedName("mobile")
    public String mobile;

    @SerializedName("email")
    public String email;

    @SerializedName("password")
    public String password;

    @SerializedName("countryId")
    public int countryId;
}
