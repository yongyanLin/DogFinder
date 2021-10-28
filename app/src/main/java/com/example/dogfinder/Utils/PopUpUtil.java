package com.example.dogfinder.Utils;


import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import com.example.dogfinder.R;

public class PopUpUtil extends PopupWindow {
    private Context context;
    private View view;
    private LinearLayout camera,gallery;
    private Button cancel;
    public PopUpUtil(Context mContext, View.OnClickListener itemsOnClick) {

        this.view = LayoutInflater.from(mContext).inflate(R.layout.popup_window, null);

        camera = (LinearLayout) view.findViewById(R.id.camera_btn);
        gallery = (LinearLayout) view.findViewById(R.id.gallery_btn);
        cancel = (Button) view.findViewById(R.id.cancel);

        cancel.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {

                dismiss();
            }
        });
        // add listener
        camera.setOnClickListener(itemsOnClick);
        gallery.setOnClickListener(itemsOnClick);

        // outside can be clicked
        this.setOutsideTouchable(true);
        // if outside the window dismiss automatically
        this.view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                int height = view.findViewById(R.id.pop_layout).getTop();

                int y = (int) event.getY();
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (y < height) {
                        dismiss();
                    }
                }
                return true;
            }
        });


        //set the popup window
        this.setContentView(this.view);
        // 设置弹出窗体的宽和高
        this.setHeight(RelativeLayout.LayoutParams.WRAP_CONTENT);
        this.setWidth(RelativeLayout.LayoutParams.MATCH_PARENT);

        // 设置弹出窗体可点击
        this.setFocusable(true);

        // 实例化一个ColorDrawable颜色为半透明
        ColorDrawable dw = new ColorDrawable(0xb0000000);
        // 设置弹出窗体的背景
        this.setBackgroundDrawable(dw);

        // 设置弹出窗体显示时的动画，从底部向上弹出
        this.setAnimationStyle(R.style.popup_style);


    }

}
