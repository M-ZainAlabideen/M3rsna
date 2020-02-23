package app.m3resna.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import app.m3resna.R;
import app.m3resna.webservices.models.PaymentHistory;
import butterknife.BindView;
import butterknife.ButterKnife;

public class PaymentHistoryAdapter extends RecyclerView.Adapter<PaymentHistoryAdapter.viewHolder> {
    Context context;
    ArrayList<PaymentHistory> paymentHistoryList;

    public PaymentHistoryAdapter(Context context, ArrayList<PaymentHistory> paymentHistoryList) {
        this.context = context;
        this.paymentHistoryList = paymentHistoryList;
    }

    public class viewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.item_payment_history_tv_date)
        TextView date;
        @BindView(R.id.item_payment_history_tv_price)
        TextView price;
        @BindView(R.id.item_payment_history_tv_description)
        TextView description;

        public viewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    @NonNull
    @Override
    public PaymentHistoryAdapter.viewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View childView = LayoutInflater.from(context).inflate(R.layout.item_payment_history, viewGroup, false);
        return new PaymentHistoryAdapter.viewHolder(childView);
    }

    @Override
    public void onBindViewHolder(@NonNull PaymentHistoryAdapter.viewHolder viewHolder, final int position) {
        viewHolder.date.setText(paymentHistoryList.get(position).getDate());
        viewHolder.price.setText(String.valueOf(paymentHistoryList.get(position).price)+ " " +context.getString(R.string.kuwaitCurrency));
        if (paymentHistoryList.get(position).isSubscription) {
            viewHolder.description.setText(context.getString(R.string.buy) + " " + paymentHistoryList.get(position).getDetails());
            viewHolder.price.setTextColor(Color.parseColor("#3DA412"));
            viewHolder.price.setCompoundDrawablesWithIntrinsicBounds(null, ContextCompat.getDrawable(context, R.mipmap.ic_arrow_green), null, null);
        } else {
            viewHolder.description.setText(context.getString(R.string.addAd) + " " + paymentHistoryList.get(position).getDetails());
            viewHolder.price.setTextColor(Color.parseColor("#FF0000"));
            viewHolder.price.setCompoundDrawablesWithIntrinsicBounds(null, ContextCompat.getDrawable(context, R.mipmap.ic_arrow_red), null, null);
        }

    }

    @Override
    public int getItemCount() {
        return paymentHistoryList.size();
    }

}

