package com.wenjin.bean;

import android.graphics.Bitmap;

/**
 * 消息pojo
 * 
 * @author Administrator
 * 
 */
public class Message {
	private int id;
	private String send_ctn;
	private String send_person;
	private String send_date;
	private Bitmap bitmap;
	private String msgId;
	public String getSend_ctn() {
		return send_ctn;
	}

	public void setSend_ctn(String send_ctn) {
		this.send_ctn = send_ctn;
	}

	public String getSend_person() {
		return send_person;
	}

	public void setSend_person(String send_person) {
		this.send_person = send_person;
	}

	public String getSend_date() {
		return send_date;
	}

	public void setSend_date(String send_date) {
		this.send_date = send_date;
	}

	public Bitmap getBitmap() {
		return bitmap;
	}

	public void setBitmap(Bitmap bitmap) {
		this.bitmap = bitmap;
	}

	public Message(String send_ctn, String send_person, String send_date,
			Bitmap bitmap) {
		super();
		this.send_ctn = send_ctn;
		this.send_person = send_person;
		this.send_date = send_date;
		this.bitmap = bitmap;
	}

	public Message() {
		super();
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getMsgId() {
		return msgId;
	}

	public void setMsgId(String msgId) {
		this.msgId = msgId;
	}
	
}
