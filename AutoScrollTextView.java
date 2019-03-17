package com.iflytek.record.ainoterecord.ui.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.support.v7.widget.AppCompatEditText;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

import com.iflytek.framelib.util.RefreshModeUtils;
import com.iflytek.framelib.util.StringUtils;
import com.iflytek.framelib.util.ToastUtil;
import com.iflytek.record.ainoterecord.R;
import com.iflytek.record.ainoterecord.model.bean.LineInfo;
import com.iflytek.record.ainoterecord.model.bean.RecordFragment;
import com.iflytek.record.ainoterecord.ui.span.CubeSpan;
import com.iflytek.record.ainoterecord.ui.span.LineSpan;
import com.iflytek.record.ainoterecord.util.TextPaintUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * 自动滚动换行
 * 这是个EditText 不是TextView
 */
public class AutoScrollTextView extends AppCompatEditText {
    private String TAG = AutoScrollTextView.class.getSimpleName();
    private ViewCallBack textOnClick;
    private List<LineBean> lineHeight = new ArrayList<>();
    private StringBuilder variableString = new StringBuilder();
    private StringBuilder stableString = new StringBuilder();
    private int preLen;
    private GestureDetector mGestureDetector;
    /**
     * 高亮相关
     **/
    final BackgroundColorSpan bgColorSpan = new BackgroundColorSpan(Color.parseColor("#FF000000"));
    final ForegroundColorSpan colorSpan = new ForegroundColorSpan(Color.parseColor("#FFFFFFFF"));
    private int start, end; // 高亮起始位置
    // 保留位置信息
    private List<LineInfo> lineInfoHashMap = new ArrayList<>();

    private Runnable task = new Runnable() {
        @Override
        public void run() {
            computePagingValue();
        }
    };

    public AutoScrollTextView(Context context) {
        super(context);
    }

