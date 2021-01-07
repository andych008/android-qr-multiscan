package com.smartahc.android.coreqr;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.CornerPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Paint.Join;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import com.google.zxing.Result;
import com.google.zxing.ResultPoint;


public class ViewFinderView extends View implements IViewFinder {
    private static final String TAG = "ViewFinderView";

    private Rect mFramingRect;
    private static final int[] SCANNER_ALPHA = new int[]{0, 64, 128, 192, 255, 192, 128, 64};
    private int scannerAlpha;
    private static final int POINT_SIZE = 10;
    private static final long ANIMATION_DELAY = 80L;
    private final int mDefaultLaserColor;
    private final int mDefaultMaskColor;
    private final int mDefaultBorderColor;
    private final int mDefaultBorderStrokeWidth;
    private final int mDefaultBorderLineLength;
    protected Paint mLaserPaint;
    protected Paint mFinderMaskPaint;
    protected Paint mBorderPaint;
    protected Paint mPointPaint;
    protected int mBorderLineLength;
    protected boolean mSquareViewFinder;
    private boolean mIsLaserEnabled;
    private float mBordersAlpha;
    private int mViewFinderOffset;

    private Result[] rawResult;
    private float resultScale =1.0f;

    public void setResultScale(float resultScale) {
        this.resultScale = resultScale;
    }


    void setRawResult(Result... rawResult) {
        if (rawResult != null && rawResult.length > 0) {
            this.rawResult = rawResult;
            postInvalidate();
        }
    }
    public ViewFinderView(Context context) {
        super(context);
        this.mDefaultLaserColor = this.getResources().getColor(R.color.viewfinder_laser);
        this.mDefaultMaskColor = this.getResources().getColor(R.color.viewfinder_mask);
        this.mDefaultBorderColor = this.getResources().getColor(R.color.viewfinder_border);
        this.mDefaultBorderStrokeWidth = this.getResources().getInteger(R.integer.viewfinder_border_width);
        this.mDefaultBorderLineLength = this.getResources().getInteger(R.integer.viewfinder_border_length);
        this.mViewFinderOffset = 0;
        this.init();
    }

