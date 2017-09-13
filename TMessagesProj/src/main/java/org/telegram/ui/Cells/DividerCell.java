/*
 * This is the source code of Telegram for Android v. 3.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2017.
 */

package org.telegram.ui.Cells;

import android.content.Context;
import android.graphics.Canvas;
import android.view.View;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.ActionBar.Theme;

public class DividerCell extends View {

    public DividerCell(Context context) {
        super(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), AndroidUtilities.dp(16) + 1);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if(Theme.usePlusTheme)setPaintColor();//plus
        canvas.drawLine(getPaddingLeft(), AndroidUtilities.dp(8), getWidth() - getPaddingRight(), AndroidUtilities.dp(8), Theme.dividerPaint);
    }
    //plus
    private void setPaintColor(){
        String key = getTag() != null ? getTag().toString() : null;
        if(key != null){
            int color = AndroidUtilities.getIntDef(key, 0xffd9d9d9);
            Theme.dividerPaint.setColor(color);
            if(key.contains("00")){
                Theme.dividerPaint.setColor(0x00000000);
            }
        }
    }
    //
}
