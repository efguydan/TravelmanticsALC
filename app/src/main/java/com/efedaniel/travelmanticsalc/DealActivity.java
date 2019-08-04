package com.efedaniel.travelmanticsalc;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.cottacush.android.currencyedittext.CurrencyInputWatcher;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import static android.text.TextUtils.isEmpty;
import static com.efedaniel.travelmanticsalc.FirebaseUtil.isAdmin;
import static com.efedaniel.travelmanticsalc.FirebaseUtil.mDatabaseReference;

public class DealActivity extends AppCompatActivity {

    private static final int PICTURE_RESULT = 563;

    @BindView(R.id.title) TextInputEditText titleEditText;
    @BindView(R.id.price) TextInputEditText priceEditText;
    @BindView(R.id.description) TextInputEditText descriptionEditText;
    @BindView(R.id.image) ImageView dealImageView;
    @BindView(R.id.btnImage) Button uploadButton;

    private Unbinder unbinder;
    private TravelDeal mDeal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deal);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        unbinder = ButterKnife.bind(this);
        TravelDeal deal = (TravelDeal) getIntent().getSerializableExtra("deal_extra");
        mDeal = deal == null ? new TravelDeal() : deal;
        if (!isAdmin) uploadButton.setVisibility(View.GONE);
        initializeDeal();
    }

    private void initializeDeal() {
        priceEditText.addTextChangedListener(new CurrencyInputWatcher(priceEditText,"₦", Locale.getDefault()));
        titleEditText.setText(mDeal.getTitle());
        priceEditText.setText(mDeal.getPrice());
        descriptionEditText.setText(mDeal.getDescription());
        showImage(mDeal.getImageUrl());
    }

    @Override
    public boolean onSupportNavigateUp(){
        onBackPressed();
        return true;
    }

    @OnClick(R.id.btnImage)
    public void onuploadImageClicked() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/jpeg");
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        startActivityForResult(intent.createChooser(intent, "Insert Picture"), PICTURE_RESULT);
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
        if (mDeal.getId() != null) menu.findItem(R.id.save_menu).setTitle(R.string.update);
        switchEditTexts(isAdmin);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item ) {
        switch (item.getItemId()) {
            case R.id.save_menu:
                saveDeal();
                Toast.makeText(this, "Deal saved", Toast.LENGTH_LONG).show();
                finish();
                return true;
            case R.id.delete_menu:
                deleteDeal();
                finish();
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
        descriptionEditText.setFocusable(isAdmin);
        priceEditText.setFocusable(isAdmin);
        titleEditText.setFocusable(isAdmin);
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
        Toast.makeText(this, "Deal Deleted", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }
}
