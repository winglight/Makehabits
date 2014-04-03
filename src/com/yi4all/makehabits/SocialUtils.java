package com.yi4all.makehabits;

import twitter4j.TwitterException;

import com.facebook.android.Facebook;
import com.facebook.android.FacebookError;
import com.yi4all.makehabits.twitter.DialogError;
import com.yi4all.makehabits.twitter.Twitter;
import com.yi4all.makehabits.twitter.TwitterError;
import com.yi4all.makehabits.twitter.Twitter.DialogListener;

import android.app.Activity;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;

public class SocialUtils {

	private final static String LOG_TAG = "SocialUtils";

	/**** Twitter ****/
	private final static String CONSUMER_KEY = "ZY8SMeeqrIt5C0uz9unpg";
	private final static String CONSUMER_SECRET = "w4InRlKA1D0wXM7yZJwgUhZO7CFZKyqz1pyW22WGTwg";

	/****** Facebook ******/
	public static Facebook facebook = new Facebook("144144472394084");

	public static Twitter twitter = new Twitter(R.drawable.twitter,
			CONSUMER_KEY, CONSUMER_SECRET);

	public static void authorizeTwitter(final Activity context) {

		if (!isTWPrepared(context)) {

			twitter.authorize(context, new DialogListener() {

				@Override
				public void onTwitterError(TwitterError e) {
					ActivityUtils.toastMsg(context, context.getString(
							R.string.connect_twitter_failed, e.toString()));
					twitter = new Twitter(R.drawable.twitter, CONSUMER_KEY,
							CONSUMER_SECRET);
				}

				@Override
				public void onError(DialogError e) {
					ActivityUtils.toastMsg(context, context.getString(
							R.string.connect_twitter_failed, e.toString()));
					twitter = new Twitter(R.drawable.twitter, CONSUMER_KEY,
							CONSUMER_SECRET);
				}

				@Override
				public void onComplete(Bundle values) {
					// save access_token,secret_token to
					// preference
					String accessToken = values.getString(Twitter.ACCESS_TOKEN);
					String secretToken = values.getString(Twitter.SECRET_TOKEN);

					ActivityUtils.savePrefTwitter(context, accessToken,
							secretToken);
					
					ActivityUtils.toastMsg(context, 
							R.string.connect_twitter_success);
					
					//send message to activity
					Message msg = Message.obtain();
					msg.arg1 = MainActivity.MSG_MODE_AUTHORIZED_TWITTER;
					((MainActivity)context).getmPrepareShareHandler().sendMessage(msg);
				}

				@Override
				public void onCancel() {
					ActivityUtils.toastMsg(context, R.string.auth_warning);
					twitter = new Twitter(R.drawable.twitter, CONSUMER_KEY,
							CONSUMER_SECRET);
				}
			});
		}
	}

	public static boolean isTWPrepared(Activity context) {
		String[] pref = ActivityUtils.getPrefTwitter(context);
		String access = pref[0];
		String secret = pref[1];
		if (access == null || secret == null || access.length() == 0
				|| secret.length() == 0) {
			return false;
		} else {
			twitter.setOAuthAccessToken(access, secret);
			return true;
		}
	}

	public static void authorizeFB(final Activity context) {

		/*
		 * Only call authorize if the access_token has expired.
		 */
		if (!isFBPrepared(context)) {

			// create handler
			facebook.authorize(context, new String[] { "publish_stream" },
					new com.facebook.android.Facebook.DialogListener() {
						@Override
						public void onComplete(Bundle values) {
							ActivityUtils.savePrefFB(context,
									facebook.getAccessToken(),
									facebook.getAccessExpires());
							ActivityUtils.toastMsg(context, 
									R.string.connect_facebook_success);
							
							//send message to activity
							if(context instanceof MainActivity){
							
							Message msg = Message.obtain();
							msg.arg1 = MainActivity.MSG_MODE_AUTHORIZED_FACEBOOK;
							
							((MainActivity)context).getmPrepareShareHandler().sendMessage(msg);
							}
						}

						@Override
						public void onFacebookError(FacebookError e) {
							ActivityUtils.toastMsg(context, context.getString(
									R.string.connect_facebook_failed,
									e.toString()));
						}

						@Override
						public void onError(com.facebook.android.DialogError e) {
							ActivityUtils.toastMsg(context, context.getString(
									R.string.connect_facebook_failed,
									e.toString()));
						}

						@Override
						public void onCancel() {
							ActivityUtils.toastMsg(context,
									R.string.auth_warning);
						}
					});
		}
	}

	public static boolean isFBPrepared(Activity context) {
		String access_token = ActivityUtils.getPrefFBAccess(context);
		long expires = ActivityUtils.getPrefFBExpires(context);
		if (access_token != null) {
			facebook.setAccessToken(access_token);
		}
		if (expires != 0) {
			facebook.setAccessExpires(expires);
		}
		return facebook.isSessionValid();
	}

	public static boolean shareToTwitter(Activity context, String content) {
		// 1.authorize
		// authorizeTwitter(context);
		if (isTWPrepared(context)) {
			// 2.create a new twitter
			try {
				Log.d(LOG_TAG, "beginning twitter-----");
				twitter.getTwitter().updateStatus(content);
				Log.d(LOG_TAG, "finishing twitter-----");
				return true;
			} catch (TwitterException e) {
				e.printStackTrace();
				Log.d(LOG_TAG, " twitter error-----" + e);
			}
		}
		return false;
	}

	public static boolean shareToFB(Activity context, String content) {
		// 1.authrize
		// authorizeFB(context);
		if (isFBPrepared(context)) {
			// 2.post to wall
			try {
				String response = facebook.request("me");
				Log.d(LOG_TAG, "me response: " + response);
				Bundle parameters = new Bundle();
				parameters.putString("message", content);
				parameters.putString("description",
						"Activity on App: Make Habits for Me");
				response = facebook.request("me/feed", parameters, "POST");
				Log.d(LOG_TAG, "got response: " + response);
				if (response == null || response.equals("")
						|| response.equals("false")) {
					Log.v("Error", "Blank response");
					return false;
				} else {
					return true;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return false;
	}
}
