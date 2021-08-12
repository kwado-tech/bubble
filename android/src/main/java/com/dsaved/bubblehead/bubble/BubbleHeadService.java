package com.dsaved.bubblehead.bubble;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.util.Base64;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

public class BubbleHeadService extends Service implements View.OnClickListener {
    private WindowManager mWindowManager;
    private View mFloatingWidgetView;
    private ImageView remove_image_view;
    private final Point szWindow = new Point();
    private View removeFloatingWidgetView;
    private static boolean showCloseButton = false, _bounce = true, _dragToClose = true;
    private boolean _continueToSnap = false;

    private int x_init_cord, y_init_cord, x_init_margin, y_init_margin;
    static Bitmap _image;

    // Set the value for showing close button to true or false
    public static void shouldShowCloseButton(Boolean show) {
        showCloseButton = show;
    }

    // set to true to enable bouncing
    public static void bounce(Boolean bounce) {
        _bounce = bounce;
    }

    // Set the value for drag to close to enable dragging bubble to close
    public static void dragToClose(Boolean dragToClose) {
        _dragToClose = dragToClose;
    }

    public static void startService(Context activity, String image) {
        byte[] decodedBytes = Base64.decode(image, Base64.DEFAULT);
        _image = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
        Intent i = new Intent();
        i.setAction(Intent.ACTION_MAIN);
        i.addCategory(Intent.CATEGORY_HOME);
        activity.startActivity(i);

        Intent intent = new Intent(activity, BubbleHeadService.class);
        activity.startService(intent);
    }

    public static void stopService(Context activity) {
        Intent intent = new Intent(activity, BubbleHeadService.class);
        activity.stopService(intent);
    }

