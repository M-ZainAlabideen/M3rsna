package app.m3resna.webservices.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import app.m3resna.MainActivity;
import app.m3resna.adapters.MainAdapter;

public class AdPosition {
    @SerializedName("id")
    public int id;

    @SerializedName("arabicName")
    private String arabicName;

    @SerializedName("englishName")
    private String englishName;

    @SerializedName("price")
    public int price;

    @SerializedName("numberOfDays")
    public int numberOfDays;

    @SerializedName("photoUrl")
    public String photoUrl;

    public String getName() {
        if (MainActivity.isEnglish)
            return englishName;
        else
            return arabicName;
    }
}
