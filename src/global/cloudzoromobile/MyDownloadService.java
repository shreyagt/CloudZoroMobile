
package global.cloudzoromobile;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.mail.BodyPart;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Store;



import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Toast;

public class MyDownloadService extends Service 
{
	int files_count;
	String filenms,DOWNLOAD_FILE_NAME;
	ArrayList<String> files_to_be_downloaded;
	String HOST = "pop.gmail.com";
	Store store;
	String USERNAME,PASSWORD;
	int total_count;
	int downloaded=0;
	
	Session session;
	String filename="";
	MailDownloadThread thread;
	ArrayList<String> file_list;
	
	int messages_count=1;
	boolean message_rcvd=false;
	
	public void onCreate()
	{
		super.onCreate();
		files_to_be_downloaded=new ArrayList<String>();
		file_list=new ArrayList<String>();
		
		
	}
	
	
	
	public void createNotification(Context context, String Message,String Filename) {
		NotificationManager notificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		Notification notification = new Notification(R.drawable.ic_launcher,"CloudZoro App", System.currentTimeMillis());
		// Hide the notification after its selected
		notification.flags |= Notification.FLAG_AUTO_CANCEL;

		Intent intent = new Intent(context, MainActivity.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
				intent, 0);
		notification.setLatestEventInfo(context, "CloudZoro Downloading File:"+Filename,Message, pendingIntent);
		notificationManager.notify(0, notification);

	}
	
	public void onStart(Intent intnt,int id)
	{
		super.onStart(intnt, id);
		Toast.makeText(this,"Service Started",Toast.LENGTH_LONG).show();
		Bundle b=intnt.getBundleExtra("dat");
		if(b!=null)
		{
		String fnames=b.getString("filenames");
		if(fnames!=null)
		{
			files_to_be_downloaded.clear();
			filenms=fnames;
			StringTokenizer st=new StringTokenizer(fnames,",");
			while(st.hasMoreTokens())
			{
			files_to_be_downloaded.add(st.nextToken());
			 
			}
		}
		
		String count=b.getString("count");
		if(count!=null)
		{
		files_count=Integer.parseInt(count);
		total_count=files_count;
		}
		
		
		String USER=b.getString("user");
		if(USER!=null)
		{
		  USERNAME=USER;
		}
		
		
		String PASS=b.getString("pass");
		if(PASS!=null)
		{
		  PASSWORD=PASS;
		}
		
		String Dfilename=b.getString("finalname");
		if(Dfilename!=null)
		{
		  DOWNLOAD_FILE_NAME=Dfilename;
		}
		
		
		String file=b.getString("file");
		if(file!=null)
		{
		messages_count++;	
		if(messages_count>=total_count)
		{
		  new MailDownloadThread().start();
		}
		}
		}

	}
	
	public Store GmailConnection()
	{
		if(store!=null)
		{
			return store;
		}
		else
			{
			return getConnectiontoGmail(); 
			}
	}
	
