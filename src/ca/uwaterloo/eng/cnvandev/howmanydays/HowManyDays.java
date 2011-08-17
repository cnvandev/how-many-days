package ca.uwaterloo.eng.cnvandev.howmanydays;

import java.text.DateFormat;
import java.util.Calendar;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TextView;

public class HowManyDays extends Activity {
	Context context = null;
	Resources resources = null;
	SharedPreferences settings = null;
	
	Calendar IRS = null;
	
	private String dateString = null;
	private String hoursString = null;
	private String minutesString = null;
	private String secondsString = null;
	private String timeString = null;
	private String smallTimeString = null;
	private String daysAgoString = null;
	
	private TextView timeView = null;
	private TextView bigView = null;
	private TextView todayView = null;
	
	private int irsYear = 0;
	private int irsMonth = 0;
	private int irsDay = 0;
	
    static final int DATE_DIALOG_ID = 0;
	
	private Handler mHandler = new Handler();
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Get the resources we'll need.
        context = getApplicationContext();
        resources = context.getResources();
        settings = getPreferences(Context.MODE_PRIVATE);
        
        setContentView(R.layout.main);
        
        // Get the strings we need, to avoid loading them later.
        dateString = resources.getString(R.string.x_days);
        hoursString = resources.getString(R.string.x_hours);
        minutesString = resources.getString(R.string.x_minutes);
        secondsString = resources.getString(R.string.x_seconds);
        timeString = resources.getString(R.string.x_full_time);
        smallTimeString = resources.getString(R.string.x_small_time);
        daysAgoString = resources.getString(R.string.x_days_ago);
        
        // Get the IRS date from preferences and set it locally. 
        IRS = Calendar.getInstance();
        irsYear = settings.getInt("irsYear", 2014);
        irsMonth = settings.getInt("irsMonth", 1);
        irsDay = settings.getInt("irsDay", 5);
        IRS.set(irsYear, irsMonth, irsDay);
        
        // Set the views we'll need to update so we can do it quickly.
        timeView = (TextView) findViewById(R.id.timeView);
        bigView = (TextView) findViewById(R.id.daysView);
        todayView = (TextView) findViewById(R.id.todayView);
        
        // Update the time for viewing.
        updateTime();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	// Inflate the options menu from XML.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle dialog selection - pick a date.
        switch (item.getItemId()) {
        case R.id.set_irs:
        	showDialog(DATE_DIALOG_ID);
        	return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
    
    // The handler for the date picker.
    private DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {
    	// Save the date when it's handed back by the dialog.
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        	// Store the date locally.
            irsYear = year;
            irsMonth = monthOfYear;
            irsDay = dayOfMonth;
            IRS.set(irsYear, irsMonth, irsDay);
            
            // Save the IRS date to the preferences.
            SharedPreferences.Editor editor = settings.edit();
            editor.putInt("irsYear", year);
            editor.putInt("irsMonth", monthOfYear);
            editor.putInt("irsDay", dayOfMonth);
            editor.commit();

            // Update the time now that it's been changed.
            updateTime();
        }
    };
    
    // The handler for the year picker.
    /*private DatePickerDialog.OnDateSetListener mYearSetListener = new DatePickerDialog.OnDateSetListener() {
    	// Save the date when it's handed back by the dialog.
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            // IRS is typically the first saturday in February, so we try to find that.
            // Starting from February 1st and loop forward until we hit a saturday.
            Calendar new_IRS = Calendar.getInstance();
            new_IRS.set(year, 2, 1);
            while(new_IRS.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY) {
            	new_IRS.add(1, Calendar.DAY_OF_MONTH);
            }
            
            // Store the information locally.
            irsYear = year;
            irsMonth = new_IRS.get(Calendar.MONTH);
            irsDay = new_IRS.get(Calendar.DAY_OF_MONTH);
            
            Log.i("IRS", irsYear + ", " + irsMonth + ", " + irsDay);
            
            // Save the year to the preferences.
            SharedPreferences.Editor editor = settings.edit();
            editor.putInt("irsYear", year);
            editor.putInt("irsYear", irsMonth);
            editor.putInt("irsYear", irsDay);
            editor.commit();

            // Update the time now that it's been changed.
            updateTime();
        }
    };*/
    
