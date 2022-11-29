package com.annmonstar.androidauctionapp.ui;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.pdf.PdfDocument;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.annmonstar.androidauctionapp.Adapter.MyCampaignAdapter;
import com.annmonstar.androidauctionapp.Adapter.PaymentsAdapter;
import com.annmonstar.androidauctionapp.Models.Products;
import com.annmonstar.androidauctionapp.R;
import com.annmonstar.androidauctionapp.ui.notifications.MyCampaigns;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PaymentsActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 200;
    PaymentsAdapter mAdapter;
    List<Products> mPayments = new ArrayList<>();
    private RecyclerView mRecyclerView;
    private Button saveReport;
    private ProgressBar progressBar;
    private TextView noPayments;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payments);

        getSupportActionBar().setTitle("Payments");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        saveReport = findViewById(R.id.save_report);
        mRecyclerView = (RecyclerView) findViewById(R.id.myCamp);
        progressBar = findViewById(R.id.progress_bar);
        noPayments = findViewById(R.id.no_payments);
        noPayments.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mAdapter = new PaymentsAdapter(this, mPayments);
        mRecyclerView.setAdapter(mAdapter);

        getPayments();
        if (!checkPermission()) {
            ActivityCompat.requestPermissions(this, new String[]{WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                requestPermission();
            }
            Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
        }

        saveReport.setOnClickListener(v -> generatePDF());

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    private void getPayments() {
        mPayments.clear();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("payments");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    reference.child(Objects.requireNonNull(dataSnapshot.getKey())).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            Products productModel = snapshot.getValue(Products.class);
                            assert productModel != null;
                            if (productModel.getUid().equalsIgnoreCase(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())) {
                                mPayments.add(productModel);
                                mAdapter.notifyDataSetChanged();
                            }
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
        if (mPayments.isEmpty()){
            noPayments.setVisibility(View.VISIBLE);
        }
        progressBar.setVisibility(View.INVISIBLE);
    }

    private void generatePDF() {
        PdfDocument document = new PdfDocument();
        ArrayList<CardView> cardViewList = mAdapter.getCardViewList();
        for (int i = 0; i < cardViewList.size(); i++) {
            // Iterate till the last of the array list and add each view individually to the document.
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(cardViewList.get(i).getMeasuredWidth(),
                    cardViewList.get(i).getMeasuredHeight(), i).create();

            // create a new page from the PageInfo
            PdfDocument.Page page = document.startPage(pageInfo);
            cardViewList.get(i).draw(page.getCanvas());
            // do final processing of the page
            document.finishPage(page);
        }

        // all created files will be saved at path /sdcard/PDFDemo_AndroidSRC/
        File outputFile = new File(Environment.getExternalStorageDirectory().getPath(), "Payments.pdf");

        try {
            outputFile.createNewFile();
            OutputStream out = new FileOutputStream(outputFile);
            document.writeTo(out);
            document.close();
            out.close();
            Toast.makeText(this, "PDF saved successfully", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private boolean checkPermission() {
        // checking of permissions.
        int permission1 = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);
        int permission2 = ContextCompat.checkSelfPermission(getApplicationContext(), READ_EXTERNAL_STORAGE);
        return permission1 == PackageManager.PERMISSION_GRANTED && permission2 == PackageManager.PERMISSION_GRANTED;
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    private void requestPermission() {
        // requesting permissions if not provided.
        if (!Environment.isExternalStorageManager()) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
            startActivity(intent);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0) {

                // after requesting permissions we are showing
                // users a toast message of permission granted.
                boolean writeStorage = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                boolean readStorage = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                if (writeStorage && readStorage) {
                    Toast.makeText(this, "Permission Granted..", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Permission Denied.", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }
    }

}