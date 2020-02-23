package app.m3resna.webservices.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import app.m3resna.MainActivity;

public class Country {
    @SerializedName("id")
     public int id;

    @SerializedName("arabicName")
     public String arabicName;

    @SerializedName("englishName")
     public String englishName;

    @SerializedName("photoUrl")
    public String flagUrl;

    @SerializedName("code")
     public String code;

    public String getName(){
        if(MainActivity.isEnglish)
            return englishName;
        else
            return arabicName;
    }
}
