package com.example.asus.firebasechatapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.github.curioustechizen.ago.RelativeTimeTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Date;


public class MainActivity extends AppCompatActivity {

    private static final String MESSAGE_CHILD = "message_child" ;
    private FirebaseAuth mFireBaseAuth;
    private FirebaseUser mFireBaseUser;
    private String mUserName;
    private String mPhotourl;
    private RecyclerView recycleView;
    private EditText isiPesan;
    private ImageView btnSend;
    private DatabaseReference mFirebaseDatabaseReference;
    private long mTimestamp;
    private LinearLayoutManager mLinearLayoutManager;
    private FirebaseRecyclerAdapter<ChatModel, MessageViewHolder> mFirebaseAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //TODO 6 : Ambil data dari Auth Gmail
        mFireBaseAuth = FirebaseAuth.getInstance();
        mFireBaseUser = mFireBaseAuth.getCurrentUser();

        if (mFireBaseUser == null){
            Toast.makeText(this, "Login dulu", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, AuthenticationActivity.class));
            finish();
        }else {
            mUserName = mFireBaseUser.getDisplayName();
            mPhotourl = mFireBaseUser.getPhotoUrl().toString();
            Toast.makeText(this, "Anda login sebagai "+mUserName, Toast.LENGTH_SHORT).show();
        }

        //TODO 7 : buat model
        recycleView = (RecyclerView)findViewById(R.id.list_pesan);
        isiPesan = (EditText)findViewById(R.id.edit_text);
        btnSend = (ImageView)findViewById(R.id.btn_send);

        //TODO 8 : kirim data ke database
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = isiPesan.getText().toString();
                mTimestamp = new Date().getTime();
                if (TextUtils.isEmpty(message)){
                    Toast.makeText(MainActivity.this, "Tidak bisa mengirim teks kosong", Toast.LENGTH_SHORT).show();
                }else {
                    ChatModel chatMessage = new ChatModel(message, mUserName, mPhotourl, mTimestamp);
                    mFirebaseDatabaseReference.child(MESSAGE_CHILD).push().setValue(chatMessage, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError != null){
                                Toast.makeText(MainActivity.this, "Gagal terkirim", Toast.LENGTH_SHORT).show();
                            }else {
                                Toast.makeText(MainActivity.this, "Terkirim", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    isiPesan.setText("");
                }
            }
        });

        //TODO 9 : tampil pesan
        recycleView.setHasFixedSize(true);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setStackFromEnd(true);
        mFirebaseAdapter = new FirebaseRecyclerAdapter<ChatModel, MessageViewHolder>(
                ChatModel.class,
                R.layout.item_chat_message,
                MessageViewHolder.class,
                mFirebaseDatabaseReference.child(MESSAGE_CHILD)
        ) {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            protected void populateViewHolder(final MessageViewHolder viewHolder, ChatModel model, int position) {
                if (model.getText() != null) {
                    // set data ke item
                    viewHolder.messageTextView.setText(model.getText());
                    viewHolder.messengerTextView.setText(model.getName());
                    viewHolder.timestamp.setReferenceTime(model.getTimestamp());
                    Glide.with(MainActivity.this)
                            .load(model.getPhotoUrl())
                            .asBitmap()
                            .centerCrop()
                            .error(R.drawable.ic_account_round)
                            .into(new BitmapImageViewTarget(viewHolder.messengerImageView){
                                @Override
                                protected void setResource(Bitmap resource){
                                    RoundedBitmapDrawable rounded = RoundedBitmapDrawableFactory.create(MainActivity.this.getResources(), resource);
                                    rounded.setCircular(true);

                                    viewHolder.messengerImageView.setImageDrawable(rounded);
                                }
                            });

                    String nama = model.getName();
                    if (nama != null && nama.equals(mUserName)){
                        viewHolder.layoutbackground.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.balon_message));
                        viewHolder.viewpucuk.setBackground(ContextCompat.getDrawable(MainActivity.this, R.color.holo_blue_light));
                    }else{
                        viewHolder.layoutbackground.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.balon_message_lawan));
                        viewHolder.viewpucuk.setBackground(ContextCompat.getDrawable(MainActivity.this, R.color.holo_blue_light));
                    }

                } else {
                    Toast.makeText(MainActivity.this, "Data Kosong", Toast.LENGTH_SHORT).show();
                }
            }
        };

        mFirebaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int modelCount = mFirebaseAdapter.getItemCount();
                int lastVisiblePosition = mLinearLayoutManager.findLastVisibleItemPosition();
                if (lastVisiblePosition == -1 || (positionStart >= (modelCount - 1) && lastVisiblePosition == (positionStart - 1))){
                    recycleView.scrollToPosition(positionStart);
                }
            }
        });
        recycleView.setLayoutManager(mLinearLayoutManager);
        recycleView.setAdapter(mFirebaseAdapter);
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        private ImageView messengerImageView;
        private View viewpucuk;
        private TextView messageTextView;
        private TextView messengerTextView;
        private LinearLayout layoutbackground;
        private RelativeTimeTextView timestamp;
        private LinearLayout layoututama;
        public MessageViewHolder(View itemView) {
            super(itemView);
            layoututama = (LinearLayout) itemView.findViewById(R.id.layoututama);
            timestamp = (RelativeTimeTextView) itemView.findViewById(R.id.timestamp);
            layoutbackground = (LinearLayout) itemView.findViewById(R.id.layoutbackground);
            messengerTextView = (TextView) itemView.findViewById(R.id.messengerTextView);
            messageTextView = (TextView) itemView.findViewById(R.id.messageTextView);
            viewpucuk = (View) itemView.findViewById(R.id.viewpucuk);
            messengerImageView = (ImageView) itemView.findViewById(R.id.messengerImageView);
        }
    }
}
