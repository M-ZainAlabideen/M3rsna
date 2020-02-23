package app.m3resna.fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.nguyenhoanglam.imagepicker.model.Config;
import com.nguyenhoanglam.imagepicker.model.Image;
import com.nguyenhoanglam.imagepicker.ui.imagepicker.ImagePicker;
import com.sucho.placepicker.AddressData;
import com.sucho.placepicker.MapType;
import com.sucho.placepicker.PlacePicker;
import com.vincent.videocompressor.VideoCompress;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import app.m3resna.MainActivity;
import app.m3resna.R;
import app.m3resna.adapters.AdsPositionsAdapter;
import app.m3resna.adapters.GeneralDialogAdapter;
import app.m3resna.adapters.AdImagesAdapter;
import app.m3resna.classes.Constants;
import app.m3resna.classes.FixControl;
import app.m3resna.classes.GlobalFunctions;
import app.m3resna.classes.LocationTracker;
import app.m3resna.classes.Navigator;
import app.m3resna.classes.RecyclerItemClickListener;
import app.m3resna.classes.SessionManager;
import app.m3resna.models.AdImage;
import app.m3resna.webservices.RetrofitConfig;
import app.m3resna.webservices.models.AdPosition;
import app.m3resna.webservices.models.City;
import app.m3resna.webservices.models.Country;
import app.m3resna.webservices.models.Gender;
import app.m3resna.webservices.models.Category;
import app.m3resna.webservices.responses.ProductDetailsResponse;
import app.m3resna.webservices.models.Zone;
import app.m3resna.webservices.models.productAttachment;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.app.Activity.RESULT_OK;
import static com.sucho.placepicker.Constants.ADDRESS_INTENT;

public class AddAdFragment extends Fragment {
    public static FragmentActivity activity;
    public static AddAdFragment fragment;
    private View childView;
    private SessionManager sessionManager;

    private AlertDialog dialog;

    private ArrayList<AdImage> adImagesList = new ArrayList<>();
    private AdImagesAdapter adImagesAdapter;
    private LinearLayoutManager layoutManager;

    private ArrayList<Category> categoriesList = new ArrayList<>();
    private ArrayList<Gender> gendersList = new ArrayList<>();
    private ArrayList<Country> countriesList = new ArrayList<>();
    private ArrayList<City> citiesList = new ArrayList<>();
    private ArrayList<Zone> zonesList = new ArrayList<>();
    private ArrayList<AdPosition> adsPositionsList = new ArrayList<>();

    private String adTitleStr;
    private String phoneNumStr;
    private String descriptionStr;
    private String instagramLinkStr = "";
    private String secondPhoneNumStr = "";
    private String whatsNumStr = "";
    private String locationStr = "";
    private int categoryId;
    private int genderId;
    private int countryId;
    private int cityId;
    private int zoneId;
    private int adPositionId;
    private String outputVideoPath;
    private double latitude;
    private double longitude;

    private boolean isUploadedBefore = false;
    private int adId;
    private ProductDetailsResponse productDetails;

    private final int REQUEST_TAKE_GALLERY_VIDEO = 101;
    private final int REQUEST_TAKE_CAMERA_VIDEO = 102;
    private final int REQUEST_PLACE_PICKER = 103;

    @BindView(R.id.fragment_add_ad_cl_container)
    ConstraintLayout container;
    @BindView(R.id.fragment_add_ad_nsv_mediaContainer)
    NestedScrollView mediaContainer;
    @BindView(R.id.fragment_add_ad_cl_videoContainer)
    ConstraintLayout videoContainer;
    @BindView(R.id.fragment_add_ad_iv_videoFrame)
    ImageView videoFrame;
    @BindView(R.id.fragment_add_ad_tv_imagesError)
    TextView imagesError;
    @BindView(R.id.fragment_add_ad_rv_adImages)
    RecyclerView media;
    @BindView(R.id.fragment_add_ad_tv_adPosition)
    TextView adPosition;
    @BindView(R.id.fragment_add_ad_et_adTitle)
    EditText adTitle;
    @BindView(R.id.fragment_add_ad_et_phoneNumber)
    EditText phoneNumber;
    @BindView(R.id.fragment_add_ad_tv_categoryName)
    TextView categoryName;
    @BindView(R.id.fragment_add_ad_tv_genderTxt)
    TextView genderTxt;
    @BindView(R.id.fragment_add_ad_cl_genderContainer)
    ConstraintLayout genderContainer;
    @BindView(R.id.fragment_add_ad_tv_gender)
    TextView gender;
    @BindView(R.id.fragment_add_ad_tv_location)
    TextView location;
    @BindView(R.id.fragment_add_ad_tv_countryName)
    TextView countryName;
    @BindView(R.id.fragment_add_ad_iv_countryFlag)
    ImageView countryFlag;
    @BindView(R.id.fragment_add_ad_tv_cityName)
    TextView cityName;
    @BindView(R.id.fragment_add_ad_tv_zoneName)
    TextView zoneName;
    @BindView(R.id.fragment_add_ad_et_description)
    EditText description;
    @BindView(R.id.fragment_add_ad_et_instagramLink)
    EditText instagramLink;
    @BindView(R.id.fragment_add_ad_et_secondPhoneNum)
    EditText secondPhoneNum;
    @BindView(R.id.fragment_add_ad_et_whatsNumber)
    EditText whatsNumber;
    @BindView(R.id.fragment_add_ad_tv_done)
    TextView done;
    @BindView(R.id.loading)
    ProgressBar loading;

