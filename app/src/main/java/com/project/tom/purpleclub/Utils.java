package com.project.tom.purpleclub;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * Created by Tom on 2016/1/29.
 */
public class Utils {

    public static Bitmap decodeBitmapFromSource(Resources res,int resId,int reqWidth,int reqHeight){
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res,resId,options);

        //计算样本大小
        options.inSampleSize = calculateInSampleSize(options,reqWidth,reqHeight);

        //用inSampleSize来decode bitmap
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res,resId,options);
    }

    public static int calculateInSampleSize(BitmapFactory.Options options,int reqWidth,int reqHeight){
        //图像的原长宽
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if(height > reqHeight || width > reqWidth){
            final int halfHeight = height/2;
            final int halfWidth = width/2;
            while((halfHeight/inSampleSize) > reqHeight && (halfWidth/inSampleSize) > reqWidth){
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }
}
