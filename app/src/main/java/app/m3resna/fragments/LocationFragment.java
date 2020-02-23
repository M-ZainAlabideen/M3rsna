package app.m3resna.fragments;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import app.m3resna.R;
import app.m3resna.classes.Constants;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LocationFragment extends Fragment {
    public static FragmentActivity activity;
    public static LocationFragment fragment;
    private View childView;

    public static LocationFragment newInstance(FragmentActivity activity, String latitude, String longitude) {
        fragment = new LocationFragment();
        LocationFragment.activity = activity;
        Bundle b = new Bundle();
        b.putString(Constants.LATITUDE, latitude);
        b.putString(Constants.LONGITUDE, longitude);
        fragment.setArguments(b);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        childView = inflater.inflate(R.layout.fragment_location, container, false);
        ButterKnife.bind(this, childView);
        return childView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.fragment_location_f_map);  //use SuppoprtMapFragment for using in fragment instead of activity  MapFragment = activity   SupportMapFragment = fragment
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap mMap) {
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

                mMap.clear(); //clear old markers

                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(
                        Double.parseDouble(getArguments().getString(Constants.LATITUDE)),
                        Double.parseDouble(getArguments().getString(Constants.LONGITUDE))), 14.0f));


                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(
                                Double.parseDouble(getArguments().getString(Constants.LATITUDE)),
                                Double.parseDouble(getArguments().getString(Constants.LONGITUDE))))
                        .icon(bitmapDescriptorFromVector(getActivity(), R.mipmap.ic_mark)));

            }
        });


    }


    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }


    @OnClick(R.id.fragment_location_tv_getDirections)
    public void getDirectionsClick() {
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]
                    {android.Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 100);

        } else {

            String url = "http://maps.google.com/maps?daddr=" +
                    Double.parseDouble(getArguments().getString(Constants.LATITUDE)) + ""
                    + "," + Double.parseDouble(getArguments().getString(Constants.LONGITUDE)) + "" + "&mode=driving";
            Uri uri = Uri.parse(url);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.setPackage("com.google.android.apps.maps");
            startActivity(intent);
        }
    }
}



