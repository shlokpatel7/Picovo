package com.example.shlokpatel.picovo;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {
    List<UserPost> userPostList;
    Context context;
    FirebaseFirestore firebaseFirestore;
    FirebaseAuth firebaseAuth;

    public PostAdapter(List<UserPost> userPostList, Context context, FirebaseFirestore firebaseFirestore, FirebaseAuth firebaseAuth) {
        this.userPostList = userPostList;
        this.context = context;
        this.firebaseFirestore = firebaseFirestore;
        this.firebaseAuth = firebaseAuth;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.post_list_item, parent, false);
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final PostViewHolder holder, int position) {
        holder.setIsRecyclable(false);
        final String userId = firebaseAuth.getCurrentUser().getUid();
        final String userPostId = userPostList.get(position).UserPostId;
        final UserPost current = userPostList.get(position);
        holder.userDesc.setText(current.getDesc());
        //Date
        long millisecond = current.getTimestamp().getTime();
        String dateString = DateFormat.format("dd/MM/yyyy", new Date(millisecond)).toString();
        holder.postDate.setText("" + dateString);
        Glide.with(context).load(current.getImage_url())
                .thumbnail(Glide.with(context).load(current.getThumb()))
                .into(holder.postImage);
        firebaseFirestore.collection("Users").document(current.getUser_id())
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            String userId = task.getResult().getString("name");
                            String imageUrl = task.getResult().getString("image");
                            holder.userName.setText(userId);
                            Glide.with(context).load(imageUrl).into(holder.userPic);
                        } else {
                            Toast.makeText(context,
                                    "" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        firebaseFirestore.collection("Posts/"+userPostId+"/Likes").document(userId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                if(documentSnapshot.exists()){
                    holder.likeBtn.setImageDrawable(context.getDrawable(R.mipmap.liked_btn));
                }
                else{
                    holder.likeBtn.setImageDrawable(context.getDrawable(R.mipmap.like_btn));
                }
            }
        });
        firebaseFirestore.collection("Posts/"+userPostId+"/Likes").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                if(!documentSnapshots.isEmpty()){
                    int count=documentSnapshots.size();
                    holder.likesCount.setText(""+count);
                }else{
                    holder.likesCount.setText("0");
                }
            }
        });
        holder.likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseFirestore.collection("Posts/" + userPostId + "/Likes").document(userId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(!task.getResult().exists()){
                            Map<String,Object> likeMap=new HashMap<>();
                            likeMap.put("timestamp",FieldValue.serverTimestamp());
                            firebaseFirestore.collection("Posts/"+userPostId+"/Likes").document(userId).set(likeMap);
                        }
                        else {
                            firebaseFirestore.collection("Posts/"+userPostId+"/Likes").document(userId).delete();
                        }
                    }
                });

            }
        });
    }

    @Override
    public int getItemCount() {
        return userPostList.size();
    }

    public class PostViewHolder extends RecyclerView.ViewHolder {
        TextView userName, userDesc, postDate;
        CircleImageView userPic;
        ImageView likeBtn;
        TextView likesCount;
        ImageView postImage;

        public PostViewHolder(View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.user_name);
            userDesc = itemView.findViewById(R.id.post_desc);
            postDate = itemView.findViewById(R.id.date_post);
            userPic = itemView.findViewById(R.id.user_pic);
            postImage = itemView.findViewById(R.id.post_image);
            likeBtn = itemView.findViewById(R.id.like_button);
            likesCount = itemView.findViewById(R.id.likes_count);
        }
    }
}
