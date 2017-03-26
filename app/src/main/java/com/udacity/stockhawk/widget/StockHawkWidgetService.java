package com.udacity.stockhawk.widget;

import android.content.Intent;
import android.widget.RemoteViewsService;

/**
 * Service for remote views
 */

public class StockHawkWidgetService extends RemoteViewsService {
    /**
     * Get appropriate  factory for data.
     *
     * @param intent
     */
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new StockHawkWidgetListProvider(getApplicationContext(), intent);
    }
}
