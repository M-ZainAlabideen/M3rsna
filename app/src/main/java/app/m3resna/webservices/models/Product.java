package app.m3resna.webservices.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import app.m3resna.classes.GlobalFunctions;

public class Product {
    @SerializedName("productAttachment")
    public productAttachment productAttachment;

    @SerializedName("title")
    public String title;

    @SerializedName("id")
    public int id;

    @SerializedName("isFavorite")
    public boolean isFavorite;

    @SerializedName("isPin")
    public boolean isPin;

    //Ad Link
    @SerializedName("link")
    public String link;

    //Ad imageUrl
    @SerializedName("photoUrl")
    public String photoUrl;

    @SerializedName("endDate")
    private String endDate;

    public boolean isAd;

    public long getRemainDays() {
        SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy");
        Date endDateFormat = null;
        try {
            endDateFormat = df.parse(GlobalFunctions.formatDate(endDate));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return GlobalFunctions.differenceDate( GlobalFunctions.currentDate(),endDateFormat);
    }
}
