package app.m3resna.webservices.responses;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

import app.m3resna.webservices.models.AdPosition;
import app.m3resna.webservices.models.Category;
import app.m3resna.webservices.models.City;
import app.m3resna.webservices.models.Country;
import app.m3resna.webservices.models.Gender;
import app.m3resna.webservices.models.User;
import app.m3resna.webservices.models.Zone;
import app.m3resna.webservices.models.productAttachment;

public class ProductDetailsResponse {
    @SerializedName("id")
    public Integer id;

    @SerializedName("user")
    public User user;

    @SerializedName("title")
    public String title;

    @SerializedName("description")
    public String description;

    @SerializedName("latitude")
    public String latitude;

    @SerializedName("longitude")
    public String longitude;

    @SerializedName("category")
    public Category category;

    @SerializedName("gender")
    public Gender gender;

    @SerializedName("countryId")
    @Expose
    public Integer countryId;

    @SerializedName("country")
    public Country country;

    @SerializedName("city")
    public City city;

    @SerializedName("district")
    public Zone zone;

    @SerializedName("mobileNumber")
    public String mobileNumber;

    @SerializedName("whatsAppNumber")
    public String whatsAppNumber;

    @SerializedName("alternativeMobileNumber")
    public String alternativeMobileNumber;

    @SerializedName("addTypeId")
    public Integer addPositionId;

    @SerializedName("addType")
    public AdPosition adPosition;

    @SerializedName("productAttachments")
    public ArrayList<productAttachment> productAttachments;

    @SerializedName("isFavorite")
    public boolean isFav;

    @SerializedName("instagramLink")
    public String instagramLink;

    @SerializedName("numberOfViews")
    public int numberOfViews;

}
