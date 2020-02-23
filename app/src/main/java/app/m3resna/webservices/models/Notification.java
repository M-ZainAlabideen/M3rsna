package app.m3resna.webservices.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import app.m3resna.MainActivity;
import app.m3resna.classes.GlobalFunctions;

public class Notification {

    @SerializedName("arabicMessage")
    private String arabicMessage;

    @SerializedName("englishMessage")
    private String englishMessage;

    @SerializedName("isRead")
    public boolean isRead;

    @SerializedName("creationDate")
    private String date;

    public String getMessage() {
        if (MainActivity.isEnglish)
            return englishMessage;
        else
            return arabicMessage;
    }

    public String getDate(){
        return GlobalFunctions.formatDate(date);
    }
}
