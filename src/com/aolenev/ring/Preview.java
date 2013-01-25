package com.aolenev.ring;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

public class Preview extends Activity {
	
	ImageView	imageView;
	public float	scale;
	public float	rotate;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.preview_layout);
		scale = MainActivity.getScale() * PanAndZoomListener.finalScale;
		rotate = PanAndZoomListener.rotation;
		Log.e("scale", "value: " + scale);

		imageView = (ImageView) findViewById(R.id.imagePreview);
		final Drawable image = resize(Drawable.createFromPath(getIntent().getSerializableExtra("Image").toString()), this);
		Bitmap ring = ((BitmapDrawable)getResources().getDrawable(R.drawable.face_circle_tiled2)).getBitmap();
		final Bitmap photo = ((BitmapDrawable) image).getBitmap();
		
		Matrix matrix = new Matrix();
		matrix.postScale(scale, scale);
		matrix.postRotate(rotate);
		
		ring = Bitmap.createBitmap(ring, 0, 0, ring.getWidth(), ring.getHeight(), matrix, true);
		
		final Bitmap result = overlay(photo, ring);
		imageView.post(new Runnable() {

			@Override
			public void run() {
				imageView.setImageBitmap(result);
			}
		});
		
		Button cancel = (Button) findViewById(R.id.cancel);
		cancel.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();				
			}
		});
		
		Button save = (Button) findViewById(R.id.saveFileButton);
		save.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}
	
//	private static Bitmap resizeByValue(Bitmap d, float value){
//		Double imageHeight = (double) d.getHeight() * value;
//		Double imageWidth = (double) d.getWidth() * value;
//		return Bitmap.createScaledBitmap(d, imageWidth.intValue(), imageHeight.intValue(), false);
//	}
	
	private static Drawable resize(Drawable image, Context context) {
		Bitmap d = ((BitmapDrawable) image).getBitmap();
		Double imageHeight = (double) d.getHeight();
		Double imageWidth = (double) d.getWidth();
		DisplayMetrics dm = new DisplayMetrics();
		((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(dm);
		Double screenHeight = (double) dm.heightPixels * 1.0;
		Double pow = imageHeight / screenHeight;
		imageHeight = imageHeight / pow;
		imageWidth = imageWidth / pow;

		Bitmap bitmapOrig = Bitmap.createScaledBitmap(d, imageWidth.intValue(), imageHeight.intValue(), false);
		return new BitmapDrawable(context.getResources(), bitmapOrig);
	}
	
	private Bitmap overlay(Bitmap bmp1, Bitmap bmp2) {
	        Bitmap bmOverlay = Bitmap.createBitmap(bmp1.getWidth(), bmp1.getHeight(), bmp1.getConfig());
	        Canvas canvas = new Canvas(bmOverlay);
	        canvas.drawBitmap(bmp1, new Matrix(), null);
	        canvas.drawBitmap(bmp2, bmp1.getWidth() / 2 - bmp2.getWidth() / 2, bmp1.getHeight() / 2 - bmp2.getHeight() / 2, null);
	        return bmOverlay;
	    }

}
