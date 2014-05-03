package rp3.data.entity;

import java.util.Date;

import rp3.app.BaseActivity;
import rp3.app.BaseFragment;
import rp3.app.BaseListFragment;
import rp3.core.R;
import rp3.data.Message;
import rp3.data.MessageCollection;
import rp3.db.sqlite.DataBase;
import rp3.util.Convert;
import rp3.util.Format;
import android.content.ContentValues;
import android.content.Context;

public abstract class EntityBase<T> {
		
	public final static int ACTION_INSERT = -1;
	public final static int ACTION_UPDATE = -2;
	public final static int ACTION_APPROVE = -3;
	public final static int ACTION_DELETE = -4;
	public final static int ACTION_NULLIFY = -5;
	public final static int ACTION_DEFAULT = 0;
	
	public EntityBase(){
	}
	
	private OnEntityCheckerListener< T > onEntityCheckerListener;
	private ContentValues contentValues;
	private MessageCollection messageCollection;
	private int currentAction = ACTION_DEFAULT;	
	private Context context;
	
	public abstract long getID();
	
	public abstract void setID(long id);	
	
	public abstract boolean isAutoGeneratedId();
	
	public abstract String getTableName();
	
	public abstract void setValues();
		
	protected void onBeforeInsert(){		
	}
	
	protected void onBeforeUpdate(){		
	}
	
	protected void onBeforeDelete(){		
	}
	

	public void setContext(Context c) {
		context	= c;
	}
	
	@SuppressWarnings("unchecked")
	public void setContext(BaseFragment c) {
		context = c.getContext();
		onEntityCheckerListener = (OnEntityCheckerListener<T>)c;
	}
	
	@SuppressWarnings("unchecked")
	public void setContext(BaseListFragment c) {
		context = c.getContext();
		onEntityCheckerListener = (OnEntityCheckerListener<T>)c;
	}
	
	@SuppressWarnings("unchecked")
	public void setContext(BaseActivity c) {
		context = c;
		onEntityCheckerListener = (OnEntityCheckerListener<T>)c;
	}
	
	public void setAction(int actionId){
		currentAction = actionId;
	}
	
	public int getAction(){
		return currentAction;
	}
	
	public void setValidationListener(OnEntityCheckerListener<T> l){
		onEntityCheckerListener = l;
	}
	
	protected ContentValues getValues(){
		if(contentValues==null) contentValues = new ContentValues();
		return contentValues;
	}
	
	public MessageCollection getMessages(){
		if(messageCollection==null) messageCollection = new MessageCollection();
		return messageCollection;
	}
	
	protected void clearValues(){
		getValues().clear();
	}
	
	protected void setValue(String key, Double value){
		getValues().put(key, value);
	}
	
	protected void setValue(String key, Integer value){
		getValues().put(key, value);
	}
	
	protected void setValue(String key, Short value){
		getValues().put(key, value);
	}
	
	protected void setValue(String key, Float value){
		getValues().put(key, value);
	}
	
	protected void setValue(String key, String value){		
		getValues().put(key, value);
	}
	
	protected void setValue(String key, Long value){
		getValues().put(key, value);
	}
	
	protected void setValue(String key, boolean value){
		getValues().put(key, Format.getDataBaseBoolean(value) );
	}
	
	protected void setValue(String key, Date value){
		getValues().put(key, Convert.getTicksFromDate(value) );
	}
	
	public static <T> boolean insert(DataBase db, EntityBase<T> e){
		return e.insert(db);
	}
	
	public static <T> boolean insert(DataBase db, EntityBase<T> e, int actionId){
		return e.insert(db,actionId);
	}
	
	public void initializeMessages(){
		getMessages().clear();
	}
	
	public boolean isValid(){
		return !getMessages().hasErrorMessage();
	}
	
	public void addErrorValidation(int key, String message, String title){
		getMessages().addMessage(message,title,Message.ERROR_TYPE, key);
	}

	public void addErrorValidation(int key, String message){
		getMessages().addMessage(message,null,Message.ERROR_TYPE, key);
	}
	
