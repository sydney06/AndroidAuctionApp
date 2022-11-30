package com.annmonstar.androidauctionapp.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.annmonstar.androidauctionapp.Models.Products;
import com.annmonstar.androidauctionapp.R;
import com.annmonstar.androidauctionapp.ui.authentication.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AdminActivity extends AppCompatActivity {
    private AdminAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private List<Products> allProducts = new ArrayList<>();
    private ProgressBar progressBar;
    private TextView noPayments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        Objects.requireNonNull(getSupportActionBar()).setTitle("Admin");

        mRecyclerView = findViewById(R.id.myCamp);
        progressBar = findViewById(R.id.progress_bar);
        noPayments = findViewById(R.id.no_payments);
        noPayments.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(linearLayoutManager);

        mAdapter = new AdminAdapter(this, allProducts);
        mRecyclerView.setAdapter(mAdapter);
        getAllProducts();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.admin_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.logout) {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(AdminActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void getAllProducts() {
        allProducts.clear();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Products");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Log.d("GGGG", dataSnapshot + "");
                    ref.child(Objects.requireNonNull(dataSnapshot.getKey())).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            Products products = dataSnapshot.getValue(Products.class);
                            if (products.getApprove().equals("Pending")) {
                                allProducts.add(products);
                                mAdapter.notifyDataSetChanged();
                            }
                            if (allProducts.isEmpty()) {
                                noPayments.setVisibility(View.VISIBLE);
                            }
                            progressBar.setVisibility(View.INVISIBLE);
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                        }
                    });
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}