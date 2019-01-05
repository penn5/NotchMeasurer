package tk.hack5.notchmeasurer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

public class FullscreenActivity extends Activity {

    private int x = -1;
    private int y = -1;
    private int side = -1;
    private int direction = -1; //0 is right, 1 is left, 2 is down, 3 is up

    private int left;
    private int right;
    private int bottom;

    private Runnable nextStep = new Runnable() {

        @Override
        public void run() {
            displayResults();
        }
    };

    // Lub kanging, thx https://stackoverflow.com/a/6758962/5509575
    private boolean isPackagePresent(@SuppressWarnings("SameParameterValue") String targetPackage){
        PackageManager pm=getPackageManager();
        try {
            pm.getPackageInfo(targetPackage,PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= 28 && ! isPackagePresent("me.phh.treble.app") && getIntent().getBooleanExtra("redirect", true)) { //If we're on phh-treble, we don't want to use the system api because it wont know.
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
        setContentView(R.layout.activity_fullscreen);
        findViewById(R.id.root).setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    private void setDirection(boolean sideways) {
        final RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(findViewById(R.id.right_visible).getLayoutParams());
        if (sideways) {
            params.addRule(RelativeLayout.RIGHT_OF, R.id.left_visible);
        } else {
            params.addRule(RelativeLayout.BELOW, R.id.left_visible);
        }
        findViewById(R.id.right_visible).setLayoutParams(params);
    }

    private void setColors(boolean swapped) {
        if (swapped) {
            findViewById(R.id.left_visible).setBackgroundColor(0xFFFF0000);
            findViewById(R.id.right_visible).setBackgroundColor(0xFF00FF00);
        } else {
            findViewById(R.id.left_visible).setBackgroundColor(0xFF00FF00);
            findViewById(R.id.right_visible).setBackgroundColor(0xFFFF0000);
        }
    }

    private void setYOffset(int yOffset) { //Always remember the red dot
        if (direction == 2 || direction == 3) {
            yOffset -= 1; //The red dot and green dot should be part of this padding
        }
        final RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(findViewById(R.id.top_invisible).getLayoutParams());
        params.height = yOffset;
        findViewById(R.id.top_invisible).setLayoutParams(params);
    }

    private void setXOffset(int xOffset) { //Always remember the red dot
        if (direction == 0 || direction == 1) {
            xOffset -= 1; //The red dot and green dot should be part of this padding
        }
        final RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(findViewById(R.id.left_invisible).getLayoutParams());
        params.width = xOffset;
        findViewById(R.id.left_invisible).setLayoutParams(params);
    }

    private void startMeasurement() {
        findViewById(R.id.measure_button).setVisibility(View.VISIBLE);
        findViewById(R.id.done_button).setVisibility(View.VISIBLE);
        findViewById(R.id.start_buttons).setVisibility(View.INVISIBLE);
    }

    private void measureNotch() {
        setDirection(true); //We should always measure this sideways
        setColors(false);

        DisplayMetrics dm = getResources().getDisplayMetrics();
        final int width = dm.widthPixels;

        y = 0;
        x = 0;

        direction = 0;

        nextStep = new Runnable() {
            @Override
            public void run() {
                left = x;
                setColors(true);
                x = width - 1;
                y = 0;
                direction = 1;
                setDirection(true);
                setColors(true);
                nextStep = new Runnable() {
                    @Override
                    public void run() {
                        right = x;
                        x = ((right - left) / 2) + left;
                        y = 200; //Max notch depth supported
                        setDirection(false); //Red is lower by default, that's wrong
                        setColors(true);
                        direction = 3;
                        nextStep = new Runnable() {
                            @Override
                            public void run() {
                                bottom = y;
                                displayResultsNotch();
                            }
                        };
                        startMeasurement();
                    }
                };
                startMeasurement();
            }
        };
        startMeasurement();
    }

    public void measureNotch(View v) {
        measureNotch();
    }

    private void stopMeasurement() {
        findViewById(R.id.measure_button).setVisibility(View.INVISIBLE);
        findViewById(R.id.done_button).setVisibility(View.INVISIBLE);
        findViewById(R.id.start_buttons).setVisibility(View.VISIBLE);
    }

    public void measureRoundedCorners(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        CharSequence[] sides = {"Top Left", "Top Right", "Bottom Left", "Bottom Right"};
        builder.setTitle("Which side?")
                .setItems(sides, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        measureRoundedCorner(which, true);
                    }
                });
        builder.show();
    }

    private void measureRoundedCorner(int corner, @SuppressWarnings("SameParameterValue") boolean sameBothWays) { //0=top left,1=top right,2=bottom left,3=bottom right; samebothways means if the corner has the same y as x
        setDirection(true); //We should always measure this sideways

        DisplayMetrics dm = getResources().getDisplayMetrics();
        final int width = dm.widthPixels;
        final int height = dm.heightPixels;

        side = corner;

        if (side == 0 || side == 2) {
            Log.e("notchmeasurer", "setcolors true");
            setColors(true);
            x=0;
            setXOffset(0);
            direction = 0;
        } else {
            Log.e("notchmeasurer", "setcolors false");
            setColors(false);
            x=width - 1;
            setXOffset(x);
            direction = 1;
        }

        if (side == 0 || side == 1) {
            y=0;
            setYOffset(0);
        } else {
            //noinspection SuspiciousNameCombination
            y=width;
            setYOffset(height);
        }

        nextStep = new Runnable() {
            @Override
            public void run() {
                displayResults();
            }
        };

        if (! sameBothWays) {
            setDirection(false);
            nextStep = new Runnable() {
                @Override
                public void run() {
                    displayResults();
                    setDirection(false); //Red is lower by default
                    if (side == 0 || side == 1) {
                        setColors(true);
                        setYOffset(0);
                    } else {
                        setColors(false);
                        setYOffset(height);
                    }

                    if (side == 0 || side == 2) {
                        setXOffset(0);
                    } else {
                        setXOffset(width);
                    }
                    nextStep = new Runnable() {
                        @Override
                        public void run() {
                            displayResults();
                        }
                    };
                    startMeasurement();
                }
            };
        }
        startMeasurement();
    }
    public void roundedCornersClick(View view) {
        switch (direction) {
            case 0: //right
                x += 1;
                break;
            case 1://left
                x -= 1;
                break;
            case 2://down
                y += 1;
                break;
            case 3://up
                y -= 1;
                break;
            default:
                break;
        }
        Log.e("notchmeasurer", Integer.toString(x));
        Log.e("notchmeasurer", Integer.toString(y));
        setXOffset(x);
        setYOffset(y);
    }

    public void doneClick(View view) {
        stopMeasurement();
        nextStep.run();
    }

    private void displayResultsNotch() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Absolute left: "+Integer.toString(left) + "\nWidth: "+Integer.toString(right - left) + "\nHeight: "+Integer.toString(bottom));
        builder.show();
    }


    private void displayResults() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Rounded Corners:"+Integer.toString(x));
        builder.show();
    }
}
