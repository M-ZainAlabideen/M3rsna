package app.m3resna.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

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
import app.m3resna.classes.FixControl;
import app.m3resna.classes.GlobalFunctions;
import app.m3resna.classes.Navigator;
import app.m3resna.classes.SessionManager;
import app.m3resna.webservices.RetrofitConfig;
import app.m3resna.webservices.requests.LoginRequest;
import app.m3resna.webservices.responses.RegisterResponse;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginFragment extends Fragment {
    public static FragmentActivity activity;
    public static LoginFragment fragment;
    private View childView;
    private SessionManager sessionManager;

    @BindView(R.id.fragment_login_cl_container)
    ConstraintLayout container;
    @BindView(R.id.fragment_login_et_phone)
    EditText phone;
    @BindView(R.id.fragment_login_et_password)
    EditText password;
    @BindView(R.id.loading)
    ProgressBar loading;

    public static LoginFragment newInstance(FragmentActivity activity) {
        fragment = new LoginFragment();
        LoginFragment.activity = activity;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        childView = inflater.inflate(R.layout.fragment_login, container, false);
        ButterKnife.bind(this, childView);
        return childView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MainActivity.setupAppbar(true, false, true, getString(R.string.login2), true,
                false, false, true,
                true, Constants.ACCOUNT);
        GlobalFunctions.hasNewNotificationsApi(activity);
        FixControl.setupUI(container, activity);
        sessionManager = new SessionManager(activity);
        loading.setVisibility(View.GONE);

    }

    @OnClick(R.id.fragment_login_tv_login)
    public void loginClick() {
        if (!GlobalFunctions.isNetworkConnected(activity)) {
            Snackbar.make(childView, getString(R.string.noConnection), Snackbar.LENGTH_SHORT).show();
        } else {
            String phoneStr = phone.getText().toString();
            String passwordStr = password.getText().toString();
            if (phoneStr == null || phoneStr.isEmpty()) {
                phone.setError(getString(R.string.enterPhone));
            } else {
                if (!FixControl.isValidPhone(phoneStr)) {
                    phone.setError(getString(R.string.invalidPhone));
                } else {
                    phone.setError(null);
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
            if (passwordStr != null && !passwordStr.isEmpty() &&
                    phoneStr != null && !phoneStr.isEmpty() && passwordStr.length() >= 6
                    && FixControl.isValidPhone(phoneStr)) {
                LoginRequest myRequest = new LoginRequest();
                myRequest.mobile = phoneStr;
                myRequest.password = passwordStr;
                loginApi(myRequest);
            }
        }
    }

    @OnClick(R.id.fragment_login_tv_register)
    public void registerClick() {
        Navigator.loadFragment(activity, RegistrationFragment.newInstance(activity), R.id.app_bar_main_fl_mainContainer, true);
    }

    @OnClick(R.id.fragment_login_tv_forgetPassword)
    public void forgetPassword() {
        Navigator.loadFragment(activity, ForgetPasswordFragment.newInstance(activity), R.id.app_bar_main_fl_mainContainer, true);
    }

    public void loginApi(LoginRequest request) {
        loading.setVisibility(View.VISIBLE);
        GlobalFunctions.DisableLayout(container);
        RetrofitConfig.getServices().LOGIN_CALL(sessionManager.getUserToken(), request)
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
                            clearFragments(1);
                            sessionManager.guestLogout();
                            sessionManager.LoginSession();
                            MainActivity.login.setVisibility(View.GONE);
                        } else if (responseCode == 400) {
                            phone.setError(getString(R.string.invalidePhoneORPassword));
                            password.setError(getString(R.string.invalidePhoneORPassword));
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
