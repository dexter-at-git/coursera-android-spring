package vandy.mooc.presenter;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.RatingBar.OnRatingBarChangeListener;
import vandy.mooc.R;
import vandy.mooc.common.ConfigurableOps;
import vandy.mooc.common.GenericAsyncTask;
import vandy.mooc.common.GenericAsyncTaskOps;
import vandy.mooc.model.provider.Video;
import vandy.mooc.model.provider.VideoController;
import vandy.mooc.model.services.DownloadVideoService;
import vandy.mooc.model.services.UploadVideoService;
import vandy.mooc.provider.VideoContract.VideoEntry;
import vandy.mooc.provider.VideoProvider;
import vandy.mooc.utils.VideoGalleryUtils;
import vandy.mooc.view.VideoDetailsActivity;
import vandy.mooc.view.VideoListActivity;
import vandy.mooc.view.ui.VideoAdapter;

public class VideoDetailsOps implements ConfigurableOps,
		GenericAsyncTaskOps<Video, Void, Video> {

	/**
	 * Debugging tag used by the Android logger.
	 */
	private static final String TAG = VideoDetailsOps.class.getSimpleName();

	/**
	 * Used to enable garbage collection.
	 */
	private WeakReference<VideoDetailsActivity> mActivity;

	// private Video mVideo;

	/**
	 * It allows access to application-specific resources.
	 */
	private Context mApplicationContext;

	/**
	 * The GenericAsyncTask used to expand an Video in a background thread via
	 * the Video web service.
	 */
	private GenericAsyncTask<Video, Void, Video, VideoDetailsOps> mAsyncTask;

	/**
	 * VideoController mediates the communication between Server and Android
	 * Storage.
	 */
	VideoController mVideoController;

	/**
	 * The RatingBar that contains a rating for Video.
	 */
	private RatingBar mRatingBar;

	/**
	 * Default constructor that's needed by the GenericActivity framework.
	 */
	public VideoDetailsOps() {
	}

	@Override
	public void onConfiguration(Activity activity, boolean firstTimeIn) {

		final String time = firstTimeIn ? "first time" : "second+ time";

		Log.d(TAG, "onConfiguration() called the " + time + " with activity = "
				+ activity);

		// (Re)set the mActivity WeakReference.
		mActivity = new WeakReference<>((VideoDetailsActivity) activity);

		if (firstTimeIn) {
			// Get the Application Context.
			mApplicationContext = activity.getApplicationContext();

			// Create VideoController that will mediate the
			// communication between Server and Android Storage.
			mVideoController = new VideoController(mApplicationContext);
		}

	}

	public void setVideoRating(Video video) {
		mAsyncTask = new GenericAsyncTask<>(this);
		mAsyncTask.execute(video);
	}

	public Boolean checkVideoInLocalStorage(Video mVideo) {

		final String TITLE_SELECTION = VideoEntry.COLUMN_TITLE + " = ?";

		Cursor cursor = mApplicationContext.getContentResolver().query(
				VideoEntry.CONTENT_URI, null, TITLE_SELECTION,
				new String[] { mVideo.getTitle() }, null);

		if (cursor != null && cursor.moveToFirst()) {
			Log.v(TAG, "Cursor not null and has first item");

			List<Video> videoList = new ArrayList<Video>();

			cursor.moveToFirst();
			do {
				videoList.add(getVideoDataFromCursor(cursor));
			} while (cursor.moveToNext());

			return true;

		} else {
			return false;
		}
	}

	public void playVideo(Video video) {
		Cursor cursor = VideoGalleryUtils.getVideoByTitle(mApplicationContext,
				video.getTitle());

		if (cursor == null || !cursor.moveToFirst())
			return;

		int cursorSize = cursor.getCount();

		cursor.moveToFirst();

		int id = cursor.getInt(cursor
				.getColumnIndex(MediaStore.MediaColumns._ID));
		String title = cursor.getString(cursor
				.getColumnIndex(MediaStore.Video.VideoColumns.TITLE));
		String contentType = cursor.getString(cursor
				.getColumnIndex(MediaStore.MediaColumns.MIME_TYPE));
		String data = cursor.getString(cursor
				.getColumnIndex(MediaStore.Video.VideoColumns.DATA));

		Uri videoUri = Uri.parse(data);
		Intent intent = new Intent(Intent.ACTION_VIEW, videoUri);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setDataAndType(videoUri, contentType);
		mApplicationContext.startActivity(intent);
	}

	public void downloadVideo(Video video) {
		mApplicationContext.startService(DownloadVideoService.makeIntent(
				mApplicationContext, video.getId()));

		ContentValues values = new ContentValues();
		values.put(VideoEntry.COLUMN_TITLE, video.getTitle());
		values.put(VideoEntry.COLUMN_DURATION, video.getDuration());
		values.put(VideoEntry.COLUMN_CONTENT_TYPE, video.getContentType());
		values.put(VideoEntry.COLUMN_DATA_URL, video.getDataUrl());
		values.put(VideoEntry.COLUMN_STAR_RATING, video.getRating());

		mApplicationContext.getContentResolver().insert(VideoEntry.CONTENT_URI,
				values);

	}

	public Video doInBackground(Video... params) {
		Video video = mVideoController.setVideoRating(params[0].getId(),
				params[0].getRating());
		return video;
	}

	@Override
	public void onPostExecute(Video result) {
		if (result == null)
			return;

		// If the object was found, display the results.
		mActivity.get().setViewFields(result);

		// Indicate we're done with the AsyncTask.
		mAsyncTask = null;
	}

	private Video getVideoDataFromCursor(Cursor data) {
		if (data == null)
			return null;
		else {
			// Obtain data from the first row.
			final long id = data.getLong(data.getColumnIndex(VideoEntry._ID));
			final String title = data.getString(data
					.getColumnIndex(VideoEntry.COLUMN_TITLE));
			final long duration = data.getLong(data
					.getColumnIndex(VideoEntry.COLUMN_DURATION));
			final String contentType = data.getString(data
					.getColumnIndex(VideoEntry.COLUMN_CONTENT_TYPE));
			final String dataUrl = data.getString(data
					.getColumnIndex(VideoEntry.COLUMN_DATA_URL));
			final float rating = data.getLong(data
					.getColumnIndex(VideoEntry.COLUMN_STAR_RATING));

			return new Video(id, title, duration, contentType, dataUrl, rating);
		}
	}

}
