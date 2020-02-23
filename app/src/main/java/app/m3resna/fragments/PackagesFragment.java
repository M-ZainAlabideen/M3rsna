package app.m3resna.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import app.m3resna.adapters.PackagesAdapter;
import app.m3resna.adapters.PaymentMethodsAdapter;
import app.m3resna.classes.Constants;
import app.m3resna.classes.GlobalFunctions;
import app.m3resna.classes.Navigator;
import app.m3resna.classes.RecyclerItemClickListener;
import app.m3resna.classes.SessionManager;
import app.m3resna.webservices.RetrofitConfig;
import app.m3resna.webservices.models.Package;
import app.m3resna.webservices.models.PaymentMethod;
import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PackagesFragment extends Fragment {
    public static FragmentActivity activity;
    public static PackagesFragment fragment;
    private View childView;
    private SessionManager sessionManager;

    private ArrayList<Package> packagesList = new ArrayList<>();
    private PackagesAdapter packagesAdapter;
    private LinearLayoutManager layoutManager;

    private ArrayList<PaymentMethod> paymentMethodsList = new ArrayList<>();
    private AlertDialog dialog;

    private int paymentMethodId;
    private int packageId;
    private double packagePrice;

    @BindView(R.id.fragment_packages_cl_container)
    ConstraintLayout container;
    @BindView(R.id.fragment_packages_rv_packages)
    RecyclerView packages;
    @BindView(R.id.loading)
    ProgressBar loading;

    public static PackagesFragment newInstance(FragmentActivity activity, String flag) {
        fragment = new PackagesFragment();
        PackagesFragment.activity = activity;
        Bundle b = new Bundle();
        b.putString(Constants.FLAG, flag);
        fragment.setArguments(b);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        childView = inflater.inflate(R.layout.fragment_packages, container, false);
        ButterKnife.bind(this, childView);
        return childView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MainActivity.setupAppbar(true, false, true, getString(R.string.selectPackage),
                true, false, false, false, true, Constants.CATEGORY);
        GlobalFunctions.hasNewNotificationsApi(activity);
        sessionManager = new SessionManager(activity);

        layoutManager = new LinearLayoutManager(activity);
        packagesAdapter = new PackagesAdapter(activity, packagesList, new PackagesAdapter.OnItemClickListener() {
            @Override
            public void packageClick(int position) {
                packageId = packagesList.get(position).id;
                packagePrice = packagesList.get(position).price;

                if (!GlobalFunctions.isNetworkConnected(activity)) {
                    Snackbar.make(childView, getString(R.string.noConnection), Snackbar.LENGTH_SHORT).show();
                } else {
                    if (packagesList.get(position).price == 0) {
                        selectPackagesApi(sessionManager.getUserId(), packagesList.get(position).id, 0);
                    } else {
                        /*
                         * after selection package ,call the paymentMethods api and show popUp(AlertDialog)
                         * */
                        paymentMethodsApi();
                    }
                }
            }
        });
        packages.setLayoutManager(layoutManager);
        packages.setAdapter(packagesAdapter);

        if (packagesList.size() == 0) {
            packagesApi();
        } else {
            loading.setVisibility(View.GONE);
        }
    }

    private void packagesApi() {
        RetrofitConfig.getServices().PACKAGES_CALL(sessionManager.getUserToken(), sessionManager.getUserId())
                .enqueue(new Callback<ArrayList<Package>>() {
                    @Override
                    public void onResponse(Call<ArrayList<Package>> call, Response<ArrayList<Package>> response) {
                        loading.setVisibility(View.GONE);
                        int responseCode = response.code();
                        if (responseCode == 200) {
                            if (response.body().size() > 0) {
                                packagesList.clear();
                                packagesList.addAll(response.body());
                                packagesAdapter.notifyDataSetChanged();
                            }
                        } else {
                            Snackbar.make(childView, getString(R.string.generalError), Snackbar.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ArrayList<Package>> call, Throwable t) {
                        GlobalFunctions.generalErrorMessage(loading, activity);
                    }
                });
    }

    private void paymentMethodsApi() {
        loading.setVisibility(View.VISIBLE);
        GlobalFunctions.DisableLayout(container);
        RetrofitConfig.getServices().PAYMENT_METHODS_CALL(sessionManager.getUserToken())
                .enqueue(new Callback<ArrayList<PaymentMethod>>() {
                    @Override
                    public void onResponse(Call<ArrayList<PaymentMethod>> call, Response<ArrayList<PaymentMethod>> response) {
                        loading.setVisibility(View.GONE);
                        GlobalFunctions.EnableLayout(container);
                        int responseCode = response.code();
                        if (responseCode == 200) {
                            if (response.body().size() > 0) {
                                paymentMethodsList.clear();
                                paymentMethodsList.addAll(response.body());
                                paymentMethodsDialog(paymentMethodsList);
                            }
                        } else {
                            Snackbar.make(childView, getString(R.string.generalError), Snackbar.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ArrayList<PaymentMethod>> call, Throwable t) {
                        GlobalFunctions.generalErrorMessage(loading, activity);
                        GlobalFunctions.EnableLayout(container);
                    }
                });
    }

    public void paymentMethodsDialog(final ArrayList<PaymentMethod> paymentMethodsList) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        View paymentsMethodsView = activity.getLayoutInflater().inflate(R.layout.dialog_payment_methods, null);
        RecyclerView paymentsMethods = (RecyclerView) paymentsMethodsView.findViewById(R.id.dialog_payment_paymentMethods_rv_methods);
        Button confirm = (Button) paymentsMethodsView.findViewById(R.id.dialog_payments_methods_btn_confirm);
        Button cancel = (Button) paymentsMethodsView.findViewById(R.id.dialog_payment_methods_btn_cancel);

        layoutManager = new LinearLayoutManager(activity);
        PaymentMethodsAdapter paymentMethodsAdapter = new PaymentMethodsAdapter(activity, paymentMethodsList);
        paymentsMethods.setLayoutManager(layoutManager);
        paymentsMethods.setAdapter(paymentMethodsAdapter);
        builder.setCancelable(true);
        builder.setView(paymentsMethodsView);
        dialog = builder.create();
        dialog.show();

        paymentsMethods.addOnItemTouchListener(new RecyclerItemClickListener(activity, paymentsMethods, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                PaymentMethodsAdapter.selectedPosition = position;
                paymentMethodsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onItemLongClick(View view, int position) {

            }
        }));

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                paymentMethodId = paymentMethodsList.get(PaymentMethodsAdapter.selectedPosition).id;
                closePopUp();
                PaymentMethodsAdapter.selectedPosition = 0;
                selectPackagesApi(sessionManager.getUserId(), packageId, paymentMethodId);
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closePopUp();
                PaymentMethodsAdapter.selectedPosition = 0;
            }
        });

    }

    public void closePopUp() {
        dialog.cancel();

    }

    private void selectPackagesApi(int userId, int packageId, int paymentMethodId) {
        loading.setVisibility(View.VISIBLE);
        GlobalFunctions.DisableLayout(container);
        RetrofitConfig.getServices().SELECT_PACKAGE_CALL(sessionManager.getUserToken(), userId, packageId, paymentMethodId)
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        loading.setVisibility(View.GONE);
                        GlobalFunctions.EnableLayout(container);
                        int responseCode = response.code();
                        if (responseCode == 200) {
                            if (response.body() != null) {
                                String responseStr;
                                try {
                                    responseStr = response.body().string();
                                    if (responseStr.equals("1")) {
                                        clearFragments(1);
                                        Snackbar.make(childView, getString(R.string.subscriptionSuccessfully), Snackbar.LENGTH_SHORT).show();
                                    } else {
                                        Navigator.loadFragment(activity, UrlsFragment.newInstance(activity, responseStr, true, getArguments().getString(Constants.FLAG)), R.id.app_bar_main_fl_mainContainer, true);
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        } else if (responseCode == 203) {
                            Log.d(Constants.M3RESNA_APP, "user Not found");
                        } else if (responseCode == 204) {
                            Log.d(Constants.M3RESNA_APP, "packageId Not found");
                        } else if (responseCode == 205) {
                            Log.d(Constants.M3RESNA_APP, "paymentMethod Not found");
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


    private void clearFragments(int count) {
        for (int i = 1; i <= count; i++) {
            getFragmentManager().popBackStack();
        }
    }
}


