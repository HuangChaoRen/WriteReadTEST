package com.adapter;

import java.io.File;
import java.util.ArrayList;

import com.domain.FileInfo;
import com.example.writereadtest.R;
import com.test.DataCleanManager;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;



public class FileChooserAdapter extends BaseAdapter {
	public static final int FILE_DIRECTORY = 0x00;// 
	public static final int FILE_NOT_DIRECTORY = 0x01;// 
	private static final int FILE_TYPE_COUNT = 2;
	
	private ArrayList<FileInfo> mFileLists;
	private LayoutInflater mLayoutInflater = null;



	public FileChooserAdapter(Context context, ArrayList<FileInfo> fileLists) {
		super();
		mFileLists = fileLists;
		mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mFileLists.size();
	}

	@Override
	public FileInfo getItem(int position) {
		// TODO Auto-generated method stub
		return mFileLists.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}
	

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		int fileDirectoryType = getItemViewType(position);
		BaseViewHolder holder = null;
		if (convertView == null && mLayoutInflater != null) {
			holder = new BaseViewHolder();
			switch (fileDirectoryType) {
			case FILE_DIRECTORY:
				convertView = mLayoutInflater.inflate(R.layout.listview_item_directory, parent , false);
				holder = new DirectoryViewHolder();
				fillDirectoryViewHolder((DirectoryViewHolder) holder, convertView);
				convertView.setTag(holder);
				break;
			case FILE_NOT_DIRECTORY:
				convertView = mLayoutInflater.inflate(R.layout.listview_item_directory_no, parent , false);
				holder = new DirectoryNoViewHolder();
				fillDirectoryNoViewHolder((DirectoryNoViewHolder) holder, convertView);
				convertView.setTag(holder);
				break;
			default:
				break;
			}
		} else {
			holder = (BaseViewHolder) convertView.getTag();
		}

		FileInfo fileInfo = getItem(position);
		if(fileDirectoryType == FILE_DIRECTORY){
			hanldeDirectoryFileView((DirectoryViewHolder) holder, fileInfo);
		}else if(fileDirectoryType == FILE_NOT_DIRECTORY){
			hanldeDirectoryNoFileView((DirectoryNoViewHolder)holder, fileInfo);
		}
		return convertView;
	}

	static class BaseViewHolder {
		ImageView imgFileIcon;
		TextView tvFileName;
	}
	
	static class DirectoryViewHolder extends BaseViewHolder{
		
	}
	
	static class DirectoryNoViewHolder extends BaseViewHolder{
		TextView tvFileSize;
	}
	
	
	private void fillBaseViewHolder(BaseViewHolder holder , View convertView){
		holder.imgFileIcon = (ImageView) convertView.findViewById(R.id.imgFileIcon);
		holder.tvFileName = (TextView) convertView.findViewById(R.id.tvFileName);
	}
	
	private void fillDirectoryViewHolder(DirectoryViewHolder directoryViewHolder , View convertView){
		fillBaseViewHolder(directoryViewHolder, convertView);
	}
	
	private void fillDirectoryNoViewHolder(DirectoryNoViewHolder directoryNoHolder , View convertView){
		fillBaseViewHolder(directoryNoHolder, convertView);
		directoryNoHolder.tvFileSize = (TextView) convertView.findViewById(R.id.tvFileSize);
	}
	
	private void hanldeBaseFileView(BaseViewHolder holder , FileInfo fileInfo){
		holder.imgFileIcon.setBackgroundResource(R.drawable.icon_file_directory);
		holder.tvFileName.setText(fileInfo.getFileName());
		holder.tvFileName.setTextColor(Color.GRAY);
	}
	
	private void hanldeDirectoryFileView(DirectoryViewHolder directoryHolder , FileInfo fileInfo){
		hanldeBaseFileView(directoryHolder, fileInfo);
	}
	
	private void hanldeDirectoryNoFileView(DirectoryNoViewHolder directoryNoHolder , FileInfo fileInfo){
//		directoryNoHolder.imgFileIcon.setBackgroundResource(OpenFileUtil.getFileIconResourceId(fileInfo.getFilePath()));
		directoryNoHolder.tvFileName.setText(fileInfo.getFileName());
		directoryNoHolder.tvFileSize.setText(DataCleanManager.getFormatSize(new File(fileInfo.getFilePath()).length()));
	}
	
	@Override
	public int getItemViewType(int position) {
		FileInfo fileInfo = getItem(position);
		if(fileInfo.isDirectory()){
			return FILE_DIRECTORY;
		}else{
			return FILE_NOT_DIRECTORY;
		}
	}
	
	@Override
	public int getViewTypeCount() {
		// TODO Auto-generated method stub
		return FILE_TYPE_COUNT;
	}
	

}
