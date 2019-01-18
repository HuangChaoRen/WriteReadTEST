package com.shareprence;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class SharePreferenceUtil {
	private SharedPreferences sp;   
	private Editor editor;   
	

	public SharePreferenceUtil(Context context, String file) {   
        sp = context.getSharedPreferences(file, context.MODE_PRIVATE);   
        editor = sp.edit();   
    }   
  
    //记录是否自动刷机
    public void set_checkbox_auto_write_program(boolean state) {   
        editor.putBoolean("auto_write_program", state);   
        editor.commit();   
    }   
   
    public boolean get_checkbox_auto_write_program() {   
        return sp.getBoolean("auto_write_program",false);   
    }   

}
