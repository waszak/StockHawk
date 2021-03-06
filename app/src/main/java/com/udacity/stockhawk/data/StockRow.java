package com.udacity.stockhawk.data;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Class that represents row in widget
 */

public class StockRow {

    private String mSymbol;
    private Float mPrice;
    private Float mRawAbsoluteChange;
    private Float mPercentageChange;

    private static final String POSITIVE_DOLLAR_PREFIX = "+$";
    private static final String POSITIVE = "+";
    private static final int MIN_FRACTION_DIGITS = 2;
    private static final int MAX_FRACTION_DIGITS = 2;

    private final DecimalFormat mDollarFormatWithPlus;
    private final DecimalFormat mDollarFormat;
    private final DecimalFormat mPercentageFormat;

    public StockRow(String symbol, float price, float rawAbsoluteChange, float percentageChange) {
        mSymbol = symbol;
        mPrice = price;
        mRawAbsoluteChange = rawAbsoluteChange;
        mPercentageChange = percentageChange;
        mDollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
        mDollarFormatWithPlus = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
        mDollarFormatWithPlus.setPositivePrefix(POSITIVE_DOLLAR_PREFIX);
        mPercentageFormat = (DecimalFormat) NumberFormat.getPercentInstance(Locale.getDefault());
        mPercentageFormat.setMaximumFractionDigits(MAX_FRACTION_DIGITS);
        mPercentageFormat.setMinimumFractionDigits(MIN_FRACTION_DIGITS);
        mPercentageFormat.setPositivePrefix(POSITIVE);
    }

    public String getSymbol(){return mSymbol;}

    public String getPrice(){ return mDollarFormat.format(mPrice);}

    public String getRawAbsoluteChange(){
        return mDollarFormatWithPlus.format(mRawAbsoluteChange);
    }
    public String getPercentageChange(){
        return  mPercentageFormat.format(mPercentageChange / 100);
    }

    public boolean isPositiveChange(){
        return mRawAbsoluteChange > 0;
    }
}
