package com.example.shlokpatel.picovo;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {
    RecyclerView recyclerView;
    List<UserPost> list;
    FirebaseFirestore firebaseFirestore;
    PostAdapter postAdapter;
    DocumentSnapshot lastVisible;
    Boolean isFirstPageFirstLoad = true;
    FirebaseAuth auth;
    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.fragment_home, container, false);
        recyclerView = view.findViewById(R.id.post_list_view);
        recyclerView.setNestedScrollingEnabled(false);
        firebaseFirestore = FirebaseFirestore.getInstance();
        auth=FirebaseAuth.getInstance();
        list = new ArrayList<>();
        postAdapter = new PostAdapter(list, getActivity(), firebaseFirestore,auth);
        recyclerView.setLayoutManager(new LinearLayoutManager(container.getContext()));
        recyclerView.setAdapter(postAdapter);
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    Boolean isReachedBottom = !recyclerView.canScrollVertically(-1);
                    if (isReachedBottom) {
                        Toast.makeText(container.getContext(), "Reached " + lastVisible.getString("desc"), Toast.LENGTH_SHORT).show();
                        loadMorePost();
                    }
                }
            });
            Query orderQuery = firebaseFirestore.collection("Posts")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .limit(3);
            orderQuery.addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                    if (!documentSnapshots.isEmpty()) {

                        if (isFirstPageFirstLoad) {
                            lastVisible = documentSnapshots.getDocuments().get(documentSnapshots.size() - 1);
                        }

                        for (DocumentChange documentChange : documentSnapshots.getDocumentChanges()) {
                            if (documentChange.getType() == DocumentChange.Type.ADDED) {
                                String postId=documentChange.getDocument().getId();
                                UserPost userPost = documentChange.getDocument().toObject(UserPost.class).withId(postId);
                                if (isFirstPageFirstLoad) {
                                    list.add(userPost);
                                }
                                else{
                                    list.add(0,userPost);
                                }
                                postAdapter.notifyDataSetChanged();
                            }
                        }
                        isFirstPageFirstLoad = false;
                    }
                }
            });
        }
        // Inflate the layout for this fragment
        return view;
    }

    public void loadMorePost() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            Query orderQuery = firebaseFirestore.collection("Posts")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .startAfter(lastVisible)
                    .limit(3);
            orderQuery.addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                    if (!documentSnapshots.isEmpty()) {

                        lastVisible = documentSnapshots.getDocuments().get(documentSnapshots.size() - 1);

                        for (DocumentChange documentChange : documentSnapshots.getDocumentChanges()) {
                            if (documentChange.getType() == DocumentChange.Type.ADDED) {
                                String postId=documentChange.getDocument().getId();
                                UserPost userPost = documentChange.getDocument().toObject(UserPost.class).withId(postId);
                                list.add(userPost);
                                postAdapter.notifyDataSetChanged();
                            }
                        }
                    }
                }
            });
        }
    }
}
