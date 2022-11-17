package com.annmonstar.androidauctionapp.Adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.annmonstar.androidauctionapp.Models.Products;
import com.annmonstar.androidauctionapp.R;
import com.annmonstar.androidauctionapp.ui.ProductInformationActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;
import java.util.Objects;


public class MyCampaignAdapter extends RecyclerView.Adapter<MyCampaignAdapter.Viewholder> {
    Context context;
    List<Products> myProducts;
    StorageReference mStorage;

    public MyCampaignAdapter(Context context, List<Products> myProducts) {
        this.context = context;
        this.myProducts = myProducts;
    }

    @NonNull
    @Override
    public Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.my_camp_layout, parent, false);
        return new Viewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Viewholder holder, int position) {

        Products productModel = myProducts.get(position);
        holder.pName.setText(productModel.getName());
        holder.pdesc.setText("Ksh " + productModel.getBid());
        getPData(productModel.getMainImageUrl(), holder.pImage);
        mStorage = FirebaseStorage.getInstance().getReference();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Products").child(productModel.getName());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!(myProducts.get(holder.getLayoutPosition()).getWinner().equals("default"))) {
                    String uid = Objects.requireNonNull(myProducts.get(holder.getLayoutPosition()).getWinner());
                    String amount = productModel.getBid();

                    DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
                    reference1.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String email = Objects.requireNonNull(snapshot.child("email").getValue()).toString();
                            String name = Objects.requireNonNull(snapshot.child("name").getValue()).toString();
                            holder.bidstatus.setText("Product sold to " + name + " at Ksh " + amount + "\nContact them at - " + email);

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                } else {
                    holder.bidstatus.setText("Bidding is over.\nNo one bid for your product");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ProductInformationActivity.class);
                intent.putExtra("pname", productModel.getName());
                intent.putExtra("pdesc", productModel.getDescription());
                intent.putExtra("prate", productModel.getBid());
                intent.putExtra("uid", productModel.getUid());
                intent.putExtra("status", productModel.getStatus());
                intent.putExtra("mine", "mine");
                context.startActivity(intent);
            }
        });

        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeAt(holder.getLayoutPosition());
                DatabaseReference dR = FirebaseDatabase.getInstance().getReference("Products").child(productModel.getName());
                dR.removeValue();
            }
        });

    }

    public void removeAt(int position) {

        myProducts.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, myProducts.size());
    }

    @Override
    public int getItemCount() {
        return myProducts.size();
    }

    private void deleteProduct() {

    }


    private void getPData(String url, ImageView holder) {
        if (url != null) {
            mStorage
                    .child(url).getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            Log.d("ADAPTER", task.getResult() + "");
                            Glide.with(Objects.requireNonNull(context))
                                    .load(task.getResult())
                                    .diskCacheStrategy(DiskCacheStrategy.DATA)
                                    .placeholder(R.drawable.default_send_image)
                                    .into(holder);

                        }
                    });
        }
    }

    public static class Viewholder extends RecyclerView.ViewHolder {

        private final ImageView pImage;
        private final ImageView delete;
        private final TextView pName;
        private final TextView pdesc;
        private final TextView bidstatus;

        public Viewholder(@NonNull View itemView) {
            super(itemView);
            pName = itemView.findViewById(R.id.pname);
            pdesc = itemView.findViewById(R.id.pdesc);
            pImage = itemView.findViewById(R.id.pimage);
            delete = itemView.findViewById(R.id.delete);
            bidstatus = itemView.findViewById(R.id.bidStatus);
        }
    }
}
