package ccx.stefan.voiceeffect;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.Log;
import android.view.animation.LinearInterpolator;
import android.widget.TextView;


/**
 *
 */
public class CustomActiveView extends TextView {

    private static final String tag = CustomActiveView.class.getSimpleName();

    private static final int DEFAULT_RIPPLE_COLOR = Color.parseColor("#7f1c96e2");
    /**
     * 波纹的颜色
     */
    private int mRippleColor = DEFAULT_RIPPLE_COLOR;
    /**
     * 默认的波纹的最小值
     */
    private int mMinSize = 200;
    /**
     * 波纹动画效果是否正在进行
     */
    private boolean animationRunning = false;

    private float currentProgress = 0;
    /**
     * 动画中波纹的个数
     */
    private int mRippleNum = 4;

    private int mTotalTime = Integer.MAX_VALUE;

    private int DEFAULT_DURATION = 500;
    public static final int MODE_IN = 1;
    public static final int MODE_OUT = 2;

    private int mode = MODE_OUT;

    private int mPeriod = 10;
    private int mCenterX;
    private int mCenterY;
    private float mRadius;
    private Paint mPaint;
    /**
     * 计算变换的最终progress
     */
    private int mTarget = 0;



    private RadialGradient mRadialGradient;
    private ObjectAnimator mAnimator;
    private ValueAnimator mValueAnimator;

    public CustomActiveView(Context context) {
        super(context);
        initPaint();
        initAnimation();
    }

    public CustomActiveView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initPaint();
        initAnimation();
    }

    public CustomActiveView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initPaint();
        initAnimation();
    }

    public void setTargetAnimProgress(final int target) {
        if (mValueAnimator != null && mValueAnimator.isRunning()) {
            mValueAnimator.cancel();
        }
        createAnim(target);

    }

    private void createAnim(final int target) {
        mValueAnimator = ObjectAnimator.ofInt(this, "currentProgress", mTarget, target);
        mValueAnimator.setDuration(DEFAULT_DURATION);
        mValueAnimator.start();
        mValueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mTarget = (int) currentProgress;
            }
        });
    }

    private void initPaint() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaint.setColor(mRippleColor);


    }

    private void initAnimation() {
        mAnimator = ObjectAnimator.ofInt(this, "currentProgress", 0, 100);
        mAnimator.setRepeatCount(ObjectAnimator.INFINITE);
        mAnimator.setRepeatMode(ObjectAnimator.RESTART);
        mAnimator.setInterpolator(new LinearInterpolator());
        mAnimator.setEvaluator(mProgressEvaluator);
        mAnimator.setDuration(mTotalTime);
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public void startRippleAnimation() {
        if (!animationRunning) {
            mAnimator.start();
            animationRunning = true;
        }
    }

    public void stopRippleAnimation() {
        if (animationRunning) {
            mAnimator.end();
            animationRunning = false;
        }
    }

    public boolean isRippleAnimationRunning() {
        return animationRunning;
    }

    public float getCurrentProgress() {
        return currentProgress;
    }

    public void setCurrentProgress(int currentProgress) {
        this.currentProgress = currentProgress;
        invalidate();

    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int resultWidth = 0;
        int modeWidth = MeasureSpec.getMode(widthMeasureSpec);
        int sizeWidth = MeasureSpec.getSize(widthMeasureSpec);
        if (modeWidth == MeasureSpec.EXACTLY) {
            resultWidth = sizeWidth;
        } else {
            resultWidth = mMinSize;
            if (modeWidth == MeasureSpec.AT_MOST) {
                resultWidth = Math.min(resultWidth, sizeWidth);
            }
        }

        int resultHeight = 0;
        int modeHeight = MeasureSpec.getMode(heightMeasureSpec);
        int sizeHeight = MeasureSpec.getSize(heightMeasureSpec);
        if (modeHeight == MeasureSpec.EXACTLY) {
            resultHeight = sizeHeight;
        } else {
            resultHeight = mMinSize;
            if (modeHeight == MeasureSpec.AT_MOST) {
                resultHeight = Math.min(resultHeight, sizeHeight);
            }
        }

        mCenterX = resultWidth / 2;
        mCenterY = resultHeight / 2;
        mRadius = Math.max(resultWidth, resultHeight) / 2;

//        mRadius >>= 1;
        mRadialGradient = new RadialGradient(mCenterX, mCenterY, mRadius / 3 * 2,
                new int[]{Color.TRANSPARENT, Color.TRANSPARENT, DEFAULT_RIPPLE_COLOR}, null, Shader.TileMode.CLAMP);

        mPaint.setShader(mRadialGradient);


        setMeasuredDimension(resultWidth, resultHeight);
    }

    @Override
    public void onDraw(Canvas canvas) {


        for (int i = 0; i < mRippleNum; i++) {
            float progress = (currentProgress + i * 100 / (mRippleNum)) ;
            if (mode == 1)
                progress = 100 - progress;

            float half = mRadius / 2.0f;
            float rate = progress / 100f;
            mPaint.setStrokeWidth(half * rate);

            canvas.drawCircle(mCenterX, mCenterY, (mRadius - half + dp2px(9)) * rate, mPaint);

        }
        super.onDraw(canvas);
    }

    private float dp2px(float dpx) {
        return getResources().getDisplayMetrics().density * dpx + 0.5f;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (isRippleAnimationRunning())
            stopRippleAnimation();
    }

    /**
     * 自定义估值器
     */
    private TypeEvaluator mProgressEvaluator = new TypeEvaluator() {

        @Override
        public Object evaluate(float fraction, Object startValue, Object endValue) {
            fraction = (fraction * mTotalTime / mPeriod) % 100;
            return fraction;
        }
    };

    public void setRippleNum(int mRippleNum) {
        this.mRippleNum = mRippleNum;
    }
}
