package com.udacity.stockhawk.ui;

import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;

import java.io.IOException;
import java.io.StringReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import au.com.bytecode.opencsv.CSVReader;
import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class StockDetailsActivity extends AppCompatActivity
{
    public static final String[] PROJECTION = new String[]{Contract.Quote.COLUMN_SYMBOL,
            Contract.Quote.COLUMN_HISTORY};

    @BindView(R.id.chart) LineChart mChart;
    @BindView(R.id.stock_name) TextView mStockName;
   /* BindView() CandleStickChart mChart;
    BindView SeekBar mSeekBarX, mSeekBarY;
    BindView TextView tvX, tvY;*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_details);
        ButterKnife.bind(this);

        Intent intentThatStartedThisActivity = getIntent();
        String stockSymbol = null;
        if (intentThatStartedThisActivity.hasExtra(MainActivity.STOCK)) {
            stockSymbol = intentThatStartedThisActivity.getStringExtra(MainActivity.STOCK);
        }
        Timber.d("Received stock symbol %s",stockSymbol);
        mStockName.setText(stockSymbol);
        Description description = new Description();
        description.setText("");
        mChart.setDescription(description);
        drawChart(stockSymbol);
    }

    private void drawChart(String stockSymbol){
        final LinkedList<Long> xAxisData = new LinkedList<>();
        List<Entry> entries = getHistory(stockSymbol, xAxisData);
        LineData lineData = new LineData(new LineDataSet(entries,stockSymbol));
        mChart.setData(lineData);
        XAxis xAxis = mChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                Date date = new Date(xAxisData.get((int)value) );
                return SimpleDateFormat.getDateInstance(DateFormat.SHORT,
                        getLocale()).format(date);
            }
        });
    }

    private Locale getLocale() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return getResources().getConfiguration().getLocales().get(0);
        }
        return getResources().getConfiguration().locale;
    }

    private List<Entry> getHistory(String stockSymbol, final  LinkedList<Long> xAxis){
        final LinkedList<Entry> entries = new LinkedList<>();
        String history = getHistoryString(stockSymbol);
        CSVReader csvReader = new CSVReader(new StringReader(history));
        int xAxisPosition = 0;
        try {
            List<String[]> lines = csvReader.readAll();
            for(int i = lines.size() - 1; i >= 0; i-- ){
                String[] line = lines.get(i);
                xAxis.addFirst(Long.parseLong(line[0]));
                Entry entry = new Entry(xAxisPosition++, Float.parseFloat(line[1]) );
                entries.add(entry);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return entries;
    }

    private String getHistoryString(String stockSymbol) {
        Cursor cursor = getCursor(stockSymbol);
        String history = "";
        if(cursor.moveToFirst()){
           history = cursor.getString(
                    cursor.getColumnIndex(Contract.Quote.COLUMN_HISTORY));
        }
        if(!cursor.isClosed()){
            cursor.close();
        }
        return history;
    }

    private Cursor getCursor(String stockSymbol) {
        return getContentResolver().query(Contract.Quote.makeUriForStock(stockSymbol),
                PROJECTION,null,null,null);
    }

}
