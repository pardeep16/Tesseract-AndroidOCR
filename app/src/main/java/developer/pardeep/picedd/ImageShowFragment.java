package developer.pardeep.picedd;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ImageShowFragment extends Fragment {

    ImageView imageView;
    TextView textView;
    View view;


    public ImageShowFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
       view=inflater.inflate(R.layout.fragment_image_show, container, false);
        imageView=(ImageView)view.findViewById(R.id.imageViewShowFragment);
        textView=(TextView)view.findViewById(R.id.textViewShowFragment);

        String imagePath=MainActivity.imagePathInGallery;
        String textShow=MainActivity.textOfImage;
        if(imagePath!=null){
            Bitmap bitmap= BitmapFactory.decodeFile(imagePath);
            imageView.setImageBitmap(bitmap);
        }
        if(textShow!=null){
            textView.setText(textShow);
        }

        return view;
    }


}