    // create an empty constructor
    public BubbleHeadService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @SuppressLint({"ClickableViewAccessibility", "InflateParams"})
    @Override
    public void onCreate() {
        super.onCreate();
        // init WindowManager
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        getWindowManagerDefaultDisplay();

        // Init LayoutInflater
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

        // Inflate the removing view layout we created
        removeFloatingWidgetView = inflater.inflate(R.layout.bubble_head_remove_widget, null);

        // Add the view to the window.
        WindowManager.LayoutParams paramRemove;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            paramRemove = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);
        } else {
            paramRemove = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);
        }

        // Specify the view position
        paramRemove.gravity = Gravity.TOP | Gravity.LEFT;

        // Initially the Removing widget view is not visible, so set visibility to GONE
        removeFloatingWidgetView.setVisibility(View.GONE);
        remove_image_view = (ImageView) removeFloatingWidgetView.findViewById(R.id.remove_img);

        // Add the view to the window
        mWindowManager.addView(removeFloatingWidgetView, paramRemove);

        // Inflate the floating view layout we created
        mFloatingWidgetView = inflater.inflate(R.layout.layout_bubble_head, null);

        // Add the view to the window.
        WindowManager.LayoutParams params;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);
        } else {
            params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);
        }

        // Specify the view position
        params.gravity = Gravity.TOP | Gravity.LEFT;

        // Initially view will be added to top-left corner, you change x-y coordinates according to your need
        params.x = 0;
        params.y = 100;

        // Add the view to the window
        mWindowManager.addView(mFloatingWidgetView, params);

        //set image to chatHead
        ImageView chatHeadImage = mFloatingWidgetView.findViewById(R.id.chat_head_profile_iv);
        chatHeadImage.setImageBitmap(_image);

        // find id of close image button
        ImageView closeBubbleHead = mFloatingWidgetView.findViewById(R.id.close_bubble_head);
        closeBubbleHead.setOnClickListener(this);
        if (!showCloseButton) {
            closeBubbleHead.setVisibility(View.GONE);
        }

        implementTouchListenerToFloatingWidgetView();
    }

    private void getWindowManagerDefaultDisplay() {
        mWindowManager.getDefaultDisplay().getSize(szWindow);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void implementTouchListenerToFloatingWidgetView() {
        _continueToSnap = true;
        // Drag and move chat head using user's touch action.
        mFloatingWidgetView.findViewById(R.id.root_container);
        mFloatingWidgetView.setOnTouchListener(new View.OnTouchListener() {
            long time_start = 0, time_end = 0;

            boolean isLongClick = false;
            boolean inBounded = false;
            int remove_img_width = 0, remove_img_height = 0;

            final Handler handler_longClick = new Handler();
            final Runnable runnable_longClick = new Runnable() {
                @Override
                public void run() {
                    isLongClick = true;
                    removeFloatingWidgetView.setVisibility(View.VISIBLE);
                    onFloatingWidgetLongClick();
                }
            };

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) mFloatingWidgetView.getLayoutParams();

                int x_cord = (int) event.getRawX();
                int y_cord = (int) event.getRawY();

                int x_cord_Destination, y_cord_Destination;

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        time_start = System.currentTimeMillis();

                        if (_dragToClose) {
                            handler_longClick.postDelayed(runnable_longClick, 100);
                        }
                        remove_img_width = remove_image_view.getLayoutParams().width;
                        remove_img_height = remove_image_view.getLayoutParams().height;

                        x_init_cord = x_cord;
                        y_init_cord = y_cord;

                        // remember the initial position.
                        x_init_margin = layoutParams.x;
                        y_init_margin = layoutParams.y;
                        return true;
                    case MotionEvent.ACTION_UP:
                        isLongClick = false;
                        removeFloatingWidgetView.setVisibility(View.GONE);
                        remove_image_view.getLayoutParams().height = remove_img_height;
                        remove_image_view.getLayoutParams().width = remove_img_width;

                        if (_dragToClose) {
                            handler_longClick.removeCallbacks(runnable_longClick);
                        }

                        // If user drag and drop the floating widget view
                        // into remove view then stop the service
                        if (inBounded) {
                            stopSelf();
                            inBounded = false;
                            break;
                        }

                        // Difference between initial coordinate and current coordinate
                        int x_diff = x_cord - x_init_cord;
                        int y_diff = y_cord - y_init_cord;

                        // check if action move is little as move happen on view with just a tap
                        if (Math.abs(x_diff) < 5 && Math.abs(y_diff) < 5) {
                            time_end = System.currentTimeMillis();
                            // only perform click if time is less than 200ms
                            if ((time_end - time_start) < 200) {
                                onFloatingWidgetClick();
                            }
                        }

                        y_cord_Destination = y_init_margin + y_diff;

                        int barHeight = getStatusBarHeight();
                        if (y_cord_Destination < 0) {
                            y_cord_Destination = 0;
                        } else if (y_cord_Destination + (mFloatingWidgetView.getHeight() + barHeight) > szWindow.y) {
                            y_cord_Destination = szWindow.y - (mFloatingWidgetView.getHeight() + barHeight);
                        }

                        layoutParams.y = y_cord_Destination;

                        inBounded = false;

                        // reset position
                        resetPosition(x_cord);
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        int x_diff_move = x_cord - x_init_cord;
                        int y_diff_move = y_cord - y_init_cord;

                        x_cord_Destination = x_init_margin + x_diff_move;
                        y_cord_Destination = y_init_margin + y_diff_move;

                        // If user long click the floating view, update remove view
                        if (isLongClick) {
                            int x_bound_left = szWindow.x / 2 - (int) (remove_img_width * 1.5);
                            int x_bound_right = szWindow.x / 2 + (int) (remove_img_width * 1.5);
                            int y_bound_top = szWindow.y - (int) (remove_img_height * 1.5);

                            // If Floating view comes under Remove View update Window Manager
                            if ((x_cord >= x_bound_left && x_cord <= x_bound_right) && y_cord >= y_bound_top) {
                                inBounded = true;

                                int x_cord_remove = (int) ((szWindow.x - (remove_img_height * 1.5)) / 2);
                                int y_cord_remove = (int) (szWindow.y - ((remove_img_width * 1.5) + getStatusBarHeight()));

                                if (remove_image_view.getLayoutParams().height == remove_img_height) {
                                    WindowManager.LayoutParams param_remove = (WindowManager.LayoutParams) removeFloatingWidgetView.getLayoutParams();
                                    param_remove.x = x_cord_remove;
                                    param_remove.y = y_cord_remove;

                                    mWindowManager.updateViewLayout(removeFloatingWidgetView, param_remove);
                                }

                                layoutParams.x = x_cord_remove + (Math.abs(removeFloatingWidgetView.getWidth() - mFloatingWidgetView.getWidth())) / 2;
                                layoutParams.y = y_cord_remove + (Math.abs(removeFloatingWidgetView.getHeight() - mFloatingWidgetView.getHeight())) / 2;

                                // Update the layout with new X & Y coordinate
                                mWindowManager.updateViewLayout(mFloatingWidgetView, layoutParams);
                                break;
                            } else {
                                // If Floating window gets out of the Remove view update Remove view again
                                inBounded = false;
                                remove_image_view.getLayoutParams().height = remove_img_height;
                                remove_image_view.getLayoutParams().width = remove_img_width;
//                                onFloatingWidgetClick();
                            }

                        }

                        layoutParams.x = x_cord_Destination;
                        layoutParams.y = y_cord_Destination;

                        // Update the layout with new X & Y coordinate
                        mWindowManager.updateViewLayout(mFloatingWidgetView, layoutParams);
                        return true;
                }
                return false;
            }
        });
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.close_bubble_head) {
            stopSelf();
        }
    }

    private void onFloatingWidgetLongClick() {
        // Get remove Floating view params
        WindowManager.LayoutParams removeParams = (WindowManager.LayoutParams) removeFloatingWidgetView.getLayoutParams();

        // get x and y coordinates of remove view
        int x_cord = (szWindow.x - removeFloatingWidgetView.getWidth()) / 2;
        int y_cord = szWindow.y - (removeFloatingWidgetView.getHeight() + getStatusBarHeight());

        removeParams.x = x_cord;
        removeParams.y = y_cord;

        // Update Remove view params
        mWindowManager.updateViewLayout(removeFloatingWidgetView, removeParams);
    }

    //  Reset position of Floating Widget view on dragging
    private void resetPosition(int x_cord_now) {
        if (_continueToSnap) {
            if (x_cord_now <= szWindow.x / 2) {
                snapToLeft(x_cord_now);
            } else {
                snapToRight(x_cord_now);
            }
        }
    }

    private void snapToLeft(final int current_x_cord) {
        final int x = szWindow.x - current_x_cord;
        new CountDownTimer(500, 5) {
            final WindowManager.LayoutParams mParams = (WindowManager.LayoutParams) mFloatingWidgetView.getLayoutParams();

            public void onTick(long t) {
                long step = (500 - t) / 5;
                mParams.x = -(int) (current_x_cord * current_x_cord * step);
                if (_bounce) {
                    mParams.x = -(int) (double) bounceValue(step, x);
                }
                mWindowManager.updateViewLayout(mFloatingWidgetView, mParams);
            }

            public void onFinish() {
                mParams.x = 0;
                mWindowManager.updateViewLayout(mFloatingWidgetView, mParams);
            }
        }.start();
    }

    private void snapToRight(final int current_x_cord) {
        new CountDownTimer(500, 5) {
            final WindowManager.LayoutParams mParams = (WindowManager.LayoutParams) mFloatingWidgetView.getLayoutParams();

            public void onTick(long t) {
                long step = (500 - t) / 5;
                mParams.x = (int) (szWindow.x + (current_x_cord * current_x_cord * step) - mFloatingWidgetView.getWidth());
                if (_bounce) {
                    mParams.x = szWindow.x + (int) (double) bounceValue(step, current_x_cord) - mFloatingWidgetView.getWidth();
                }
                mWindowManager.updateViewLayout(mFloatingWidgetView, mParams);
            }

            public void onFinish() {
                mParams.x = szWindow.x - mFloatingWidgetView.getWidth();
                mWindowManager.updateViewLayout(mFloatingWidgetView, mParams);
            }
        }.start();
    }

    private double bounceValue(long step, long scale) {
        return scale * Math.exp(-0.15 * step) * Math.cos(0.08 * step);
    }

    private int getStatusBarHeight() {
        return (int) Math.ceil(25 * getApplicationContext().getResources().getDisplayMetrics().density);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        getWindowManagerDefaultDisplay();
        WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) mFloatingWidgetView.getLayoutParams();

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (layoutParams.y + (mFloatingWidgetView.getHeight() + getStatusBarHeight()) > szWindow.y) {
                layoutParams.y = szWindow.y - (mFloatingWidgetView.getHeight() + getStatusBarHeight());
                mWindowManager.updateViewLayout(mFloatingWidgetView, layoutParams);
            }

            if (layoutParams.x != 0 && layoutParams.x < szWindow.x) {
                resetPosition(szWindow.x);
            }
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            if (layoutParams.x > szWindow.x) {
                resetPosition(szWindow.x);
            }
        }
    }

    private void onFloatingWidgetClick() {
        _continueToSnap = false;
        // bring the application to front
        Intent it = new Intent("intent.bring.app.to.foreground");
        it.setComponent(new ComponentName(getPackageName(), getApplicationContext().getPackageName() + ".MainActivity"));
        it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getApplicationContext().startActivity(it);

        // stop the service
        stopSelf();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mFloatingWidgetView != null) {
            mWindowManager.removeView(mFloatingWidgetView);
        }
        if (removeFloatingWidgetView != null) {
            mWindowManager.removeView(removeFloatingWidgetView);
        }
    }
}
