package app.m3resna.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;

import app.m3resna.R;
import app.m3resna.classes.FixControl;
import app.m3resna.webservices.models.AdPosition;
import butterknife.BindView;
import butterknife.ButterKnife;

public class AdsPositionsAdapter extends RecyclerView.Adapter<AdsPositionsAdapter.viewHolder> {
    Context context;
    ArrayList<AdPosition> adsPositionsList;

    public AdsPositionsAdapter(Context context, ArrayList<AdPosition> adsPositionsList) {
        this.context = context;
        this.adsPositionsList = adsPositionsList;
    }


    public class viewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.item_ad_position_iv_positionImg)
        ImageView positionImg;
        @BindView(R.id.item_ad_position_tv_adPosition)
        TextView adPosition;
        @BindView(R.id.item_ad_position_tv_adDuration)
        TextView adDuration;
        @BindView(R.id.item_ad_position_tv_adPrice)
        TextView adPrice;

        public viewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    @NonNull
    @Override
    public AdsPositionsAdapter.viewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View childView = LayoutInflater.from(context).inflate(R.layout.item_ad_position, viewGroup, false);
        return new AdsPositionsAdapter.viewHolder(childView);
    }

    @Override
    public void onBindViewHolder(@NonNull AdsPositionsAdapter.viewHolder viewHolder, final int position) {
        setImage(adsPositionsList.get(position).photoUrl, viewHolder.positionImg);
        viewHolder.adPosition.setText(adsPositionsList.get(position).getName());
        viewHolder.adDuration.setText(context.getString(R.string.duration) + " "
                + String.valueOf(adsPositionsList.get(position).numberOfDays) + " "
                + context.getString(R.string.days));
        if (adsPositionsList.get(position).price == 0) {
            viewHolder.adPrice.setVisibility(View.GONE);
        } else {
            viewHolder.adPrice.setText(context.getString(R.string.credit) + ": " + String.valueOf(adsPositionsList.get(position).price));
        }
    }

    @Override
    public int getItemCount() {
        return adsPositionsList.size();
    }

    private void setImage(String imagePath, ImageView image) {
        if (imagePath != null) {
            try {
                int Width = FixControl.getImageWidth(context, R.mipmap.placeholder_ad_position_image);
                int Height = FixControl.getImageHeight(context, R.mipmap.placeholder_ad_position_image);
                image.getLayoutParams().height = Height;
                image.getLayoutParams().width = Width;
                Glide.with(context.getApplicationContext()).load(imagePath)
                        .apply(new RequestOptions().centerCrop()
                                .placeholder(R.mipmap.placeholder_ad_position_image))
                        .into(image);

            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }
    }

}
