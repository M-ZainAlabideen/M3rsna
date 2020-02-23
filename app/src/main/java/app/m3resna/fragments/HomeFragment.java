package app.m3resna.fragments;

import android.os.Bundle;
import android.os.Handler;
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
import androidx.viewpager.widget.ViewPager;

import com.duolingo.open.rtlviewpager.RtlViewPager;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import app.m3resna.MainActivity;
import app.m3resna.R;
import app.m3resna.adapters.MainAdapter;
import app.m3resna.adapters.SliderAdapter;
import app.m3resna.classes.Constants;
import app.m3resna.classes.GlobalFunctions;
import app.m3resna.classes.Navigator;
import app.m3resna.classes.SessionManager;
import app.m3resna.webservices.RetrofitConfig;
import app.m3resna.webservices.models.Category;
import app.m3resna.webservices.models.Slider;
import butterknife.BindView;
import butterknife.ButterKnife;
import me.relex.circleindicator.CircleIndicator;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {
    public static FragmentActivity activity;
    public static HomeFragment fragment;
    private View childView;
    private SessionManager sessionManager;
    private ArrayList<Slider> sliderImagesList = new ArrayList<>();
    private SliderAdapter sliderAdapter;
    private int currentPage = 0;
    private int NUM_PAGES = 0;

    private ArrayList<Category> categoriesList = new ArrayList<>();
    private LinearLayoutManager layoutManager;
    private MainAdapter categoriesAdapter;

    @BindView(R.id.fragment_home_cl_container)
    ConstraintLayout container;
    @BindView(R.id.fragment_home_vp_slider)
    RtlViewPager slider;
    @BindView(R.id.fragment_home_ci_sliderCircles)
    CircleIndicator sliderCircles;
    @BindView(R.id.fragment_home_rv_categories)
    RecyclerView categories;
    @BindView(R.id.loading)
    ProgressBar loading;

    public static HomeFragment newInstance(FragmentActivity activity) {
        fragment = new HomeFragment();
        HomeFragment.activity = activity;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        childView = inflater.inflate(R.layout.fragment_home, container, false);
        ButterKnife.bind(this, childView);
        return childView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MainActivity.setupAppbar(true, true, false, null,
                false, true, false, true, true, Constants.CATEGORY);
        GlobalFunctions.hasNewNotificationsApi(activity);
        sessionManager = new SessionManager(activity);

        if (!GlobalFunctions.isNetworkConnected(activity)) {
            Snackbar.make(childView, getString(R.string.noConnection), Snackbar.LENGTH_SHORT).show();
        } else {
            sliderApi();
        }

        if (categoriesList.size() == 0) {
            categoriesApi();
        } else {
            loading.setVisibility(View.GONE);
        }

        layoutManager = new LinearLayoutManager(activity);
        categoriesAdapter = new MainAdapter(activity, categoriesList, null, null, Constants.CATEGORY, new MainAdapter.OnItemClickListener() {
            @Override
            public void imageClick(int position) {
                Navigator.loadFragment(activity, FullAdFragment.newInstance(activity, false, categoriesList.get(position))
                        , R.id.app_bar_main_fl_mainContainer, true);
            }

            @Override
            public void favClick(int position, ImageView makeRemoveFav) {
                //not used in Home
            }

            @Override
            public void renewClick(int position) {

            }
        });
        categories.setLayoutManager(layoutManager);
        categories.setAdapter(categoriesAdapter);

        /*make slider autoPlay , every 5 seconds the image will replace with the next one
         * when lastImage come , the cycle will start from 0 again*/
        slider.setCurrentItem(0, true);
        final Handler handler = new Handler();
        final Runnable update = new Runnable() {
            @Override
            public void run() {
                if (currentPage == NUM_PAGES) {
                    currentPage = 0;
                }
                slider.setCurrentItem(currentPage++, true);
            }
        };


        Timer swipeTimer = new Timer();
        swipeTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.post(update);
            }
        }, 5000, 5000);


    }

    private void sliderApi() {
        RetrofitConfig.getServices().HOME_SLIDER_CALL(sessionManager.getUserToken(), sessionManager.getCountryId())
                .enqueue(new Callback<ArrayList<Slider>>() {
                    @Override
                    public void onResponse(Call<ArrayList<Slider>> call, Response<ArrayList<Slider>> response) {
                        loading.setVisibility(View.GONE);
                        int responseCode = response.code();
                        if (responseCode == 200) {
                            if (response.body() != null && response.body().size() > 0) {
                                sliderImagesList.clear();
                                sliderImagesList.addAll(response.body());
                                setupSlider();
                            }
                        } else if (responseCode == 201) {
                            Log.d(Constants.M3RESNA_APP, "country not found");
                        } else {
                            Snackbar.make(childView, getString(R.string.generalError), Snackbar.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ArrayList<Slider>> call, Throwable t) {
                        GlobalFunctions.generalErrorMessage(loading, activity);
                    }
                });
    }

    private void setupSlider() {
        NUM_PAGES = sliderImagesList.size();
        sliderAdapter = new SliderAdapter(activity, sliderImagesList, false);
        slider.setAdapter(sliderAdapter);
        sliderCircles.setViewPager(slider);
        sliderCircles.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float v, int i1) {
                currentPage = position;
            }

            @Override
            public void onPageSelected(int i) {

            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });

    }

    private void categoriesApi() {
        GlobalFunctions.DisableLayout(container);
        RetrofitConfig.getServices().CATEGORIES_CALL(sessionManager.getUserToken())
                .enqueue(new Callback<ArrayList<Category>>() {
                    @Override
                    public void onResponse(Call<ArrayList<Category>> call, Response<ArrayList<Category>> response) {
                        loading.setVisibility(View.GONE);
                        GlobalFunctions.EnableLayout(container);
                        int responseCode = response.code();
                        if (responseCode == 200) {
                            if (response.body() != null && response.body().size() > 0) {
                                categoriesList.clear();
                                categoriesList.addAll(response.body());
                                categoriesAdapter.notifyDataSetChanged();
                            }
                        } else if (responseCode == 203) {
                            Log.d(Constants.M3RESNA_APP, "categories not found");
                        } else {
                            Snackbar.make(childView, getString(R.string.generalError), Snackbar.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ArrayList<Category>> call, Throwable t) {
                        GlobalFunctions.generalErrorMessage(loading, activity);
                        GlobalFunctions.EnableLayout(container);
                    }
                });
    }

}
