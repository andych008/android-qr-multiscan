package com.smartahc.android.coreqr;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.LuminanceSource;
import com.google.zxing.NotFoundException;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.multi.qrcode.QRCodeMultiReader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;


public class ZXingScannerView extends BarcodeScannerView {
    private static final String TAG = "ZXingScannerView";
    public static final List<BarcodeFormat> ALL_FORMATS = new ArrayList();
    private List<BarcodeFormat> mFormats;
    private ZXingScannerView.ResultHandler mResultHandler;
    // QrCodeMultiReader : 仅支持二维码扫描，可扫描多个二维码，返回多个结果；
    private QRCodeMultiReader qrCodeMultiReader;

    private EnumMap hints;

    private Handler mainHandler = new Handler(Looper.getMainLooper());



    private ZXingScannerView.ResultHandler internalResultHandler = new ResultHandler() {

        private ArrayList<String> results = new ArrayList<>();
        private long lastTime;
        @Override
        public void handleResult(Result... results) {
            Log.v("result len:", ""+results.length);
            if (results.length > 0) {
                for (int i = 0; i < results.length; i++) {
                    Result result = results[i];
                    printLog(result.getText());
                }
                //
                long currentTimeMillis = System.currentTimeMillis();
                long offTime = currentTimeMillis - lastTime;

                if (results.length >= 4) {
//                    stopCameraPreview();

                    mResultHandler.handleResult(results);
                } else if (offTime < 16000) {
                    if (this.results.size() == results.length) {
                        lastTime = currentTimeMillis;
                        if (offTime < 1000) {
                            this.results.clear();
                            resumeCameraPreview(this);
                        } else {

                            mResultHandler.handleResult(results);
                        }
                    } else {
                        this.results.clear();
                        resumeCameraPreview(this);
                    }
                }
            }
        }

        @Override
        public void onItemSelect(Result result) {

        }

        private void printLog(String result) {
            if (!results.contains(result)) {
                Log.v("new result :", result);
                results.add(result);
            }
        }
    };
    public ZXingScannerView(Context context) {
        super(context);
        this.initQRMultiFormatReader();
    }


