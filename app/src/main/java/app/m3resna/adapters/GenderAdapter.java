package app.m3resna.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import app.m3resna.R;
import app.m3resna.webservices.models.Gender;
import butterknife.BindView;
import butterknife.ButterKnife;

public class GenderAdapter extends RecyclerView.Adapter<GenderAdapter.viewHolder> {
    Context context;
    ArrayList<Gender> gendersList;
    public static int selectedPosition = 0;
    OnItemClickListener listener;

    public GenderAdapter(Context context, ArrayList<Gender> gendersList,OnItemClickListener listener) {
        this.context = context;
        this.gendersList = gendersList;
        this.listener = listener;
    }


    public class viewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.item_gender_tv_genderName)
        TextView genderName;

        public viewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    @NonNull
    @Override
    public GenderAdapter.viewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View childView = LayoutInflater.from(context).inflate(R.layout.item_gender, viewGroup, false);
        return new GenderAdapter.viewHolder(childView);
    }

    @Override
    public void onBindViewHolder(@NonNull GenderAdapter.viewHolder viewHolder, final int position) {
        viewHolder.genderName.setText(gendersList.get(position).getName());
        if (position == selectedPosition) {
            viewHolder.genderName.setBackgroundResource(R.mipmap.box_tap_sel);
            viewHolder.genderName.setTextColor(context.getColor(R.color.white));
        } else {
            viewHolder.genderName.setBackgroundResource(R.mipmap.box_tap_unsel);
            viewHolder.genderName.setTextColor(context.getColor(R.color.orange));
        }
        viewHolder.genderName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.itemClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return gendersList.size();
    }

    public interface OnItemClickListener {
        public void itemClick(int position);
    }
}
