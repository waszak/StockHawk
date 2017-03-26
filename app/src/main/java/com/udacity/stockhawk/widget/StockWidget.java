package com.udacity.stockhawk.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.widget.RemoteViews;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.sync.QuoteIntentService;

import butterknife.ButterKnife;

/**
 * Implementation of App Widget functionality.
 */
public class StockWidget extends AppWidgetProvider{

    private static PendingIntent getPendingIntent(Context context) {
        Intent refreshIntent = new Intent(context, QuoteIntentService.class);
        return PendingIntent.getService(context, 0, refreshIntent, 0);
    }

    @NonNull
    private static Intent getServiceIntent(Context context, int [] appWidgetIds) {
        Intent intent = new Intent(context, StockHawkWidgetService.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
        return intent;
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        // There may be multiple widgets active, so update all of them
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.stock_widget);
        views.setRemoteAdapter(R.id.widget_stock_list, getServiceIntent(context, appWidgetIds));
        //views.setEmptyView(R.id.widget_stock_list, R.id.wi);

        //views.setOnClickPendingIntent(R.id.widget_stock_list, getPendingIntent(context));

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetIds, views);

    }

}