	public void addRequiredErrorValidation(int key){
		getMessages().addMessage(context.getText(R.string.validation_field_required).toString(),null,Message.ERROR_TYPE, key);
	}
	
	public void addErrorValidation(int key, int messageResId, int titleResId){
		getMessages().addMessage( context.getText(messageResId).toString(),
				context.getText(titleResId).toString(),
				Message.ERROR_TYPE, key);
	}

	public void addErrorValidation(int key, int messageResId){
		getMessages().addMessage(
				context.getText(messageResId).toString(),
				null,Message.ERROR_TYPE, key);
	}
	
	protected void prepareInsert(int actionId){
		setAction(actionId);
		clearValues();
		initializeMessages();
		onBeforeInsert();				
		setValues();
		executeValidate();
	}
	
	protected boolean insert(DataBase db){			
		return insert(db, ACTION_INSERT);
	}
	
	protected boolean insert(DataBase db, int actionId){			
		setAction(actionId);
		prepareInsert(actionId);
		return executeInsert(db);
	}
	
	protected boolean executeInsert(DataBase db){
		if(isValid())
			insertDb(db);
		return false;
	}
	
	protected boolean insertDb(DataBase db){
		boolean result = db.insert(getTableName(), getValues()) != 0;
		if(result && isAutoGeneratedId())
			setID(db.getLongLastInsertRowId());
		return result;
	}
	
	public static <T> boolean update(DataBase db, EntityBase<T> e){		
		return e.update(db);
	}
	
	public static <T> boolean update(DataBase db, EntityBase<T> e, int actionId){		
		return e.update(db, actionId);
	}
	
	@SuppressWarnings("unchecked")
	protected void executeValidate(){
		initializeMessages();
		validate();
		if(onEntityCheckerListener!=null){
			if(isValid())
				onEntityCheckerListener.onEntityValidationSuccess((T)this);
			else{
				MessageCollection mc = new MessageCollection();
				for(Message m: getMessages().getMessages()){
					if(m.getMessageType() == Message.ERROR_TYPE){
						onEntityCheckerListener.onEntityItemValidationFailed(m,(T)this);
						mc.addMessage(m);
					}
				}
				onEntityCheckerListener.onEntityValidationFailed(mc, (T)this);
			}
		}
	}

	protected void prepareUpdate(int actionId){
		setAction(actionId);
		clearValues();
		initializeMessages();
		onBeforeUpdate();			
		setValues();
		executeValidate();	
	}
	
	protected boolean update(DataBase db, int actionId){
		setAction(actionId);
		prepareUpdate(actionId);		
		return executeUpdate(db);		
	}
	
	protected boolean update(DataBase db){
		return update(db, ACTION_UPDATE);
	}
	
	protected boolean executeUpdate(DataBase db){
		if(isValid()){
			return updateDb(db);
		}		 
		return false;	
	}
	
	protected boolean updateDb(DataBase db){
		return db.update(getTableName(), getValues(), getID()) != 0;		
	}
	
	protected void prepareDelete(int actionId){
		setAction(actionId);
		onBeforeDelete();
		executeValidate();
	}
	
	public static <T> boolean delete(DataBase db, EntityBase<T> e){		
		return e.delete(db);
	}
	
	public static <T> boolean delete(DataBase db, EntityBase<T> e, int actionId){		
		return e.delete(db, actionId);
	}
	
	public boolean delete(DataBase db){
		return delete(db, ACTION_DELETE);
	}
	
	public boolean delete(DataBase db, int actionId){
		setAction(actionId);
		prepareDelete(actionId);
		return executeDelete(db);
	}
	
	protected boolean executeDelete(DataBase db){
		if(isValid()){
			return deleteDb(db);
		}
		return false;
	}
	
	protected boolean deleteDb(DataBase db){
		return db.delete(this.getTableName(), this.getID() ) != 0;
	}
	
	public boolean validate(){
		return true;
	}
	
	public static long deleteAll(DataBase db, String tableName){
		return db.delete(tableName);
	}
	
	public static long deleteAll(DataBase db, String tableName, boolean truncateAutIncrementId){
		return db.delete(tableName, truncateAutIncrementId);
	}
		
}