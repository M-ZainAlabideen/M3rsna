package app.m3resna.fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
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
import app.m3resna.classes.Navigator;
import app.m3resna.classes.RecyclerItemClickListener;
import app.m3resna.classes.SessionManager;
import app.m3resna.webservices.RetrofitConfig;
import app.m3resna.webservices.models.Country;
import app.m3resna.webservices.requests.RegisterRequest;
import app.m3resna.webservices.responses.RegisterResponse;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegistrationFragment extends Fragment {
    public static FragmentActivity activity;
    public static RegistrationFragment fragment;
    private View childView;
    private ArrayList<Country> countriesList = new ArrayList<>();
    private SessionManager sessionManager;
    private AlertDialog dialog;
    private int countryId;

    @BindView(R.id.fragment_registration_cl_container)
    ConstraintLayout container;
    @BindView(R.id.fragment_registration_tv_countryName)
    TextView countryName;
    @BindView(R.id.fragment_registration_iv_countryFlag)
    ImageView countryFlag;
    @BindView(R.id.fragment_registration_et_name)
    EditText name;
    @BindView(R.id.fragment_registration_et_phone)
    EditText phone;
    @BindView(R.id.fragment_registration_et_email)
    EditText email;
    @BindView(R.id.fragment_registration_et_password)
    EditText password;
    @BindView(R.id.fragment_registration_cb_agree)
    CheckBox agree;
    @BindView(R.id.loading)
    ProgressBar loading;

    public static RegistrationFragment newInstance(FragmentActivity activity) {
        fragment = new RegistrationFragment();
        RegistrationFragment.activity = activity;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        childView = inflater.inflate(R.layout.fragment_registration, container, false);
        ButterKnife.bind(this, childView);
        return childView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MainActivity.setupAppbar(true, false, true, getString(R.string.register), true,
                false, false, true, true, Constants.ACCOUNT);
        GlobalFunctions.hasNewNotificationsApi(activity);
        FixControl.setupUI(container, activity);
        sessionManager = new SessionManager(activity);
        if (countriesList.size() > 0) {
            countryName.setText(countriesList.get(0).getName());
            setFlagImage(countriesList.get(0).flagUrl, countryFlag);
            countryId = countriesList.get(0).id;

            loading.setVisibility(View.GONE);
        } else {
            if (!GlobalFunctions.isNetworkConnected(activity)) {
                Snackbar.make(childView, getString(R.string.noConnection), Snackbar.LENGTH_SHORT).show();
            } else {
                countriesApi();
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


    @OnClick(R.id.fragment_registration_v_selectCountry)
    public void selectCountryClick() {
        countriesPopUp();
    }

    @OnClick(R.id.fragment_registration_tv_terms)
    public void termsClick() {
        Navigator.loadFragment(activity, AboutUsFragment.newInstance(activity, true), R.id.app_bar_main_fl_mainContainer, true);
    }

    private void countriesPopUp() {
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

    private void countriesApi() {
        GlobalFunctions.DisableLayout(container);
        RetrofitConfig.getServices().COUNTRIES_CALL().enqueue(new Callback<ArrayList<Country>>() {
            @Override
            public void onResponse(Call<ArrayList<Country>> call, Response<ArrayList<Country>> response) {
                loading.setVisibility(View.GONE);
                GlobalFunctions.EnableLayout(container);
                int responseCode = response.code();
                if (responseCode == 200) {
                    if (response.body() != null) {
                        countriesList = response.body();
                        countryId = countriesList.get(0).id;
                        countryName.setText(countriesList.get(0).getName());
                        setFlagImage(countriesList.get(0).flagUrl, countryFlag);
                        loading.setVisibility(View.GONE);
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

    @OnClick(R.id.fragment_registration_tv_register)
    public void registerClick() {
        if (!GlobalFunctions.isNetworkConnected(activity)) {
            Snackbar.make(childView, getString(R.string.noConnection), Snackbar.LENGTH_SHORT).show();
        } else {
            if (!agree.isChecked()) {
                Snackbar.make(childView, getString(R.string.agreeForTerms), Snackbar.LENGTH_SHORT).show();
            } else {
                String nameStr = name.getText().toString();
                String phoneStr = phone.getText().toString();
                String emailStr = email.getText().toString();
                String passwordStr = password.getText().toString();
                if (nameStr == null || nameStr.isEmpty()) {
                    name.setError(getString(R.string.enterName));
                } else {
                    name.setError(null);
                }
                if (phoneStr == null || phoneStr.isEmpty()) {
                    phone.setError(getString(R.string.enterPhone));
                } else {
                    if (!FixControl.isValidPhone(phoneStr)) {
                        phone.setError(getString(R.string.invalidPhone));
                    } else {
                        phone.setError(null);
                    }
                }
                if (emailStr == null || emailStr.isEmpty()) {
                    email.setError(getString(R.string.enterEmail));
                } else {
                    if (!FixControl.isValidEmail(emailStr)) {
                        email.setError(getString(R.string.invalidEmail));
                    } else {
                        email.setError(null);
                    }
                }
                if (passwordStr == null || passwordStr.isEmpty()) {
                    password.setError(getString(R.string.enterPassword));
                } else {
                    if (passwordStr.length() < 6) {
                        password.setError(getString(R.string.invalidPassword));
                    } else {
                        password.setError(null);
                    }
                }
                if (nameStr != null && !nameStr.isEmpty() && phoneStr != null && !phoneStr.isEmpty()
                        && emailStr != null && !emailStr.isEmpty() && passwordStr != null && !passwordStr.isEmpty()
                        && FixControl.isValidEmail(emailStr) && FixControl.isValidPhone(phoneStr) && passwordStr.length() >= 6) {
                    RegisterRequest myRequest = new RegisterRequest();
                    myRequest.fullName = nameStr;
                    myRequest.mobile = phoneStr;
                    myRequest.email = emailStr;
                    myRequest.password = passwordStr;
                    myRequest.countryId = countryId;
                    myRequest.id = sessionManager.getUserId();
                    registerApi(myRequest);
                }
            }
        }
    }

    private void registerApi(RegisterRequest request) {
        loading.setVisibility(View.VISIBLE);
        GlobalFunctions.DisableLayout(container);
        RetrofitConfig.getServices().REGISTER_CALL(sessionManager.getUserToken(), request)
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
                            sessionManager.setUserId(myResponse.user.id);
                            sessionManager.setUserToken(myResponse.token);
                            sessionManager.setCountryId(myResponse.country.id);
                            sessionManager.setCountryName(myResponse.country.arabicName,myResponse.country.englishName);
                            sessionManager.setName(myResponse.user.fullName);
                            sessionManager.setEmail(myResponse.user.email);
                            sessionManager.setPhoneNumber(myResponse.user.mobile);
                            sessionManager.guestLogout();
                            sessionManager.LoginSession();
                           clearFragments(2);
                            MainActivity.login.setVisibility(View.GONE);
                        } else if (responseCode == 203) {
                            phone.setError(getString(R.string.phoneExists));
                        } else if (responseCode == 204) {
                            email.setError(getString(R.string.emailExists));
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

    private void clearFragments(int count) {
        for (int i = 1; i <= count; i++) {
            getFragmentManager().popBackStack();
        }
    }
}
