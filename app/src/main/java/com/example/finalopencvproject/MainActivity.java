package com.example.finalopencvproject;


import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import android.support.v7.app.AppCompatActivity;

import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;

public class MainActivity extends AppCompatActivity {

    /**
     * “选择图片”时的标识位
     */
    private static final int CHOOSE_PHOTO = 1;

    /**
     * 显示图片位置的标识位
     */
    private int DISPLAY_IMAGE = 1;

    /**
     * 图片拼接成功的标识位
     */
    public final static int OK = 0;

    /**
     * 需要更多图片进行拼接的标识位
     */
    public final static int ERR_NEED_MORE_IMGS = 1;

    /**
     * 图片不符合拼接标准的标识位
     */
    public final static int ERR_HOMOGRAPHY_EST_FAIL = 2;

    /**
     * 图片参数处理失败的标识位
     */
    public final static int ERR_CAMERA_PARAMS_ADJUST_FAIL = 3;

    /**
     * “选择图片1”的按钮实例
     */
    private Button mBtnSelect;

    private Button mMerge;

    /**
     * 显示图像1的实例
     */
    private ImageView mImageView;


    /**
     * 存储待拼接的图像集合
     */
    private String[] mImagePath = new String[]{};

    /**
     * 存储待拼接的图像集合的索引
     */
    private static int i = 0;

    // 引用native方法
    static {
        System.loadLibrary("native-lib");
    }

    private TextView showPath_tv;
    private ProgressBar progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 初始化布局
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 初始化控件实例
        mBtnSelect = findViewById(R.id.btn_select);
        progress = findViewById(R.id.progress);

        mMerge = findViewById(R.id.btn_merge);
        mImageView = findViewById(R.id.imageView);
        showPath_tv = findViewById(R.id.showPath_tv);


