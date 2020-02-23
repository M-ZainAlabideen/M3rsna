package app.m3resna.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.duolingo.open.rtlviewpager.RtlViewPager;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import app.m3resna.MainActivity;
import app.m3resna.R;
import app.m3resna.adapters.CitiesAdapter;
import app.m3resna.adapters.GenderAdapter;
import app.m3resna.adapters.MainAdapter;
import app.m3resna.adapters.SliderAdapter;
import app.m3resna.classes.Constants;
import app.m3resna.classes.GlobalFunctions;
import app.m3resna.classes.Navigator;
import app.m3resna.classes.RecyclerItemClickListener;
import app.m3resna.classes.SessionManager;
import app.m3resna.webservices.RetrofitConfig;
import app.m3resna.webservices.models.Category;
import app.m3resna.webservices.models.City;
import app.m3resna.webservices.models.Country;
import app.m3resna.webservices.models.Gender;
import app.m3resna.webservices.models.Product;
import app.m3resna.webservices.models.Slider;
import butterknife.BindView;
import butterknife.ButterKnife;
import me.relex.circleindicator.CircleIndicator;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductsFragment extends Fragment {
    public static FragmentActivity activity;
    public static ProductsFragment fragment;
    private View childView;
    private SessionManager sessionManager;

    private ArrayList<Slider> sliderImagesList = new ArrayList<>();
    private SliderAdapter sliderAdapter;
    private int currentPage = 0;
    private int NUM_PAGES = 0;

    private Category category;
    private int categoryId;

    private ArrayList<Gender> gendersList = new ArrayList<>();
    private LinearLayoutManager genderLayoutManager;
    private GenderAdapter genderAdapter;
    private Integer genderId = null;

    private ArrayList<Product> productsList = new ArrayList<>();
    private ArrayList<Product> commercialAdsList = new ArrayList<>();

    //this array has mixedList of productsItems and adsItems
    private ArrayList<Product> mixedList = new ArrayList<>();
    private LinearLayoutManager mixedLayoutManager;
    private MainAdapter mixedAdapter;
    private int pageIndex = 1;
    private boolean isLoading = false;
    private boolean isLastPage = false;

    private int productCounter = 0;
    private int adCounter = 0;

    private ArrayList<Country> countriesList = new ArrayList<>();
    private ArrayList<String> countriesNamesList = new ArrayList<>();
    private ArrayAdapter<String> countriesAdapter;
    private ArrayList<City> citiesList = new ArrayList<>();
    CitiesAdapter citiesAdapter;
    private int countryId;
    //cityId is optional so make it Integer not int , in case of no city selected send cityId by null
    private Integer cityId = null;

    AlertDialog dialog;
    View filterView;
    RecyclerView cities;

    @BindView(R.id.fragment_products_cl_container)
    ConstraintLayout container;
    @BindView(R.id.fragment_products_vp_slider)
    RtlViewPager slider;
    @BindView(R.id.fragment_products_ci_sliderCircles)
    CircleIndicator sliderCircles;
    @BindView(R.id.fragment_products_rv_genders)
    RecyclerView genders;
    @BindView(R.id.fragment_products_rv_mixed)
    RecyclerView mixed;
    @BindView(R.id.loading)
    ProgressBar loading;

    public static ProductsFragment newInstance(FragmentActivity activity, Category category) {
        fragment = new ProductsFragment();
        ProductsFragment.activity = activity;
        Bundle b = new Bundle();
        b.putSerializable(Constants.CATEGORY, category);
        fragment.setArguments(b);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        childView = inflater.inflate(R.layout.fragment_products, container, false);
        ButterKnife.bind(this, childView);
        return childView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        category = (Category) getArguments().getSerializable(Constants.CATEGORY);
        loading.setVisibility(View.GONE);
        categoryId = category.id;
        MainActivity.setupAppbar(true, false, true, category.getName(),
                true, true, true, true, true, Constants.CATEGORY);
        GlobalFunctions.hasNewNotificationsApi(activity);
        sessionManager = new SessionManager(activity);

        genderLayoutManager = new LinearLayoutManager(activity, RecyclerView.HORIZONTAL, false);
        genderAdapter = new GenderAdapter(activity, gendersList, new GenderAdapter.OnItemClickListener() {
            @Override
            public void itemClick(int position) {
                if (position != 0) {
                    genderId = gendersList.get(position).id;
                } else {
                    genderId = null;
                }
                GenderAdapter.selectedPosition = position;
                genderAdapter.notifyDataSetChanged();
                if (!GlobalFunctions.isNetworkConnected(activity)) {
                    Snackbar.make(childView, getString(R.string.noConnection), Snackbar.LENGTH_SHORT).show();
                } else {
                    initializePagination();
                    productsApi(categoryId, countryId, cityId, genderId);
                }
            }
        });
        genders.setLayoutManager(genderLayoutManager);
        genders.setAdapter(genderAdapter);

        mixedLayoutManager = new LinearLayoutManager(activity);
        mixedAdapter = new MainAdapter(activity, null, mixedList, null, Constants.PRODUCT, new MainAdapter.OnItemClickListener() {
            @Override
            public void imageClick(int position) {
                if (!mixedList.get(position).isAd) {
                    Navigator.loadFragment(activity, ProductDetailsFragment.newInstance(activity, mixedList.get(position).id), R.id.app_bar_main_fl_mainContainer, true);
                } else {
                    if (mixedList.get(position).link != null && !mixedList.get(position).link.isEmpty()) {
                        Navigator.loadFragment(activity, UrlsFragment.newInstance(activity, mixedList.get(position).link, false, null), R.id.app_bar_main_fl_mainContainer, false);
                    }
                }
            }

            @Override
            public void favClick(int position, ImageView makeRemoveFav) {
                makeRemoveFavApi(mixedList.get(position).id, makeRemoveFav);
            }

            @Override
            public void renewClick(int position) {

            }
        });
        mixed.setLayoutManager(mixedLayoutManager);
        mixed.setAdapter(mixedAdapter);

        if(!category.isShowType){
            genders.setVisibility(View.INVISIBLE);
        }

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


        if (sliderImagesList.size() == 0) {
            if (!GlobalFunctions.isNetworkConnected(activity)) {
                Snackbar.make(childView, getString(R.string.noConnection), Snackbar.LENGTH_SHORT).show();
            } else {
                sliderApi();
            }
        } else {
            setupSlider();
        }
        if (gendersList.size() == 0) {
            genderTypesApi();
            initializePagination();
            mixedList.clear();
        }
        if (countriesList.size() == 0) {
            GenderAdapter.selectedPosition = 0;
            genderId = null;
            countriesApi();
        }

        MainActivity.filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createFilterDialog();
            }
        });
    }

    private void sliderApi() {
        RetrofitConfig.getServices().CATEGORY_SLIDER_CALL(sessionManager.getUserToken(), sessionManager.getCountryId())
                .enqueue(new Callback<ArrayList<Slider>>() {
                    @Override
                    public void onResponse(Call<ArrayList<Slider>> call, Response<ArrayList<Slider>> response) {
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

    private void genderTypesApi() {
        RetrofitConfig.getServices().GENDERS_CALL(sessionManager.getUserToken())
                .enqueue(new Callback<ArrayList<Gender>>() {
                    @Override
                    public void onResponse(Call<ArrayList<Gender>> call, Response<ArrayList<Gender>> response) {
                        int responseCode = response.code();
                        if (responseCode == 200) {
                            if (response.body() != null && response.body().size() > 0) {
                                gendersList.clear();
                                Gender all = new Gender();
                                all.setName(getString(R.string.all));
                                all.id = 0;
                                gendersList.add(all);
                                gendersList.addAll(response.body());
                                genderAdapter.notifyDataSetChanged();
                                //in the first calling of productsApi use the currentCountryId
                                productsApi(categoryId, sessionManager.getCountryId(), cityId, genderId);
                            }
                        } else {
                            Snackbar.make(childView, getString(R.string.generalError), Snackbar.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ArrayList<Gender>> call, Throwable t) {
                        GlobalFunctions.generalErrorMessage(loading, activity);
                    }
                });
    }

    private void productsApi(int categoryId, int countryId, Integer cityId, Integer genderId) {
        loading.setVisibility(View.VISIBLE);
        productsList.clear();
        commercialAdsList.clear();
        mixedList.clear();
        mixedAdapter.notifyDataSetChanged();
        RetrofitConfig.getServices().PRODUCTS_CALL(sessionManager.getUserToken(),
                categoryId, sessionManager.getUserId(), countryId,
                cityId, genderId, pageIndex).enqueue(new Callback<ArrayList<Product>>() {
            @Override
            public void onResponse(Call<ArrayList<Product>> call, Response<ArrayList<Product>> response) {
                loading.setVisibility(View.GONE);
                GlobalFunctions.EnableLayout(container);
                int responseCode = response.code();
                if (responseCode == 200) {
                    if (response.body() != null && response.body().size() > 0) {
                        productsList.clear();
                        productsList.addAll(response.body());
                        if (productsList.size() > 3) {
                            productsAdsApi(countryId, categoryId);
                        } else {
                            setProductsData();
                        }
                        mixed.addOnScrollListener(new RecyclerView.OnScrollListener() {
                            @Override
                            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                                super.onScrollStateChanged(recyclerView, newState);
                            }

                            @Override
                            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                                super.onScrolled(recyclerView, dx, dy);
                                if (!isLastPage) {
                                    int visibleItemCount = mixedLayoutManager.getChildCount();

                                    int totalItemCount = mixedAdapter.getItemCount();

                                    int pastVisibleItems = mixedLayoutManager.findFirstVisibleItemPosition();

                                /*isLoading variable used for check if the user send many requests
                                for pagination(make many scrolls in the same time)
                                1- if isLoading true >> there is request already sent so,
                                no more requests till the response of last request coming
                                2- else >> send new request for load more data (News)*/
                                    if (!isLoading) {

                                        if ((visibleItemCount + pastVisibleItems) >= totalItemCount) {
                                            isLoading = true;

                                            pageIndex = pageIndex + 1;

                                            getMoreProducts(categoryId, countryId, cityId, genderId);

                                        }
                                    }
                                }
                            }
                        });
                    } else {
                        isLastPage = true;
                        Snackbar.make(childView, getString(R.string.noProductsFound), Snackbar.LENGTH_SHORT).show();
                    }
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

    private void getMoreProducts(int categoryId, int countryId, Integer cityId, Integer genderId) {
        loading.setVisibility(View.VISIBLE);
        GlobalFunctions.DisableLayout(container);
        RetrofitConfig.getServices().PRODUCTS_CALL(sessionManager.getUserToken(),
                categoryId, sessionManager.getUserId(), countryId,
                cityId, genderId, pageIndex)
                .enqueue(new Callback<ArrayList<Product>>() {
                    @Override
                    public void onResponse(Call<ArrayList<Product>> call, Response<ArrayList<Product>> response) {
                        loading.setVisibility(View.GONE);
                        GlobalFunctions.EnableLayout(container);
                        int responseCode = response.code();
                        if (responseCode == 200) {
                            if (response.body().size() > 0) {
                                productsList.addAll(response.body());
                                setMixData();
                                mixedAdapter.notifyDataSetChanged();
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

    private void productsAdsApi(int countryId, int categoryId) {
        loading.setVisibility(View.VISIBLE);
        GlobalFunctions.DisableLayout(container);
        RetrofitConfig.getServices().COMMERCIAL_ADS_CALL(sessionManager.getUserToken(), countryId, categoryId)
                .enqueue(new Callback<ArrayList<Product>>() {
                    @Override
                    public void onResponse(Call<ArrayList<Product>> call, Response<ArrayList<Product>> response) {
                        loading.setVisibility(View.GONE);
                        GlobalFunctions.EnableLayout(container);
                        int responseCode = response.code();
                        if (responseCode == 200) {
                            if (response.body() != null && response.body().size() > 0) {
                                commercialAdsList.clear();
                                commercialAdsList.addAll(response.body());
                                setMixData();
                                mixedAdapter.notifyDataSetChanged();
                            } else {
                                setProductsData();
                            }
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

    private void setMixData() {
        /*
         * We do a combination of the elements of the productList and the adsList,
         * where we add an element of ads after every 3 products
         * and if the adsList ends and there are still other elements in the productList
         *  we add elements from the adsList again and we start from element 0*/
        mixedList.clear();
        adCounter = 0;
        productCounter = 0;
        for (Product item : productsList) {
            if (productCounter < 3) {
                setProductInList(item);
            } else {
                try {
                    setAdInList();
                } catch (IndexOutOfBoundsException e) {
                    adCounter = 0;
                    setAdInList();
                } finally {
                    adCounter++;
                    productCounter = 0;
                    setProductInList(item);
                }

            }
        }
    }

    private void setProductsData() {
        mixedList.clear();
        for (Product item : productsList) {
            item.isAd = false;
            mixedList.add(item);
        }
        mixedAdapter.notifyDataSetChanged();
    }


    private void setProductInList(Product product) {
        product.isAd = false;
        mixedList.add(product);
        productCounter++;
    }

    private void setAdInList() {
        commercialAdsList.get(adCounter).isAd = true;
        mixedList.add(commercialAdsList.get(adCounter));
    }

    private void makeRemoveFavApi(int productId, ImageView makeRemoveFav) {
        loading.setVisibility(View.VISIBLE);
        GlobalFunctions.DisableLayout(container);
        RetrofitConfig.getServices().MAKE_REMOVE_FAVORITE_CALL(sessionManager.getUserToken(), productId, sessionManager.getUserId())
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        loading.setVisibility(View.GONE);
                        GlobalFunctions.EnableLayout(container);
                        int responseCode = response.code();
                        if (responseCode == 200) {
                            String result = "";
                            try {
                                result = response.body().string();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            if (result.equals("true")) {
                                makeRemoveFav.setImageResource(R.mipmap.ic_heart_sel);
                            } else if (result.equals("false")) {
                                makeRemoveFav.setImageResource(R.mipmap.ic_heart_unsel);
                            }
                        } else if (responseCode == 201) {
                            Log.d(Constants.M3RESNA_APP, "Ad not found");
                        } else if (responseCode == 203) {
                            Log.d(Constants.M3RESNA_APP, "user not found");
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


    private void countriesApi() {
        loading.setVisibility(View.VISIBLE);
        GlobalFunctions.DisableLayout(container);
        RetrofitConfig.getServices().COUNTRIES_CALL().enqueue(new Callback<ArrayList<Country>>() {
            @Override
            public void onResponse(Call<ArrayList<Country>> call, Response<ArrayList<Country>> response) {
                int responseCode = response.code();
                if (responseCode == 200) {
                    if (response.body() != null && response.body().size() > 0) {
                        countriesList.clear();
                        countriesList.addAll(response.body());
                        countryId = countriesList.get(0).id;
                        citiesApi(countryId, loading, false);
                    }
                } else {
                    loading.setVisibility(View.GONE);
                    GlobalFunctions.EnableLayout(container);
                    if (responseCode == 202) {
                        Log.d(Constants.M3RESNA_APP, "countries not found");
                    } else {
                        Snackbar.make(childView, getString(R.string.generalError), Snackbar.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<ArrayList<Country>> call, Throwable t) {
                GlobalFunctions.generalErrorMessage(loading, activity);
                GlobalFunctions.EnableLayout(container);
            }
        });
    }

    private void citiesApi(int countryId, ProgressBar loading, boolean isOpenedDialog) {
        if (isOpenedDialog) {
            GlobalFunctions.DisableLayout((ViewGroup) filterView);
        } else {
            GlobalFunctions.DisableLayout(container);
        }
        RetrofitConfig.getServices().CITIES_CALL(sessionManager.getUserToken(), countryId).enqueue(new Callback<ArrayList<City>>() {
            @Override
            public void onResponse(Call<ArrayList<City>> call, Response<ArrayList<City>> response) {
                loading.setVisibility(View.GONE);
                GlobalFunctions.EnableLayout(container);
                if (isOpenedDialog) {
                    GlobalFunctions.EnableLayout((ViewGroup) filterView);
                } else {
                    GlobalFunctions.EnableLayout(container);
                }
                int responseCode = response.code();
                if (responseCode == 200) {
                    if (response.body() != null && response.body().size() > 0) {
                        citiesList.clear();
                        City all = new City();
                        all.countryId = countryId;
                        all.id = 0;
                        all.setName(getString(R.string.all));
                        citiesList.add(all);
                        citiesList.addAll(response.body());
                        if (isOpenedDialog) {
                            setupCitiesList();
                        }
                    }
                } else if (responseCode == 202) {
                    Log.d(Constants.M3RESNA_APP, "cities not found");
                } else {
                    Snackbar.make(childView, getString(R.string.generalError), Snackbar.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ArrayList<City>> call, Throwable t) {
                GlobalFunctions.generalErrorMessage(loading, activity);
                if (isOpenedDialog) {
                    GlobalFunctions.EnableLayout((ViewGroup) filterView);
                } else {
                    GlobalFunctions.EnableLayout(container);
                }
            }
        });
    }

    private void createFilterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        filterView = activity.getLayoutInflater().inflate(R.layout.dialog_filter, null);
        Spinner countries = (Spinner) filterView.findViewById(R.id.dialog_filter_spin_countries);
        cities = (RecyclerView) filterView.findViewById(R.id.dialog_filter_rv_cities);
        ProgressBar loading = (ProgressBar) filterView.findViewById(R.id.loading);

        countriesNamesList.clear();
        for (Country item : countriesList) {
            countriesNamesList.add(item.getName());
        }
        countriesAdapter = new ArrayAdapter<String>(activity,
                android.R.layout.simple_spinner_item, countriesNamesList);
        countriesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        countries.setAdapter(countriesAdapter);

        countries.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
               // CitiesAdapter.selectedPosition = -1;
                countryId = countriesList.get(position).id;
                citiesAdapter.notifyDataSetChanged();
                citiesApi(countryId, loading, true);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        setupCitiesList();
        cities.addOnItemTouchListener(new RecyclerItemClickListener(activity, cities, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                cityId = citiesList.get(position).id;
                if (cityId == 0)
                    cityId = null;
               // CitiesAdapter.selectedPosition = position;
                citiesAdapter.notifyDataSetChanged();
                initializePagination();
                productsApi(categoryId, countryId, cityId, genderId);
                closeFilterDialog();
            }

            @Override
            public void onItemLongClick(View view, int position) {

            }
        }));

        builder.setView(filterView);
        dialog = builder.create();
        dialog.show();
    }

    private void closeFilterDialog() {
        dialog.cancel();
    }

    private void setupCitiesList() {
        citiesAdapter = new CitiesAdapter(activity, citiesList);
        cities.setLayoutManager(new GridLayoutManager(activity, 3));
        cities.setAdapter(citiesAdapter);
    }

    private void initializePagination() {
        pageIndex = 1;
        isLoading = false;
        isLastPage = false;
    }
}
