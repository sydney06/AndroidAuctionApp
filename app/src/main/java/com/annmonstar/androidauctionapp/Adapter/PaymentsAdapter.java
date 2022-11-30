package com.annmonstar.androidauctionapp.Adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.annmonstar.androidauctionapp.Models.Products;
import com.annmonstar.androidauctionapp.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PaymentsAdapter extends RecyclerView.Adapter<PaymentsAdapter.Viewholder> {
    private static ArrayList<CardView> cardViewArrayList = new ArrayList<>();
    Context context;
    List<Products> myProducts;
    StorageReference mStorage;

    public PaymentsAdapter(Context context, List<Products> myProducts) {
        this.context = context;
        this.myProducts = myProducts;
    }

    private static void addCardView(CardView cardView) {
        cardViewArrayList.add(cardView);
    }

    public static ArrayList<CardView> getCardViewList() {
        return cardViewArrayList;
    }

    @NonNull
    @Override
    public PaymentsAdapter.Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.payments_item, parent, false);
        return new PaymentsAdapter.Viewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PaymentsAdapter.Viewholder holder, int position) {

        Products productModel = myProducts.get(position);
        String message = "You paid Ksh." + productModel.getBid() + " for " + productModel.getName();
        holder.notificationMessage.setText(message);
        holder.notificationTimestamp.setText(productModel.getTimestamp());
        getPData(productModel.getMainImageUrl(), holder.notificationIcon);
        mStorage = FirebaseStorage.getInstance().getReference();
        addCardView(holder.cardView);

    }

    @Override
    public int getItemCount() {
        return myProducts.size();
    }

    private void getPData(String url, ImageView holder) {
        if (url != null) {
            mStorage
                    .child(url).getDownloadUrl().addOnCompleteListener(task -> {
                        Log.d("ADAPTER", task.getResult() + "");
                        Glide.with(Objects.requireNonNull(context))
                                .load(task.getResult())
                                .diskCacheStrategy(DiskCacheStrategy.DATA)
                                .placeholder(R.drawable.default_send_image)
                                .into(holder);

                    });
        }
    }

    public static class Viewholder extends RecyclerView.ViewHolder {

        private final ImageView notificationIcon;
        private final TextView notificationMessage;
        private final TextView notificationTimestamp;
        private final CardView cardView;

        public Viewholder(@NonNull View itemView) {
            super(itemView);
            notificationMessage = itemView.findViewById(R.id.notification_message);
            notificationTimestamp = itemView.findViewById(R.id.notification_timestamp);
            notificationIcon = itemView.findViewById(R.id.notification_icon);
            cardView = itemView.findViewById(R.id.card_view);
        }
    }
}