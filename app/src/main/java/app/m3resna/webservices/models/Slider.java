package app.m3resna.webservices.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Slider {

    @SerializedName("imagePath")
    public String path;

    @SerializedName("productId")
    public int productId;

    //an additional variable used in case of product details slider to know the type of item video or image
    public String fileType;
    //an additional variable used in case of product details slider to get videoPath and open video if found
    public String videoPath;
}
