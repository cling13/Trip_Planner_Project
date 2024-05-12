package com.example.plannerproject010;

import static com.example.plannerproject010.MainActivity.context;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SimpleAdapter extends RecyclerView.Adapter<SimpleAdapter.ViewHolder> implements ItemTouchHelperListner {

    private ArrayList<listClass> data = null;
    private  ItemClickListner itemClickListner;

    @Override
    public boolean onItemMove(int from_position, int to_position) {
        listClass tmp = data.get(from_position);
        data.remove(from_position);
        data.add(to_position, tmp);

        notifyItemMoved(from_position, to_position);
        return true;
    }

    @Override
    public void onItemSwipe(int position) {
        MyDBHelper dbHelper=new MyDBHelper(context,"plantable",null,1);
        SQLiteDatabase db=dbHelper.getWritableDatabase();
        String sql="DELETE FROM plantable WHERE id = '"+data.get(position).getId()+"';";
        db.execSQL(sql);
        db.close();
        data.remove(position);
        notifyItemRemoved(position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameText;
        TextView addressText;
        ImageView placeImage;
        Button placeSelectBtn;

        ViewHolder(View itemView) {
            super(itemView);

            addressText=itemView.findViewById(R.id.placeAddress);
            nameText = itemView.findViewById(R.id.placeName);
            placeImage=itemView.findViewById(R.id.placeImage);
            placeSelectBtn=itemView.findViewById(R.id.placeSelectBtn);

            placeSelectBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(itemClickListner!=null)
                    {
                        int position=getAdapterPosition();
                        if(position!=RecyclerView.NO_POSITION)
                        {
                            itemClickListner.onItemBtnClick(position);
                        }
                    }
                }
            });
        }
    }

    SimpleAdapter(ArrayList<listClass>list, ItemClickListner itemClickListner){

        try {
            // 패키지 매니저를 통해 앱의 메타 데이터 가져오기
            ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            // 메타 데이터에서 API 키 가져오기
            String key = appInfo.metaData.getString("com.google.android.geo.API_KEY");


            Places.initialize(context,key);
            // 가져온 API 키 사용하기
            // 예: Google Places API 호출 등
        } catch (PackageManager.NameNotFoundException e) {
            // 메타 데이터를 찾을 수 없는 경우 예외 처리
            e.printStackTrace();
        }

        data=list;
        this.itemClickListner=itemClickListner;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        Context context=parent.getContext();
        LayoutInflater inflater=(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        view = inflater.inflate(R.layout.itemlayout, parent, false);
        SimpleAdapter.ViewHolder vh=new SimpleAdapter.ViewHolder(view);

        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder,int position) {

        listClass text = data.get(position);
        holder.placeImage.setImageBitmap(text.getImage());
        holder.nameText.setText(text.getName());
        holder.addressText.setText(text.getAddress());
        holder.placeSelectBtn.setText(text.getBtnName());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position=holder.getAdapterPosition();

                if(itemClickListner!=null){
                    itemClickListner.onItemClick(position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void addItem(listClass listClass)
    {
        data.add(listClass);
    }

    public void addItemList(String tmp,Context context)
    {
        List<Place.Field> placeFields = Arrays.asList(
                Place.Field.ID,
                Place.Field.NAME,
                Place.Field.ADDRESS,
                Place.Field.LAT_LNG,
                Place.Field.PHOTO_METADATAS // 사진 메타데이터 필드
        );

        FetchPlaceRequest request = FetchPlaceRequest.builder(tmp, placeFields).build();
        PlacesClient placesClient = Places.createClient(context);

        placesClient.fetchPlace(request).addOnSuccessListener((response) -> {
            Place place = response.getPlace();

            // 장소 정보 사용
            LatLng placeLatLng = place.getLatLng();
            String placeName = place.getName();
            String placeAddress = place.getAddress();

            List<PhotoMetadata> photoMetadataList = place.getPhotoMetadatas();

            if (photoMetadataList != null && !photoMetadataList.isEmpty()) {

                //사진 메타데이터 가져오기
                PhotoMetadata photoMetadata = photoMetadataList.get(0);

                //사진 크기 설정
                FetchPhotoRequest photoRequest = FetchPhotoRequest.builder(photoMetadata)
                        .setMaxWidth(500) // Optional.
                        .setMaxHeight(500) // Optional.
                        .build();

                //리스트에 여행지 정보 추가
                placesClient.fetchPhoto(photoRequest).addOnSuccessListener((fetchPhotoResponse) -> {
                    Bitmap bitmap = fetchPhotoResponse.getBitmap();
                    listClass t = new listClass(bitmap, placeName, placeAddress, placeLatLng,"추가",tmp);
                    ((MainActivity) MainActivity.context).addList(t);
                }).addOnFailureListener((exception) -> {
                });
            }

        }).addOnFailureListener((exception) -> {
            Log.e("FetchPlace", "Failed to fetch place: " + exception.getMessage());
            exception.printStackTrace();
        });
    }
}

