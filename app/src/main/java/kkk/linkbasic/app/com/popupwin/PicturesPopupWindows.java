package kkk.linkbasic.app.com.popupwin;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Nicholas.Huang
 * @Declaration: 图片弹出popupwindow
 * @Email: kurode@sina.cn
 * <p>
 * 2017/3/21 17:31
 **/
public class PicturesPopupWindows extends PopupWindow implements View.OnClickListener {

    private final int TAKE_PHOTO = 10001; //拍照
    private final int LOCAL_PHOTO = 10002; //本地选择照片
    private static final String CAMERA_PATH = Environment
            .getExternalStorageDirectory()
            + File.separator
            + "DCIM"
            + File.separator + "Camera" + File.separator;

    private View take_photo, local_photo_pick, btn_photo_cancel;
    private View mMenuView;
    //    private ImageView ivSamplePhoto;
    private Activity mActivity;
    private File saveFile;
    private int outputX = 0;
    private int outputY = 0;
    private int aspectX = 1;
    private int aspectY = 1;

    private OnSelectPhotoListener onSelectPhotoListener;


    /**
     * 图片选择器
     *
     * @param context 需要在Activity的onActivityResult 中回调图片选择器的 {@link #onActivityResult(int, int, Intent)}
     * @param outputW 需要图片的宽
     * @param outputH 需要图片的宽
     * @param aspectX 图片裁剪比例X
     * @param aspectY 图片裁剪比例Y
     */
    public PicturesPopupWindows(Activity context, int outputW, int outputH, int aspectX, int aspectY, OnSelectPhotoListener onSelectPhotoListener) {
        super(context);
        this.mActivity = context;
        this.outputX = outputW;
        this.outputY = outputH;
        this.aspectX = aspectX;
        this.aspectY = aspectY;
        this.onSelectPhotoListener = onSelectPhotoListener;

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mMenuView = inflater.inflate(R.layout.photo_popup_layout, null);
        take_photo = mMenuView.findViewById(R.id.btn_photo_upload);
        local_photo_pick = mMenuView.findViewById(R.id.btn_photo_pick);
        btn_photo_cancel = mMenuView.findViewById(R.id.btn_photo_cancel);

        local_photo_pick.setOnClickListener(this);
        take_photo.setOnClickListener(this);
        btn_photo_cancel.setOnClickListener(this);
        this.setContentView(mMenuView);
        this.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        this.setHeight(ViewGroup.LayoutParams.MATCH_PARENT);

        this.setFocusable(true);

        this.setAnimationStyle(R.style.animBottom);
        ColorDrawable dw = new ColorDrawable(0x90000000);
        this.setBackgroundDrawable(dw);
    }


    /**
     * 关闭加载过程中的等待对话框
     */
    public void closeLoadingDialog() {

    }


    /**
     * 显示加载过程中的等待对话框
     *
     * @param message      加载对话框提示的内容
     * @param isCancelable 是否允许取消对话框
     */
    public void showLoadingDialog(String message, boolean isCancelable) {
    }

    private class LoadImageTask extends AsyncTask<Uri, Void, String> {
        private String filePath;

        public LoadImageTask(String filePath) {
            this.filePath = filePath;
        }

        @Override
        protected void onPreExecute() {
//            showLoadingDialog("正在加载图片......", false);
        }

        @Override
        protected String doInBackground(Uri... params) {
            try {
                if (TextUtils.isEmpty(filePath)) {
                    String[] proj = {MediaStore.Images.Media.DATA};
                    Cursor cursor = mActivity.getContentResolver().query(params[0], proj,
                            null, null, null);
                    int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    cursor.moveToFirst();
                    filePath = cursor.getString(column_index);
                }
                return saveImage(filePath);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            closeLoadingDialog();
            if (result == null) {
                Toast.makeText(mActivity, "图片加载失败", Toast.LENGTH_SHORT).show();
                return;
            }
            if (onSelectPhotoListener != null)
                onSelectPhotoListener.onSucceed(result);
        }

        private String getSDCaredPath() {
            return Environment.getExternalStorageDirectory().getPath();
        }

        // 保存图片
        private String saveImage(String imgPath) {
            if (imgPath == null)
                return null;
            FileOutputStream fos = null;
            try {
                ImageFactory imageFactory = new ImageFactory();
                Bitmap cropBitmap = BitmapFactory.decodeFile(imgPath);
                if (outputX > 0 && outputY > 0) {
                    cropBitmap = imageFactory.ratio(cropBitmap, outputX, outputY);
                }

                String path = CAMERA_PATH + getPhotoFileName();
                File file = new File(getSDCaredPath() + File.separator + "linkbasic");
                if (!file.exists()) {
                    file.mkdir();
                }
                file = new File(getSDCaredPath() + File.separator + "linkbasic" + File.separator + "CropCache");
                if (!file.exists()) {
                    file.mkdir();
                }
                File imgFile = new File(path);
                if (imgFile.isFile() && imgFile.exists()) {
                    imgFile.delete();
                }
                imgFile.createNewFile();
                fos = new FileOutputStream(imgFile);

                cropBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.flush();
                return path;
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK)
            return;
        switch (requestCode) {
            case TAKE_PHOTO: // 拍摄照片
                if (saveFile != null && saveFile.exists()) {
                    Uri fileUri = Uri.fromFile(saveFile);
                    new LoadImageTask(fileUri.getPath()).execute();
                }
                break;
            case LOCAL_PHOTO:// 选取本地图片
                if (data == null)
                    return;
                // 获取资源路径
                Uri uri = data.getData();
                new LoadImageTask(null).execute(uri);
                break;
        }
    }


    @Override
    public void onClick(View v) {
        dismiss();
        switch (v.getId()) {
            case R.id.btn_photo_cancel:
                break;

            case R.id.btn_photo_upload://拍照
                Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                getSaveFile(getPhotoFileName());
                Uri fileUri = PermissionHelper.getOutputUri(saveFile.getAbsolutePath());
                intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
                mActivity.startActivityForResult(intent, TAKE_PHOTO);
                break;

            case R.id.btn_photo_pick://相册
                Intent localIntent = new Intent();
                localIntent.setAction(Intent.ACTION_PICK);
                localIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                mActivity.startActivityForResult(localIntent, LOCAL_PHOTO);
                break;
        }
    }

    /**
     * 设定相片名
     *
     * @return
     */

    public String getPhotoFileName() {
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
        return "TMS_" + dateFormat.format(date) + ".jpg";
    }

    /**
     * 保存相片
     *
     * @param photoFileName
     * @return
     */
    private File getSaveFile(String photoFileName) {
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            saveFile = new File(CAMERA_PATH, photoFileName);
        }
        return saveFile;
    }

    public interface OnSelectPhotoListener {
        void onSucceed(String path);
    }

}
