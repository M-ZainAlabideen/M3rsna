package app.m3resna.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

import app.m3resna.R;
import app.m3resna.classes.FixControl;
import app.m3resna.webservices.models.PaymentMethod;
import butterknife.BindView;
import butterknife.ButterKnife;

public class PaymentMethodsAdapter extends RecyclerView.Adapter<PaymentMethodsAdapter.viewHolder> {
    Context context;
    List<PaymentMethod> paymentMethodList;
    public static int selectedPosition = 0;

    public PaymentMethodsAdapter(Context context, List<PaymentMethod> paymentMethodList) {
        this.context = context;
        this.paymentMethodList = paymentMethodList;
    }

    public class viewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.item_payment_method_iv_methodImage)
        ImageView methodImage;
        @BindView(R.id.item_payment_method_rb_methodCheck)
        RadioButton methodCheck;
        @BindView(R.id.item_payment_method_tv_methodName)
        TextView methodName;
        @BindView(R.id.item_payment_method_tv_methodPrice)
        TextView methodPrice;
        @BindView(R.id.item_payment_method_v_select)
        View select;

        public viewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }


    @NonNull
    @Override
    public PaymentMethodsAdapter.viewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View childView = LayoutInflater.from(context).inflate(R.layout.item_payment_method, viewGroup, false);
        return new PaymentMethodsAdapter.viewHolder(childView);
    }

    @Override
    public void onBindViewHolder(@NonNull final PaymentMethodsAdapter.viewHolder viewHolder, final int position) {
        viewHolder.methodPrice.setVisibility(View.GONE);
        viewHolder.methodName.setText(paymentMethodList.get(position).getName());
        int Width = FixControl.getImageWidth(context, R.mipmap.payment_card);
        int Height = FixControl.getImageHeight(context, R.mipmap.payment_card);
        viewHolder.methodImage.getLayoutParams().height = Height;
        viewHolder.methodImage.getLayoutParams().width = Width;
        if (paymentMethodList.get(position).imageUrl != null
                && !paymentMethodList.get(position).imageUrl.matches("")) {
            setImage(paymentMethodList.get(position).imageUrl, viewHolder.methodImage);

            if (position == selectedPosition)
                viewHolder.methodCheck.setChecked(true);
            else
                viewHolder.methodCheck.setChecked(false);
        }
    }

    @Override
    public int getItemCount() {
        return paymentMethodList.size();
    }

    private void setImage(String imagePath, ImageView image) {
        if (imagePath != null) {
            try {
                int Width = FixControl.getImageWidth(context, R.mipmap.payment_card);
                int Height = FixControl.getImageHeight(context, R.mipmap.payment_card);
                image.getLayoutParams().height = Height;
                image.getLayoutParams().width = Width;
                Glide.with(context.getApplicationContext()).load(imagePath)
                        .apply(new RequestOptions().placeholder(R.mipmap.payment_card))
                        .into(image);

            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }
    }
}

