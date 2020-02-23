package app.m3resna.fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.duolingo.open.rtlviewpager.RtlViewPager;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import app.m3resna.MainActivity;
import app.m3resna.R;
import app.m3resna.adapters.SliderAdapter;
import app.m3resna.classes.Constants;
import app.m3resna.classes.FixControl;
import app.m3resna.classes.GlobalFunctions;
import app.m3resna.classes.Navigator;
import app.m3resna.classes.SessionManager;
import app.m3resna.webservices.RetrofitConfig;
import app.m3resna.webservices.models.Contact;
import app.m3resna.webservices.models.Setting;
import app.m3resna.webservices.models.Slider;
import app.m3resna.webservices.models.productAttachment;
import app.m3resna.webservices.responses.ProductDetailsResponse;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.relex.circleindicator.CircleIndicator;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductDetailsFragment extends Fragment {
    public static FragmentActivity activity;
    public static ProductDetailsFragment fragment;
    private View childView;
    private SessionManager sessionManager;
    private ProductDetailsResponse productDetails;
    private ArrayList<Setting> appSetting = new ArrayList<>();
    private String isShowViews;
    private ArrayList<Slider> sliderList = new ArrayList<>();
    private SliderAdapter sliderAdapter;
    private int currentPage = 0;
    private int NUM_PAGES = 0;
    private String iphoneLink;
    private String androidLink;
    AlertDialog dialog;
    AlertDialog.Builder builder;

    @BindView(R.id.fragment_product_details_cl_container)
    ConstraintLayout container;
    @BindView(R.id.fragment_product_details_vp_slider)
    RtlViewPager slider;
    @BindView(R.id.fragment_product_details_ci_sliderCircles)
    CircleIndicator sliderCircles;
    @BindView(R.id.fragment_product_details_tv_views)
    TextView views;
    @BindView(R.id.fragment_product_details_tv_companyName)
    TextView companyName;
    @BindView(R.id.fragment_product_details_iv_companyLogo)
    ImageView companyLogo;
    @BindView(R.id.fragment_product_details_tv_productTitle)
    TextView productTitle;
    @BindView(R.id.fragment_product_details_iv_makeRemoveFav)
    ImageView fav;
    @BindView(R.id.fragment_product_details_tv_address)
    TextView address;
    @BindView(R.id.fragment_product_details_tv_description)
    TextView description;
    @BindView(R.id.loading)
    ProgressBar loading;


    public static ProductDetailsFragment newInstance(FragmentActivity activity, int productId) {
        fragment = new ProductDetailsFragment();
        ProductDetailsFragment.activity = activity;
        Bundle b = new Bundle();
        b.putInt(Constants.PRODUCT_ID, productId);
        fragment.setArguments(b);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        childView = inflater.inflate(R.layout.fragment_product_details, container, false);
        ButterKnife.bind(this, childView);
        return childView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MainActivity.setupAppbar(true, false, true, getString(R.string.productDetails),
                true, true, false, true, true, Constants.CATEGORY);
        GlobalFunctions.hasNewNotificationsApi(activity);
        container.setVisibility(View.GONE);
        sessionManager = new SessionManager(activity);
        if (!GlobalFunctions.isNetworkConnected(activity)) {
            Snackbar.make(childView, getString(R.string.noConnection), Snackbar.LENGTH_SHORT).show();
        } else {
            appSettingApi();
        }

        /*make slider autoPlay , every 5 seconds the image will replace with the next one
         * when lastImage come , the cycle will start from 0 again*/
        slider.setCurrentItem(0, true);
        final Handler handler = new Handler();
        final Runnable update = new Runnable() {
            @Override
            public void run() {
                if (currentPage == NUM_PAGES) {
                    currentPage = 0;
                }
                slider.setCurrentItem(currentPage++, true);
            }
        };


        Timer swipeTimer = new Timer();
        swipeTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.post(update);
            }
        }, 5000, 5000);

    }

    private void setupSlider() {
        NUM_PAGES = sliderList.size();
        sliderAdapter = new SliderAdapter(activity, sliderList, true);
        slider.setAdapter(sliderAdapter);
        sliderCircles.setViewPager(slider);
        sliderCircles.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float v, int i1) {
                currentPage = position;
            }

            @Override
            public void onPageSelected(int i) {

            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });

    }

    private void appSettingApi() {
        loading.setVisibility(View.VISIBLE);
        GlobalFunctions.DisableLayout(container);
        RetrofitConfig.getServices().APP_SETTING_CALL()
                .enqueue(new Callback<ArrayList<Setting>>() {
                    @Override
                    public void onResponse(Call<ArrayList<Setting>> call, Response<ArrayList<Setting>> response) {
                        loading.setVisibility(View.GONE);
                        GlobalFunctions.EnableLayout(container);
                        int responseCode = response.code();
                        if (responseCode == 200) {
                            if (response.body() != null && response.body().size() > 0) {
                                appSetting.clear();
                                appSetting.addAll(response.body());
                                for (Setting item : appSetting) {
                                    if (item.name.equals(Constants.SHOW_VIEWS)) {
                                        isShowViews = item.value;
                                    }
                                }
                            }
                            productDetailsApi(getArguments().getInt(Constants.PRODUCT_ID));
                        }
                    }

                    @Override
                    public void onFailure(Call<ArrayList<Setting>> call, Throwable t) {
                        GlobalFunctions.generalErrorMessage(loading, activity);
                        GlobalFunctions.EnableLayout(container);
                    }
                });
    }


    @OnClick(R.id.fragment_product_details_iv_makeRemoveFav)
    public void favClick() {
        makeRemoveFavApi();
    }

    private void makeRemoveFavApi() {
        loading.setVisibility(View.VISIBLE);
        GlobalFunctions.DisableLayout(container);
        RetrofitConfig.getServices().MAKE_REMOVE_FAVORITE_CALL(sessionManager.getUserToken(), productDetails.id, sessionManager.getUserId())
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        loading.setVisibility(View.GONE);
                        GlobalFunctions.EnableLayout(container);
                        int responseCode = response.code();
                        if (responseCode == 200) {
                            String result = "";
                            try {
                                result = response.body().string();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            if (result.equals("true")) {
                                fav.setImageResource(R.mipmap.ic_heart_details_sel);
                            } else if (result.equals("false")) {
                                fav.setImageResource(R.mipmap.ic_heart_details_unsel);
                            }
                        } else if (responseCode == 201) {
                            Log.d(Constants.M3RESNA_APP, "Ad not found");
                        } else if (responseCode == 203) {
                            Log.d(Constants.M3RESNA_APP, "user not found");
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

    @OnClick(R.id.fragment_product_details_v_companyDetails)
    public void companyDetailsCLick() {
        Navigator.loadFragment(activity, OwnerCompanyFragment.newInstance(activity, productDetails.user.id), R.id.app_bar_main_fl_mainContainer, true);
    }

    @OnClick(R.id.fragment_product_details_iv_phone)
    public void phoneCLick() {
        if (productDetails.alternativeMobileNumber == null || productDetails.alternativeMobileNumber.isEmpty()) {
            callPhoneNumber(productDetails.mobileNumber);
        } else {
            createDialogPhones();
        }
    }

    private void createDialogPhones() {
        //use the dialog of Gallery/Camera for show the mainNumber and alternativeNumber
        View phonesDialogView = activity.getLayoutInflater().inflate(R.layout.dialog_capture_video, null);
        TextView gallery = (TextView) phonesDialogView.findViewById(R.id.dialog_capture_video_tv_gallery);
        TextView camera = (TextView) phonesDialogView.findViewById(R.id.dialog_capture_video_tv_camera);

        gallery.setText(productDetails.mobileNumber);
        camera.setText(productDetails.alternativeMobileNumber);

        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callPhoneNumber(productDetails.mobileNumber);
                closeDialog();
            }
        });

        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callPhoneNumber(productDetails.alternativeMobileNumber);
                closeDialog();
            }
        });
        builder = new AlertDialog.Builder(activity);
        builder.setCancelable(true);
        builder.setView(phonesDialogView);
        dialog = builder.create();
        dialog.show();
    }

    public void closeDialog() {
        dialog.cancel();

    }

    private void callPhoneNumber(String phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("tel:" + phoneNumber));
        activity.startActivity(intent);
    }

    @OnClick(R.id.fragment_product_details_iv_whats)
    public void whatsClick() {
        if (productDetails.whatsAppNumber != null && !productDetails.whatsAppNumber.isEmpty()) {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("http://api.whatsapp.com/send?phone=" + "+965" + productDetails.whatsAppNumber + "&text="));
                startActivity(intent);
            } catch (Exception e) {
                Snackbar.make(childView, getString(R.string.whatsAppNotFound), Snackbar.LENGTH_SHORT).show();
                e.printStackTrace();
            }

        } else {
            Snackbar.make(childView, getString(R.string.whatsNumberNotAvailable), Snackbar.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.fragment_product_details_iv_instagram)
    public void instagramCLick() {
        if (productDetails.instagramLink != null && !productDetails.instagramLink.isEmpty()) {
            Navigator.loadFragment(activity, UrlsFragment.newInstance(activity, productDetails.instagramLink, false, null), R.id.app_bar_main_fl_mainContainer, false);
        } else {
            Snackbar.make(childView, getString(R.string.instagramLinkNotAvailable), Snackbar.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.fragment_product_details_iv_share)
    public void shareClick() {
        contactsApi();
    }

    public void contactsApi() {
        loading.setVisibility(View.VISIBLE);
        GlobalFunctions.DisableLayout(container);
        RetrofitConfig.getServices().CONTACTS_CALL()
                .enqueue(new Callback<ArrayList<Contact>>() {
                    @Override
                    public void onResponse(Call<ArrayList<Contact>> call, Response<ArrayList<Contact>> response) {
                        loading.setVisibility(View.GONE);
                        GlobalFunctions.EnableLayout(container);
                        int responseCode = response.code();
                        if (responseCode == 200) {
                            ArrayList<Contact> myResponse = response.body();
                            if (myResponse.size() > 0) {
                                for (Contact item : response.body()) {
                                    if (item.name.equals(Constants.IPHONE_LINK))
                                        iphoneLink = item.value;
                                    else if (item.name.equals(Constants.ANDROID_LINK))
                                        androidLink = item.value;
                                }
                                share();
                            }
                        } else {
                            Snackbar.make(childView, getString(R.string.generalError), Snackbar.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ArrayList<Contact>> call, Throwable t) {
                        GlobalFunctions.generalErrorMessage(loading, activity);
                        GlobalFunctions.EnableLayout(container);
                    }
                });
    }

    private void share() {
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        String shareBody = productDetails.title + "\n" + getString(R.string.checkAdOnApp)
                + "\n\n" + getString(R.string.iPhone) + "\n" + iphoneLink
                + "\n\n" + getString(R.string.android) + "\n" + androidLink;
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
        startActivity(Intent.createChooser(sharingIntent, "Share via"));
    }

    @OnClick(R.id.fragment_product_details_v_showOnMap)
    public void showOnMapClick() {
        if (productDetails.latitude != null && !productDetails.latitude.isEmpty() && !productDetails.latitude.equals("0.0")
                && productDetails.longitude != null && !productDetails.longitude.isEmpty() && !productDetails.longitude.equals("0.0")) {
            if (!GlobalFunctions.isFineLocationPermission(activity)) {
                GlobalFunctions.requestFineLocationPermission(activity);
            } else {
                Navigator.loadFragment(activity, LocationFragment.newInstance(activity, productDetails.latitude, productDetails.longitude), R.id.app_bar_main_fl_mainContainer, true);
            }
        } else {
            Snackbar.make(childView, getString(R.string.locationNotAvailable), Snackbar.LENGTH_SHORT).show();
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
                            setProductData();
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

    private void setProductData() {
        container.setVisibility(View.VISIBLE);
        productTitle.setText(productDetails.title);
        sliderList.clear();
        for (
                productAttachment item : productDetails.productAttachments) {
            if (item.fileType.equals(Constants.I)) {
                Slider slider = new Slider();
                slider.path = item.fileUrl;
                slider.fileType = Constants.IMAGE;
                sliderList.add(slider);
            } else if (item.fileType.equals(Constants.V)) {
                Slider slider = new Slider();
                slider.path = item.filePlaceholderUrl;
                slider.fileType = Constants.VIDEO;
                slider.videoPath = item.fileUrl;
                sliderList.add(slider);
            }
        }

        setupSlider();

        companyName.setText(productDetails.user.fullName);
        if(isShowViews.equals(Constants.ON)){
            views.setVisibility(View.VISIBLE);
            views.setText(getString(R.string.views)+" ("+productDetails.numberOfViews+")");
        }else{
            views.setVisibility(View.GONE);
        }
        setImage(productDetails.user.photoUrl, companyLogo);
        if ((productDetails.isFav)) {
            fav.setImageResource(R.mipmap.ic_heart_details_sel);
        } else {
            fav.setImageResource(R.mipmap.ic_heart_details_unsel);
        }
        if (productDetails.country != null && productDetails.city != null && productDetails.zone != null
                && productDetails.country.getName() != null && productDetails.city.getName() != null && productDetails.zone.getName() != null) {
            address.setVisibility(View.VISIBLE);
            address.setText(productDetails.country.getName() + ", " + productDetails.city.getName() + ", " + productDetails.zone.getName());
            //address.setText(getAddressFromLngLat(Double.parseDouble(productDetails.latitude), Double.parseDouble(productDetails.longitude)));
        } else {
            address.setVisibility(View.GONE);
        }
        description.setText(productDetails.description);
    }

    private void setImage(String imagePath, ImageView image) {
        if (imagePath != null) {
            try {
                int Width = FixControl.getImageWidth(activity, R.mipmap.placeholder_company_logo);
                int Height = FixControl.getImageHeight(activity, R.mipmap.placeholder_company_logo);
                image.getLayoutParams().height = Height;
                image.getLayoutParams().width = Width;
                Glide.with(activity).load(imagePath)
                        .apply(new RequestOptions().centerCrop()
                                .placeholder(R.mipmap.placeholder_company_logo))
                        .into(image);

            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }
    }

}

