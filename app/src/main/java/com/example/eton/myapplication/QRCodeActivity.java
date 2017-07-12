package com.example.eton.myapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ButtonBarLayout;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.util.Hashtable;

public class QRCodeActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "QRCodeActivity";
    ImageView qrCodeView;
    Button commonBtn;
    Button specialBtn;

    String message;
    int qrCodeWidth;
    int qrCodeHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode);

        qrCodeView = (ImageView) findViewById(R.id.qrCode_imageView);
        commonBtn = (Button) findViewById(R.id.common_btn);
        specialBtn = (Button) findViewById(R.id.special_btn);

        commonBtn.setOnClickListener(this);
        specialBtn.setOnClickListener(this);

        message = "http://www.letmegooglethat.com/?q=%E6%8E%8C%E6%AB%83";
        qrCodeWidth = 500;
        qrCodeHeight = 500;

    }

    /**
     * 生成一般二維碼
     *
     * @param str 二維碼的信息
     * @param w   生成二維碼的寬度
     * @param h   生成二維碼的高度
     * @return 生成的二維碼 bitmap
     * @throws WriterException
     */
    public static Bitmap createQRCode(String str, int w, int h) throws WriterException {
        Hashtable<EncodeHintType, Object> hints = new Hashtable<EncodeHintType, Object>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
        hints.put(EncodeHintType.CHARACTER_SET, "utf-8");

        BitMatrix matrix = new MultiFormatWriter().encode(str, BarcodeFormat.QR_CODE, w, h, hints);
        matrix = deleteWhite(matrix);
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (matrix.get(x, y)) {
                    pixels[y * width + x] = Color.BLACK;
                } else {
                    pixels[y * width + x] = Color.WHITE;
                }
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }

    /**
     * 生成二維碼 中間插入小圖片
     *
     * @param str 内容
     * @return Bitmap
     * @throws WriterException
     */
    public static Bitmap cretaeQRCodeLogo(String str, Bitmap icon, int w, int h, int color) throws WriterException {
        // 缩放一个40*40的圖片
        icon = zoomBitmap(icon, 25);
        Hashtable<EncodeHintType, Object> hints = new Hashtable<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
        hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
//        hints.put(EncodeHintType.MARGIN, 1);
        // 生成二维矩陣,编碼時指定大小,不要生成了圖片以後再進行缩放,這樣會模糊導致識別失敗
        BitMatrix matrix = new MultiFormatWriter().encode(str, BarcodeFormat.QR_CODE, w, h, hints);
        matrix = deleteWhite(matrix);
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        // 二维矩陣轉為一维像素數组,也就是一直横著排了
        int halfW = width / 2;
        int halfH = height / 2;
        int[] pixels = new int[width * height];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (x > halfW - 25 && x < halfW + 25 && y > halfH - 25 && y < halfH + 25) {
                    pixels[y * width + x] = icon.getPixel(x - halfW + 25, y - halfH + 25);
                } else {
                    if (matrix.get(x, y)) {
                        pixels[y * width + x] = color;
                    } else { // 無信息設置像素點為白色
                        pixels[y * width + x] = Color.WHITE;
                    }
                }

            }
        }
        Bitmap mBitmap = Bitmap.createBitmap(width, height,
                Bitmap.Config.ARGB_8888);
        // 通過像素數组生成 bitmap
        mBitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return mBitmap;
    }

    public static BitMatrix deleteWhite(BitMatrix matrix) {
        int[] rec = matrix.getEnclosingRectangle();
        int resWidth = rec[2] + 1;
        int resHeight = rec[3] + 1;

        BitMatrix resMatrix = new BitMatrix(resWidth, resHeight);
        resMatrix.clear();
        for (int i = 0; i < resWidth; i++) {
            for (int j = 0; j < resHeight; j++) {
                if (matrix.get(i + rec[0], j + rec[1]))
                    resMatrix.set(i, j);
            }
        }

        float ratio = resWidth;
        ratio = ratio / matrix.getWidth();
        return resMatrix;
    }

    /**
     * 缩放圖片
     *
     * @param icon
     * @param h
     * @return
     */
    public static Bitmap zoomBitmap(Bitmap icon, int h) {
        // 缩放圖片
        Matrix m = new Matrix();
        float sx = (float) 2 * h / icon.getWidth();
        float sy = (float) 2 * h / icon.getHeight();
        m.setScale(sx, sy);
        // 重新構造一個2h*2h的圖片
        return Bitmap.createBitmap(icon, 0, 0, icon.getWidth(), icon.getHeight(), m, false);
    }

    @Override
    public void onClick(View view) {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_round);

        switch (view.getId()) {
            case R.id.common_btn:
                try {
                    bitmap = createQRCode(message, qrCodeWidth, qrCodeHeight);
                } catch (WriterException e) {
                    e.printStackTrace();
                    Log.e(TAG, "onClick: writer exception", e);
                }

                qrCodeView.setImageBitmap(bitmap);
                break;
            case R.id.special_btn:
                try {
                    bitmap = cretaeQRCodeLogo(message, bitmap, qrCodeWidth, qrCodeHeight, Color.RED);
                } catch (WriterException e) {
                    e.printStackTrace();
                    Log.e(TAG, "onClick: writer exception", e);
                }
                qrCodeView.setImageBitmap(bitmap);
                break;
        }
    }
}
