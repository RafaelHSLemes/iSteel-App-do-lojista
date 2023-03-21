package com.general.files;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.widget.ImageView;

import com.luis.store.R;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.utils.CommonUtilities;
import com.utils.Logger;
import com.utils.Utils;
import com.view.SelectableRoundedImageView;

import org.json.JSONObject;

public class AppFunctions {
    Context mContext;
    GeneralFunctions generalFunc;

    public AppFunctions(Context mContext) {
        this.mContext = mContext;
        generalFunc = MyApp.getInstance().getGeneralFun(mContext);
    }

    public void checkProfileImage(SelectableRoundedImageView userProfileImgView, JSONObject userProfileJson, String imageKey) {
        String vImgName_str = generalFunc.getJsonValueStr(imageKey, userProfileJson);

        Picasso.get().load(CommonUtilities.COMPANY_PHOTO_PATH + generalFunc.getMemberId() + "/" + vImgName_str).placeholder(R.mipmap.ic_no_pic_user).error(R.mipmap.ic_no_pic_user).into(userProfileImgView);
    }

    public void checkProfileImage(SelectableRoundedImageView userProfileImgView, JSONObject userProfileJson, String imageKey, ImageView profilebackimage) {
        String vImgName_str = generalFunc.getJsonValueStr(imageKey, userProfileJson);

        Picasso.get().load(CommonUtilities.COMPANY_PHOTO_PATH + generalFunc.getMemberId() + "/" + vImgName_str).placeholder(R.mipmap.ic_no_pic_user).error(R.mipmap.ic_no_pic_user).into(userProfileImgView);

        Picasso.get().load(CommonUtilities.COMPANY_PHOTO_PATH + generalFunc.getMemberId() + "/" + vImgName_str).placeholder(R.mipmap.ic_no_pic_user).error(R.mipmap.ic_no_pic_user).into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                Utils.setBlurImage(bitmap, profilebackimage);
            }

            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable){

            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        });
    }

    public boolean checkSinchInstance(SinchService.SinchServiceInterface sinchServiceInterface) {
        boolean isNull=sinchServiceInterface!=null && sinchServiceInterface.getSinchClient()!=null;
        Logger.d("call","Instance"+isNull);
        return isNull;
    }

    public static Spanned fromHtml(String html){
        if(!Utils.checkText(html)){
            // return an empty spannable if the html is null
            return new SpannableString("");
        }else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // FROM_HTML_MODE_LEGACY is the behaviour that was used for versions below android N
            // we are using this flag to give a consistent behaviour
            return Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY);
        } else {
            return Html.fromHtml(html);
        }
    }


}