    @Override
    protected Dialog onCreateDialog(int id) {
    	// Create the actual dialogs when they're requested.
        switch (id) {
        	// The date picker dialog is a standard year-month-date one.
        	case DATE_DIALOG_ID:
        		DatePickerDialog IRSPicker = new DatePickerDialog(this, mDateSetListener, irsYear, irsMonth, irsDay);
        		IRSPicker.setTitle(R.string.pick_irs);
        		return IRSPicker;
        }
        return null;
    }
    
    @Override
    public void onResume() {
    	super.onResume();
    	
    	// Kick off the update timer.
        mHandler.postDelayed(updateTimeTask, 100);
    }
    
    // A runnable to check and update the time every 100 milliseconds.
    private Runnable updateTimeTask = new Runnable() {
		public void run() {
			updateTime();
	    	
	    	// Set the next timer.
	    	mHandler.postDelayed(updateTimeTask, 100);
		}
    };
    
    // Updates the time displayed on the screen.
    public void updateTime() {
    	// Calculate the number of days.
    	Calendar rightNow = Calendar.getInstance();
    	Calendar counter = (Calendar) rightNow.clone();
    	long daysBetween = 0;
    	while (counter.before(IRS)) {
    		counter.add(Calendar.DAY_OF_MONTH, 1);
    		daysBetween++;
    	}
    	
    	// Figure out the number of hours, minutes and seconds until midnight.
    	int hoursUntilMidnight = 23 - rightNow.get(Calendar.HOUR_OF_DAY); // 23 because there's a 0 hour.
    	int minutesUntilHour = 59 - rightNow.get(Calendar.MINUTE); // 59 because there's a minute 0.
    	int secondsUntilMinute = 60 - rightNow.get(Calendar.SECOND);
    	
    	Log.i("How Many Days?", daysBetween + " days, " +
    							hoursUntilMidnight + " hours, " +
    							minutesUntilHour + " minutes, " +
    							secondsUntilMinute + " seconds.");
    	
    	// Update the time view with the correct text.
    	if (daysBetween > 0) {
	    	bigView.setText(String.format(dateString, daysBetween));
    		timeView.setText(String.format(timeString, hoursUntilMidnight, minutesUntilHour, secondsUntilMinute));
    	} else if (hoursUntilMidnight > 0) {
    		bigView.setText(String.format(hoursString, hoursUntilMidnight));
    		timeView.setText(String.format(smallTimeString, minutesUntilHour, secondsUntilMinute));
    	} else if (minutesUntilHour > 0) {
    		bigView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 54);
    		bigView.setText(String.format(minutesString, minutesUntilHour));
    		timeView.setText(String.format(smallTimeString, 0, secondsUntilMinute));
    	} else if (secondsUntilMinute > 0) {
    		bigView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 54);
    		bigView.setText(String.format(secondsString, secondsUntilMinute));
    		timeView.setVisibility(View.GONE);
    	} else {
    		// Hide the countdown bit and show the pre-made DONE screen.
    		LinearLayout countdown = (LinearLayout) findViewById(R.id.countdown);
    		LinearLayout done = (LinearLayout) findViewById(R.id.done);
    		countdown.setVisibility(View.GONE);
    		done.setVisibility(View.VISIBLE);
    	}
    	
        DateFormat theDate = DateFormat.getDateTimeInstance();
        todayView.setText(theDate.format(rightNow.getTime()));
    }
    
    public void onPause() {
    	super.onPause();
    	
    	// Remove the time updater while we're not using it.
    	mHandler.removeCallbacks(updateTimeTask);
    }
}