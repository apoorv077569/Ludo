package com.playzelo.ludomodule.utils;

import android.view.View;

import com.playzelo.ludomodule.databinding.LudoLayoutBinding;

public class TokenPathMapper {

    private final LudoLayoutBinding binding;

    public TokenPathMapper(LudoLayoutBinding binding) {
        this.binding = binding;
    }

    public View[] getBluePath() {
        return new View[]{
                binding.pathRight17, binding.pathRight16, binding.pathRight15, binding.pathRight14, binding.pathRight13,
                binding.pathBottom3, binding.pathBottom6, binding.pathBottom9, binding.pathBottom12, binding.pathBottom15,
                binding.pathBottom18, binding.pathBottom17, binding.pathBottom16, binding.pathBottom13, binding.pathBottom10,
                binding.pathBottom7, binding.pathBottom4, binding.pathBottom1, binding.pathLeft18, binding.pathLeft17,
                binding.pathLeft16, binding.pathLeft15, binding.pathLeft14, binding.pathLeft13, binding.pathLeft7,
                binding.pathLeft1, binding.pathLeft2, binding.pathLeft3, binding.pathLeft4,
                binding.pathLeft5, binding.pathLeft6, binding.pathTop16, binding.pathTop13, binding.pathTop10,
                binding.pathTop7, binding.pathTop4, binding.pathTop1, binding.pathTop2, binding.pathTop3,
                binding.pathTop6, binding.pathTop9, binding.pathTop12, binding.pathTop15, binding.pathTop18,
                binding.pathRight1, binding.pathRight2, binding.pathRight3, binding.pathRight4,
                binding.pathRight5, binding.pathRight6, binding.pathRight12, binding.pathRight11,
                binding.pathRight10, binding.pathRight9, binding.pathRight8, binding.pathRight7,
                binding.center
        };
    }
    public View[] getRedPath() {
        return new View[]{
                binding.pathTop6, binding.pathTop9, binding.pathTop12, binding.pathTop15, binding.pathTop18,
                binding.pathRight1, binding.pathRight2, binding.pathRight3, binding.pathRight4, binding.pathRight5,
                binding.pathRight6, binding.pathRight12, binding.pathRight18, binding.pathRight17, binding.pathRight16,
                binding.pathRight15, binding.pathRight14, binding.pathRight13, binding.pathBottom3, binding.pathBottom6,
                binding.pathBottom9, binding.pathBottom12, binding.pathBottom15, binding.pathBottom18, binding.pathBottom17,
                binding.pathBottom16, binding.pathBottom13, binding.pathBottom10, binding.pathBottom7,
                binding.pathBottom4, binding.pathBottom1, binding.pathLeft18, binding.pathLeft17,
                binding.pathLeft16, binding.pathLeft15, binding.pathLeft14, binding.pathLeft13, binding.pathLeft7,
                binding.pathLeft1, binding.pathLeft2, binding.pathLeft3, binding.pathLeft4, binding.pathLeft5,
                binding.pathLeft6, binding.pathTop16, binding.pathTop13, binding.pathTop10, binding.pathTop7,
                binding.pathTop4, binding.pathTop1, binding.pathTop2, binding.pathTop5, binding.pathTop8, binding.pathTop11,
                binding.pathTop14, binding.pathTop17, binding.center
        };
    }

    public View[] getGreenPath() {
        return new View[]{
                binding.pathLeft2, binding.pathLeft3, binding.pathLeft4, binding.pathLeft5, binding.pathLeft6,
                binding.pathTop16, binding.pathTop13, binding.pathTop10, binding.pathTop7, binding.pathTop4, binding.pathTop1,
                binding.pathTop2, binding.pathTop3, binding.pathTop6, binding.pathTop9, binding.pathTop12, binding.pathTop15,
                binding.pathTop18, binding.pathRight1, binding.pathRight2, binding.pathRight3,
                binding.pathRight4, binding.pathRight5, binding.pathRight6, binding.pathRight12, binding.pathRight18,
                binding.pathRight17, binding.pathRight16, binding.pathRight15, binding.pathRight14, binding.pathRight13,
                binding.pathBottom3, binding.pathBottom6, binding.pathBottom9, binding.pathBottom12,
                binding.pathBottom15, binding.pathBottom18, binding.pathBottom17, binding.pathBottom16,
                binding.pathBottom13, binding.pathBottom10, binding.pathBottom7, binding.pathBottom4, binding.pathBottom1,
                binding.pathLeft18, binding.pathLeft17, binding.pathLeft16, binding.pathLeft15,
                binding.pathLeft14, binding.pathLeft13, binding.pathLeft7, binding.pathLeft8, binding.pathLeft9,
                binding.pathLeft10, binding.pathLeft11, binding.pathLeft12, binding.center
        };
    }

    public View[] getYellowPath() {
        return new View[]{
                binding.pathBottom13, binding.pathBottom10, binding.pathBottom7, binding.pathBottom4, binding.pathBottom1,
                binding.pathLeft18, binding.pathLeft17, binding.pathLeft16, binding.pathLeft15, binding.pathLeft14, binding.pathLeft13,
                binding.pathLeft7, binding.pathLeft1, binding.pathLeft2, binding.pathLeft3, binding.pathLeft4, binding.pathLeft5, binding.pathLeft6,
                binding.pathTop16, binding.pathTop13, binding.pathTop10, binding.pathTop7, binding.pathTop4, binding.pathTop1, binding.pathTop2,
                binding.pathTop3, binding.pathTop6, binding.pathTop9, binding.pathTop12, binding.pathTop15, binding.pathTop18,
                binding.pathRight1, binding.pathRight2, binding.pathRight3, binding.pathRight4, binding.pathRight5, binding.pathRight6,
                binding.pathRight12, binding.pathRight18, binding.pathRight17, binding.pathRight16, binding.pathRight15, binding.pathRight14,
                binding.pathRight13, binding.pathBottom3, binding.pathBottom6, binding.pathBottom9, binding.pathBottom12, binding.pathBottom15,
                binding.pathBottom18, binding.pathBottom17, binding.pathBottom14, binding.pathBottom11, binding.pathBottom8, binding.pathBottom5,
                binding.pathBottom2, binding.center
        };
    }



    // You can add similar methods for Red, Green, Yellow paths
}
