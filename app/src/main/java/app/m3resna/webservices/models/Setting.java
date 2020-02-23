package app.m3resna.webservices.models;

import com.google.gson.annotations.SerializedName;

public class Setting {
    @SerializedName("name")
    public String name;

    @SerializedName("value")
    public String value;
}
