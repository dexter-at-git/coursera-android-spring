package vandy.mooc.model.services;

import java.io.File;
import java.io.InputStream;
import java.util.List;

import retrofit.client.Header;
import retrofit.client.Response;
import vandy.mooc.R;
import vandy.mooc.model.provider.VideoController;
import vandy.mooc.utils.VideoStorageUtils;
import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;

/**
 * Intent Service that will run in background and Uploads the Video having a
 * given Id. After the operation, it will send broadcast the Intent that will
 * send the result of the Upload to the VideoListActivity.
 */
public class DownloadVideoService extends IntentService {
	/**
	 * Custom Action that will be used to send Broadcast to the
	 * VideoListActivity.
	 */
	public static final String ACTION_DOWNLOAD_SERVICE_RESPONSE = "vandy.mooc.services.DownloadVideoService.RESPONSE";

	/**
	 * Key, used to store the videoId as an EXTRA in Intent.
	 */
	private static final String KEY_DOWNLOAD_VIDEO_ID = "download_videoId";

	/**
	 * Default Id , if no Id is present in Extras of the received Intent.
	 */
	private static final long DEFAULT_VIDEO_ID = 0;

	/**
	 * It is used by Notification Manager to send Notifications.
	 */
	private static final int NOTIFICATION_ID = 2;

	/**
	 * VideoController mediates the communication between Server and Android
	 * Storage.
	 */
	private VideoController mController;

	/**
	 * Manages the Notification displayed in System UI.
	 */
	private NotificationManager mNotifyManager;

	/**
	 * Builder used to build the Notification.
	 */
	private Builder mBuilder;

	/**
	 * Constructor for UploadVideoService.
	 * 
	 * @param name
	 */
	public DownloadVideoService(String name) {
		super("DownloadVideoService");
	}

	/**
	 * Constructor for UploadVideoService.
	 * 
	 * @param name
	 */
	public DownloadVideoService() {
		super("DownloadVideoService");
	}

	/**
	 * Factory method that makes the implicit intent another Activity uses to
	 * call this Service.
	 * 
	 * @param context
	 * @param videoId
	 * @return
	 */
	public static Intent makeIntent(Context context, long videoId) {
		return new Intent(context, DownloadVideoService.class).putExtra(
				KEY_DOWNLOAD_VIDEO_ID, videoId);
	}

	/**
	 * Hook method that is invoked on the worker thread with a request to
	 * process. Only one Intent is processed at a time, but the processing
	 * happens on a worker thread that runs independently from other application
	 * logic.
	 * 
	 * @param intent
	 */
	@Override
	protected void onHandleIntent(Intent intent) {
		// Starts the Notification to show the progress of video
		// upload.
		startNotification();

		// Create VideoController that will mediates the communication
		// between Server and Android Storage.
		mController = new VideoController(getApplicationContext());

		// Get the videoId from the Extras of Intent.
		long videoId = intent.getLongExtra(KEY_DOWNLOAD_VIDEO_ID,
				DEFAULT_VIDEO_ID);

		Response downloadResponse = mController.downloadVideo(videoId);
		List<Header> headers = downloadResponse.getHeaders();
		String filename = "";
		for (Header header : headers) {
			if (header.getName() != null
					&& header.getName().equals("Content-Disposition")) {
				filename = header.getValue();
				filename = filename.split("\"")[1];
				break;
			}
		}

		File downloadedFile = VideoStorageUtils.storeVideoInExternalDirectory(
				getApplicationContext(), downloadResponse, filename);

		if (downloadedFile != null && downloadedFile.exists()) {
			finishNotification("Download complete");
		} else {
			finishNotification("Download failed");
		}

		// Send the Broadcast to Activity that the Video Upload is
		// completed.
		sendBroadcast();
	}

	/**
	 * Send the Broadcast to Activity that the Video Upload is completed.
	 */
	private void sendBroadcast() {
		sendBroadcast(new Intent(ACTION_DOWNLOAD_SERVICE_RESPONSE)
				.addCategory(Intent.CATEGORY_DEFAULT));
	}

	/**
	 * Finish the Notification after the Video is Uploaded.
	 * 
	 * @param status
	 */
	private void finishNotification(String status) {
		// When the loop is finished, updates the notification.
		mBuilder.setContentText(status);

		// Removes the progress bar.
		mBuilder.setProgress(0, 0, false);
		mNotifyManager.notify(NOTIFICATION_ID, mBuilder.build());
	}

	/**
	 * Starts the Notification to show the progress of video upload.
	 */
	private void startNotification() {
		// Gets the access to System Notification Services.
		mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		// Build the Notification and sets an activity indicator
		// for an operation of indeterminate length.
		mBuilder = new NotificationCompat.Builder(this)
				.setContentTitle("Video Download")
				.setContentText("Download in progress")
				.setSmallIcon(R.drawable.ic_download).setProgress(0, 0, true);

		// Issues the notification
		mNotifyManager.notify(NOTIFICATION_ID, mBuilder.build());
	}
}