    public static AddAdFragment newInstance(FragmentActivity activity, int adId) {
        fragment = new AddAdFragment();
        AddAdFragment.activity = activity;
        Bundle b = new Bundle();
        b.putInt(Constants.AD_ID, adId);
        fragment.setArguments(b);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        childView = inflater.inflate(R.layout.fragment_add_ad, container, false);
        ButterKnife.bind(this, childView);
        return childView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sessionManager = new SessionManager(activity);
        FixControl.setupUI(container, activity);
        loading.setVisibility(View.GONE);
        if (outputVideoPath == null) {
            videoContainer.setVisibility(View.GONE);
        }
        adPosition.setVisibility(View.GONE);
        imagesError.setVisibility(View.GONE);
        phoneNumber.setText(sessionManager.getPhoneNumber());
        adId = getArguments().getInt(Constants.AD_ID);
        String title = "";
        if (adId == 0) {
            title = getString(R.string.addAd);
            done.setText(getString(R.string.add));
        } else {
            title = getString(R.string.myAds);
            done.setText(getString(R.string.save));

            if (!GlobalFunctions.isNetworkConnected(activity)) {
                Snackbar.make(childView, getString(R.string.noConnection), Snackbar.LENGTH_SHORT).show();
            } else {
                if (adId != 0) {
                    if (productDetails == null) {
                        productDetailsApi(adId);
                    }
                }
            }
        }
        MainActivity.setupAppbar(true, false, true, title,
                true, false, false, false, false, null);

        adImagesAdapter = new AdImagesAdapter(activity, adImagesList, new AdImagesAdapter.OnItemClickListener() {
            @Override
            public void openImageClick(int position) {
                ArrayList<String> adImagesPathsList = new ArrayList<>();
                for (AdImage item : adImagesList) {
                    adImagesPathsList.add(item.image);
                }
                Navigator.loadFragment(activity, ImageGestureFragment.newInstance(activity, adImagesPathsList, position), R.id.app_bar_main_fl_mainContainer, true);
            }

            @Override
            public void deleteImageClick(int position) {
                if (adId != 0) {
                    deleteAttachmentApi(productDetails.productAttachments.get(position).id);
                }
                adImagesList.remove(position);
                adImagesAdapter.notifyDataSetChanged();
            }

        });
        layoutManager = new LinearLayoutManager(activity, RecyclerView.HORIZONTAL, false);
        media.setLayoutManager(layoutManager);
        media.setAdapter(adImagesAdapter);

        if (categoryId != 0) {
            for (Category item : categoriesList) {
                if (categoryId == item.id) {
                    categoryName.setText(item.getName());
                    if (!item.isShowType) {
                        genderTxt.setVisibility(View.GONE);
                        genderContainer.setVisibility(View.GONE);
                        genderId = 0;
                    } else {
                        genderTxt.setVisibility(View.VISIBLE);
                        genderContainer.setVisibility(View.VISIBLE);
                    }
                }
            }
        } else {
            categoriesApi();
        }

        if (genderId != 0) {
            for (Gender item : gendersList) {
                if (genderId == item.id) {
                    gender.setText(item.getName());
                }
            }
        } else {
            genderApi();
        }

        if (countryId != 0) {
            for (Country item : countriesList) {
                if (countryId == item.id) {
                    countryName.setText(item.getName());
                    setFlagImage(item.flagUrl, countryFlag);
                }
            }
        } else {
            countriesApi();
        }

        if (locationStr != null && !locationStr.isEmpty()) {
            location.setText(locationStr);
        }
    }

