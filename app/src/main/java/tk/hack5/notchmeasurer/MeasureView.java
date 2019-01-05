package tk.hack5.notchmeasurer;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.Log;
import android.view.DisplayCutout;
import android.view.View;
import android.view.WindowInsets;

import java.util.ArrayList;
import java.util.List;

public class MeasureView extends View {

    public MeasureView(Context context) {
        super(context);
    }

    public MeasureView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MeasureView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public MainActivity context;

    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    public WindowInsets onApplyWindowInsets(WindowInsets insets) {
        Log.e("measureview", "GOT INSETS!!!");
        //context.callback(insets);
        DisplayCutout dcutout = insets.getDisplayCutout();
        if (dcutout == null) {
            Intent intent = new Intent(context, FullscreenActivity.class);
            intent.putExtra("redirect", false);
            context.startActivity(intent);
        }
        List<Rect> cutouts = insets.getDisplayCutout().getBoundingRects();
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        ArrayList<CharSequence> items = new ArrayList<>();
        for (Rect cutout: cutouts) {
            items.add("Cutout:\nAbsolute top:"+cutout.top+"\nAbsolute left:"+cutout.left+"\nHeight:"+cutout.height()+"\nWidth:"+cutout.width());
        }
        dialog.setItems(items.toArray(new CharSequence[0]), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        })
                .setTitle("Insets");
        dialog.show();
        Log.e("measureview","sent dialog");
        return insets;
    }
}
