package app.m3resna.fragments;

import android.os.Bundle;
import android.util.Log;
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

import app.m3resna.MainActivity;
import app.m3resna.R;
import app.m3resna.classes.Constants;
import app.m3resna.classes.FixControl;
import app.m3resna.classes.GlobalFunctions;
import app.m3resna.classes.Navigator;
import app.m3resna.classes.SessionManager;
import app.m3resna.webservices.RetrofitConfig;
import app.m3resna.webservices.requests.ChangePasswordRequest;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChangePasswordFragment extends Fragment {
    public static FragmentActivity activity;
    public static ChangePasswordFragment fragment;
    private View childView;
    private SessionManager sessionManager;

    @BindView(R.id.fragment_change_password_cl_container)
    ConstraintLayout container;
    @BindView(R.id.fragment_change_password_et_oldPassword)
    EditText oldPassword;
    @BindView(R.id.fragment_change_password_et_newPassword)
    EditText newPassword;
    @BindView(R.id.fragment_change_password_et_confirmPassword)
    EditText confirmPassword;
    @BindView(R.id.loading)
    ProgressBar loading;

    public static ChangePasswordFragment newInstance(FragmentActivity activity) {
        fragment = new ChangePasswordFragment();
        ChangePasswordFragment.activity = activity;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        childView = inflater.inflate(R.layout.fragment_change_password, container, false);
        ButterKnife.bind(this, childView);
        return childView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MainActivity.setupAppbar(true, false, true, getString(R.string.changePassword),
                true, true, false, true, true, Constants.ACCOUNT);
        GlobalFunctions.hasNewNotificationsApi(activity);
        FixControl.setupUI(container, activity);
        sessionManager = new SessionManager(activity);
        loading.setVisibility(View.GONE);
    }

    @OnClick(R.id.fragment_change_password_tv_done)
    public void doneClick() {
        if (!GlobalFunctions.isNetworkConnected(activity)) {
            Snackbar.make(childView, getString(R.string.noConnection), Snackbar.LENGTH_SHORT).show();
        } else {
            String oldPasswordStr = oldPassword.getText().toString();
            String newPasswordStr = newPassword.getText().toString();
            String confirmPasswordStr = confirmPassword.getText().toString();

            if (oldPasswordStr == null || oldPasswordStr.isEmpty()) {
                oldPassword.setError(getString(R.string.enterOldPassword));
            } else {
                if (oldPasswordStr.length() < 6) {
                    oldPassword.setError(getString(R.string.invalidPassword));
                } else {
                    oldPassword.setError(null);
                }
            }

            if (newPasswordStr == null || newPasswordStr.isEmpty()) {
                newPassword.setError(getString(R.string.enterNewPassword));
            } else {
                if (newPasswordStr.length() < 6) {
                    newPassword.setError(getString(R.string.invalidPassword));
                } else {
                    newPassword.setError(null);
                }

            }

            if (confirmPasswordStr == null || confirmPasswordStr.isEmpty()) {
                confirmPassword.setError(getString(R.string.enterConfirmPassword));
            } else {
                if (confirmPasswordStr.length() < 6) {
                    confirmPassword.setError(getString(R.string.invalidPassword));
                } else {
                    if (newPasswordStr != null && !newPasswordStr.isEmpty() &&
                            confirmPasswordStr != null && !confirmPasswordStr.isEmpty()
                            && newPasswordStr.length() >= 6 && confirmPasswordStr.length() >= 6
                            && !newPasswordStr.equals(confirmPasswordStr)) {
                        confirmPassword.setError(getString(R.string.passwordMisMatched));

                    } else {
                        confirmPassword.setError(null);
                    }
                }
            }

            if (oldPasswordStr != null && !oldPasswordStr.isEmpty() &&
                    newPasswordStr != null && !newPasswordStr.isEmpty() &&
                    confirmPasswordStr != null && !confirmPasswordStr.isEmpty()
                    && newPasswordStr.equals(confirmPasswordStr) && newPasswordStr.length() >= 6) {
                ChangePasswordRequest myRequest = new ChangePasswordRequest();
                myRequest.userId = sessionManager.getUserId();
                myRequest.oldPassword = oldPasswordStr;
                myRequest.password = newPasswordStr;
                changePasswordApi(myRequest);
            }

        }
    }

    private void changePasswordApi(ChangePasswordRequest request) {
        loading.setVisibility(View.VISIBLE);
        GlobalFunctions.DisableLayout(container);
        RetrofitConfig.getServices().CHANGE_PASSWORD_CALL(sessionManager.getUserToken(), request)
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        loading.setVisibility(View.GONE);
                        GlobalFunctions.EnableLayout(container);
                        int responseCode = response.code();
                        if (responseCode == 200) {
                            Snackbar.make(childView, getString(R.string.passwordChangedSuccessfully), Snackbar.LENGTH_SHORT).show();
                            Navigator.loadFragment(activity, AccountFragment.newInstance(activity), R.id.app_bar_main_fl_mainContainer, true);
                        } else if (responseCode == 201) {
                            Log.d(Constants.M3RESNA_APP, "user not found");
                        } else if (responseCode == 202) {
                            oldPassword.setError(getString(R.string.oldPasswordIncorrect));
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

}
