package global.cloudzoromobile;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class Settings extends PreferenceActivity 
{
	@SuppressWarnings("deprecation")
	public void onCreate(Bundle b)
	{
		super.onCreate(b);
		addPreferencesFromResource(R.xml.settings);
	}

}
