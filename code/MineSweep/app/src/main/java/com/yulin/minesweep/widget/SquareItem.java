package com.yulin.minesweep.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.yulin.minesweep.R;
import com.yulin.minesweep.i.OnGridOperatorListener;
import com.yulin.minesweep.simple.SimpleGridLayout;

/**
 * 方格
 */
public class SquareItem extends View {

    private static final String TAG = "houchenl_SquareItem";

    // 4种状态：未打开默认、未打开插旗、未打开问号、打开
    public static final int STATE_IDLE = 0;
    public static final int STATE_FLAG = 1;
    private static final int STATE_QUESTION = 2;
    public static final int STATE_OPENED = 3;

    private int mIndex;
    private boolean mIsMine = false;    // 是否是有雷的格子
    private int mMineCount;    // 本方格周围九宫格范围内雷的个数

    private int mState = STATE_IDLE;

    private int mBackgroundColor = getResources().getColor(R.color.grid_bg_default);

    private Paint mPaint;
    private Paint mTextPaint;
    private Rect mImageRect = new Rect(10, 10, 30, 30);
    private Bitmap mBugBitmap;
    private Bitmap mFlagBitmap;
    private Bitmap mQuestionBitmap;

    private OnGridOperatorListener mOperatorListener;

    private SimpleGridLayout mParent;

    private GestureDetector mGestureDetector;

    public SquareItem(Context context) {
        this(context, null);
    }

    public SquareItem(Context context, int index, SimpleGridLayout parent) {
        this(context, null);
        mIndex = index;
        mParent = parent;
    }

    public SquareItem(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SquareItem(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.YELLOW);

        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextSize(60);
        mTextPaint.setColor(Color.BLACK);

        mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                return onSingleTapConfirm();
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                return onDoubleTapConfirm();
            }
        });
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mImageRect = new Rect(0, 0, w, h);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
//        Log.d(TAG, "onDraw: mState " + mState);

        canvas.drawColor(mBackgroundColor);

        // 如果已打开，如果是雷，画雷，如果旁边数字大于0，显示数字
        if (mState == STATE_OPENED) {
            if (isMine()) {
                canvas.drawBitmap(getBugBitmap(), null, mImageRect, null);
            } else if (mMineCount > 0) {
                String mineCount = String.valueOf(mMineCount);
                canvas.drawText(mineCount, 60, 60, mTextPaint);
            }
        } else if (mState == STATE_FLAG) {
            canvas.drawBitmap(getFlagBitmap(), null, mImageRect, null);
        } else if (mState == STATE_QUESTION) {
            canvas.drawBitmap(getQuestionBitmap(), null, mImageRect, null);
        }
    }

    public boolean onSingleTapConfirm() {
        Log.d(TAG, "onSingleTap: ");
        if (mParent.getStatus() == SimpleGridLayout.STATUS_OPEN) {
            // 如果当前状态是插旗或问号，不可点击
            if (mState == STATE_FLAG || mState == STATE_QUESTION) {
                return true;
            }

            // 翻开方格
            if (isMine()) {
                // 踩到雷
                mOperatorListener.onClickBug();
                return true;
            } else if (mMineCount == 0) {
                mOperatorListener.onClickEmpty(mIndex);
            }

            openSquare();
        } else if (mParent.getStatus() == SimpleGridLayout.STATUS_INSERT_FLAG) {
            // 插旗或问号，如果当前是默认状态，设置为插旗；如果当前是插旗状态，设置为问号；如果当前是问号，设置为默认
            if (mState == STATE_IDLE) {
                setState(STATE_FLAG);
            } else if (mState == STATE_FLAG) {
                setState(STATE_QUESTION);
            } else if (mState == STATE_QUESTION) {
                setState(STATE_IDLE);
            }
        }

        return true;
    }

    private boolean onDoubleTapConfirm() {
        Log.d(TAG, "onDoubleTap: ");

        /*
        * 双击时，如果当前方格已打开，且周围雷数大于0，尝试自动打开周围所有方格
        * */
        if (isOpened() && mMineCount > 0 && mOperatorListener != null) {
            mOperatorListener.onDoubleTap(mIndex);
        }

        return false;
    }

    public void openSquare() {
        setState(STATE_OPENED);
    }

    public void setState(int state) {
        Log.d(TAG, "setState: mState " + mState + ", state " + state);
        if (mState == state) {
            return;
        }
        if (state == STATE_OPENED) {
            if (mOperatorListener != null) {
                // 打开成功，避免重复打开
                mOperatorListener.onOpened(mIndex);
            }
        }
        mState = state;
        updateDisplay();
    }

    public int getState() {
        return mState;
    }

    // 根据状态刷新显示
    private void updateDisplay() {
        Log.d(TAG, "updateDisplay: mState " + mState);
        switch (mState) {
            case STATE_FLAG:
            case STATE_QUESTION:
            case STATE_IDLE:
                invalidate();
                break;
            case STATE_OPENED:
                showOpened();
                break;
        }
    }

    // 显示已打开
    private void showOpened() {
        mBackgroundColor = getResources().getColor(R.color.grid_bg_opened);
        invalidate();
    }

    public void setMine(boolean mine) {
        mIsMine = mine;
    }

    public boolean isMine() {
        return mIsMine;
    }

    public int getMineCount() {
        return mMineCount;
    }

    public void setMineCount(int mineCount) {
        mMineCount = mineCount;
    }

    public void increaseMinCount() {
        mMineCount++;
    }

    public void setOperatorListener(OnGridOperatorListener operatorListener) {
        mOperatorListener = operatorListener;
    }

    /**
     * 是否是空白方格
     */
    public boolean isEmptySquare() {
        return mMineCount == 0;
    }

    public boolean isOpened() {
        return mState == STATE_OPENED;
    }

    public boolean isFlaged() {
        return mState == STATE_FLAG;
    }

    public boolean isQuestion() {
        return mState == STATE_QUESTION;
    }

    public boolean isIdle() {
        return mState == STATE_IDLE;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mGestureDetector != null && mGestureDetector.onTouchEvent(event)) {
            return true;
        }
        return super.onTouchEvent(event);
    }

    private Bitmap getBugBitmap() {
        if (mBugBitmap == null) {
            mBugBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.bug);
        }
        return mBugBitmap;
    }

    private Bitmap getFlagBitmap() {
        if (mFlagBitmap == null) {
            mFlagBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.flag);
        }
        return mFlagBitmap;
    }

    private Bitmap getQuestionBitmap() {
        if (mQuestionBitmap == null) {
            mQuestionBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.question);
        }
        return mQuestionBitmap;
    }

}
