package app.m3resna.webservices.requests;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class LoginRequest {

    @SerializedName("Mobile")
    public String mobile;

    @SerializedName("password")
    public String password;
}
