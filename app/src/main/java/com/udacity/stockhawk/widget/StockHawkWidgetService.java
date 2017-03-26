package com.udacity.stockhawk.widget;

import android.content.Intent;
import android.widget.RemoteViewsService;

/**
 * Created by Waszak on 26.03.2017.
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
