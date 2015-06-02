package com.wenjin.adapter;

import java.util.List;
import java.util.Timer;

import com.wenjin.bean.Message;
import com.wenjin.bean.User;
import com.wenjin.chatroom.R;
import com.wenjin.tool.ExpressionUtil;

import android.content.Context;
import android.os.Handler;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
/**
 * 聊天页面ListView内容适配器
 */
public class ChatMsgViewAdapter extends BaseAdapter{
	private Context context;
    private LayoutInflater mInflater;
    private List<Message> msgList;
    private User curUser;
    
    public ChatMsgViewAdapter(Context context, User curUser, List<Message> msgList) {
        this.context = context;
        this.msgList = msgList;
        this.curUser = curUser;
        mInflater = LayoutInflater.from(this.context);
    }

    public int getCount() {
        return msgList.size();
    }

    public Object getItem(int position) {
        return msgList.get(position);
    }

    public long getItemId(int position) {
        return position;
    }
    
	public int getItemViewType(int position) {
		// 区别两种view的类型，标注两个不同的变量来分别表示各自的类型
		Message msg = msgList.get(position);
		if (msg.getId() == curUser.getId()) {
			return 0;
		} else {
			return 1;
		}
	}
    
	public int getViewTypeCount() {
		// 这个方法默认返回1，如果希望listview的item都是一样的就返回1，我们这里有两种风格，返回2
		return 2;
	}
	
    public View getView(final int position, View convertView, ViewGroup parent) {
    	final Message msg = msgList.get(position);
    	ViewHolder viewHolder = null;
	    if (convertView == null)
	    {	
	    	  if (msg.getId() == curUser.getId())
			  {	
				  convertView = mInflater.inflate(R.layout.chat_item_right, null);
			  }else{
				  convertView = mInflater.inflate(R.layout.chat_item_left, null);
			  }
	    	  viewHolder = new ViewHolder();
			  viewHolder.tvSendTime = (TextView) convertView.findViewById(R.id.tv_sendtime);
			  viewHolder.tvUserName = (TextView) convertView.findViewById(R.id.tv_username);
			  viewHolder.msgBgView = (View) convertView.findViewById(R.id.chat_msg_bg);
			  viewHolder.ivUserImage = (ImageView) convertView.findViewById(R.id.iv_userhead);
			  convertView.setTag(viewHolder);
	    }else{
	        viewHolder = (ViewHolder) convertView.getTag();
	    }
	    viewHolder.tvSendTime.setText(msg.getSend_date());
	    viewHolder.tvUserName.setText(msg.getSend_person());
	    SpannableString spannableString = ExpressionUtil.getExpressionString(context, msg.getSend_ctn()); 
	    TextView tvContent = (TextView) viewHolder.msgBgView.findViewById(R.id.tv_chatcontent);
	    tvContent.setText(spannableString);
	    viewHolder.msgBgView.setOnClickListener(new View.OnClickListener() {
	    	public void onClick(View v) {
	    	}
	    });

	    if(null!= msg.getBitmap()){
	    	viewHolder.ivUserImage.setImageBitmap(msg.getBitmap());
	    }
	    return convertView;
    }
    
    private class ViewHolder { 
        public TextView tvSendTime;
        public TextView tvUserName;
        public View msgBgView;
        public ImageView ivUserImage;
    }
}
