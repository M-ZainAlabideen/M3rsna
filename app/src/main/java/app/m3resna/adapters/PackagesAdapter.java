package app.m3resna.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import app.m3resna.R;
import app.m3resna.classes.Constants;
import app.m3resna.classes.SessionManager;
import app.m3resna.webservices.models.Package;
import butterknife.BindView;
import butterknife.ButterKnife;

/*
 * the Adapter of Packages which will be shown for user to select one,
 * after selection of package the  user can add Ads in application.
 * */

public class PackagesAdapter extends RecyclerView.Adapter<PackagesAdapter.viewHolder> {
    Context context;
    SessionManager sessionManager;
    ArrayList<Package> packagesList;
    OnItemClickListener listener;

    public PackagesAdapter(Context context, ArrayList<Package> packagesList, OnItemClickListener listener) {
        this.context = context;
        this.packagesList = packagesList;
        this.listener = listener;
    }


    public class viewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.item_package_cv_container)
        CardView container;
        @BindView(R.id.item_package_v_selectPackage)
        View selectPackage;
        @BindView(R.id.item_package_tv_packageName)
        TextView packageName;
        @BindView(R.id.item_package_tv_packagePrice)
        TextView packagePrice;
        @BindView(R.id.item_package_tv_credit)
        TextView credit;

        public viewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            sessionManager = new SessionManager(context);
        }
    }

    @NonNull
    @Override
    public PackagesAdapter.viewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View childView = LayoutInflater.from(context).inflate(R.layout.item_package, viewGroup, false);
        return new PackagesAdapter.viewHolder(childView);
    }

    @Override
    public void onBindViewHolder(@NonNull PackagesAdapter.viewHolder viewHolder, final int position) {
        Typeface enBold = Typeface.createFromAsset(context.getAssets(), Constants.MONTSERRAT_MEDIUM);
        viewHolder.packagePrice.setTypeface(enBold);
        viewHolder.container.setCardBackgroundColor(Color.parseColor(packagesList.get(position).colorCode));
        viewHolder.packageName.setText(packagesList.get(position).getName());
        viewHolder.packagePrice.setText(String.valueOf(packagesList.get(position).price));
        viewHolder.credit.setText(context.getString(R.string.credit) + ": " + packagesList.get(position).credit);
        viewHolder.selectPackage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.packageClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return packagesList.size();
    }

    public interface OnItemClickListener {
        void packageClick(int position);
    }
}
