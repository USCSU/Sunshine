package com.example.android.sunshine;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * {@link ForecastAdapter} exposes a list of weather forecasts
 * from a {@link android.database.Cursor} to a {@link android.widget.ListView}.
 */
public class ForecastAdapter extends CursorAdapter {
    //index of type
    private static final int VIEW_TYPE_TODAY =0;
    private static final int VIEW_TYPE_FUTURE_DAY =1;
    private static final int VIEW_TYPE_COUNT =2;

    public ForecastAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }
    //use this class as a tag of adpater so that we can retrieve it conveniently
    public static class ViewHolder{
        public final ImageView iconView;
        public final TextView dateView;
        public final TextView descriptionView;
        public final TextView highTempView;
        public final TextView lowTempView;

        public ViewHolder(View view) {
            iconView = (ImageView) view.findViewById(R.id.list_item_icon);
            dateView = (TextView) view.findViewById(R.id.list_item_date_textview);
            descriptionView = (TextView) view.findViewById(R.id.list_item_forecast_textview);
            highTempView = (TextView) view.findViewById(R.id.list_item_high_textview);
            lowTempView = (TextView) view.findViewById(R.id.list_item_low_textview);
        }
    }
    //has to overwrite two layout type method position: index of cursor; output: index of layout
    @Override//choose layout
    public int getItemViewType(int position) {
        return position==0?VIEW_TYPE_TODAY:VIEW_TYPE_FUTURE_DAY;
    } //return layout numbers
    public int getViewTypeCount(){
        return VIEW_TYPE_COUNT;
    }

    private String formatHighLows(double high, double low) {
        boolean isMetric = Utility.isMetric(mContext);
        String highLowStr = Utility.formatTemperature(high, isMetric) + "/" + Utility.formatTemperature(low, isMetric);
        return highLowStr;
    }

    /*
        This is ported from FetchWeatherTask --- but now we go straight from the cursor to the
        string.
     */
    private String convertCursorRowToUXFormat(Cursor cursor) {
        // get row indices for our cursor
//        int idx_max_temp = cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP);
//        int idx_min_temp = cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP);
//        int idx_date = cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_DATE);
//        int idx_short_desc = cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_SHORT_DESC);

        String highAndLow = formatHighLows(
                cursor.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMP),
                cursor.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP));

        return Utility.formatDate(cursor.getLong(ForecastFragment.COL_WEATHER_DATE)) +
                " - " + cursor.getString(ForecastFragment.COL_WEATHER_DESC) +
                " - " + highAndLow;
    }

    /*
        Remember that these views are reused as needed.
     */
    @Override //change newView to inflate two type layout
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        //get layout index
        int viewType = getItemViewType(cursor.getPosition());
        int layoutId = -1; //indicate which layout should be used; R.id.***
        if(viewType == VIEW_TYPE_TODAY)
            layoutId = R.layout.list_item_forecast_today;
        else
            layoutId = R.layout.list_item_forecast;
        View view =  LayoutInflater.from(context).inflate(layoutId,parent,false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder); // actually its just a hashset so that we can retrieve that fast
        return view;
//        View view = LayoutInflater.from(context).inflate(R.layout.list_item_forecast, parent, false);
//
//        return view;
    }

    /*
        This is where we fill-in the views with the contents of the cursor.
     */
    @Override
    //get data from cursor and populate to view
    public void bindView(View view, Context context, Cursor cursor) {
        // our view is pretty simple here --- just a text view
        // we'll keep the UI functional with a simple (and slow!) binding.

//        TextView tv = (TextView)view;
//        tv.setText(convertCursorRowToUXFormat(cursor));
        ViewHolder viewHolder = (ViewHolder)view.getTag();

        //get weather id
        int weather_id = cursor.getInt(ForecastFragment.COL_WEATHER_ID);
        //get icon
        viewHolder.iconView.setImageResource(R.drawable.ic_snow);
        //get date
        String date = Utility.getFriendlyDayString(context, cursor.getLong(ForecastFragment.COL_WEATHER_DATE));
        viewHolder.dateView.setText(date);
        //get description
        String desc = cursor.getString(ForecastFragment.COL_WEATHER_DESC);
        viewHolder.descriptionView.setText(desc);
        //get high tem
        boolean isMatic = Utility.isMetric(context);
        String high = Utility.formatTemperature(cursor.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMP), isMatic);
        viewHolder.highTempView.setText(high);

        //get low tempture
        String low = Utility.formatTemperature(cursor.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP),isMatic);
        viewHolder.lowTempView.setText(low);
    }
}
