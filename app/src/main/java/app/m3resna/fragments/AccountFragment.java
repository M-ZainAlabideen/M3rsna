package app.m3resna.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import app.m3resna.MainActivity;
import app.m3resna.R;
import app.m3resna.classes.Constants;
import app.m3resna.classes.GlobalFunctions;
import app.m3resna.classes.Navigator;
import app.m3resna.classes.SessionManager;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AccountFragment extends Fragment {
    public static FragmentActivity activity;
    public static AccountFragment fragment;
    private View childView;
    private SessionManager sessionManager;
    

    public static AccountFragment newInstance(FragmentActivity activity) {
        fragment = new AccountFragment();
        AccountFragment.activity = activity;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        childView = inflater.inflate(R.layout.fragment_account, container, false);
        ButterKnife.bind(this, childView);
        return childView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MainActivity.setupAppbar(true, false, true, getString(R.string.myAccount),
                true, true, false, true, true, Constants.ACCOUNT);
        GlobalFunctions.hasNewNotificationsApi(activity);
        sessionManager = new SessionManager(activity);

    }

    @OnClick(R.id.fragment_account_v_ads)
    public void myAdsClick(){
        Navigator.loadFragment(activity, MyAdsFragment.newInstance(activity),R.id.app_bar_main_fl_mainContainer,true);
    }

    @OnClick(R.id.fragment_account_v_editProfile)
    public void editProfileClick(){
        Navigator.loadFragment(activity,EditProfileFragment.newInstance(activity),R.id.app_bar_main_fl_mainContainer,true);
    }

    @OnClick(R.id.fragment_account_v_changePassword)
    public void changePasswordClick(){
        Navigator.loadFragment(activity,ChangePasswordFragment.newInstance(activity),R.id.app_bar_main_fl_mainContainer,true);
    }

    @OnClick(R.id.fragment_account_v_mySubscription)
    public void mySubscriptionsClick(){
        Navigator.loadFragment(activity, RemainCreditFragment.newInstance(activity),R.id.app_bar_main_fl_mainContainer,true);
    }

    @OnClick(R.id.fragment_account_v_paymentHistory)
    public void paymentHistoryClick(){
        Navigator.loadFragment(activity, PaymentHistoryFragment.newInstance(activity),R.id.app_bar_main_fl_mainContainer,true);
    }

    @OnClick(R.id.fragment_account_v_logout)
    public void logoutClick(){
        sessionManager.logout();
        activity.finish();
        activity.overridePendingTransition(0, 0);
        startActivity(new Intent(activity, MainActivity.class));
        MainActivity.login.setVisibility(View.VISIBLE);
        Navigator.loadFragment(activity, GuestFragment.newInstance(activity), R.id.app_bar_main_fl_mainContainer, false);
    }
}

