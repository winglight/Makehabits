package com.yi4all.makehabits;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.yi4all.makehabits.twitter.Twitter;

public class ActivityUtils {

	public static String getPrefParameter(Context context, String key) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		String res = prefs.getString(key, null);
		return res;
	}
	
	public static String[] getPrefTwitter(Context context) {
		String[] res = new String[2];
		res[0] = getPrefParameter(context, "t_" + Twitter.ACCESS_TOKEN);
		res[1] = getPrefParameter(context, "t_" + Twitter.SECRET_TOKEN);
		return res;
	}
	
	public static String getPrefFBAccess(Context context) {
		return getPrefParameter(context, "f_access_token");
	}
	
	public static long getPrefFBExpires(Context context) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		long res = prefs.getLong("f_access_expires", 0);
		return res;
	}
	
	// save parameters into preference
		public static void savePrefTwitter(Context context, String accessToken, String secretToken) {
			SharedPreferences prefs = PreferenceManager
					.getDefaultSharedPreferences(context);
			Editor editor = prefs.edit();
			editor.putString("t_" + Twitter.ACCESS_TOKEN, accessToken);
			editor.putString("t_" + Twitter.SECRET_TOKEN, secretToken);
			editor.commit();
		}
		
		public static void savePrefFB(Context context, String accessToken, long expires) {
			SharedPreferences prefs = PreferenceManager
					.getDefaultSharedPreferences(context);
			Editor editor = prefs.edit();
			editor.putString("f_access_token", accessToken);
			editor.putLong("f_access_expires", expires);
			editor.commit();
		}
		
		public static void toastMsg(final Activity context, final String msg) {
			context.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					Toast.makeText(context, msg, Toast.LENGTH_SHORT)
							.show();
				}
			});
		}

		public static void toastMsg(final Activity context, int resId, String... args) {
			final String msg = context.getString(resId, args);
			toastMsg(context, msg);
		}
}
