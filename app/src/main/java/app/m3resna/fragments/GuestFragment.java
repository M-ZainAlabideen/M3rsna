package app.m3resna.fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import java.util.ArrayList;

import app.m3resna.MainActivity;
import app.m3resna.R;
import app.m3resna.adapters.GeneralDialogAdapter;
import app.m3resna.classes.Constants;
import app.m3resna.classes.FixControl;
import app.m3resna.classes.GlobalFunctions;
import app.m3resna.classes.LocaleHelper;
import app.m3resna.classes.RecyclerItemClickListener;
import app.m3resna.classes.SessionManager;
import app.m3resna.webservices.RetrofitConfig;
import app.m3resna.webservices.models.Country;
import app.m3resna.webservices.responses.RegisterResponse;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GuestFragment extends Fragment {
    public static FragmentActivity activity;
    public static GuestFragment fragment;
    private View childView;
    private ArrayList<Country> countriesList = new ArrayList<>();
    private SessionManager sessionManager;
    private AlertDialog dialog;
    private int countryId;

    @BindView(R.id.fragment_guest_cl_container)
    ConstraintLayout container;
    @BindView(R.id.fragment_guest_tv_countryName)
    TextView countryName;
    @BindView(R.id.fragment_guest_iv_countryFlag)
    ImageView countryFlag;
    @BindView(R.id.fragment_guest_iv_arrow)
    ImageView arrow;
    @BindView(R.id.loading)
    ProgressBar loading;

    public static GuestFragment newInstance(FragmentActivity activity) {
        fragment = new GuestFragment();
        GuestFragment.activity = activity;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        childView = inflater.inflate(R.layout.fragment_guest, container, false);
        ButterKnife.bind(this, childView);
        return childView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MainActivity.setupAppbar(false, false, false, null, false,
                false, false, false, false, null);
        sessionManager = new SessionManager(activity);
        container.setVisibility(View.GONE);
        if (countriesList.size() > 0) {
            loading.setVisibility(View.GONE);
            container.setVisibility(View.VISIBLE);
        } else {
            if (!GlobalFunctions.isNetworkConnected(activity)) {
                Snackbar.make(childView, getString(R.string.noConnection), Snackbar.LENGTH_SHORT).show();
            } else {
                countriesApi();
            }
        }
    }

    @OnClick(R.id.fragment_guest_v_changeLang)
    public void changeLangClick() {
        changeLanguage();
    }

    private void changeLanguage() {
         /*for changing the language of App
        1- check the value of currentLanguage  and Reflects it
         2- set the new value of Language in local and change the value of languageSharedPreference to new value
         3- restart the mainActivity with noAnimation
        * */

        if (sessionManager.getUserLanguage().equals("en")) {
            sessionManager.setUserLanguage("ar");
            MainActivity.isEnglish = false;
        } else {
            sessionManager.setUserLanguage("en");
            MainActivity.isEnglish = true;
        }

        LocaleHelper.setLocale(activity, sessionManager.getUserLanguage());
        activity.finish();
        activity.overridePendingTransition(0, 0);
        startActivity(new Intent(activity, MainActivity.class));
        GlobalFunctions.setUpFont(activity);
    }

    @OnClick(R.id.fragment_guest_v_selectCountry)
    public void selectCountryClick() {
        createCountriesDialog();
    }

    private void createCountriesDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        View popUpView = activity.getLayoutInflater().inflate(R.layout.dialog_general, null);
        RecyclerView countries = (RecyclerView) popUpView.findViewById(R.id.dialog_general_rv_generalDialog);
        countries.setLayoutManager(new LinearLayoutManager(activity));
        countries.setAdapter(new GeneralDialogAdapter(activity, Constants.COUNTRY, null, null, countriesList, null, null));
        builder.setCancelable(true);
        builder.setView(popUpView);
        dialog = builder.create();
        dialog.show();
        countries.addOnItemTouchListener(new RecyclerItemClickListener(activity, countries, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                countryId = countriesList.get(position).id;
                countryName.setText(countriesList.get(position).getName());
                setFlagImage(countriesList.get(position).flagUrl, countryFlag);
                closePopUp();
            }

            @Override
            public void onItemLongClick(View view, int position) {

            }
        }));
    }

    public void closePopUp() {
        dialog.cancel();

    }


    @OnClick(R.id.fragment_guest_tv_continueAsGuest)
    public void continueAsGuestClick() {
        if (!GlobalFunctions.isNetworkConnected(activity)) {
            Snackbar.make(childView, getString(R.string.noConnection), Snackbar.LENGTH_SHORT).show();
        } else {
            guestApi(countryId);
        }
    }

    private void guestApi(int countryId) {
        loading.setVisibility(View.VISIBLE);
        GlobalFunctions.DisableLayout(container);
        RetrofitConfig.getServices().GUEST_CALL(countryId)
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        loading.setVisibility(View.GONE);
                        GlobalFunctions.EnableLayout(container);
                        int responseCode = response.code();
                        if (responseCode == 200) {
                            RegisterResponse myResponse = null;
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
                                Type type = new TypeToken<RegisterResponse>() {
                                }.getType();
                                JsonReader reader = new JsonReader(new StringReader(outResponse));
                                reader.setLenient(true);
                                myResponse = new Gson().fromJson(jsonResponse, type);
                            }
                            if (response.body() != null) {
                                sessionManager.setUserId(myResponse.user.id);
                                sessionManager.setUserToken(myResponse.token);
                                sessionManager.setCountryId(myResponse.country.id);
                                sessionManager.setCountryName(myResponse.country.arabicName,myResponse.country.englishName);
                                sessionManager.guestSession();
                                MainActivity.login.setVisibility(View.VISIBLE);
                                activity.finish();
                                activity.overridePendingTransition(0, 0);
                                startActivity(new Intent(activity, MainActivity.class));
                            }
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

    private void countriesApi() {
        GlobalFunctions.DisableLayout(container);
        RetrofitConfig.getServices().COUNTRIES_CALL().enqueue(new Callback<ArrayList<Country>>() {
            @Override
            public void onResponse(Call<ArrayList<Country>> call, Response<ArrayList<Country>> response) {
                loading.setVisibility(View.GONE);
                container.setVisibility(View.VISIBLE);
                GlobalFunctions.EnableLayout(container);
                int responseCode = response.code();
                if (responseCode == 200) {
                    if (response.body() != null && response.body().size() > 0) {
                        countriesList.clear();
                        countriesList.addAll(response.body());
                        countryName.setText(countriesList.get(0).getName());
                        countryName.setText(countriesList.get(0).getName());
                        setFlagImage(countriesList.get(0).flagUrl, countryFlag);
                        countryId = countriesList.get(0).id;
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

}

