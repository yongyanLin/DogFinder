package com.example.dogfinder.Utils;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import com.example.dogfinder.R;

public class postPopUpUtil extends PopupWindow {
    private View view;
    public LinearLayout browse_btn,edit_btn;
    private Button cancel;
    public postPopUpUtil(Context mContext, View.OnClickListener itemsOnClick) {

        this.view = LayoutInflater.from(mContext).inflate(R.layout.popup_post_window, null);

        browse_btn = (LinearLayout) view.findViewById(R.id.browse_btn);
        edit_btn = (LinearLayout) view.findViewById(R.id.edit_btn);
        cancel = (Button) view.findViewById(R.id.cancel);

        cancel.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {

                dismiss();
            }
        });
        // add listener
        browse_btn.setOnClickListener(itemsOnClick);
        edit_btn.setOnClickListener(itemsOnClick);

        // outside can be clicked
        this.setOutsideTouchable(true);
        // if outside the window dismiss automatically
        this.view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                int height = view.findViewById(R.id.pop_post_layout).getTop();

                int y = (int) event.getY();
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (y < height) {
                        dismiss();
                    }
                }
                return true;
            }
        });



        this.setContentView(this.view);

        this.setHeight(RelativeLayout.LayoutParams.WRAP_CONTENT);
        this.setWidth(RelativeLayout.LayoutParams.MATCH_PARENT);


        this.setFocusable(true);


        ColorDrawable dw = new ColorDrawable(0xb0000000);

        this.setBackgroundDrawable(dw);


        this.setAnimationStyle(R.style.popup_style);


    }
}
