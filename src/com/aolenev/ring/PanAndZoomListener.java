package com.aolenev.ring;

import android.graphics.PointF;
import android.util.FloatMath;
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
	static final int		NONE		= 0;
	static final int		DRAG		= 1;
	static final int		ZOOM		= 2;
	int				mode		= NONE;
	// Remember some things for zooming
	PointF				start		= new PointF();
	PointF				mid		= new PointF();
	static float			rotation	= 0;
	float				oldDist		= 1f;
	ImageView			ringImageOverlay;
	public static float		finalScale	= 1.0f;
	
	

	public PanAndZoomListener(ImageView ringImageOverlay) {
		this.ringImageOverlay = ringImageOverlay;
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
					rotation += (event.getX() - start.x) / 200.0f;
					ringImageOverlay.setRotation(rotation);
				} else if (mode == ZOOM) {
					float newDist = spacing(event);
					if (newDist > 10f) {
						final float scale = newDist / oldDist;
						final float oldScale = ringImageOverlay.getScaleX() - 1;
						finalScale = scale + oldScale;
						oldDist = newDist;
						Log.e("scale", "" + scale);
						ringImageOverlay.post(new Runnable() {

							@Override
							public void run() {
								ringImageOverlay.setScaleX(scale + oldScale);
								ringImageOverlay.setScaleY(scale + oldScale);

							}
						});
					}
				}
				break;
		}
		return true; // indicate event was handled
	}
 
	private float spacing(MotionEvent event) {

		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
		return FloatMath.sqrt(x * x + y * y);
	}

	private void midPoint(PointF point, MotionEvent event) {
		float x = event.getX(0) + event.getX(1);
		float y = event.getY(0) + event.getY(1);
		point.set(x / 2, y / 2);
	}

}