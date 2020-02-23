package app.m3resna.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import app.m3resna.adapters.PaymentHistoryAdapter;
import app.m3resna.classes.Constants;
import app.m3resna.classes.GlobalFunctions;
import app.m3resna.classes.SessionManager;
import app.m3resna.webservices.RetrofitConfig;
import app.m3resna.webservices.models.Category;
import app.m3resna.webservices.models.PaymentHistory;
import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PaymentHistoryFragment extends Fragment {
    public static FragmentActivity activity;
    public static PaymentHistoryFragment fragment;
    private View childView;
    private SessionManager sessionManager;


    private ArrayList<PaymentHistory> paymentHistoryList = new ArrayList<>();
    private LinearLayoutManager layoutManager;
    private PaymentHistoryAdapter paymentHistoryAdapter;
    private int pageIndex = 1;
    private boolean isLoading = false;
    private boolean isLastPage = false;


    @BindView(R.id.fragment_payment_history_cl_container)
    ConstraintLayout container;
    @BindView(R.id.fragment_payment_history_rv_paymentHistory)
    RecyclerView paymentHistory;
    @BindView(R.id.loading)
    ProgressBar loading;

    public static PaymentHistoryFragment newInstance(FragmentActivity activity) {
        fragment = new PaymentHistoryFragment();
        PaymentHistoryFragment.activity = activity;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        childView = inflater.inflate(R.layout.fragment_payment_history, container, false);
        ButterKnife.bind(this, childView);
        return childView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loading.setVisibility(View.GONE);
        MainActivity.setupAppbar(true, false, true, getString(R.string.paymentDetails),
                true, false, false, true, true, Constants.ACCOUNT);
        GlobalFunctions.hasNewNotificationsApi(activity);
        sessionManager = new SessionManager(activity);

        layoutManager = new LinearLayoutManager(activity);
        paymentHistoryAdapter = new PaymentHistoryAdapter(activity, paymentHistoryList);
        paymentHistory.setLayoutManager(layoutManager);
        paymentHistory.setAdapter(paymentHistoryAdapter);
        if (paymentHistoryList.size() == 0) {
            paymentHistoryApi();
        }
    }

    private void paymentHistoryApi() {
        loading.setVisibility(View.VISIBLE);
        GlobalFunctions.DisableLayout(container);
        RetrofitConfig.getServices().PAYMENTS_HISTORY(sessionManager.getUserToken(),
                sessionManager.getUserId(), pageIndex).enqueue(new Callback<ArrayList<PaymentHistory>>() {
            @Override
            public void onResponse(Call<ArrayList<PaymentHistory>> call, Response<ArrayList<PaymentHistory>> response) {
                loading.setVisibility(View.GONE);
                GlobalFunctions.EnableLayout(container);
                int responseCode = response.code();
                if (responseCode == 200) {
                    if (response.body() != null && response.body().size() > 0) {
                        paymentHistoryList.clear();
                        paymentHistoryList.addAll(response.body());
                        paymentHistoryAdapter.notifyDataSetChanged();
                        paymentHistory.addOnScrollListener(new RecyclerView.OnScrollListener() {
                            @Override
                            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                                super.onScrollStateChanged(recyclerView, newState);
                            }

                            @Override
                            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                                super.onScrolled(recyclerView, dx, dy);
                                if (!isLastPage) {
                                    int visibleItemCount = layoutManager.getChildCount();

                                    int totalItemCount = paymentHistoryAdapter.getItemCount();

                                    int pastVisibleItems = layoutManager.findFirstVisibleItemPosition();

                                /*isLoading variable used for check if the user send many requests
                                for pagination(make many scrolls in the same time)
                                1- if isLoading true >> there is request already sent so,
                                no more requests till the response of last request coming
                                2- else >> send new request for load more data */
                                    if (!isLoading) {

                                        if ((visibleItemCount + pastVisibleItems) >= totalItemCount) {
                                            isLoading = true;

                                            pageIndex = pageIndex + 1;

                                            getMorePaymentHistory();

                                        }
                                    }
                                }
                            }
                        });
                    } else {
                        isLastPage = true;
                        Snackbar.make(childView, getString(R.string.noPaymentHistory), Snackbar.LENGTH_SHORT).show();
                    }
                } else {
                    Snackbar.make(childView, getString(R.string.generalError), Snackbar.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ArrayList<PaymentHistory>> call, Throwable t) {
                GlobalFunctions.generalErrorMessage(loading, activity);
                GlobalFunctions.EnableLayout(container);
            }
        });
    }

    private void getMorePaymentHistory() {
        loading.setVisibility(View.VISIBLE);
        GlobalFunctions.DisableLayout(container);
        RetrofitConfig.getServices().PAYMENTS_HISTORY(sessionManager.getUserToken(),
                sessionManager.getUserId(), pageIndex)
                .enqueue(new Callback<ArrayList<PaymentHistory>>() {
                    @Override
                    public void onResponse(Call<ArrayList<PaymentHistory>> call, Response<ArrayList<PaymentHistory>> response) {
                        loading.setVisibility(View.GONE);
                        GlobalFunctions.EnableLayout(container);
                        int responseCode = response.code();
                        if (responseCode == 200) {
                            if (response.body().size() > 0) {
                                paymentHistoryList.addAll(response.body());
                                paymentHistoryAdapter.notifyDataSetChanged();
                            } else {
                                isLastPage = true;
                                pageIndex = pageIndex - 1;
                            }
                            isLoading = false;

                        }
                    }

                    @Override
                    public void onFailure(Call<ArrayList<PaymentHistory>> call, Throwable t) {
                        GlobalFunctions.generalErrorMessage(loading, activity);
                        GlobalFunctions.EnableLayout(container);
                    }
                });
    }
}

