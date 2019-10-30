package com.yulin.minesweep.i;

public interface OnGridOperatorListener {

    // 点击含有雷的方格
    void onClickBug();

    /**
     * 点击空白方格，即周围1个雷都没有的方格
     * @param index 空白方格的序号
     * */
    void onClickEmpty(int index);

    /**
     * 双击已点开方格，若周围雷数大于0，尝试自动打开所有方格
     * */
    void onDoubleTap(int index);

    void onOpened(int index);

}