    private void productDetailsApi(int productId) {
        loading.setVisibility(View.VISIBLE);
        GlobalFunctions.DisableLayout(container);
        RetrofitConfig.getServices().PRODUCT_DETAILS_CALL(sessionManager.getUserToken(), productId, sessionManager.getUserId())
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        loading.setVisibility(View.GONE);
                        GlobalFunctions.EnableLayout(container);
                        int responseCode = response.code();
                        if (responseCode == 200) {
                            ResponseBody body = response.body();
                            String outResponse = "";
                            String jsonResponse = "";

                            try {
                                BufferedReader reader = new BufferedReader(new InputStreamReader(body.byteStream()));

                                StringBuilder out = new StringBuilder();

                                String newLine = System.getProperty("line.separator");

                                String line;

                                while ((line = reader.readLine()) != null) {
                                    out.append(line);
                                    out.append(newLine);
                                }
                                outResponse = out.toString();
                                jsonResponse = out.toString();

                            } catch (Exception ex) {

                                ex.printStackTrace();
                            }

                            if (outResponse != null) {
                                outResponse = outResponse.replace("\"", "");
                                outResponse = outResponse.replace("\n", "");

                                Type type = new TypeToken<ProductDetailsResponse>() {
                                }.getType();

                                JsonReader reader = new JsonReader(new StringReader(outResponse));

                                reader.setLenient(true);

                                productDetails = new Gson().fromJson(jsonResponse, type);
                            }
                            setAdData();
                        } else if (responseCode == 204) {
                            Log.d(Constants.M3RESNA_APP, "ad not found");
                        } else {
                            Snackbar.make(childView, getString(R.string.generalError), Snackbar.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        GlobalFunctions.generalErrorMessage(loading, activity);
                        GlobalFunctions.EnableLayout(container);
                    }
                });
    }

    private void setAdData() {
        adPosition.setVisibility(View.VISIBLE);
        adPositionId = productDetails.addPositionId;
        if (adPositionId == 6)
            adPosition.setText(getString(R.string.payFromPackage));
        else {
            adPosition.setText(productDetails.adPosition.getName());
        }
        adTitle.setText(productDetails.title);
        phoneNumber.setText(productDetails.mobileNumber);
        description.setText(productDetails.description);
        secondPhoneNum.setText(productDetails.alternativeMobileNumber);
        categoryId = productDetails.category.id;
        categoryName.setText(productDetails.category.getName());
        adPositionId = productDetails.addPositionId;
        countryId = productDetails.country.id;
        countryName.setText(productDetails.country.getName());
        setFlagImage(productDetails.country.flagUrl, countryFlag);
        citiesApi(countryId);
        if (productDetails.city != null) {
            cityId = productDetails.city.id;
            cityName.setText(productDetails.city.getName());
            zonesApi(cityId);
        }
        if (productDetails.zone != null) {
            zoneId = productDetails.zone.id;
            zoneName.setText(productDetails.zone.getName());
        }

        if (productDetails.whatsAppNumber != null && !productDetails.whatsAppNumber.isEmpty()) {
            whatsNumber.setText(productDetails.whatsAppNumber);
        }
        if (productDetails.latitude != null && !productDetails.latitude.isEmpty() && !productDetails.latitude.equals("0.0")
                && productDetails.longitude != null && !productDetails.longitude.isEmpty() && !productDetails.longitude.equals("0.0")) {
            latitude = Double.parseDouble(productDetails.latitude);
            longitude = Double.parseDouble(productDetails.longitude);
            location.setText(getAddressFromLngLat(Double.parseDouble(productDetails.latitude), Double.parseDouble(productDetails.longitude)));
        }

        if (categoryId != 0) {
            for (Category item : categoriesList) {
                if (categoryId == item.id) {
                    categoryName.setText(item.getName());
                    if (!item.isShowType) {
                        genderTxt.setVisibility(View.GONE);
                        genderContainer.setVisibility(View.GONE);
                        genderId = 0;
                    } else {
                        genderTxt.setVisibility(View.VISIBLE);
                        genderContainer.setVisibility(View.VISIBLE);
                    }
                }
            }
        }
        if (productDetails.gender != null) {
            genderId = productDetails.gender.id;
            gender.setText(productDetails.gender.getName());
        }
        adImagesList.clear();
        for (productAttachment item : productDetails.productAttachments) {
            if (item.fileType.equals(Constants.I)) {
                AdImage adImage = new AdImage();
                adImage.image = item.fileUrl;
                adImage.isAddedBefore = true;
                adImagesList.add(adImage);
                adImagesAdapter.notifyDataSetChanged();
            } else if (item.fileType.equals(Constants.V)) {
                videoContainer.setVisibility(View.VISIBLE);
                setImage(item.filePlaceholderUrl, videoFrame);
                outputVideoPath = item.fileUrl;
                isUploadedBefore = true;
            }
        }
    }

    private void setImage(String imagePath, ImageView image) {
        if (imagePath != null) {
            try {
                int Width = FixControl.getImageWidth(activity, R.mipmap.placeholder_media);
                int Height = FixControl.getImageHeight(activity, R.mipmap.placeholder_media);
                image.getLayoutParams().height = Height;
                image.getLayoutParams().width = Width;
                Glide.with(activity).load(imagePath)
                        .apply(new RequestOptions().centerCrop()
                                .placeholder(R.mipmap.placeholder_media))
                        .into(image);

            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }
    }

    private void setFlagImage(String imagePath, ImageView image) {
        if (imagePath != null) {
            try {
                int Width = FixControl.getImageWidth(activity, R.mipmap.placeholder_flag);
                int Height = FixControl.getImageHeight(activity, R.mipmap.placeholder_flag);
                image.getLayoutParams().height = Height;
                image.getLayoutParams().width = Width;
                Glide.with(activity).load(imagePath)
                        .apply(new RequestOptions().centerCrop()
                                .placeholder(R.mipmap.placeholder_flag))
                        .into(image);

            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }
    }


    @OnClick(R.id.fragment_add_ad_iv_pickImage)
    public void pickImageClick() {
        if (adImagesList.size() >= 5) {
            imagesError.setVisibility(View.VISIBLE);
            imagesError.setText(R.string.fiveImagesOrLess);
        } else {
            imagesError.setVisibility(View.GONE);
            ImagePicker.with(this)              //  Initialize ImagePicker with activity or fragment context
                    .setCameraOnly(false)               //  Camera mode
                    .setMultipleMode(false)              //  Select multiple images or single image
                    .setFolderMode(true)                //  Folder mode
                    .setShowCamera(true)                //  Show camera button
                    .setMaxSize(1)                     //  Max images can be selected
                    .setSavePath("ImagePicker")         //  Image capture folder name
                    .setAlwaysShowDoneButton(true)      //  Set always show done button in multiple mode
                    .setKeepScreenOn(true)              //  Keep screen on when selecting images
                    .start();
        }
    }

    @OnClick(R.id.fragment_add_ad_iv_pickVideo)
    public void pickVideoClick() {
        if (outputVideoPath == null) {
            if (!GlobalFunctions.isWriteExternalStorageAllowed(activity)) {
                GlobalFunctions.requestWriteExternalStoragePermission(activity);
            } else if (!GlobalFunctions.isReadExternalStorageAllowed(activity)) {
                GlobalFunctions.requestReadExternalStoragePermission(activity);
            } else if (!GlobalFunctions.isCameraPermission(activity)) {
                GlobalFunctions.requestCameraPermission(activity);
            } else {
                createGeneralDialog(Constants.VIDEO);
            }
        } else {
            Snackbar.make(childView, getString(R.string.removeVideoFirst), Snackbar.LENGTH_SHORT).show();
        }
    }

    public void closePopUp() {
        dialog.cancel();

    }

    private void chooseVideoFromGallery() {
        final Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(Intent.createChooser(intent, "Select Video"), REQUEST_TAKE_GALLERY_VIDEO);
    }

    private void takeVideoFromCamera() {
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        startActivityForResult(intent, REQUEST_TAKE_CAMERA_VIDEO);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        loading.setVisibility(View.GONE);
        if (requestCode == Config.RC_PICK_IMAGES && resultCode == RESULT_OK && data != null) {
            ArrayList<Image> images = data.getParcelableArrayListExtra(Config.EXTRA_IMAGES);
            if (images != null) {
                for (Image uri : images) {
                    AdImage adImage = new AdImage();
                    adImage.image = uri.getPath();
                    adImage.isAddedBefore = false;
                    adImagesList.add(adImage);
                    adImagesAdapter.notifyDataSetChanged();
                }
            }
        } else if (requestCode == REQUEST_PLACE_PICKER && resultCode == RESULT_OK && data != null) {
            AddressData addressData = data.getParcelableExtra(ADDRESS_INTENT);
            longitude = addressData.getLongitude();
            latitude = addressData.getLatitude();
            location.setText(getAddressFromLngLat(addressData.getLatitude(), addressData.getLongitude()));
            location.setError(null);

        } else if (resultCode == RESULT_OK && data != null && (requestCode == REQUEST_TAKE_GALLERY_VIDEO || requestCode == REQUEST_TAKE_CAMERA_VIDEO)) {
            Uri selectedVideoUri = data.getData();
            long videoDuration = checkVideoDurationValidation(selectedVideoUri);
            if (videoDuration > 120000) {
                Snackbar.make(childView, getString(R.string.videoDuration), Snackbar.LENGTH_SHORT).show();
            } else {
                String videoPath = "";
                if (requestCode == REQUEST_TAKE_GALLERY_VIDEO) {
                    videoPath = getPath(selectedVideoUri);
                } else if (requestCode == REQUEST_TAKE_CAMERA_VIDEO) {
                    videoPath = getRealPath(selectedVideoUri);
                }
                createCompressedVideoPath(videoPath);
            }
        }

    }

    private long checkVideoDurationValidation(Uri uri) {
        Cursor cursor = MediaStore.Video.query(activity.getContentResolver(), uri, new
                String[]{MediaStore.Video.VideoColumns.DURATION});
        long duration = 0;
        if (cursor != null && cursor.moveToFirst()) {
            duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Video
                    .VideoColumns.DURATION));
            cursor.close();
        }

        return duration;
    }

    public String getPath(Uri uri) {
        String[] projection = {MediaStore.Video.Media.DATA};
        Cursor cursor = activity.getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } else
            return null;
    }

    public String getRealPath(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = activity.managedQuery(uri, projection, null, null, null);
        if (cursor != null) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } else
            return null;
    }

    private void createCompressedVideoPath(String videoPath) {
        String outputDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
        String outputPath = outputDir + File.separator + "VID_" + sessionManager.getUserId() + Calendar.getInstance().getTimeInMillis() + ".mp4";
        compressVideo(videoPath, outputPath);
    }

    private void compressVideo(String input, final String output) {
        VideoCompress.VideoCompressTask task = VideoCompress.compressVideoLow(input, output, new VideoCompress.CompressListener() {
            @Override
            public void onStart() {
                //Start Compress
                loading.setVisibility(View.VISIBLE);
                GlobalFunctions.DisableLayout(container);
            }

            @Override
            public void onSuccess() {
                //Finish successfully
                loading.setVisibility(View.GONE);
                GlobalFunctions.EnableLayout(container);
                videoContainer.setVisibility(View.VISIBLE);
                setVideoFrame(output, videoFrame);
                outputVideoPath = output;
                isUploadedBefore = false;
            }

            @Override
            public void onFail() {
                //Failed
                loading.setVisibility(View.GONE);
                GlobalFunctions.EnableLayout(container);
                Snackbar.make(childView, getString(R.string.canNotCompress), Snackbar.LENGTH_SHORT).show();
            }

            @Override
            public void onProgress(float percent) {
                //Progress
            }
        });

    }

    private void setVideoFrame(String outputVideoPath, ImageView image) {
        if (outputVideoPath != null) {
            try {
                int Width = FixControl.getImageWidth(activity, R.mipmap.placeholder_media);
                int Height = FixControl.getImageHeight(activity, R.mipmap.placeholder_media);
                image.getLayoutParams().height = Height;
                image.getLayoutParams().width = Width;
                image.setImageBitmap(GlobalFunctions.loadVideoFrameFromPath(outputVideoPath));
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }
    }

    private String getAddressFromLngLat(Double latitude, Double longitude) {
        Geocoder geocoder;
        List<Address> addresses = new ArrayList<>();
        Locale locale = new Locale(sessionManager.getUserLanguage());
        geocoder = new Geocoder(activity, locale);

        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String address = addresses.get(0).getAddressLine(0);
        String city = addresses.get(0).getLocality();
        String state = addresses.get(0).getAdminArea();
        String country = addresses.get(0).getCountryName();
        String postalCode = addresses.get(0).getPostalCode();
        String knownName = addresses.get(0).getFeatureName();
        return address;
    }

    @OnClick(R.id.fragment_add_ad_iv_play)
    public void playClick() {
        Navigator.loadFragment(activity, UrlsFragment.newInstance(activity, outputVideoPath, false, null), R.id.app_bar_main_fl_mainContainer, true);
    }

    @OnClick(R.id.fragment_add_ad_iv_deleteVideo)
    public void deleteVideoClick() {
        if (adId != 0) {
            for (productAttachment item : productDetails.productAttachments) {
                if (item.fileType.equals(Constants.V)) {
                    deleteAttachmentApi(item.id);
                }
            }
        } else {
            outputVideoPath = null;
            videoFrame.setImageResource(0);
            videoContainer.setVisibility(View.GONE);
        }

    }

    private void deleteAttachmentApi(int attachmentId) {
        loading.setVisibility(View.VISIBLE);
        GlobalFunctions.DisableLayout(container);
        RetrofitConfig.getServices().DELETE_ATTACHMENT_CALL(sessionManager.getUserToken(), attachmentId)
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        loading.setVisibility(View.GONE);
                        GlobalFunctions.EnableLayout(container);
                        int responseCode = response.code();
                        if (responseCode == 200) {
                            outputVideoPath = null;
                            videoFrame.setImageResource(0);
                            videoContainer.setVisibility(View.GONE);
                        } else if (responseCode == 201) {
                            Log.d(Constants.M3RESNA_APP, "attachment not found");
                        } else {
                            Snackbar.make(childView, getString(R.string.generalError), Snackbar.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        GlobalFunctions.generalErrorMessage(loading, activity);
                        GlobalFunctions.EnableLayout(container);
                    }
                });
    }

    @OnClick(R.id.fragment_add_ad_v_selectCategory)
    public void selectCategoryClick() {
        createGeneralDialog(Constants.CATEGORY);
    }

    private void categoriesApi() {
        loading.setVisibility(View.VISIBLE);
        GlobalFunctions.DisableLayout(container);
        RetrofitConfig.getServices().CATEGORIES_CALL(sessionManager.getUserToken())
                .enqueue(new Callback<ArrayList<Category>>() {
                    @Override
                    public void onResponse(Call<ArrayList<Category>> call, Response<ArrayList<Category>> response) {
                        loading.setVisibility(View.GONE);
                        GlobalFunctions.EnableLayout(container);
                        int responseCode = response.code();
                        if (responseCode == 200) {
                            if (response.body() != null && response.body().size() > 0) {
                                categoriesList.clear();
                                categoriesList.addAll(response.body());
                            }
                        } else if (responseCode == 203) {
                            Log.d(Constants.M3RESNA_APP, "categories not found");
                        } else {
                            Snackbar.make(childView, getString(R.string.generalError), Snackbar.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ArrayList<Category>> call, Throwable t) {
                        GlobalFunctions.generalErrorMessage(loading, activity);
                        GlobalFunctions.EnableLayout(container);
                    }
                });
    }

    @OnClick(R.id.fragment_add_ad_v_selectGender)
    public void selectGenderClick() {
        createGeneralDialog(Constants.GENDER);
    }

    private void genderApi() {
        loading.setVisibility(View.VISIBLE);
        GlobalFunctions.DisableLayout(container);
        RetrofitConfig.getServices().GENDERS_CALL(sessionManager.getUserToken())
                .enqueue(new Callback<ArrayList<Gender>>() {
                    @Override
                    public void onResponse(Call<ArrayList<Gender>> call, Response<ArrayList<Gender>> response) {
                        loading.setVisibility(View.GONE);
                        GlobalFunctions.EnableLayout(container);
                        int responseCode = response.code();
                        if (responseCode == 200) {
                            if (response.body() != null && response.body().size() > 0) {
                                gendersList.clear();
                                gendersList.addAll(response.body());
                            }
                        } else {
                            Snackbar.make(childView, getString(R.string.generalError), Snackbar.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ArrayList<Gender>> call, Throwable t) {
                        GlobalFunctions.generalErrorMessage(loading, activity);
                        GlobalFunctions.EnableLayout(container);
                    }
                });
    }

    @OnClick(R.id.fragment_add_ad_iv_pickLocation)
    public void pickLocationClick() {
        if (!GlobalFunctions.isFineLocationPermission(activity)) {
            GlobalFunctions.requestFineLocationPermission(activity);
        } else {
            pickLocation();
        }

    }

    private void pickLocation() {
        double currentLat = 0;
        double currentLong = 0;
        LocationTracker tracker = new LocationTracker(activity);
        // check if GPS or Network enabled
        if (tracker.canGetLocation()) {
            currentLat = tracker.getLatitude();
            currentLong = tracker.getLongitude();
        }

        Intent intent = new PlacePicker.IntentBuilder()
                .setLatLong(currentLat, currentLong)  // Initial Latitude and Longitude the Map will load into
                .showLatLong(true)  // Show Coordinates in the Activity
                .setMapZoom(12.0f)  // Map Zoom Level. Default: 14.0
                .setAddressRequired(true) // Set If return only Coordinates if cannot fetch Address for the coordinates. Default: True
                .hideMarkerShadow(true) // Hides the shadow under the map marker. Default: False
                .setMarkerDrawable(R.drawable.ic_map_marker) // Change the default Marker Image
                .setMarkerImageImageColor(R.color.orange)
                .setFabColor(R.color.orange)
                .setPrimaryTextColor(R.color.grayDark) // Change text color of Shortened Address
                .setSecondaryTextColor(R.color.gray) // Change text color of full Address
                .setMapRawResourceStyle(R.raw.map_style)  //Set Map Style (https://mapstyle.withgoogle.com/)
                //   https://maps.googleapis.com/maps/api/staticmap?key=YOUR_API_KEY&center=47.65,-122.35000000000002&zoom=12&format=png&maptype=roadmap&size=480x360
                .setMapType(MapType.NORMAL)
                .onlyCoordinates(true)  //Get only Coordinates from Place Picker
                .build(activity);
        startActivityForResult(intent, REQUEST_PLACE_PICKER);
    }

    @OnClick(R.id.fragment_add_ad_v_selectCountry)
    public void selectCountryClick() {
        createGeneralDialog(Constants.COUNTRY);
    }


    private void countriesApi() {
        loading.setVisibility(View.VISIBLE);
        GlobalFunctions.DisableLayout(container);
        RetrofitConfig.getServices().COUNTRIES_CALL().enqueue(new Callback<ArrayList<Country>>() {
            @Override
            public void onResponse(Call<ArrayList<Country>> call, Response<ArrayList<Country>> response) {
                loading.setVisibility(View.GONE);
                GlobalFunctions.EnableLayout(container);
                int responseCode = response.code();
                if (responseCode == 200) {
                    if (response.body() != null && response.body().size() > 0) {
                        countriesList.clear();
                        countriesList.addAll(response.body());
                        if (adId == 0) {
                            countryName.setText(countriesList.get(0).getName());
                            countryId = countriesList.get(0).id;
                            citiesApi(countryId);
                            setFlagImage(countriesList.get(0).flagUrl, countryFlag);
                        }
                    }
                } else if (responseCode == 202) {
                    Log.d(Constants.M3RESNA_APP, "countries not found");
                } else {
                    Snackbar.make(childView, getString(R.string.generalError), Snackbar.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ArrayList<Country>> call, Throwable t) {
                GlobalFunctions.generalErrorMessage(loading, activity);
                GlobalFunctions.EnableLayout(container);
            }
        });
    }

    @OnClick(R.id.fragment_add_ad_v_selectCity)
    public void selectCityClick() {
        createGeneralDialog(Constants.CITY);
    }

    private void citiesApi(int countryId) {
        loading.setVisibility(View.VISIBLE);
        GlobalFunctions.DisableLayout(container);
        RetrofitConfig.getServices().CITIES_CALL(sessionManager.getUserToken(), countryId).enqueue(new Callback<ArrayList<City>>() {
            @Override
            public void onResponse(Call<ArrayList<City>> call, Response<ArrayList<City>> response) {
                loading.setVisibility(View.GONE);
                GlobalFunctions.EnableLayout(container);
                int responseCode = response.code();
                if (responseCode == 200) {
                    if (response.body() != null && response.body().size() > 0) {
                        citiesList.clear();
                        citiesList.addAll(response.body());
                    }
                } else if (responseCode == 202) {
                    Log.d(Constants.M3RESNA_APP, "cities not found");
                } else {
                    Snackbar.make(childView, getString(R.string.generalError), Snackbar.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ArrayList<City>> call, Throwable t) {
                GlobalFunctions.generalErrorMessage(loading, activity);
                GlobalFunctions.EnableLayout(container);
            }
        });
    }

    @OnClick(R.id.fragment_add_ad_v_selectZone)
    public void selectZoneClick() {
        createGeneralDialog(Constants.ZONE);
    }

    private void zonesApi(int cityId) {
        loading.setVisibility(View.VISIBLE);
        GlobalFunctions.DisableLayout(container);
        RetrofitConfig.getServices().ZONES_CALL(sessionManager.getUserToken(), cityId).enqueue(new Callback<ArrayList<Zone>>() {
            @Override
            public void onResponse(Call<ArrayList<Zone>> call, Response<ArrayList<Zone>> response) {
                loading.setVisibility(View.GONE);
                GlobalFunctions.EnableLayout(container);
                int responseCode = response.code();
                if (responseCode == 200) {
                    if (response.body() != null && response.body().size() > 0) {
                        zonesList.clear();
                        zonesList.addAll(response.body());
                    }
                } else if (responseCode == 202) {
                    Log.d(Constants.M3RESNA_APP, "zones not found");
                } else {
                    Snackbar.make(childView, getString(R.string.generalError), Snackbar.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ArrayList<Zone>> call, Throwable t) {
                GlobalFunctions.generalErrorMessage(loading, activity);
                GlobalFunctions.EnableLayout(container);
            }
        });
    }

    private void createGeneralDialog(String flag) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        View generalDialogView = null;
        RecyclerView generalDialogRecycler;

        if (flag.equals(Constants.AD_POSITION)) {
            generalDialogView = activity.getLayoutInflater().inflate(R.layout.dialog_ads_positions, null);
            generalDialogRecycler = (RecyclerView) generalDialogView.findViewById(R.id.dialog_ads_positions_rv_adsPositions);
            generalDialogRecycler.setLayoutManager(new LinearLayoutManager(activity));
            generalDialogRecycler.setAdapter(new AdsPositionsAdapter(activity, adsPositionsList));

        } else if (flag.equals(Constants.VIDEO)) {
            generalDialogRecycler = null;
            generalDialogView = activity.getLayoutInflater().inflate(R.layout.dialog_capture_video, null);
            TextView gallery = (TextView) generalDialogView.findViewById(R.id.dialog_capture_video_tv_gallery);
            TextView camera = (TextView) generalDialogView.findViewById(R.id.dialog_capture_video_tv_camera);

            gallery.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    chooseVideoFromGallery();
                    closePopUp();
                }
            });

            camera.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    takeVideoFromCamera();
                    closePopUp();
                }
            });
        } else {
            generalDialogView = activity.getLayoutInflater().inflate(R.layout.dialog_general, null);
            generalDialogRecycler = (RecyclerView) generalDialogView.findViewById(R.id.dialog_general_rv_generalDialog);
            generalDialogRecycler.setLayoutManager(new LinearLayoutManager(activity));
            generalDialogRecycler.setAdapter(new GeneralDialogAdapter(activity, flag, categoriesList, gendersList, countriesList, citiesList, zonesList));
        }

        builder.setCancelable(true);
        builder.setView(generalDialogView);
        dialog = builder.create();
        dialog.show();
        if (!flag.equals(Constants.VIDEO)) {
            generalDialogRecycler.addOnItemTouchListener(new RecyclerItemClickListener(activity, generalDialogRecycler, new RecyclerItemClickListener.OnItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    if (flag.equals(Constants.COUNTRY)) {
                        countryId = countriesList.get(position).id;
                        countryName.setText(countriesList.get(position).getName());
                        countryName.setError(null);
                        setFlagImage(countriesList.get(position).flagUrl, countryFlag);
                        citiesList.clear();
                        cityName.setText("");
                        cityId = 0;
                        zonesList.clear();
                        zoneName.setText("");
                        zoneId = 0;
                        citiesApi(countryId);
                        closePopUp();
                    } else if (flag.equals(Constants.CITY)) {
                        cityId = citiesList.get(position).id;
                        cityName.setText(citiesList.get(position).getName());
                        zonesList.clear();
                        zoneName.setText("");
                        zoneId = 0;
                        zonesApi(cityId);
                        closePopUp();
                    } else if (flag.equals(Constants.ZONE)) {
                        zoneId = zonesList.get(position).id;
                        zoneName.setText(zonesList.get(position).getName());
                        closePopUp();
                    } else if (flag.equals(Constants.GENDER)) {
                        genderId = gendersList.get(position).id;
                        gender.setText(gendersList.get(position).getName());
                        closePopUp();
                    } else if (flag.equals(Constants.CATEGORY)) {
                        categoryId = categoriesList.get(position).id;
                        categoryName.setText(categoriesList.get(position).getName());
                        categoryName.setError(null);
                        closePopUp();
                        if (categoryId != 0) {
                            for (Category item : categoriesList) {
                                if (categoryId == item.id) {
                                    categoryName.setText(item.getName());
                                    if (!item.isShowType) {
                                        genderTxt.setVisibility(View.GONE);
                                        genderContainer.setVisibility(View.GONE);
                                        genderId = 0;
                                    } else {
                                        genderTxt.setVisibility(View.VISIBLE);
                                        genderContainer.setVisibility(View.VISIBLE);
                                    }
                                }
                            }
                        }
                    } else if (flag.equals(Constants.AD_POSITION)) {
                        adPositionId = adsPositionsList.get(position).id;
                        prepareAdData(adId, adTitleStr, descriptionStr, latitude, longitude, categoryId, genderId, countryId, cityId, zoneId,
                                phoneNumStr, whatsNumStr, instagramLinkStr, secondPhoneNumStr, adPositionId, sessionManager.getUserId(),
                                outputVideoPath, adImagesList);
                        closePopUp();

                    }
                }

                @Override
                public void onItemLongClick(View view, int position) {

                }
            }));
        }
    }

    @OnClick(R.id.fragment_add_ad_tv_done)
    public void doneClick() {
        adTitleStr = adTitle.getText().toString();
        phoneNumStr = phoneNumber.getText().toString();
        descriptionStr = description.getText().toString();
        instagramLinkStr = instagramLink.getText().toString();
        secondPhoneNumStr = secondPhoneNum.getText().toString();
        whatsNumStr = whatsNumber.getText().toString();
        locationStr = location.getText().toString();
        if (adImagesList.size() == 0) {
            imagesError.setVisibility(View.VISIBLE);
            imagesError.setText(getString(R.string.selectOneImage));
        }
        if (adTitleStr == null || adTitleStr.isEmpty()) {
            adTitle.setError(getString(R.string.enterAdTitle));
        } else {
            adTitle.setError(null);
        }
        if (categoryId == 0) {
            categoryName.setError(getString(R.string.selectCategory));
        } else {
            categoryName.setError(null);
        }

        if (phoneNumStr == null || phoneNumStr.isEmpty()) {
            phoneNumber.setError(getString(R.string.enterPhone));
        } else {
            if (!FixControl.isValidPhone(phoneNumStr)) {
                phoneNumber.setError(getString(R.string.invalidPhone));
            } else {
                phoneNumber.setError(null);

            }
        }

        if (countryId == 0) {
            countryName.setError(getString(R.string.selectCountry));
        } else {
            countryName.setError(null);
        }
        if (locationStr == null || locationStr.isEmpty()) {
            location.setError(getString(R.string.pickLocation));
        } else {
            location.setError(null);
        }
        if (descriptionStr == null || descriptionStr.isEmpty()) {
            description.setError(getString(R.string.enterAdDescription));
        } else {
            description.setError(null);
        }
        if (secondPhoneNumStr != null && !secondPhoneNumStr.isEmpty() && !FixControl.isValidPhone(secondPhoneNumStr)) {
            secondPhoneNum.setError(getString(R.string.invalidSecondPhone));
            //in this case use return for breaking the function
            return;
        } else {
            secondPhoneNum.setError(null);
        }
        if (whatsNumStr != null && !whatsNumStr.isEmpty() && !FixControl.isValidPhone(whatsNumStr)) {
            whatsNumber.setError(getString(R.string.invalidWhatsPhone));
            //in this case use return for breaking the function
            return;
        } else {
            whatsNumber.setError(null);
        }
        if (adTitleStr != null && !adTitleStr.isEmpty() && categoryId != 0
               && locationStr != null && !locationStr.isEmpty()
                && phoneNumStr != null && !phoneNumStr.isEmpty() && countryId != 0
                && descriptionStr != null && !descriptionStr.isEmpty()
                && FixControl.isValidPhone(phoneNumStr)) {
            if (adId == 0) {
                adsPositionsApi();
            } else {
                String newVideoPath = null;
                if (!isUploadedBefore) {
                    newVideoPath = outputVideoPath;
                }
                prepareAdData(adId, adTitleStr, descriptionStr, latitude, longitude, categoryId, genderId, countryId, cityId, zoneId,
                        phoneNumStr, whatsNumStr, instagramLinkStr, secondPhoneNumStr, adPositionId, sessionManager.getUserId(),
                        newVideoPath, adImagesList);
            }
        }
    }

    private void adsPositionsApi() {
        loading.setVisibility(View.VISIBLE);
        GlobalFunctions.DisableLayout(container);
        RetrofitConfig.getServices().ADS_POSITIONS_CALL(sessionManager.getUserToken())
                .enqueue(new Callback<ArrayList<AdPosition>>() {
                    @Override
                    public void onResponse(Call<ArrayList<AdPosition>> call, Response<ArrayList<AdPosition>> response) {
                        loading.setVisibility(View.GONE);
                        GlobalFunctions.EnableLayout(container);
                        int responseCode = response.code();
                        if (responseCode == 200) {
                            if (response.body() != null && response.body().size() > 0) {
                                adsPositionsList.clear();
                                adsPositionsList.addAll(response.body());
                                createGeneralDialog(Constants.AD_POSITION);
                            }
                        } else {
                            Snackbar.make(childView, getString(R.string.generalError), Snackbar.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ArrayList<AdPosition>> call, Throwable t) {
                        GlobalFunctions.generalErrorMessage(loading, activity);
                        GlobalFunctions.EnableLayout(container);
                    }
                });
    }

    private void prepareAdData(int adId, String adTitleStr, String adDescriptionStr, double latitude, double longitude,
                               int categoryId, int genderId, int countryId,
                               int cityId, int zoneId, String phoneNumberStr, String whatsNumberStr,
                               String instagramLinkStr, String secondPhoneNumStr,
                               int adPositionId, int userId,
                               String videoPath, ArrayList<AdImage> imagesPaths) {

        RequestBody adTitleReq = RequestBody.create(MediaType.parse("text/plain"), adTitleStr);
        RequestBody adDescriptionReq = RequestBody.create(MediaType.parse("text/plain"), adDescriptionStr);

        RequestBody adIdReq = null;
        if (adId != 0)
            adIdReq = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(adId));

        RequestBody latitudeReq = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(latitude));
        RequestBody longitudeReq = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(longitude));
        RequestBody categoryIdReq = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(categoryId));
        RequestBody genderIdReq = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(genderId));
        RequestBody countryIdReq = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(countryId));
        RequestBody cityIdReq = null;
        if (cityId != 0)
            cityIdReq = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(cityId));
        RequestBody zoneIdReq = null;
        if (zoneId != 0)
            zoneIdReq = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(zoneId));
        RequestBody phoneNumberReq = RequestBody.create(MediaType.parse("text/plain"), phoneNumberStr);
        RequestBody whatsNumberReq = RequestBody.create(MediaType.parse("text/plain"), whatsNumberStr);
        RequestBody instagramLinkReq = RequestBody.create(MediaType.parse("text/plain"), instagramLinkStr);
        RequestBody secondPhoneNumReq = RequestBody.create(MediaType.parse("text/plain"), secondPhoneNumStr);
        RequestBody adPositionIdReq = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(adPositionId));
        RequestBody userIdReq = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(userId));

        MultipartBody.Part videoPart = null;
        if (videoPath != null && !videoPath.isEmpty()) {
            File videoFile = new File(videoPath);
            RequestBody videoRequestBody = RequestBody.create(MediaType.parse("video/*"), videoFile);
            videoPart = MultipartBody.Part.createFormData("attachmentvideo", videoFile.getName(), videoRequestBody);
        }
        File imageFile;
        RequestBody imageRequestBody;
        ArrayList<MultipartBody.Part> adImagesParts = new ArrayList<>();
        for (AdImage item : imagesPaths) {
            if (!item.isAddedBefore) {
                imageFile = new File(item.image);
                imageRequestBody = RequestBody.create(MediaType.parse("image/*"), imageFile);
                adImagesParts.add(MultipartBody.Part.createFormData("attachmentimages", imageFile.getName(), imageRequestBody));
            }
        }
        addNewAdApi(adIdReq, adTitleReq, adDescriptionReq, latitudeReq, longitudeReq, categoryIdReq, genderIdReq,
                countryIdReq, cityIdReq, zoneIdReq, phoneNumberReq, whatsNumberReq, instagramLinkReq, secondPhoneNumReq, adPositionIdReq,
                userIdReq, videoPart, adImagesParts);
    }

    private void addNewAdApi(RequestBody adIdReq,
                             RequestBody adTitleReq,
                             RequestBody adDescriptionReq,
                             RequestBody latitudeReq,
                             RequestBody longitudeReq,
                             RequestBody categoryIdReq,
                             RequestBody genderIdReq,
                             RequestBody countryIdReq,
                             RequestBody cityIdReq,
                             RequestBody zoneIdReq,
                             RequestBody phoneNumberReq,
                             RequestBody whatsNumberReq,
                             RequestBody instagramLinkReq,
                             RequestBody secondPhoneNumReq,
                             RequestBody adPositionIdReq,
                             RequestBody userIdReq,
                             MultipartBody.Part video,
                             ArrayList<MultipartBody.Part> images) {
        loading.setVisibility(View.VISIBLE);
        GlobalFunctions.DisableLayout(container);
        RetrofitConfig.getServices().ADD_NEW_AD_CALL(
                sessionManager.getUserToken(),
                adIdReq,
                adTitleReq,
                adDescriptionReq,
                latitudeReq,
                longitudeReq,
                categoryIdReq,
                genderIdReq,
                countryIdReq,
                cityIdReq,
                zoneIdReq,
                phoneNumberReq,
                whatsNumberReq,
                secondPhoneNumReq,
                adPositionIdReq,
                userIdReq,
                null,
                instagramLinkReq,
                video,
                images
        ).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                loading.setVisibility(View.GONE);
                GlobalFunctions.EnableLayout(container);
                int responseCode = response.code();
                if (responseCode == 200) {
                    if (response.body() != null) {
                        String responseStr;
                        try {
                            responseStr = response.body().string();
                            if (responseStr.equals("1")) {
                                clearCurrentFragment();
                                Navigator.loadFragment(activity, MyAdsFragment.newInstance(activity), R.id.app_bar_main_fl_mainContainer, true);
                                if (adId == 0) {
                                    Snackbar.make(childView, getString(R.string.adAddedSuccessfully), Snackbar.LENGTH_SHORT).show();
                                } else {
                                    Snackbar.make(childView, getString(R.string.adUpdatedSuccessfully), Snackbar.LENGTH_SHORT).show();
                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } else if (responseCode == 201) {
                    Snackbar.make(childView, getString(R.string.noEnoughBalance), Snackbar.LENGTH_SHORT).show();
                    Navigator.loadFragment(activity,PackagesFragment.newInstance(activity,null),R.id.app_bar_main_fl_mainContainer,true);
                } else if (responseCode == 202) {
                    Log.d(Constants.M3RESNA_APP, "country not found");
                } else if (responseCode == 203) {
                    Log.d(Constants.M3RESNA_APP, "user not found");
                } else if (responseCode == 204) {
                    Log.d(Constants.M3RESNA_APP, "category not found");
                } else if (responseCode == 205) {
                    Log.d(Constants.M3RESNA_APP, "adPositionId(AdType) not found");
                } else if (responseCode == 206) {
                    Log.d(Constants.M3RESNA_APP, "payment method not found");
                } else {
                    Snackbar.make(childView, getString(R.string.generalError), Snackbar.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                GlobalFunctions.generalErrorMessage(loading, activity);
                GlobalFunctions.EnableLayout(container);
            }
        });
    }

    private void clearCurrentFragment() {
        getFragmentManager().popBackStack();
    }
}
