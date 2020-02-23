package app.m3resna.models;

public class AdImage {
    /*this model is offline model (not coming from server)
    this model used for saving the image of Ad
    in case of editAd some images in adImagesList added before and will not be added again
     so make isAddedBefore = true and if add someNewImages the isAddedBefore will equal false
    if isAddedBefore = true >> the current image*/
    public String image;
    public boolean isAddedBefore;
}
