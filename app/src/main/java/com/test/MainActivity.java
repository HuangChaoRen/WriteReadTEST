package com.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import com.example.writereadtest.R;
import com.md5.SVMD5Util;
import com.shareprence.SharePreferenceUtil;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.RecoverySystem;
import android.os.StatFs;
import android.os.SystemClock;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Chronometer;
import android.widget.Toast;
import android.widget.Chronometer.OnChronometerTickListener;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity implements OnClickListener,OnChronometerTickListener {

	private static Context mContext;
	
	public static final int FILE_CHOOSE_REQUEST_CODE = 0x02;
	public static final int OUT_FILE_CHOOSE_REQUEST_CODE = 0x03;
	public final static String EXTRA_FILE_CHOOSER = "file_chooser";
	public final static String OUT_EXTRA_FILE_CHOOSER = "out_file_chooser";
	private final String THREAD_SEND_KEY = "result";		
	
	public final static int close_ProgressDialog = 0;
	public final static int open_ProgressDialog = 1;
	public final static int show_write_userdata_result = 2;			//在Utils.java中使用了
	
	public static final String OS_FILE_NAME = "updateOS.rom.zip";
	private final String test_temp_file_name = "TEST_FRO_WRITE_READ";
//	public static final String CACHE_OS_FILE_PATH = "/data/data/com.example.writereadtest/files";
	private String CACHE_OS_FILE_PATH = null;
//	public static final String SDCARD_OS_FILE_PATH = "/storage/usb0/";
//	public static final String SDCARD_OS_FILE_PATH = Utils.getSDCARDPath();
	
	private final String TAG = "CR";
	private final String USERDATA_TAG = "USER DATA";
	private final String PROGRAM_TAG = "PROGRAM DATA";
	
	private SharePreferenceUtil spUtils = null;		//数据库操作类
	
	private String root_folder_path = null;			//根目录
	private final String log_file_name = "TEST_WRITE_READ_USER_LOG.txt";		//日志文件名
	private File test_file = null;				//用于疯狂读写的文件
	private FileWriter test_file_writer = null;				//用于疯狂写的文件输入流
	private FileReader test_file_reader = null;				//用于验证的输出流
	private BufferedReader tempBufferReader = null;			
	
	private Button btn_user_write = null;		//写入用户数据区按钮
	private Button btn_program_write = null;	//开始刷机按钮
	private TextView tv_savepath = null;		//日志保存路径
	private TextView tv_result = null;			
	private TextView tv_result2 = null;
	private Button btn_file_sel = null;			//选择日志保存路径按钮
	private Button btn_delete_temp = null;		//删除缓存按钮	
	private CheckBox chebox_auto_write_program = null;
	private Chronometer timer = null;
	private ProgressDialog pd; 			//等待界面
	private EditText et_count = null;		//写入次数
	private Button btn_out_sel = null;		//选择外部数据文件
	private TextView tv_out_path = null;	//外部数据文件路径
	private Button btn_write_out = null;	//写外部数据文件按钮
	private EditText et_count_out = null;	//写外部数据次数
	
	private String write_test_data = "abcdefghijklmn -={}[]:!@#$%^&*()_+-/? opqrstuvwxyz";		//测试数据
	
//	private File os_file = new File(SDCARD_OS_FILE_PATH + OS_FILE_NAME);	//os升级文件
	private File os_file = null;	//os升级文件
	private File cache_file = null;				//缓存os升级文件
	private File out_file = null;
	private File temp_file = null;
	
//	private String write_user_data_result = "";			//界面显示的结果
	
	private boolean not_stop_symbol = false;				//用于结束循环的标志，为false时停止死循环，true时进行死循环
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;
		setContentView(R.layout.activity_main);
		
		try {
			CACHE_OS_FILE_PATH = getApplicationContext().getFilesDir().getPath();
			
			spUtils = new SharePreferenceUtil(getApplicationContext(), "WriteReadTest");
			
			tv_result = (TextView)findViewById(R.id.tv_result);
			tv_result2 = (TextView)findViewById(R.id.tv_result2);
			tv_savepath = (TextView) findViewById(R.id.tv_save_path);
			btn_user_write = (Button) findViewById(R.id.btn_user);
			btn_file_sel = (Button)findViewById(R.id.btn_file_sel);
			btn_delete_temp = (Button)findViewById(R.id.btn_delete_temp);
			btn_program_write = (Button)findViewById(R.id.btn_program);
			chebox_auto_write_program = (CheckBox)findViewById(R.id.cb_auto_write_program);
			et_count = (EditText)findViewById(R.id.et_count);
			timer = (Chronometer) findViewById(R.id.timer);
			timer.stop();				//停止计时
			
			//root_folder_path = Environment.getExternalStorageDirectory().getPath();
			root_folder_path = this.getFilesDir().getPath();
			Log.d(TAG, "root_folder_path = " + root_folder_path);

			btn_user_write.setOnClickListener (this);
			btn_file_sel.setOnClickListener(this);
			btn_program_write.setOnClickListener(this);
			btn_delete_temp.setOnClickListener(this);
			findViewById(R.id.root_layout).setOnClickListener(this);
			chebox_auto_write_program.setOnClickListener(this);
			if(spUtils.get_checkbox_auto_write_program()){	//上次已经勾选
				chebox_auto_write_program.performClick();
			}
			timer.setOnChronometerTickListener(this);
			
			btn_out_sel = (Button) findViewById(R.id.btn_out_sel);
			btn_out_sel.setOnClickListener(this);
			tv_out_path = (TextView) findViewById(R.id.tv_out_path);
			btn_write_out = (Button) findViewById(R.id.btn_write_out);
			btn_write_out.setOnClickListener(this);
			et_count_out = (EditText)findViewById(R.id.et_count_out);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	
	
	@Override
	public void onClick(View v) {
		//隐藏软键盘
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);  
		imm.hideSoftInputFromWindow(v.getWindowToken(), 0);  
		
		try {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			//触发主界面点击事件
			case R.id.root_layout:
				//隐藏软键盘
//				InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);  
//				imm.hideSoftInputFromWindow(v.getWindowToken(), 0);  
				break;
			//开始写程序区
			case R.id.btn_program:
				Log.d(TAG, "begin write program!!!!");
				
				os_file = new File(Utils.getSDCARDPath() + OS_FILE_NAME);
				
				Log.d(TAG,"os_file path is?---"+(os_file.getAbsolutePath()));
				
				if(os_file!=null && os_file.exists()){
//						RecoverySystem.verifyPackage(os_file, null, null);	//检查安装包
						cache_file = new File(CACHE_OS_FILE_PATH, OS_FILE_NAME);
							
						/* 显示ProgressDialog */  
						
						pd = ProgressDialog.show(MainActivity.this, "准备刷机中", "准备刷机中，请稍后……");  
						pd.setCanceledOnTouchOutside(false);
						/* 开启一个新线程，在新线程里执行耗时的方法 */  
		                new Thread(new Runnable() {  
		                    @Override  
		                    public void run() {
		                    	
		                    	Utils.copy_file(os_file, cache_file);
//		                    	pd.dismiss();	//关闭ProgressDialog
		                    	handler.sendEmptyMessage(close_ProgressDialog);
		                    	try {
		                    		Utils.WriteLog(PROGRAM_TAG,tv_savepath.getText().toString(), true, 0,0,"进行了一次刷机");
		                    		//刷机
									RecoverySystem.installPackage(mContext, cache_file);  
								} catch (Exception e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}		
		                    }  
		  
		                }).start();
							
//							Utils.copy_file(os_file, cache_file);
							
						tv_result2.setText("DONE");
				}else{
					Log.d(TAG, "OS FILE IS NULL!!!");
					tv_result2.setText("OS升级文件不存在!!!");
				}
				Log.d(TAG, "finish write program!!!!");
				
				break;
				
			//开始写用户数据区
			case R.id.btn_user:
				Log.d(TAG, "begin write userdata!!!!");
				test_file = new File(root_folder_path,test_temp_file_name);
				//初始化测试文件输入和输出流
				if (test_file.exists()) {
					//将旧文件重命名，将旧文件存储起来
					test_file.renameTo(new File(root_folder_path,test_temp_file_name + System.currentTimeMillis()));
					
					test_file_writer = new FileWriter(test_file);
					test_file_reader = new FileReader(test_file);
				}else{
					test_file.createNewFile();
					test_file_writer = new FileWriter(test_file);
					test_file_reader = new FileReader(test_file);
				}
				
				/* 显示ProgressDialog */  
//                pd = ProgressDialog.show(MainActivity.this, "正在写入", "写入数据中，请稍后……");  
				pd = new ProgressDialog(MainActivity.this);  
				pd.setCanceledOnTouchOutside(false);
				pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);  
				pd.setTitle("正在写入");  
				pd.setMessage("写入数据中，请稍后……");  
				pd.setIndeterminate(false);  
				pd.setCancelable(false);
				
				/* 开启一个新线程，在新线程里执行耗时的方法 */  
                new Thread(new Runnable() {  
                    @SuppressWarnings("deprecation")
					@Override  
                    public void run() {
                    	try {
//                    		Message message = new Message();;		//用于传值给主线程
//                    		Bundle bundle = new Bundle();		//用于传值给主线程
                    		//当有数字时
							if(et_count.getText().toString()!=null && !"".equals(et_count.getText().toString())){
								
								long write_count = Long.valueOf(et_count.getText().toString());
								
								//打开提示框
								handler.sendEmptyMessage(open_ProgressDialog);
								Thread.sleep(4000);
								Utils.sentDataToMain(handler, THREAD_SEND_KEY, writeAndCheck_UserData(write_count));							

							//当用户没有输入数字，进入死循环
							}else{
								not_stop_symbol = true;
								pd.setButton(Html.fromHtml("<font color=\"#ff0000\">点击停止</font>"),new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										// TODO Auto-generated method stub
										not_stop_symbol = false;
									}
								});           
								handler.sendEmptyMessage(open_ProgressDialog);
								Utils.sentDataToMain(handler, THREAD_SEND_KEY, writeAndCheck_UserData(1));
							}
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
                    	handler.sendEmptyMessage(close_ProgressDialog);
                    	
                    }  
  
                }).start(); 
               
				Log.d(TAG, "USER DATA WRITE FINISH!!!");
				break;
			//文件选择
			case R.id.btn_file_sel:
				Intent intent = new Intent(MainActivity.this, FileChooseActivity.class);
				startActivityForResult(intent, FILE_CHOOSE_REQUEST_CODE);
				break;
			//自动执行勾选
			case R.id.cb_auto_write_program:
				if(chebox_auto_write_program.isChecked()){			//勾选动作
					btn_program_write.setEnabled(false);
					timer.setBase(SystemClock.elapsedRealtime());  
					timer.start();
					spUtils.set_checkbox_auto_write_program(true);		//记录勾选动作
				}else{			//取消勾选动作
					btn_program_write.setEnabled(true);
					timer.setBase(SystemClock.elapsedRealtime());   
					timer.stop();
					spUtils.set_checkbox_auto_write_program(false);		//记录勾选动作
				}
				break;
			//删除测试缓存
			case R.id.btn_delete_temp:
				test_file = new File(root_folder_path,test_temp_file_name);
				if(test_file.exists()){
					test_file.delete();
				}
				Toast.makeText(getApplicationContext(), "删除完成", Toast.LENGTH_LONG).show();
				break;
			//从u盘读取数据写入用户数据区
			case R.id.btn_write_out:
				
				Log.d(TAG, "begin write out userdata!!!!");
				
				if(tv_out_path.getText().toString()!=null && !"".equals(tv_out_path.getText().toString())){
					out_file = new File(tv_out_path.getText().toString());
					if(out_file!=null && out_file.exists()){
						pd = new ProgressDialog(MainActivity.this);  
						pd.setCanceledOnTouchOutside(false);
						pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);  
						pd.setTitle("正在写入");  
						pd.setMessage("写入数据中，请稍后……");  
						pd.setIndeterminate(false);  
						pd.setCancelable(false);
						temp_file = new File(root_folder_path,test_temp_file_name);
						if(temp_file.exists()){
							temp_file.delete();
						}
						btn_write_out.setEnabled(false);
						new Thread(new Runnable() {  
		                    @SuppressWarnings("deprecation")
							@Override  
		                    public void run() {
		                    	try {
		                    		//当有数字时
									if(et_count_out.getText().toString()!=null && !"".equals(et_count_out.getText().toString())){
										long write_count = Long.valueOf(et_count_out.getText().toString());
										handler.sendEmptyMessage(open_ProgressDialog);
										
										Thread.sleep(4000);
										Utils.sentDataToMain(handler, THREAD_SEND_KEY, writeoutUserData(write_count));

									//当用户没有输入数字，进入死循环
									}else{
										not_stop_symbol = true;
										pd.setButton(Html.fromHtml("<font color=\"#ff0000\">点击停止</font>"),new DialogInterface.OnClickListener() {
											@Override
											public void onClick(DialogInterface dialog, int which) {
												// TODO Auto-generated method stub
												not_stop_symbol = false;
											}
										});           
										handler.sendEmptyMessage(open_ProgressDialog);
										Utils.sentDataToMain(handler, THREAD_SEND_KEY, writeoutUserData(0));
									}
								} catch (Exception e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
		                    	
		                    	handler.sendEmptyMessage(close_ProgressDialog);
		                    	
		                    }  
		  
		                }).start(); 
		               
						Log.d(TAG, "USER DATA WRITE FINISH!!!");
						
					}else{
						Toast.makeText(getApplicationContext(), "请选择数据文件", Toast.LENGTH_LONG).show();
					}
				}
				
				break;
			case R.id.btn_out_sel:
				Intent intent1 = new Intent(MainActivity.this, OutFileChooseActivity.class);
				startActivityForResult(intent1, OUT_FILE_CHOOSE_REQUEST_CODE);
				break;
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			Log.e(TAG, "ONCLICK ERROR!");
		}
	}

	/**
	 * Activity关闭的时候执行
	 */
	@Override
	protected void onDestroy()  {
		// TODO Auto-generated method stub
		super.onDestroy();
		
		if(Utils.log_writer!=null){
			try {
				Utils.log_writer.flush();
				Utils.log_writer.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Utils.log_writer = null;
		}
		if(test_file_writer!=null){
			try {
				test_file_writer.flush();
				test_file_writer.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			test_file_writer = null;
		}
		if(test_file_reader!=null){
			try {
				test_file_reader.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			test_file_reader = null;
		}
		if(tempBufferReader!=null){
			try {
				tempBufferReader.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			tempBufferReader = null;
		}
	}

	/**
	 * 接收文件选择返回的路径
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(resultCode == RESULT_OK){
			switch(requestCode){
				//返回选择的地址
				case FILE_CHOOSE_REQUEST_CODE:
					String fileChoosePath = data.getStringExtra(EXTRA_FILE_CHOOSER);
					if(fileChoosePath!=null && !"".equals(fileChoosePath)){
						tv_savepath.setText(fileChoosePath + "/" + log_file_name);
					}
					break;
				case OUT_FILE_CHOOSE_REQUEST_CODE:
					// 获取路径名
					String outfileChoosePath = data.getStringExtra(OUT_EXTRA_FILE_CHOOSER);
					if (outfileChoosePath != null && !"".equals(outfileChoosePath)) {
						tv_out_path.setText(outfileChoosePath);
					} else {
						tv_out_path.setText("");
					}
					break;
			}
		}
		
	}
	
	/**
	 * 计时的触发
	 */
	@Override
	public void onChronometerTick(Chronometer chronometer) {
		// TODO Auto-generated method stub
		switch(chronometer.getId()){
		case R.id.timer:
			//倒计时5秒
			if(SystemClock.elapsedRealtime() - timer.getBase()> 5 * 1000){
				timer.stop();
				btn_program_write.performClick();	//触发刷机按钮
			}
			break;
		}
	}
	
	/**
	 * 疯狂写和验证测试数据
	 * @throws Exception 
	 */
	private String writeAndCheck_UserData(long count) throws Exception {
		long i = 0;
		long data_length_count = 0;
		long data_length_count_temp = 0;
//		double data_length_count_mb = 0;
		StringBuffer temp = new StringBuffer();
		String msg = "";
		tempBufferReader = new BufferedReader(test_file_reader);
		
		//开始写入
		while(i < count || not_stop_symbol){
			
			try{
				test_file_writer.write(write_test_data,0,write_test_data.length());		//写入测试数据
				test_file_writer.flush();
				
			}catch(Exception e){
				//输入日志
				Utils.WriteLog(USERDATA_TAG,tv_savepath.getText().toString(), false, 0,0,e.toString());
				
				tv_result.setText("ERROR");
			}
			
			temp.append(write_test_data);
			data_length_count_temp = data_length_count_temp + write_test_data.length();
			i++;
			
			//当输入的数据超出一定范围
			if(data_length_count_temp > 250000){
				//先判断写入和读出是否一致，不一致则提前停止
				
				if(!temp.toString().equals(tempBufferReader.readLine())){
					msg = "写入出错！尝试写入的数据与实际写入的数据不一致!";
		    		Utils.WriteLog(USERDATA_TAG,tv_savepath.getText().toString(), false, data_length_count_temp,test_file.length(),msg);
					return msg + "尝试写入：" + data_length_count_temp + "字节 , 成功写入：" + test_file.length() + "字节";
				}
				temp.delete(0, temp.length());
				temp.setLength(0);
				data_length_count = data_length_count + data_length_count_temp;
				data_length_count_temp = 0;
			}
			
			//判断是否超出存储空间
			if(Utils.getFreeSpace(root_folder_path) <= write_test_data.length()){
				data_length_count = data_length_count + data_length_count_temp;
				if(!temp.toString().equals(tempBufferReader.readLine())){
					msg = "写入出错！尝试写入的数据与实际写入的数据不一致!!";
		    		Utils.WriteLog(USERDATA_TAG,tv_savepath.getText().toString(), false, data_length_count,test_file.length(),msg);
				}else{
					msg = "写入结束！存储空间不足!!";
					Utils.WriteLog(USERDATA_TAG,tv_savepath.getText().toString(), true, data_length_count,test_file.length(),msg);
				}
				
				return msg + "尝试写入：" + data_length_count + "字节 , 成功写入：" + test_file.length() + "字节";
			}
			
		}
		data_length_count = data_length_count + data_length_count_temp;
		String buffer;
//		if(!temp.toString().equals(tempBufferReader.readLine())){
		if((buffer=tempBufferReader.readLine())!=null && !temp.toString().equals(buffer)){
			
			msg = "写入出错！尝试写入的数据与实际写入的数据不一致!!!";
    		Utils.WriteLog(USERDATA_TAG,tv_savepath.getText().toString(), false, data_length_count,test_file.length(),msg);
		}
    	
    	//大小不符合，输出报错日志
		else if(test_file.length() != data_length_count){
    		msg = "写入出错！尝试输入字节数与实际输入字节数不相等!!!";
    		Utils.WriteLog(USERDATA_TAG,tv_savepath.getText().toString(), false, data_length_count,test_file.length(),msg);
    	}else{		//写入数据没有问题
    		msg = "写入成功！";
    		Utils.WriteLog(USERDATA_TAG,tv_savepath.getText().toString(), true, data_length_count,test_file.length(),"");
    	}
    	
    	return msg + "尝试写入：" + data_length_count + "字节, 成功写入：" + test_file.length() + "字节";
    	
	}
	
	/**
	 * 疯狂写和验证外部写入的测试数据
	 * @throws Exception 
	 */
	private String writeoutUserData(long write_count){
		int err_Times = 0;
		int count = 0;
		while(count<write_count || not_stop_symbol){
			Log.d(TAG, "begin copy out user data");
			try {
				Utils.copy_file(out_file, temp_file);
				if(!SVMD5Util.getFileMD5String(out_file).equals(SVMD5Util.getFileMD5String(temp_file))){
					Utils.WriteLog(USERDATA_TAG,tv_savepath.getText().toString(), false, out_file.length(),temp_file.length(),"写入失败！MD5验证不通过！");
					err_Times++;
				}else{
					Utils.WriteLog(USERDATA_TAG,tv_savepath.getText().toString(), true, out_file.length(),temp_file.length(),"");
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
				err_Times++;
				try {
					Utils.WriteLog(USERDATA_TAG,tv_savepath.getText().toString(), false, out_file.length(),temp_file.length(),e.toString());
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}finally{
				count++;
				Log.d(TAG, "finish copy out user data");
			}
			
		}
		return "写入"+ count + "次，出错" + err_Times + "次";
	}
	
	
	/**
	 * 用于传递主线程与子线程之间的数据
	 */
	Handler handler = new Handler() {  
	    @Override  
	    public void handleMessage(Message msg) {	// handler接收到消息后就会执行此方法  
	        switch(msg.what){
	        case close_ProgressDialog:
	        	pd.dismiss();// 关闭ProgressDialog  
	        	break;
	        case open_ProgressDialog:
	        	pd.show();// 显示ProgressDialog  
	        	break;
	        case show_write_userdata_result:
				tv_result.setText(Html.fromHtml("<font color=\"#ff0000\">"+msg.getData().getString(THREAD_SEND_KEY)+"</font>"));			//显示结果
				btn_write_out.setEnabled(true);
//				write_user_data_result = "";
	        	break;
	        case 3:
	        	try {
					if(!SVMD5Util.getFileMD5String(out_file).equals(SVMD5Util.getFileMD5String(temp_file))){
						tv_result.setText(Html.fromHtml("<font color=\"#ff0000\">写入失败！MD5验证不通过！</font>"));
						Utils.WriteLog(USERDATA_TAG,tv_savepath.getText().toString(), false, out_file.length(),temp_file.length(),"写入失败！MD5验证不通过！");
					}else{
						tv_result.setText(Html.fromHtml("<font color=\"#ff0000\">成功写入"+temp_file.length()+"字节</font>"));
						Utils.WriteLog(USERDATA_TAG,tv_savepath.getText().toString(), true, out_file.length(),temp_file.length(),"");
					}
					pd.dismiss();// 关闭ProgressDialog
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}finally{
					pd.dismiss();// 关闭ProgressDialog  
				}
	        	break;
	        }
	    	
	    }  
	};  

	
}