package com.cashmaker.android;

/*
 * Touch screen config
 */
public final class TouchScreenConfig {

    //touch event type
    public static final int TOUCH_EVENT_DOWN      = 0;
    public static final int TOUCH_EVENT_MOVE      = 1;
    public static final int TOUCH_EVENT_UP        = 2;

    //screen orient defined
    public static final int SCREEN_PORTRAIT        = 0; //portrait
    public static final int SCREEN_LANDSCAPE_RIGHT = 1; //landscape right
    public static final int SCREEN_LANDSCAPE_LEFT  = 2; //landscape left
    public static final int SCREEN_UPSIDEDOWN      = 3; //upsidedown

    //inject input event mode
    public static final int INJECT_INPUT_EVENT_MODE_WAIT_FOR_FINISH = 2;

    //current screen orient
    public static int  screenOrient =  0;
    //current orient of setting
    public static int  orientOfSetting = 0;
    //current screen width
    public static int  screenWidth;
    //current screen height;
    public static int  screenHeight;
}