        // “选择图片1”的点击方法
        mBtnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                } else {
                    DISPLAY_IMAGE = 1;
                    openAlbum();
//                    selectedAudioFile();
                }
            }
        });

        // “拼接图像”的点击事件
        mMerge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progress.setVisibility(View.VISIBLE);
                Executors.newCachedThreadPool().execute(new Runnable() {
                    @Override
                    public void run() {
                        mergeBitmap(mImagePath, new onStitchResultListener() {
                            @Override
                            public void onSuccess(Bitmap bitmap) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        progress.setVisibility(View.GONE);
                                        Toast.makeText(MainActivity.this, "图片拼接成功！", Toast.LENGTH_LONG).show();
                                        replaceImage(bitmap);
                                    }
                                });

                            }

                            @Override
                            public void onError(String errorMsg) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        progress.setVisibility(View.GONE);
                                        Toast.makeText(MainActivity.this, "图片拼接失败！--" + errorMsg, Toast.LENGTH_LONG).show();
                                        System.out.println(errorMsg);
                                    }
                                });

                            }
                        });
                    }
                });

            }
        });

        // 调用Native程序的示例
        /*
        TextView tv = findViewById(R.id.sample_text);
        tv.setText(stringFromJNI());
         */
    }

    private static final int LOCALEMED_RETURN_CODE = 0x559;//资质证书 图片选择返回Code

    public void selectedAudioFile() {
        PictureSelectorFormPhoto(this,
                PictureMimeType.ofImage(), 100, false,
                PictureConfig.MULTIPLE, selectList, LOCALEMED_RETURN_CODE);
    }

    /**
     * 图片
     *
     * @param activity
     * @param mode
     * @param maxSelectNum
     * @param selectedMode
     * @param list
     */
    public static void PictureSelectorFormPhoto(AppCompatActivity activity, int mode, int maxSelectNum,
                                                boolean enableCrop, int selectedMode, List<LocalMedia> list, int resuestCode) {
        // 进入相册 以下是例子：不需要的api可以不写
        PictureSelector.create(activity)
                .openGallery(mode)// 全部.PictureMimeType.ofAll()、图片.ofImage()、视频.ofVideo()、音频.ofAudio()
                .theme(R.style.picture_default_style)// 主题样式设置 具体参考 values/styles   用法：R.style.picture.white.style
                .maxSelectNum(maxSelectNum)// 最大图片选择数量
                .minSelectNum(1)// 最小选择数量
                .imageSpanCount(4)// 每行显示个数
                .selectionMode(selectedMode)// 多选 or 单选
                .previewImage(true)// 是否可预览图片
                .previewVideo(true)// 是否可预览视频
                .enablePreviewAudio(true) // 是否可播放音频
                .isCamera(true)// 是否显示拍照按钮
                .isZoomAnim(true)// 图片列表点击 缩放效果 默认true
                .imageFormat(PictureMimeType.PNG)// 拍照保存图片格式后缀,默认jpeg
                .enableCrop(false)// 是否裁剪
                .compress(false)// 是否压缩
                .synOrAsy(true)//同步true或异步false 压缩 默认同步
                //.compressSavePath(getPath())//压缩图片保存地址
                //.sizeMultiplier(0.5f)// glide 加载图片大小 0~1之间 如设置 .glideOverride()无效
                .hideBottomControls(true)// 是否显示uCrop工具栏，默认不显示
                .isGif(false)// 是否显示gif图片
                .showCropFrame(false)// 是否显示裁剪矩形边框 圆形裁剪时建议设为false
                .showCropGrid(false)// 是否显示裁剪矩形网格 圆形裁剪时建议设为false
                .openClickSound(false)// 是否开启点击声音
                .selectionMedia(list)// 是否传入已选图片
                .isDragFrame(true)// 是否可拖动裁剪框(固定)
//                        .videoMaxSecond(15)
//                        .videoMinSecond(10)
                .previewEggs(true)// 预览图片时 是否增强左右滑动图片体验(图片滑动一半即可看到上一张是否选中)
                //.cropCompressQuality(90)// 裁剪压缩质量 默认100
                .minimumCompressSize(150)// 小于100kb的图片不压缩
//                .cropWH()// 裁剪宽高比，设置如果大于图片本身宽高则无效
                .rotateEnabled(true) // 裁剪是否可旋转图片
                .scaleEnabled(true)// 裁剪是否可放大缩小图片
                // .recordVideoSecond()//录制视频秒数 默认60s
                .forResult(resuestCode);//结果回调onActivityResult code
    }

    /**
     * 动态申请权限的处理方法
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openAlbum();
                } else {
                    Toast.makeText(this, "拒绝授权将无法使用程序", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
                break;
        }
    }

    /**
     * 打开相册
     */
    private void openAlbum() {
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("application/vnd.google-earth.kml+xml");
        startActivityForResult(intent, CHOOSE_PHOTO);
    }

    /* OpenCV的测试方法
    private Bitmap hivePic() {
        Log.e(TAG, "hivePic: HHHA:0====>");
        Mat des = new Mat();
        Mat src = new Mat();
        Bitmap srcBit = BitmapFactory.decodeResource(getResources(), R.drawable.test2);
        Utils.bitmapToMat(srcBit, src);
        Bitmap grayBit = Bitmap.createBitmap(src.cols(), src.rows(), Bitmap.Config.ARGB_8888);
        Imgproc.cvtColor(src, des, Imgproc.COLOR_BGR2GRAY);
        Utils.matToBitmap(des, grayBit);
        return grayBit;
    }
     */
    private List<LocalMedia> selectList = new ArrayList<>();
    private List<String> selectPathLists = new ArrayList<>();

    /**
     * Activity的回调处理
     *
     * @param requestCode 请求参数
     * @param resultCode  结果参数
     * @param data        数据
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case CHOOSE_PHOTO:
                if (resultCode == RESULT_OK) {
//                    if (Build.VERSION.SDK_INT >= 19) {
//                        handleImageOnKitKat(data); /* 4.4及以上系统使用这个方法处理图片 */
//                    } else {
//                        handleImageBeforeKitKat(data); /* 4.4及以下系统使用这个方法处理图片 */
//                    }
                    String path = SDFileSelecteUtil.getFilePath(getContext(), data);
                    showPath_tv.setText(path + "");
                }
                break;
            case LOCALEMED_RETURN_CODE:
                selectList = PictureSelector.obtainMultipleResult(data);
                if (selectList.size() > 0) {
                    selectPathLists.clear();
                    for (int j = 0; j < selectList.size(); j++) {
                        LocalMedia localMedia = selectList.get(j);
                        if (localMedia.isCompressed()) {
                            selectPathLists.add(localMedia.getCompressPath());
                        } else {
                            selectPathLists.add(localMedia.getPath());
                        }
                    }
                    mImagePath = new String[selectPathLists.size()];
                    mImagePath = selectPathLists.toArray(mImagePath);
                    showPath_tv.setText(selectPathLists.get(0));
                } else {
                    selectList.clear();
                    selectPathLists.clear();
                    showPath_tv.setText("");
                }

                break;
            default:
                break;
        }
    }


    private String getRealPathFromURI(Uri contentURI) {
        String result;
        Cursor cursor = getContentResolver().query(contentURI, null, null, null, null);
        //不能直接调用contentprovider的接口函数，需要使用contentresolver对象，通过URI间接调用contentprovider
        if (cursor == null) {
            // Source is Dropbox or other similar local file path
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }

    public String UriToFilePath(Activity activity, String url) {
        Uri uri3 = Uri.parse(url);
        String[] proj = {MediaStore.MediaColumns.DATA};
        Cursor actualimagecursor = activity.managedQuery(uri3, proj, null, null, null);
        int actual_image_column_index = actualimagecursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        actualimagecursor.moveToFirst();
        return actualimagecursor.getString(actual_image_column_index);
    }

    /**
     * 4.4及以上系统处理图片的方法
     *
     * @param data 数据
     */
    @TargetApi(19)
    private void handleImageOnKitKat(Intent data) {
        String imagePath = null;
        Uri uri = data.getData();
        if (DocumentsContract.isDocumentUri(this, uri)) {
            String docId = DocumentsContract.getDocumentId(uri);
            if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                String id = docId.split(":")[1];
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
            } else if ("com.android.provideres.downloads.documents".equals(uri.getAuthority())) {
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(docId));
                imagePath = getImagePath(contentUri, null);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            imagePath = getImagePath(uri, null);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            imagePath = uri.getPath();
        }
        showPath_tv.setText(imagePath + "");
//        displayImage(imagePath);
    }

    /**
     * 4.4以下系统处理图片的方法
     *
     * @param data 数据
     */
    private void handleImageBeforeKitKat(Intent data) {
        Uri uri = data.getData();
        String imagePath = getImagePath(uri, null);
        showPath_tv.setText(imagePath + "");
        // displayImage(imagePath);
    }

    /**
     * 获取图片路径
     *
     * @param uri       图片Url
     * @param selection 默认为空值
     * @return
     */
    private String getImagePath(Uri uri, String selection) {
        String path = null;
        Cursor cursor = getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        mImagePath[i++] = path;
        return path;
    }

    /**
     * 图片显示的方法
     *
     * @param imagePath 图片路径
     */
    private void displayImage(String imagePath) {
        if (imagePath != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            if (DISPLAY_IMAGE == 1) {
                mImageView.setImageBitmap(bitmap);
            }
        } else {
            Toast.makeText(this, "获取图片失败", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 拼接图片的方法
     *
     * @param paths    图像URL的集合
     * @param listener 监听器回调
     * @return
     */
    private void mergeBitmap(String paths[], @NonNull onStitchResultListener listener) {
        for (String path : paths) {
            if (!new File(path).exists()) {
                listener.onError("无法读取文件或文件不存在:" + path);
                return;
            }
        }
        int wh[] = stitchImages(paths);
        switch (wh[0]) {
            case OK: {
                Bitmap bitmap = Bitmap.createBitmap(wh[1], wh[2], Bitmap.Config.ARGB_8888);
                int result = getBitmap(bitmap);
                if (result == OK && bitmap != null) {
                    listener.onSuccess(bitmap);
                } else {
                    listener.onError("图片合成失败");
                }
            }
            break;
            case ERR_NEED_MORE_IMGS: {
                listener.onError("需要更多图片");
                return;
            }
            case ERR_HOMOGRAPHY_EST_FAIL: {
                listener.onError("图片对应不上");
                return;
            }
            case ERR_CAMERA_PARAMS_ADJUST_FAIL: {
                listener.onError("图片参数处理失败");
                return;
            }
        }
    }


    public Context getContext() {
        return this;
    }

    /**
     * 拼接监听回调的接口
     */
    public interface onStitchResultListener {

        void onSuccess(Bitmap bitmap);

        void onError(String errorMsg);
    }

    /**
     * 替换图片的方法
     *
     * @param bitmap 拼接后的图像
     */
    private void replaceImage(Bitmap bitmap) {
        mImageView.setImageBitmap(bitmap);
    }

    /**
     * 调用底层的JNI方法（示例）
     *
     * @return
     */
    // public native String stringFromJNI();
    private native static int[] stitchImages(String path[]);

    private native static void getMat(long mat);

    private native static int getBitmap(Bitmap bitmap);
}
