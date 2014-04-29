package rp3.app;

import java.util.List;

import rp3.configuration.Configuration;
import rp3.core.R;
import rp3.data.Constants;
import rp3.data.Message;
import rp3.data.MessageCollection;
import rp3.data.entity.OnEntityCheckerListener;
import rp3.db.sqlite.DataBase;
import rp3.db.sqlite.DataBaseService;
import rp3.db.sqlite.DataBaseServiceHelper;
import rp3.util.ViewUtils;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.util.AttributeSet;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ProgressBar;
import android.widget.SpinnerAdapter;

public class BaseActivity extends FragmentActivity implements DataBaseService,
		LoaderCallbacks<Cursor>, OnEntityCheckerListener<Object>  {

	protected Class<? extends SQLiteOpenHelper> dataBaseClass;
	protected Context context;
	private DataBase db;
	private int closeResourceOn = Constants.CLOSE_RESOURCES_ON_STOP;
	private boolean isRestoreInstance = false;
	private ProgressBar loadingView;
	private View rootView;
	private int currentDialogId;
	private int menuResource; 
	private FragmentTransaction fragmentTransaction;
	private boolean inFragmentTransaction;
	
	public BaseActivity() {
		this.context = this ;		
	}

	public boolean isRestoreInstance() {
		return isRestoreInstance;
	}

	public void setContentView(int layoutResID, int menuResID){
		setContentView(layoutResID);
		menuResource = menuResID;
	}
	
	public FragmentManager getCurrentFragmentManager(){
		return getSupportFragmentManager();
	}
	
	public LoaderManager getCurrentLoaderManager(){
		return getSupportLoaderManager();
	}
	
	public void setFragment(int id, Fragment fragment){
		if(inFragmentTransaction){
			fragmentTransaction.replace(id, fragment);
		}
		else{
			getCurrentFragmentManager().beginTransaction().replace(id, fragment).commit();
		}
	}
	
	public void beginSetFragment(){
		inFragmentTransaction = true;
		fragmentTransaction = getCurrentFragmentManager().beginTransaction();
	}
	
	public void endSetFragment(){
		inFragmentTransaction = false;
		fragmentTransaction.commit();
		fragmentTransaction = null;
	}
	
	public void setFragments(int[] ids, Fragment[] fragments){
		FragmentTransaction ft = getCurrentFragmentManager().beginTransaction();
		for(int i = 0; i < ids.length; i ++)
			ft.replace(ids[i], fragments[0]);
		ft.commit();
	}
	
	public View getRootView() {
		if (rootView == null)
			rootView = getWindow().getDecorView();
		return rootView;
	}	
	
	public void showDialogFragment(DialogFragment f, String tagName) {
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		Fragment prev = getSupportFragmentManager().findFragmentByTag(tagName);
		if (prev != null) {
			ft.remove(prev);
		}
		ft.addToBackStack(null);
		f.show(ft, tagName);
	}

	private ProgressBar getLoadingView() {
		if (loadingView == null) {
			if (getRootView() != null) {
				loadingView = (ProgressBar) getRootView().findViewById(
						R.id.loading);
			}
		}
		return loadingView;
	}

	public void showDefaultLoading() {
		if (getLoadingView() != null) {
			getLoadingView().setVisibility(View.VISIBLE);
		}
	}

	public void hideDefaultLoading() {
		if (getLoadingView() != null) {
			getLoadingView().setVisibility(View.GONE);
		}
	}
	
	public void setDataBaseParameters(Class<? extends SQLiteOpenHelper> dataBase) {
		dataBaseClass = dataBase;
	}

	public void setDataBaseParameters(Class<? extends SQLiteOpenHelper> dataBase,
			int closeResourceOn) {
		dataBaseClass = dataBase;
		this.closeResourceOn = closeResourceOn;
	}

	@Override
	public void setDataBaseParameters(Context c,
			Class<? extends SQLiteOpenHelper> dataBase) {
		dataBaseClass = dataBase;
		context = c;
	}

	public DataBase getDataBase() {
		if (db == null)
			db = DataBaseServiceHelper.getWritableDatabase(context,
					dataBaseClass);
		return db;
	}

	public void closeDataBase() {
		db.close();
		db = null;
	}

	public void setDataBase(DataBase dataBase) {
		db = dataBase;
	}

	public void closeDataBaseResources() {
		DataBaseServiceHelper.closeResources(this);
	}

	@Override
	public boolean IsActiveDataBase() {
		return db != null;
	}

	@Override
	public Class<? extends SQLiteOpenHelper> getDataBaseClass() {
		return dataBaseClass;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Configuration.TryInitializeConfiguration(this.context, getDataBaseClass());
		if (savedInstanceState != null)
			isRestoreInstance = true;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {		
		if(menuResource!=0) getMenuInflater().inflate(menuResource, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public View onCreateView(View parent, String name, Context context,
			AttributeSet attrs) {
		View r = super.onCreateView(parent, name, context, attrs);
		
		return r;
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		if (closeResourceOn == Constants.CLOSE_RESOURCES_ON_PAUSE)
			closeDataBaseResources();
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (closeResourceOn == Constants.CLOSE_RESOURCES_ON_STOP)
			closeDataBaseResources();
	}

	/* Inline Dialog Confirmation */

	private void setInlineDialog() {
		if (getRootView().findViewById(R.id.base_confirmation_dialog) != null) {

			setButtonClickListener(R.id.button_positive_confirmation,
					new View.OnClickListener() {

						@Override
						public void onClick(View arg0) {
							onPositiveConfirmation(currentDialogId);
						}
					});

			setButtonClickListener(R.id.button_negative_confirmation,
					new View.OnClickListener() {

						@Override
						public void onClick(View arg0) {
							onNegativeConfirmation(currentDialogId);
						}
					});

		}
	}

	public void showDialogConfirmation(int id, int message) {
		showDialogConfirmation(id, message, message);
	}

	public void showDialogConfirmation(final int id, int message, int title) {
		currentDialogId = id;

		if (getRootView().findViewById(R.id.action_group) != null)
			setViewVisibility(R.id.action_group, View.GONE);

		if (getRootView().findViewById(R.id.base_confirmation_dialog) != null) {
			setInlineDialog();
			setViewVisibility(R.id.base_confirmation_dialog, View.VISIBLE);
			setTextViewText(R.id.textView_dialog_message, getText(message)
					.toString());
		} else {
			Builder dialog = new AlertDialog.Builder(this)
					.setTitle(title)
					.setMessage(message)
					.setPositiveButton(R.string.confirmation_positive,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface arg0,
										int arg1) {
									onPositiveConfirmation(id);
								}
							})
					.setNegativeButton(R.string.confirmation_negative,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface arg0,
										int arg1) {
									onNegativeConfirmation(id);
								}
							}).setCancelable(false);
			dialog.show();
		}
	}

	public void hideDialogConfirmation() {
		if (getRootView().findViewById(R.id.base_confirmation_dialog) != null)
			setViewVisibility(R.id.base_confirmation_dialog, View.GONE);
		if (getRootView().findViewById(R.id.action_group) != null)
			setViewVisibility(R.id.action_group, View.VISIBLE);
	}

	public void onPositiveConfirmation(int id) {
		hideDialogConfirmation();
	}

	public void onNegativeConfirmation(int id) {
		hideDialogConfirmation();
	}
	
	public void cancelAnimationTransition(){
		this.overridePendingTransition(0, 0);
	}

	/* View Set Extensions */

	public void setTextViewText(int id, String value) {
		ViewUtils.setTextViewText(getRootView(), id, value);
	}

	public void setTextViewCurrencyText(int id, double value) {
		ViewUtils.setTextViewCurrencyText(getRootView(), id, value);
	}

	public void setTextViewNumberText(int id, double value) {
		ViewUtils.setTextViewNumberText(getRootView(), id, value);
	}

	public void setButtonClickListener(int id, OnClickListener l) {
		ViewUtils.setButtonClickListener(getRootView(), id, l);
	}

	public void setImageButtonClickListener(int id, OnClickListener l) {
		ViewUtils.setImageButtonClickListener(getRootView(), id, l);
	}

	public void setListViewAdapter(int id, ListAdapter adapter) {
		ViewUtils.setListViewAdapter(getRootView(), id, adapter);
	}

	public void setSpinnerAdapter(int id, SpinnerAdapter adapter){
		ViewUtils.setSpinnerAdapter(getRootView(), id, adapter);
	}
	
	public int getSpinnerSelectedIntID(int id){
		return ViewUtils.getSpinnerSelectedIntID(getRootView(), id);
	}
	
	public long getSpinnerSelectedLongID(int id){
		return ViewUtils.getSpinnerSelectedLongID(getRootView(), id);
	}
	
	public void setSpinnerSimpleAdapter(int id, String columnName, Cursor c) {
		ViewUtils.setSpinnerSimpleAdapter(getRootView(), this, id, columnName, c);
	}
	
	public void setSpinnerSimpleAdapter(int id,List<Object> objects) {
		ViewUtils.setSpinnerSimpleAdapter(getRootView(), this, id, objects);
	}
	
	public void setSpinnerSimpleAdapter(int id,Object[] objects) {
		ViewUtils.setSpinnerSimpleAdapter(getRootView(), this, id, objects);
	}
	
	public void setListViewOnItemClickListener(int id,
			AdapterView.OnItemClickListener l) {
		ViewUtils.setListViewOnClickListener(getRootView(), id, l);
	}

	public String getTextViewString(int id) {
		return ViewUtils.getTextViewString(getRootView(), id);
	}

	public int getTextViewInt(int id) {
		return ViewUtils.getTextViewInt(getRootView(), id);
	}

	public double getTextViewDouble(int id) {
		return ViewUtils.getTextViewDouble(getRootView(), id);
	}

	public void setViewVisibility(int id, int visibility) {
		ViewUtils.setViewVisibility(getRootView(), id, visibility);
	}

	public int getViewVisibility(int id) {
		return ViewUtils.getViewVisibility(getRootView(), id);
	}
	
	public void setViewError(int id, String text){
		ViewUtils.setViewError(getRootView(),id,text);
	}
	
	public void setViewError(int id, Message m){
		ViewUtils.setViewError(getRootView(),id,m);
	}

	// public String getTextEditText(int id){
	// return ViewUtils.getTextViewText(getRootView(), id);
	// }

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return null;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor c) {
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
	}

	@Override
	public void onBackPressed() {
		if (getRootView().findViewById(R.id.base_confirmation_dialog) != null
				&& getRootView().findViewById(R.id.base_confirmation_dialog)
						.getVisibility() == View.VISIBLE) {
			hideDialogConfirmation();
		} else
			super.onBackPressed();
	}

	@Override
	public void onEntityValidationFailed(MessageCollection mc, Object e) {
	}

	@Override
	public void onEntityItemValidationFailed(Message m, Object e) {
	}

	@Override
	public void onEntityValidationSuccess(Object e) {
	}
}
