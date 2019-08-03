package com.efedaniel.travelmanticsalc;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FirebaseUtil {

    private static FirebaseUtil firebaseUtil;
    private static ListActivity caller;
    public static FirebaseDatabase mFirebaseDatabase;
    public static DatabaseReference mDatabaseReference;
    public static FirebaseAuth mFirebaseAuth;
    public static FirebaseStorage mFirebaseStorage;
    public static StorageReference mStorageReference;
    public static FirebaseAuth.AuthStateListener mAuthStateListener;
    public static ArrayList<TravelDeal> mDeals;
    public static final int FirebaseUISignInKey = 100;
    public static boolean isAdmin;

    private FirebaseUtil() {
    }

    public static void openFirebaseReference(String ref, final ListActivity callerActivity) {
        if (firebaseUtil == null) {
            firebaseUtil = new FirebaseUtil();
            mFirebaseDatabase = FirebaseDatabase.getInstance();
            mFirebaseAuth = FirebaseAuth.getInstance();
            caller = callerActivity;
            mAuthStateListener = firebaseAuth -> {
                if (firebaseAuth.getCurrentUser() == null)  signIn();
                else checkAdmin(firebaseAuth.getUid());
            };
            connectStorage();
        }
        mDeals = new ArrayList<>();
        mDatabaseReference = mFirebaseDatabase.getReference().child(ref);
    }

    private static void checkAdmin(String uid) {
        isAdmin = false;
        DatabaseReference ref = mFirebaseDatabase.getReference().child("administrators").child(uid);
        ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                isAdmin = true;
                caller.showMenu();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private static void signIn() {
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build());
        caller.startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .setTheme(R.style.LoginTheme)
                        .setLogo(R.drawable.travelmantis_logo_white)
                        .build(),
                FirebaseUISignInKey);
    }

    public static void attachAuthListener() {
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    public static void detachAuthListener() {
        mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
    }

    public static void connectStorage() {
         mFirebaseStorage = FirebaseStorage.getInstance();
         mStorageReference = mFirebaseStorage.getReference().child("deals_pictures");
    }

}
