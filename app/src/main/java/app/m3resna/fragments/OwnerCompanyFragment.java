package app.m3resna.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.util.ArrayList;

import app.m3resna.MainActivity;
import app.m3resna.R;
import app.m3resna.adapters.MainAdapter;
import app.m3resna.classes.Constants;
import app.m3resna.classes.FixControl;
import app.m3resna.classes.GlobalFunctions;
import app.m3resna.classes.Navigator;
import app.m3resna.classes.SessionManager;
import app.m3resna.webservices.RetrofitConfig;
import app.m3resna.webservices.models.Company;
import app.m3resna.webservices.models.Product;
import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OwnerCompanyFragment extends Fragment {
    public static FragmentActivity activity;
    public static OwnerCompanyFragment fragment;
    private View childView;
    private SessionManager sessionManager;
    private Company ownerCompanyData;
    private ArrayList<Product> companyProductsList = new ArrayList<>();
    private MainAdapter companyProductsAdapter;
    private LinearLayoutManager layoutManager;

    @BindView(R.id.fragment_owner_company_cl_container)
    ConstraintLayout container;
    @BindView(R.id.fragment_owner_company_iv_companyImg)
    ImageView companyImg;
    @BindView(R.id.fragment_owner_company_tv_companyName)
    TextView companyName;
    @BindView(R.id.fragment_owner_company_tv_aboutCompany)
    TextView aboutCompany;
    @BindView(R.id.fragment_owner_company_rv_companyProducts)
    RecyclerView companyProducts;
    @BindView(R.id.loading)
    ProgressBar loading;

    public static OwnerCompanyFragment newInstance(FragmentActivity activity, int productId) {
        fragment = new OwnerCompanyFragment();
        OwnerCompanyFragment.activity = activity;
        Bundle b = new Bundle();
        b.putInt(Constants.PRODUCT_ID, productId);
        fragment.setArguments(b);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        childView = inflater.inflate(R.layout.fragment_owner_company, container, false);
        ButterKnife.bind(this, childView);
        return childView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MainActivity.setupAppbar(true, false, true, getString(R.string.aboutCompany),
                true, true, false, true, true, Constants.CATEGORY);
        GlobalFunctions.hasNewNotificationsApi(activity);
        container.setVisibility(View.GONE);
        sessionManager = new SessionManager(activity);
        if (!GlobalFunctions.isNetworkConnected(activity)) {
            Snackbar.make(childView, getString(R.string.noConnection), Snackbar.LENGTH_SHORT).show();
        } else {
            ownerCompanyDataApi(getArguments().getInt(Constants.PRODUCT_ID));
        }

        layoutManager = new LinearLayoutManager(activity);
        companyProductsAdapter = new MainAdapter(activity, null, companyProductsList, null, Constants.PRODUCT, new MainAdapter.OnItemClickListener() {
            @Override
            public void imageClick(int position) {
                Navigator.loadFragment(activity, ProductDetailsFragment.newInstance(activity, companyProductsList.get(position).id), R.id.app_bar_main_fl_mainContainer, true);
            }

            @Override
            public void favClick(int position, ImageView makeRemoveFav) {
                makeRemoveFavApi(companyProductsList.get(position).id, makeRemoveFav);
            }

            @Override
            public void renewClick(int position) {

            }
        });
        companyProducts.setLayoutManager(layoutManager);
        companyProducts.setAdapter(companyProductsAdapter);
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

    private void ownerCompanyDataApi(int companyId) {
        loading.setVisibility(View.VISIBLE);
        GlobalFunctions.DisableLayout(container);
        RetrofitConfig.getServices().OWNER_COMPANY_DATA_CALL(sessionManager.getUserToken(),
                sessionManager.getUserId(),companyId)
                .enqueue(new Callback<ArrayList<Company>>() {
                    @Override
                    public void onResponse(Call<ArrayList<Company>> call, Response<ArrayList<Company>> response) {
                        loading.setVisibility(View.GONE);
                        GlobalFunctions.EnableLayout(container);
                        int responseCode = response.code();
                        if (responseCode == 200) {
                            if (response.body() != null) {
                                ownerCompanyData = response.body().get(0);
                                setProductData();
                            }
                        } else if (responseCode == 201) {
                            Log.d(Constants.M3RESNA_APP, "user not found");
                        } else {
                            Snackbar.make(childView, getString(R.string.generalError), Snackbar.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ArrayList<Company>> call, Throwable t) {
                        GlobalFunctions.generalErrorMessage(loading, activity);
                        GlobalFunctions.EnableLayout(container);
                    }
                });

    }

    private void setProductData() {
        container.setVisibility(View.VISIBLE);
        setImage(ownerCompanyData.photoUrl, companyImg);
        companyName.setText(ownerCompanyData.fullName);
        if (ownerCompanyData.about != null && !ownerCompanyData.about.isEmpty()) {
            aboutCompany.setVisibility(View.VISIBLE);
            aboutCompany.setText(ownerCompanyData.about);
        } else {
            aboutCompany.setVisibility(View.GONE);

        }
        companyProductsList.clear();
        for (Product item: ownerCompanyData.products) {
            item.isAd = false;
            companyProductsList.add(item);
        }
        companyProductsAdapter.notifyDataSetChanged();

    }

    private void setImage(String imagePath, ImageView image) {
        if (imagePath != null) {
            try {
                int Width = FixControl.getImageWidth(activity, R.mipmap.placeholder_company);
                int Height = FixControl.getImageHeight(activity, R.mipmap.placeholder_company);
                image.getLayoutParams().height = Height;
                image.getLayoutParams().width = Width;
                Glide.with(activity).load(imagePath)
                        .apply(new RequestOptions().centerCrop()
                                .placeholder(R.mipmap.placeholder_company))
                        .into(image);

            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }
    }

}


