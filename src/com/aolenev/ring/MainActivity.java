package com.aolenev.ring;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.hardware.Camera.Size;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.SurfaceHolder;
import android.view.Window;
import android.view.WindowManager;
import android.view.SurfaceView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.FrameLayout;

public class MainActivity extends Activity implements SurfaceHolder.Callback, View.OnClickListener, Camera.PictureCallback, Camera.PreviewCallback, Camera.AutoFocusCallback {
	private Camera		camera;
	private SurfaceHolder	surfaceHolder;
	private SurfaceView	preview;
	private Button		shotBtn;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Portret orientation
		// setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

		// Full Screen
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.activity_main);

		preview = (SurfaceView) findViewById(R.id.SurfaceView01);

		surfaceHolder = preview.getHolder();
		surfaceHolder.addCallback(this);
		surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

		shotBtn = (Button) findViewById(R.id.Button01);
		shotBtn.setText("Shot");
		shotBtn.setOnClickListener(this);
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
			File saveDir = new File("/sdcard/Ring/");

			if (!saveDir.exists()) {
				saveDir.mkdirs();
			}

			FileOutputStream os = new FileOutputStream(String.format("/sdcard/Ring/%d.jpg", System.currentTimeMillis()));
			os.write(paramArrayOfByte);
			os.close();
		} catch (Exception e) {
		}

		paramCamera.startPreview();
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
}