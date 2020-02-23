package app.m3resna.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import app.m3resna.R;
import app.m3resna.webservices.models.Notification;
import butterknife.BindView;
import butterknife.ButterKnife;

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.viewHolder> {
    Context context;
    ArrayList<Notification> notificationList;
    NotificationsAdapter.OnItemClickListener listener;

    public NotificationsAdapter(Context context, ArrayList<Notification> notificationList, NotificationsAdapter.OnItemClickListener listener) {
        this.context = context;
        this.notificationList = notificationList;
        this.listener = listener;
    }

    public class viewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.item_notification_tv_date)
        TextView date;
        @BindView(R.id.item_notification_tv_title)
        TextView title;
        @BindView(R.id.item_notification_iv_line)
        ImageView line;
        @BindView(R.id.item_notification_v_details)
        View details;

        public viewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    @NonNull
    @Override
    public NotificationsAdapter.viewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View childView = LayoutInflater.from(context).inflate(R.layout.item_notification, viewGroup, false);
        return new NotificationsAdapter.viewHolder(childView);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationsAdapter.viewHolder viewHolder, final int position) {
        viewHolder.title.setText(notificationList.get(position).getMessage());
        viewHolder.date.setText(notificationList.get(position).getDate());
        if (notificationList.get(position).isRead) {
            viewHolder.line.setImageResource(R.mipmap.line_gray);
            viewHolder.date.setTextColor(context.getResources().getColor(R.color.lightGray));
            viewHolder.title.setTextColor(context.getResources().getColor(R.color.gray));
        } else {
            viewHolder.line.setImageResource(R.mipmap.line_red);
            viewHolder.date.setTextColor(context.getResources().getColor(R.color.gray));
            viewHolder.title.setTextColor(context.getResources().getColor(R.color.black));
        }
        viewHolder.details.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.itemClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    public interface OnItemClickListener {
        public void itemClick(int position);
    }
}
