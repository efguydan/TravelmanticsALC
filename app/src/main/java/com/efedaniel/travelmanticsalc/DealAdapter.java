package com.efedaniel.travelmanticsalc;

import android.content.Intent;
import android.content.res.Resources;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import static android.text.TextUtils.isEmpty;

public class DealAdapter extends RecyclerView.Adapter<DealAdapter.DealViewHolder> {

    private ArrayList<TravelDeal> mDeals;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;

    public DealAdapter() {
        mFirebaseDatabase = FirebaseUtil.mFirebaseDatabase;
        mDatabaseReference = FirebaseUtil.mDatabaseReference;
        mDeals = FirebaseUtil.mDeals;
        mDatabaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                TravelDeal deal = dataSnapshot.getValue(TravelDeal.class);
                Log.d("The Title is", deal.getTitle());
                deal.setId(dataSnapshot.getKey());
                mDeals.add(deal);
                notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @NonNull
    @Override
    public DealViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new DealViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.single_deal, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull DealViewHolder holder, int position) {
        holder.bind(mDeals.get(position));
    }

    @Override
    public int getItemCount() {
        return mDeals.size();
    }

    public class DealViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView titleTextView;
        TextView descriptionTextView;
        TextView priceTextView;
        ImageView dealImageView;

        public DealViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.title);
            descriptionTextView = itemView.findViewById(R.id.description);
            priceTextView = itemView.findViewById(R.id.price);
            dealImageView = itemView.findViewById(R.id.deal_image);
            itemView.setOnClickListener(this);
        }

        public void bind(TravelDeal deal) {
            titleTextView.setText(deal.getTitle());
            descriptionTextView.setText(deal.getDescription());
            priceTextView.setText(deal.getPrice());
            showImage(deal.getImageUrl());
        }

        private void showImage(String url) {
            if (isEmpty(url)) return;
            Picasso.get()
                    .load(url)
                    .resize(100, 100)
                    .centerCrop()
                    .into(dealImageView);
        }

        @Override
        public void onClick(View view) {
            Intent intent = new Intent(view.getContext(), DealActivity.class);
            intent.putExtra("deal_extra", mDeals.get(getAdapterPosition()));
            view.getContext().startActivity(intent);
        }
    }
}
