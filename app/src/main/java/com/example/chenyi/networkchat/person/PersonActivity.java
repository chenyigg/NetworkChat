package com.example.chenyi.networkchat.person;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.transition.Fade;
import android.view.KeyEvent;
import android.widget.EditText;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.example.chenyi.networkchat.R;
import com.example.chenyi.networkchat.bean.User;
import com.example.chenyi.networkchat.util.GildeUtil;
import com.example.chenyi.networkchat.util.TimeUtil;
import com.example.chenyi.networkchat.util.ToastUtil;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;

import static android.os.Environment.getExternalStorageDirectory;

/**
 * Created by chenyi on 2017/5/21.
 */

public class PersonActivity extends AppCompatActivity {

    private static final int SELECT_PHOTOS_REQUEST_CODE = 1;
    public static final int REVISABILITY = 0;
    public static final int UNREVISABILITY = -1;

    private boolean change = false;
    private User user;
    private int revisability = 0;

    @BindView(R.id.background)
    ImageView background;
    @BindView(R.id.head_pic)
    CircleImageView pic;
    @BindView(R.id.name)
    EditText name;
    @BindView(R.id.phone)
    EditText phone;
    @BindView(R.id.mail)
    EditText mail;
    @BindView(R.id.exit)
    ImageView exit;
    @OnClick(R.id.exit)
    public void exit() {
        backResult();
    }

    @OnClick(R.id.head_pic)
    public void setPic() {
        if (revisability == UNREVISABILITY)
            return;
        change = true;
        // 调用系统图库获取图片
        Intent intent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, SELECT_PHOTOS_REQUEST_CODE);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person);
        ButterKnife.bind(this);

        setupWindowAnimations();

        Bundle extras = getIntent().getExtras();
        user = extras.getParcelable("user");
        revisability = extras.getInt("type");
        if (revisability == UNREVISABILITY) {
            name.setFocusable(false);
            mail.setFocusable(false);
            phone.setFocusable(false);
        }
        if (user == null)
            user = new User();
        name.setText(user.getName());
        mail.setText(user.getMail());
        phone.setText(user.getPhone());
        GildeUtil.setPicture(pic, user.getPic());
        GildeUtil.setBlurPicture(background, user.getPic());
    }

    private void setupWindowAnimations() {
        Fade enterTransition = new Fade();
        enterTransition.setDuration(200);
        getWindow().setEnterTransition(enterTransition);
        getWindow().setExitTransition(enterTransition);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // 是否触发按键为 back 键，并为 back 键设置监听
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            backResult();
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    private void backResult() {
        // 实例化 Bundle，设置需要传递的参数
        Bundle bundle = new Bundle();

        if (isChange() || change) {
            user.setName(name.getText().toString());
            user.setPhone(phone.getText().toString());
            user.setMail(mail.getText().toString());

            bundle.putParcelable("user", user);
        } else {
            bundle.putParcelable("user", null);
        }
        // 将修改后的用户信息返回给主页面
        setResult(RESULT_OK, this.getIntent().putExtras(bundle));
        finishAfterTransition();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case SELECT_PHOTOS_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    Uri uri = data.getData();
                    if (uri != null) {
                        // 将图片设置到头像中
                        Glide.with(this)
                                .load(uri)
                                .asBitmap()
                                .into(new SimpleTarget<Bitmap>(250, 250) {
                                    @Override
                                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                                        saveHeadPic(resource);
                                    }
                                });

                    } else {
                        Bundle b = data.getExtras();
                        if (b != null) {
                            // 这里是有些拍照后的图片是直接存放到 Bundle 中的，
                            // 所以我们可以从这里面获取Bitmap图片
                            Bitmap image = b.getParcelable("data");
                            if (image != null) {
                                saveHeadPic(image);
                            } else {
                                ToastUtil.showToast("获取图片失败");
                            }
                        }
                    }
                }
                break;
        }
    }

    /**
     * 将获取的图片进行压缩并保存
     */
    public void saveHeadPic(Bitmap bitmap) {
        //将bitmap保存到本地，spath :生成图片取个名字和路径包含类型
        String path = getExternalStorageDirectory()
                .getAbsolutePath() + "/NC/picture/";
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String filePath = path + "me_"+ TimeUtil.getCurrentTime() +".jpg";
        saveImage(bitmap, filePath);
        user.setPic(filePath);
        GildeUtil.setBlurPicture(background, filePath);
        pic.setImageBitmap(bitmap);
    }

    /**
     * bitmap后存到对应路径
     */
    public void saveImage(Bitmap photo, String spath) {
        try {
            BufferedOutputStream bos = new BufferedOutputStream(
                    new FileOutputStream(spath, false));
            photo.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            bos.flush();
            bos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isChange() {
        return textChange(name, user.getName()) ||
                textChange(mail, user.getMail()) ||
                textChange(phone, user.getPhone());
    }

    private boolean textChange(EditText et, String text) {
        return !et.getText().toString().equals(text);
    }

}
