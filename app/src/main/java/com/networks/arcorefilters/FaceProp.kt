package com.networks.arcorefilters


import androidx.annotation.DrawableRes
import androidx.annotation.RawRes

data class FaceProp constructor(
    val title: String,
    @DrawableRes val icon: Int,
    val modelId: Int,
    val type: PropType
)

enum class PropType{
    MODEL, TEXTURE
}

