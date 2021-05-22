package com.zectan.soundroid;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.zectan.soundroid.objects.Playlist;
import com.zectan.soundroid.objects.Song;

import java.util.ArrayList;
import java.util.List;

// https://www.glyric.com/2018/merlin/aagaya-nilave

public class MainActivity extends AppCompatActivity {
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Reference views
        bottomNavigationView = findViewById(R.id.bottom_navigator);

        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.nav_host_fragment);
        assert navHostFragment != null;
        NavController navController = navHostFragment.getNavController();
        NavigationUI.setupWithNavController(bottomNavigationView, navController);
    }

    public void showBottomNavigator() {
        bottomNavigationView.setVisibility(View.VISIBLE);
    }

    public void hideBottomNavigator() {
        bottomNavigationView.setVisibility(View.GONE);
    }

    public Playlist getStaticPlaylist() {
        String folder = getFilesDir().getPath();
        List<Song> songs = new ArrayList<>();
        songs.add(new Song(
                folder,
                "lHEOj3d7YS4",
                "All About You",
                "Taeyeon",
                "https://popgasa1.files.wordpress.com/2019/07/3286779.jpg?w=750",
                Color.parseColor("#8c4525")
        ));

        songs.add(new Song(
                folder,
                "Y1fB--J5qWQ",
                "Can You See My Heart",
                "Heize",
                "https://popgasa1.files.wordpress.com/2019/08/3288231.jpg",
                Color.parseColor("#4f5a6c")
        ));

        songs.add(new Song(
                folder,
                "zVzbknPjmoQ",
                "Our Happy Ending",
                "IU",
                "https://i1.sndcdn.com/artworks-KkZXDbcIwzEJ8mVn-m2vcCQ-t500x500.jpg",
                Color.parseColor("#6c5854")
        ));

        songs.add(new Song(
                folder,
                "26PJHfGx9-w",
                "Another Day",
                "Punch",
                "https://popgasa1.files.wordpress.com/2019/07/3285256.jpg",
                Color.parseColor("#4c3d5b")
        ));

        songs.add(new Song(
                folder,
                "dqn89S8pmjI",
                "Done For Me",
                "Punch",
                "https://i1.sndcdn.com/artworks-000592721822-kwf8o4-t500x500.jpg",
                Color.parseColor("#5c2816")
        ));

        songs.add(new Song(
                folder,
                "WgBtHRRZYw4",
                "Lean On Me",
                "10cm",
                "https://colorcodedlyrics.com/wp-content/uploads/2019/09/10cm-Hotel-Del-Luna-OST-Part-2.jpg",
                Color.parseColor("#27293a")
        ));

        songs.add(new Song(
                folder,
                "vyHA9DZOqLo",
                "Only You",
                "Yang Da II",
                "https://popgasa1.files.wordpress.com/2019/08/3288225.jpg",
                Color.parseColor("#2b2d31")
        ));

        songs.add(new Song(
                folder,
                "jHab0yitNlI",
                "So Long",
                "Paul Kim",
                "https://popgasa1.files.wordpress.com/2019/08/3291163.jpg",
                Color.parseColor("#d5c8be")
        ));

        songs.add(new Song(
                folder,
                "pG_I5YaQKPk",
                "Say Goodbye",
                "Ha Yea Song",
                "https://i.scdn.co/image/ab67616d0000b273a685a1c4095cbefdb639e231",
                Color.parseColor("#525d43")
        ));

        songs.add(new Song(
                folder,
                "ISdbASQUn-8",
                "See The Stars",
                "Red Velvet",
                "https://popgasa1.files.wordpress.com/2019/08/3290807.jpg",
                Color.parseColor("#746758")
        ));

        songs.add(new Song(
                folder,
                "oHT_XE3NONo",
                "Can You Hear Me?",
                "Ben",
                "https://popgasa1.files.wordpress.com/2019/08/3290808.jpg",
                Color.parseColor("#1e242e")
        ));

        songs.add(new Song(
                folder,
                "joVz8HZqzVU",
                "Love Deluna",
                "Punch",
                "https://www.allkpop.com/upload/2019/08/content/232056/1566608161-x-ost-part.jpg",
                Color.parseColor("#ba9080")
        ));

        songs.add(new Song(
                folder,
                "xpFBk05QGFI",
                "At The End",
                "Chung Ha",
                "https://www.dramamilk.com/wp-content/uploads/2019/08/Hotel-del-luna-OST-1-5-3.jpg",
                Color.parseColor("#6d4939")
        ));

//        songs.add(new Song(
//                dir,
//                "Df_4SNsQVH4",
//                "Lilac",
//                "IU",
//                "https://www.youtube.com/watch?v=Df_4SNsQVH4",
//                "https://bandwagon-gig-finder.s3.amazonaws.com/system/tinymce/image/file/2156/content_mceu_47980652711616659911799.jpg"
//        ));
//
//        songs.add(new Song(
//                dir,
//                "JSOBF_WhqEM",
//                "Dear Name",
//                "IU",
//                "https://www.youtube.com/watch?v=JSOBF_WhqEM",
//                "https://images.genius.com/8e62c5c1b9e45ff21bb62d1d52a67f6c.608x608x1.jpg"
//        ));
//
//        songs.add(new Song(
//                dir,
//                "B0Ij_eTECXc",
//                "I Love You Boy",
//                "Suzy",
//                "https://www.youtube.com/watch?v=B0Ij_eTECXc",
//                "https://m.media-amazon.com/images/I/81WZBZB3quL._SS500_.jpg"
//        ));
//
//        songs.add(new Song(
//                dir,
//                "TqIAndOnd74",
//                "My Sea",
//                "IU",
//                "https://www.youtube.com/watch?v=TqIAndOnd74",
//                "https://bandwagon-gig-finder.s3.amazonaws.com/system/tinymce/image/file/2156/content_mceu_47980652711616659911799.jpg"
//        ));
//
//        songs.add(new Song(
//                dir,
//                "ZThVobEtp_o",
//                "Celebrity",
//                "IU",
//                "https://www.youtube.com/watch?v=ZThVobEtp_o",
//                "https://static.wikia.nocookie.net/kpop/images/b/bf/IU_Celebrity_album_cover.png/revision/latest?cb=20210127180814"
//        ));
//
//        songs.add(new Song(
//                dir,
//                "dZJaIkqyfSo",
//                "Here I Am Again",
//                "Yarin Baek",
//                "https://www.youtube.com/watch?v=dZJaIkqyfSo",
//                "https://m.media-amazon.com/images/I/716ZpE867eL._SS500_.jpg"
//        ));
//
//        songs.add(new Song(
//                dir,
//                "Oj18EikZMuU",
//                "End Of Time",
//                "Alan Walker",
//                "https://www.youtube.com/watch?v=Oj18EikZMuU",
//                "https://static.wikia.nocookie.net/alan-walker/images/5/53/End_of_Time.jpeg/revision/latest?cb=20200316191815"
//        ));
//
//        songs.add(new Song(
//                dir,
//                "DT61L8hbbJ4",
//                "Mayday",
//                "The Fat Rat",
//                "https://www.youtube.com/watch?v=DT61L8hbbJ4",
//                "https://i1.sndcdn.com/artworks-000320118375-om14o8-t500x500.jpg"
//        ));
//
//        songs.add(new Song(
//                dir,
//                "mSLuJYtl89Y",
//                "Let's Go",
//                "Lensko",
//                "https://www.youtube.com/watch?v=mSLuJYtl89Y",
//                "https://i1.sndcdn.com/artworks-000241547430-87l4kz-t500x500.jpg"
//        ));

        return new Playlist("", "Hotel Del Luna OST", songs);

    }
}