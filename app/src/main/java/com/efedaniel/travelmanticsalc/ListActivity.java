package com.efedaniel.travelmanticsalc;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static com.efedaniel.travelmanticsalc.FirebaseUtil.isAdmin;

public class ListActivity extends AppCompatActivity {

    @BindView(R.id.deals) RecyclerView dealsRecyclerView;
    private Unbinder unbinder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        unbinder = ButterKnife.bind(this);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.insert_menu).setVisible(isAdmin);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.list_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.insert_menu:
                Intent intent = new Intent(this, DealActivity.class);
                startActivity(intent);
                return true;
            case R.id.logout_menu:
                AuthUI.getInstance()
                        .signOut(this)
                        .addOnCompleteListener(task -> FirebaseUtil.attachAuthListener());
                FirebaseUtil.detachAuthListener();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        FirebaseUtil.detachAuthListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        FirebaseUtil.openFirebaseReference("traveldeals", this);
        DealAdapter dealAdapter = new DealAdapter();
        dealsRecyclerView.setAdapter(dealAdapter);
        dealsRecyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        FirebaseUtil.attachAuthListener();
    }

    public void showMenu() {
        invalidateOptionsMenu();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == FirebaseUtil.FirebaseUISignInKey && resultCode == RESULT_OK) {
            Toast.makeText(getBaseContext(), getString(R.string.welcome_back), Toast.LENGTH_LONG).show();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }
}
