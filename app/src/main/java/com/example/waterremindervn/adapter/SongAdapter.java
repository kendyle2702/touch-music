package com.example.waterremindervn.adapter;

import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.waterremindervn.R;
import com.example.waterremindervn.model.Song;

public class SongAdapter extends ListAdapter<Song, SongAdapter.SongViewHolder> {

    private final OnSongClickListener listener;
    private final boolean isTop100;

    public interface OnSongClickListener {
        void onSongClick(Song song, int position);
        void onFavoriteClick(Song song, int position);
    }

    public SongAdapter(OnSongClickListener listener, boolean isTop100) {
        super(new SongDiffCallback());
        this.listener = listener;
        this.isTop100 = isTop100;
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_song, parent, false);
        return new SongViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {
        Song song = getItem(position);
        holder.bind(song, position);
    }

    class SongViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvSongTitle;
        private final TextView tvArtist;
        private final ImageView imgSongCover;
        private final ImageButton btnFavorite;
        private final TextView tvRank;

        SongViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSongTitle = itemView.findViewById(R.id.tvSongTitle);
            tvArtist = itemView.findViewById(R.id.tvArtist);
            imgSongCover = itemView.findViewById(R.id.imgSongCover);
            btnFavorite = itemView.findViewById(R.id.btnFavorite);
            tvRank = itemView.findViewById(R.id.tvRank);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onSongClick(getItem(position), position);
                }
            });

            btnFavorite.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onFavoriteClick(getItem(position), position);
                }
            });
        }

        void bind(Song song, int position) {
            tvSongTitle.setText(song.getName());
            tvArtist.setText(song.getSinger());
            
            String imageUrl = song.getImage();
            // Log URL ảnh để debug
            Log.d("SongAdapter", "Loading image: " + imageUrl + " for song: " + song.getName());
            
            // Xóa bỏ bất kỳ tint nào được áp dụng trước đó
            imgSongCover.setColorFilter(null);
            
            // Load song cover image với nhiều tùy chọn hơn và theo dõi chi tiết
            Glide.with(imgSongCover.getContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_music_note) // Giữ placeholder cho trường hợp ảnh chưa tải xong
                    .error(R.drawable.ic_music_note) // Giữ error cho trường hợp tải ảnh lỗi
                    .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.ALL)
                    .skipMemoryCache(false) // Đảm bảo sử dụng memory cache
                    .centerCrop()
                    .timeout(30000) // Tăng timeout lên 30 giây
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            Log.e("SongAdapter", "Lỗi tải ảnh: " + imageUrl + ", lỗi: " + (e != null ? e.getMessage() : "null"));
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            Log.d("SongAdapter", "Tải ảnh thành công: " + imageUrl);
                            return false;
                        }
                    })
                    .into(imgSongCover);
            
            // Set favorite icon
            btnFavorite.setImageResource(song.isFavorite() ? 
                    R.drawable.ic_favorite_filled : 
                    R.drawable.ic_favorite_border);
            
            // Show rank number for Top 100
            if (isTop100) {
                tvRank.setVisibility(View.VISIBLE);
                tvRank.setText(String.valueOf(position + 1));
            } else {
                tvRank.setVisibility(View.GONE);
            }
        }
    }

    static class SongDiffCallback extends DiffUtil.ItemCallback<Song> {
        @Override
        public boolean areItemsTheSame(@NonNull Song oldItem, @NonNull Song newItem) {
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Song oldItem, @NonNull Song newItem) {
            return oldItem.getId().equals(newItem.getId()) &&
                    oldItem.getName().equals(newItem.getName()) &&
                    oldItem.getSinger().equals(newItem.getSinger()) &&
                    oldItem.getImage().equals(newItem.getImage()) &&
                    oldItem.isFavorite() == newItem.isFavorite();
        }
    }
} 