    public AutoScrollTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AutoScrollTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mGestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {

            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onDoubleTapEvent(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onContextClick(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (velocityX < -500) {
                    nextPage();
                } else if (velocityX > 500) {
                    prePage();
                }
                return true;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                return true;
            }


            @Override
            public boolean onSingleTapUp(final MotionEvent e) {
                Log.e(TAG, "onTextClick onSingleTapUp" + e.getX());
                if (textOnClick == null) return true;
                int i, y = 0;
                for (i = 0; i < lineHeight.size(); i++) {
                    y += lineHeight.get(i).lineHeight;
                    if (y > (e.getY() + getScrollY())) {
                        break;
                    }
                }
                if (i >= lineHeight.size()) {
                    return true;
                }
                LineBean bean = lineHeight.get(i);
                int index = bean.start;
                int end = Math.min(bean.end, getText().length());
                if (index > end || end > length() || end < 0 || index > length()) {
                    return true;
                }
                String content = getText().subSequence(bean.start, end).toString();
                Log.e(TAG, "index = " + index + ", content: " + content);

                // 确定index的位置
                float[] widths = TextPaintUtil.getArrays(AutoScrollTextView.this, content);
                int position;
                for (position = 0; position < widths.length; position++) {
                    if (widths[position] > e.getX()) {
                        Log.e(TAG, "position = " + (position) + ", content = " + content.substring(position));
                        index += position;
                        break;
                    }
                }

                if (content.startsWith(StringUtils.DOUBLE_SPACE)) index += 3;
                if (content.endsWith(System.lineSeparator()) && position == 0)
                    index += content.length();

                for (LineInfo info : lineInfoHashMap) {
                    if (info.start <= index && index <= info.end) {
                        textOnClick.onTextClick(info.id, info.time, info.msg);
                        setHighLight(info.start, info.end);
                    }
                }
                return true;
            }
        });

        Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/Roboto-Regular.ttf");//根据路径得到Typeface
        setTypeface(tf);//设置字体
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        String str = getResources().getString(R.string.network_connect_transfer);
        if (str.equals(getText().toString()) || (textOnClick != null && (textOnClick.getModelStatus() == HeadViewControl.RECORD_OPTION_STATUS_TRANSFER))) {
            return false;
        } else {
            return mGestureDetector.onTouchEvent(event);
        }
    }


    public void setTextOnClick(ViewCallBack textOnClick) {
        this.textOnClick = textOnClick;
    }


    //------控制滚动----------
    SpannableString transferSs;
    StyleSpan styleSpan = new StyleSpan(android.graphics.Typeface.ITALIC);
    int count = 0;
    ArrayList<Integer> indexs = new ArrayList<>();

    public void addString(boolean isFlush, boolean isTemp, String content) {
        if (getContext().getResources().getString(R.string.network_connect_transfer).equals(getText().toString())) {
            setText("");
        }
        if (stableString.length() == 0) {
            stableString.append(StringUtils.DOUBLE_SPACE);
            append(StringUtils.DOUBLE_SPACE);
        }

        if (getText().toString().endsWith(content) && content.contains(HeadViewControl.OFFLINE_PLACEHOLDER)) {
            return;
        }
        variableString.delete(0, variableString.length());
        if (!isTemp) {
            // 处理字符串
            int start = stableString.length();
            stableString.append(content);
            for (int i = start; i < stableString.length(); i++) {
                if (StringUtils.isCutting(stableString.charAt(i))) {
                    if ((++count) % 3 == 0) indexs.add(i);
                }
            }
            Collections.reverse(indexs);
            for (Integer index : indexs) {
                stableString.insert(index + 1, StringUtils.INSERT_EMPTY);
            }
            indexs.clear();
        } else {
            if (isFlush) {
                preLen = stableString.length();
            }
            variableString.append(content);
        }
        if (isFlush) {
            if (preLen > length()) {
                //可能会造成ANR, 优化思路就是匹配字符串，用delete方式处理
                setText(stableString);
            } else {
                getText().delete(preLen, length());
            }
            if (preLen < stableString.length()) {
                getText().append(stableString.substring(preLen));
            }
            RefreshModeUtils.setShowMode(this, RefreshModeUtils.EINK_SHOW_MODE_UIFC);
            computeScroll(true);
        } else {
            RefreshModeUtils.resetShowMode(this);
        }
    }

    /**
     * @param data
     * @param //recording 是否在录音
     */


    public String EMPTY = StringUtils.DOUBLE_SPACE + StringUtils.DOUBLE_SPACE + StringUtils.DOUBLE_SPACE + StringUtils.DOUBLE_SPACE +
            StringUtils.DOUBLE_SPACE + StringUtils.DOUBLE_SPACE + StringUtils.DOUBLE_SPACE + StringUtils.DOUBLE_SPACE +
            StringUtils.DOUBLE_SPACE + StringUtils.DOUBLE_SPACE + StringUtils.DOUBLE_SPACE + StringUtils.DOUBLE_SPACE +
            StringUtils.DOUBLE_SPACE + StringUtils.DOUBLE_SPACE + StringUtils.DOUBLE_SPACE + StringUtils.DOUBLE_SPACE +
            StringUtils.DOUBLE_SPACE + StringUtils.DOUBLE_SPACE + StringUtils.DOUBLE_SPACE + StringUtils.DOUBLE_SPACE + StringUtils.DOUBLE_SPACE + System.lineSeparator();


    /**
     * 说明: 如果有翻译内容首行没有空两个，前后有分隔符以及换行。否则每三个分隔符号进行首行空两行
     *
     * @param data
     */

    StringBuilder preB = new StringBuilder();

    public void setTextByWords(final List<RecordFragment> data) {
        Log.e(TAG, "setTextByWords---- before");

        StringBuilder sb = new StringBuilder();
        for (RecordFragment item : data) {
            sb.append(item.getContent().replaceAll(StringUtils.REG_EMPTY, ""));
        }

        if (sb.toString().equals(preB.toString())) {
            return;
        }
        preB = new StringBuilder(sb.toString());

        // 判断两次内容是否一样，如果一样直接退出

        setWords(data);

        Log.e(TAG, "setTextByWords---- after");
    }


    private void setWords(final List<RecordFragment> data) {
        if (!data.isEmpty())
            setText("[正在渲染中]");
        lineInfoHashMap.clear();
        new Thread(new Runnable() {
            @Override
            public void run() {

                final SpannableStringBuilder ssb = new SpannableStringBuilder();
                int count = 0;
                int index = 0; // 当前所在位置
                boolean isDivided = false;
                boolean has = false;// 是否用标点符号
                // 首行空两格
                ssb.append(StringUtils.DOUBLE_SPACE);
                // 插入换行符操作
                List<Integer> points = new ArrayList<>();
                for (RecordFragment record : data) {
                    boolean hasTranslate = !TextUtils.isEmpty(record.getTranslate());
                    boolean isFirst = (index == 0);
                    int key = (record.getStartIndex() + record.getEndIndex()) / 2, start, end;

                    if (hasTranslate) { //
                        if (isFirst) ssb.clear();
                        // 没有分割线
                        if (ssb.length() > 0 && !isDivided) {
                            cutOffule();
                        }
                        start = ssb.length();
                        ssb.append(record.getContent().replaceAll("\\s*", ""));
                        // 添加换行
                        ssb.append(System.lineSeparator());
                        // 添加翻译内容
                        SpannableString spannableString = new SpannableString(StringUtils.DOUBLE_SPACE + record.getTranslate().replaceAll("\\s*", ""));
                        spannableString.setSpan(new CubeSpan(), 0, 3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        ssb.append(spannableString);
                        // 添加分割线
                        cutOffule();
                        isDivided = true;
                        count = 0;
                        has = true;

                    } else { // 没有翻译内容
                        // 首行空两格
                        if (count == 0 && has) {
                            ssb.append(System.lineSeparator());
                            ssb.append(StringUtils.DOUBLE_SPACE);
                        }
                        start = ssb.length();


                        isDivided = false;
                        int l = ssb.length();
                        ssb.append(record.getContent().replaceAll("\\s*", ""));
                        has = false;
                        for (int i = l; i < ssb.length(); i++) {
                            if (StringUtils.isCutting(ssb.charAt(i))) {
                                has = true;
                                if ((++count) % 3 == 0) {
                                    points.add(i);
                                }
                            }
                        }

                        Collections.reverse(points);
                        for (Integer i : points) {
                            ssb.insert(i + 1, StringUtils.INSERT_EMPTY);
                        }
                        points.clear();
                    }
//                    Log.e(TAG, "record: " + record.getContent().replaceAll("\\s*", ""));
                    end = ssb.length();
                    lineInfoHashMap.add(new LineInfo(record.getId(), start, end, record.getBg(), record.getContent()));
                    index++;
                }

                post(new Runnable() {
                    @Override
                    public void run() {
                        stableString = new StringBuilder(ssb);
                        setText(ssb);
                        playAudioModel();
                    }
                });

            }
        }).start();

    }


    // 添加分割线
    private void cutOffule() {
        append(System.lineSeparator());
        append(EMPTY);
        getText().setSpan(new LineSpan(), length() - EMPTY.length(), length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    /**
     * 要计算翻页
     */
    public void playAudioModel() {
        removeCallbacks(task);
        postDelayed(task, HeadViewControl.DELAY_TIME);
    }

    // 下一页
    public void nextPage() {

        int y = getScrollY();
        if (y + getHeight() < maxHeight) {
            scrollBy(0, getHeight());
        } else {
            ToastUtil.showNoticeToast(getContext().getApplicationContext(), "最后一页了");
        }

    }

    // 上一页
    public void prePage() {
        int y = getScrollY();
        if (y <= 10) {
            ToastUtil.showNoticeToast(getContext().getApplicationContext(), "首页");
        } else {
            int value = getScrollY() - getHeight();
            if (value <= 0) {
                scrollTo(0, 0);
            } else {
                scrollBy(0, -getHeight());
            }
        }
    }


    /**
     * @param isScroll 是否滚动到最后一页
     */
    private void computeScroll(final boolean isScroll) {
        if (transferSs != null) {
            transferSs = null;
        }
        Layout layout = getLayout();
        transferSs = new SpannableString(variableString);
        transferSs.setSpan(styleSpan, 0, variableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        append(transferSs);
        setSelection(length());
        int height = 0;
        Rect rect = new Rect();
        for (int i = 0; i < getLineCount(); i++) {
            layout.getLineBounds(i, rect);
            height += rect.height();
        }
        Log.e(TAG, String.format("LineCount = %d, isScroll = %b, tvContent.height = %d, height = %d", getLineCount(), isScroll, getHeight(), height));
        if (height > getHeight() && isScroll) {
            int scrollY = height - getHeight();
            setScrollY(scrollY);
            invalidate();
            Log.e(TAG, String.format("LineCount = %d, tvContent.height = %d, height = %d, value = %d, scrollY = %d", getLineCount(), getHeight(), height, scrollY, getScrollY()));
        }
    }


    /* 播放录音状态下更新 翻页使用 计算翻页 */
    private int maxHeight = 0;

    private void computePagingValue() {
        // 重置标记位
        lineHeight.clear();
        Log.e(TAG, "Hello computePagingValue  " + getScrollY());
        if (getScrollY() != 0) {
            scrollTo(0, 0);
        }
        Layout layout = getLayout();
        Rect rect = new Rect();
        maxHeight = 0;
        for (int i = 0; i < getLineCount(); i++) {
            layout.getLineBounds(i, rect);
            LineBean bean = new LineBean(i, rect.height(), layout.getLineStart(i), layout.getLineEnd(i));
            lineHeight.add(bean);
            maxHeight += rect.height();
        }
        // 双重for循环 处理
        for (LineInfo info : lineInfoHashMap) {
            int y = 0;
            for (LineBean line : lineHeight) {
                if (info.start <= line.end) {
                    info.scrollStart = y;
                    break;
                }
                y += line.lineHeight;
            }

            y = 0;
            for (LineBean line : lineHeight) {
                y += line.lineHeight;
                if (info.end <= line.end) {
                    info.scrollEnd = y;
                    break;
                }
            }
        }
    }


    /**
     * @param start -1 清除高亮效果
     * @param end
     */
    public void setHighLight(int start, int end) {
        Log.e(TAG, String.format("setHighLight start = %d end = %d  len = %d", start, end, stableString.length()));

        if (start == -2) {// 重置操作
            this.start = -1;
            this.end = -1;
        }
        if (start == 0 && end == 0) {
            setText(stableString);
            append(variableString);
            return;
        }
        if ((TextUtils.isEmpty(stableString))) {
            Log.e(TAG, "isEmpty.." + start);
            return;
        }
        if (start == -1) {
            Log.e(TAG, "setHighLight length " + length());
            scrollTo(0, 0);
            return;
        }

        if (end > length()) {
            Log.e(TAG, "setHighLight length end > length() ");

            return;
        }


        if (this.start == start && this.end == end) {
            Log.e(TAG, "setHighLight length this.start == start && this.end == end");
            return;
        }

        int mid = (start + end) / 2;
        // 双重for循环 处理
        for (LineInfo info : lineInfoHashMap) {
            if (info.start <= mid && mid <= info.end && info.scrollEnd != 0) {
                getText().setSpan(bgColorSpan, info.start, info.end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                getText().setSpan(colorSpan, info.start, info.end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                if (info.scrollStart >= getScrollY() && getScrollY() + getHeight() >= info.scrollEnd) {

                } else {
                    scrollTo(0, info.scrollStart);
                }

                this.start = start;
                this.end = end;
                return;
            }

        }
        Log.e(TAG, "setHighLight setHighLight setHighLight");
    }


    public void setHighLightById(long id) {
        for (LineInfo info : lineInfoHashMap) {
            if (Long.compare(id, info.id) == 0) {
                setHighLight(info.start, info.end);
                return;
            }
        }
    }

    interface ViewCallBack {
        // 文字点击
        void onTextClick(long id, int bg, String text);

        //        boolean isTransferStatus();
        int getModelStatus();
    }

}