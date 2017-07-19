package com.example.eton.myapplication;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.ftp.FTPSClient;

import java.io.IOException;
import java.io.PrintWriter;


public class FTPActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "FTPActivity";

    private String host;
    private int type;
    private String userName;
    private String userPwd;
    private int port;

    private EditText hostEdt;
    private EditText portEdt;
    private EditText userEdt;
    private EditText passwordEdt;
    private Spinner typeSpinner;
    private TextView contentTv;

    private FTPClient mFTPClient;
    private StringBuilder result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ftp);

        hostEdt = (EditText) findViewById(R.id.host_edt);
        portEdt = (EditText) findViewById(R.id.port_edt);
        userEdt = (EditText) findViewById(R.id.user_edt);
        passwordEdt = (EditText) findViewById(R.id.password_edt);
        typeSpinner = (Spinner) findViewById(R.id.type_spinner);
        contentTv = (TextView) findViewById(R.id.content_tv);

        ArrayAdapter<CharSequence> typeArrayAdapter = ArrayAdapter.createFromResource(this,
                R.array.type,
                android.R.layout.simple_spinner_dropdown_item);
        typeSpinner.setAdapter(typeArrayAdapter);
        typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                type = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        // 斷開 ftp 連線
        disconnect();
    }

    // Method to connect to FTPS server:
    public void ftpConnect(final String host, final String username, final String password,
                           final int port, final int type) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    switch (type) {
                        case 0: // ftp
                            mFTPClient = new FTPClient();
                            break;
                        case 1: // ftps
                            mFTPClient = new FTPSClient(true);
                            break;
                    }

                    mFTPClient.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out)));

                    // connecting to the host
                    mFTPClient.connect(host);

                    if (type == 1) {
                        // Set data channel protection to private
                        ((FTPSClient) mFTPClient).execPROT("P");
                    }
                    boolean status = false;
                    // now check the reply code, if positive mean connection success
                    if (FTPReply.isPositiveCompletion(mFTPClient.getReplyCode())) {
                        // login using username & password

                        status = mFTPClient.login(username, password);

                        mFTPClient.setFileType(FTP.BINARY_FILE_TYPE);
                        mFTPClient.enterLocalPassiveMode();

                    }

                    if (status) {
                        // 列出目錄資料
                        String[] files = ftpPrintFilesList(null);
                        for (String name : files) {
                            updateResult(name);
                        }
                    } else {
                        updateResult("login failed.");
                    }
                } catch (Exception e) {
                    String message = "Error: could not connect to host " + host;
                    Log.e(TAG, message);
                    updateResult(message);
                }

            }
        }).start();
    }

    private void disconnect() {
        if (mFTPClient == null) {
            return;
        }
        // 斷開 ftp 連線
        try {
            mFTPClient.disconnect();
            updateResult("disconnect.");
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "disconnect: disconnect exception", e);
        }
    }

    // Method to list all files in a directory:
    public String[] ftpPrintFilesList(String dir_path) {
        String[] fileList = null;
        try {

            FTPFile[] ftpFiles = mFTPClient.listFiles(dir_path);
            int length = ftpFiles.length;
            Log.d(TAG, "ftpPrintFilesList: files list length= " + length);
            fileList = new String[length];
            for (int i = 0; i < length; i++) {
                String name = ftpFiles[i].getName();
                boolean isFile = ftpFiles[i].isFile();

                if (isFile) {
                    fileList[i] = "File : " + name;
                    Log.i(TAG, "Ftp File : " + name);
                } else {
                    fileList[i] = "Directory : " + name;
                    Log.i(TAG, "Ftp Directory : " + name);
                }
            }
            return fileList;
        } catch (IOException e) {
            Log.e(TAG, "ftpPrintFilesList: exception ", e);
            e.printStackTrace();
            return fileList;
        }
    }

    private void updateResult(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                result = result.append(message + "\n");
                contentTv.setText(result);
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.connect_btn:
                host = String.valueOf(hostEdt.getText());
                port = Integer.parseInt(String.valueOf(portEdt.getText()));
                userName = String.valueOf(userEdt.getText());
                userPwd = String.valueOf(passwordEdt.getText());
                ftpConnect(host, userName, userPwd, port, type);
                break;
            case R.id.disconnect_btn:
                disconnect();
                break;
        }
    }
}
