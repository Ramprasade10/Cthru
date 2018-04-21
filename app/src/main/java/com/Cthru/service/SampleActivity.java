package com.Cthru.service;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class SampleActivity extends Activity implements OnClickListener {
	private final int RESPONSE_OK = 200;
	TextToSpeech tsp;
	private final int IMAGE_PICKER_REQUEST = 1;
	
	private TextView picNameText;
	private EditText langCodeField;
	private EditText apiKeyFiled;
	
	private String apiKey;
	private String langCode;
	private String fileName;

	@TargetApi(Build.VERSION_CODES.DONUT)
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.main);

		picNameText = (TextView) findViewById(R.id.imageName);
		langCodeField = (EditText) findViewById(R.id.lanuageCode);
		apiKeyFiled = (EditText) findViewById(R.id.apiKey);
//tsp=new TextToSpeech(this,this);
		tsp=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
			@Override
			public void onInit(int status) {
				if(status != TextToSpeech.ERROR) {
					tsp.setLanguage(Locale.UK);
				}
			}
		});
		final Button pickButton = (Button) findViewById(R.id.picImagebutton);
		pickButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Starting image picker activity
				startActivityForResult(new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI), IMAGE_PICKER_REQUEST);
			}
		});

		final Button convertButton = (Button) findViewById(R.id.convert);
		convertButton.setOnClickListener(this);
	}
	
	@Override
	public void onClick(View v) {
		apiKey = apiKeyFiled.getText().toString();
		langCode = langCodeField.getText().toString();
		
		// Checking are all fields set
		if (fileName != null && !apiKey.equals("") && !langCode.equals("")) {
			final ProgressDialog dialog = ProgressDialog.show( SampleActivity.this, "Loading ...", "Converting to text.", true, false);
			final Thread thread = new Thread(new Runnable() {
				@Override
				public void run() {
					final OCRServiceAPI apiClient = new OCRServiceAPI(apiKey);
					apiClient.convertToText(langCode, fileName);
					
					// Doing UI related code in UI thread
					runOnUiThread(new Runnable() {
						@TargetApi(Build.VERSION_CODES.DONUT)
						@Override
						public void run() {
							dialog.dismiss();
tsp.speak(apiClient.getResponseText(),TextToSpeech.QUEUE_FLUSH, null);
/*							final AlertDialog.Builder alert = new AlertDialog.Builder(SampleActivity.this);
							alert.setMessage(apiClient.getResponseText());
							alert.setPositiveButton(
									"OK",
									new DialogInterface.OnClickListener() {
										public void onClick( DialogInterface dialog, int id) {
										}
									});

							// Setting dialog title related from response code
							if (apiClient.getResponseCode() == RESPONSE_OK) {
								alert.setTitle("Success");
							} else {
								alert.setTitle("Faild");
							}

							alert.show();
*/
						}
					});
				}
			});
			thread.start();
		} else {
			Toast.makeText(SampleActivity.this, "All data are required.", Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == IMAGE_PICKER_REQUEST && resultCode == RESULT_OK) {
			fileName = getRealPathFromURI(data.getData());
			picNameText.setText("Selected: en" + getStringNameFromRealPath(fileName));
		}
	}

	/*
	 * Returns image real path.
	 */
	private String getRealPathFromURI(final Uri contentUri) {
		final String[] proj = { MediaStore.Images.Media.DATA };
		final Cursor cursor = managedQuery(contentUri, proj, null, null, null);
		int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();
		
		return cursor.getString(column_index);
	}

	/*
	 * Cuts selected file name from real path to show in screen.
	 */
	private String getStringNameFromRealPath(final String bucketName) {
		return bucketName.lastIndexOf('/') > 0 ? bucketName.substring(bucketName.lastIndexOf('/') + 1) : bucketName;
	}
}