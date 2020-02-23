package app.m3resna.webservices.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class productAttachment {
    @SerializedName("id")
    public int id;

    @SerializedName("fileUrl")
    public String fileUrl;

    @SerializedName("fileType")
    public String fileType;

    @SerializedName("filePlaceholderUrl")
    public String filePlaceholderUrl;
}
