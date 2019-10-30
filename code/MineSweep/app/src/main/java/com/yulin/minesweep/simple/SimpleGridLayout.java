package com.yulin.minesweep.simple;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.yulin.minesweep.R;
import com.yulin.minesweep.i.OnGridOperatorListener;
import com.yulin.minesweep.widget.SquareItem;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * 简单模式网络布局区
 */
public class SimpleGridLayout extends ViewGroup implements OnGridOperatorListener {

    private static final String TAG = "houchenl_SimpleGridLay";

    // 默认行列数
    private static final int DEFAULT_ROW_COUNT = 9;
    private static final int DEFAULT_COLUMN_COUNT = 9;

    // 10个雷
    private static final int MINE_COUNT = 10;

    public static final int STATUS_OPEN = 1;
    public static final int STATUS_INSERT_FLAG = 2;

    // 方格行数
    private int mRowCount = DEFAULT_ROW_COUNT;

    // 方格列数
    private int mColumnCount = DEFAULT_COLUMN_COUNT;

    private Paint mGridPaint;

    /**
     * 缓存待点击的空白方格列表的序号
     */
    private LinkedList<Integer> mListEmptySquareIndex = new LinkedList<>();

    private int mStatus = STATUS_OPEN;    // 点击时的操作： 1. 翻开  2. 插旗

    private int mUnOpenedSquares = mRowCount * mColumnCount;

    public SimpleGridLayout(Context context) {
        this(context, null);
    }

