package com.cashmaker.android;

import android.hardware.input.InputManager;
import android.os.SystemClock;
import android.util.Log;
import android.view.InputDevice;
import android.view.InputEvent;
import android.view.MotionEvent;
import android.view.MotionEvent.PointerCoords;
import android.view.MotionEvent.PointerProperties;

import java.lang.reflect.Method;
import java.util.ArrayList;

/*
 * Touch Event Manager
 */
public final class TouchEventManager {
	// -----------------------------------------------------------------
	// Input Manager
	private InputManager manager = null;
	// manager reflect inject method
	private Method injectMethod = null;
	// current deviceid
	private int deviceId = 0;
	// save pointer array
	private ArrayList<TouchPoint> pointers;
	// -----------------------------------------------------------------
	/*
	 * private final float DEFAULT_SIZE = 1.0f; private final int
	 * DEFAULT_META_STATE = 0; private final float DEFAULT_PRECISION_X = 1.0f;
	 * private final float DEFAULT_PRECISION_Y = 1.0f; private final int
	 * DEFAULT_EDGE_FLAGS = 0;
	 */
	// -----------------------------------------------------------------
	// singleton
	private volatile static TouchEventManager _share = null;

	// -----------------------------------------------------------------
	// static single instances
	public static TouchEventManager shard() {
		if (_share == null) {
			synchronized (TouchEventManager.class) {
				if (_share == null) {
					_share = new TouchEventManager();

				}
			}
		}
		return _share;
	}

	// -----------------------------------------------------------------
	// private constructor
	private TouchEventManager() {
		// get input device id
		deviceId = getInputDeviceId();
		// init
		init();
		// init pointer array
		pointers = new ArrayList<TouchPoint>();
	}

	// -----------------------------------------------------------------
	// get current eviceid
	private int getInputDeviceId() {
		final int DEFAULT_DEVICE_ID = 0;
		int[] devIds = InputDevice.getDeviceIds();
		for (int devId : devIds) {
			InputDevice inputDev = InputDevice.getDevice(devId);
			try {
				Class<?> inputDevClass = Class.forName(inputDev.getClass()
						.getName());
				Method method = inputDevClass.getDeclaredMethod(
						"supportsSource", int.class);
				method.setAccessible(true);
				boolean ret = false;
				Boolean retObj = (Boolean) method.invoke(inputDev,
						InputDevice.SOURCE_TOUCHSCREEN);
				ret = retObj.booleanValue();
				if (ret)
					return devId;
			} catch (Exception e) {
				System.out.println(e.toString());
			}
		}
		return DEFAULT_DEVICE_ID;
	}

