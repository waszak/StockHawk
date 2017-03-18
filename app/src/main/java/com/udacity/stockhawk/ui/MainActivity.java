package com.udacity.stockhawk.ui;

import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.PrefUtils;
import com.udacity.stockhawk.sync.QuoteSyncJob;
import com.udacity.stockhawk.util.BaseAsyncTask;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>,
        SwipeRefreshLayout.OnRefreshListener,
        StockAdapter.StockAdapterOnClickHandler {

    private static String TAG = MainActivity.class.getSimpleName();
    private static final int STOCK_LOADER = 0;
    private static final String STOCK_DIALOG_FRAGMENT = "STOCK_DIALOG_FRAGMENT";

    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.recycler_view)
    RecyclerView mStockRecyclerView;

    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.swipe_refresh)
    SwipeRefreshLayout mSwipeRefreshLayout;

    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.error)
    TextView mError;

    private StockAdapter mAdapter;

    @Override
    public void onClick(String symbol) {
        Timber.d("Symbol clicked: %s", symbol);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mAdapter = new StockAdapter(this, this);
        mStockRecyclerView.setAdapter(mAdapter);
        mStockRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setRefreshing(true);
        onRefresh();

        QuoteSyncJob.initialize(this);
        getSupportLoaderManager().initLoader(STOCK_LOADER, null, this);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                String symbol = mAdapter.getSymbolAtPosition(viewHolder.getAdapterPosition());
                PrefUtils.removeStock(MainActivity.this, symbol);
                getContentResolver().delete(Contract.Quote.makeUriForStock(symbol), null, null);
            }
        }).attachToRecyclerView(mStockRecyclerView);


    }

    private boolean networkUp() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }

    @Override
    public void onRefresh() {
        QuoteSyncJob.syncImmediately(this);
        if (!networkUp() && mAdapter.getItemCount() == 0) {
            mSwipeRefreshLayout.setRefreshing(false);
            mError.setText(getString(R.string.error_no_network));
            mError.setVisibility(View.VISIBLE);
        } else if (!networkUp()) {
            mSwipeRefreshLayout.setRefreshing(false);
            Toast.makeText(this, R.string.toast_no_connectivity, Toast.LENGTH_LONG).show();
        } else if (PrefUtils.getStocks(this).size() == 0) {
            mSwipeRefreshLayout.setRefreshing(false);
            mError.setText(getString(R.string.error_no_stocks));
            mError.setVisibility(View.VISIBLE);
        } else {
            mError.setVisibility(View.GONE);
        }
    }

    public void button(@SuppressWarnings("UnusedParameters") View view) {
        new AddStockDialog().show(getFragmentManager(), STOCK_DIALOG_FRAGMENT);
    }

    void addStock(String symbol) {
        if (symbol != null && !symbol.isEmpty()) {
            if (networkUp()) {
                mSwipeRefreshLayout.setRefreshing(true);
            } else {
                String message = getString(R.string.toast_stock_added_no_connectivity, symbol);
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }
            BaseAsyncTask<String,Void,Boolean> stockExitsTask = new BaseAsyncTask<>();
            stockExitsTask.setTask(getStockExistsTask(symbol));
            stockExitsTask.setCallback(getCallbackForStockExistsTask(symbol));
            stockExitsTask.execute();
        }
    }

    private BaseAsyncTask.ICallbackTask<Boolean> getCallbackForStockExistsTask(final String stockSymbol){
        return new BaseAsyncTask.ICallbackTask<Boolean>() {
            @Override
            public void onStart() {}

            @Override
            public void onSuccess(Boolean exists) {
                if(exists) {
                    PrefUtils.addStock(MainActivity.this, stockSymbol);
                }else{
                    String message = getString(R.string.error_stock_not_exists, stockSymbol);
                    Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
                }
                QuoteSyncJob.syncImmediately(MainActivity.this);
            }

            @Override
            public void onError() {
                String message = getString(R.string.error_try_again);
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
            }
        };
    }
    private BaseAsyncTask.ITask<Boolean> getStockExistsTask(final String stockSymbol){
        return new BaseAsyncTask.ITask<Boolean>() {
            @Override
            public Boolean task() {
                try {
                    Timber.d(TAG, "getStock: " + stockSymbol);
                    Stock stock = YahooFinance.get(stockSymbol);
                    return stock.getQuote().getPrice() != null;
                }catch (IOException e){
                    e.printStackTrace();
                    return false;
                }catch (Exception e){
                    e.printStackTrace();
                    //When stock name is for example "."
                    //we get arrayindexoutofboundsexception
                    //from yahoo api.
                    Timber.e(TAG, "YahooAPI parsing errors: "+ e.getMessage() );
                    return false;
                }
            }
        };
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this,
                Contract.Quote.URI,
                Contract.Quote.QUOTE_COLUMNS.toArray(new String[]{}),
                null, null, Contract.Quote.COLUMN_SYMBOL);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mSwipeRefreshLayout.setRefreshing(false);
        if (data.getCount() != 0) {
            mError.setVisibility(View.GONE);
        }
        mAdapter.setCursor(data);
    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mSwipeRefreshLayout.setRefreshing(false);
        mAdapter.setCursor(null);
    }


    private void setDisplayModeMenuItemIcon(MenuItem item) {
        if (PrefUtils.getDisplayMode(this)
                .equals(getString(R.string.pref_display_mode_absolute_key))) {
            item.setIcon(R.drawable.ic_percentage);
        } else {
            item.setIcon(R.drawable.ic_dollar);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity_settings, menu);
        MenuItem item = menu.findItem(R.id.action_change_units);
        setDisplayModeMenuItemIcon(item);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_change_units) {
            PrefUtils.toggleDisplayMode(this);
            setDisplayModeMenuItemIcon(item);
            mAdapter.notifyDataSetChanged();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
