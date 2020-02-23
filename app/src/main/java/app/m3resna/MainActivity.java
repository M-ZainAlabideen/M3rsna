package app.m3resna;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.lang.reflect.Type;
import java.util.ArrayList;

import app.m3resna.adapters.GeneralDialogAdapter;
import app.m3resna.classes.Constants;
import app.m3resna.classes.FixControl;
import app.m3resna.classes.GlobalFunctions;
import app.m3resna.classes.LocaleHelper;
import app.m3resna.classes.Navigator;
import app.m3resna.classes.RecyclerItemClickListener;
import app.m3resna.classes.SessionManager;
import app.m3resna.fragments.AboutUsFragment;
import app.m3resna.fragments.AccountFragment;
import app.m3resna.fragments.AddAdFragment;
import app.m3resna.fragments.ContactUsFragment;
import app.m3resna.fragments.FavoritesFragment;
import app.m3resna.fragments.FullAdFragment;
import app.m3resna.fragments.GuestFragment;
import app.m3resna.fragments.HomeFragment;
import app.m3resna.fragments.LoginFragment;
import app.m3resna.fragments.NotificationsFragment;
import app.m3resna.webservices.RetrofitConfig;
import app.m3resna.webservices.models.Country;
import app.m3resna.webservices.responses.RegisterResponse;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    public static ConstraintLayout activityContainer;
    public static DrawerLayout drawer;
    public static NavigationView navigation;
    public static AppBarLayout appbar;
    public static Toolbar toolbar;
    public static LinearLayout bottomContainer;
    public static FrameLayout mainContainer;

    public static LinearLayout login;

    public static ImageView logo;
    public static ImageView back;
    public static ImageView filter;
    public static ImageView sideMenu;
    public static TextView countryName;
    public static ImageView add;
    public static TextView title;
    public static ImageView home;
    public static ImageView account;
    public static ImageView favorites;
    public static ImageView notifications;

    public static boolean isEnglish = false;
    private SessionManager sessionManager;
    private AlertDialog dialog;
    private ArrayList<Country> countriesList = new ArrayList<>();

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(LocaleHelper.onAttach(newBase)));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        GlobalFunctions.setDefaultLanguage(this);
        GlobalFunctions.setUpFont(this);
        sessionManager = new SessionManager(this);

        activityContainer = (ConstraintLayout) findViewById(R.id.activity_main_cl_activityContainer);
        drawer = (DrawerLayout) findViewById(R.id.activity_main_dl_drawer);
        navigation = (NavigationView) findViewById(R.id.activity_main_nv_navigation);
        appbar = (AppBarLayout) findViewById(R.id.app_bar_main_ab_appbar);
        toolbar = (Toolbar) findViewById(R.id.app_bar_main_tb_toolbar);
        mainContainer = (FrameLayout) findViewById(R.id.app_bar_main_fl_mainContainer);
        bottomContainer = (LinearLayout) findViewById(R.id.app_bar_main_ll_bottomContainer);
        login = (LinearLayout) findViewById(R.id.menu_main_ll_login);
        logo = (ImageView) findViewById(R.id.app_bar_main_iv_logo);
        back = (ImageView) findViewById(R.id.app_bar_main_iv_back);
        filter = (ImageView) findViewById(R.id.app_bar_main_iv_filter);
        sideMenu = (ImageView) findViewById(R.id.app_bar_main_iv_sideMenu);
        countryName = (TextView) findViewById(R.id.menu_main_tv_countryName);
        add = (ImageView) findViewById(R.id.app_bar_main_iv_add);
        title = (TextView) findViewById(R.id.app_bar_main_tv_title);
        home = (ImageView) findViewById(R.id.app_bar_main_iv_home);
        account = (ImageView) findViewById(R.id.app_bar_main_iv_account);
        favorites = (ImageView) findViewById(R.id.app_bar_main_iv_favorites);
        notifications = (ImageView) findViewById(R.id.app_bar_main_iv_notifications);

        FixControl.setupUI(activityContainer, this);
        GlobalFunctions.hasNewNotificationsApi(this);
        //setup the toolbar and navigationDrawer
        setSupportActionBar(toolbar);

        final ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
                //adding animation when the content of activity change (fragment navigation)
                float moveFactor = (navigation.getWidth() * slideOffset);
                mainContainer.setTranslationX(-moveFactor);
            }
        };

        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        toolbar.setNavigationIcon(null);

        if (!sessionManager.isGuest() && !sessionManager.isLoggedIn()) {
            login.setVisibility(View.VISIBLE);
            Navigator.loadFragment(this, GuestFragment.newInstance(this), R.id.app_bar_main_fl_mainContainer, false);

        } else {
            if (sessionManager.isGuest()) {
                login.setVisibility(View.VISIBLE);
            } else if (sessionManager.isLoggedIn()) {
                login.setVisibility(View.GONE);
            }
            Navigator.loadFragment(this, FullAdFragment.newInstance(this, true, null), R.id.app_bar_main_fl_mainContainer, false);
            countryName.setText(sessionManager.getCountryName());
        }
    }

    @OnClick(R.id.app_bar_main_iv_sideMenu)
    public void sideMenuClick() {
        //open and close the sideMenu when the navigationIcon clicked
        if (drawer.isDrawerOpen(Gravity.RIGHT)) {
            drawer.closeDrawers();
        } else {
            drawer.openDrawer(Gravity.RIGHT);
        }
    }


    //the back button action of all the app
    @OnClick(R.id.app_bar_main_iv_back)
    public void back() {
        onBackPressed();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            if (drawer.isDrawerOpen(Gravity.RIGHT)) {
                drawer.closeDrawers();
            } else {
                onBackPressed();
            }
        }
        return true;
    }

    @OnClick(R.id.app_bar_main_iv_add)
    public void addClick() {
        if (sessionManager.isLoggedIn()) {
            Navigator.loadFragment(this, AddAdFragment.newInstance(this, 0), R.id.app_bar_main_fl_mainContainer, true);
        } else {
            Navigator.loadFragment(this, LoginFragment.newInstance(this), R.id.app_bar_main_fl_mainContainer, true);
        }
    }

    //handling SideMenu items click
    @OnClick(R.id.menu_main_ll_login)
    public void loginClick() {
        Navigator.loadFragment(this, LoginFragment.newInstance(this), R.id.app_bar_main_fl_mainContainer, true);
        drawer.closeDrawers();
    }

    @OnClick(R.id.menu_main_ll_aboutUs)
    public void aboutUsClick() {
        Navigator.loadFragment(this, AboutUsFragment.newInstance(this, false), R.id.app_bar_main_fl_mainContainer, true);
        drawer.closeDrawers();
    }

    @OnClick(R.id.menu_main_ll_contactUs)
    public void contactUsClick() {
        Navigator.loadFragment(this, ContactUsFragment.newInstance(this), R.id.app_bar_main_fl_mainContainer, true);
        drawer.closeDrawers();
    }

    @OnClick(R.id.menu_main_ll_country)
    public void selectCountryClick() {
        if (countriesList.size() == 0) {
            countriesApi();
        } else {
            createCountriesDialog();
        }
        //fix it
        //drawer.closeDrawers();
    }

    @OnClick(R.id.menu_main_ll_changeLanguage)
    public void changeLangClick() {
        changeLanguage();
    }

    //handling bottomAppbar items click
    @OnClick(R.id.app_bar_main_iv_home)
    public void homeClick() {
        Navigator.loadFragment(this, HomeFragment.newInstance(this), R.id.app_bar_main_fl_mainContainer, true);
    }

    @OnClick(R.id.app_bar_main_iv_account)
    public void accountClick() {
        if (sessionManager.isLoggedIn()) {
            Navigator.loadFragment(this, AccountFragment.newInstance(this), R.id.app_bar_main_fl_mainContainer, true);
        } else {
            Navigator.loadFragment(this, LoginFragment.newInstance(this), R.id.app_bar_main_fl_mainContainer, true);
        }
    }

    @OnClick(R.id.app_bar_main_iv_favorites)
    public void favoritesClick() {
        Navigator.loadFragment(this, FavoritesFragment.newInstance(this), R.id.app_bar_main_fl_mainContainer, true);
    }

    @OnClick(R.id.app_bar_main_iv_notifications)
    public void notificationsClick() {
        Navigator.loadFragment(this, NotificationsFragment.newInstance(this), R.id.app_bar_main_fl_mainContainer, true);
    }

    private void createCountriesDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View popUpView = getLayoutInflater().inflate(R.layout.dialog_general, null);
        RecyclerView countries = (RecyclerView) popUpView.findViewById(R.id.dialog_general_rv_generalDialog);
        countries.setLayoutManager(new LinearLayoutManager(this));
        countries.setAdapter(new GeneralDialogAdapter(this, Constants.COUNTRY, null, null, countriesList, null, null));
        builder.setCancelable(true);
        builder.setView(popUpView);
        dialog = builder.create();
        dialog.show();
        countries.addOnItemTouchListener(new RecyclerItemClickListener(this, countries, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                updateCountriesApi(countriesList.get(position).id);
                closePopUp();
            }

            @Override
            public void onItemLongClick(View view, int position) {

            }
        }));
    }

    public void closePopUp() {
        dialog.cancel();

    }


    private void countriesApi() {
        RetrofitConfig.getServices().COUNTRIES_CALL().enqueue(new Callback<ArrayList<Country>>() {
            @Override
            public void onResponse(Call<ArrayList<Country>> call, Response<ArrayList<Country>> response) {
                int responseCode = response.code();
                if (responseCode == 200) {
                    if (response.body() != null && response.body().size() > 0) {
                        countriesList.clear();
                        countriesList.addAll(response.body());
                        createCountriesDialog();
                    }
                } else if (responseCode == 202) {
                    Log.d(Constants.M3RESNA_APP, "countries not found");
                }
            }

            @Override
            public void onFailure(Call<ArrayList<Country>> call, Throwable t) {
            }
        });
    }

    private void updateCountriesApi(int countryId) {
        RetrofitConfig.getServices().UPDATE_COUNTRY_CALL(sessionManager.getUserToken(),
                sessionManager.getUserId(), countryId).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                int responseCode = response.code();
                if (responseCode == 200) {
                    Country myResponse = null;
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
                        Type type = new TypeToken<Country>() {
                        }.getType();
                        JsonReader reader = new JsonReader(new StringReader(outResponse));
                        reader.setLenient(true);
                        myResponse = new Gson().fromJson(jsonResponse, type);
                    }
                    if (response.body() != null) {
                        countryName.setText(myResponse.getName());
                        sessionManager.setCountryName(myResponse.arabicName, myResponse.englishName);
                        sessionManager.setCountryId(myResponse.id);
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }


    private void changeLanguage() {
         /*for changing the language of App
        1- check the value of currentLanguage  and Reflects it
         2- set the new value of Language in local and change the value of languageSharedPreference to new value
         3- restart the mainActivity with noAnimation
        * */

        if (sessionManager.getUserLanguage().equals(Constants.EN)) {
            sessionManager.setUserLanguage(Constants.AR);
            MainActivity.isEnglish = false;
        } else {
            sessionManager.setUserLanguage(Constants.EN);
            MainActivity.isEnglish = true;
        }

        LocaleHelper.setLocale(this, sessionManager.getUserLanguage());
        finish();
        overridePendingTransition(0, 0);
        startActivity(new Intent(this, MainActivity.class));
        GlobalFunctions.setUpFont(this);
    }

    /*this function used for initialization and setUp topAppbar and bottomAppbar for each screen
     * for example: if title = true and sideMenu = false >> this mean that this screen has title but not has sideMenu and so on
     * */

    public static void setupAppbar(boolean hasTopAppbar, boolean hasLogo, boolean hasTitle, String titleValue, boolean hasBack, boolean hasAdd, boolean hasFilter, boolean hasSideMenu,
                                   boolean hasBottomAppbar, String bottomAppbarSelection) {
        hideAppbarComponents();

        if (hasTopAppbar) {
            appbar.setVisibility(View.VISIBLE);
        }
        if (hasLogo) {
            logo.setVisibility(View.VISIBLE);
        }
        if (hasTitle) {
            title.setVisibility(View.VISIBLE);
            title.setText(titleValue);
        }
        if (hasBack) {
            back.setVisibility(View.VISIBLE);
        }
        if (hasAdd) {
            add.setVisibility(View.VISIBLE);
        }
        if (hasFilter) {
            filter.setVisibility(View.VISIBLE);
        }
        if (hasSideMenu) {
            sideMenu.setVisibility(View.VISIBLE);
        }
        if (hasBottomAppbar) {
            bottomContainer.setVisibility(View.VISIBLE);
            if (bottomAppbarSelection.equals(Constants.CATEGORY)) {
                home.setImageResource(R.mipmap.ic_home_sel);
                account.setImageResource(R.mipmap.ic_profile_unsel);
                favorites.setImageResource(R.mipmap.ic_fav_unsel);
            } else if (bottomAppbarSelection.equals(Constants.ACCOUNT)) {
                home.setImageResource(R.mipmap.ic_home_unsel);
                account.setImageResource(R.mipmap.ic_profile_sel);
                favorites.setImageResource(R.mipmap.ic_fav_unsel);
            } else if (bottomAppbarSelection.equals(Constants.FAVORITES)) {
                home.setImageResource(R.mipmap.ic_home_unsel);
                account.setImageResource(R.mipmap.ic_profile_unsel);
                favorites.setImageResource(R.mipmap.ic_fav_sel);
            } else if (bottomAppbarSelection.equals(Constants.NOTIFICATIONS)) {
                home.setImageResource(R.mipmap.ic_home_unsel);
                account.setImageResource(R.mipmap.ic_profile_unsel);
                favorites.setImageResource(R.mipmap.ic_fav_unsel);
                notifications.setImageResource(R.mipmap.ic_notifi_sel);
            }
        }
    }

    public static void hideAppbarComponents() {
        appbar.setVisibility(View.GONE);
        bottomContainer.setVisibility(View.GONE);
        logo.setVisibility(View.GONE);
        title.setVisibility(View.GONE);
        filter.setVisibility(View.GONE);
        back.setVisibility(View.GONE);
        sideMenu.setVisibility(View.GONE);
        add.setVisibility(View.GONE);
    }
}
