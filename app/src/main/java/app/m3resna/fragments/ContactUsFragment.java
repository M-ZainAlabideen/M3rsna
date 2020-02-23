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

import java.util.ArrayList;

import app.m3resna.MainActivity;
import app.m3resna.R;
import app.m3resna.classes.Constants;
import app.m3resna.classes.FixControl;
import app.m3resna.classes.GlobalFunctions;
import app.m3resna.classes.Navigator;
import app.m3resna.classes.SessionManager;
import app.m3resna.webservices.RetrofitConfig;
import app.m3resna.webservices.models.Contact;
import app.m3resna.webservices.requests.SendMessageRequest;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ContactUsFragment extends Fragment {
    public static FragmentActivity activity;
    public static ContactUsFragment fragment;
    private View childView;
    private SessionManager sessionManager;
    private String facebook;
    private String snapChat;
    private String instagram;
    private String twitter;
    private String whatsApp;

    @BindView(R.id.fragment_contact_us_cl_container)
    ConstraintLayout container;
    @BindView(R.id.fragment_contact_us_et_name)
    EditText name;
    @BindView(R.id.fragment_contact_us_et_email)
    EditText email;
    @BindView(R.id.fragment_contact_us_et_message)
    EditText message;
    @BindView(R.id.loading)
    ProgressBar loading;

    public static ContactUsFragment newInstance(FragmentActivity activity) {
        fragment = new ContactUsFragment();
        ContactUsFragment.activity = activity;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        childView = inflater.inflate(R.layout.fragment_contact_us, container, false);
        ButterKnife.bind(this, childView);
        return childView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MainActivity.setupAppbar(true, false, true, getString(R.string.contactUs),
                true, true, false, true, true, Constants.CATEGORY);
        GlobalFunctions.hasNewNotificationsApi(activity);
        FixControl.setupUI(container, activity);
        sessionManager = new SessionManager(activity);
        if (!GlobalFunctions.isNetworkConnected(activity)) {
            Snackbar.make(childView, getString(R.string.noConnection), Snackbar.LENGTH_SHORT).show();
        } else {
            contactsApi();
        }

        if (sessionManager.isLoggedIn()) {
            name.setText(sessionManager.getName());
            email.setText(sessionManager.getEmail());
        }
    }

    @OnClick(R.id.fragment_contact_us_iv_facebook)
    public void facebookClick() {
        if (facebook == null || facebook.isEmpty()) {
            Snackbar.make(childView, getString(R.string.invalidLink), Snackbar.LENGTH_SHORT).show();
        } else {
            Navigator.loadFragment(activity, UrlsFragment.newInstance(activity, facebook, false, null), R.id.app_bar_main_fl_mainContainer, false);
        }
    }

    @OnClick(R.id.fragment_contact_us_iv_snapChat)
    public void snapChatClick() {
        if (snapChat == null || snapChat.isEmpty()) {
            Snackbar.make(childView, getString(R.string.invalidLink), Snackbar.LENGTH_SHORT).show();
        } else {
            Navigator.loadFragment(activity, UrlsFragment.newInstance(activity, snapChat, false, null), R.id.app_bar_main_fl_mainContainer, false);
        }
    }

    @OnClick(R.id.fragment_contact_us_iv_whatsApp)
    public void whatsAppClick() {
        if (whatsApp == null || whatsApp.isEmpty()) {
            Snackbar.make(childView, getString(R.string.invalidLink), Snackbar.LENGTH_SHORT).show();
        } else {
            Navigator.loadFragment(activity, UrlsFragment.newInstance(activity, whatsApp, false, null), R.id.app_bar_main_fl_mainContainer, false);
        }
    }

    @OnClick(R.id.fragment_contact_us_iv_instagram)
    public void instagramClick() {
        if (instagram == null || instagram.isEmpty()) {
            Snackbar.make(childView, getString(R.string.invalidLink), Snackbar.LENGTH_SHORT).show();
        } else {
            Navigator.loadFragment(activity, UrlsFragment.newInstance(activity, instagram, false, null), R.id.app_bar_main_fl_mainContainer, false);
        }
    }

    @OnClick(R.id.fragment_contact_us_iv_twitter)
    public void twitterClick() {
        if (twitter == null || twitter.isEmpty()) {
            Snackbar.make(childView, getString(R.string.invalidLink), Snackbar.LENGTH_SHORT).show();
        } else {
            Navigator.loadFragment(activity, UrlsFragment.newInstance(activity, twitter, false, null), R.id.app_bar_main_fl_mainContainer, false);
        }
    }

    @OnClick(R.id.fragment_contact_us_tv_send)
    public void sendClick() {
        if (!GlobalFunctions.isNetworkConnected(activity)) {
            Snackbar.make(childView, getString(R.string.noConnection), Snackbar.LENGTH_SHORT).show();
        } else {
            String nameStr = name.getText().toString();
            String emailStr = email.getText().toString();
            String messageStr = message.getText().toString();
            if (nameStr == null || nameStr.isEmpty()) {
                name.setError(getString(R.string.enterName));
            } else {
                name.setError(null);
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
            if (messageStr == null || messageStr.isEmpty()) {
                message.setError(getString(R.string.enterMessage));
            } else {
                message.setError(null);
            }

            if (nameStr != null && !nameStr.isEmpty() && emailStr != null && !emailStr.isEmpty()
                    && messageStr != null && !messageStr.isEmpty() && FixControl.isValidEmail(emailStr)) {
                SendMessageRequest myRequest = new SendMessageRequest();
                myRequest.id = 0;
                myRequest.name = nameStr;
                myRequest.email = emailStr;
                myRequest.message = messageStr;
                sendMessageApi(myRequest);
            }
        }
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
                                    if (item.name.equals(Constants.INSTAGRAM))
                                        instagram = item.value;
                                    else if (item.name.equals(Constants.TWITTER))
                                        twitter = item.value;
                                    else if (item.name.equals(Constants.WHATS_APP))
                                        whatsApp = item.value;
                                    else if (item.name.equals(Constants.FACEBOOK))
                                        facebook = item.value;
                                    else if (item.name.equals(Constants.SNAP_CHAT))
                                        snapChat = item.value;
                                }
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

    public void sendMessageApi(SendMessageRequest request) {
        loading.setVisibility(View.VISIBLE);
        GlobalFunctions.DisableLayout(container);
        RetrofitConfig.getServices().SEND_MESSAGE_CALL(request)
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        loading.setVisibility(View.GONE);
                        GlobalFunctions.EnableLayout(container);
                        int responseCode = response.code();
                        if (responseCode == 200) {
                            Snackbar.make(childView, getString(R.string.thanksMessage), Snackbar.LENGTH_SHORT).show();
                            name.setText("");
                            email.setText("");
                            message.setText("");
                            Navigator.loadFragment(activity, HomeFragment.newInstance(activity), R.id.app_bar_main_fl_mainContainer, true);
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

