package app.m3resna.webservices.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import app.m3resna.MainActivity;
import butterknife.BindView;

public class Package {
    @SerializedName("id")
    public int id;

    @SerializedName("arabicName")
    private String arabicName;

    @SerializedName("englishName")
    private String englishName;

    @SerializedName("credit")
    public int credit;

    @SerializedName("price")
    public double price;

    @SerializedName("colorCode")
    public String colorCode;

    public String getName() {
        if (MainActivity.isEnglish)
            return englishName;
        else
            return arabicName;
    }

}
