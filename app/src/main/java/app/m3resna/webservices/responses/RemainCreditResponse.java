package app.m3resna.webservices.responses;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import app.m3resna.classes.GlobalFunctions;

public class RemainCreditResponse {
    @SerializedName("remainCredit")
    public int remainCredit;
}
