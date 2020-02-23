package app.m3resna.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import app.m3resna.R;
import app.m3resna.webservices.models.City;
import butterknife.BindView;
import butterknife.ButterKnife;

public class CitiesAdapter extends RecyclerView.Adapter<CitiesAdapter.viewHolder> {
    Context context;
    ArrayList<City> citiesList;
    public CitiesAdapter(Context context, ArrayList<City> citiesList) {
        this.context = context;
        this.citiesList = citiesList;
    }


    public class viewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.item_city_tv_cityName)
        TextView cityName;

        public viewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    @NonNull
    @Override
    public CitiesAdapter.viewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View childView = LayoutInflater.from(context).inflate(R.layout.item_city, viewGroup, false);
        return new CitiesAdapter.viewHolder(childView);
    }

    @Override
    public void onBindViewHolder(@NonNull CitiesAdapter.viewHolder viewHolder, final int position) {
        viewHolder.cityName.setText(citiesList.get(position).getName());
    }

    @Override
    public int getItemCount() {
        return citiesList.size();
    }
}
