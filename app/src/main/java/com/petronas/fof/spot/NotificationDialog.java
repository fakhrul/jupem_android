package com.petronas.fof.spot;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.petronas.fof.spot.helpers.ImageHelper;


/**
 * Created by Asif
 */

public class NotificationDialog {
    private static String TAG = "NotificationDialog";


    Dialog mDialog;
    Context mContext;
    private ImageHelper helper;

    public void showNotificationDialog(final Context mContext, String pushContent, final String imgUrl) {
        this.mContext=mContext;
        helper=new ImageHelper(mContext);
        mDialog = new Dialog(mContext);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mDialog.setContentView(R.layout.dialog_notification);
        mDialog.setCancelable(true);
        mDialog.show();

        TextView content= mDialog.findViewById(R.id.notificationContent);
        final ImageView image= mDialog.findViewById(R.id.notificationImage);
        ImageView close= mDialog.findViewById(R.id.close);

        content.setText(pushContent);
        if (imgUrl!=null && !imgUrl.isEmpty()){

            Glide.with(mContext).load(imgUrl).
                    error(R.mipmap.ic_launcher).
                    override(300,200).centerCrop().dontAnimate().
                    placeholder(R.drawable.image_placeholder).
                    into(image);
        }
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                helper.zoomImageFromThumb(image,imgUrl, mDialog.getWindow().getDecorView().getRootView());
            }
        });
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
            }
        });

    }

}
