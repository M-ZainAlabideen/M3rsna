package app.m3resna.webservices.responses;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import app.m3resna.MainActivity;

public class AboutUsResponse {
    @SerializedName("aboutusAr")
    private String aboutUsAr;

    @SerializedName("aboutusEn")
    private String aboutUsEn;

    @SerializedName("termsConditionAr")
    private String termsAr;

    @SerializedName("termsConditionEn")
    private String termsEn;

    public String getAboutUs() {
        if (MainActivity.isEnglish)
            return aboutUsEn;
        else
            return aboutUsAr;
    }

    public String getTerms() {
        if (MainActivity.isEnglish)
            return termsEn;
        else
            return termsAr;
    }
}