    public SimpleGridLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SimpleGridLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init(context);
    }

    private void init(Context context) {
        /*
        * 创建SimpleGridLayout对象时，addChildViews()耗时582ms，其它方法耗时0ms
        * */
        addChildViews(context);
        placeMineRandomly();

        mGridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mGridPaint.setStyle(Paint.Style.STROKE);
        mGridPaint.setColor(getResources().getColor(R.color.grid_line));
        mGridPaint.setStrokeWidth(6);
    }

    /**
     * 添加子布局
     */
    private void addChildViews(Context context) {
        int count = mRowCount * mColumnCount;
        for (int i = 0; i < count; i++) {
            SquareItem item = new SquareItem(context, i, this);
            item.setOperatorListener(this);
            addView(item);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize, heightSize;

        // Get the width based on the measure specs
        widthSize = getDefaultSize(0, widthMeasureSpec);

        // Get the height based on the measure specs
        heightSize = getDefaultSize(0, heightMeasureSpec);

        int majorDimension = Math.min(widthSize, heightSize);
        // Measure all child views
        int blockDimension = majorDimension / mColumnCount;
        int blockSpec = MeasureSpec.makeMeasureSpec(blockDimension, MeasureSpec.EXACTLY);
        measureChildren(blockSpec, blockSpec);

        // Must call this to save our own dimensions
        setMeasuredDimension(majorDimension, majorDimension);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int row, col, left, top;
        for (int i = 0; i < getChildCount(); i++) {
            row = i / mColumnCount;
            col = i % mColumnCount;
            View child = getChildAt(i);
            left = col * child.getMeasuredWidth();
            top = row * child.getMeasuredHeight();

            child.layout(left, top, left + child.getMeasuredWidth(), top + child.getMeasuredHeight());
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);

        // Draw the grid lines
        for (int i = 0; i <= getWidth(); i += (getWidth() / mColumnCount)) {
            canvas.drawLine(i, 0, i, getHeight(), mGridPaint);
        }
        for (int i = 0; i < getHeight(); i += (getHeight() / mColumnCount)) {
            canvas.drawLine(0, i, getWidth(), i, mGridPaint);
        }
    }

    /**
     * 将10个雷随机分布在方格中
     */
    private void placeMineRandomly() {
        for (int i = 0; i < MINE_COUNT; i++) {
            while (true) {
                // 分布第i个雷；先获取雷放置在空格序号，如果该空格已有雷，重新获取序号
                int index = (int) (Math.random() * getChildCount());
                SquareItem item = getItem(index);
                if (item != null) {
                    if (!item.isMine()) {
                        // 没有雷，设置有雷
                        item.setMine(true);
                        updateMineCount(index);
                        break;
                    }
                    // 已有雷，继续获取新方格
                }
            }
        }
    }

    /**
     * 每设置1个方格含雷，将它周围8个方格的周围雷的数量加1
     */
    private void updateMineCount(int index) {
        // 获取含雷方格的行、列数
        int r = getRow(index);
        int c = getColumn(index);

        increaseMineCount(r - 1, c - 1);
        increaseMineCount(r, c - 1);
        increaseMineCount(r + 1, c - 1);
        increaseMineCount(r - 1, c);
        increaseMineCount(r + 1, c);
        increaseMineCount(r - 1, c + 1);
        increaseMineCount(r, c + 1);
        increaseMineCount(r + 1, c + 1);
    }

    /**
     * 将指定方格周围的含雷数加1
     */
    private void increaseMineCount(int r, int c) {
        int index = getIndex(r, c);
        if (index >= 0 && index < getChildCount()) {
            SquareItem item = getItem(index);
            if (item != null) {
                item.increaseMinCount();
            }
        }
    }

    /**
     * 方格序号转化为方格行号，行号从0开始
     */
    private int getRow(int index) {
        if (index < 0 || index >= getChildCount()) {
            return -1;
        }

        return index / mRowCount;
    }

    /**
     * 方格序号转化为方格列号，列号从0开始
     */
    private int getColumn(int index) {
        if (index < 0 || index >= getChildCount()) {
            return -1;
        }

        return index % mRowCount;
    }

    /**
     * 方格行、列数转换为序号
     */
    private int getIndex(int r, int c) {
        if (r < 0 || c < 0 || r >= mRowCount || c >= mColumnCount) {
            // 行、列号越界，返回-1
            return -1;
        }

        // 行、列号正常，返回序号
        return r * mRowCount + c;
    }

    private SquareItem getItem(int index) {
        if (index >= 0 && index < getChildCount()) {
            return (SquareItem) getChildAt(index);
        }

        return null;
    }

    @Override
    public void onClickBug() {
        // 踩到雷时，所有方格都打开
        for (int i = 0; i < getChildCount(); i++) {
            SquareItem item = getItem(i);
            if (item != null) {
                item.setState(SquareItem.STATE_OPENED);
            }
        }
    }

    @Override
    public void onClickEmpty(int index) {
        /*
         * 点开一个空白方格后，触发周围所有方格的点击事件，把它周围所有方格都打开。如果周围仍有空白方格，会继续调用本方法，
         * 直到打开所有连续的空白方格
         * */
        int r = getRow(index);
        int c = getColumn(index);

        openItem(r - 1, c - 1);
        openItem(r, c - 1);
        openItem(r + 1, c - 1);
        openItem(r - 1, c);
        openItem(r + 1, c);
        openItem(r - 1, c + 1);
        openItem(r, c + 1);
        openItem(r + 1, c + 1);

        // 点击缓存中下一个空白方格
        int emptyIndex = popEmptySquareIndex();
        Log.d(TAG, "onClickEmpty: empty size " + (mListEmptySquareIndex.size() + 1) + ", emptyIndex " + emptyIndex);
        if (emptyIndex >= 0) {
            SquareItem item = getItem(emptyIndex);
            if (item != null) {
                item.onSingleTapConfirm();
            }
        }
    }

    /**
    * 尝试自动打开周围所有方格
    * */
    @Override
    public void onDoubleTap(int index) {
        /*
        * 如果周围方格中插旗的方格数和当前方格周围雷数相同，表明已全部找到周围的雷，双击自动打开剩余未插旗的方格。
        * 如果插旗错误，会将雷格打开，game over。如果条件不符合，不响应
        * */

        // 获取周围所有方格
        List<SquareItem> squaresAround = getSquaresAround(index);
        // 获取周围方格中插旗的方格数目，如果有问号的方格，不能自动打开，直接return
        int flagCount = 0;
        for (int i = 0; i < squaresAround.size(); i++) {
            if (squaresAround.get(i).isQuestion()) {
                return;
            }
            if (squaresAround.get(i).isFlaged()) {
                flagCount++;
            }
        }
        // 如果周围插旗方格个数等于当前方格周围雷的个数，尝试打开周围所有未插旗且未打开状态为默认的方格
        SquareItem currentItem = getItem(index);
        if (currentItem != null && currentItem.getMineCount() == flagCount) {
            for (SquareItem item : squaresAround) {
                if (item.isIdle()) {
                    item.onSingleTapConfirm();
                }
            }
        }
        Log.d(TAG, "onDoubleTap: flag count " + flagCount);
    }

    @Override
    public void onOpened(int index) {
        mUnOpenedSquares--;
        if (mUnOpenedSquares == MINE_COUNT) {
            Toast.makeText(getContext(), "success", Toast.LENGTH_SHORT).show();
        }
    }

    // 打开指定位置的方格
    private void openItem(int r, int c) {
        int index = getIndex(r, c);
        if (index >= 0 && index < getChildCount()) {
            SquareItem item = getItem(index);
            if (item != null) {
                if (item.isEmptySquare() && !item.isOpened()) {
                    Log.d(TAG, "openItem: r " + r + ", c " + c + ", index " + index + ", isEmptySquare && !isOpened, add");
                    addEmptySquareIndex(index);
                }
                item.setState(SquareItem.STATE_OPENED);
            }
        }
    }

    private int popEmptySquareIndex() {
        if (mListEmptySquareIndex.size() > 0) {
            return mListEmptySquareIndex.pop();
        }
        return -1;
    }

    private void addEmptySquareIndex(int index) {
        if (!mListEmptySquareIndex.contains(index)) {
            mListEmptySquareIndex.push(index);
        }
    }

    public int getStatus() {
        return mStatus;
    }

    public void setStatus(int status) {
        mStatus = status;
    }

    /**
     * 获取指定方格周围的所有方格
     * @param index 指定方格的序号
     * */
    private List<SquareItem> getSquaresAround(int index) {
        return getSquaresAround(index, -1);
    }

    /**
     * 获取指定方格周围指定状态的所有方格
     * @param index 指定方格的序号
     * @param state 指定状态
     * */
    private List<SquareItem> getSquaresAround(int index, int state) {
        List<SquareItem> items = new ArrayList<>();

        int r = getRow(index);
        int c = getColumn(index);

        addItem(items, getItem(getIndex(r - 1, c - 1)), state);
        addItem(items, getItem(getIndex(r, c - 1)), state);
        addItem(items, getItem(getIndex(r + 1, c - 1)), state);
        addItem(items, getItem(getIndex(r - 1, c)), state);
        addItem(items, getItem(getIndex(r + 1, c)), state);
        addItem(items, getItem(getIndex(r - 1, c + 1)), state);
        addItem(items, getItem(getIndex(r, c + 1)), state);
        addItem(items, getItem(getIndex(r + 1, c + 1)), state);

        return items;
    }

    /**
     * @param state -1表示所有状态
     * */
    private void addItem(List<SquareItem> items, SquareItem item, int state) {
        if (items != null && item != null && item.getState() == state) {
            if (item.getState() == state || state == -1) {
                items.add(item);
            }
        }
    }

}
