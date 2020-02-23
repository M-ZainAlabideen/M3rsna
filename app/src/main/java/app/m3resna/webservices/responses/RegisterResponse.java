package app.m3resna.webservices.responses;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import app.m3resna.webservices.models.Country;
import app.m3resna.webservices.models.User;

public class RegisterResponse {
    @SerializedName("user")
    public User user;

    @SerializedName("token")
    public String token;

    @SerializedName("country")
    public Country country;
}
