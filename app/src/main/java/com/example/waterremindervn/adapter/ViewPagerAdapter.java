package com.example.waterremindervn.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.waterremindervn.fragment.MyListFragment;
import com.example.waterremindervn.fragment.SearchFragment;
import com.example.waterremindervn.fragment.Top100Fragment;

import java.util.HashMap;

public class ViewPagerAdapter extends FragmentStateAdapter {

    private static final int TAB_COUNT = 3;
    
    // Fragment positions
    public static final int POSITION_MY_LIST = 0;
    public static final int POSITION_TOP100 = 1;
    public static final int POSITION_SEARCH = 2;
    
    // Lưu trữ các fragment để có thể truy cập chúng sau khi tạo
    private final HashMap<Integer, Fragment> fragments = new HashMap<>();

    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Fragment fragment;
        
        switch (position) {
            case POSITION_MY_LIST:
                fragment = new MyListFragment();
                break;
            case POSITION_TOP100:
                fragment = new Top100Fragment();
                break;
            case POSITION_SEARCH:
                fragment = new SearchFragment();
                break;
            default:
                fragment = new MyListFragment();
                break;
        }
        
        // Lưu fragment vào HashMap để có thể truy cập sau này
        fragments.put(position, fragment);
        return fragment;
    }
    
    // Phương thức lấy fragment dựa vào vị trí
    public Fragment getFragment(int position) {
        return fragments.get(position);
    }

    @Override
    public int getItemCount() {
        return TAB_COUNT;
    }
} 