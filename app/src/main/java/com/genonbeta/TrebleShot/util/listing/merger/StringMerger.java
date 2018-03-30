package com.genonbeta.TrebleShot.util.listing.merger;

import android.support.annotation.NonNull;

import com.genonbeta.TrebleShot.util.listing.ComparableMerger;
import com.genonbeta.TrebleShot.util.listing.Merger;

/**
 * created by: Veli
 * date: 29.03.2018 01:44
 */
public class StringMerger extends ComparableMerger<String>
{
	private String mString;

	public StringMerger(String string)
	{
		mString = string;
	}

	@Override
	public boolean equals(Object obj)
	{
		return obj.equals(mString);
	}

	public String getString()
	{
		return mString;
	}

	@Override
	public int compareTo(@NonNull ComparableMerger<String> o)
	{
		if (!(o instanceof StringMerger))
			return -1;

		return ((StringMerger) o).getString().compareTo(getString());
	}
}