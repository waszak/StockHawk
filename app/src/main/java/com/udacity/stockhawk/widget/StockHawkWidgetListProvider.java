package com.udacity.stockhawk.widget;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.PrefUtils;
import com.udacity.stockhawk.data.StockRow;

import java.util.ArrayList;

import timber.log.Timber;

/**
 * Created by Waszak on 26.03.2017.
 */

public class StockHawkWidgetListProvider implements RemoteViewsService.RemoteViewsFactory {

    private ArrayList<StockRow> mStockRows;
    private Context mContext;
    public StockHawkWidgetListProvider(Context context, Intent intent){
        mContext = context;
    }

    /*
        Called when factory is created;
     */
    @Override
    public void onCreate() {
        Timber.d("Factory is created");
        mStockRows = new ArrayList<>();
        onDataSetChanged();
    }

    @Override
    public void onDataSetChanged() {
        Timber.d("Stock list data changed");
        Cursor cursor = getCursor();

        if (!cursor.moveToFirst()) {
            Timber.d("moveToFirst failed");
            cursor.close();
            return;
        }
        mStockRows.clear();
        mStockRows.add(Contract.Quote.mapToStockRow(cursor));
        while(cursor.moveToNext()){
            mStockRows.add(Contract.Quote.mapToStockRow(cursor));
        }

        cursor.close();
    }

    private Cursor getCursor() {
        return mContext.getContentResolver().query(
                Contract.Quote.URI,
                Contract.Quote.QUOTE_COLUMNS.toArray(new String[]{}),
                null, null, Contract.Quote.COLUMN_SYMBOL
        );
    }

    /**
     *
     * @param position of view we want in adapter
     * @return view at position
     */
    @Override
    public RemoteViews getViewAt(int position) {
        Timber.d("Get view at position "+position);
        RemoteViews remoteViews =  new RemoteViews(mContext.getPackageName(),
                R.layout.list_item_quote);
        StockRow stockRow = mStockRows.get(position);

        remoteViews.setTextViewText(R.id.symbol, stockRow.getSymbol());
        remoteViews.setTextViewText(R.id.price, stockRow.getPrice());

        remoteViews.setInt(
                R.id.change,
                "setBackgroundResource",
                stockRow.isPositiveChange() ? R.drawable.percent_change_pill_green : R.drawable.percent_change_pill_red
        );

        String change = stockRow.getRawAbsoluteChange();
        if (!isDollarWithPlusMode()) {
            change = stockRow.getPercentageChange();

        }
        remoteViews.setTextViewText(R.id.change, change);

        return remoteViews;
    }

    private boolean isDollarWithPlusMode() {
        return PrefUtils.getDisplayMode(mContext).equals(mContext.getString(R.string.pref_display_mode_absolute_key));
    }

    @Override
    public int getCount() {
        return mStockRows.size();
    }

    @Override
    public void onDestroy() {

    }


    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    /**
     * @return numbers of views returned by this provider
     */
    @Override
    public int getViewTypeCount() {
        return 1;
    }

    /**
     *
     * @param position
     * @return position of row in data set
     */
    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     *
     * @return if false there are ItemId are unique.
     */
    @Override
    public boolean hasStableIds() {
        return false;
    }
}