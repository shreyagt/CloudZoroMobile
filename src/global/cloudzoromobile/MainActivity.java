package global.cloudzoromobile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import com.google.android.gcm.GCMRegistrar;



import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener {

	Button b1,b2;
	EditText t1;
	
	
	//new
	String regId;
	TabHost tbhost;
	LinearLayout lyt_progress;
	RelativeLayout lyt_download;
	String Download_File_Name;
	String Selected_File_Path;
	String filenames="";
	String PROJECT_NUMBER="461893423315";
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		b1=(Button)findViewById(R.id.btn1);
		b2=(Button)findViewById(R.id.btn2);
		t1=(EditText)findViewById(R.id.txt2);
		b1.setOnClickListener(this);
		b2.setOnClickListener(this);
		

		String s = ((MyApp)this.getApplication()).getfileToDownload();
		if(s!="\0")
		{
				t1.setText(s);
		}
		

		
		GCMRegistrar.checkDevice(this);
		GCMRegistrar.checkManifest(this);
		regId = GCMRegistrar.getRegistrationId(this);
		if (regId.equals(""))
		{		
				GCMRegistrar.register(this,PROJECT_NUMBER);					
			 	  
	    }

		
	}	
	
	//new
	 public void onStart()
	    {
	    	super.onStart();
	    	  GCMRegistrar.checkDevice(this);
	  		GCMRegistrar.checkManifest(this);
	  		regId = GCMRegistrar.getRegistrationId(this);
	  		if (regId.equals(""))
	  		{		
	  				GCMRegistrar.register(this,PROJECT_NUMBER);					
	  			 	  
	  	    }      
	    	
	    	SharedPreferences pref=PreferenceManager.getDefaultSharedPreferences(this);
	    	InfoClass.SERVER_ADDRESS=pref.getString("address",null);
	    	InfoClass.EMAIL_ID=pref.getString("username",null);
	    	InfoClass.PASSWORD=pref.getString("password",null);
	    	if(InfoClass.SERVER_ADDRESS==null||InfoClass.EMAIL_ID==null||InfoClass.PASSWORD==null)
	    	{
	    		Intent it=new Intent(this,Settings.class);
	    		startActivity(it);
	    	}
	    	
	    }
	    

	@Override
	public void onClick(View v) {
		if(v.equals(b1))
		{
			Intent it=new Intent(this,FileActivity.class);
	    	startActivity(it);
	    	finish();
		}
		else if(v.equals(b2))
		{
			String filename1=t1.getText().toString();
			
			//new
			try
	    	{
	    		
	    		
	    		FileInputStream fis=new FileInputStream(new File(filename1));
	    		//FileInputStream fis=new FileInputStream(new File("/sdcard/Track10.mp3.cloudzoro"));
	    		BufferedReader br=new BufferedReader(new InputStreamReader(fis));
	    		String message=br.readLine();
	    		if(message.equals("#cloudzoro"))
	    		{
	    			String project_num=br.readLine();
	    			InfoClass.PROJECT_ID=project_num;
	    			Download_File_Name=br.readLine();
	    			int number_of_pieces=Integer.parseInt(br.readLine());  
	    			
	    			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
	    			Editor edit = prefs.edit();
	    			edit.putString("project_id",project_num);
	    			edit.putString("download_file",Download_File_Name);
	    			edit.putInt("no_of_pieces",number_of_pieces);
	    			filenames="";
	    			for(int i=0;i<number_of_pieces;i++)
	    			{
	    				if(i==0)
	    				{
	    				  filenames=br.readLine();
	    				}
	    				else
	    				{
	    					filenames=filenames+","+br.readLine();
	    				}
	    			
	    			}
	    			edit.putString("file_names",filenames);
	    			edit.commit();
	    			br.close();
	    				
	    				prefs = PreferenceManager.getDefaultSharedPreferences(this);
	    				InfoClass.REGISTRATION_ID= prefs.getString("reg_id",null);
	    				//InfoClass.REGISTRATION_ID=regId;
	    				Toast.makeText(this,"Reg ID:"+InfoClass.REGISTRATION_ID, Toast.LENGTH_LONG).show();
	    				new RegistrationThread().start();
	    				new RequestSendingThread().start();
	    				Intent it=new Intent(this,MyDownloadService.class);
	    				Bundle b=new Bundle();
	    				b.putString("filenames",filenames);
	    				b.putString("count", ""+number_of_pieces);
	    				b.putString("user",InfoClass.EMAIL_ID);
	    				b.putString("pass",InfoClass.PASSWORD);
	    				b.putString("finalname",Download_File_Name);
	    				it.putExtra("dat",b);
	    				startService(it);
	    		}
	    		else
	    		{
	    			Toast.makeText(this,"Invalid File Format",Toast.LENGTH_LONG).show();
	    		}
	    		
	    	}
	    	catch(Exception e)
	    	{
	    		Toast.makeText(this,"Error in Downloading"+e.getLocalizedMessage(),Toast.LENGTH_LONG).show();
	    	}
	    	
	    	
		}	
		
	}
	
	
	
	 class RequestSendingThread extends Thread
	    {
	    	public void run()
	    	{
	    		 BufferedReader in = null;
	 	    	try 
	 	    	{
	 	    		
	 	    	HttpClient client = new DefaultHttpClient();
	 	    	HttpPost request = new HttpPost("http://"+InfoClass.SERVER_ADDRESS+":8080/rserver/rrequest");
	 	    	List<NameValuePair> postParameters = new ArrayList<NameValuePair>();
	 	    	postParameters.add(new BasicNameValuePair("project_id",InfoClass.PROJECT_ID));
	 	    	postParameters.add(new BasicNameValuePair("reg_id",InfoClass.REGISTRATION_ID));
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
	 	    	Message m1=mHandler.obtainMessage();
	 	    	m1.setData(b1);
	 	    	mHandler.sendMessage(m1);
	 	    	
	 	    
	 	    	}
	 	    	catch(Exception e)
	 	    	{
	 	    		Bundle b1=new Bundle();
	 		    	b1.putString("data","Error in sending "+e.getMessage());
	 		    	Message m1=mHandler.obtainMessage();
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
	    
	  
	    class RegistrationThread extends Thread
	    {
	    public void run()
	    {
	    	 BufferedReader in = null;
		    	try 
		    	{
		    		
		    	HttpClient client = new DefaultHttpClient();
		    	HttpPost request = new HttpPost("http://"+InfoClass.SERVER_ADDRESS+":8080/rserver/register");
		    	List<NameValuePair> postParameters = new ArrayList<NameValuePair>();
		    	postParameters.add(new BasicNameValuePair("project_id",InfoClass.PROJECT_ID));
		    	postParameters.add(new BasicNameValuePair("reg_id",InfoClass.REGISTRATION_ID));
		    	postParameters.add(new BasicNameValuePair("uname",InfoClass.EMAIL_ID));
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
		    	Message m1=mHandler.obtainMessage();
		    	m1.setData(b1);
		    	mHandler.sendMessage(m1);
		    	
		    
		    	}
		    	catch(Exception e)
		    	{
		    		Bundle b1=new Bundle();
			    	b1.putString("data","Error in sending "+e.getMessage());
			    	Message m1=mHandler.obtainMessage();
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
	    public boolean onCreateOptionsMenu(Menu menu) {
	        // Inflate the menu; this adds items to the action bar if it is present.
	        getMenuInflater().inflate(R.menu.activity_main, menu);
	        return true;
	    }
	    
	    public boolean onOptionsItemSelected(MenuItem itm)
	    {
	    	if(itm.getItemId()==R.id.menu_settings)
	    	{
	    		Intent it=new Intent(this,Settings.class);
	    		startActivity(it);
	    	}
	    	else
	    	{
	    		Intent unregIntent = new Intent("com.google.android.c2dm.intent.UNREGISTER");
	        	unregIntent.putExtra("app", PendingIntent.getBroadcast(this, 0, new Intent(), 0));
	        	startService(unregIntent);
	        	
	    		
	    	}
	    	return true;
	    }
	    
	    
	    private final Handler mHandler = new Handler() {
	        @Override
	        public void handleMessage(Message msg) 
	        {
	        	Bundle b=msg.getData();
	    		String res=b.getString("data");
	    		Toast.makeText(MainActivity.this,res, Toast.LENGTH_LONG).show();
	           
	        }
	        
	    };

}
