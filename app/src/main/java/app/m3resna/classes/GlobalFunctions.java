package app.m3resna.classes;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.material.snackbar.Snackbar;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import app.m3resna.MainActivity;
import app.m3resna.R;
import app.m3resna.webservices.RetrofitConfig;
import app.m3resna.webservices.models.Notification;
import io.github.inflationx.calligraphy3.CalligraphyConfig;
import io.github.inflationx.calligraphy3.CalligraphyInterceptor;
import io.github.inflationx.viewpump.ViewPump;
import me.leolin.shortcutbadger.ShortcutBadger;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class GlobalFunctions {
    public static SessionManager sessionManager;
    private static int WRITE_PERMISSION_CODE = 23;
    private static int READ_PERMISSION_CODE = 33;
    private static int CAMERA_CODE = 43;

    public static void setUpFont(Context context) {
        sessionManager = new SessionManager(context);
        if (sessionManager.getUserLanguage().equals(Constants.EN)) {
            ViewPump.init(ViewPump.builder()
                    .addInterceptor(new CalligraphyInterceptor(
                            new CalligraphyConfig.Builder()
                                    .setDefaultFontPath(Constants.MONTSERRAT_REGULAR)
                                    .setFontAttrId(R.attr.fontPath)
                                    .build()))
                    .build());
        } else {
            ViewPump.init(ViewPump.builder()
                    .addInterceptor(new CalligraphyInterceptor(
                            new CalligraphyConfig.Builder()
                                    .setDefaultFontPath(Constants.DROID_ARABIC_REGULAR)
                                    .setFontAttrId(R.attr.fontPath)
                                    .build()))
                    .build());
        }
    }

    public static void DisableLayout(ViewGroup layout) {
        layout.setEnabled(false);
        for (int i = 0; i < layout.getChildCount(); i++) {
            View child = layout.getChildAt(i);
            if (child instanceof ViewGroup) {
                DisableLayout((ViewGroup) child);
            } else {
                child.setEnabled(false);
            }
        }
    }

    public static void EnableLayout(ViewGroup layout) {
        layout.setEnabled(true);
        for (int i = 0; i < layout.getChildCount(); i++) {
            View child = layout.getChildAt(i);
            if (child instanceof ViewGroup) {
                EnableLayout((ViewGroup) child);
            } else {
                child.setEnabled(true);
            }
        }
    }

    public static String formatDate(String date) {
        String dateResult = "";
        Locale locale = new Locale(Constants.EN);


        SimpleDateFormat dateFormatter1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", locale);
        SimpleDateFormat dateFormatter2 = new SimpleDateFormat("MM/dd/yyyy", locale);

        //SimpleDateFormat dateFormatter2 = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss aaa", locale);

        int index = date.lastIndexOf('/');

        try {

            dateResult = dateFormatter2.format(dateFormatter1.parse(date.substring(index + 1)));

        } catch (ParseException e) {

            e.printStackTrace();

        }

        return dateResult;
    }

    public static String formatDateAndTime(String dateAndTime) {
        String dateResult = "";
        Locale locale = new Locale("en");


        SimpleDateFormat dateFormatter1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", locale);
        SimpleDateFormat dateFormatter2 = new SimpleDateFormat("MM/dd/yyyy\nhh:mm aaa", locale);
        int index = dateAndTime.lastIndexOf('/');

        try {

            dateResult = dateFormatter2.format(dateFormatter1.parse(dateAndTime.substring(index + 1)));

        } catch (ParseException e) {

            e.printStackTrace();

        }

        dateFormatter2.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date date = null;
        try {
            date = dateFormatter2.parse(dateResult);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        dateFormatter2.setTimeZone(TimeZone.getDefault());
        String formattedDate = dateFormatter2.format(date);
        return formattedDate;
    }

    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnected() && netInfo.isAvailable()) {
            return true;
        } else {
            return false;
        }

    }

    public static void generalErrorMessage(ProgressBar loading, Context context) {
        loading.setVisibility(View.GONE);
        Snackbar.make(loading, context.getString(R.string.generalError), Snackbar.LENGTH_SHORT).show();
    }

    public static void setDefaultLanguage(Context context) {
        SessionManager sessionManager = new SessionManager(context);
        String language = sessionManager.getUserLanguage();
        if (language.equals(Constants.EN)) {
            MainActivity.isEnglish = true;
            LocaleHelper.setLocale(context, Constants.EN);
        } else {
            MainActivity.isEnglish = false;
            LocaleHelper.setLocale(context, Constants.AR);
            if (language == null || language.isEmpty()) {
                sessionManager.setUserLanguage(Constants.AR);
            }
        }
    }


    public static Bitmap loadVideoFrameFromPath(String p_videoPath)
            throws Throwable {
        Bitmap m_bitmap = null;
        MediaMetadataRetriever m_mediaMetadataRetriever = null;
        try {
            m_mediaMetadataRetriever = new MediaMetadataRetriever();
            m_mediaMetadataRetriever.setDataSource(p_videoPath);
            m_bitmap = m_mediaMetadataRetriever.getFrameAtTime();
        } catch (Exception m_e) {
            throw new Throwable(
                    "Exception in retriveVideoFrameFromVideo(String p_videoPath)"
                            + m_e.getMessage());
        } finally {
            if (m_mediaMetadataRetriever != null) {
                m_mediaMetadataRetriever.release();
            }
        }
        return m_bitmap;
    }


    public static boolean isWriteExternalStorageAllowed(FragmentActivity activity) {
        //Getting the permission status
        int result = ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        //If permission is granted returning true
        if (result == PackageManager.PERMISSION_GRANTED)
            return true;

        //If permission is not granted returning false
        return false;
    }

    //Requesting permission
    public static void requestWriteExternalStoragePermission(FragmentActivity activity) {

        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            //If the user has denied the permission previously your code will come to this block
            //Here you can explain why you need this permission
            //Explain here why you need this permission

            //checkMyPermission();
        }

        //And finally ask for the permission
        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_PERMISSION_CODE);
    }


    public static boolean isReadExternalStorageAllowed(FragmentActivity activity) {
        //Getting the permission status
        int result = ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE);

        //If permission is granted returning true
        if (result == PackageManager.PERMISSION_GRANTED)
            return true;

        //If permission is not granted returning false
        return false;
    }

    //Requesting permission
    public static void requestReadExternalStoragePermission(FragmentActivity activity) {

        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            //If the user has denied the permission previously your code will come to this block
            //Here you can explain why you need this permission
            //Explain here why you need this permission

            //checkMyPermission();
        }

        //And finally ask for the permission
        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_PERMISSION_CODE);
    }

    public static boolean isCameraPermission(FragmentActivity activity) {
        //Getting the permission status
        int result = ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA);

        //If permission is granted returning true
        if (result == PackageManager.PERMISSION_GRANTED)
            return true;

        //If permission is not granted returning false
        return false;
    }

    public static void requestCameraPermission(FragmentActivity activity) {

        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.CAMERA)) {
            //If the user has denied the permission previously your code will come to this block
            //Here you can explain why you need this permission
            //Explain here why you need this permission

            //checkMyPermission();
        }

        //And finally ask for the permission
        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.CAMERA}, CAMERA_CODE);
    }


    public static boolean isFineLocationPermission(FragmentActivity activity) {
        //Getting the permission status
        int result = ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION);

        //If permission is granted returning true
        if (result == PackageManager.PERMISSION_GRANTED)
            return true;

        //If permission is not granted returning false
        return false;
    }

    public static void requestFineLocationPermission(FragmentActivity activity) {

        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_FINE_LOCATION)) {
            //If the user has denied the permission previously your code will come to this block
            //Here you can explain why you need this permission
            //Explain here why you need this permission

            //checkMyPermission();
        }

        //And finally ask for the permission
        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, CAMERA_CODE);
    }

    public static void hasNewNotificationsApi(final Context context) {
        ShortcutBadger.removeCount(context);
        MainActivity.notifications.setImageResource(R.mipmap.ic_notifi_unsel);
        RetrofitConfig.getServices().NOTIFICATIONS_CALL(sessionManager.getUserToken(), sessionManager.getUserId(), 0)
                .enqueue(new Callback<ArrayList<Notification>>() {
                    @Override
                    public void onResponse(Call<ArrayList<Notification>> call, Response<ArrayList<Notification>> response) {
                        int responseCode = response.code();
                        if (responseCode == 200) {
                            if (response.body() != null && response.body().size() > 0) {
                                int counter = 0;
                                for (Notification value : response.body()) {
                                    if (!value.isRead) {
                                        MainActivity.notifications.setImageResource(R.mipmap.ic_notifi_new);
                                        counter += 1;
                                    }
                                }
                                if (counter > 0) {
                                    ShortcutBadger.applyCount(context, counter);
                                } else {
                                    MainActivity.notifications.setImageResource(R.mipmap.ic_notifi_unsel);
                                }
                            }
                        } else {
                            MainActivity.notifications.setImageResource(R.mipmap.ic_notifi_unsel);
                        }
                    }

                    @Override
                    public void onFailure(Call<ArrayList<Notification>> call, Throwable t) {
                        MainActivity.notifications.setImageResource(R.mipmap.ic_notifi_unsel);
                    }
                });
    }

    public static Date currentDate() {
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy");
        String formattedDate = df.format(c);
        Date date = null;
        try {
            date = df.parse(formattedDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public static long differenceDate(Date startDate, Date endDate) {
        //milliseconds
        long different = endDate.getTime() - startDate.getTime();
        System.out.println("startDate : " + startDate);
        System.out.println("endDate : " + endDate);
        System.out.println("different : " + different);
        long secondsInMilli = 1000;
        long minutesInMilli = secondsInMilli * 60;
        long hoursInMilli = minutesInMilli * 60;
        long daysInMilli = hoursInMilli * 24;
        long elapsedDays = different / daysInMilli;
        different = different % daysInMilli;
        long elapsedHours = different / hoursInMilli;
        different = different % hoursInMilli;
        long elapsedMinutes = different / minutesInMilli;
        different = different % minutesInMilli;
        long elapsedSeconds = different / secondsInMilli;
        return elapsedDays;
    }
}

