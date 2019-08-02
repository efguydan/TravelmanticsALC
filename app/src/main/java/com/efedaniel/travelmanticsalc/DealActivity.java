package com.efedaniel.travelmanticsalc;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import static android.text.TextUtils.isEmpty;
import static com.efedaniel.travelmanticsalc.FirebaseUtil.isAdmin;
import static com.efedaniel.travelmanticsalc.FirebaseUtil.mDatabaseReference;

public class DealActivity extends AppCompatActivity {

    private static final int PICTURE_RESULT = 563;

    private EditText titleEditText;
    private EditText priceEditText;
    private EditText descriptionEditText;
    private ImageView dealImageView;
    private Button button;

    private TravelDeal mDeal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deal);
        TravelDeal deal = (TravelDeal) getIntent().getSerializableExtra("deal_extra");
        mDeal = deal == null ? new TravelDeal() : deal;
        initializeTextViews();
        dealImageView = findViewById(R.id.image);
        button = findViewById(R.id.btnImage);
        if (!isAdmin) {
            button.setVisibility(View.GONE);
        }
        button.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/jpeg");
            intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
            startActivityForResult(intent.createChooser(intent, "Insert Picture"), PICTURE_RESULT);
        });
        showImage(mDeal.getImageUrl());
    }

    private void initializeTextViews() {
        titleEditText = findViewById(R.id.title);
        priceEditText = findViewById(R.id.price);
        descriptionEditText = findViewById(R.id.description);
        titleEditText.setText(mDeal.getTitle());
        priceEditText.setText(mDeal.getPrice());
        descriptionEditText.setText(mDeal.getDescription());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICTURE_RESULT && resultCode == RESULT_OK) {
            Uri imageUri = data.getData();
            StorageReference ref = FirebaseUtil.mStorageReference.child(imageUri.getLastPathSegment());
            ref.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
                ref.getDownloadUrl().addOnSuccessListener(uri -> {
                    mDeal.setImageUrl(uri.toString());
                    showImage(mDeal.getImageUrl());
                });
                mDeal.setImageName(taskSnapshot.getStorage().getPath());
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.save_menu, menu);
        menu.findItem(R.id.delete_menu).setVisible(isAdmin);
        menu.findItem(R.id.save_menu).setVisible(isAdmin);
        switchEditTexts(isAdmin);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item ) {
        switch (item.getItemId()) {
            case R.id.save_menu:
                saveDeal();
                Toast.makeText(this, "Deal saved", Toast.LENGTH_LONG).show();
                backToList();
                return true;
            case R.id.delete_menu:
                deleteDeal();
                Toast.makeText(this, "Deal Deleted", Toast.LENGTH_LONG).show();
                backToList();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showImage(String url) {
        if (isEmpty(url)) return;
        int width = Resources.getSystem().getDisplayMetrics().widthPixels;
        Picasso.get()
                .load(url)
                .resize(width, width*2/3)
                .centerCrop()
                .into(dealImageView);
    }

    private void switchEditTexts(boolean isAdmin) {
        priceEditText.setEnabled(isAdmin);
        descriptionEditText.setEnabled(isAdmin);
        titleEditText.setEnabled(isAdmin);
    }

    private void saveDeal() {
        mDeal.setTitle(titleEditText.getText().toString());
        mDeal.setDescription(descriptionEditText.getText().toString());
        mDeal.setPrice(priceEditText.getText().toString());
        if(mDeal.getId() == null) {
            mDatabaseReference.push().setValue(mDeal);
        } else {
            mDatabaseReference.child(mDeal.getId()).setValue(mDeal);
        }

        cleanEditTexts(titleEditText, priceEditText, descriptionEditText);
    }

    private void deleteDeal() {
        if (mDeal.getId() == null) {
            Toast.makeText(this, "Please save the deal before deleting", Toast.LENGTH_SHORT).show();
            return;
        }
        mDatabaseReference.child(mDeal.getId()).removeValue();
        if (!isEmpty(mDeal.getImageName())) {
            FirebaseUtil.mFirebaseStorage.getReference().child(mDeal.getImageName()).delete()
                    .addOnSuccessListener(aVoid -> Log.d("Delete Image", "Image Successfully Deleted"))
                    .addOnFailureListener(e -> Log.d("Delete Image", e.getMessage()));
        }
    }

    private void backToList() {
        Intent intent = new Intent(this, ListActivity.class);
        startActivity(intent);
        finish();
    }

    private void cleanEditTexts(EditText... editTexts) {
        for (EditText editText : editTexts) editText.setText("");
        titleEditText.requestFocus();
    }
}
