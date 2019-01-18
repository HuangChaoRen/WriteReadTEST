package com.broadcast;

import java.io.File;
import java.util.List;

import com.test.MainActivity;
import com.test.Utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class BootStart extends BroadcastReceiver{
	final String mounted = "android.intent.action.MEDIA_MOUNTED";  
	final String unmounted = "android.intent.action.MEDIA_UNMOUNTED";  
	final String boot_completed= "android.intent.action.BOOT_COMPLETED";
	
	
	@Override
	public void onReceive(Context context, Intent intent) {
		
		
		// TODO Auto-generated method stub
		if(intent.getAction().equals(mounted)){
			
			File os_sdcard_file = new File(Utils.getSDCARDPath() + MainActivity.OS_FILE_NAME);
	        Log.d("CR", "mount---" + os_sdcard_file.getAbsolutePath());
			if(os_sdcard_file!=null && os_sdcard_file.exists()){
	        	Intent start_Intent=new Intent(context,MainActivity.class);  
				start_Intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);  
				context.startActivity(start_Intent); 
	        }else{
	        	Toast.makeText(context, "文件为空", Toast.LENGTH_LONG).show();
	        }
	        
		}
		
		//删除缓存os文件
		File os_cache_file = new File(context.getFilesDir().getPath() + "/" + MainActivity.OS_FILE_NAME);
		if(os_cache_file!=null && os_cache_file.exists()){
			os_cache_file.delete();
		}
		
	}  

}