	// -----------------------------------------------------------------
	// init Manager instances
	private void init() {
		try {
			Class<?> inputManagerClass = Class
					.forName("android.hardware.input.InputManager");
			Method method = inputManagerClass.getDeclaredMethod("getInstance");
			method.setAccessible(true);
			Object object = method.invoke(null);
			manager = (InputManager) object;

			injectMethod = inputManagerClass.getDeclaredMethod(
					"injectInputEvent", InputEvent.class, int.class);
			injectMethod.setAccessible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// -----------------------------------------------------------------
	// clear all data
	public synchronized void clearEvents() {
		if (null != pointers)
			pointers.clear();
	}

	// -----------------------------------------------------------------
	// touch action down
	public void touchDown(int id, int x, int y) {
		int index = this.addPointer(id, x, y);
		this.sendTouchEvent(TouchScreenConfig.TOUCH_EVENT_DOWN, index);
	}

	// touch action move
	public void touchMove(int id, int x, int y) {
		int index = this.addPointer(id, x, y);
		this.sendTouchEvent(TouchScreenConfig.TOUCH_EVENT_MOVE, index);
	}

	// touch action up
	public void touchUp(int id, int x, int y) {
		int index = this.addPointer(id, x, y);
		this.sendTouchEvent(TouchScreenConfig.TOUCH_EVENT_UP, index);
		this.removePointer(id);
	}

	// -----------------------------------------------------------------
	// add pointer to array
	private int addPointer(int id, int x, int y) {
		int index = getIndexOfId(id);

		TouchPoint point = null;
		if (index > -1) {
			point = pointers.get(index);
			point.x = x;
			point.y = y;
		} else {
			point = new TouchPoint();
			point.id = id;
			point.x = x;
			point.y = y;
			pointers.add(point);

			index = pointers.size() - 1;
		}

		this.convertPoint(point);

		return index;
	}

	// remove pointer from array
	private void removePointer(int id) {
		int index = getIndexOfId(id);

		if (index > -1)
			pointers.remove(index);
	}

	// return index of id
	private int getIndexOfId(int id) {
		int index = -1;

		for (int i = 0; i < pointers.size(); i++) {
			TouchPoint point = pointers.get(i);
			if (point.id == id) {
				index = i;
				break;
			}
		}

		return index;
	}

	// convert point from screen orient to orient of setting
	private void convertPoint(TouchPoint point) {
		if (point == null)
			return;
		if (TouchScreenConfig.screenWidth == 0
				|| TouchScreenConfig.screenHeight == 0) {
			TouchScreenConfig.screenWidth = 1080;
			TouchScreenConfig.screenHeight = 1920;
		}
		int x = point.x, y = point.y;
		if (TouchScreenConfig.screenOrient == TouchScreenConfig.SCREEN_LANDSCAPE_LEFT) {// 左
			x = (TouchScreenConfig.screenHeight - 1) - point.y;
			y = point.x;
		}
		if (TouchScreenConfig.screenOrient == TouchScreenConfig.SCREEN_LANDSCAPE_RIGHT) {// 右
			x = point.y;
			y = (TouchScreenConfig.screenWidth - 1) - point.x;

		}
		if (TouchScreenConfig.screenOrient == TouchScreenConfig.SCREEN_UPSIDEDOWN) {// 向下
			x = (TouchScreenConfig.screenWidth - 1) - point.x;
			y = (TouchScreenConfig.screenHeight - 1) - point.y;
		}
		LogUitl.e("TouchEventManager", String.format( "T convertPoint %d point.x = %d point.y = %d x = %d y = %d w = %d h = %d",TouchScreenConfig.screenOrient ,point.x,point.y,x,y,TouchScreenConfig.screenWidth,TouchScreenConfig.screenHeight));
		point.x = x;
		point.y = y;
	}

	// -----------------------------------------------------------------
	// send touch event
	private void sendTouchEvent(int action, int index) {
		// pointer count
		int pointCount = pointers.size();
		PointerProperties[] pp = new PointerProperties[pointCount];
		PointerCoords[] pc = new PointerCoords[pointCount];
		for (int i = 0; i < pointCount; i++) {
			TouchPoint point = pointers.get(i);

			pp[i] = new PointerProperties();
			pp[i].clear();
			pp[i].id = point.id;

			pc[i] = new PointerCoords();
			pc[i].x = point.x;
			pc[i].y = point.y;
		}

		// action
		int act = 0;
		// down
		if (action == TouchScreenConfig.TOUCH_EVENT_DOWN) {
			if (pointCount > 1) {
				act = (((pointCount - 1) << 8) | MotionEvent.ACTION_POINTER_DOWN);
			} else
				act = MotionEvent.ACTION_DOWN;

		}
		// up
		else if (action == TouchScreenConfig.TOUCH_EVENT_UP) {
			if (pointCount > 1) {
				act = (((pointCount - 1) << 8) | MotionEvent.ACTION_POINTER_UP);
			} else
				act = MotionEvent.ACTION_UP;
		} else
			act = MotionEvent.ACTION_MOVE;

		long now = SystemClock.uptimeMillis();
		// motion event
		MotionEvent event = MotionEvent.obtain(now, now, act, pointCount, pp,
				pc, 0, 0, 0.0f, 0.0f, deviceId, 0, 0, 0);
		event.setSource(InputDevice.SOURCE_TOUCHSCREEN);
		// send event
		try {
			if (injectMethod != null)
				injectMethod
						.invoke(manager,
								event,
								TouchScreenConfig.INJECT_INPUT_EVENT_MODE_WAIT_FOR_FINISH);
		} catch (Exception e) {
			System.out.println(e.toString());
		}
	}
}