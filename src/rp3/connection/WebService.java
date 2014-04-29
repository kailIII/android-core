package rp3.connection;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpResponseException;
import org.ksoap2.transport.HttpTransportSE;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Log;
import android.util.Xml;

import rp3.configuration.Configuration;
import rp3.configuration.WebServiceData;
import rp3.configuration.WebServiceDataMethod;

public class WebService {

	public static final String TYPE_SOAP = "SOAP";
	public static final String TYPE_REST = "REST";
	
	private String wsConfigurationName;		
	private WebServiceData wsData;
	private WebServiceDataMethod wsMethod;
	private List<WebServiceParameter> parameters;
	private String respJSONString;
	private Object respSoap;
	
	public WebService(){				
	}
	
	public WebService(String wsConfigurationName, String methodName){				
		setConfigurationName(wsConfigurationName, methodName);
	}	
	
	public void setConfigurationName(String wsConfigurationName, String methodName){		
		this.wsConfigurationName = wsConfigurationName;		
		wsData = Configuration.getWebServiceConfiguration().get(this.wsConfigurationName);
		wsMethod = wsData.getMethod(methodName);
	}
	
	public List<WebServiceParameter> getParameters(){
		if(parameters == null) parameters = new ArrayList<WebServiceParameter>();
		return parameters;
	}
	
	public void addParameter(String name, Object value){
		getParameters().add(new WebServiceParameter(name, value, value.getClass()));
	}
	
	public void addParameter(String name, Object value, Object valueType){
		getParameters().add(new WebServiceParameter(name, value, valueType, true));
	}
	
	public void addStringParameter(String name, String value){
		getParameters().add(new WebServiceParameter(name, value, String.class));
	}
	
	public void addBooleanParameter(String name, boolean value){
		getParameters().add(new WebServiceParameter(name, value, Boolean.class));
	}
	
	public void addIntParameter(String name, int value){
		getParameters().add(new WebServiceParameter(name, value, Integer.class));
	}
	
	public void addLongParameter(String name, long value){
		getParameters().add(new WebServiceParameter(name, value, Long.class));
	}
	
	public void addDoubleParameter(String name, double value){
		getParameters().add(new WebServiceParameter(name, value, Double.class));
	}
	
	public void addFloatParameter(String name, double value){
		getParameters().add(new WebServiceParameter(name, value, Float.class));
	}
	
	public void addFloatParameter(String name, Object value, Object valueType){
		getParameters().add(new WebServiceParameter(name, value, valueType));
	}
	
	public boolean getBooleanResponse(){
		return Boolean.parseBoolean(getStringResponse());
	}
	
	public long getLongResponse(){
		return Long.parseLong(getStringResponse());
	}
	
	public double getDoubleResponse(){
		return Double.parseDouble(getStringResponse());
	}
	
	public double getFloatResponse(){
		return Float.parseFloat(getStringResponse());
	}
	
	public int getIntegerResponse(){
		return Integer.parseInt(getStringResponse());
	}
	
	private String getStringResponse(){
		if(wsData.getType().equalsIgnoreCase(TYPE_SOAP)){
        	return getSoapPrimitiveResponse().getValue().toString();
        }else if(wsData.getType().equalsIgnoreCase(TYPE_REST)){
        	return respJSONString;
        } 
		return null;
	}
	
	public JSONArray getJSONArrayResponse(){
		try {
			return new JSONArray(respJSONString);
		} catch (JSONException e) {			
			e.printStackTrace();
		}
		return null;
	}
	
	public JSONObject getJSONObjectResponse(){
		try {
			return new JSONObject(respJSONString);
		} catch (JSONException e) {			
			e.printStackTrace();
		}
		return null;
	}
	
	public SoapPrimitive getSoapPrimitiveResponse(){
		return ((SoapPrimitive) respSoap);
	}
	
	public SoapObject getSoapObjectResponse(){		
        return ((SoapObject) respSoap);
	}
	
	public XmlPullParser getXmlPullParserResponse(){
		XmlPullParser parser = Xml.newPullParser();
		try {
			parser.setInput(new StringReader(getStringResponse()));
			return parser;
		} catch (XmlPullParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public Document getXmlDocument(){	
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;
		try {
			builder = factory.newDocumentBuilder();
			Document d1 = builder.parse(new InputSource(new StringReader(getStringResponse())));
			return d1;
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	private void executeSoap() throws HttpResponseException, IOException, XmlPullParserException{		
		SoapObject request = new SoapObject(wsData.getNamespace(), wsMethod.getMethodId());                
        
		 // Create envelope
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
                SoapEnvelope.VER11);
        
        for(WebServiceParameter p : this.getParameters()){        	
            PropertyInfo paramPI = new PropertyInfo();
            paramPI.setName(p.getName());            
            paramPI.setValue(p.getValue());
            paramPI.setType(p.getValueType());    
            
            request.addProperty(paramPI);
            
            //envelope.encodingStyle = SoapSerializationEnvelope.XSD;
            
            if(p.isComplexType() && !(p.getValueType() instanceof SoapObject)){
            	envelope.addMapping(wsData.getNamespace(), p.getValue().getClass().getSimpleName(), (Class<?>) p.getValueType());
            }                             
        }                               
        
        envelope.setAddAdornments(false);
        envelope.implicitTypes = true;
        if(wsData.isDotNet())
        	envelope.dotNet = true;        
                
        // Set output SOAP object
        envelope.setOutputSoapObject(request);
        // Create HTTP call object
        HttpTransportSE androidHttpTransport = new HttpTransportSE(wsData.getUrl());        
        
      
        // Invole web service
        androidHttpTransport.call(wsMethod.getAction(), envelope);
        // Get the response            
        Object resultObject = envelope.getResponse();            
        
        respSoap = resultObject;                                          
	}
	
	private void executeRest(){		
		String urlString = wsData.getUrl() + "/" + wsMethod.getAction();
		android.os.Debug.waitForDebugger();
		try
		{
			HttpClient httpClient = new DefaultHttpClient();
			HttpResponse resp = null;
			
			if(wsMethod.getWebMethod().equalsIgnoreCase("POST")){
				HttpPost post = new HttpPost(urlString);
				post.setHeader("content-type", "application/json");
				
				JSONObject dato = new JSONObject();
				for(WebServiceParameter p : this.getParameters()){	
					if(p.getValue() instanceof JSONObject){
						dato = (JSONObject)p.getValue();
						break;
					}
					dato.put(p.getName(), p.getValue());
				}
				
				StringEntity entity = new StringEntity(dato.toString());
				post.setEntity(entity);							 				
					
			    resp = httpClient.execute(post);			        
			}
			else{
				for(WebServiceParameter p : this.getParameters()){        	
					urlString = urlString.replace(p.getName(), p.getValue().toString());
		        }
				
				HttpGet get = new HttpGet(urlString);					
				get.setHeader("content-type", "application/json");									
				
				resp = httpClient.execute(get);		        		        		        
			}
			
			respJSONString = EntityUtils.toString(resp.getEntity());
		}
		catch(Exception ex)
		{
			Log.e("Service Rest","Error!", ex);
		}		
	}
	
	public void invokeWebService() throws HttpResponseException, IOException, XmlPullParserException {		
        if(wsData.getType().equalsIgnoreCase(TYPE_SOAP)){
        	executeSoap();
        }else if(wsData.getType().equalsIgnoreCase(TYPE_REST)){
        	executeRest();
        }        
    }
}