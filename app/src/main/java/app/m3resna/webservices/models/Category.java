package app.m3resna.webservices.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

import app.m3resna.MainActivity;

public class Category implements Serializable {
    @SerializedName("id")
    public int id;

    @SerializedName("arabicName")
    private String arabicName;

    @SerializedName("englishName")
    private String englishName;

    @SerializedName("imageUrl")
    public String imageUrl;

    @SerializedName("isShowType")
    public boolean isShowType;

    public String getName() {
        if (MainActivity.isEnglish)
            return englishName;
        else
            return arabicName;
    }
}
