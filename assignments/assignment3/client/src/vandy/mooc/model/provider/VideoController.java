package vandy.mooc.model.provider;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import retrofit.RestAdapter;
import retrofit.client.Response;
import retrofit.mime.TypedFile;
import vandy.mooc.common.Utils;
import vandy.mooc.model.provider.VideoStatus.VideoState;
import vandy.mooc.model.webdata.VideoServiceProxy;
import vandy.mooc.utils.Constants;
import vandy.mooc.utils.VideoGalleryUtils;
import android.content.Context;
import android.net.Uri;

/**
 * Mediates communication between the Video Service and the local storage on the
 * Android device.
 */
public class VideoController {
	/**
	 * Allows access to application-specific resources and classes.
	 */
	private Context mContext;

	/**
	 * Defines methods that access the Android MediaStore Video Content Provider
	 * and do CRUD operations on it.
	 */
	private AndroidVideoCache mAndroidVideoCache;

	/**
	 * Defines methods that communicate with the Video Service.
	 */
	private VideoServiceProxy mVideoServiceProxy;

	/**
	 * Constructor that initializes the VideoController.
	 * 
	 * @param context
	 */
	public VideoController(Context context) {
		// Store the Application Context.
		mContext = context;

		// Initialize the AndroidVideo cache.
		mAndroidVideoCache = new AndroidVideoCache(mContext);

		// Initialize the VideoServiceProxy.
		mVideoServiceProxy = new RestAdapter.Builder()
				.setEndpoint(Constants.SERVER_URL).build()
				.create(VideoServiceProxy.class);
	}

	/**
	 * Uploads the Video having the given Id. This Id is the Id of Video in
	 * Android Video Content Provider.
	 * 
	 * @param videoId
	 *            Id of the Video to be uploaded.
	 * 
	 * @return result of the video upload operation. True - If the Video is
	 *         successfully uploaded. False- If there was a failure while
	 *         Uploading Video.
	 */
	public boolean uploadVideo(long videoId, Uri videoUri) {
		// Get the Video from Android Video Content Provider having
		// the given Id.
		Video androidVideo = mAndroidVideoCache.getVideoById(videoId);

		// Check if any such Video exists in Android Video Content
		// Provider.
		if (androidVideo != null) {
			// Add the metadata of the Video to Server and get the
			// result Video that contains additional metadata
			// generated by Server.
			Video receivedVideo = mVideoServiceProxy.addVideo(androidVideo);

			// Check if the Server returns any Video metadata.
			if (receivedVideo != null) {
				// Prepare to Upload the Video data.

				// Get the path of video from videoUri.
				String filePath = VideoGalleryUtils.getPath(mContext, videoUri);

				// Create an instance of the file to be uploaded.
				File videoFile = new File(filePath);

				// Check the file size in MegaBytes.
				long size = videoFile.length() / Constants.MEGA_BYTE;

				// Check if the file size is less than the size of the
				// video that can be uploaded to the server.
				if (size < Constants.MAX_SIZE) {
					// Finally, upload the Video data to the server
					// and get the status of the uploaded video data.
					VideoStatus status = mVideoServiceProxy.setVideoData(
							receivedVideo.getId(),
							new TypedFile(receivedVideo.getContentType(),
									videoFile));

					// Check if the Status of the Video or not.
					if (status.getState() == VideoState.READY) {
						// Video successfully uploaded.
						return true;
					}
				} /* else */
				// @@ Show a toast indicating the video was too
				// large to upload.
				{
	            	Utils.showToast(mContext, "Uploading video size is greater than 50MB");
	        		return false;
				}
			}
		}
		// Error occured while uploading the video.
		return false;
	}

	/**
	 * Get the List of Videos from Server
	 * 
	 * @return the List of Videos from Server or null if there is failure in
	 *         getting the Videos.
	 */
	public List<Video> getVideoList() {
		return (ArrayList<Video>) mVideoServiceProxy.getVideoList();
	}

	public Video setVideoRating(long videoId, float rating) {

		Video video = mVideoServiceProxy.setVideoRating(videoId, rating);

		return video;
	}

	public Response downloadVideo(long videoId) {

			Response is = mVideoServiceProxy.downloadVideo(videoId);
				return is;
		

	}
}
