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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.annmonstar.androidauctionapp.ui.HomeActivity;
import com.annmonstar.androidauctionapp.Models.Products;
import com.annmonstar.androidauctionapp.R;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class AdapterClass extends RecyclerView.Adapter<AdapterClass.Viewholder> {
    List<Products>allProducts = new ArrayList<>();
    Context mContext;
    StorageReference mStorage;
    private  Uri imageUri;
    public AdapterClass(HomeActivity homeActivity, List<Products> allProducts) {
        this.mContext =  homeActivity;
        this.allProducts = allProducts;
    }

    @NonNull
    @Override
    public Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.product_lay,parent,false);
        return new Viewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Viewholder holder, int position) {

        Products products = allProducts.get(position);
        holder.pname.setText(products.getName());
        holder.pdesc.setText(products.getDescription());
        holder.pbid.setText("Ksh "+products.getBid());
        mStorage = FirebaseStorage.getInstance().getReference();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Products").child(products.getName());
        reference.child("Images").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!Objects.requireNonNull(snapshot.child("image0").getValue()).toString().equals("default")){
                    String url = Objects.requireNonNull(snapshot.child("image0").getValue()).toString();
                    mStorage
                            .child(url).getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    Log.d("ADAPTER", task.getResult()+"");
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

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, ProductInformationActivity.class);
                intent.putExtra("pname",products.getName());
                intent.putExtra("pdesc",products.getDescription());
                intent.putExtra("prate",products.getBid());
                intent.putExtra("uid",products.getUid());
                intent.putExtra("status",products.getStatus());
                intent.putExtra("image",imageUri.toString());
                mContext.startActivity(intent);

            }
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

    public static class Viewholder extends RecyclerView.ViewHolder{

        public ImageView imageView;
        public TextView pname,pdesc,pbid;
        public Viewholder(@NonNull View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.pimage);
            pname = (TextView) itemView.findViewById(R.id.pname);
            pdesc = (TextView) itemView.findViewById(R.id.pdesc);
            pbid = (TextView) itemView.findViewById(R.id.pbid);

        }
    }
}
