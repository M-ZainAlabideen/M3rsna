package app.m3resna.fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
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
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.nguyenhoanglam.imagepicker.model.Config;
import com.nguyenhoanglam.imagepicker.model.Image;
import com.nguyenhoanglam.imagepicker.ui.imagepicker.ImagePicker;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.lang.reflect.Type;
import java.util.ArrayList;

import app.m3resna.MainActivity;
import app.m3resna.R;
import app.m3resna.adapters.GeneralDialogAdapter;
import app.m3resna.classes.Constants;
import app.m3resna.classes.FixControl;
import app.m3resna.classes.GlobalFunctions;
import app.m3resna.classes.RecyclerItemClickListener;
import app.m3resna.classes.SessionManager;
import app.m3resna.webservices.RetrofitConfig;
import app.m3resna.webservices.models.Country;
import app.m3resna.webservices.models.User;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.app.Activity.RESULT_OK;

public class EditProfileFragment extends Fragment {
    public static FragmentActivity activity;
    public static EditProfileFragment fragment;
    private View childView;
    private SessionManager sessionManager;
    private User user;
    private ArrayList<Country> countriesList = new ArrayList<>();
    private AlertDialog dialog;
    private int countryId;

    @BindView(R.id.fragment_edit_profile_cl_container)
    ConstraintLayout container;
    @BindView(R.id.fragment_edit_profile_iv_companyImg)
    ImageView companyImg;
    @BindView(R.id.fragment_edit_profile_et_name)
    EditText name;
    @BindView(R.id.fragment_edit_profile_et_email)
    EditText email;
    @BindView(R.id.fragment_edit_profile_tv_countryName)
    TextView countryName;
    @BindView(R.id.fragment_add_new_ad_iv_countryFlag)
    ImageView countryFlag;
    @BindView(R.id.fragment_edit_profile_et_phoneNumber)
    EditText phoneNumber;
    @BindView(R.id.fragment_edit_profile_et_instagramLink)
    EditText instagramLink;
    @BindView(R.id.fragment_edit_profile_et_aboutCompany)
    EditText aboutCompany;
    @BindView(R.id.loading)
    ProgressBar loading;
    private String companyImgPath;

    public static EditProfileFragment newInstance(FragmentActivity activity) {
        fragment = new EditProfileFragment();
        EditProfileFragment.activity = activity;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        childView = inflater.inflate(R.layout.fragment_edit_profile, container, false);
        ButterKnife.bind(this, childView);
        return childView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MainActivity.setupAppbar(true, false, true, getString(R.string.editProfile),
                true, true, false, true, true, Constants.ACCOUNT);
        sessionManager = new SessionManager(activity);
        GlobalFunctions.hasNewNotificationsApi(activity);
        FixControl.setupUI(container, activity);

        if (user == null) {
            if (!GlobalFunctions.isNetworkConnected(activity)) {
                Snackbar.make(childView, getString(R.string.noConnection), Snackbar.LENGTH_SHORT).show();
            } else {
                userDataApi();
            }
        } else {
            loading.setVisibility(View.GONE);
        }
    }

