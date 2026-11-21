package de.anton.wannpasstes;

/**
 * Form backing object for editing a search profile. All fields are nullable
 * so that incomplete or invalid input can be shown again with error messages.
 */
public class ProfileForm {

    private String profileName;
    private Integer locationId;
    private String locationName;
    private Activity activity;
    private Integer minDurationMinutes;
    private TemperaturePreference temperaturePreference;
    private WindPreference windPreference;
    private PrecipitationPreference precipitationPreference;

    public static ProfileForm of(SearchProfile profile) {
        ProfileForm form = new ProfileForm();
        form.setProfileName(profile.getName());
        form.setLocationId(profile.getLocationId());
        form.setLocationName(profile.getLocationName());
        form.setActivity(profile.getActivity());
        form.setMinDurationMinutes(profile.getMinDurationMinutes());
        form.setTemperaturePreference(profile.getTemperaturePreference());
        form.setWindPreference(profile.getWindPreference());
        form.setPrecipitationPreference(profile.getPrecipitationPreference());
        return form;
    }

    public boolean hasLocationName() {
        return locationName != null && !locationName.isBlank();
    }

    /** Location name, or a placeholder if the name is unknown. */
    public String getLocationLabel() {
        if (hasLocationName()) {
            return locationName;
        }
        return locationId == null ? "Kein Ort gewählt" : "Unbekannter Ort (ID " + locationId + ")";
    }

    public String getProfileName() {
        return profileName;
    }

    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }

    public Integer getLocationId() {
        return locationId;
    }

    public void setLocationId(Integer locationId) {
        this.locationId = locationId;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public Integer getMinDurationMinutes() {
        return minDurationMinutes;
    }

    public void setMinDurationMinutes(Integer minDurationMinutes) {
        this.minDurationMinutes = minDurationMinutes;
    }

    public TemperaturePreference getTemperaturePreference() {
        return temperaturePreference;
    }

    public void setTemperaturePreference(TemperaturePreference temperaturePreference) {
        this.temperaturePreference = temperaturePreference;
    }

    public WindPreference getWindPreference() {
        return windPreference;
    }

    public void setWindPreference(WindPreference windPreference) {
        this.windPreference = windPreference;
    }

    public PrecipitationPreference getPrecipitationPreference() {
        return precipitationPreference;
    }

    public void setPrecipitationPreference(PrecipitationPreference precipitationPreference) {
        this.precipitationPreference = precipitationPreference;
    }
}
