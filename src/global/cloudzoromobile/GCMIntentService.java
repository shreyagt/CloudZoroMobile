package global.cloudzoromobile;




import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.mail.Session;
import javax.mail.Store;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;



import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gcm.GCMBaseIntentService;

public class GCMIntentService extends GCMBaseIntentService
{
	
	 Store store;
         
    int Pieces=0;
    int done=0;
    Session session;
	ArrayList<String> Responses=new ArrayList<String>();
	SharedPreferences prefs;
	
	public static boolean isDownloading=false;	
	
	String HOST = "pop.gmail.com";
	
	
	public static String project_number="461893423315";	
	String SERVER_ADDRESS;
	String USERNAME;
	String PASSWORD;
	String DOWNLOAD_FILE_NAME;
	String REGISTRATION_ID;
	ArrayList<String> File_Name_List=new ArrayList<String>();
	String filenms;
	boolean download_started=false;
	String data_to_download="";
	
	String project_id,reg_id,email_id,filenames;
	
	public GCMIntentService()
	{
		super(project_number);	
	}
	
	
	
	
	@Override
	protected void onError(Context arg0, String arg1) {
		// TODO Auto-generated method stub
		
	}
	
    class NewRequestThread extends Thread
    {
    	public void run()
    	{
    		 BufferedReader in = null;
 	    	try 
 	    	{
 	    		
 	    	HttpClient client = new DefaultHttpClient();
 	    	HttpPost request = new HttpPost("http://"+SERVER_ADDRESS+":8080/rserver/rrequest");
 	    	List<NameValuePair> postParameters = new ArrayList<NameValuePair>();
 	    	postParameters.add(new BasicNameValuePair("project_id",project_number));
 	    	postParameters.add(new BasicNameValuePair("reg_id",REGISTRATION_ID));
 	    	postParameters.add(new BasicNameValuePair("email_id",InfoClass.EMAIL_ID));
 	    	postParameters.add(new BasicNameValuePair("file_name",filenames));
 	    	UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(postParameters);
 	    	request.setEntity(formEntity);
 	    	HttpResponse response = client.execute(request);
 	    	in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
 	    	StringBuffer sb = new StringBuffer("");
 	    	String line = "";
 	    	String NL = System.getProperty("line.separator");
 	    	while ((line = in.readLine()) != null) {
 	    	sb.append(line);
 	    	}
 	    	in.close();
 	    	String result = sb.toString();
 	    	Bundle b1=new Bundle();
 	    	b1.putString("data",result);
 	    	android.os.Message m1=mHandler.obtainMessage();
 	    	m1.setData(b1);
 	    	mHandler.sendMessage(m1);
 	    	
 	    
 	    	}
 	    	catch(Exception e)
 	    	{
 	    		Bundle b1=new Bundle();
 		    	b1.putString("data","Error in sending "+e.getMessage());
 		    	android.os.Message m1=mHandler.obtainMessage();
 		    	m1.setData(b1);
 		    	mHandler.sendMessage(m1);
 	    		
 	    	}
 	    	
 	    	finally {
 	    		if (in != null) {
 	    		try {
 	    		in.close();
 	    		} catch (IOException e) {
 	    		e.printStackTrace();
 	    		}
 	    		}
 	    		}
    		
    	}
    }

	@Override
	protected void onMessage(Context cntxt, Intent arg1) {
		
		Log.d("Message","Message Received");
		
		if(!download_started)
		{
		download_started=true;
		prefs = PreferenceManager.getDefaultSharedPreferences(cntxt);
		SERVER_ADDRESS = prefs.getString("address",null);
		Log.d("ADDRESS:", SERVER_ADDRESS);
		//USERNAME = prefs.getString("username",null);
		USERNAME ="cloudzoromail";
		Log.d("USERNAME:", USERNAME);
		PASSWORD = prefs.getString("password",null);
		Log.d("PASSWORD:", PASSWORD);
		DOWNLOAD_FILE_NAME = prefs.getString("download_file",null);
		Log.d("DOWNLOAD FILE NAME:", DOWNLOAD_FILE_NAME);
		REGISTRATION_ID=prefs.getString("reg_id",null);
		Log.d("REG_ID:", REGISTRATION_ID);
		filenms=prefs.getString("file_names",null);
		Log.d("FILENAME:", filenms);
		StringTokenizer st=new StringTokenizer(filenms,",");
		while(st.hasMoreTokens())
		{
			String data=st.nextToken();
			if(!File_Name_List.contains(data))
			{
			File_Name_List.add(data);
			}
		}
		}
		String payload = arg1.getStringExtra("message");
		//createNotification(getBaseContext(), payload);
		//new MailDownloadThread().start();
		Toast.makeText(this,"Message Received from Cloud",Toast.LENGTH_LONG).show();
		Intent it=new Intent(cntxt,MyDownloadService.class);
		Bundle b=new Bundle();
		b.putString("file",payload);
		it.putExtra("dat",b);
		cntxt.startService(it);
	}

	
	public void createNotification(Context context, String payload) {
		NotificationManager notificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		Notification notification = new Notification(R.drawable.ic_launcher,"Message received", System.currentTimeMillis());
		// Hide the notification after its selected
		notification.flags |= Notification.FLAG_AUTO_CANCEL;

		Intent intent = new Intent(context, MainActivity.class);
		intent.putExtra("payload", payload);
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
				intent, 0);
		notification.setLatestEventInfo(context, "Message","New message received", pendingIntent);
		notificationManager.notify(0, notification);

	}
	
	@Override
	protected void onRegistered(Context arg0, String arg1) {
		// TODO Auto-generated method stub
		Log.d("App:Registration", arg1);
		prefs = PreferenceManager.getDefaultSharedPreferences(arg0);
		Editor edit = prefs.edit();
		edit.putString("reg_id", arg1);
		edit.commit();
		InfoClass.REGISTRATION_ID=arg1;
		prefs = PreferenceManager.getDefaultSharedPreferences(arg0);
		SERVER_ADDRESS = prefs.getString("address",null);		
		REGISTRATION_ID=prefs.getString("reg_id",null);
		filenames=prefs.getString("file_names",null);
		if(download_started)
		{
		new NewRequestThread().start();
		}
	}
	
	

	@Override
	protected void onUnregistered(Context arg0, String arg1) {
		// TODO Auto-generated method stub
		Log.d("unnRegistration", arg1);
		
		
	}
	
	
	
	 private final Handler mHandler = new Handler() {
	        @Override
	        public void handleMessage(android.os.Message msg) 
	        {
	        	Bundle b=msg.getData();
	    		String res=b.getString("data");
	    		Toast.makeText(getBaseContext(),res, Toast.LENGTH_LONG).show();
	           
	        }
	        
	    };
	
	   
}
	
	
	
	


