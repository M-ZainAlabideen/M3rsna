package app.m3resna.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import app.m3resna.classes.GlobalFunctions;
import app.m3resna.webservices.RetrofitConfig;
import app.m3resna.webservices.responses.AboutUsResponse;
import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class AboutUsFragment extends Fragment {
    public static FragmentActivity activity;
    public static AboutUsFragment fragment;
    private View childView;

    @BindView(R.id.fragment_about_us_wv_content)
    WebView content;
    @BindView(R.id.loading)
    ProgressBar loading;

    public static AboutUsFragment newInstance(FragmentActivity activity, boolean isTerms) {
        fragment = new AboutUsFragment();
        AboutUsFragment.activity = activity;
        Bundle b = new Bundle();
        b.putBoolean(Constants.IS_TERMS, isTerms);
        fragment.setArguments(b);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        childView = inflater.inflate(R.layout.fragment_about_us, container, false);
        ButterKnife.bind(this, childView);
        return childView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String title = "";
        if (fragment.getArguments().getBoolean(Constants.IS_TERMS)) {
            title = getString(R.string.terms);
        } else {
            title = getString(R.string.aboutUs);
        }
        MainActivity.setupAppbar(true, false, true, title,
                true, true, false, true, true, Constants.ACCOUNT);
        GlobalFunctions.hasNewNotificationsApi(activity);
        if (!GlobalFunctions.isNetworkConnected(activity)) {
            Snackbar.make(childView, getString(R.string.noConnection), Snackbar.LENGTH_SHORT).show();
        } else {
            aboutUsApi();
        }
    }

    private void aboutUsApi() {
        loading.setVisibility(View.VISIBLE);
        RetrofitConfig.getServices().ABOUT_US_CALL()
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        loading.setVisibility(View.GONE);
                        int responseCode = response.code();
                        if (responseCode == 200) {
                            AboutUsResponse aboutUsResponse = null;
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

                                Type type = new TypeToken<AboutUsResponse>() {
                                }.getType();

                                JsonReader reader = new JsonReader(new StringReader(outResponse));

                                reader.setLenient(true);

                                aboutUsResponse = new Gson().fromJson(jsonResponse, type);
                            }
                                if (fragment.getArguments().getBoolean(Constants.IS_TERMS)) {
                                    setupWebView(aboutUsResponse.getTerms());
                                } else {
                                    setupWebView(aboutUsResponse.getAboutUs());

                                }
                            }
                         else {
                            Snackbar.make(childView, getString(R.string.generalError), Snackbar.LENGTH_SHORT).show();
                        }

                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        GlobalFunctions.generalErrorMessage(loading, activity);
                    }
                });
    }

    private void setupWebView(String contentStr) {
        contentStr = contentStr.replace("font", "f");
        contentStr = contentStr.replace("color", "c");
        contentStr = contentStr.replace("size", "s");
        String fontName;
        if (MainActivity.isEnglish)
            fontName = Constants.MONTSERRAT_REGULAR;
        else
            fontName = Constants.CAIRO_REGULAR;

        String head = "<head><style>@font-face {font-family: 'verdana';src: url('file:///android_asset/" + fontName + "');}body {font-family: 'verdana';}</style></head>";
        String htmlData = "<html>" + head + (MainActivity.isEnglish ? "<body dir=\"ltr\"" : "<body dir=\"rtl\"") + " style=\"font-family: verdana\">" +
                contentStr + "</body></html>";
        content.loadDataWithBaseURL("", htmlData, "text/html; charset=utf-8", "utf-8", "");

    }
}

