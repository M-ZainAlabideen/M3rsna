package app.m3resna.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

import app.m3resna.MainActivity;
import app.m3resna.R;
import app.m3resna.adapters.MainAdapter;
import app.m3resna.classes.Constants;
import app.m3resna.classes.GlobalFunctions;
import app.m3resna.classes.Navigator;
import app.m3resna.classes.SessionManager;
import app.m3resna.webservices.RetrofitConfig;
import app.m3resna.webservices.models.Product;
import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyAdsFragment extends Fragment {
    public static FragmentActivity activity;
    public static MyAdsFragment fragment;
    private View childView;
    private SessionManager sessionManager;

    private ArrayList<Product> myAdsList = new ArrayList<>();
    private LinearLayoutManager layoutManager;
    private MainAdapter myAdsAdapter;
    private int pageIndex = 1;
    private boolean isLoading = false;
    private boolean isLastPage = false;

    @BindView(R.id.fragment_my_ads_cl_container)
    ConstraintLayout container;
    @BindView(R.id.fragment_my_ads_rv_myAds)
    RecyclerView myAds;
    @BindView(R.id.loading)
    ProgressBar loading;

    public static MyAdsFragment newInstance(FragmentActivity activity) {
        fragment = new MyAdsFragment();
        MyAdsFragment.activity = activity;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        childView = inflater.inflate(R.layout.fragment_my_ads, container, false);
        ButterKnife.bind(this, childView);
        return childView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MainActivity.setupAppbar(true, false, true, getString(R.string.myAds),
                true, true, false, true, true, Constants.ACCOUNT);
        GlobalFunctions.hasNewNotificationsApi(activity);
        sessionManager = new SessionManager(activity);

        if (myAdsList.size() == 0) {
            if (!GlobalFunctions.isNetworkConnected(activity)) {
                Snackbar.make(childView, getString(R.string.noConnection), Snackbar.LENGTH_SHORT).show();
            } else {
                myAdsApi();
            }
        } else {
            loading.setVisibility(View.GONE);
        }

        layoutManager = new LinearLayoutManager(activity);
        myAdsAdapter = new MainAdapter(activity, null, null, myAdsList, Constants.MY_ADS, new MainAdapter.OnItemClickListener() {
            @Override
            public void imageClick(int position) {
                Navigator.loadFragment(activity, AddAdFragment.newInstance(activity, myAdsList.get(position).id), R.id.app_bar_main_fl_mainContainer, true);
            }

            @Override
            public void favClick(int position, ImageView makeRemoveFav) {

            }

            @Override
            public void renewClick(int position) {
                renewAdApi(position);
            }
        });
        myAds.setLayoutManager(layoutManager);
        myAds.setAdapter(myAdsAdapter);
    }

    private void myAdsApi() {
        GlobalFunctions.DisableLayout(container);
        RetrofitConfig.getServices().USER_ADS_CALL(sessionManager.getUserToken(), sessionManager.getUserId(), pageIndex)
                .enqueue(new Callback<ArrayList<Product>>() {
                    @Override
                    public void onResponse(Call<ArrayList<Product>> call, Response<ArrayList<Product>> response) {
                        loading.setVisibility(View.GONE);
                        GlobalFunctions.EnableLayout(container);
                        int responseCode = response.code();
                        if (response.code() == 200) {
                            if (response.body() != null && response.body().size() > 0) {
                                myAdsList.clear();
                                myAdsList.addAll(response.body());
                                myAdsAdapter.notifyDataSetChanged();
                                myAds.addOnScrollListener(new RecyclerView.OnScrollListener() {
                                    @Override
                                    public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                                        super.onScrollStateChanged(recyclerView, newState);
                                    }

                                    @Override
                                    public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                                        super.onScrolled(recyclerView, dx, dy);
                                        if (!isLastPage) {
                                            int visibleItemCount = layoutManager.getChildCount();

                                            int totalItemCount = layoutManager.getItemCount();

                                            int pastVisibleItems = layoutManager.findFirstVisibleItemPosition();

                                /*isLoading variable used for check if the user send many requests
                                for pagination(make many scrolls in the same time)
                                1- if isLoading true >> there is request already sent so,
                                no more requests till the response of last request coming
                                2- else >> send new request for load more data (News)*/
                                            if (!isLoading) {

                                                if ((visibleItemCount + pastVisibleItems) >= totalItemCount) {
                                                    isLoading = true;

                                                    pageIndex = pageIndex + 1;

                                                    getMoreAds();

                                                }
                                            }
                                        }
                                    }
                                });
                            } else {
                                isLastPage = true;
                                Snackbar.make(childView, getString(R.string.noAdsFound), Snackbar.LENGTH_SHORT).show();
                            }
                        } else if (responseCode == 201) {
                            Log.d(Constants.M3RESNA_APP, "user not found");
                        } else {
                            Snackbar.make(childView, getString(R.string.generalError), Snackbar.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ArrayList<Product>> call, Throwable t) {
                        GlobalFunctions.generalErrorMessage(loading, activity);
                        GlobalFunctions.EnableLayout(container);
                    }
                });
    }

    private void getMoreAds() {
        loading.setVisibility(View.VISIBLE);
        GlobalFunctions.DisableLayout(container);
        RetrofitConfig.getServices().USER_ADS_CALL(sessionManager.getUserToken(), sessionManager.getUserId(), pageIndex)
                .enqueue(new Callback<ArrayList<Product>>() {
                    @Override
                    public void onResponse(Call<ArrayList<Product>> call, Response<ArrayList<Product>> response) {
                        loading.setVisibility(View.GONE);
                        GlobalFunctions.EnableLayout(container);
                        int responseCode = response.code();
                        if (responseCode == 200) {
                            if (response.body().size() > 0) {
                                myAdsList.addAll(response.body());
                                myAdsAdapter.notifyDataSetChanged();

                            } else {
                                isLastPage = true;
                                pageIndex = pageIndex - 1;
                            }
                            isLoading = false;

                        }
                    }

                    @Override
                    public void onFailure(Call<ArrayList<Product>> call, Throwable t) {
                        GlobalFunctions.generalErrorMessage(loading, activity);
                        GlobalFunctions.EnableLayout(container);
                    }
                });

    }

    private void renewAdApi(int productId) {
        loading.setVisibility(View.VISIBLE);
        GlobalFunctions.DisableLayout(container);
        RetrofitConfig.getServices().RENEW_CALL(sessionManager.getUserToken(), productId, sessionManager.getUserId())
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        loading.setVisibility(View.GONE);
                        GlobalFunctions.EnableLayout(container);
                        int responseCode = response.code();
                        if (responseCode == 200) {
                            pageIndex = 0;
                            isLastPage = false;
                            isLoading = false;
                            myAdsApi();
                        } else if (responseCode == 201) {
                            Snackbar.make(childView, getString(R.string.noEnoughBalance), Snackbar.LENGTH_SHORT).show();
                        } else if (responseCode == 203) {
                            Log.d(Constants.M3RESNA_APP,"user not found");
                        } else if (responseCode == 205) {
                            Log.d(Constants.M3RESNA_APP,"add type not found");
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {

                    }
                });
    }
}

