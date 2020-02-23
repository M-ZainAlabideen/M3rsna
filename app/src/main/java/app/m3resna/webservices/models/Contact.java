package app.m3resna.webservices.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Contact {
    @SerializedName("name")
    public String name;

    @SerializedName("value")
    public String value;
}
