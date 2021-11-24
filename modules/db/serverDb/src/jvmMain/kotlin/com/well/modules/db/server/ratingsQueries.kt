package com.well.modules.db.server

import com.well.modules.models.Rating

fun Ratings.toRating(): Rating = Rating(value = value_, text = text)