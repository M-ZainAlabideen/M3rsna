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

import java.io.IOException;
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

public class FavoritesFragment extends Fragment {
    public static FragmentActivity activity;
    public static FavoritesFragment fragment;
    private View childView;
    private SessionManager sessionManager;
    private ArrayList<Product> favoritesList = new ArrayList<>();
    private MainAdapter favoritesAdapter;
    private LinearLayoutManager layoutManager;
    private int pageIndex = 1;
    private boolean isLoading = false;
    private boolean isLastPage = false;

    @BindView(R.id.fragment_favorites_cl_container)
    ConstraintLayout container;
    @BindView(R.id.fragment_favorites_rv_favorites)
    RecyclerView favorites;
    @BindView(R.id.loading)
    ProgressBar loading;

    public static FavoritesFragment newInstance(FragmentActivity activity) {
        fragment = new FavoritesFragment();
        FavoritesFragment.activity = activity;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        childView = inflater.inflate(R.layout.fragment_favorites, container, false);
        ButterKnife.bind(this, childView);
        return childView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MainActivity.setupAppbar(true, false, true, getString(R.string.favorites),
                true, true, false, true, true, Constants.FAVORITES);
        GlobalFunctions.hasNewNotificationsApi(activity);
        sessionManager = new SessionManager(activity);
        layoutManager = new LinearLayoutManager(activity);
        favoritesAdapter = new MainAdapter(activity, null, favoritesList, null, Constants.PRODUCT, new MainAdapter.OnItemClickListener() {
            @Override
            public void imageClick(int position) {
                Navigator.loadFragment(activity, ProductDetailsFragment.newInstance(activity, favoritesList.get(position).id), R.id.app_bar_main_fl_mainContainer, true);
            }

            @Override
            public void favClick(int position, ImageView makeRemoveFav) {
                makeFavApi(favoritesList.get(position).id, makeRemoveFav, position);
            }

            @Override
            public void renewClick(int position) {

            }
        });
        favorites.setLayoutManager(layoutManager);
        favorites.setAdapter(favoritesAdapter);

        if (favoritesList.size() == 0) {
            if (!GlobalFunctions.isNetworkConnected(activity)) {
                Snackbar.make(childView, getString(R.string.noConnection), Snackbar.LENGTH_SHORT).show();
            } else {
                favoritesApi();
            }
        } else {
            loading.setVisibility(View.GONE);
            GlobalFunctions.EnableLayout(container);
        }
    }

    private void makeFavApi(int productId, ImageView makeRemoveFav, int position) {
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
                            if (result.equals("false")) {
                                makeRemoveFav.setImageResource(R.mipmap.ic_heart_unsel);
                                favoritesList.remove(position);
                                favoritesAdapter.notifyDataSetChanged();
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

    private void favoritesApi() {
        GlobalFunctions.DisableLayout(container);
        RetrofitConfig.getServices().FAVORITES_CALL(sessionManager.getUserToken(), sessionManager.getUserId(), pageIndex)
                .enqueue(new Callback<ArrayList<Product>>() {
                    @Override
                    public void onResponse(Call<ArrayList<Product>> call, Response<ArrayList<Product>> response) {
                        loading.setVisibility(View.GONE);
                        GlobalFunctions.EnableLayout(container);
                        int responseCode = response.code();
                        if (responseCode == 200) {
                            if (response.body() != null && response.body().size() > 0) {
                                favoritesList.clear();
                                favoritesList.addAll(response.body());
                                favoritesAdapter.notifyDataSetChanged();
                                favorites.addOnScrollListener(new RecyclerView.OnScrollListener() {
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

                                                    getMoreFavorites();

                                                }
                                            }
                                        }
                                    }
                                });
                            } else {
                                isLastPage = true;
                                Snackbar.make(childView, getString(R.string.noFavoritesFound), Snackbar.LENGTH_SHORT).show();
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

    private void getMoreFavorites() {
        loading.setVisibility(View.VISIBLE);
        GlobalFunctions.DisableLayout(container);
        RetrofitConfig.getServices().FAVORITES_CALL(sessionManager.getUserToken(), sessionManager.getUserId(), pageIndex)
                .enqueue(new Callback<ArrayList<Product>>() {
                    @Override
                    public void onResponse(Call<ArrayList<Product>> call, Response<ArrayList<Product>> response) {
                        loading.setVisibility(View.GONE);
                        GlobalFunctions.EnableLayout(container);
                        int responseCode = response.code();
                        if (responseCode == 200) {
                            if (response.body().size() > 0) {
                                favoritesList.addAll(response.body());
                                favoritesAdapter.notifyDataSetChanged();

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

}

