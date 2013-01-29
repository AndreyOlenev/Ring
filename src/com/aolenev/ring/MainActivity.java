package com.aolenev.ring;

/**
 * 1. open camera
 * 2. overlay outline of hand on camera view. This is what instructs the user on where to place their hand.
 * 3. Allows user to snap the photo
 * 4. Displays the photo just taken with a static ring image on top.
 * 5. Allows the user to tap "Save" and save the image to their photo library.
 */

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.Window;
import android.view.WindowManager;
import android.view.SurfaceView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class MainActivity extends Activity implements SurfaceHolder.Callback, View.OnClickListener, Camera.PictureCallback, Camera.PreviewCallback, Camera.AutoFocusCallback {
	private Camera		camera;
	private SurfaceHolder	surfaceHolder;
	private SurfaceView	preview;
	private ImageButton		shotBtn;
	public static String		screenSize;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Portret orientation
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		// Full Screen
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.activity_main);

		preview = (SurfaceView) findViewById(R.id.mySurfaceView);

		surfaceHolder = preview.getHolder();
		surfaceHolder.addCallback(this);

		shotBtn = (ImageButton) findViewById(R.id.shotButton);
		shotBtn.setOnClickListener(this);
		
		ImageView handOverlay = (ImageView) findViewById(R.id.handOverlay);
		final Drawable handImage = getResources().getDrawable(R.drawable.hand1);
		handOverlay.setImageDrawable(handImage);

		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		
		//Log.e("start res", "w: " + dm.widthPixels + " h: " + dm.heightPixels);
		
		int width = dm.widthPixels;
		int height = dm.widthPixels * 4 / 3;
		
		if (dm.widthPixels == 480){
			screenSize = "hdpi";
		} else if (dm.widthPixels == 320) {
			screenSize = "ldpi";
		}
		
		//Log.e("res", "w: " + width + " h: " + height);		
		
		android.widget.FrameLayout.LayoutParams params = new android.widget.FrameLayout.LayoutParams(width, height);
		preview.setLayoutParams(params);
		showPopup(this);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		camera = Camera.open();
		camera.setDisplayOrientation(90);
	}

	@Override
	protected void onPause() {
		super.onPause();

		if (camera != null) {
			camera.setPreviewCallback(null);
			camera.stopPreview();
			camera.release();
			camera = null;
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		try {
			camera.setPreviewDisplay(holder);
			camera.setPreviewCallback(this);
		} catch (IOException e) {
			e.printStackTrace();
		}

		Size previewSize = camera.getParameters().getPreviewSize();
		float aspect = (float) previewSize.width / previewSize.height;

		int previewSurfaceWidth = preview.getWidth();
		int previewSurfaceHeight = preview.getHeight();

		LayoutParams lp = preview.getLayoutParams();

		if (this.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
			camera.setDisplayOrientation(90);
			lp.height = previewSurfaceHeight;
			lp.width = (int) (previewSurfaceHeight / aspect);
		} else {
			camera.setDisplayOrientation(0);
			lp.width = previewSurfaceWidth;
			lp.height = (int) (previewSurfaceWidth / aspect);
		}
		

		preview.setLayoutParams(lp);
		camera.startPreview();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
	}

	@Override
	public void onClick(View v) {
		if (v == shotBtn) {

			// camera.takePicture(null, null, null, this);
			camera.autoFocus(this);
		}
	}

	@Override
	public void onPictureTaken(byte[] paramArrayOfByte, Camera paramCamera) {
		try {
			File saveDir = new File(Environment.getExternalStorageDirectory().getPath(), "Ring");

			if (!saveDir.exists()) {
				saveDir.mkdirs();
			}
			
			File file = new File(saveDir, System.currentTimeMillis() + ".jpg");
			
			FileOutputStream os = new FileOutputStream(file);
			os.write(paramArrayOfByte);
			os.close();
			startPreview(file.getPath());
		} catch (Exception e) {
		}

	}

	@Override
	public void onAutoFocus(boolean paramBoolean, Camera paramCamera) {
		if (paramBoolean) {
			paramCamera.takePicture(null, null, null, this);
		}
	}

	@Override
	public void onPreviewFrame(byte[] paramArrayOfByte, Camera paramCamera) {
		
	}
		
	public void startPreview(String drawable) {
		
		Intent i = new Intent(this, Preview.class);
		i.putExtra("Image", drawable);
		startActivityForResult(i, 0);	
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, final Intent data) {
		if (resultCode == 0) finish();
	}
	
	private void showPopup(final Activity context) {
		LinearLayout viewGroup = (LinearLayout) context.findViewById(R.id.popup);
		LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View layout = layoutInflater.inflate(R.layout.popup_layout, viewGroup);

		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
		alertDialogBuilder.setView(layout);
		final AlertDialog alertDialog = alertDialogBuilder.create();
		//alertDialog.dismiss();
		alertDialog.show();

	}
}