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
	public int		screenS		= 0;
	private int		ringPositionX;
	private int		ringPositionY;
	Bitmap			ringBitmap;
	private int[]		rings		= { R.drawable.ring1, R.drawable.ring2, R.drawable.ring3, R.drawable.ring4, R.drawable.ring5 };
	private int		currentRing	= 0;

	public void printRing(Drawable ring) {
		ringBitmap = ((BitmapDrawable) ring).getBitmap();
		ringImageOverlay.setImageDrawable(ring);

		android.widget.RelativeLayout.LayoutParams params = new android.widget.RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

		/** calculate ring position */
		double coef = MainActivity.screenHeight / 800.0;
		ringPositionX = (int) (coef * 162) - (ringBitmap.getHeight() / 2);
		ringPositionY = (int) ((coef * 288) + (MainActivity.screenWidth - 480) / 2) - (ringBitmap.getWidth() / 2);

		params.setMargins(ringPositionX, ringPositionY, 0, 0);
		ringImageOverlay.setLayoutParams(params);
		ringImageOverlay.setRotation(PanAndZoomListener.rotation);
		ringImageOverlay.setScaleX(PanAndZoomListener.finalScale);
		ringImageOverlay.setScaleY(PanAndZoomListener.finalScale);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setResult(1);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.preview_layout);
		ringImageOverlay = (ImageView) findViewById(R.id.ringImageOverlay);

		imageView = (ImageView) findViewById(R.id.imagePreview);
		final String filePath = getIntent().getSerializableExtra("Image").toString();
		Bitmap photo = ((BitmapDrawable) resize(Drawable.createFromPath(filePath), this)).getBitmap();

		/** rotate resized photo */
		Matrix matrix = new Matrix();
		matrix.postRotate(90);
		final Bitmap photoViewed = Bitmap.createBitmap(photo, 0, 0, photo.getWidth(), photo.getHeight(), matrix, true);
		imageView.setImageBitmap(photoViewed);

		/** set first ring */
		Drawable ring = getResources().getDrawable(R.drawable.ring1);
		ringBitmap = ((BitmapDrawable) ring).getBitmap();
		ringImageOverlay.setImageDrawable(ring);
		RelativeLayout myLayout = (RelativeLayout) findViewById(R.id.previewRelativeLayout);
		final PanAndZoomListener listener = new PanAndZoomListener(ringImageOverlay, ringBitmap);
		myLayout.setOnTouchListener(listener);
		/** set touch listener */

		printRing(ring);

		/**
		 * example changing ring image
		 */
		Button change = (Button) findViewById(R.id.changeRing);
		change.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (currentRing >= 4)
					currentRing = 0;
				else
					currentRing++;
				printRing(getResources().getDrawable(rings[currentRing]));

			}
		});

		Button cancel = (Button) findViewById(R.id.cancel);
		cancel.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				setResult(1);
				finish();
			}
		});
		Button save = (Button) findViewById(R.id.saveFileButton);
		save.setOnClickListener(new View.OnClickListener() {

			/**
			 * in this method we create photo with overlay. 
			 * Bitmap result - result image, you can apply some filters to
			 * image if you need
			 */
			@Override
			public void onClick(View v) {
				Matrix matrix = new Matrix();
				float scale = PanAndZoomListener.finalScale;
				float rotation = PanAndZoomListener.rotation;
				matrix.postScale(scale, scale);
				matrix.postRotate(rotation);
				Bitmap result;

				Bitmap temp = Bitmap.createBitmap(ringBitmap, 0, 0, ringBitmap.getWidth(), ringBitmap.getHeight(), matrix, true);
				result = overlay(photoViewed, temp, ringPositionX, ringPositionY);
				saveFile(result, filePath);

//				Drawable resultDrawable = new BitmapDrawable(getResources(), result);
//				resultDrawable.setColorFilter(getResources().getColor(R.color.lightBlue), PorterDuff.Mode.MULTIPLY);
				setResult(0);
				finish();
			}
		});
	}

	public void saveFile(final Bitmap result, final String filePath) {
		/** new thread to save file */
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					File file = new File(filePath);

					FileOutputStream os = new FileOutputStream(file);
					result.compress(Bitmap.CompressFormat.PNG, 100, os);
					os.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

//	private static Bitmap resizeByValue(Bitmap d, float value) {
//		Double imageHeight = (double) d.getHeight() * value;
//		Double imageWidth = (double) d.getWidth() * value;
//		return Bitmap.createScaledBitmap(d, imageWidth.intValue(), imageHeight.intValue(), false);
//	}

	/** resize photo */
	private static Drawable resize(Drawable image, Context context) {
		Bitmap d = ((BitmapDrawable) image).getBitmap();
		Double imageHeight = (double) d.getHeight();
		Double imageWidth = (double) d.getWidth();
		Double pow = imageHeight / MainActivity.screenWidth;
		imageHeight = imageHeight / pow;
		imageWidth = imageWidth / pow;

		Bitmap bitmapOrig = Bitmap.createScaledBitmap(d, imageWidth.intValue(), imageHeight.intValue(), false);
		return new BitmapDrawable(context.getResources(), bitmapOrig);
	}

	private Bitmap overlay(Bitmap bmp1, Bitmap bmp2, float x, float y) {

		/**
		 * if we change rotation or scale of ring we need to calculate
		 * shift of image
		 */
		int shiftX = (ringBitmap.getWidth() - bmp2.getWidth()) / 2;
		int shiftY = (ringBitmap.getHeight() - bmp2.getHeight()) / 2;

		Bitmap bmOverlay = Bitmap.createBitmap(bmp1.getWidth(), bmp1.getHeight(), bmp1.getConfig());
		Canvas canvas = new Canvas(bmOverlay);
		canvas.drawBitmap(bmp1, new Matrix(), null);
		canvas.drawBitmap(bmp2, x + shiftX, y + shiftY, null);
		return bmOverlay;
	}
}
