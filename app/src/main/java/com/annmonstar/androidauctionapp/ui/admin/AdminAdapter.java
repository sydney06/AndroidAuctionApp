package com.annmonstar.androidauctionapp.ui.admin;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.annmonstar.androidauctionapp.Models.Products;
import com.annmonstar.androidauctionapp.R;
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

public class AdminAdapter extends RecyclerView.Adapter<AdminAdapter.Viewholder> {
    List<Products> allProducts;
    Context mContext;
    StorageReference mStorage;
    private Uri imageUri;

    public AdminAdapter(Context context, List<Products> allProducts) {
        this.mContext = context;
        this.allProducts = allProducts;
    }

    @NonNull
    @Override
    public Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.admin_layout_item, parent, false);
        return new Viewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Viewholder holder, int position) {

        Products products = allProducts.get(position);
        holder.pname.setText(products.getName());
        holder.pdesc.setText(products.getDescription());
        holder.pbid.setText(String.valueOf("Ksh " + products.getBid()));
        mStorage = FirebaseStorage.getInstance().getReference();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Products").child(products.getName());
        reference.child("Images").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!Objects.requireNonNull(snapshot.child("image0").getValue()).toString().equals("default")) {
                    String url = Objects.requireNonNull(snapshot.child("image0").getValue()).toString();
                    mStorage
                            .child(url).getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    Log.d("ADAPTER", task.getResult() + "");
                                    imageUri = task.getResult();
                                    Glide
                                            .with(mContext)
                                            .load(imageUri)
                                            .diskCacheStrategy(DiskCacheStrategy.DATA)
                                            .centerCrop()
                                            .into(holder.imageView);
                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        holder.approve.setOnClickListener(v -> {
            FirebaseDatabase.getInstance().getReference().child("Products")
                    .child(allProducts.get(holder.getLayoutPosition()).getName()).child("approve").setValue("Approved");
        });


    }

    @Override
    public int getItemCount() {
        return allProducts.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public static class Viewholder extends RecyclerView.ViewHolder {
        public ImageView imageView;
        public Button approve;
        public TextView pname, pdesc, pbid;

        public Viewholder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.notification_icon);
            approve = itemView.findViewById(R.id.approve);
            pname = itemView.findViewById(R.id.pname);
            pdesc = itemView.findViewById(R.id.pdesc);
            pbid = itemView.findViewById(R.id.pbid);

        }
    }
}
