package com.annmonstar.androidauctionapp.ui;

import static com.annmonstar.androidauctionapp.ui.utils.Constants.BUSINESS_SHORT_CODE;
import static com.annmonstar.androidauctionapp.ui.utils.Constants.CALLBACKURL;
import static com.annmonstar.androidauctionapp.ui.utils.Constants.PARTYB;
import static com.annmonstar.androidauctionapp.ui.utils.Constants.PASSKEY;
import static com.annmonstar.androidauctionapp.ui.utils.Constants.TRANSACTION_TYPE;
import static com.annmonstar.androidauctionapp.ui.utils.STKPushUtils.getPassword;
import static com.annmonstar.androidauctionapp.ui.utils.STKPushUtils.getTimestamp;
import static com.annmonstar.androidauctionapp.ui.utils.STKPushUtils.sanitizePhoneNumber;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.annmonstar.androidauctionapp.Adapter.LiveBidding;
import com.annmonstar.androidauctionapp.Adapter.pInfo_AllImageView;
import com.annmonstar.androidauctionapp.Models.AccessToken;
import com.annmonstar.androidauctionapp.Models.BiddingModal;
import com.annmonstar.androidauctionapp.Models.STKPush;
import com.annmonstar.androidauctionapp.R;
import com.annmonstar.androidauctionapp.ui.utils.DarajaApiClient;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductInformationActivity extends AppCompatActivity {
    private final List<String> imageList = new ArrayList<>();
    private final List<BiddingModal> biddingList = new ArrayList<>();
    StorageReference mStorage;
    private String name;
    private String bid;
    private String uid;
    private String status;
    private pInfo_AllImageView mAdapter;
    private TextView pname, pdesc, sellername, biddingStatus;
    private ImageView imageView;
    private TextView rate, sellerbidViewname, sellercity;
    private CircleImageView sellerImage;
    private DarajaApiClient mApiClient;
    private ProgressDialog mProgressDialog;
    private RecyclerView bidView;
    private EditText bidtext;
    private LiveBidding mAdapter2;
    private Button bidBtn;
    private String imageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_information);
        RecyclerView allImageView = findViewById(R.id.allImageView);

        sellername = findViewById(R.id.SellerName);
        sellercity = findViewById(R.id.SellerCity);
        sellerImage = findViewById(R.id.sellerProfile);
        TextView title = findViewById(R.id.title);
        TextView sold = findViewById(R.id.sold);
        LinearLayout biddingLayout = findViewById(R.id.bidLayout);

        bidBtn = findViewById(R.id.bidbtn);
        bidtext = findViewById(R.id.bidtxt);
        biddingStatus = findViewById(R.id.status_value);


        bidView = findViewById(R.id.bidView);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        imageView = findViewById(R.id.pImage);
        rate = findViewById(R.id.pbid);
        pname = findViewById(R.id.pname);
        pdesc = findViewById(R.id.pdesc);

        Button mMakePayment = findViewById(R.id.make_payment);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        allImageView.setLayoutManager(linearLayoutManager);
        mAdapter = new pInfo_AllImageView(ProductInformationActivity.this, imageList, imageView);
        allImageView.setAdapter(mAdapter);

        mStorage = FirebaseStorage.getInstance().getReference();

        LinearLayoutManager linearLayoutManager2 = new LinearLayoutManager(this);


        bidView.setLayoutManager(linearLayoutManager2);
        mAdapter2 = new LiveBidding(ProductInformationActivity.this, biddingList);
        bidView.setAdapter(mAdapter2);


        getPreInfo();
        getSellerInfo();
        getData();
        bidStart();
        getBidding();
        if (status.equals("Expired")) {
            biddingStatus.setText("Expired");
        }else{
            biddingStatus.setText("Running");
        }

        if (status.equalsIgnoreCase("stop")) {
            biddingLayout.setVisibility(View.GONE);
            title.setVisibility(View.GONE);
            sold.setVisibility(View.VISIBLE);
            sold.setText("Bidding is over for this product");
        }
        mProgressDialog = new ProgressDialog(this);
        mApiClient = new DarajaApiClient();
        mApiClient.setIsDebug(true);

        getAccessToken();
        mMakePayment.setOnClickListener(v -> {
            if (status.equals("Expired")) {
                makePayment();
            } else {
                Toast.makeText(ProductInformationActivity.this, "Bidding still in progress.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getBidding() {
        biddingList.clear();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Products")
                .child(name).child("bidding");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        reference.child(Objects.requireNonNull(dataSnapshot.getKey())).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                BiddingModal biddingModal = snapshot.getValue(BiddingModal.class);
                                biddingList.add(biddingModal);
                                bidView.scrollToPosition(biddingList.size() - 1);
                                mAdapter2.notifyDataSetChanged();

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void bidStart() {
        bidBtn.setOnClickListener(v -> {
            if (!uid.equalsIgnoreCase(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                String b = bidtext.getText().toString().trim();
                if (!b.isEmpty()) {
                    if (Integer.parseInt(b) > Integer.parseInt(bid)) {
                        bidtext.setText("");
                        String uid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Products")
                                .child(name).child("bidding");
                        String puch_id = reference.push().getKey();
                        HashMap<Object, String> hashMap = new HashMap<>();
                        hashMap.put("bid", b);
                        hashMap.put("uid", uid);
                        assert puch_id != null;
                        reference.child(puch_id).setValue(hashMap);
                        FirebaseDatabase.getInstance().getReference().child("Products")
                                .child(name).child("bid").setValue(b);
                        FirebaseDatabase.getInstance().getReference().child("Products")
                                .child(name).child("winner").setValue(uid);
                        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                        Date date = new Date();
                        String todaysDate = formatter.format(date);
                        BiddingModal biddingModal = new BiddingModal(b, uid, name, todaysDate, imageUrl);
                        saveBid(biddingModal);

                    } else {
                        Toast.makeText(ProductInformationActivity.this, "Bidding amount must be greater than " + bid, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ProductInformationActivity.this, "Please enter a bidding amount", Toast.LENGTH_SHORT).show();
                }

            } else {
                Toast.makeText(ProductInformationActivity.this, "You can't bid in your own product", Toast.LENGTH_SHORT).show();
            }

        });
    }

    private void getSellerInfo() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = snapshot.child("name").getValue().toString();
                String profile = snapshot.child("image").getValue().toString();
                String city = snapshot.child("city").getValue().toString();

                sellername.setText(name);
                sellercity.setText("From " + city);

                Glide.with(Objects.requireNonNull(ProductInformationActivity.this))
                        .load(profile)
                        .diskCacheStrategy(DiskCacheStrategy.DATA)
                        .placeholder(R.drawable.default_avatar)
                        .into(sellerImage);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void getData() {
        imageList.clear();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Products").child(name).child("Images");

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (int i = 0; i < snapshot.getChildrenCount(); i++) {
                    mStorage
                            .child(Objects.requireNonNull(snapshot.child("image" + i).getValue()).toString()).getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    imageList.add(task.getResult().toString());
                                }
                            });

                }

                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getPreInfo() {
        name = getIntent().getStringExtra("pname");
        String desc = getIntent().getStringExtra("pdesc");
        bid = getIntent().getStringExtra("prate");
        uid = getIntent().getStringExtra("uid");
        status = getIntent().getStringExtra("status");
        String mine = getIntent().getStringExtra("mine");

        // bidNow.setText("View Bidding of Your Product");

        pname.setText(name);
        pdesc.setText(desc);
        rate.setText("Bidding Starts at Ksh " + bid);
        FirebaseDatabase.getInstance().getReference().child("Products").child(name).child("Images").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!Objects.requireNonNull(snapshot.child("image0").getValue()).toString().equals("default")) {
                    imageUrl = Objects.requireNonNull(snapshot.child("image0").getValue()).toString();
                    mStorage
                            .child(imageUrl).getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    Log.d("ADAPTER", task.getResult() + "");
                                    Glide
                                            .with(ProductInformationActivity.this)
                                            .load(task.getResult())
                                            .diskCacheStrategy(DiskCacheStrategy.DATA)
                                            .centerCrop()
                                            .into(imageView);
                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }

        });


    }

    private void makePayment() {
        FirebaseDatabase.getInstance().getReference().child("Products").child(name).child("winner").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                } else {
                    if (task.getResult().getValue().toString().equals(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())) {
                        FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("phoneNumber").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DataSnapshot> task) {
                                if (!task.isSuccessful()) {
                                    Log.e("firebase", "Error getting data", task.getException());
                                } else {
                                    if (Integer.parseInt(bid) < 150000) {
                                        performSTKPush(task.getResult().getValue().toString(), Integer.parseInt(bid));
                                    } else {
                                        Toast.makeText(ProductInformationActivity.this, "The amount is past M-Pesa Limit", Toast.LENGTH_LONG).show();
                                    }
                                }
                            }
                        });
                    } else {
                        Toast.makeText(ProductInformationActivity.this, "You're not the winner of the bid", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }

    private void saveBid(BiddingModal bid) {
        Random rand = new Random();
        String bidId = String.format("%06d", rand.nextInt(999999));
        FirebaseDatabase.getInstance().getReference().child("Users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("bids").child(bidId).setValue(bid);

    }

    private void savePayment(BiddingModal bid) {
        Random rand = new Random();
        String bidId = String.format("%06d", rand.nextInt(999999));
        FirebaseDatabase.getInstance().getReference().child("Users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("payments").child(bidId).setValue(bid);

    }


    private void getAccessToken() {
        mApiClient.setGetAccessToken(true);
        mApiClient.mpesaService().getAccessToken().enqueue(new Callback<AccessToken>() {
            @Override
            public void onResponse(@NonNull Call<AccessToken> call, @NonNull Response<AccessToken> response) {
                if (response.isSuccessful()) {
                    assert response.body() != null;
                    mApiClient.setAuthToken(response.body().accessToken);
                }
            }

            @Override
            public void onFailure(@NonNull Call<AccessToken> call, @NonNull Throwable t) {
                Toast.makeText(ProductInformationActivity.this, "Transaction failed, try again", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void performSTKPush(String phone_number, int amount) {
        mProgressDialog.setMessage("Processing your request");
        mProgressDialog.setTitle("Please Wait...");
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.show();
        String timestamp = getTimestamp();
        STKPush stkPush = new STKPush(
                BUSINESS_SHORT_CODE,
                getPassword(BUSINESS_SHORT_CODE, PASSKEY, timestamp),
                timestamp,
                TRANSACTION_TYPE,
                String.valueOf(amount),
                sanitizePhoneNumber(phone_number),
                PARTYB,
                sanitizePhoneNumber(phone_number),
                CALLBACKURL,
                "Annmonstar  Inc.", //Account reference
                "Live Auction payment"  //Transaction description
        );


        mApiClient.setGetAccessToken(false);
        //Sending the data to the Mpesa API, remember to remove the logging when in production.

        mApiClient.mpesaService().sendPush(stkPush).enqueue(new Callback<STKPush>() {
            @Override
            public void onResponse(@NonNull Call<STKPush> call, @NonNull Response<STKPush> response) {
                mProgressDialog.dismiss();
                try {
                    if (response.isSuccessful()) {
                        Intent intent = new Intent(ProductInformationActivity.this, HomeActivity.class);
                        startActivity(intent);
                        FirebaseDatabase.getInstance().getReference().child("Products")
                                .child(name).child("status").setValue("Paid");
                        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                        Date date = new Date();
                        String todaysDate = formatter.format(date);
                        BiddingModal biddingModal = new BiddingModal(amount + "", uid, name, todaysDate, imageUrl);
                        savePayment(biddingModal);
                        finish();
                    } else {
                        Toast.makeText(getApplicationContext(), response.message(), Toast.LENGTH_SHORT).show();
                        Log.d("TAGElse", response.toString());
                    }
                } catch (Exception e) {
                    Log.d("TAGcat", response.toString());
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(@NonNull Call<STKPush> call, @NonNull Throwable t) {
                Log.d("TAGTHRO", t.toString());
                mProgressDialog.dismiss();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}