package com.aolenev.ring;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;

public class PanAndZoomListener implements OnTouchListener {

	public static class Anchor {

		public static final int	CENTER	= 0;
		public static final int	TOPLEFT	= 1;
	}

	// We can be in one of these 3 states
	static final int	NONE		= 0;
	static final int	DRAG		= 1;
	static final int	ZOOM		= 2;
	int			mode		= NONE;
	// Remember some things for zooming
	PointF			start		= new PointF();
	PointF			mid		= new PointF();
	static float		rotation	= 1;
	float			oldDist		= 1f;
	ImageView		ringImageOverlay;
	public static float	finalScale	= 1.0f;
	Matrix			matrix		= new Matrix();
	float			oldScale	= 1.0f;
	Bitmap			ringBitmap;

	public PanAndZoomListener(ImageView ringImageOverlay, Bitmap ringBitmap) {
		this.ringImageOverlay = ringImageOverlay;
		this.ringBitmap = ringBitmap;
	}

	public boolean onTouch(View view, MotionEvent event) {
		// Handle touch events here...
		switch (event.getAction() & MotionEvent.ACTION_MASK) {
			case MotionEvent.ACTION_DOWN:
				start.set(event.getX(), event.getY());
				// Log.e(TAG, "mode=DRAG");
				mode = DRAG;
				break;
			case MotionEvent.ACTION_POINTER_DOWN:
				oldDist = spacing(event);
				if (oldDist > 10f) {
					midPoint(mid, event);
					mode = ZOOM;
				}
				break;
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_POINTER_UP:
				mode = NONE;
				break;
			case MotionEvent.ACTION_MOVE:
				if (mode == DRAG) {
					float tempRotation = (event.getX() - start.x) / 200.0f;
					rotation += tempRotation;
					Log.e("rotation", "" + rotation);
					//matrix.postScale(scale, scale);
					//Log.e("rotation", "rot: " + rotation);
					//matrix.postRotate(tempRotation, ringImageOverlay.getWidth() / 2, ringImageOverlay.getHeight() / 2);
					//Bitmap scaledBitmap = Bitmap.createBitmap(ringBitmap, 0, 0, ringBitmap.getWidth(), ringBitmap.getHeight(), matrix, true);
					//ringImageOverlay.setImageBitmap(scaledBitmap);
					//ringImageOverlay.setImageMatrix(matrix);
					ringImageOverlay.setRotation(rotation);
				} else if (mode == ZOOM) {
					float newDist = spacing(event);
					if (newDist > 10f) {
						final float scale = (newDist / oldDist) - 1;
						finalScale = scale + oldScale;
						oldDist = newDist;
						oldScale = finalScale;
						//Log.e("scale", "" + finalScale);
						//matrix.setScale(finalScale, finalScale);
						//matrix.postRotate(rotation, ringImageOverlay.getWidth() / 2, ringImageOverlay.getHeight() / 2);
						//Bitmap scaledBitmap = Bitmap.createBitmap(ringBitmap, 0, 0, ringBitmap.getWidth(), ringBitmap.getHeight(), matrix, true);
						//ringImageOverlay.setImageBitmap(scaledBitmap);
						//ringImageOverlay.setImageMatrix(matrix);
						ringImageOverlay.setScaleX(finalScale);
						ringImageOverlay.setScaleY(finalScale);
					}
				}
				break;
		}
		return true;
	}

	private float spacing(MotionEvent event) {

		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
		return (float) Math.sqrt(x * x + y * y);
	}

	private void midPoint(PointF point, MotionEvent event) {
		float x = event.getX(0) + event.getX(1);
		float y = event.getY(0) + event.getY(1);
		point.set(x / 2, y / 2);
	}
	
	public void clearRingOverlay(){
		this.ringImageOverlay.setImageDrawable(null);
	}

}