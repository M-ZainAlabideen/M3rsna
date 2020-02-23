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
import app.m3resna.classes.Constants;
import app.m3resna.classes.FixControl;
import app.m3resna.webservices.models.Category;
import app.m3resna.webservices.models.City;
import app.m3resna.webservices.models.Country;
import app.m3resna.webservices.models.Gender;
import app.m3resna.webservices.models.Zone;
import butterknife.BindView;
import butterknife.ButterKnife;

public class GeneralDialogAdapter extends RecyclerView.Adapter<GeneralDialogAdapter.viewHolder> {
    Context context;
    String flag;
    ArrayList<Category> categoriesList;
    ArrayList<Gender> gendersList;
    ArrayList<Country> countriesList;
    ArrayList<City> citiesList;
    ArrayList<Zone> zonesList;

    public GeneralDialogAdapter(Context context, String flag, ArrayList<Category> categoriesList, ArrayList<Gender> gendersList, ArrayList<Country> countriesList, ArrayList<City> citiesList, ArrayList<Zone> zonesList) {
        this.context = context;
        this.flag = flag;
        this.categoriesList = categoriesList;
        this.gendersList = gendersList;
        this.countriesList = countriesList;
        this.citiesList = citiesList;
        this.zonesList = zonesList;
    }


    public class viewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.item_dialog_general_tv_name)
        TextView name;
        @BindView(R.id.item_dialog_general_iv_flag)
        ImageView flag;

        public viewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    @NonNull
    @Override
    public GeneralDialogAdapter.viewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View childView = LayoutInflater.from(context).inflate(R.layout.item_dialog_general, viewGroup, false);
        return new GeneralDialogAdapter.viewHolder(childView);
    }

    @Override
    public void onBindViewHolder(@NonNull GeneralDialogAdapter.viewHolder viewHolder, final int position) {
        viewHolder.flag.setVisibility(View.GONE);
        if (flag.equals(Constants.CATEGORY)) {
            viewHolder.name.setText(categoriesList.get(position).getName());
        } else if (flag.equals(Constants.GENDER)) {
            viewHolder.name.setText(gendersList.get(position).getName());
        } else if (flag.equals(Constants.COUNTRY)) {
            viewHolder.flag.setVisibility(View.VISIBLE);
            setImage(countriesList.get(position).flagUrl,viewHolder.flag);
            viewHolder.name.setText(countriesList.get(position).getName());
        } else if (flag.equals(Constants.CITY)) {
            viewHolder.name.setText(citiesList.get(position).getName());
        } else if (flag.equals(Constants.ZONE)) {
            viewHolder.name.setText(zonesList.get(position).getName());
        }
    }

    @Override
    public int getItemCount() {
        if (flag.equals(Constants.CATEGORY)) {
            return categoriesList.size();
        } else if (flag.equals(Constants.GENDER)) {
            return gendersList.size();
        } else if (flag.equals(Constants.COUNTRY)) {
            return countriesList.size();
        } else if (flag.equals(Constants.CITY)) {
            return citiesList.size();
        } else {
            return zonesList.size();
        }
    }

    private void setImage(String imagePath,ImageView image) {
        if (imagePath != null) {
            try {
                /*in this case the height and width of cardView will used instead of the height and width of placeHolder
                 *for making the image after loading fill the cardView
                 * */
                int Width = FixControl.getImageWidth(context,R.mipmap.placeholder_flag);
                int Height = FixControl.getImageHeight(context,R.mipmap.placeholder_flag);
                image.getLayoutParams().height = Height;
                image.getLayoutParams().width = Width;
                Glide.with(context.getApplicationContext()).load(imagePath)
                        .apply(new RequestOptions().centerCrop()
                                .placeholder(R.mipmap.placeholder_flag))
                        .into(image);

            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }
    }
}
