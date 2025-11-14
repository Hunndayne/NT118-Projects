package com.example.enggo; // Phải trùng package với HomeActivity

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ImageSliderAdapter extends RecyclerView.Adapter<ImageSliderAdapter.ImageSliderViewHolder> {

    // Danh sách các ID ảnh (từ R.drawable)
    private List<Integer> imageList;

    // Constructor
    public ImageSliderAdapter(List<Integer> imageList) {
        this.imageList = imageList;
    }

    // Lớp ViewHolder để giữ tham chiếu đến ImageView
    // (Làm static inner class là cách làm tốt nhất trong Java)
    public static class ImageSliderViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public ImageSliderViewHolder(@NonNull View itemView) {
            super(itemView);
            // Ánh xạ ImageView từ layout 'item_slider_image.xml'
            imageView = itemView.findViewById(R.id.imageViewSlider);
        }
    }

    // Tạo ViewHolder mới (gọi layout 'item_slider_image.xml')
    @NonNull
    @Override
    public ImageSliderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_slider_image, parent, false);
        return new ImageSliderViewHolder(view);
    }

    // Gán dữ liệu (ảnh) cho ViewHolder tại vị trí 'position'
    @Override
    public void onBindViewHolder(@NonNull ImageSliderViewHolder holder, int position) {
        // Lấy ID ảnh từ danh sách và gán cho ImageView
        holder.imageView.setImageResource(imageList.get(position));
    }

    // Trả về tổng số lượng ảnh
    @Override
    public int getItemCount() {
        return imageList.size();
    }
}