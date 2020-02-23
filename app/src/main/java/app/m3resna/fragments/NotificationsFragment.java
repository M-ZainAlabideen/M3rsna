package app.m3resna.fragments;

import android.os.Bundle;
import android.util.Log;
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
import app.m3resna.adapters.NotificationsAdapter;
import app.m3resna.classes.Constants;
import app.m3resna.classes.GlobalFunctions;
import app.m3resna.classes.Navigator;
import app.m3resna.classes.SessionManager;
import app.m3resna.webservices.RetrofitConfig;
import app.m3resna.webservices.models.Notification;
import butterknife.BindView;
import butterknife.ButterKnife;
import me.leolin.shortcutbadger.ShortcutBadger;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationsFragment extends Fragment {
    public static FragmentActivity activity;
    public static NotificationsFragment fragment;
    private View childView;
    private SessionManager sessionManager;

    private ArrayList<Notification> notificationsList = new ArrayList<>();
    private LinearLayoutManager layoutManager;
    private NotificationsAdapter notificationsAdapter;
    private int pageIndex = 1;
    private boolean isLoading = false;
    private boolean isLastPage = false;

    @BindView(R.id.fragment_notifications_cl_container)
    ConstraintLayout container;
    @BindView(R.id.fragment_notifications_rv_notifications)
    RecyclerView notifications;
    @BindView(R.id.loading)
    ProgressBar loading;

    public static NotificationsFragment newInstance(FragmentActivity activity) {
        fragment = new NotificationsFragment();
        NotificationsFragment.activity = activity;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        childView = inflater.inflate(R.layout.fragment_notifications, container, false);
        ButterKnife.bind(this, childView);
        return childView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MainActivity.setupAppbar(true, false, true, getString(R.string.notifications),
                true, true, false, true, true, Constants.NOTIFICATIONS);
        sessionManager = new SessionManager(activity);
        layoutManager = new LinearLayoutManager(activity);
        notificationsAdapter = new NotificationsAdapter(activity, notificationsList, new NotificationsAdapter.OnItemClickListener() {
            @Override
            public void itemClick(int position) {

            }
        });
        notifications.setLayoutManager(layoutManager);
        notifications.setAdapter(notificationsAdapter);
        if (!GlobalFunctions.isNetworkConnected(activity)) {
            Snackbar.make(childView, getString(R.string.noConnection), Snackbar.LENGTH_SHORT).show();
        } else {
            notificationsApi();
        }
    }

    private void notificationsApi() {
        GlobalFunctions.DisableLayout(container);
        RetrofitConfig.getServices().NOTIFICATIONS_CALL(sessionManager.getUserToken(), sessionManager.getUserId()
                , pageIndex).enqueue(new Callback<ArrayList<Notification>>() {
            @Override
            public void onResponse(Call<ArrayList<Notification>> call, Response<ArrayList<Notification>> response) {
                loading.setVisibility(View.GONE);
                GlobalFunctions.EnableLayout(container);
                int responseCode = response.code();
                if (responseCode == 200) {
                    if (response.body() != null && response.body().size() > 0) {
                        notificationsList.clear();
                        notificationsList.addAll(response.body());
                        notificationsAdapter.notifyDataSetChanged();
                        updateNotificationsApi();
                        notifications.addOnScrollListener(new RecyclerView.OnScrollListener() {
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

                                            getMoreNotifications();

                                        }
                                    }
                                }
                            }
                        });
                    }
                } else if (responseCode == 203) {
                    Log.d(Constants.M3RESNA_APP, "user not found");
                } else if (responseCode == 204) {
                    isLastPage = true;
                    Snackbar.make(childView, getString(R.string.noNotificationsFound), Snackbar.LENGTH_SHORT).show();
                } else {
                    Snackbar.make(childView, getString(R.string.generalError), Snackbar.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ArrayList<Notification>> call, Throwable t) {
                GlobalFunctions.generalErrorMessage(loading, activity);
                GlobalFunctions.EnableLayout(container);
            }
        });
    }

    private void getMoreNotifications() {
        loading.setVisibility(View.VISIBLE);
        GlobalFunctions.DisableLayout(container);
        RetrofitConfig.getServices().NOTIFICATIONS_CALL(sessionManager.getUserToken(),
                sessionManager.getUserId(), pageIndex)
                .enqueue(new Callback<ArrayList<Notification>>() {
                    @Override
                    public void onResponse(Call<ArrayList<Notification>> call, Response<ArrayList<Notification>> response) {
                        loading.setVisibility(View.GONE);
                        GlobalFunctions.EnableLayout(container);
                        int responseCode = response.code();
                        if (responseCode == 200) {
                            if (response.body().size() > 0) {
                                notificationsList.addAll(response.body());
                                notificationsAdapter.notifyDataSetChanged();
                            } else {
                                isLastPage = true;
                                pageIndex = pageIndex - 1;
                            }
                            isLoading = false;

                        }
                    }

                    @Override
                    public void onFailure(Call<ArrayList<Notification>> call, Throwable t) {
                        GlobalFunctions.generalErrorMessage(loading, activity);
                        GlobalFunctions.EnableLayout(container);
                    }
                });

    }

    private void updateNotificationsApi() {
        RetrofitConfig.getServices().UPDATE_NOTIFICATIONS_CALL(sessionManager.getUserToken(), sessionManager.getUserId())
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        int responseCode = response.code();
                        if (responseCode == 200) {
                            ShortcutBadger.removeCount(activity);
                        } else if (responseCode == 203) {
                            Log.d(Constants.M3RESNA_APP, "user not found");
                        } else {
                            Snackbar.make(childView, getString(R.string.generalError), Snackbar.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        GlobalFunctions.generalErrorMessage(loading, activity);
                    }
                });
    }

}
