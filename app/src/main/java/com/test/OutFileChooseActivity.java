package com.test;

import java.io.File;
import java.util.ArrayList;

import com.adapter.FileChooserAdapter;
import com.domain.FileInfo;
import com.example.writereadtest.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class OutFileChooseActivity extends Activity{
	private ListView lvFileChooser;
	private View btnBack;
	private TextView tvBack;
//	private TextView mTvPath;

	private String mSdcardRootPath; // sdcard 根路径
	private String mLastFilePath; // 当前显示的路径

	private ArrayList<FileInfo> mFileLists;
	private FileChooserAdapter mAdatper;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.out_activity_file_chooser);

//		mSdcardRootPath = Environment.getExternalStorageDirectory().getAbsolutePath();
		mSdcardRootPath = Environment.getExternalStorageDirectory().getParentFile().getParentFile().getAbsolutePath();

		btnBack = findViewById(R.id.btnBack);
		btnBack.setOnClickListener(mClickListener);

		tvBack = (TextView) findViewById(R.id.tvBack);
		lvFileChooser = (ListView) findViewById(R.id.lvFileChooser);
		lvFileChooser.setEmptyView(findViewById(R.id.tvEmptyHint));
		lvFileChooser.setOnItemClickListener(mItemClickListener);
		setGridViewAdapter(mSdcardRootPath);
	}

	// 配置适配器
	private void setGridViewAdapter(String filePath) {
		
		updateFileItems(filePath);
		mAdatper = new FileChooserAdapter(this, mFileLists);
		lvFileChooser.setAdapter(mAdatper);
	}

	// 根据路径更新数据，并且通知Adatper数据改变
	private void updateFileItems(String filePath) {
		mLastFilePath = filePath;

		if (!mLastFilePath.equals(mSdcardRootPath)) {
			tvBack.setText("上一步");
		} else { // 是sdcard路径 ，直接结束
			tvBack.setText("返回");
		}
		if (mFileLists == null)
			mFileLists = new ArrayList<FileInfo>();
		if (!mFileLists.isEmpty())
			mFileLists.clear();

		File[] files = folderScan(filePath);
		if (files == null)
			return;

		for (int i = 0; i < files.length; i++) {
			if (files[i].isHidden()) // 不显示隐藏文件
				continue;

			//隐藏不存在的外部存储目录
			if(files[i] != null && mLastFilePath.equals("/storage")){
				if(files[i].getName().equals("usb0") && !Utils.isMounted("/storage/usb0")){
					continue;
				}else if(files[i].getName().equals("usb1")  && !Utils.isMounted("/storage/usb1")){
					continue;
				}else if(files[i].getName().equals("sdcard1")  && !Utils.isMounted("/storage/sdcard1")){
					continue;
				}
			}
			
			String fileAbsolutePath = files[i].getAbsolutePath();
			String fileName = files[i].getName();
			boolean isDirectory = false;
			if (files[i].isDirectory()) {
				isDirectory = true;
			}
			FileInfo fileInfo = new FileInfo(fileAbsolutePath, fileName, isDirectory);
			mFileLists.add(fileInfo);
		}
		// When first enter , the object of mAdatper don't initialized
		if (mAdatper != null)
			mAdatper.notifyDataSetChanged(); // 重新刷新
	}

	// 获得当前路径的所有文件
	private File[] folderScan(String path) {
		File file = new File(path);
		File[] files = file.listFiles();
		return files;
	}

	private View.OnClickListener mClickListener = new OnClickListener() {
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.btnBack:
				backProcess();
				break;
			default:
				break;
			}
		}
	};

	private AdapterView.OnItemClickListener mItemClickListener = new OnItemClickListener() {
		public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
			FileInfo fileInfo = (FileInfo) (((FileChooserAdapter) adapterView.getAdapter()).getItem(position));
			if (fileInfo.isDirectory()) { // 点击项为文件夹, 显示该文件夹下所有文件
				updateFileItems(fileInfo.getFilePath());
			} else { // 如果是支持的发送文件，则将该路径通知给调用者
				try {
					File file = new File(fileInfo.getFilePath());
					Intent intent = new Intent();
					intent.putExtra(MainActivity.OUT_EXTRA_FILE_CHOOSER, fileInfo.getFilePath());
					setResult(RESULT_OK, intent);
					finish();
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	};

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			backProcess();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	// 返回上一层目录的操作
	public void backProcess() {
		// 判断当前路径是不是sdcard路径 ， 如果不是，则返回到上一层。
		if (!mLastFilePath.equals(mSdcardRootPath)) {
			File thisFile = new File(mLastFilePath);
			String parentFilePath = thisFile.getParent();
			updateFileItems(parentFilePath);
		} else { // 是sdcard路径 ，直接结束
			setResult(RESULT_CANCELED);
			finish();
		}
	}
}
