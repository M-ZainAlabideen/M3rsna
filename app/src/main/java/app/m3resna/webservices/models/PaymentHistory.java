package app.m3resna.webservices.models;

import com.google.gson.annotations.SerializedName;

import app.m3resna.MainActivity;
import app.m3resna.classes.GlobalFunctions;

public class PaymentHistory {
    @SerializedName("date")
    public String date;

    @SerializedName("amount")
    public int price;

    @SerializedName("arabicService")
    private String detailsAR;

    @SerializedName("englishService")
    private String detailsEN;

    @SerializedName("isSubscription")
    public boolean isSubscription;

    public String getDetails() {
        if (MainActivity.isEnglish)
            return detailsEN;
        else
            return detailsAR;
    }

    public String getDate() {
        return GlobalFunctions.formatDateAndTime(date);
    }

}
