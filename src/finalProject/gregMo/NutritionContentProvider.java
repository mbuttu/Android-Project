package finalProject.gregMo;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

public class NutritionContentProvider extends ContentProvider {

	private NutritionDatabaseHelper db;
	public static final String PROVIDER_NAME = "cs.ecl.android.provider.nutrition";
	public static final String PERSONAL_INFO_TABLE = "personal_information";
	
	public static final Uri CONTENT_URI_PERSONAL = Uri.parse("content://" +
			PROVIDER_NAME + "/" + PERSONAL_INFO_TABLE );
	
	
	private static final int PERSONAL_INFO = 1;
	private static final int PERSONAL_INFO_ID = 2;
	
	//Used for matching in queries to know if to do an operation on whole table or for
	//only a specific record
	private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	static {
		uriMatcher.addURI(PROVIDER_NAME, PERSONAL_INFO_TABLE , PERSONAL_INFO);
		uriMatcher.addURI(PROVIDER_NAME, PERSONAL_INFO_TABLE + "/#", PERSONAL_INFO_ID);
	}
	
	//Create an instance of the database helper
	@Override
	public boolean onCreate()
	{
		db = new NutritionDatabaseHelper(getContext());
		return false;
	}
	
	@Override
	public int delete(Uri uri, String whereClause, String[] whereArgs) {
		int numberRowsAffected = 0;
		SQLiteDatabase nutritionDB = db.getWritableDatabase();
		switch (uriMatcher.match(uri))
		{
		case PERSONAL_INFO:
			numberRowsAffected = nutritionDB.delete(PersonalInformationTable.TABLE_NAME, whereClause, whereArgs);
			break;
		case PERSONAL_INFO_ID:
			String id = uri.getLastPathSegment();
			numberRowsAffected = nutritionDB.delete(PersonalInformationTable.TABLE_NAME,
					PersonalInformationTable.COLUMN_ID + " = " + id 
					+ (!TextUtils.isEmpty(whereClause) ? 
							" AND (" + whereClause + ")" : "") , whereArgs);
			break;
			default:
				throw new IllegalArgumentException("Unknown URI " + uri);
		}
		
		//in case we had an observer?
		getContext().getContentResolver().notifyChange(uri, null);
		return numberRowsAffected;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		long id = 0;
		SQLiteDatabase nutritionDB = db.getWritableDatabase();
		if ( uriMatcher.match(uri) == PERSONAL_INFO )
		{
			id = nutritionDB.insert(PersonalInformationTable.TABLE_NAME, "" , values);
			if ( id > 0)
			{
				Uri _uri = ContentUris.withAppendedId(CONTENT_URI_PERSONAL, id);
				getContext().getContentResolver().notifyChange(_uri, null);
				return _uri;
			}
		}
		
		throw new SQLException("Failed to insert row into " + uri);
		
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		
		SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
		builder.setTables(PersonalInformationTable.TABLE_NAME);
		SQLiteDatabase nutritionDB = db.getWritableDatabase();
		
		if ( uriMatcher.match(uri) == PERSONAL_INFO_ID )
		{
			builder.appendWhere(PersonalInformationTable.COLUMN_ID + " = " + uri.getLastPathSegment());
		}
		if ( TextUtils.isEmpty(sortOrder))
		{
			sortOrder = "desc";
		}
		
		Cursor cl = builder.query(nutritionDB , projection, 
				selection, selectionArgs, null, null, null);
		cl.setNotificationUri(getContext().getContentResolver(), uri);
		return cl;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		
		int rowsUpdated = 0;
		SQLiteDatabase nutritionDB = db.getWritableDatabase();
		switch ( uriMatcher.match(uri))
		{
		case PERSONAL_INFO:
			rowsUpdated = nutritionDB.update(PersonalInformationTable.TABLE_NAME, 
					values, selection, selectionArgs);
			break;
		case PERSONAL_INFO_ID:
			rowsUpdated = nutritionDB.update(PersonalInformationTable.TABLE_NAME, values,
					PersonalInformationTable.COLUMN_ID + " = " + uri.getLastPathSegment()
					+ (!TextUtils.isEmpty(selection) ? " AND (" + selection + ")" : ""), 
					selectionArgs);
			break;
		default: 
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		
		getContext().getContentResolver().notifyChange(uri, null);
		return rowsUpdated;
		
	}

	@Override
	public String getType(Uri uri) {
		switch (uriMatcher.match(uri))
		{
		case PERSONAL_INFO:
			return "vnc.android.cursos.dir/vnd.ecl.soccer ";
		case PERSONAL_INFO_ID:
			return "vnd.android.cursor.item/vnd.ecl.soccer ";
		default:
			throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
	}
	
	
}