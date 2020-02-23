package app.m3resna.webservices.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import app.m3resna.MainActivity;

public class Gender {
    @SerializedName("id")
    public int id;

    @SerializedName("arabicName")
    private String arabicName;

    @SerializedName("englishName")
    private String englishName;

    public void setName(String name) {
        if (MainActivity.isEnglish)
            englishName = name;
        else
            arabicName = name;
    }

    public String getName() {
        if (MainActivity.isEnglish)
            return englishName;
        else
            return arabicName;
    }
}
