package app.m3resna.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;

import app.m3resna.R;
import app.m3resna.classes.FixControl;
import app.m3resna.models.AdImage;
import butterknife.BindView;
import butterknife.ButterKnife;


public class AdImagesAdapter extends RecyclerView.Adapter<AdImagesAdapter.viewHolder> {
    Context context;
    ArrayList<AdImage> adImagesList;
    AdImagesAdapter.OnItemClickListener listener;

    public AdImagesAdapter(Context context, ArrayList<AdImage> adImagesList, AdImagesAdapter.OnItemClickListener listener) {
        this.context = context;
        this.adImagesList = adImagesList;
        this.listener = listener;
    }


    public class viewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.item_ad_image_iv_image)
        ImageView image;
        @BindView(R.id.item_ad_image_iv_delete)
        ImageView delete;

        public viewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

    }

    @NonNull
    @Override
    public AdImagesAdapter.viewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View childView = LayoutInflater.from(context).inflate(R.layout.item_ad_image, viewGroup, false);
        return new AdImagesAdapter.viewHolder(childView);
    }

    @Override
    public void onBindViewHolder(@NonNull AdImagesAdapter.viewHolder viewHolder, final int position) {

        setImage(adImagesList.get(position).image, viewHolder.image);

        viewHolder.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.openImageClick(position);
            }
        });

        viewHolder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.deleteImageClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return adImagesList.size();
    }

    public interface OnItemClickListener {
        public void openImageClick(int position);

        public void deleteImageClick(int position);
    }

    private void setImage(String imagePath, ImageView image) {
        if (imagePath != null) {
            try {
                int Width = FixControl.getImageWidth(context, R.mipmap.placeholder_media);
                int Height = FixControl.getImageHeight(context, R.mipmap.placeholder_media);
                image.getLayoutParams().height = Height;
                image.getLayoutParams().width = Width;
                Glide.with(context.getApplicationContext()).load(imagePath)
                        .apply(new RequestOptions().centerCrop()
                                .placeholder(R.mipmap.placeholder_media))
                        .into(image);

            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }
    }
}

