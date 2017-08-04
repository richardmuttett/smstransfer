package com.muttett.smstransfer;

import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.Date;

public class MainActivity extends Activity {

  private static final String FILENAME = "messages.txt";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    Button button = (Button) findViewById(R.id.export);
    button.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        try {
          exportMessages();
        } catch (Exception ex) {
          // No op
        }
      }
    });
  }

  private void exportMessages() throws Exception {
    File file = new File(Environment.getExternalStorageDirectory().getPath() + FILENAME);

    if (file.createNewFile()) {
      FileOutputStream outputStream = new FileOutputStream(file);
      OutputStreamWriter writer = new OutputStreamWriter(outputStream);
      Uri messageUri = Uri.parse("content://sms");
      ContentResolver cr = getContentResolver();
      Cursor cursor = cr.query(messageUri, null, null, null, null);

      if (cursor != null) {
        int totalSMS = cursor.getCount();

        if (cursor.moveToFirst()) {
          int columns = cursor.getColumnCount();

          StringBuilder stringBuilder = new StringBuilder();

          for (int j = 0; j < columns; j++) {
            stringBuilder.append(cursor.getColumnName(j));

            if (j < columns - 1) {
              stringBuilder.append("\t");
            }
          }

          stringBuilder.append("\n");
          writer.append(stringBuilder);

          for (int i = 0; i < totalSMS; i++) {
            stringBuilder = new StringBuilder();

            for (int j = 0; j < columns; j++) {
              int type = cursor.getType(j);

              switch (type) {
                case Cursor.FIELD_TYPE_STRING:
                  String value = cursor.getString(j);
                  stringBuilder.append(value.replaceAll("\n", ""));
                  break;
                case Cursor.FIELD_TYPE_INTEGER:
                  if (cursor.getColumnName(j).contains("date") && cursor.getLong(j) > 0) {
                    Date date = new Date(cursor.getLong(j));
                    stringBuilder.append(date.toLocaleString());
                  } else {
                    stringBuilder.append(cursor.getInt(j));
                  }

                  break;
              }

              if (j < columns - 1) {
                stringBuilder.append("\t");
              }
            }

            stringBuilder.append("\n");
            writer.append(stringBuilder);

            cursor.moveToNext();
          }
        }

        cursor.close();
      }

      writer.close();
      outputStream.close();
    }
  }
}
