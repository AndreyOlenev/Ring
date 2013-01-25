package com.aolenev.ring;

import java.io.Serializable;

import android.graphics.drawable.Drawable;

public class MyDrawable implements Serializable {

	private Drawable	image;

	public MyDrawable(Drawable image) {
		this.image = image;
	}
	
	public void setDrawable (Drawable newImage) {
		this.image = newImage;
	}
	
	public Drawable getDrawable() {
		return this.image;
	}
}