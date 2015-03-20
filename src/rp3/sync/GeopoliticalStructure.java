package rp3.sync;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.transport.HttpResponseException;

import rp3.connection.HttpConnection;
import rp3.connection.WebService;
import rp3.content.SyncAdapter;
import rp3.data.models.Contract;
import rp3.data.models.GeopoliticalStructureType;
import rp3.db.sqlite.DataBase;
import rp3.util.Convert;

public class GeopoliticalStructure {

	public static int executeSync(DataBase db){
        WebService webService = null;
        boolean lastPage = false;
        int page = 1;
		try
		{
            do {
                webService = new WebService("Core","GetGeopoliticalStructures");
                webService.addCurrentAuthToken();
                webService.addParameter("@page", page);
                webService.addParameter("@size", 1000);
                page++;
                lastPage = true;

                try {
                    webService.invokeWebService();
                } catch (HttpResponseException e) {
                    if(e.getStatusCode() == HttpConnection.HTTP_STATUS_UNAUTHORIZED)
                        return SyncAdapter.SYNC_EVENT_AUTH_ERROR;
                    return SyncAdapter.SYNC_EVENT_HTTP_ERROR;
                } catch (Exception e) {
                    return SyncAdapter.SYNC_EVENT_ERROR;
                }

                JSONArray types = webService.getJSONArrayResponse();

                for(int i=0; i < types.length(); i++){

                    try {
                        JSONObject type = types.getJSONObject(i);
                        rp3.data.models.GeopoliticalStructureType modelType = new GeopoliticalStructureType();
                        modelType.setName(type.getString("N"));
                        modelType.setID(type.getLong("Id"));
                        modelType.setLevelStructure(type.getInt("Lev"));

                        rp3.data.models.GeopoliticalStructureType.insert(db, modelType);

                        JSONArray strs = type.getJSONArray("Content");
                        if(strs.length() != 0)
                            lastPage = false;


                        for(int j=0; j < strs.length(); j++){
                            JSONObject str = strs.getJSONObject(j);
                            rp3.data.models.GeopoliticalStructure modelStr = new rp3.data.models.GeopoliticalStructure();
                            modelStr.setID(str.getLong("Id"));
                            modelStr.setIsoCode(str.getString("Iso"));
                            modelStr.setGeopoliticalStructureTypeId(str.getInt("TId"));
                            modelStr.setLatitude( str.isNull("La") ? null : Double.parseDouble(str.getString("La")));
                            modelStr.setLongitude( str.isNull("Lo") ? null : Double.parseDouble(str.getString("Lo")));
                            modelStr.setName(str.getString("N"));
                            modelStr.setParents(str.getString("P"));
                            modelStr.setParentGeopoliticalStructureId(str.isNull("PId") ? 0 : str.getLong("PId") );

                            rp3.data.models.GeopoliticalStructure.insert(db, modelStr);
                        }



                    } catch (JSONException e) {

                        return SyncAdapter.SYNC_EVENT_ERROR;
                    }


                }
            }while(!lastPage);

		}finally{
			webService.close();
		}
		
		return SyncAdapter.SYNC_EVENT_SUCCESS;		
	}
	
	public static int executeSyncLastUpdate(DataBase db, long time){
        WebService webService = null;
        boolean lastPage = false;
        int page = 1;
		try
		{
            do {
                webService = new WebService("Core","GetGeopoliticalStructures");
                webService.addCurrentAuthToken();
                webService.addParameter("@lastUpdate", time);
                webService.addParameter("@page", page);
                webService.addParameter("@size", 1000);
                page++;
                lastPage = true;

                try {
                    webService.invokeWebService();
                } catch (HttpResponseException e) {
                    if(e.getStatusCode() == HttpConnection.HTTP_STATUS_UNAUTHORIZED)
                        return SyncAdapter.SYNC_EVENT_AUTH_ERROR;
                    return SyncAdapter.SYNC_EVENT_HTTP_ERROR;
                } catch (Exception e) {
                    return SyncAdapter.SYNC_EVENT_ERROR;
                }

                JSONArray types = webService.getJSONArrayResponse();

                for(int i=0; i < types.length(); i++){

                    try {
                        JSONObject type = types.getJSONObject(i);
                        rp3.data.models.GeopoliticalStructureType modelType = new GeopoliticalStructureType();
                        modelType.setName(type.getString("N"));
                        modelType.setID(type.getLong("Id"));
                        modelType.setLevelStructure(type.getInt("Lev"));

                        rp3.data.models.GeopoliticalStructureType.insert(db, modelType);

                        JSONArray strs = type.getJSONArray("Content");
                        if(strs.length() != 0)
                            lastPage = false;

                        for(int j=0; j < strs.length(); j++){
                            JSONObject str = strs.getJSONObject(j);
                            rp3.data.models.GeopoliticalStructure modelStr = new rp3.data.models.GeopoliticalStructure();
                            modelStr.setID(str.getLong("Id"));
                            modelStr.setIsoCode(str.getString("Iso"));
                            modelStr.setGeopoliticalStructureTypeId(str.getInt("TId"));
                            modelStr.setLatitude( str.isNull("La") ? null : Double.parseDouble(str.getString("La")));
                            modelStr.setLongitude( str.isNull("Lo") ? null : Double.parseDouble(str.getString("Lo")));
                            modelStr.setName(str.getString("N"));
                            modelStr.setParents(str.getString("P"));
                            modelStr.setParentGeopoliticalStructureId(str.isNull("PId") ? 0 : str.getLong("PId") );

                            rp3.data.models.GeopoliticalStructure.insert(db, modelStr);
                        }



                    } catch (JSONException e) {

                        return SyncAdapter.SYNC_EVENT_ERROR;
                    }


                }
            }while(!lastPage);

		}finally{
			webService.close();
		}
		
		return SyncAdapter.SYNC_EVENT_SUCCESS;		
	}
}
