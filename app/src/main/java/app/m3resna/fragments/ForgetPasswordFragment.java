package app.m3resna.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.google.android.material.snackbar.Snackbar;

import app.m3resna.MainActivity;
import app.m3resna.R;
import app.m3resna.classes.Constants;
import app.m3resna.classes.FixControl;
import app.m3resna.classes.GlobalFunctions;
import app.m3resna.classes.Navigator;
import app.m3resna.classes.SessionManager;
import app.m3resna.webservices.RetrofitConfig;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ForgetPasswordFragment extends Fragment {
    public static FragmentActivity activity;
    public static ForgetPasswordFragment fragment;
    private View childView;
    private SessionManager sessionManager;

    @BindView(R.id.fragment_forget_password_cl_container)
    ConstraintLayout container;
    @BindView(R.id.fragment_forget_password_et_email)
    EditText email;
    @BindView(R.id.loading)
    ProgressBar loading;

    public static ForgetPasswordFragment newInstance(FragmentActivity activity) {
        fragment = new ForgetPasswordFragment();
        ForgetPasswordFragment.activity = activity;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        childView = inflater.inflate(R.layout.fragment_forget_password, container, false);
        ButterKnife.bind(this, childView);
        return childView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MainActivity.setupAppbar(true, false, true, getString(R.string.recoverPassword),
                true, false, false, false, false, null);
        FixControl.setupUI(container, activity);
        sessionManager = new SessionManager(activity);
        loading.setVisibility(View.GONE);
    }

    @OnClick(R.id.fragment_forget_password_tv_done)
    public void doneClick() {
        if (!GlobalFunctions.isNetworkConnected(activity)) {
            Snackbar.make(childView, getString(R.string.noConnection), Snackbar.LENGTH_SHORT).show();
        } else {
            String emailStr = email.getText().toString();
            if (emailStr == null || emailStr.isEmpty()) {
                email.setError(getString(R.string.enterEmail));
            } else {
                if (!FixControl.isValidEmail(emailStr)) {
                    email.setError(getString(R.string.invalidEmail));
                } else {
                    email.setError(null);
                }
            }
            if (emailStr != null && !emailStr.isEmpty() && FixControl.isValidEmail(emailStr)) {
                forgetPasswordApi(emailStr);
            }
        }
    }

    private void forgetPasswordApi(String email) {
        loading.setVisibility(View.VISIBLE);
        GlobalFunctions.DisableLayout(container);
        RetrofitConfig.getServices().FORGET_PASSWORD_CALL(sessionManager.getUserToken(), email)
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        loading.setVisibility(View.GONE);
                        GlobalFunctions.EnableLayout(container);
                        int responseCode = response.code();
                        if (responseCode == 200) {
                            Snackbar.make(childView, getString(R.string.checkYourMail), Snackbar.LENGTH_SHORT).show();
                            Navigator.loadFragment(activity, LoginFragment.newInstance(activity), R.id.app_bar_main_fl_mainContainer, true);
                        } else if (responseCode == 201) {
                            Log.d(Constants.M3RESNA_APP, "email is empty");
                        } else if (responseCode == 203) {
                            Log.d(Constants.M3RESNA_APP, "email not found");
                            Toast.makeText(activity, "email not found", Toast.LENGTH_SHORT).show();
                        }else {
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

}