    public ViewFinderView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mDefaultLaserColor = this.getResources().getColor(R.color.viewfinder_laser);
        this.mDefaultMaskColor = this.getResources().getColor(R.color.viewfinder_mask);
        this.mDefaultBorderColor = this.getResources().getColor(R.color.viewfinder_border);
        this.mDefaultBorderStrokeWidth = this.getResources().getInteger(R.integer.viewfinder_border_width);
        this.mDefaultBorderLineLength = this.getResources().getInteger(R.integer.viewfinder_border_length);
        this.mViewFinderOffset = 0;
        this.init();
    }

    private void init() {
        this.mLaserPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.mLaserPaint.setColor(this.mDefaultLaserColor);
//        this.mLaserPaint.setStyle(Style.FILL);
        this.mFinderMaskPaint = new Paint();
        this.mFinderMaskPaint.setColor(this.mDefaultMaskColor);

        this.mPointPaint = new Paint();
        this.mPointPaint.setColor(this.mDefaultBorderColor);
        this.mPointPaint.setStyle(Style.FILL);
        this.mPointPaint.setStrokeWidth((float) this.mDefaultBorderStrokeWidth);
        this.mPointPaint.setAntiAlias(true);

        this.mBorderPaint = new Paint();
        this.mBorderPaint.setColor(this.mDefaultBorderColor);
        this.mBorderPaint.setStyle(Style.STROKE);
        this.mBorderPaint.setStrokeWidth((float) this.mDefaultBorderStrokeWidth);
        this.mBorderPaint.setAntiAlias(true);
        this.mBorderLineLength = this.mDefaultBorderLineLength;
        this.scannerLineMoveDistance = dp2Px(5);
        this.scannerLineHeight = dp2Px(3);
    }

    public void setLaserColor(int laserColor) {
        this.mLaserPaint.setColor(laserColor);
    }

    public void setMaskColor(int maskColor) {
        this.mFinderMaskPaint.setColor(maskColor);
    }

    public void setBorderColor(int borderColor) {
        this.mBorderPaint.setColor(borderColor);
    }

    public void setBorderStrokeWidth(int borderStrokeWidth) {
        this.mBorderPaint.setStrokeWidth((float) borderStrokeWidth);
    }

    public void setBorderLineLength(int borderLineLength) {
        this.mBorderLineLength = borderLineLength;
    }

    public void setLaserEnabled(boolean isLaserEnabled) {
        this.mIsLaserEnabled = isLaserEnabled;
    }

    public void setBorderCornerRounded(boolean isBorderCornersRounded) {
        if (isBorderCornersRounded) {
            this.mBorderPaint.setStrokeJoin(Join.ROUND);
        } else {
            this.mBorderPaint.setStrokeJoin(Join.BEVEL);
        }

    }

    public void setBorderAlpha(float alpha) {
        int colorAlpha = (int) (255.0F * alpha);
        this.mBordersAlpha = alpha;
        this.mBorderPaint.setAlpha(colorAlpha);
    }

    public void setBorderCornerRadius(int borderCornersRadius) {
        this.mBorderPaint.setPathEffect(new CornerPathEffect((float) borderCornersRadius));
    }

    public void setViewFinderOffset(int offset) {
        this.mViewFinderOffset = offset;
    }

    public void setSquareViewFinder(boolean set) {
        this.mSquareViewFinder = set;
    }

    public void setupViewFinder() {
        this.updateFramingRect();
        this.invalidate();
    }

    public Rect getFramingRect() {
        return this.mFramingRect;
    }

    private int scannerStart = 0;
    private int scannerEnd = 0;
    private int scannerLineHeight = 0;
    private int scannerLineMoveDistance = 0;

    public void onDraw(Canvas canvas) {
        if (mFramingRect != null) {
            drawResultPoint(canvas);

            if (this.mIsLaserEnabled) {
                if(scannerStart == 0 || scannerEnd == 0) {
                    scannerStart = mFramingRect.top+mFramingRect.height()/5;
                    scannerEnd = mFramingRect.bottom - scannerLineHeight-mFramingRect.height()*2/5;
                }

                this.drawLineScanner(canvas);
//                this.drawLaser(canvas);
            }


            postInvalidateDelayed(ANIMATION_DELAY, 0, 0, getWidth(), getHeight());
        }
    }

    public void drawViewFinderMask(Canvas canvas) {
        int width = canvas.getWidth();
        int height = canvas.getHeight();
        Rect framingRect = this.getFramingRect();
        canvas.drawRect(0.0F, 0.0F, (float) width, (float) framingRect.top, this.mFinderMaskPaint);
        canvas.drawRect(0.0F, (float) framingRect.top, (float) framingRect.left, (float) (framingRect.bottom + 1), this.mFinderMaskPaint);
        canvas.drawRect((float) (framingRect.right + 1), (float) framingRect.top, (float) width, (float) (framingRect.bottom + 1), this.mFinderMaskPaint);
        canvas.drawRect(0.0F, (float) (framingRect.bottom + 1), (float) width, (float) height, this.mFinderMaskPaint);
    }

    /**
     * 绘制线性式扫描
     * @param canvas
     */
    private void drawLineScanner(Canvas canvas){

        Rect frame = this.getFramingRect();
        //线性渐变
        LinearGradient linearGradient = new LinearGradient(
                frame.left, scannerStart,
                frame.left, scannerStart + scannerLineHeight,
                shadeColor(mDefaultLaserColor),
                mDefaultLaserColor,
                Shader.TileMode.MIRROR);

        mLaserPaint.setShader(linearGradient);
        if(scannerStart <= scannerEnd) {
            //椭圆
            RectF rectF = new RectF(frame.left + 2 * scannerLineHeight, scannerStart, frame.right - 2 * scannerLineHeight, scannerStart + scannerLineHeight);
            canvas.drawOval(rectF, mLaserPaint);
            scannerStart += scannerLineMoveDistance;
        } else {
            scannerStart = frame.top;
        }
    }


    public void drawLaser(Canvas canvas) {
        Rect framingRect = this.getFramingRect();
        this.mLaserPaint.setAlpha(SCANNER_ALPHA[this.scannerAlpha]);
        this.scannerAlpha = (this.scannerAlpha + 1) % SCANNER_ALPHA.length;
        int middle = framingRect.height() / 2 + framingRect.top;
        canvas.drawRect((float) (framingRect.left + 2), (float) (middle - 1), (float) (framingRect.right - 1), (float) (middle + 2), this.mLaserPaint);
        postInvalidateDelayed(ANIMATION_DELAY, 0, 0, getWidth(), getHeight());
    }


    private void drawResultPoint4(Canvas canvas) {
        if (rawResult != null && rawResult.length > 0) {
            Rect framingRect = this.getFramingRect();
            int frameLeft = framingRect.left;
            int frameTop = framingRect.top;

            for (Result result : rawResult) {
                ResultPoint[] points = result.getResultPoints();
                for (ResultPoint point : points) {
                    canvas.drawCircle(frameLeft + (int) (point.getX() / resultScale),
                            frameTop + (int) (point.getY() / resultScale),
                            POINT_SIZE, mPointPaint);
                }
            }
        }
    }

    private void drawResultPoint(Canvas canvas) {
        if (rawResult != null && rawResult.length > 0) {
            Rect framingRect = this.getFramingRect();
            int frameLeft = framingRect.left;
            int frameTop = framingRect.top;

            for (Result result : rawResult) {
                ResultPoint[] points = result.getResultPoints();
                float x = 0f;
                float y = 0f;
                for (ResultPoint point : points) {
                    x+=point.getX();
                    y+=point.getY();
                }
                canvas.drawCircle(frameLeft + (int) (x/points.length / resultScale),
                        frameTop + (int) (y/points.length / resultScale),
                        POINT_SIZE, mPointPaint);
            }
        }
    }

    protected void onSizeChanged(int xNew, int yNew, int xOld, int yOld) {
        this.updateFramingRect();
    }

    public synchronized void updateFramingRect() {
        this.mFramingRect = new Rect(this.mViewFinderOffset,  this.mViewFinderOffset,
                this.getWidth() - this.mViewFinderOffset, this.getHeight() - this.mViewFinderOffset);
//        scannerLineHeight=getHeight()*2/5;
    }

    /**
     * 处理颜色模糊
     * @param color
     * @return
     */
    private int shadeColor(int color) {
        String hax = Integer.toHexString(color);
        String result = "01"+hax.substring(2);
        return Integer.valueOf(result, 16);
    }

    private int dp2Px(int dp) {
        return (int) (dp * getContext().getResources().getDisplayMetrics().density + 0.5f);
    }
}