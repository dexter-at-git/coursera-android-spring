package vandy.mooc.model.provider;

import java.util.Objects;

import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * This "Plain Ol' Java Object" (POJO) class represents data of interest
 * downloaded in Json from the Video Service via the VideoServiceProxy.
 */
public class Video implements Parcelable {
	/**
	 * Various fields corresponding to data downloaded in Json from the Video
	 * WebService.
	 */
	private long id;
	private String title;
	private long duration;
	private String contentType;
	private float averageRating;
	private int ratingCount;

	/**
	 * Stores the path to stream the video from.
	 * 
	 * @JsonIgnore is used to completely exclude a member from the process of
	 *             serialization and de-serialization.
	 */
	@JsonIgnore
	private String dataUrl;

	/**
	 * No-op constructor
	 */
	public Video() {
	}

	/**
	 * Constructor that initializes title, duration and contentType.
	 */
	public Video(String title, long duration, String contentType) {
		this.title = title;
		this.duration = duration;
		this.contentType = contentType;
	}

	/**
	 * Constructor that initializes all the fields of interest.
	 */
	public Video(long id, String title, long duration, String contentType,
			String dataUrl, float rating) {
		this.id = id;
		this.title = title;
		this.duration = duration;
		this.contentType = contentType;
		this.dataUrl = dataUrl;
		this.averageRating = rating;
	}

	/*
	 * Getters and setters to access Video.
	 */

	/**
	 * Get the Id of the Video.
	 * 
	 * @return id of video
	 */
	public long getId() {
		return id;
	}

	/**
	 * Get the Video by Id
	 * 
	 * @param id
	 */
	public void setId(long id) {
		this.id = id;
	}

	/**
	 * Get the Title of Video.
	 * 
	 * @return title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * Set the Title of Video.
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * Get the Duration of Video.
	 * 
	 * @return Duration of Video.
	 */
	public long getDuration() {
		return duration;
	}

	/**
	 * Set the Duration of Video.
	 */
	public void setDuration(long duration) {
		this.duration = duration;
	}

	/**
	 * Get the DataUrl of Video
	 * 
	 * @return dataUrl of Video
	 */
	@JsonProperty
	public String getDataUrl() {
		return dataUrl;
	}

	/**
	 * Set the DataUrl of the Video.
	 */
	@JsonIgnore
	public void setDataUrl(String dataUrl) {
		this.dataUrl = dataUrl;
	}

	/**
	 * Get ContentType of Video.
	 * 
	 * @return contentType of Video.
	 */
	public String getContentType() {
		return contentType;
	}

	/**
	 * Set the ContentType of Video.
	 */
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
	
	public float getRating() {
		return averageRating;
	}

	public void setRating(float averageRating) {
		this.averageRating = averageRating;
	}

	
	public int getRatingCount() {
		return ratingCount;
	}

	public void setRatingCount(int ratingCount) {
		this.ratingCount = ratingCount;
	}

	/**
	 * @return the textual representation of Video object.
	 */
	@Override
	public String toString() {
		return "{" + "Id: " + id + ", " + "Title: " + title + ", "
				+ "Duration: " + duration + ", " + "ContentType: "
				+ contentType + ", " + "Data URL: " + dataUrl + "}";
	}

	/**
	 * @return an Integer hash code for this object.
	 */
	@Override
	public int hashCode() {
		return Objects.hash(getTitle(), getDuration());
	}

	/**
	 * @return Compares this Video instance with specified Video and indicates
	 *         if they are equal.
	 */
	@Override
	public boolean equals(Object obj) {
		return (obj instanceof Video)
				&& Objects.equals(getTitle(), ((Video) obj).getTitle())
				&& getDuration() == ((Video) obj).getDuration();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(id);
		dest.writeString(title);
		dest.writeFloat(averageRating);
		dest.writeInt(ratingCount);
		dest.writeString(contentType);
		dest.writeLong(duration);
		dest.writeString(dataUrl);

	}

	/**
	 * Private constructor provided for the CREATOR interface, which is used to
	 * de-marshal an WeatherData from the Parcel of data.
	 * <p>
	 * The order of reading in variables HAS TO MATCH the order in
	 * writeToParcel(Parcel, int)
	 *
	 * @param in
	 */
	private Video(Parcel in) {
		id = in.readLong();
		title = in.readString();
		averageRating = in.readFloat();
		ratingCount = in.readInt();
		contentType = in.readString();
		duration = in.readLong();
		dataUrl = in.readString();
	}

	public static final Parcelable.Creator<Video> CREATOR = new Parcelable.Creator<Video>() {
		public Video createFromParcel(Parcel in) {
			return new Video(in);
		}

		@Override
		public Video[] newArray(int size) {
			// TODO Auto-generated method stub
			return new Video[size];
		}

	};
}
