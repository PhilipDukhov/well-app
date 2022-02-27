package com.well.modules.db.server

import com.well.modules.models.Review

fun Ratings.toReview(): Review = Review(value = value_, text = text)