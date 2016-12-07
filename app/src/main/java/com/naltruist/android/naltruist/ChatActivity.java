package com.naltruist.android.naltruist;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Activity;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.analytics.FirebaseAnalytics;

import com.bumptech.glide.Glide;
import com.firebase.geofire.util.Constants;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import de.hdodenhof.circleimageview.CircleImageView;



public class ChatActivity extends AppCompatActivity {

    public TextView messageTextView;
    public TextView messengerTextView;
    public CircleImageView messengerImageView;
    public EditText mMessageEditText;
    public Button mSendButton;
    private RecyclerView mMessageRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private StaggeredGridLayoutManager mStagGridLayoutManager;
    private ProgressBar mProgressBar;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private String mUsername;
    private String mPhotoUrl;
    private FirebaseAnalytics mFirebaseAnalytics;
    private SharedPreferences mSharedPreferences;
    public static final String MESSAGES_CHILD = "helpmessages";
    public static final String ANONYMOUS = "anonymous";
    private static final String MESSAGE_SENT_EVENT = "message_sent";
    private FirebaseRecyclerAdapter<NalMessage, MessageViewHolder> mFirebaseAdapter;

    private DatabaseReference mFirebaseDatabaseReference;

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        public TextView messageTextView;
        public TextView messengerTextView;
        public CircleImageView messengerImageView;


        public MessageViewHolder(View v) {
            super(v);
            messageTextView = (TextView) itemView.findViewById(R.id.messageTextView);
            messengerTextView = (TextView) itemView.findViewById(R.id.messengerTextView);
            messengerImageView = (CircleImageView) itemView.findViewById(R.id.messengerImageView);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);


        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        //mUsername = mFirebaseUser.getDisplayName();
        //mPhotoUrl = "";

        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mMessageRecyclerView = (RecyclerView) findViewById(R.id.messageRecyclerView);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setStackFromEnd(true);
        //mStagGridLayoutManager = new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL);

        mMessageEditText = (EditText) findViewById(R.id.messageEditText);
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();

        if (mFirebaseUser == null) {
            // Not signed in, launch the Sign In activity
            startActivity(new Intent(this, SignInActivity.class));
            finish();
            return;
        } else {
            mUsername = mFirebaseUser.getDisplayName();

//            mPhotoUrl = mFirebaseUser.getPhotoUrl().toString();
        }
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        Bundle extras = getIntent().getExtras();
        final String roomId = extras.getString("roomId");
        Toast.makeText(this, "this is the " + roomId, Toast.LENGTH_SHORT).show();

        mFirebaseAdapter = new FirebaseRecyclerAdapter<NalMessage, MessageViewHolder>(

                NalMessage.class, R.layout.message, MessageViewHolder.class, mFirebaseDatabaseReference.child(MESSAGES_CHILD + "/" + roomId)) {

            @Override
            protected void populateViewHolder(MessageViewHolder viewHolder, NalMessage nalMessage, int position) {
                mProgressBar.setVisibility(ProgressBar.INVISIBLE);

                viewHolder.messageTextView.setText(nalMessage.getText());
                viewHolder.messengerTextView.setText(nalMessage.getName());
                if (nalMessage.getPhotoUrl() == null) {
                    viewHolder.messengerImageView.setImageDrawable(ContextCompat.getDrawable(ChatActivity.this,
                            R.drawable.ic_account_circle_black_36dp));
                } else {
                    Glide.with(ChatActivity.this)
                            .load(nalMessage.getPhotoUrl())
                            .into(viewHolder.messengerImageView);
                }
            }
        };


        mMessageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0) {
                    mSendButton.setEnabled(true);
                } else {
                    mSendButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        mSendButton = (Button) findViewById(R.id.sendButton);
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mUsername == null) {
                    mUsername = "Anonymous";
                }

                NalMessage nalMessage = new NalMessage(mMessageEditText.getText().toString(), mUsername,
                        mPhotoUrl);
                mFirebaseDatabaseReference.child(MESSAGES_CHILD + "/" + roomId).push().setValue(nalMessage);
                mMessageEditText.setText("");
                mFirebaseAnalytics.logEvent(MESSAGE_SENT_EVENT, null);
            }
        });


        mFirebaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int friendlyMessageCount = mFirebaseAdapter.getItemCount();
                int lastVisiblePosition = mLinearLayoutManager.findLastCompletelyVisibleItemPosition();
                // If the recycler view is initially being loaded or the user is at the bottom of the list, scroll
                // to the bottom of the list to show the newly added message.
                if (lastVisiblePosition == -1 ||
                        (positionStart >= (friendlyMessageCount - 1) && lastVisiblePosition == (positionStart - 1))) {
                    mMessageRecyclerView.scrollToPosition(positionStart);
                }
            }
        });

        mMessageRecyclerView.setLayoutManager(mLinearLayoutManager);

        RecyclerView.ItemDecoration mItemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        mMessageRecyclerView.addItemDecoration(mItemDecoration);
        mMessageRecyclerView.setAdapter(mFirebaseAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStart() {

        super.onStart();
    }

    @Override
    protected void onStop() {

        super.onStop();
    }
}