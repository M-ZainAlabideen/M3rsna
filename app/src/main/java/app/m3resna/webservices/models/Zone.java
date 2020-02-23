package app.m3resna.webservices.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import app.m3resna.MainActivity;

public class Zone {
    @SerializedName("id")
    public int id;

    @SerializedName("arabicName")
    private String arabicName;

    @SerializedName("englishName")
    private String englishName;

    public String getName(){
        if(MainActivity.isEnglish)
            return englishName;
        else
            return arabicName;
    }
}
