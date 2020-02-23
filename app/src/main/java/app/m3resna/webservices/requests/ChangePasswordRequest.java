package app.m3resna.webservices.requests;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ChangePasswordRequest {
    @SerializedName("userid")
    public int userId;

    @SerializedName("oldPassword")
    public String oldPassword;

    @SerializedName("password")
    public String password;
}
