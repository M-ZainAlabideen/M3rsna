package app.m3resna.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;

import app.m3resna.R;
import app.m3resna.classes.Constants;
import app.m3resna.classes.FixControl;
import app.m3resna.classes.Navigator;
import app.m3resna.fragments.ImageGestureFragment;
import app.m3resna.fragments.ProductDetailsFragment;
import app.m3resna.fragments.UrlsFragment;
import app.m3resna.webservices.models.Slider;

public class SliderAdapter extends PagerAdapter {
    Context context;
    ArrayList<Slider> sliderList;
    boolean isProductDetails;

    public SliderAdapter(Context context, ArrayList<Slider> sliderList, boolean isProductDetails) {
        this.context = context;
        this.sliderList = sliderList;
        this.isProductDetails = isProductDetails;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, final int position) {
        View childView = LayoutInflater.from(context).inflate(R.layout.item_slider, container, false);
        ImageView sliderImage = (ImageView) childView.findViewById(R.id.item_slider_iv_sliderImg);
        ImageView shadow = (ImageView) childView.findViewById(R.id.item_slider_iv_shadow);
        ImageView play = (ImageView) childView.findViewById(R.id.item_slider_iv_play);
        if (sliderList.get(position).fileType != null && sliderList.get(position).fileType.equals(Constants.VIDEO)) {
            shadow.setVisibility(View.VISIBLE);
            play.setVisibility(View.VISIBLE);
        } else {
            shadow.setVisibility(View.GONE);
            play.setVisibility(View.GONE);
        }
        if (sliderList.get(position).path != null
                && !sliderList.get(position).path.isEmpty()) {
            setImage(sliderList.get(position).path,sliderImage);
        }

        sliderImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<String> paths = new ArrayList<>();

                if (isProductDetails) {
                    for (Slider item : sliderList) {
                        if (item.fileType.equals(Constants.IMAGE)) {
                            paths.add(item.path);
                        }
                    }
                    Navigator.loadFragment((FragmentActivity) context, ImageGestureFragment.newInstance((FragmentActivity) context, paths, position), R.id.app_bar_main_fl_mainContainer, true);
                } else {
                    if (sliderList.get(position).productId == 0) {
                        paths.add(sliderList.get(position).path);
                        Navigator.loadFragment((FragmentActivity) context, ImageGestureFragment.newInstance((FragmentActivity) context, paths, position), R.id.app_bar_main_fl_mainContainer, true);
                    } else {
                        Navigator.loadFragment((FragmentActivity) context, ProductDetailsFragment.newInstance((FragmentActivity) context, sliderList.get(position).productId), R.id.app_bar_main_fl_mainContainer, true);
                    }
                }
            }
        });

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigator.loadFragment((FragmentActivity) context, UrlsFragment.newInstance((FragmentActivity) context, sliderList.get(position).videoPath, false, Constants.VIDEO), R.id.app_bar_main_fl_mainContainer, true);
            }
        });

        container.addView(childView, 0);
        return childView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getCount() {
        return sliderList.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view.equals(object);
    }

    @Override
    public int getItemPosition(Object object) {
        // refresh all fragments when data set changed
        return PagerAdapter.POSITION_NONE;
    }

    private void setImage(String imagePath, ImageView image) {
        if (imagePath != null) {
            try {
                int Width = FixControl.getImageWidth(context, R.mipmap.placeholder_slider);
                int Height = FixControl.getImageHeight(context, R.mipmap.placeholder_slider);
                image.getLayoutParams().height = Height;
                image.getLayoutParams().width = Width;
                Glide.with(context.getApplicationContext()).load(imagePath)
                        .apply(new RequestOptions().centerCrop()
                                .placeholder(R.mipmap.placeholder_slider))
                        .into(image);

            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }
    }
}

