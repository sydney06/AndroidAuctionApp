package com.annmonstar.androidauctionapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.annmonstar.androidauctionapp.Adapter.AdapterClass;
import com.annmonstar.androidauctionapp.Models.Products;
import com.annmonstar.androidauctionapp.R;
import com.annmonstar.androidauctionapp.ui.notifications.MyCampaigns;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class HomeActivity extends AppCompatActivity {
    private static final int TOTAL_ITEMS_TO_LOAD = 2000;
    int max = -1;
    String maxUserId = null;
    FloatingActionButton addProduct;
    RecyclerView mRecyclerView;
    AdapterClass mAdapter;
    BottomAppBar bottomAppBar;
    ImageView refresh;
    SwipeRefreshLayout mSwipeRefreshLayout;
    List<Products> allProducts = new ArrayList<>();
    private int itemPos = 0;
    private int mCurrentPage = 1;
    private String mLastKey = "";
    private String mPrevKey = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        addProduct = findViewById(R.id.addProduct);
        mRecyclerView = findViewById(R.id.productList);


        refresh = findViewById(R.id.refresh);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refreshLayout);
        bottomAppBar = (BottomAppBar) findViewById(R.id.bottomAppBar);
        //setSupportActionBar(bottomAppBar);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        mAdapter = new AdapterClass(HomeActivity.this, allProducts);
        mRecyclerView.setAdapter(mAdapter);


        getAllProducts();

        addProduct.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, addProductforBid.class);
            startActivity(intent);
        });

        refresh.setOnClickListener(v -> {
            getAllProducts();
            mSwipeRefreshLayout.setRefreshing(true);
        });
        notifySeller();

    }


    public void getAllProducts() {
        allProducts.clear();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Products");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    ref.child(Objects.requireNonNull(dataSnapshot.getKey())).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            Products products = dataSnapshot.getValue(Products.class);
                            allProducts.add(products);
                            mAdapter.notifyDataSetChanged();
                            mSwipeRefreshLayout.setRefreshing(true);
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

    private void notifySeller() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Products");
        ref.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                for (DataSnapshot dataSnapshot : task.getResult().getChildren()) {
                    Products products = dataSnapshot.getValue(Products.class);

                    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                    Date dn = new Date();
                    String formatted = formatter.format(dn);

                    try {
                        Date today = formatter.parse(formatted);
                        assert products != null;
                        Date expires = formatter.parse(Objects.requireNonNull(products.getTimestamp()));
                        assert today != null;
                        if (today.after(expires) && products.getStatus().equals("running")) {
                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users")
                                    .child(products.getUid()).child("products");
                            String product_id = reference.push().getKey();
                            assert product_id != null;
                            reference.child(product_id).setValue(products);
                            FirebaseDatabase.getInstance().getReference().child("Products")
                                    .child(products.getName()).child("status").setValue("Expired");

                        }
                        if (products.getStatus().equals("Paid")) {
                            DatabaseReference dR = FirebaseDatabase.getInstance().getReference("Products").child(products.getName());
                            dR.removeValue();
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.bottom_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Intent intent;

        if (item.getItemId() == R.id.settings) {
            intent = new Intent(HomeActivity.this, SettingsActivity.class);
            startActivity(intent);
        }
        if (item.getItemId() == R.id.mycamp) {
            intent = new Intent(HomeActivity.this, MyCampaigns.class);
            startActivity(intent);
        }
        if (item.getItemId() == R.id.bids) {
            intent = new Intent(HomeActivity.this, BidsActivity.class);
            startActivity(intent);
        }
        if (item.getItemId() == R.id.payments) {
            intent = new Intent(HomeActivity.this, PaymentsActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseDatabase.getInstance().goOnline();
        if (currentUser == null) {
            Intent startIntent = new Intent(this, MainActivity.class);
            startActivity(startIntent);
            finish();

        } else {

            final DatabaseReference mUserDatabase = FirebaseDatabase.getInstance()
                    .getReference().child("Users").child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());

            mUserDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot != null) {
                        mUserDatabase.child("online").onDisconnect().setValue(ServerValue.TIMESTAMP);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }
}