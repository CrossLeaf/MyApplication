package com.example.eton.myapplication;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MyAdapter myAdapter = new MyAdapter(getData());
        RecyclerView mainRV = (RecyclerView) findViewById(R.id.main_recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mainRV.setLayoutManager(layoutManager);
        mainRV.setAdapter(myAdapter);

        myAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Intent intent;
                switch (position) {
                    case 0:
                        intent = new Intent(getApplicationContext(), QRCodeActivity.class);
                        startActivity(intent);
                        break;
                }
            }
        });
    }

    public List<String> getData() {
        List<String> dataList = new ArrayList<>();
        dataList.add("QR code 測試");
        return dataList;
    }

    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> implements View.OnClickListener {

        private OnItemClickListener mOnItemClickListener = null;
        private List<String> mDataList;

        public MyAdapter(List<String> dataList) {
            this.mDataList = dataList;
        }

        @Override
        public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_main, parent, false);
            v.setOnClickListener(this);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(MyAdapter.ViewHolder holder, int position) {
            holder.mTextView.setText(mDataList.get(position));
            holder.itemView.setTag(position);
        }

        @Override
        public int getItemCount() {
            return mDataList.size();
        }

        @Override
        public void onClick(View v) {
            if (mOnItemClickListener != null) {
                //注意这里使用getTag方法获取position
                mOnItemClickListener.onItemClick(v, (int) v.getTag());
            }
        }

        public void setOnItemClickListener(OnItemClickListener listener) {
            this.mOnItemClickListener = listener;
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            public TextView mTextView;

            public ViewHolder(View itemView) {
                super(itemView);
                mTextView = (TextView) itemView.findViewById(R.id.info_text);
            }
        }
    }

    public static interface OnItemClickListener {
        void onItemClick(View view, int position);
    }
}
