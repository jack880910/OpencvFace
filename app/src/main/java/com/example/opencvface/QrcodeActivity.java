package com.example.opencvface;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.util.Hashtable;


public class QrcodeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode);

        ImageView code_img = findViewById(R.id.code_img);
        TextView tvContent = findViewById(R.id.content);
        BarcodeEncoder encoder = new BarcodeEncoder();
        Bitmap bitmap;
        BitMatrix result;
        MultiFormatWriter writer = new MultiFormatWriter();


            try{
                String content = "姓名：" + User.name + "\n" +
                        "生日：" + User.birthday + "\n" +
                        "疫苗名稱：" + User.vaccine_name + "\n" +
                        "疫苗批次：" + User.vaccine_batchNumber + "\n" +
                        "接種日期：" + User.vaccination_date + "\n" +
                        "施打組織：" + User.vaccination_org;

                Log.d("contentgen", ""+User.name);
                Log.d("contentgen", ""+User.birthday);
                Log.d("contentgen", ""+content);

                Hashtable<EncodeHintType, Object> hints = new Hashtable();
                hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
                result = writer.encode(content, BarcodeFormat.QR_CODE, 800,800, hints);
                bitmap = encoder.createBitmap(result);

                code_img.setImageBitmap(bitmap);
                tvContent.setText(content);

            }catch(WriterException e){
                e.printStackTrace();
            }

    }


}