    private void userDataApi() {
        GlobalFunctions.DisableLayout(container);
        RetrofitConfig.getServices().USER_DATA_CALL(sessionManager.getUserToken(), sessionManager.getUserId())
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        loading.setVisibility(View.GONE);
                        GlobalFunctions.EnableLayout(container);
                        int responseCode = response.code();
                        if (responseCode == 200) {
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

                                Type type = new TypeToken<User>() {
                                }.getType();

                                JsonReader reader = new JsonReader(new StringReader(outResponse));

                                reader.setLenient(true);

                                user = new Gson().fromJson(jsonResponse, type);
                            }
                            name.setText(user.fullName);
                            email.setText(user.email);
                            phoneNumber.setText(user.mobile);
                            if (user.instagramLink != null)
                                instagramLink.setText(user.instagramLink);
                            if (user.about != null)
                                aboutCompany.setText(user.about);
                            setImage(user.photoUrl, companyImg);
                            countriesApi();
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
                loading.setVisibility(View.GONE);
                GlobalFunctions.EnableLayout(container);
                int responseCode = response.code();
                if (responseCode == 200) {
                    if (response.body() != null && response.body().size() > 0) {
                        countriesList.clear();
                        countriesList.addAll(response.body());
                        for (Country item : countriesList) {
                            if (item.id == user.countryId) {
                                countryName.setText(item.getName());
                                countryId = item.id;
                                setFlagImage(item.flagUrl, countryFlag);

                            }
                        }

                    }
                } else if (responseCode == 202) {
                    Log.d(Constants.M3RESNA_APP, "countries not found");
                } else {
                    Snackbar.make(childView, getString(R.string.generalError), Snackbar.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ArrayList<Country>> call, Throwable t) {
                GlobalFunctions.generalErrorMessage(loading, activity);
                GlobalFunctions.EnableLayout(container);
            }
        });
    }

    @OnClick(R.id.fragment_edit_profile_iv_companyImg)
    public void companyImgCLick() {
        ImagePicker.with(this)              //  Initialize ImagePicker with activity or fragment context
                .setCameraOnly(false)               //  Camera mode
                .setMultipleMode(false)              //  Select multiple images or single image
                .setFolderMode(true)                //  Folder mode
                .setShowCamera(true)                //  Show camera button
                .setMaxSize(1)                     //  Max images can be selected
                .setSavePath("ImagePicker")         //  Image capture folder name
                .setAlwaysShowDoneButton(true)      //  Set always show done button in multiple mode
                .setKeepScreenOn(true)              //  Keep screen on when selecting images
                .start();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        loading.setVisibility(View.GONE);
        if (requestCode == Config.RC_PICK_IMAGES && resultCode == RESULT_OK && data != null) {
            ArrayList<Image> images = data.getParcelableArrayListExtra(Config.EXTRA_IMAGES);
            if (images != null) {
                for (Image uri : images) {
                    setImage(uri.getPath(), companyImg);
                    companyImgPath = uri.getPath();
                }
            }
        }
    }

    private void setImage(String imagePath, ImageView image) {
        if (imagePath != null) {
            try {
                int Width = FixControl.getImageWidth(activity, R.mipmap.placeholder_profile_image);
                int Height = FixControl.getImageHeight(activity, R.mipmap.placeholder_profile_image);
                image.getLayoutParams().height = Height;
                image.getLayoutParams().width = Width;
                Glide.with(activity).load(imagePath)
                        .apply(new RequestOptions().centerCrop()
                                .placeholder(R.mipmap.placeholder_profile_image))
                        .into(image);

            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }
    }

    private void setFlagImage(String imagePath, ImageView image) {
        if (imagePath != null) {
            try {
                int Width = FixControl.getImageWidth(activity, R.mipmap.placeholder_flag);
                int Height = FixControl.getImageHeight(activity, R.mipmap.placeholder_flag);
                image.getLayoutParams().height = Height;
                image.getLayoutParams().width = Width;
                Glide.with(activity).load(imagePath)
                        .apply(new RequestOptions().centerCrop()
                                .placeholder(R.mipmap.placeholder_flag))
                        .into(image);

            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }
    }

    @OnClick(R.id.fragment_edit_profile_v_selectCountry)
    public void selectCountryClick() {
        createCountriesDialog();
    }

    private void createCountriesDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        View popUpView = activity.getLayoutInflater().inflate(R.layout.dialog_general, null);
        RecyclerView countries = (RecyclerView) popUpView.findViewById(R.id.dialog_general_rv_generalDialog);
        countries.setLayoutManager(new LinearLayoutManager(activity));
        countries.setAdapter(new GeneralDialogAdapter(activity, Constants.COUNTRY, null, null, countriesList, null, null));
        builder.setCancelable(true);
        builder.setView(popUpView);
        dialog = builder.create();
        dialog.show();
        countries.addOnItemTouchListener(new RecyclerItemClickListener(activity, countries, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                countryId = countriesList.get(position).id;
                countryName.setText(countriesList.get(position).getName());
                setFlagImage(countriesList.get(position).flagUrl, countryFlag);
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

    @OnClick(R.id.fragment_edit_profile_tv_done)
    public void doneCLick() {
        String fullNameStr = name.getText().toString();
        String emailStr = email.getText().toString();
        String phoneNumberStr = phoneNumber.getText().toString();
        String instagramLinkStr = instagramLink.getText().toString();
        String aboutCompanyStr = aboutCompany.getText().toString();
        if (fullNameStr == null || fullNameStr.isEmpty()) {
            name.setError(getString(R.string.enterName));
        } else {
            name.setError(null);
        }
        if (emailStr == null || emailStr.isEmpty()) {
            email.setError(getString(R.string.enterEmail));
        } else {
            if (!FixControl.isValidEmail(emailStr)) {
                email.setError(getString(R.string.invalidEmail));
            } else {
                email.setError(null);
            }
        }

        if (phoneNumberStr == null || phoneNumberStr.isEmpty()) {
            phoneNumber.setError(getString(R.string.enterPhone));
        } else {
            if (!FixControl.isValidPhone(phoneNumberStr)) {
                phoneNumber.setError(getString(R.string.invalidPhone));
            } else {
                phoneNumber.setError(null);
            }
        }
        if (fullNameStr != null && !fullNameStr.isEmpty() && emailStr != null
                && !emailStr.isEmpty() && phoneNumberStr != null && !phoneNumberStr.isEmpty()
                && FixControl.isValidPhone(phoneNumberStr) && FixControl.isValidEmail(emailStr)) {
            prepareProfileData(fullNameStr, phoneNumberStr, emailStr, instagramLinkStr, aboutCompanyStr);
        }
    }

    private void prepareProfileData(String fullNameStr, String phoneNumberStr, String emailStr, String instagramLinkStr, String aboutCompanyStr) {
        File imageFile = null;
        RequestBody imageRequestBody = null;
        MultipartBody.Part companyImgPart = null;
        if (companyImgPath != null) {
            imageFile = new File(companyImgPath);
            imageRequestBody = RequestBody.create(MediaType.parse("image/*"), imageFile);
            companyImgPart = MultipartBody.Part.createFormData("userimage", imageFile.getName(), imageRequestBody);
        }
        RequestBody userIdReq = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(sessionManager.getUserId()));
        RequestBody fullNameReq = RequestBody.create(MediaType.parse("text/plain"), fullNameStr);
        RequestBody phoneNumberReq = RequestBody.create(MediaType.parse("text/plain"), phoneNumberStr);
        RequestBody emailReq = RequestBody.create(MediaType.parse("text/plain"), emailStr);
        RequestBody instagramLinkReq = null;
        if (instagramLinkStr != null && !instagramLinkStr.isEmpty()) {
            instagramLinkReq = RequestBody.create(MediaType.parse("text/plain"), instagramLinkStr);
        }
        RequestBody aboutCompanyReq = null;
        if (aboutCompanyStr != null && !aboutCompanyStr.isEmpty()) {
            aboutCompanyReq = RequestBody.create(MediaType.parse("text/plain"), aboutCompanyStr);
        }
        RequestBody countryIdReq = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(countryId));

        editProfileApi(userIdReq, fullNameReq, phoneNumberReq, emailReq, instagramLinkReq, aboutCompanyReq, countryIdReq, companyImgPart);
    }

    private void editProfileApi(RequestBody userIdReq,
                                RequestBody fullNameReq,
                                RequestBody phoneNumberReq,
                                RequestBody emailReq,
                                RequestBody instagramLinkReq,
                                RequestBody aboutCompanyReq,
                                RequestBody countryIdReq,
                                MultipartBody.Part companyImgPart) {
        loading.setVisibility(View.VISIBLE);
        GlobalFunctions.DisableLayout(container);
        RetrofitConfig.getServices().EDIT_PROFILE_CALL(sessionManager.getUserToken(),
                userIdReq, fullNameReq, phoneNumberReq, emailReq, instagramLinkReq, aboutCompanyReq, countryIdReq, companyImgPart)
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        Snackbar.make(childView, getString(R.string.profileUpdatedSuccessfully), Snackbar.LENGTH_SHORT).show();
                        loading.setVisibility(View.GONE);
                        GlobalFunctions.EnableLayout(container);
                        int responseCode = response.code();
                        if (responseCode == 200) {
                            User user = null;
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

                                Type type = new TypeToken<User>() {
                                }.getType();

                                JsonReader reader = new JsonReader(new StringReader(outResponse));

                                reader.setLenient(true);

                                user = new Gson().fromJson(jsonResponse, type);
                            }
                            clearCurrentFragment();
                            Snackbar.make(childView, getString(R.string.profileUpdatedSuccessfully), Snackbar.LENGTH_SHORT).show();
                            sessionManager.setUserId(user.id);
                            sessionManager.setCountryId(user.countryId);
                            for (Country item : countriesList) {
                                if (item.id == user.countryId) {
                                    sessionManager.setCountryName(item.arabicName,item.englishName);
                                }
                            }
                            sessionManager.setName(user.fullName);
                            sessionManager.setEmail(user.email);
                            sessionManager.setPhoneNumber(user.mobile);
                        } else if (responseCode == 201) {
                            email.setError(getString(R.string.emailExists));
                        } else if (responseCode == 203) {
                            phoneNumber.setError(getString(R.string.phoneExists));
                        } else if (responseCode == 204) {
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


    private void clearCurrentFragment() {
        getFragmentManager().popBackStack();
    }
}


