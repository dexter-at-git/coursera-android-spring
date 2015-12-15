package vandy.mooc.provider;

import vandy.mooc.provider.VideoContract.VideoEntry;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

/**
 * Content Provider to access Acronym Database.
 */
public class VideoProvider extends ContentProvider {
	/**
	 * Debugging tag used by the Android logger.
	 */
	private static final String TAG = VideoProvider.class.getSimpleName();

	/**
	 * Use AcronymDatabaseHelper to manage database creation and version
	 * management.
	 */
	private VideoDatabaseHelper mOpenHelper;

	/**
	 * The code that is returned when a URI for more than 1 items is matched
	 * against the given components. Must be positive.
	 */
	private static final int VIDEOS = 100;

	/**
	 * The code that is returned when a URI for exactly 1 item is matched
	 * against the given components. Must be positive.
	 */
	private static final int VIDEO = 101;

	/**
	 * The URI Matcher used by this content provider.
	 */
	private static final UriMatcher sUriMatcher = buildUriMatcher();

	/**
	 * Helper method to match each URI to the ACRONYM integers constant defined
	 * above.
	 * 
	 * @return UriMatcher
	 */
	private static UriMatcher buildUriMatcher() {
		// All paths added to the UriMatcher have a corresponding code
		// to return when a match is found. The code passed into the
		// constructor represents the code to return for the rootURI.
		// It's common to use NO_MATCH as the code for this case.
		final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);

		// For each type of URI that is added, a corresponding code is
		// created.
		matcher.addURI(VideoContract.CONTENT_AUTHORITY,
				VideoContract.PATH_VIDEO, VIDEOS);
		matcher.addURI(VideoContract.CONTENT_AUTHORITY,
				VideoContract.PATH_VIDEO + "/#", VIDEO);
		return matcher;
	}

	/**
	 * Hook method called when Database is created to initialize the Database
	 * Helper that provides access to the Acronym Database.
	 */
	@Override
	public boolean onCreate() {
		mOpenHelper = new VideoDatabaseHelper(getContext());
		return true;
	}

	/**
	 * Hook method called to handle requests for the MIME type of the data at
	 * the given URI. The returned MIME type should start with
	 * vnd.android.cursor.item for a single item or vnd.android.cursor.dir/ for
	 * multiple items.
	 */
	@Override
	public String getType(Uri uri) {
		// Use Uri Matcher to determine what kind of URI this is.
		final int match = sUriMatcher.match(uri);

		// Match the id returned by UriMatcher to return appropriate
		// MIME_TYPE.
		switch (match) {
		case VIDEOS:
			return VideoContract.VideoEntry.CONTENT_ITEMS_TYPE;
		case VIDEO:
			return VideoContract.VideoEntry.CONTENT_ITEM_TYPE;
		default:
			throw new UnsupportedOperationException("Unknown uri: " + uri);
		}
	}

	/**
	 * Hook method called to handle requests to insert a new row. As a courtesy,
	 * notifyChange() is called after inserting.
	 */
	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// Create and/or open a database that will be used for reading
		// and writing. Once opened successfully, the database is
		// cached, so you can call this method every time you need to
		// write to the database.
		final SQLiteDatabase db = mOpenHelper.getWritableDatabase();

		Uri returnUri;

		// Try to match against the path in a url. It returns the
		// code for the matched node (added using addURI), or -1 if
		// there is no matched node. If there's a match insert a new
		// row.
		switch (sUriMatcher.match(uri)) {
		case VIDEOS:
			// TODO+ - replace 0 with code that inserts a row in Table
			// and returns the row id.
			long id = db.insert(VideoEntry.TABLE_NAME, null, values);
			;

			// Check if a new row is inserted or not.
			if (id > 0)
				returnUri = VideoEntry.buildVideoUri(id);
			else
				throw new android.database.SQLException(
						"Failed to insert row into " + uri);
			break;
		default:
			throw new UnsupportedOperationException("Unknown uri: " + uri);
		}

		// Notifies registered observers that a row was inserted.
		getContext().getContentResolver().notifyChange(uri, null);
		return returnUri;
	}

	/**
	 * Hook method called to handle query requests from clients.
	 */
	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		Cursor retCursor;

		// Match the id returned by UriMatcher to query appropriate
		// rows.
		switch (sUriMatcher.match(uri)) {
		case VIDEOS:

			retCursor = mOpenHelper.getReadableDatabase().query(
					VideoEntry.TABLE_NAME, projection, selection,
					selectionArgs, null, null, sortOrder);
			break;
		default:
			throw new UnsupportedOperationException("Unknown uri: " + uri);
		}

		// Register to watch a content URI for changes.
		retCursor.setNotificationUri(getContext().getContentResolver(), uri);
		return retCursor;
	}

	/**
	 * Hook method called to handle requests to update one or more rows. The
	 * implementation should update all rows matching the selection to set the
	 * columns according to the provided values map. As a courtesy,
	 * notifyChange() is called after updating .
	 */
	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// Create and/or open a database that will be used for reading
		// and writing. Once opened successfully, the database is
		// cached, so you can call this method every time you need to
		// write to the database.
		final SQLiteDatabase db = mOpenHelper.getWritableDatabase();

		int rowsUpdated;

		Log.d(TAG, "update");
		// Try to match against the path in a uri. It returns the
		// code for the matched node (added using addURI), or -1 if
		// there is no matched node. If a match occurs update the
		// appropriate rows.
		switch (sUriMatcher.match(uri)) {
		case VIDEOS:
			// Updates the rows in the Database and returns no of rows
			// updated.
			rowsUpdated = db.update(VideoEntry.TABLE_NAME, values, selection,
					selectionArgs);
			break;
		default:
			throw new UnsupportedOperationException("Unknown uri: " + uri);
		}

		// Notifies registered observers that rows were updated.
		if (rowsUpdated != 0)
			getContext().getContentResolver().notifyChange(uri, null);
		return rowsUpdated;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		return 0;
	}

}
