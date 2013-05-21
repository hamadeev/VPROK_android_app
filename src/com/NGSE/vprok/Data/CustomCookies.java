package com.NGSE.vprok.Data;

import android.os.Parcel;
import android.os.Parcelable;

public class CustomCookies implements Parcelable
{
	String name;
	String value;

	public CustomCookies(String name, String value)
	{
		this.name = name;
		this.value = value;
	}
	
	public CustomCookies()
	{
		name = "";
		value = "";
	}


	@Override
	public int describeContents()
	{
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags)
	{
		dest.writeString(name);
		dest.writeString(value);
	}
	
	private CustomCookies(Parcel in)
	{
		name=in.readString();
		value=in.readString();
	}
	
	public void setName(String name)
	{
		this.name = name;
	}

	public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public CustomCookies createFromParcel(Parcel in) {
            return new CustomCookies(in);
        }

        public CustomCookies[] newArray(int size) {
            return new CustomCookies[size];
        }
    };
    
	public String getName()
	{
		return name;
	}

	public void setValue(String value)
	{
		this.value = value;
	}
	
	public String getValue()
	{
		return value;
	}

}
