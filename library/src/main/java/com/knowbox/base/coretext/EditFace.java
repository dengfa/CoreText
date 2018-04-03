/*
 * Copyright (C) 2017 The AndroidCoreText Project
 */

package com.knowbox.base.coretext;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.TextUtils;
import android.util.Log;

import com.hyena.coretext.TextEnv;
import com.hyena.coretext.blocks.CYEditFace;
import com.hyena.coretext.blocks.ICYEditable;
import com.hyena.coretext.utils.Const;
import com.hyena.coretext.utils.PaintManager;

import java.util.ArrayList;
import java.util.List;

import static com.hyena.coretext.blocks.CYEditBlock.DEFAULT_FLASH_X;

/**
 * Created by yangzc on 17/2/14.
 */
public class EditFace extends CYEditFace {

    private String mClass = BlankBlock.CLASS_CHOICE;
    private int mRoundCorner = Const.DP_1 * 5;
    private ICYEditable editable;
    private List<TextInfo> mTextList = new ArrayList<TextInfo>();

    public EditFace(TextEnv textEnv, ICYEditable editable) {
        super(textEnv, editable);
        this.editable = editable;
    }

    public int getRowsVerticalSpacing() {
        return mVerticalSpacing;
    }

    public void setRowsVerticalSpacing(int spacing) {
        mVerticalSpacing = spacing;
    }

    public void setClass(String clazz) {
        this.mClass = clazz;
    }

    private RectF mRectF = new RectF();

    @Override
    protected void drawBorder(Canvas canvas, Rect blockRect, Rect contentRect) {
        if (!mTextEnv.isEditable() || BlankBlock.CLASS_DELIVERY.equals(mClass))
            return;

        if (editable.hasFocus()) {
            mRectF.set(blockRect);
            mBorderPaint.setStrokeWidth(Const.DP_1);
            mBorderPaint.setColor(0xff44cdfc);
            mBorderPaint.setStyle(Paint.Style.STROKE);
            canvas.drawRoundRect(mRectF, mRoundCorner, mRoundCorner, mBorderPaint);
        }
    }

    @Override
    protected void drawBackGround(Canvas canvas, Rect blockRect, Rect contentRect) {
        if (!mTextEnv.isEditable() || BlankBlock.CLASS_DELIVERY.equals(mClass))
            return;

        mBackGroundPaint.setStyle(Paint.Style.FILL);
        mRectF.set(blockRect);
        if (editable.hasFocus()) {
            mBackGroundPaint.setColor(Color.WHITE);
        } else {
            mBackGroundPaint.setColor(0xffe1e9f2);
        }
        canvas.drawRoundRect(mRectF, mRoundCorner, mRoundCorner, mBackGroundPaint);
    }

    @Override
    protected void drawFlash(Canvas canvas, Rect blockRect, Rect contentRect, float flashX, float flashY, int position) {
        if (!mTextEnv.isEditable())
            return;
        mFlashPaint.setColor(0xff3eabff);
        mFlashPaint.setStrokeWidth(Const.DP_1);
        if (BlankBlock.CLASS_DELIVERY.equals(mClass)) {
            if (editable.isEditable() && editable.hasFocus() && mInputFlash) {
                String text = getText();
                float left = 0;
                int textHeight = PaintManager.getInstance().getHeight(mTextPaint);
                float top = 0;
                float textWidth = 0;
                float flashLeft = 0;
                float flashRight = 0;
                float textX = mTextX;
                if (!TextUtils.isEmpty(text)) {
                    textX = 0;
                    flashLeft = textX + PaintManager.getInstance().getWidth(mTextPaint, text.substring(0, 1)) / 2;
                    //ontouch时，position等于-1，否则大于等于0，当position大于等于0时候，按照position来判断，否则按照x，y值来判断
                    if (position >= 0) {
                        mFlashPosition = position;
                        if (position == 0) {
                            left = contentRect.left;
                            top = contentRect.top + textHeight - this.mTextPaintMetrics.bottom;
                        } else {
                            String lineText = "";
                            for (int i = 0; i < mTextList.size(); i++) {
                                TextInfo info = mTextList.get(i);
                                if (position > info.mStartPos && position <= info.mEndPos) {
                                    lineText = info.mText;
                                    top = info.mY;
                                    left = contentRect.left +
                                            PaintManager.getInstance().getWidth(mTextPaint, lineText.substring(0, position - info.mStartPos));
                                    break;
                                }
                            }
                        }
                    } else {
                        int prePosition = 0;
                        String lineText = "";
                        TextInfo lastLineInfo = mTextList.get(mTextList.size() - 1);
                        if (contentRect.top + flashY > lastLineInfo.mY) {
                            prePosition = lastLineInfo.mStartPos;
                            lineText = lastLineInfo.mText;
                            top = lastLineInfo.mY;
                        } else {
                            for (int i = 0; i < mTextList.size(); i++) {
                                TextInfo info = mTextList.get(i);
                                if (i == 0) {
                                    if (contentRect.top + flashY <= info.mY) {
                                        lineText = info.mText;
                                        prePosition = info.mStartPos;
                                        top = info.mY;
                                        break;
                                    }
                                } else if (contentRect.top + flashY > mTextList.get(i - 1).mY &&
                                        contentRect.top + flashY <= info.mY) {
                                    lineText = info.mText;
                                    prePosition = info.mStartPos;
                                    top = info.mY;
                                    break;
                                }
                            }
                        }
                        textWidth = PaintManager.getInstance().getWidth(mTextPaint, lineText);
                        if (textWidth > contentRect.width()) {
                            flashRight = textX + contentRect.width();
                        } else {
                            flashRight = textX + textWidth;
                        }

                        if (flashX <= DEFAULT_FLASH_X) {
                            if (!TextUtils.isEmpty(lineText)) {
                                if (textWidth > contentRect.width()) {
                                    left = contentRect.right;
                                } else {
                                    left = contentRect.left + textWidth;
                                }
                                mFlashPosition = lineText.length();
                            } else {
                                left = contentRect.left + contentRect.width() / 2;
                                mFlashPosition = 0;
                            }
                        } else if (!TextUtils.isEmpty(lineText) && flashX < flashLeft) {
                            mFlashPosition = 0;
                            left = contentRect.left + textX;
                        } else if ((!TextUtils.isEmpty(lineText) && flashX >= flashRight)) {
                            mFlashPosition = lineText.length();
                            left = contentRect.left + textWidth;
                        } else {
                            if (!TextUtils.isEmpty(lineText)) {
                                for (int i = 1; i < lineText.length(); i++) {
                                    if (flashX >= textX +
                                            PaintManager.getInstance().getWidth(mTextPaint, lineText.substring(0, i)) -
                                            PaintManager.getInstance().getWidth(mTextPaint, lineText.substring(i - 1, i)) / 2 &&
                                            flashX < textX
                                                    + PaintManager.getInstance().getWidth(mTextPaint, lineText.substring(0, i + 1)) -
                                                    +PaintManager.getInstance().getWidth(mTextPaint, lineText.substring(i, i + 1)) / 2) {
                                        left = contentRect.left + (textX + PaintManager.getInstance().getWidth(mTextPaint, lineText.substring(0, i)));
                                        mFlashPosition = i;
                                        break;
                                    }
                                }
                            } else {
                                mFlashPosition = 0;
                                left = contentRect.left + contentRect.width() / 2;
                            }
                        }
                        mFlashPosition += prePosition;
                    }
                } else {
                    mFlashPosition = 0;
                    top = contentRect.top + textHeight - this.mTextPaintMetrics.bottom;
                    left = contentRect.left;
                }

                left += Const.DP_1;
                int padding = (contentRect.height() - textHeight) / 2 - Const.DP_1 * 2;
                if (padding <= 0) {
                    padding = Const.DP_1 * 2;
                }
                mFlashX = flashX;
                mFlashY = top;
                canvas.drawLine(left, top - textHeight + this.mTextPaintMetrics.bottom, left, top + this.mTextPaintMetrics.bottom, mFlashPaint);
            }
        } else if (BlankBlock.CLASS_FILL_IN.equals(mClass)) {
            super.drawFlash(canvas, blockRect, blockRect, flashX, flashY, position);
        }
    }

