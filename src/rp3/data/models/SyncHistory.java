package rp3.data.models;

import java.util.Date;

import android.database.Cursor;

import rp3.data.entity.EntityBase;
import rp3.db.sqlite.Contract;
import rp3.db.sqlite.DataBase;
import rp3.util.CursorUtils;

public class SyncHistory extends EntityBase<SyncHistory> {

	private long id;
	private Date syncDate;
	private String notes;
	private String category;
	private int event;
	private String user;	
	
	@Override
	public long getID() {		
		return id;
	}

	@Override
	public void setID(long id) {
		this.id = id;
	}

	@Override
	public boolean isAutoGeneratedId() {		
		return true;
	}

	@Override
	public String getTableName() {		
		return Contract.SyncHistory.TABLE_NAME;
	}

	@Override
	public void setValues() {				
		setValue(Contract.SyncHistory.COLUMN_CATEGORY, category);
		setValue(Contract.SyncHistory.COLUMN_EVENT, event);
		setValue(Contract.SyncHistory.COLUMN_NOTES, notes);
		setValue(Contract.SyncHistory.COLUMN_SYNC_DATE, syncDate);
		setValue(Contract.SyncHistory.COLUMN_USER, user);
	}

	@Override
	public Object getValue(String key) {
		return null;
	}

	@Override
	public String getDescription() {	
		return category;
	}

	public Date getSyncDate() {
		return syncDate;
	}

	public void setSyncDate(Date syncDate) {
		this.syncDate = syncDate;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public int getEvent() {
		return event;
	}

	public void setEvent(int event) {
		this.event = event;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public static Date getLastSyncDate(DataBase db){
		return db.queryMaxDate(Contract.SyncHistory.TABLE_NAME, Contract.SyncHistory.COLUMN_SYNC_DATE);
	}
	
	public static Date getLastSyncDate(DataBase db, String category){
		return db.queryMaxDate(Contract.SyncHistory.TABLE_NAME, Contract.SyncHistory.COLUMN_SYNC_DATE,
				Contract.SyncHistory.COLUMN_CATEGORY + " = ?", category);
	}
	
	public static Date getLastSyncDate(DataBase db, int event){
		return db.queryMaxDate(Contract.SyncHistory.TABLE_NAME, Contract.SyncHistory.COLUMN_SYNC_DATE,
				Contract.SyncHistory.COLUMN_EVENT + " = ?", event);
	}
	
	public static Date getLastSyncDate(DataBase db, String category, int event){
		return db.queryMaxDate(Contract.SyncHistory.TABLE_NAME, Contract.SyncHistory.COLUMN_SYNC_DATE,
				Contract.SyncHistory.COLUMN_EVENT + " = ? AND " +
				Contract.SyncHistory.COLUMN_CATEGORY + " = ?", new String[] {String.valueOf(event), category});
	}
	
	public static SyncHistory getSyncHistory(DataBase db, long id){
		SyncHistory s = null;
		Cursor c = db.query(Contract.SyncHistory.TABLE_NAME, 
				new String[]{
					Contract.SyncHistory._ID,
					Contract.SyncHistory.COLUMN_CATEGORY,
					Contract.SyncHistory.COLUMN_EVENT,
					Contract.SyncHistory.COLUMN_NOTES,
					Contract.SyncHistory.COLUMN_SYNC_DATE,
					Contract.SyncHistory.COLUMN_USER
					},
				Contract.SyncHistory._ID + " = ? ", 
				id
				);
		
		if(c.moveToFirst()){
			s = new SyncHistory();
			s.setCategory( CursorUtils.getString(c, Contract.SyncHistory.FIELD_CATEGORY) );
			s.setSyncDate( CursorUtils.getDate(c, Contract.SyncHistory.FIELD_DATE) );
			s.setEvent( CursorUtils.getInt(c, Contract.SyncHistory.FIELD_EVENT) );
			s.setNotes( CursorUtils.getString(c, Contract.SyncHistory.FIELD_NOTES) );
			s.setUser( CursorUtils.getString(c, Contract.SyncHistory.FIELD_USER) );
			s.setID( CursorUtils.getLong(c, Contract.SyncHistory._ID) );
		}
					
		return s;		
	}
	
}