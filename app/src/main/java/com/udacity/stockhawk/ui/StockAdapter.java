package com.udacity.stockhawk.ui;


import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.PrefUtils;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

class StockAdapter extends RecyclerView.Adapter<StockAdapter.StockViewHolder> {

    private static final String POSITIVE_DOLLAR_PREFIX = "+$";
    private static final String POSITIVE = "+";
    private static final int MIN_FRACTION_DIGITS = 2;
    private static final int MAX_FRACTION_DIGITS = 2;

    private final Context mContext;
    private final DecimalFormat mDollarFormatWithPlus;
    private final DecimalFormat mDollarFormat;
    private final DecimalFormat mPercentageFormat;
    private Cursor mCursor;
    private final StockAdapterOnClickHandler mClickHandler;


    StockAdapter(Context context, StockAdapterOnClickHandler clickHandler) {
        mContext = context;
        mClickHandler = clickHandler;
        mDollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
        mDollarFormatWithPlus = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
        mDollarFormatWithPlus.setPositivePrefix(POSITIVE_DOLLAR_PREFIX);
        mPercentageFormat = (DecimalFormat) NumberFormat.getPercentInstance(Locale.getDefault());
        mPercentageFormat.setMaximumFractionDigits(MAX_FRACTION_DIGITS);
        mPercentageFormat.setMinimumFractionDigits(MIN_FRACTION_DIGITS);
        mPercentageFormat.setPositivePrefix(POSITIVE);
    }

    void setCursor(Cursor cursor) {
        this.mCursor = cursor;
        notifyDataSetChanged();
    }

    String getSymbolAtPosition(int position) {
        mCursor.moveToPosition(position);
        return mCursor.getString(Contract.Quote.POSITION_SYMBOL);
    }

    @Override
    public StockViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View item = LayoutInflater.from(mContext).inflate(R.layout.list_item_quote, parent, false);

        return new StockViewHolder(item);
    }

    @Override
    public void onBindViewHolder(StockViewHolder holder, int position) {

        mCursor.moveToPosition(position);

        holder.mSymbol.setText(mCursor.getString(Contract.Quote.POSITION_SYMBOL));
        holder.mPrice.setText(mDollarFormat.format(mCursor.getFloat(Contract.Quote.POSITION_PRICE)));

        float rawAbsoluteChange = mCursor.getFloat(Contract.Quote.POSITION_ABSOLUTE_CHANGE);
        float percentageChange = mCursor.getFloat(Contract.Quote.POSITION_PERCENTAGE_CHANGE);

        if (rawAbsoluteChange > 0) {
            holder.mChange.setBackgroundResource(R.drawable.percent_change_pill_green);
        } else {
            holder.mChange.setBackgroundResource(R.drawable.percent_change_pill_red);
        }

        String change = mDollarFormatWithPlus.format(rawAbsoluteChange);
        String percentage = mPercentageFormat.format(percentageChange / 100);

        if (PrefUtils.getDisplayMode(mContext)
                .equals(mContext.getString(R.string.pref_display_mode_absolute_key))) {
            holder.mChange.setText(change);
        } else {
            holder.mChange.setText(percentage);
        }


    }

    @Override
    public int getItemCount() {
        if (mCursor != null) {
            return mCursor.getCount();
        }
        return 0;
    }


    interface StockAdapterOnClickHandler {
        void onClick(String symbol);
    }

    class StockViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.symbol)
        TextView mSymbol;

        @BindView(R.id.price)
        TextView mPrice;

        @BindView(R.id.change)
        TextView mChange;

        StockViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            mCursor.moveToPosition(getAdapterPosition());
            int symbolColumn = mCursor.getColumnIndex(Contract.Quote.COLUMN_SYMBOL);
            mClickHandler.onClick(mCursor.getString(symbolColumn));
        }


    }
}
