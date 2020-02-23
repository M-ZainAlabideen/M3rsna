package app.m3resna.webservices;

import java.util.ArrayList;

import app.m3resna.classes.Constants;
import app.m3resna.webservices.models.AdPosition;
import app.m3resna.webservices.models.Category;
import app.m3resna.webservices.models.City;
import app.m3resna.webservices.models.Company;
import app.m3resna.webservices.models.Contact;
import app.m3resna.webservices.models.Country;
import app.m3resna.webservices.models.Gender;
import app.m3resna.webservices.models.Notification;
import app.m3resna.webservices.models.Package;
import app.m3resna.webservices.models.PaymentHistory;
import app.m3resna.webservices.models.Setting;
import app.m3resna.webservices.responses.RemainCreditResponse;
import app.m3resna.webservices.models.PaymentMethod;
import app.m3resna.webservices.models.Product;
import app.m3resna.webservices.models.Slider;
import app.m3resna.webservices.models.Zone;
import app.m3resna.webservices.requests.ChangePasswordRequest;
import app.m3resna.webservices.requests.LoginRequest;
import app.m3resna.webservices.requests.RegisterRequest;
import app.m3resna.webservices.requests.SendMessageRequest;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface WebServices {

    @GET("AppSetting")
    Call<ArrayList<Setting>> APP_SETTING_CALL();

    @POST("NewGuest")
    Call<ResponseBody> GUEST_CALL(@Query("CountryId") int CountryId);

    @POST("UserRigster")
    Call<ResponseBody> REGISTER_CALL(@Header(Constants.AUTHORIZATION) String userToken,
                                     @Body RegisterRequest request);

    @POST("LogIn")
    Call<ResponseBody> LOGIN_CALL(@Header(Constants.AUTHORIZATION) String userToken,
                                  @Body LoginRequest request);

    @Multipart
    @POST("UpdateUser")
    Call<ResponseBody> EDIT_PROFILE_CALL(@Header(Constants.AUTHORIZATION) String userToken,
                                         @Part("UserId") RequestBody UserId,
                                         @Part("FullName") RequestBody FullName,
                                         @Part("Mobile") RequestBody Mobile,
                                         @Part("Email") RequestBody Email,
                                         @Part("InstagramLink") RequestBody InstagramLink,
                                         @Part("About") RequestBody About,
                                         @Part("CountryId") RequestBody CountryId,
                                         @Part MultipartBody.Part companyImg);

    @POST("UpdateUserCountry")
    Call<ResponseBody> UPDATE_COUNTRY_CALL(@Header(Constants.AUTHORIZATION) String userToken,
                                           @Query("Userid") int UserId,
                                           @Query("Countryid") int CountryId);

    @GET("Countries")
    Call<ArrayList<Country>> COUNTRIES_CALL();

    @GET("Cities")
    Call<ArrayList<City>> CITIES_CALL(@Header(Constants.AUTHORIZATION) String userToken,
                                      @Query("CountryId") int CountryId);

    @GET("Disticts")
    Call<ArrayList<Zone>> ZONES_CALL(@Header(Constants.AUTHORIZATION) String userToken,
                                     @Query("CityId") int CityId);

    @POST("ChangePassword")
    Call<ResponseBody> CHANGE_PASSWORD_CALL(@Header(Constants.AUTHORIZATION) String userToken,
                                            @Body ChangePasswordRequest request);

    @POST("ForgetPassword")
    Call<ResponseBody> FORGET_PASSWORD_CALL(@Header(Constants.AUTHORIZATION) String userToken,
                                            @Query("email") String email);

    @GET("GetUserData")
    Call<ResponseBody> USER_DATA_CALL(@Header(Constants.AUTHORIZATION) String userToken,
                                      @Query("userId") int userId);

    @GET("SexType")
    Call<ArrayList<Gender>> GENDERS_CALL(@Header(Constants.AUTHORIZATION) String userToken);

    @GET("ContactUs")
    Call<ArrayList<Contact>> CONTACTS_CALL();

    @POST("ContactUs")
    Call<ResponseBody> SEND_MESSAGE_CALL(@Body SendMessageRequest request);

    @GET("AboutUs")
    Call<ResponseBody> ABOUT_US_CALL();

    @GET("HomeSliderPhoto")
    Call<ArrayList<Slider>> HOME_SLIDER_CALL(@Header(Constants.AUTHORIZATION) String userToken,
                                             @Query("CountryId") int CountryId);

    @GET("Categories")
    Call<ArrayList<Category>> CATEGORIES_CALL(@Header(Constants.AUTHORIZATION) String userToken);

    @Multipart
    @POST("AddAds")
    Call<ResponseBody> ADD_NEW_AD_CALL(@Header(Constants.AUTHORIZATION) String userToken,
                                       @Part("Id") RequestBody id,
                                       @Part("Title") RequestBody title,
                                       @Part("Description") RequestBody description,
                                       @Part("Latitude") RequestBody Latitude,
                                       @Part("Longitude") RequestBody Longitude,
                                       @Part("CategoryId") RequestBody CategoryId,
                                       @Part("SexTypeId") RequestBody SexTypeId,
                                       @Part("CountryId") RequestBody CountryId,
                                       @Part("CityId") RequestBody CityId,
                                       @Part("DistrictId") RequestBody DistrictId,
                                       @Part("MobileNumber") RequestBody MobileNumber,
                                       @Part("WhatsAppNumber") RequestBody WhatsAppNumber,
                                       @Part("AlternativeMobileNumber") RequestBody AlternativeMobileNumber,
                                       @Part("AddTypeId") RequestBody AddTypeId,
                                       @Part("UserId") RequestBody UserId,
                                       @Part("PaymentMethod") RequestBody PaymentMethod,
                                       @Part("InstagramLink") RequestBody InstagramLink,
                                       @Part MultipartBody.Part attachmentVideo,
                                       @Part ArrayList<MultipartBody.Part> attachmentImages);


    @GET("AdsPlaces")
    Call<ArrayList<AdPosition>> ADS_POSITIONS_CALL(@Header(Constants.AUTHORIZATION) String userToken);

    @GET("GetCompanyData")
    Call<ArrayList<Company>> OWNER_COMPANY_DATA_CALL(@Header(Constants.AUTHORIZATION) String userToken,
                                                     @Query("UserId") int UserId,
                                                     @Query("CompoanyId") int CompanyId);


    @GET("CategorySliderPhoto")
    Call<ArrayList<Slider>> CATEGORY_SLIDER_CALL(@Header(Constants.AUTHORIZATION) String userToken,
                                                 @Query("CountryId") int CountryId);

    @GET("Subscriptions")
    Call<ArrayList<Package>> PACKAGES_CALL(@Header(Constants.AUTHORIZATION) String userToken,
                                           @Query("userid") int userId);

    @GET("PaymentMehods")
    Call<ArrayList<PaymentMethod>> PAYMENT_METHODS_CALL(@Header(Constants.AUTHORIZATION) String userToken);

    @POST("MakeSubscription")
    Call<ResponseBody> SELECT_PACKAGE_CALL(@Header(Constants.AUTHORIZATION) String userToken,
                                           @Query("userid") int userId,
                                           @Query("subscriptionid") int packageId,
                                           @Query("paymnetMethodid") int paymentMethodId);

    @GET("GetUserSubscriptions")
    Call<RemainCreditResponse> REMAIN_CREDIT_CALL(@Header(Constants.AUTHORIZATION) String userToken,
                                                  @Query("userid") int userId);


    @GET("PaymentHistory")
    Call<ArrayList<PaymentHistory>> PAYMENTS_HISTORY(@Header(Constants.AUTHORIZATION) String userToken,
                                                     @Query("userid") int userId,
                                                     @Query("pageNum") int pageNum);


    @GET("Products")
    Call<ArrayList<Product>> PRODUCTS_CALL(@Header(Constants.AUTHORIZATION) String userToken,
                                           @Query("CategoryId") int categoryId,
                                           @Query("UserId") int UserId,
                                           @Query("CountryId") int CountryId,
                                           @Query("CityId") Integer CityId,
                                           @Query("SexTypeId") Integer SexTypeId,
                                           @Query("pageNum") int pageNum);

    @POST("RenewAd")
    Call<ResponseBody> RENEW_CALL(@Header(Constants.AUTHORIZATION) String userToken,
                                  @Query("ProductId") int ProductId,
                                  @Query("UserId") int UserId);

    @GET("CommAds")
    Call<ArrayList<Product>> COMMERCIAL_ADS_CALL(@Header(Constants.AUTHORIZATION) String userToken,
                                                 @Query("CountryId") int CountryId,
                                                 @Query("CategoryId") int CategoryId);

    @GET("UserFavoriteProducts")
    Call<ArrayList<Product>> FAVORITES_CALL(@Header(Constants.AUTHORIZATION) String userToken,
                                            @Query("UserId") int UserId,
                                            @Query("pageNum") int pageNum);

    @POST("FavoriteAds")
    Call<ResponseBody> MAKE_REMOVE_FAVORITE_CALL(@Header(Constants.AUTHORIZATION) String userToken,
                                                 @Query("ProductId") int ProductId,
                                                 @Query("UserId") int UserId);

    @GET("UserAds")
    Call<ArrayList<Product>> USER_ADS_CALL(@Header(Constants.AUTHORIZATION) String userToken,
                                           @Query("userid") int userId,
                                           @Query("pageNum") int pageNum);

    @POST("DeleteAds")
    Call<ResponseBody> DELETE_AD_CALL(@Header(Constants.AUTHORIZATION) String userToken,
                                      @Query("AdId") int AdId,
                                      @Query("UserId") int UserId);

    @GET("ProductDetails")
    Call<ResponseBody> PRODUCT_DETAILS_CALL(@Header(Constants.AUTHORIZATION) String userToken,
                                            @Query("ProductId") int ProductId,
                                            @Query("UserId") int UserId);

    @POST("DeleteAdsAttachment")
    Call<ResponseBody> DELETE_ATTACHMENT_CALL(@Header(Constants.AUTHORIZATION) String userToken,
                                              @Query("AttachmentId") int AttachmentId);

    @GET("PopUpAds")
    Call<ResponseBody> MAIN_POPUP_AD_CALL(@Header(Constants.AUTHORIZATION) String userToken,
                                          @Query("CountryId") int CountryId);

    @GET("CategoryPopUpAds")
    Call<ResponseBody> CATEGORY_POPUP_AD_CALL(@Header(Constants.AUTHORIZATION) String userToken,
                                              @Query("CountryId") int CountryId);

    @GET("GetUserNotifications")
    Call<ArrayList<Notification>> NOTIFICATIONS_CALL(@Header(Constants.AUTHORIZATION) String userToken,
                                                     @Query("userid") int UserId,
                                                     @Query("pageNum") int pageNum);

    @POST("UpdateUserNotifications")
    Call<ResponseBody> UPDATE_NOTIFICATIONS_CALL(@Header(Constants.AUTHORIZATION) String userToken,
                                                 @Query("userid") int UserId);

}
