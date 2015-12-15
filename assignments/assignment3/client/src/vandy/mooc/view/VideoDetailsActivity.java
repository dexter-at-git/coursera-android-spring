package vandy.mooc.view;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.RatingBar.OnRatingBarChangeListener;
import vandy.mooc.R;
import vandy.mooc.common.GenericActivity;
import vandy.mooc.common.Utils;
import vandy.mooc.model.provider.Video;
import vandy.mooc.presenter.VideoDetailsOps;
import vandy.mooc.presenter.VideoOps;
import vandy.mooc.utils.VideoGalleryUtils;
import vandy.mooc.view.ui.FloatingActionButton;
import vandy.mooc.view.ui.UploadVideoDialogFragment;

public class VideoDetailsActivity extends GenericActivity<VideoDetailsOps> {

	/**
	 * Custom Action used by Implicit Intent to call this Activity.
	 */
	public static final String ACTION_DISPLAY_VIDEO = "vandy.mooc.intent.action.DISPLAY_VIDEO";

	/**
	 * MIME_TYPE of Video Data
	 */
	public static final String TYPE_VIDEO = "parcelable/video";

	/**
	 * Key for the List of Video Data to be displayed
	 */
	public static final String KEY_VIDEO = "video";

	private Video mVideo;
	private TextView mTitle;
	private RatingBar mRating;
	private TextView mAvgRating;
	private TextView mRatingCount;
	private FloatingActionButton mDownloadVideoButton;
	private FloatingActionButton mPlayVideoButton;

	public static Intent makeIntent(Video videoData) {
		return new Intent(ACTION_DISPLAY_VIDEO).setType(TYPE_VIDEO).putExtra(
				KEY_VIDEO, videoData);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// Initialize the default layout.
		setContentView(R.layout.activity_video_details);

		// Get the intent that started this activity
		final Intent intent = getIntent();

		// Check whether it is correct intent type.
		if (intent.getType().equals(TYPE_VIDEO)) {
			// Get the Video Data from the Intent.
			mVideo = intent.getParcelableExtra(KEY_VIDEO);

			setViewFields(mVideo);
		} else {
			// Show error message.
			Utils.showToast(this, "Incorrect Data");
		}

		mRating.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_UP) {
					float touchPositionX = event.getX();
					float width = mRating.getWidth();
					float starsf = (touchPositionX / width) * 5.0f;

					Log.d(TAG, "onRatingChanged() rating " + starsf);

					int stars = (int) starsf + 1;
					mRating.setRating(starsf);

					mVideo.setRating(starsf);
					getOps().setVideoRating(mVideo);

					v.setPressed(false);
				}
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					v.setPressed(true);
				}

				if (event.getAction() == MotionEvent.ACTION_CANCEL) {
					v.setPressed(false);
				}

				return true;
			}
		});

		createDownloadButton();
		createPlayButton();

		// Call up to the special onCreate() method in
		// GenericActivity, passing in the VideoDetailsOps class to
		// instantiate and manage.
		super.onCreate(savedInstanceState, VideoDetailsOps.class);

		setButtonVisibility();
	}

	public void setViewFields(Video video) {
		mTitle = (TextView) findViewById(R.id.videoTitle);
		mTitle.setText(video.getTitle());

		mRating = (RatingBar) findViewById(R.id.videoRatingBar);
		mRating.setRating(video.getRating());

		mAvgRating = (TextView) findViewById(R.id.videoAvgRating);
		mAvgRating.setText(Float.toString(video.getRating()));

		mRatingCount = (TextView) findViewById(R.id.videoRatingCount);
		mRatingCount.setText(Integer.toString(video.getRatingCount()));
	}

	@SuppressWarnings("deprecation")
	private void createDownloadButton() {
		final DisplayMetrics metrics = getResources().getDisplayMetrics();
		final int position = (metrics.widthPixels / 4) + 5;

		mDownloadVideoButton = new FloatingActionButton.Builder(this)
				.withDrawable(
						getResources().getDrawable(R.drawable.ic_download))
				.withButtonColor(getResources().getColor(R.color.theme_primary))
				.withGravity(Gravity.TOP | Gravity.END)
				.withMargins(0, 0, position, 0).create();

		// Show the UploadVideoDialog Fragment when user clicks the
		// Button.
		mDownloadVideoButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				getOps().downloadVideo(mVideo);
				setButtonVisibility();
			}
		});
	}

	@SuppressWarnings("deprecation")
	private void createPlayButton() {
		final DisplayMetrics metrics = getResources().getDisplayMetrics();
		final int position = (metrics.widthPixels / 4) + 5;

		mPlayVideoButton = new FloatingActionButton.Builder(this)
				.withDrawable(getResources().getDrawable(R.drawable.ic_play))
				.withButtonColor(getResources().getColor(R.color.theme_primary))
				.withGravity(Gravity.TOP | Gravity.END)
				.withMargins(position, 0, 0, 0).create();

		// Show the UploadVideoDialog Fragment when user clicks the
		// Button.
		mPlayVideoButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				getOps().playVideo(mVideo);
			}
		});
	}

	private void setButtonVisibility() {
		Boolean alreadyDownloaded = getOps().checkVideoInLocalStorage(mVideo);

		if (alreadyDownloaded) {
			mDownloadVideoButton.setVisibility(View.INVISIBLE);
			mPlayVideoButton.setVisibility(View.VISIBLE);
		} else {
			mDownloadVideoButton.setVisibility(View.VISIBLE);
			mPlayVideoButton.setVisibility(View.INVISIBLE);
		}
	}
}
