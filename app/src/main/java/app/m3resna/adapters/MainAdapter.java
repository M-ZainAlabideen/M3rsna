package app.m3resna.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;

import java.util.ArrayList;

import app.m3resna.R;
import app.m3resna.classes.Constants;
import app.m3resna.classes.Navigator;
import app.m3resna.classes.SessionManager;
import app.m3resna.fragments.AddAdFragment;
import app.m3resna.webservices.RetrofitConfig;
import app.m3resna.webservices.models.Category;
import app.m3resna.webservices.models.Product;
import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainAdapter extends RecyclerView.Adapter<MainAdapter.viewHolder> {
    Context context;
    SessionManager sessionManager;
    ArrayList<Category> categoriesList;
    ArrayList<Product> mixedList;
    ArrayList<Product> myAdsList;
    String flag;
    OnItemClickListener listener;

    public MainAdapter(Context context,
                       ArrayList<Category> categoriesList,
                       ArrayList<Product> mixedList,
                       ArrayList<Product> myAdsList,
                       String flag,
                       OnItemClickListener listener) {
        this.context = context;
        this.categoriesList = categoriesList;
        this.mixedList = mixedList;
        this.myAdsList = myAdsList;
        this.flag = flag;
        this.listener = listener;
    }


    public class viewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.item_main_cv_container)
        CardView container;
        @BindView(R.id.item_main_iv_image)
        ImageView image;
        @BindView(R.id.item_main_iv_makeRemoveFav)
        ImageView makeRemoveFav;
        @BindView(R.id.item_main_iv_delete)
        ImageView delete;
        @BindView(R.id.item_main_iv_edit)
        ImageView edit;
        @BindView(R.id.item_main_iv_pin)
        ImageView pin;
        @BindView(R.id.item_main_iv_shadow)
        ImageView shadow;
        @BindView(R.id.item_main_tv_item_ProductName)
        TextView productName;
        @BindView(R.id.item_main_tv_item_categoryName)
        TextView categoryName;
        @BindView(R.id.item_main_tv_endAd)
        TextView endAd;
        @BindView(R.id.item_main_tv_renewAd)
        TextView renewAd;
        @BindView(R.id.loading)
        ProgressBar loading;

        public viewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

    }

    @NonNull
    @Override
    public MainAdapter.viewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View childView = LayoutInflater.from(context).inflate(R.layout.item_main, viewGroup, false);
        sessionManager = new SessionManager(context);
        return new MainAdapter.viewHolder(childView);
    }

    @Override
    public void onBindViewHolder(@NonNull MainAdapter.viewHolder viewHolder, final int position) {

        setupViewVisibility(flag, viewHolder, position);

        if (flag.equals(Constants.CATEGORY)) {
            viewHolder.categoryName.setText(categoriesList.get(position).getName());
            setImage(categoriesList.get(position).imageUrl, viewHolder);

        } else if (flag.equals(Constants.PRODUCT)) {
            if (mixedList.get(position).isAd) {
                setImage(mixedList.get(position).photoUrl, viewHolder);
                viewHolder.productName.setText("");
            } else {
                viewHolder.productName.setText(mixedList.get(position).title);
                if (mixedList.get(position).isFavorite) {
                    viewHolder.makeRemoveFav.setImageResource(R.mipmap.ic_heart_sel);
                } else {
                    viewHolder.makeRemoveFav.setImageResource(R.mipmap.ic_heart_unsel);
                }
                setImage(mixedList.get(position).productAttachment.fileUrl, viewHolder);
            }
        } else if (flag.equals(Constants.MY_ADS)) {
            if (myAdsList.get(position).getRemainDays() <= 5) {
                viewHolder.renewAd.setVisibility(View.VISIBLE);
                viewHolder.endAd.setVisibility(View.VISIBLE);
                viewHolder.endAd.setText(context.getString(R.string.endAd)+" "+myAdsList.get(position).getRemainDays()+" "+context.getString(R.string.days));
            }
            viewHolder.productName.setText(myAdsList.get(position).title);
            setImage(myAdsList.get(position).productAttachment.fileUrl, viewHolder);

        }

        viewHolder.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.imageClick(position);
            }
        });

        viewHolder.makeRemoveFav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.favClick(position, viewHolder.makeRemoveFav);
            }
        });

        viewHolder.edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigator.loadFragment((FragmentActivity) context, AddAdFragment.newInstance((FragmentActivity) context, myAdsList.get(position).id), R.id.app_bar_main_fl_mainContainer, true);
            }
        });

        viewHolder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteAlertMessage(position);
            }
        });

        viewHolder.renewAd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.renewClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        if (flag.equals(Constants.CATEGORY)) {
            return categoriesList.size();
        } else if (flag.equals(Constants.PRODUCT)) {
            return mixedList.size();
        } else {
            return myAdsList.size();
        }
    }

    public interface OnItemClickListener {
        public void imageClick(int position);

        public void favClick(int position, ImageView makeRemoveFav);

        public void renewClick(int position);
    }

    private void setImage(String imagePath, MainAdapter.viewHolder viewHolder) {
        if (imagePath != null) {
            try {
                /*in this case the height and width of cardView will used instead of the height and width of placeHolder
                 *for making the image after loading fill the cardView
                 * */
                int Width = viewHolder.container.getWidth();
                int Height = viewHolder.container.getHeight();
                viewHolder.image.getLayoutParams().height = Height;
                viewHolder.image.getLayoutParams().width = Width;
                Glide.with(context.getApplicationContext()).load(imagePath)
                        .apply(new RequestOptions().centerCrop()
                                .placeholder(R.mipmap.placeholder_media)).listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        viewHolder.loading.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        viewHolder.loading.setVisibility(View.GONE);
                        return false;
                    }
                }).into(viewHolder.image);

            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }
    }

    private void setupViewVisibility(String flag, MainAdapter.viewHolder viewHolder, int position) {
        viewHolder.endAd.setVisibility(View.INVISIBLE);
        viewHolder.renewAd.setVisibility(View.INVISIBLE);
        if (flag.equals(Constants.CATEGORY)) {
            viewHolder.makeRemoveFav.setVisibility(View.INVISIBLE);
            viewHolder.delete.setVisibility(View.INVISIBLE);
            viewHolder.edit.setVisibility(View.INVISIBLE);
            viewHolder.pin.setVisibility(View.INVISIBLE);
            viewHolder.shadow.setVisibility(View.VISIBLE);
            viewHolder.categoryName.setVisibility(View.VISIBLE);
            viewHolder.productName.setVisibility(View.INVISIBLE);
        } else if (flag.equals(Constants.PRODUCT)) {
            viewHolder.edit.setVisibility(View.INVISIBLE);
            viewHolder.categoryName.setVisibility(View.INVISIBLE);
            viewHolder.delete.setVisibility(View.INVISIBLE);
            viewHolder.productName.setVisibility(View.VISIBLE);
            if (mixedList.get(position).isAd) {
                viewHolder.makeRemoveFav.setVisibility(View.INVISIBLE);
                viewHolder.shadow.setVisibility(View.INVISIBLE);
                viewHolder.pin.setVisibility(View.INVISIBLE);
            } else {
                viewHolder.makeRemoveFav.setVisibility(View.VISIBLE);
                viewHolder.shadow.setVisibility(View.VISIBLE);
                if (mixedList.get(position).isPin) {
                    viewHolder.pin.setVisibility(View.VISIBLE);
                } else {
                    viewHolder.pin.setVisibility(View.INVISIBLE);
                }
            }
        } else if (flag.equals(Constants.MY_ADS)) {
            viewHolder.makeRemoveFav.setVisibility(View.INVISIBLE);
            viewHolder.delete.setVisibility(View.VISIBLE);
            viewHolder.edit.setVisibility(View.VISIBLE);
            viewHolder.pin.setVisibility(View.INVISIBLE);
            viewHolder.shadow.setVisibility(View.VISIBLE);
            viewHolder.categoryName.setVisibility(View.INVISIBLE);
            viewHolder.productName.setVisibility(View.VISIBLE);
        }
    }

    private void showDeleteAlertMessage(final int position) {
        new AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.app_name))
                .setMessage(context.getString(R.string.sureForDelete))
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        deleteAdApi(myAdsList.get(position).id, position);
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setIcon(R.drawable.ic_launcher_icon)
                .show();
    }

    private void deleteAdApi(int AdId, int position) {
        RetrofitConfig.getServices().DELETE_AD_CALL(sessionManager.getUserToken(), AdId, sessionManager.getUserId())
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.code() == 200) {
                            myAdsList.remove(position);
                            notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {

                    }
                });
    }
}
