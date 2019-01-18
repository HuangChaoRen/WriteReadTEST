package com.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StatFs;
import android.util.Log;


public class Utils {

	static FileWriter log_writer = null;
	public static void WriteLog(String TAG,String fileFullPath,boolean state,long Theoretically_size,long Practically_size,String errmsg) throws Exception {
			
		File log_file = new File(fileFullPath);
		if(log_file.exists()){
			log_writer = new FileWriter(log_file,true);
		}else{
			log_file.createNewFile();
			log_writer = new FileWriter(log_file,true);
		}
		
		String state_str = "";
		if(state){
			state_str = "成功";
		}else{
			state_str = "失败";
		}
		log_writer.write("\r\n*************************\r\n");
		log_writer.write(TAG + "\r\n"
						+ state_str+"\r\n"
						+ new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())
						+ "\r\n尝试写入:"+Theoretically_size + "字节"
						+ "\r\n实际写入:"+Practically_size + "字节"
						+ "\r\nMSG:"+errmsg);		
		log_writer.write("\r\n*************************\r\n");
		log_writer.flush();
		
	}
	
	public static void copy_file(File oldfile, File newfile) {  
		    FileChannel in = null;  
		    FileChannel out = null;  
		    FileInputStream inStream = null;  
		    FileOutputStream outStream = null;  
		    
		    try {  
		    	if(newfile.exists()){
		    		newfile.delete();		//如果有旧缓存，删除掉
		    	}
		    	
		    	if(!newfile.exists()){
					newfile.createNewFile();
				}

		    	if(oldfile!=null && oldfile.exists()){
		    		inStream = new FileInputStream(oldfile); 
			        outStream = new FileOutputStream(newfile);  
			        in = inStream.getChannel();  
			        out = outStream.getChannel();  

			        in.transferTo(0, in.size(), out);  
		    	}

		        
		    } catch (IOException e) {  
		        e.printStackTrace();  
		    } finally {
		    	if(inStream!=null){
		    		try {
						inStream.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		    		inStream = null;
		    	}
		    	if(in!=null){
		    		try {
		    			in.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		    		in = null;
		    	}
		    	if(outStream!=null){
		    		try {
		    			outStream.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		    		outStream = null;
		    	}
		    	if(out!=null){
		    		try {
		    			out.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		    		out = null;
		    	}
		    }
		    
		}  

	/**
     * 获取外置SD卡路径
     * @return  应该就一条记录或空
     */
	 public static String getSDCARDPath() {  
		         String sdcard_path = null;  
		         String sd_default = Environment.getExternalStorageDirectory()  
		                 .getAbsolutePath();  
		         if (sd_default.endsWith("/")) {  
		             sd_default = sd_default.substring(0, sd_default.length() - 1);  
		         }  
		         // 得到路径  
		         try {  
		             Runtime runtime = Runtime.getRuntime();  
		             Process proc = runtime.exec("mount");  
		             InputStream is = proc.getInputStream();  
		             InputStreamReader isr = new InputStreamReader(is);  
		             String line;  
		             BufferedReader br = new BufferedReader(isr);  
		             while ((line = br.readLine()) != null) {  
		                 if (line.contains("secure"))  
		                     continue;  
		                 if (line.contains("asec"))  
		                     continue;  
		                 if (line.contains("fat") && line.contains("/mnt/")) {  
		                     String columns[] = line.split(" ");  
		                     if (columns != null && columns.length > 1) {  
		                         if (sd_default.trim().equals(columns[1].trim())) {  
		                             continue;  
		                         }  
		                         sdcard_path = columns[1];  
		                     }  
		                 } else if (line.contains("fuse") && line.contains("/mnt/")) {  
		                     String columns[] = line.split(" ");  
		                     if (columns != null && columns.length > 1) {  
		                         if (sd_default.trim().equals(columns[1].trim())) {  
		                             continue;  
		                         }  
		                         sdcard_path = columns[1];  
		                     }  
		                 }  
		             }  
		         } catch (Exception e) {  
		            // TODO Auto-generated catch block  
		             e.printStackTrace();  
		         }  
		         
		         return "storage/" + new File(sdcard_path).getName()+ "/";  
		     }  

	 
	 /**
	  * 判断是否挂载了u盘或sdcard
	  * @param path
	  * @return
	  */
	 public static boolean isMounted(String path) {
		 final String MOUNTS_FILE = "/proc/mounts";
         boolean blnRet = false;
         String strLine = null;
         BufferedReader reader = null;
         try {
             reader = new BufferedReader(new FileReader(MOUNTS_FILE));

             while ((strLine = reader.readLine()) != null) {
                 if (strLine.contains(path)) {
                     blnRet = true;
                     break;
                 }
             }
         } catch (Exception e) {
             e.printStackTrace();
         } finally {
             if (reader != null) {
                 try {
                     reader.close();
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
                 reader = null;
             }
         }
         return blnRet;
	 }

	 //发送数据到主线程
	public static void sentDataToMain(Handler handler, String key, String data){
		Message message = new Message();
		message.what = MainActivity.show_write_userdata_result;
		Bundle bundle = new Bundle();
		bundle.putString(key, data);
		message.setData(bundle);
		handler.sendMessage(message);
	}
	
	//获取剩余空间
	public static long getFreeSpace(String path){
  
        StatFs stat = new StatFs(path);  
        // 获取可用存储块数量  
        long availableBlocks = stat.getAvailableBlocks();  
        // 每个存储块的大小  
        long blockSize = stat.getBlockSize();  
  
        // 可用存储空间  
        long availSize = availableBlocks * blockSize;  

		return availSize;
	}
	 
}
