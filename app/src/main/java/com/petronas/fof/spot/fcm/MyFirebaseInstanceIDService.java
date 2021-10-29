package com.petronas.fof.spot.fcm;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessagingService;

import timber.log.Timber;


public class MyFirebaseInstanceIDService extends FirebaseMessagingService {
	private static final String TAG = "MyFirebaseIIDService";

	/**
	 * Called if InstanceID token is updated. This may occur if the security of
	 * the previous token had been compromised. Note that this is called when the InstanceID token
	 * is initially generated so this is where you would retrieve the token.
	 */
	/*@Override
	public void onTokenRefresh() {
		super.onTokenRefresh();

		String refreshedToken = FirebaseInstanceId.getInstance().getToken();
		Timber.d( "Refreshed token: " + refreshedToken);

		sendRegistrationToServer(refreshedToken);
	}*/

	@Override
	public void onNewToken(String s) {
		super.onNewToken(s);
		String refreshedToken = FirebaseInstanceId.getInstance().getToken();
		Timber.d( "Refreshed token: " + refreshedToken);
		sendRegistrationToServer(refreshedToken);
	}

	/**
	 * Persist token to third-party servers.
	 *
	 * Modify this method to associate the user's FCM InstanceID token with any server-side account
	 * maintained by your application.
	 *
	 * @param token The new token.
	 */
	private void sendRegistrationToServer(String token) {
		// Add custom implementation, as needed.
		//server key
//		AAAAAuT5BwY:APA91bFq9e3dooV_V7PIg1_9ljr26Mr6h8xlWEjGxFjrxXepKuzHaliWQaJxsNBexd1-yqJXxijDBEhxpEbTuALBl6CaHmK369laklZBBpozhkYssjdfs6-_nATTf1iRSKP6KAPrGe-W
		//device token
//		dy2oUYOyWI0:APA91bFxgQUxLbgoY49aVe_3biXabcP8CzRpt3il0z_sl3d-MPbU-oh6OmSa1knvt3mG74KdNM03gx35YBGJDRQFw9lMNat_R8kG3HaCGdakCbXqamfZy91FkomJQhxlnmIp89ndnwcf
	}
}