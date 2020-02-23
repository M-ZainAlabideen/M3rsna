package app.m3resna.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.google.android.material.snackbar.Snackbar;

import app.m3resna.MainActivity;
import app.m3resna.R;
import app.m3resna.classes.Constants;
import app.m3resna.classes.GlobalFunctions;
import app.m3resna.classes.Navigator;
import app.m3resna.classes.SessionManager;
import app.m3resna.webservices.RetrofitConfig;
import app.m3resna.webservices.responses.RemainCreditResponse;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RemainCreditFragment extends Fragment {
    public static FragmentActivity activity;
    public static RemainCreditFragment fragment;
    private View childView;
    private SessionManager sessionManager;

    @BindView(R.id.fragment_remain_credit_cl_container)
    ConstraintLayout container;
    @BindView(R.id.fragment_remain_credit_tv_addPackage)
    TextView addPackage;
    @BindView(R.id.fragment_subscription_tv_remainCredit)
    TextView remainCredit;
    @BindView(R.id.fragment_remain_credit_cv_container)
    CardView remainCreditContainer;
    @BindView(R.id.loading)
    ProgressBar loading;

    public static RemainCreditFragment newInstance(FragmentActivity activity) {
        fragment = new RemainCreditFragment();
        RemainCreditFragment.activity = activity;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        childView = inflater.inflate(R.layout.fragment_remain_credit, container, false);
        ButterKnife.bind(this, childView);
        return childView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MainActivity.setupAppbar(true, false, true, getString(R.string.myPackages),
                true, true, false, true, true, Constants.ACCOUNT);
        GlobalFunctions.hasNewNotificationsApi(activity);
        sessionManager = new SessionManager(activity);
        remainCredit.setVisibility(View.GONE);
        remainCreditContainer.setVisibility(View.GONE);

        if (!GlobalFunctions.isNetworkConnected(activity)) {
            Snackbar.make(childView, getString(R.string.noConnection), Snackbar.LENGTH_SHORT).show();
        } else {
            remainCreditApi();
        }
    }

    @OnClick(R.id.fragment_remain_credit_tv_addPackage)
    public void addPackageCLick() {
        Navigator.loadFragment(activity, PackagesFragment.newInstance(activity, null), R.id.app_bar_main_fl_mainContainer, true);
    }


    private void remainCreditApi() {
        GlobalFunctions.DisableLayout(container);
        RetrofitConfig.getServices().REMAIN_CREDIT_CALL(sessionManager.getUserToken(), sessionManager.getUserId())
                .enqueue(new Callback<RemainCreditResponse>() {
                    @Override
                    public void onResponse(Call<RemainCreditResponse> call, Response<RemainCreditResponse> response) {
                        loading.setVisibility(View.GONE);
                        GlobalFunctions.EnableLayout(container);
                        int responseCode = response.code();
                        if (responseCode == 200 && response.body() != null) {
                            if (response.body().remainCredit != 0) {
                                remainCredit.setVisibility(View.VISIBLE);
                                remainCreditContainer.setVisibility(View.VISIBLE);
                                remainCredit.setText(getString(R.string.availableCredit) + " " + response.body().remainCredit);
                            }
                        } else if (responseCode == 203) {
                            Log.d(Constants.M3RESNA_APP, "user not found");
                        } else {
                            Snackbar.make(childView, getString(R.string.generalError), Snackbar.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<RemainCreditResponse> call, Throwable t) {
                        GlobalFunctions.generalErrorMessage(loading, activity);
                        GlobalFunctions.EnableLayout(container);
                    }
                });
    }

}


