package app.m3resna.fragments;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.google.android.material.snackbar.Snackbar;

import app.m3resna.MainActivity;
import app.m3resna.R;
import app.m3resna.classes.Constants;
import app.m3resna.classes.Navigator;
import app.m3resna.classes.SessionManager;
import butterknife.BindView;
import butterknife.ButterKnife;

/*
 * UrlsFragment is used for opening the links and Urls inside the webView
 * without leave the app*/
public class UrlsFragment extends Fragment {
    static FragmentActivity activity;
    static UrlsFragment fragment;
    private View childView;
    private SessionManager sessionManager;
    private String Url;
    private boolean isPayment;
    private String flag;
    @BindView(R.id.loading)
    ProgressBar loading;
    //webView for loading the content of Url
    @BindView(R.id.fragment_urls_wv_webView)
    WebView webView;

    //pass the Url as parameter when fragment is loaded for loading Url content
    public static UrlsFragment newInstance(FragmentActivity activity, String Url, boolean isPayment, String flag) {
        fragment = new UrlsFragment();
        UrlsFragment.activity = activity;
        if (Url.contains("http"))
            fragment.Url = Url;
        else
            fragment.Url = "https://" + Url;
        fragment.isPayment = isPayment;
        fragment.flag = flag;
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        childView = inflater.inflate(R.layout.fragment_urls, container, false);
        ButterKnife.bind(this, childView);
        return childView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MainActivity.setupAppbar(true, false, false, null,
                true, false, false, false,
                false, null);
        sessionManager = new SessionManager(activity);
        //WebView should execute JavaScript
        webView.getSettings().setJavaScriptEnabled(true);
        /*when enabling this Property is that it would then allow ANY website
        that takes advantage of DOM storage to use said storage options on the device*/
        webView.getSettings().setDomStorageEnabled(true);
        webView.setOverScrollMode(WebView.OVER_SCROLL_NEVER);
        //load the Url content inside the webView
        webView.loadUrl(Url);

        if (isPayment) {
            setWebClientForPayment();
        } else {
            if (flag != null && !flag.isEmpty()) {
                if (flag.equals(Constants.VIDEO)) {
                    videoPlayingSetup();
                }
            }
            setWebClient();
        }

    }

    private void videoPlayingSetup() {
        //for video playing
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webView.getSettings().setPluginState(WebSettings.PluginState.ON);
        webView.getSettings().setMediaPlaybackRequiresUserGesture(false);
        webView.setWebChromeClient(new WebChromeClient());
    }

    private void setWebClient() {
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith("vnd.youtube")) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                    return true;
                } else {
                    return false;
                }
            }

            public void onPageFinished(WebView view, String url) {
                // hide progress of Loading after finishing
                loading.setVisibility(View.GONE);
            }
        });
    }

    private void setWebClientForPayment() {
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.contains(Constants.SUCCESS_PAGE)) {
                        clearFragments(2);
                    Navigator.loadFragment(activity,HomeFragment.newInstance(activity), R.id.app_bar_main_fl_mainContainer, true);
                        Snackbar.make(childView, getString(R.string.subscriptionSuccessfully), Snackbar.LENGTH_SHORT).show();
                } else if (url.contains(Constants.ERROR_PAGE)) {
                    getFragmentManager().popBackStack();
                    Snackbar.make(childView, activity.getString(R.string.OperationFailed), Snackbar.LENGTH_LONG).show();
                    return false;

                }
                webView.loadUrl(url);

                return true;

            }

            @Override
            public void onPageFinished(WebView view, String url) {
                // hide progress of Loading after finishing
                loading.setVisibility(View.GONE);
            }


        });
    }

    private void clearFragments(int count) {
        for (int i = 1; i <= count; i++) {
            getFragmentManager().popBackStack();
        }
    }
}