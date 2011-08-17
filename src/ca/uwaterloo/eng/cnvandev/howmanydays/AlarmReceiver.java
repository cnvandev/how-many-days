package ca.uwaterloo.eng.cnvandev.howmanydays;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

public class AlarmReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent broadcastIntent) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(String.format())
		       .setCancelable(true)
		       .setNeutralButton(R.string.OK, new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		        	   dialog.cancel();
		           }
		       });
		AlertDialog set_answer_dialog = builder.create();
		set_answer_dialog.show();
	}

}
