package com.aolenev.ring;

import java.io.File;
import java.io.FileOutputStream;

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
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class Preview extends Activity {

	ImageView		imageView;
	private ImageView	ringImageOverlay;
	public int		screenS	= 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.preview_layout);
		
		ringImageOverlay = (ImageView) findViewById(R.id.ringImageOverlay);
		
		if (MainActivity.screenSize == "hdpi") screenS = 0;
		else screenS = 1;
		
		
		ringImageOverlay.post(new Runnable() {
			
			@Override
			public void run() {
				Drawable ringImage;
				if (screenS == 0) ringImage = getResources().getDrawable(R.drawable.ringh);
				else ringImage = getResources().getDrawable(R.drawable.ringl);
				ringImageOverlay.setImageDrawable(ringImage);
			}
		});
		
		Bitmap bitmap = ((BitmapDrawable)getResources().getDrawable(R.drawable.ringh)).getBitmap();		
		RelativeLayout myLayout = (RelativeLayout) findViewById(R.id.previewRelativeLayout);
		final PanAndZoomListener listener = new PanAndZoomListener(ringImageOverlay, bitmap);
		myLayout.setOnTouchListener(listener);
		
		android.widget.RelativeLayout.LayoutParams params = new android.widget.RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		if (screenS == 0)params.setMargins(123, 255, 0, 0);
		else params.setMargins(92, 165, 0, 0);
		ringImageOverlay.setLayoutParams(params);		

		imageView = (ImageView) findViewById(R.id.imagePreview);
		final String filePath = getIntent().getSerializableExtra("Image").toString();
		final Drawable image = resize(Drawable.createFromPath(filePath), this);
		final Bitmap ring = ((BitmapDrawable)getResources().getDrawable(R.drawable.ringh)).getBitmap();
		Bitmap photo = ((BitmapDrawable) image).getBitmap();
		
		Matrix matrix = new Matrix();
		matrix.postRotate(90);
		final Bitmap photoViewed = Bitmap.createBitmap(photo, 0, 0, photo.getWidth(), photo.getHeight(), matrix, true);		
		
		imageView.post(new Runnable() {

			@Override
			public void run() {
				imageView.setImageBitmap(photoViewed);
			}
		});
		
		Button cancel = (Button) findViewById(R.id.cancel);
		cancel.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				setResult(1);
				File file = new File(filePath);
				file.delete();
				finish();				
			}
		});
		Button save = (Button) findViewById(R.id.saveFileButton);
		save.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Matrix matrix = new Matrix();
				float scale = PanAndZoomListener.finalScale;
				float rotation = PanAndZoomListener.rotation;
				matrix.postScale(scale, scale);
				matrix.postRotate(rotation);	
				Bitmap result;
				Log.e("", "" + ringImageOverlay.getImageMatrix());
				if (screenS == 0) { 
					Bitmap temp = Bitmap.createBitmap(ring, 0, 0, ring.getWidth(), ring.getHeight(), ringImageOverlay.getImageMatrix(), true);
					result = overlay(photoViewed, temp, 123 - (temp.getWidth() - ring.getWidth()) / 2, 255 - (temp.getHeight() - ring.getHeight()) / 2);
					//saveFile(result, filePath);
				}
				else { 
					Bitmap temp = Bitmap.createBitmap(ring, 0, 0, ring.getWidth(), ring.getHeight(), ringImageOverlay.getImageMatrix(), true);
					result = overlay(photoViewed, temp, 92 - (temp.getWidth() - ring.getWidth() / 2), 165 - (temp.getHeight() - ring.getHeight()) / 2);
					//saveFile(result, filePath);
				}
				listener.clearRingOverlay();
				Drawable resultDrawable = new BitmapDrawable(getResources(), result);
				//resultDrawable.setColorFilter(getResources().getColor(R.color.lightBlue), PorterDuff.Mode.MULTIPLY);
				//imageView.setImageBitmap(result);
				imageView.setImageDrawable(resultDrawable);
				
				//setResult(0);
				//finish();
			}
		});
	}
	
	public void saveFile(final Bitmap result, final String filePath) {		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					File file = new File(filePath);
					
					FileOutputStream os = new FileOutputStream(file);
					result.compress(Bitmap.CompressFormat.PNG, 100, os);
					//os.write(rtesultPhoto);
					//os.close();
				} catch (Exception e) {
					e.printStackTrace();
				}				
			}
		}).start();
	}
	
//	private Drawable resize(Drawable image) {
//		Bitmap d = ((BitmapDrawable) image).getBitmap();
//		Bitmap bitmapOrig = Bitmap.createScaledBitmap(d, 336, 800, false);
//		return new BitmapDrawable(this.getResources(), bitmapOrig);
//	}
	
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
		Double screenWidth = (double) dm.widthPixels * 1.0;
		Double pow = imageHeight / screenWidth;
		imageHeight = imageHeight / pow;
		imageWidth = imageWidth / pow;

		Bitmap bitmapOrig = Bitmap.createScaledBitmap(d, imageWidth.intValue(), imageHeight.intValue(), false);
		return new BitmapDrawable(context.getResources(), bitmapOrig);
	}

	private Bitmap overlay(Bitmap bmp1, Bitmap bmp2, float x, float y) {
		Bitmap bmOverlay = Bitmap.createBitmap(bmp1.getWidth(), bmp1.getHeight(), bmp1.getConfig());
		Canvas canvas = new Canvas(bmOverlay);
		canvas.drawBitmap(bmp1, new Matrix(), null);
		canvas.drawBitmap(bmp2, x, y, null);
		return bmOverlay;
	}
}