	public Store getConnectiontoGmail()
	{
		try
		{
		 Properties prop = new Properties();
         prop.setProperty("mail.pop3.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
         prop.setProperty("mail.pop3.socketFactory.fallback", "false");
         prop.setProperty("mail.pop3.port", "995");
         prop.setProperty("mail.pop3.socketFactory.port", "995");
        
         prop.put("mail.pop3.host",HOST);
         prop.put("mail.store.protocol", "pop3");
         session = Session.getInstance(prop);
         store = session.getStore(); 
       
         store.connect(HOST,USERNAME,PASSWORD);
         return store;
		}
		catch(Exception e)
		{
			return null;
		}
	}
	
	public void readMail()
	{
		try
        {
			
           store=GmailConnection();
           while(true)
           {
            Folder inbox = store.getDefaultFolder().getFolder("INBOX");
            inbox.open(Folder.READ_WRITE);
 
            Message[] msg = inbox.getMessages();
            
            String subject=null;
         
               
            for(int i=0;i<msg.length;i++)
            {
                subject=msg[i].getSubject();  
                
                if(files_to_be_downloaded.contains(subject))
                {
                    handleMultipart(msg[i]);       
                }
            }
                
        
           }
          
        }
        catch(Exception e)
        {
        	 Bundle b1=new Bundle();
	  	    	b1.putString("data","Saving Attachment Failed"+e.getLocalizedMessage());
	  	    	android.os.Message m1=mHandler.obtainMessage();
	  	    	m1.setData(b1);
	  	    	mHandler.sendMessage(m1);
	  	    	readMail();
        }
	}
	
	
	 private void handle(Message msg) throws Exception {
	       
	    }
	 
	    private void handleText(Message msg) throws Exception {
	        handle(msg);
	       
	    }
	 
	    private void handleMultipart(Message msg) throws Exception {
	        String disposition;
	        BodyPart part;
	        Multipart mp = (Multipart) msg.getContent();
	        int mpCount = mp.getCount();
	        for (int m = 0; m < mpCount; m++) {
	         
	            part = mp.getBodyPart(m);
	            disposition = part.getDisposition();
	           
	            if (disposition != null && disposition.equals(part.ATTACHMENT))
	            
	            {              
	                saveAttach(part);
	                files_to_be_downloaded.remove(msg.getSubject());
	                if(files_to_be_downloaded.size()<=0)
	                {
	                	messages_count=0;
	                	//write the file integration code here
	                	
	                	FileOutputStream fos=new FileOutputStream(Environment.getExternalStorageDirectory()+"/cloudzoro/"+DOWNLOAD_FILE_NAME);
	                	StringTokenizer st=new StringTokenizer(filenms,",");
	            		while(st.hasMoreTokens())
	            		{
	            			                
	                          
	                          FileInputStream fis=new FileInputStream(Environment.getExternalStorageDirectory()+"/cloudzoro/"+st.nextToken());   
	                          byte[] data=new byte[fis.available()];
	                          fis.read(data);
	                          fos.write(data);
	                          fis.close();
	                         
	                          
	                          
	                     }
	                         fos.close();
	                         Bundle b1=new Bundle();
	     	    	    	b1.putString("data","Download Complete");
	     	    	    	android.os.Message m1=mHandler1.obtainMessage();
	     	    	    	m1.setData(b1);
	     	    	    	mHandler1.sendMessage(m1);
	            			
	            			
	            		}
	                
	               
	               
	                }
	              
	            }
	        }
	    
	    
	    public void saveAttach(BodyPart par)
	    {
	        try
	        {
	        	File f=new File(Environment.getExternalStorageDirectory()+"/cloudzoro");
            	if(!f.exists())
            	{
            		f.mkdirs();
            	}
            
	        File f1=new File(Environment.getExternalStorageDirectory()+"/cloudzoro/"+par.getFileName());
	        BufferedOutputStream bos=new BufferedOutputStream(new FileOutputStream(f1));
	        BufferedInputStream bis=new BufferedInputStream(par.getInputStream());
	        byte[] data=new byte[bis.available()];
	        bis.read(data);
	        bos.write(data);
	        bos.close();
	        bis.close();
	        downloaded++;
	        Bundle b1=new Bundle();
	    	b1.putString("data","Attachment Saved");
	    	android.os.Message m1=mHandler1.obtainMessage();
	    	m1.setData(b1);
	    	mHandler1.sendMessage(m1);
	     
	        }
	        catch(Exception e)
	        {
	        	Bundle b1=new Bundle();
	  	    	b1.putString("data","Saving Attachment Failed");
	  	    	android.os.Message m1=mHandler.obtainMessage();
	  	    	m1.setData(b1);
	  	    	mHandler.sendMessage(m1);
	        }
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
	    
	    
	    private final Handler mHandler1 = new Handler() {
	        @Override
	        public void handleMessage(android.os.Message msg) 
	        {
	        	Bundle b=msg.getData();
	    		String res=b.getString("data");
	    		if(!res.equals("Download Complete"))
	    		{
	    			if(downloaded>total_count)
	    				downloaded=total_count;
	    			createNotification(getBaseContext(),"Download Progress "+downloaded+"/"+total_count, DOWNLOAD_FILE_NAME);
	    		}
	    		else
	    		{
	    			createNotification(getBaseContext(),"Download Complete", DOWNLOAD_FILE_NAME);
	    		}
	    		
	           
	        }
	        
	    };
	
	    class MailDownloadThread extends Thread
	    {
	    	//String filename;
	    	public MailDownloadThread()
	    	{
	    		
	    	}
	    	
		@Override
		public void run() {
			// TODO Auto-generated method stub
			BufferedReader in = null;
	    	try 
	    	{
	    		synchronized(MailDownloadThread.this)
	    		{
	    
	    	 readMail();
	    	 Bundle b1=new Bundle();
		    	b1.putString("data","Read Mail Called");
		    	android.os.Message m1=mHandler.obtainMessage();
		    	m1.setData(b1);
		    	mHandler.sendMessage(m1);
	    		}
	    	}
	    	catch(Exception e)
	    	{
	    		Bundle b1=new Bundle();
		    	b1.putString("data","Error in Reading Mail "+e.getMessage());
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
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

}
