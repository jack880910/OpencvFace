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
                String content_display = "身分證號碼：" + User.key + "\n" +
                        "姓名：" + User.name + "\n" +
                        "性別：" + User.birthday + "\n" +
                        "疫苗名稱：" + User.vaccine_name + "\n" +
                        "疫苗批次：" + User.vaccine_batchNumber + "\n" +
                        "接種日期：" + User.vaccination_date + "\n" +
                        "施打組織：" + User.vaccination_org + "\n" +
                        "數位簽章驗證：" + User.isVerify + "\n" +
                        "資料提供者數位簽章：" + User.signature_org;



                String content_gencode = User.key + "&" +
                        User.name + "&" +
                        User.birthday + "&" +
                        User.vaccine_name + "&" +
                        User.vaccine_batchNumber + "&" +
                        User.vaccination_date + "&" +
                        User.vaccination_org + "&" +
                        User.signature_org + "&" +
                        User.signature_user;
                Log.d("QRcode_content:", "" + content_gencode);


//                Log.d("contentgen", ""+User.key);
//                Log.d("contentgen", ""+User.birthday);
//                Log.d("contentgen", ""+User.signature_org);
//                Log.d("contentgen", ""+content);
//                Log.d("contentgen", ""+QR_content);

                Hashtable<EncodeHintType, Object> hints = new Hashtable();
                hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
                result = writer.encode(content_gencode, BarcodeFormat.QR_CODE, 800,800, hints);
                bitmap = encoder.createBitmap(result);

                code_img.setImageBitmap(bitmap);
                tvContent.setText(content_display);

            }catch(WriterException e){
                e.printStackTrace();
            }

    }


}