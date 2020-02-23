package app.m3resna.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.lang.reflect.Type;

import app.m3resna.MainActivity;
import app.m3resna.R;
import app.m3resna.classes.Constants;
import app.m3resna.classes.GlobalFunctions;
import app.m3resna.classes.Navigator;
import app.m3resna.classes.SessionManager;
import app.m3resna.webservices.RetrofitConfig;
import app.m3resna.webservices.models.Category;
import app.m3resna.webservices.models.Product;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FullAdFragment extends Fragment {
    public static FragmentActivity activity;
    public static FullAdFragment fragment;
    private View childView;
    private SessionManager sessionManager;
    private Product product;

    @BindView(R.id.fragment_full_ad_cl_constraint_container)
    ConstraintLayout container;
    @BindView(R.id.fragment_full_ad_iv_adImage)
    ImageView adImage;
    @BindView(R.id.loading)
    ProgressBar loading;

    public static FullAdFragment newInstance(FragmentActivity activity, boolean isMainFullAd, Category category) {
        fragment = new FullAdFragment();
        FullAdFragment.activity = activity;
        Bundle b = new Bundle();
        b.putBoolean(Constants.IS_HOME_FULL_AD, isMainFullAd);
        b.putSerializable(Constants.CATEGORY, category);
        fragment.setArguments(b);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        childView = inflater.inflate(R.layout.fragment_full_ad, container, false);
        ButterKnife.bind(this, childView);
        return childView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MainActivity.setupAppbar(false, false, false, null,
                false, false, false, false, false, null);
        GlobalFunctions.hasNewNotificationsApi(activity);
        sessionManager = new SessionManager(activity);
        container.setVisibility(View.GONE);
        if (!GlobalFunctions.isNetworkConnected(activity)) {
            Snackbar.make(childView, getString(R.string.noConnection), Snackbar.LENGTH_SHORT).show();
        } else {
            if (getArguments().getBoolean(Constants.IS_HOME_FULL_AD)) {
                mainPopUpAdApi();
            } else {
                categoryPopUpAdApi();
            }
        }
    }

    private void mainPopUpAdApi() {
        loading.setVisibility(View.VISIBLE);
        RetrofitConfig.getServices().MAIN_POPUP_AD_CALL(sessionManager.getUserToken(), sessionManager.getCountryId())
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        loading.setVisibility(View.GONE);
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

                                Type type = new TypeToken<Product>() {
                                }.getType();

                                JsonReader reader = new JsonReader(new StringReader(outResponse));

                                reader.setLenient(true);

                                product = new Gson().fromJson(jsonResponse, type);
                            }
                            setImage(product.productAttachment.fileUrl, adImage);
                        } else if (responseCode == 203) {
                            clearCurrentFragment();
                            Navigator.loadFragment(activity, HomeFragment.newInstance(activity), R.id.app_bar_main_fl_mainContainer, true);
                        } else {
                            Snackbar.make(childView, getString(R.string.generalError), Snackbar.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        loading.setVisibility(View.GONE);
                        Navigator.loadFragment(activity, HomeFragment.newInstance(activity), R.id.app_bar_main_fl_mainContainer, true);
                    }
                });
    }

    private void categoryPopUpAdApi() {
        loading.setVisibility(View.VISIBLE);
        RetrofitConfig.getServices().CATEGORY_POPUP_AD_CALL(sessionManager.getUserToken(), sessionManager.getCountryId())
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        loading.setVisibility(View.GONE);
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

                                Type type = new TypeToken<Product>() {
                                }.getType();

                                JsonReader reader = new JsonReader(new StringReader(outResponse));

                                reader.setLenient(true);

                                product = new Gson().fromJson(jsonResponse, type);
                            }
                            setImage(product.productAttachment.fileUrl, adImage);
                        } else if (responseCode == 203) {
                            clearCurrentFragment();
                            Navigator.loadFragment(activity, ProductsFragment.newInstance(activity, (Category) getArguments().getSerializable(Constants.CATEGORY)), R.id.app_bar_main_fl_mainContainer, true);
                        } else {
                            Snackbar.make(childView, getString(R.string.generalError), Snackbar.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        GlobalFunctions.generalErrorMessage(loading, activity);
                    }
                });
    }

    private void setImage(String imagePath, ImageView image) {
        container.setVisibility(View.VISIBLE);
        try {
            Glide.with(activity).load(imagePath)
                    .apply(new RequestOptions().centerCrop()
                            .placeholder(R.mipmap.placeholder_full_ad))
                    .into(image);

        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }


    @OnClick(R.id.fragment_full_ad_iv_adImage)
    public void adImageClick() {
        Navigator.loadFragment(activity, ProductDetailsFragment.newInstance(activity, product.id), R.id.app_bar_main_fl_mainContainer, true);
    }

    @OnClick(R.id.fragment_full_ad_iv_closeAd)
    public void closeAdCLick() {
        if (getArguments().getBoolean(Constants.IS_HOME_FULL_AD)) {
            Navigator.loadFragment(activity, HomeFragment.newInstance(activity), R.id.app_bar_main_fl_mainContainer, true);
        } else {
            Navigator.loadFragment(activity, ProductsFragment.newInstance(activity, (Category) getArguments().getSerializable(Constants.CATEGORY)), R.id.app_bar_main_fl_mainContainer, true);
        }
    }

    private void clearCurrentFragment() {
        getFragmentManager().popBackStack();
    }
}


