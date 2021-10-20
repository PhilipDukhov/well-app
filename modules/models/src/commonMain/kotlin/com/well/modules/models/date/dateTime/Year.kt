package com.well.modules.models.date.dateTime


public class Year private constructor() {

	public companion object {

		public fun isLeap(year: Int): Boolean =
			year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)
	}
}
