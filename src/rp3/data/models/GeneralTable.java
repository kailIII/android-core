package rp3.data.models;

import java.util.ArrayList;
import java.util.List;

import android.database.Cursor;

import rp3.data.entity.EntityBase;
import rp3.db.sqlite.DataBase;
import rp3.util.CursorUtils;

public class GeneralTable extends EntityBase<GeneralTable> {

	private long id;
	private String name;
	
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
		return false;
	}

	@Override
	public String getTableName() {		
		return Contract.GeneralTable.TABLE_NAME;
	}

	@Override
	public void setValues() {
		setValue(Contract.GeneralTable._ID, this.id);
		setValue(Contract.GeneralTable.COLUMN_NAME, this.name);
	}

	@Override
	public Object getValue(String key) {
		if(key.equals(Contract.GeneralTable.COLUMN_NAME))
			return this.name;
		else if(key.equals(Contract.GeneralTable._ID))
			return id;
		return null;
	}

	@Override
	public String getDescription() {		
		return this.name;
	}
	
	public String getName(){
		return this.name;
	}
	
	public void setName(String name){
		this.name = name;
	}
	
	private static GeneralTable getGeneralTable(Cursor c){
		GeneralTable table = new GeneralTable();
		table.setID( CursorUtils.getLong(c, Contract.GeneralTable._ID));
		table.setName( CursorUtils.getString(c, Contract.GeneralTable.COLUMN_NAME));
		return table;
	}
	
	public static List<GeneralTable> getGeneralTables(DataBase db){
		List<GeneralTable> gt = new ArrayList<GeneralTable>();
		
		Cursor c = db.query(Contract.GeneralTable.TABLE_NAME, new String[]{
				Contract.GeneralTable._ID,
				Contract.GeneralTable.COLUMN_NAME
		});
		
		if(c.moveToFirst()){
			do{
				gt.add(getGeneralTable(c));
			}while (c.moveToNext());
		}
		
		return gt;
	}

	public static GeneralTable getGeneralTable(DataBase db, long id){
		Cursor c = db.query(Contract.GeneralTable.TABLE_NAME, new String[]{
				Contract.GeneralTable._ID,
				Contract.GeneralTable.COLUMN_NAME
		}, Contract.GeneralTable._ID + " = ?", id );
		
		if(c.moveToFirst())
			return getGeneralTable(c);
		else
			return null;
	}
}