    @Override
    protected void drawText(Canvas canvas, String text, Rect blockRect, Rect contentRect, boolean hasBottomLine) {
        if (BlankBlock.CLASS_DELIVERY.equals(mClass)) {
            mTextList.clear();
            if (!TextUtils.isEmpty(text)) {
                float textWidth = PaintManager.getInstance().getWidth(mTextPaint, text);
                float textHeight = PaintManager.getInstance().getHeight(mTextPaint);
                float x;
                x = (float) contentRect.left;
                canvas.save();
                canvas.clipRect(contentRect);
                TextEnv.Align align = this.mTextEnv.getTextAlign();
                float y;
                y = (float) (contentRect.top + PaintManager.getInstance().getHeight(this.mTextPaint)) - this.mTextPaintMetrics.bottom;
                int startPosition = 0;
                if (PaintManager.getInstance().getWidth(mTextPaint, text) > contentRect.width()) {
                    for (int i = 0; i < text.length(); i++) {
                        if (PaintManager.getInstance().getWidth(mTextPaint, text.substring(startPosition, i)) <= contentRect.width() &&
                                PaintManager.getInstance().getWidth(mTextPaint, text.substring(startPosition, i + 1)) > contentRect.width()) {
                            String content = text.substring(startPosition, i);
                            TextInfo info = new TextInfo();
                            info.mStartPos = startPosition;
                            info.mEndPos = i;
                            info.mText = content;
                            info.mY = y;
                            mTextList.add(info);
                            startPosition = i;
                            y += (textHeight + mVerticalSpacing);
                        }
                    }
                    if (!TextUtils.isEmpty(text.substring(startPosition, text.length()))) {
                        TextInfo info = new TextInfo();
                        info.mStartPos = startPosition;
                        info.mEndPos = text.length();
                        info.mText = text.substring(startPosition, text.length());
                        info.mY = y;
                        mTextList.add(info);
                    }
                } else {
                    TextInfo info = new TextInfo();
                    info.mStartPos = 0;
                    info.mEndPos = text.length();
                    info.mText = text;
                    info.mY = y;
                    mTextList.add(info);
                }
                for (int i = 0; i < mTextList.size(); i++) {
                    canvas.drawText(mTextList.get(i).mText, x, mTextList.get(i).mY, this.mTextPaint);
                }
                canvas.restore();
            }
        } else if (!mTextEnv.isEditable()) {
            if (BlankBlock.CLASS_FILL_IN.equals(mClass)) {
                mBottomLinePaint.set(mTextPaint);
                mBottomLinePaint.setStrokeWidth(Const.DP_1);
                super.drawText(canvas, text, blockRect, contentRect, hasBottomLine);
            } else {
                if (TextUtils.isEmpty(text)) {
                    super.drawText(canvas, "( )", blockRect, contentRect, false);
                } else {
                    super.drawText(canvas, "(" + text + ")", blockRect, contentRect, false);
                }
            }
        } else {
            super.drawText(canvas, text, blockRect, contentRect, false);
        }
    }

    @Override
    public String getText() {
        String text = super.getText();
        if (TextUtils.isEmpty(text))
            return "";
        return text;
    }

    public class TextInfo {
        String mText;
        float mY;
        int mStartPos;
        int mEndPos;
    }
}