    public ZXingScannerView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.initQRMultiFormatReader();
    }

    public void setFormats(List<BarcodeFormat> formats) {
        this.mFormats = formats;
        this.initQRMultiFormatReader();
    }

    public void setResultHandler(ZXingScannerView.ResultHandler resultHandler) {
        this.mResultHandler = resultHandler;
    }

    public Collection<BarcodeFormat> getFormats() {
        return this.mFormats == null ? ALL_FORMATS : this.mFormats;
    }

    /**
     * 初始化 qr format reader
     */
    private void initQRMultiFormatReader() {
        mViewFinderView.setOnItemSelectListener(new ViewFinderView.OnItemSelectListener() {
            @Override
            public void onItemSelect(Result result) {
                final Result finalRawResult = result;
                mainHandler.post(new Runnable() {
                    public void run() {
                        ResultHandler tmpResultHandler = ZXingScannerView.this.mResultHandler;
//                        ZXingScannerView.this.mResultHandler = null;
                        ZXingScannerView.this.stopCameraPreview();
                        if (tmpResultHandler != null) {
                            tmpResultHandler.onItemSelect(finalRawResult);
                        }
                    }
                });


            }
        });
        hints = new EnumMap(DecodeHintType.class);
        hints.put(DecodeHintType.POSSIBLE_FORMATS, this.getFormats());
        this.qrCodeMultiReader = new QRCodeMultiReader();
    }


    /**
     * 解析数据
     *
     * @param data   data
     * @param camera 相机
     */
    public void onPreviewFrame(byte[] data, Camera camera) {
        if (this.mResultHandler != null) {
            parseQrMultiFormatReader(data, camera);
        }
    }


    /**
     * 解析多个结果
     */
    private void parseQrMultiFormatReader(byte[] data, Camera camera) {

        try {
            Parameters e = camera.getParameters();
            Size size = e.getPreviewSize();
            int width = size.width;
            int height = size.height;
            if (DisplayUtils.getScreenOrientation(this.getContext()) == 1) {
                int rawResult = this.getRotationCount();
                if (rawResult == 1 || rawResult == 3) {
                    int tmp = width;
                    width = height;
                    height = tmp;
                }

                data = this.getRotatedData(data, camera);
            }

            Result[] rawResult = getRawResult(data, width, height);

            if (rawResult != null && rawResult.length > 0) {

                ResultPoint[] resultPoints = rawResult[0].getResultPoints();
                Log.d(TAG, "parseQrMultiFormatReader() called with: getResultPoints = [" + Arrays.toString(resultPoints) + "], getNumBits = [" + rawResult[0].getNumBits() + "]");
                mViewFinderView.setRawResult(rawResult);
                final Result[] finalRawResult = rawResult;
                mainHandler.post(new Runnable() {
                    public void run() {
                        ZXingScannerView.this.stopCameraPreview();
                        if (internalResultHandler != null) {
                            internalResultHandler.handleResult(finalRawResult);
                        }

                    }
                });
            } else {
                camera.setOneShotPreviewCallback(this);
            }
        } catch (RuntimeException var33) {
            Log.e("ZXingScannerView", var33.toString(), var33);
        }
    }

    /**
     * 重新扫描
     */
    public void resumeCameraPreview(ZXingScannerView.ResultHandler resultHandler) {
        this.mResultHandler = resultHandler;
        super.resumeCameraPreview();
    }

    private Result[] getRawResult(byte[] data, int width, int height) {

        Result[] rawResult1 = null;
        PlanarYUVLuminanceSource source1 = this.buildLuminanceSource(data, width, height);
        if (source1 != null) {
            BinaryBitmap finalRawResult = new BinaryBitmap(new HybridBinarizer(source1));
            try {
                rawResult1 = this.qrCodeMultiReader.decodeMultiple(finalRawResult, hints);

            } catch (ReaderException | NullPointerException | ArrayIndexOutOfBoundsException ignored) {
                ;
            } finally {
                this.qrCodeMultiReader.reset();
            }

            if (rawResult1 == null) {
                LuminanceSource handler = source1.invert();
                finalRawResult = new BinaryBitmap(new HybridBinarizer(handler));

                try {
                    rawResult1 = this.qrCodeMultiReader.decodeMultiple(finalRawResult, hints);
                } catch (NotFoundException ignored) {
                    ;
                } finally {
                    this.qrCodeMultiReader.reset();
                }
            }

            mViewFinderView.setResultScale((float) finalRawResult.getWidth()/getWidth());
//            Log.d(TAG, "parseQrMultiFormatReader() called with: finalRawResult.getWidth() = [" +finalRawResult.getWidth()
//                    + "], finalRawResult.getHeight() = [" + finalRawResult.getHeight() + "]");

        }

        return rawResult1;
    }

    private static Bitmap createThumbnail(PlanarYUVLuminanceSource source) {
        int[] pixels = source.renderThumbnail();
        int width = source.getThumbnailWidth();
        int height = source.getThumbnailHeight();
        Bitmap bitmap = Bitmap.createBitmap(pixels, 0, width, width, height, Bitmap.Config.ARGB_8888);
//        ByteArrayOutputStream out = new ByteArrayOutputStream();
//        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, out);
//        bundle.putByteArray(DecodeThread.BARCODE_BITMAP, out.toByteArray());
//        bundle.putFloat(DecodeThread.BARCODE_SCALED_FACTOR, (float) width / source.getWidth());

        return bitmap;
    }

    /**
     * PlanarYUVLuminanceSource
     */
    private PlanarYUVLuminanceSource buildLuminanceSource(byte[] data, int width, int height) {
        Rect rect = this.getFramingRectInPreview(width, height);
        if (rect == null) {
            return null;
        } else {
            PlanarYUVLuminanceSource source = null;

            try {
                source = new PlanarYUVLuminanceSource(data, width, height, 0, 0, width, height, false);
            } catch (Exception var7) {
                ;
            }

            return source;
        }
    }
    /**
     * PlanarYUVLuminanceSource
     */
    private PlanarYUVLuminanceSource buildLuminanceSourceBak(byte[] data, int width, int height) {
        Rect rect = this.getFramingRectInPreview(width, height);
        if (rect == null) {
            return null;
        } else {
            PlanarYUVLuminanceSource source = null;

            try {
                source = new PlanarYUVLuminanceSource(data, width, height, rect.left, rect.top, rect.width(), rect.height(), false);
            } catch (Exception var7) {
                ;
            }

            return source;
        }
    }

    static {
        ALL_FORMATS.add(BarcodeFormat.QR_CODE);
    }

    public interface ResultHandler {

        void handleResult(Result... results);
        void onItemSelect(Result result);

    }

}