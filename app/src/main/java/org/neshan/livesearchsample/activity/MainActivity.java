package org.neshan.livesearchsample.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.neshan.common.model.LatLng;
import org.neshan.livesearchsample.R;
import org.neshan.livesearchsample.adapter.SearchAdapter;
import org.neshan.mapsdk.MapView;
import org.neshan.servicessdk.search.NeshanSearch;
import org.neshan.servicessdk.search.model.Item;
import org.neshan.servicessdk.search.model.NeshanSearchResult;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;

public class MainActivity extends AppCompatActivity implements SearchAdapter.OnSearchItemListener {

    private MapView mapView;
    private EditText searchEditText;
    private RecyclerView recyclerView;

    private List<Item> items;
    private SearchAdapter adapter;
    private Runnable runnable;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mapView = findViewById(R.id.mapview);
        searchEditText = findViewById(R.id.txt_search);
        recyclerView = findViewById(R.id.recycler_view);

        items = new ArrayList<>();
        adapter = new SearchAdapter(items, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onStart() {
        super.onStart();

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (runnable != null) {
                    handler.removeCallbacks(runnable);
                }
                runnable = new Runnable() {
                    @Override
                    public void run() {
                        search(mapView.getCameraTargetPosition(), editable.toString());
                    }
                };

                handler.postDelayed(runnable, 1000);
            }
        });
    }

    private void search(LatLng searchPosition, String text) {
        //TODO: Replace YOUR-API-KEY with your api key
        new NeshanSearch.Builder("YOUR-API-KEY")
                .setLocation(searchPosition)
                .setTerm(text)
                .build().call(new Callback<NeshanSearchResult>() {
                    @Override
                    public void onResponse(Call<NeshanSearchResult> call, retrofit2.Response<NeshanSearchResult> response) {
                        if (response.code() == 403) {
                            Toast.makeText(MainActivity.this, getString(R.string.connection_error), Toast.LENGTH_LONG).show();
                            return;
                        }
                        if (response.body() != null) {
                            NeshanSearchResult result = response.body();
                            items = result.getItems();
                            adapter.updateList(items);
                        }
                    }

                    @Override
                    public void onFailure(Call<NeshanSearchResult> call, Throwable t) {
                        Toast.makeText(MainActivity.this, getString(R.string.connection_error), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onSeachItemClick(LatLng latLng) {
        closeKeyBoard();
        adapter.updateList(new ArrayList<Item>());
        mapView.setZoom(16f, 0);
        mapView.moveCamera(latLng, 0);
    }

    private void closeKeyBoard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (getCurrentFocus() != null) {
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }
}