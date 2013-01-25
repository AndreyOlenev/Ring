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
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.SurfaceHolder;
import android.view.Window;
import android.view.WindowManager;
import android.view.SurfaceView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.FrameLayout;

public class MainActivity extends Activity implements SurfaceHolder.Callback, View.OnClickListener, Camera.PictureCallback, Camera.PreviewCallback, Camera.AutoFocusCallback {
	private Camera		camera;
	private SurfaceHolder	surfaceHolder;
	private SurfaceView	preview;
	private Button		shotBtn;
	private ImageView	ringImageOverlay;
	public static float	scale;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Portret orientation
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

		// Full Screen
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.activity_main);

		preview = (SurfaceView) findViewById(R.id.mySurfaceView);

		surfaceHolder = preview.getHolder();
		surfaceHolder.addCallback(this);
		surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		

		shotBtn = (Button) findViewById(R.id.shotButton);
		//shotBtn.setText("Shot");
		shotBtn.setOnClickListener(this);
		
		ringImageOverlay = (ImageView) findViewById(R.id.ringImageOverlay);
		final Drawable ringImage = getResources().getDrawable(R.drawable.face_circle_tiled2);
		ringImageOverlay.post(new Runnable() {
			
			@Override
			public void run() {
				ringImageOverlay.setImageDrawable(ringImage);
			}
		});

		FrameLayout myLayout = (FrameLayout) findViewById(R.id.myFrameLayout);
		myLayout.setOnTouchListener(new PanAndZoomListener(ringImageOverlay));	
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		
		int width = dm.heightPixels * 4 / 3;
		int height = dm.heightPixels;
		scale = (float)width / 800;
		
		Log.e("res", "w: " + width + " h: " + height);
		
		android.widget.FrameLayout.LayoutParams params = new android.widget.FrameLayout.LayoutParams(width, height);
		preview.setLayoutParams(params);
		
		ImageView ringImageOverlay = (ImageView) findViewById(R.id.ringImageOverlay);
		ringImageOverlay.setLayoutParams(params);
	}

	@Override
	protected void onResume() {
		super.onResume();
		camera = Camera.open();
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
			;
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

//		Drawable image = null;
//		Bitmap bitmap = BitmapFactory.decodeByteArray(paramArrayOfByte, 0, paramArrayOfByte.length);
//		image =  new BitmapDrawable(getResources(), bitmap);
//		startPreview(image);
		//paramCamera.startPreview();
//		Log.e("asdasd", "" + paramArrayOfByte);
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
	
//	public void startPreview(Drawable drawable) {
//		
//		MyDrawable details = new MyDrawable(drawable);
//		Intent i = new Intent(this, Preview.class);
//		i.putExtra("Image", details);
//		startActivity(i);	
//	}
	
	public void startPreview(String drawable) {
		
		Intent i = new Intent(this, Preview.class);
		i.putExtra("Image", drawable);
		startActivity(i);	
	}
	
	public static float getScale(){
		return scale;
	}

}