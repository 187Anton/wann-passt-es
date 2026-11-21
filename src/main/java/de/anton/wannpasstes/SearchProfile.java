package de.anton.wannpasstes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;


@Entity
@Table(name = "search_profiles")
public class SearchProfile {

    public static final int NAME_MAX_LENGTH = 100;
    public static final int LOCATION_NAME_MAX_LENGTH = 200;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = NAME_MAX_LENGTH)
    @Column(nullable = false, length = NAME_MAX_LENGTH)
    private String name;

    @Min(1)
    @Column(nullable = false)
    private int locationId;

    /**
     * Name of the location at the time of saving. Nullable because profiles
     * saved before this column existed only know the location ID.
     */
    @Size(max = LOCATION_NAME_MAX_LENGTH)
    @Column(length = LOCATION_NAME_MAX_LENGTH)
    private String locationName;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Activity activity;

    @Min(1)
    @Column(nullable = false)
    private int minDurationMinutes;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TemperaturePreference temperaturePreference;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WindPreference windPreference;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PrecipitationPreference precipitationPreference;

    protected SearchProfile() {
    }

    public SearchProfile(
            String name,
            int locationId,
            Activity activity,
            int minDurationMinutes,
            TemperaturePreference temperaturePreference,
            WindPreference windPreference,
            PrecipitationPreference precipitationPreference) {
        this(name, locationId, null, activity, minDurationMinutes,
                temperaturePreference, windPreference, precipitationPreference);
    }

    public SearchProfile(
            String name,
            int locationId,
            String locationName,
            Activity activity,
            int minDurationMinutes,
            TemperaturePreference temperaturePreference,
            WindPreference windPreference,
            PrecipitationPreference precipitationPreference) {
        update(name, locationId, locationName, activity, minDurationMinutes,
                temperaturePreference, windPreference, precipitationPreference);
    }

    public final void update(
            String name,
            int locationId,
            String locationName,
            Activity activity,
            int minDurationMinutes,
            TemperaturePreference temperaturePreference,
            WindPreference windPreference,
            PrecipitationPreference precipitationPreference) {
        this.name = name;
        this.locationId = locationId;
        this.locationName = normalizeLocationName(locationName);
        this.activity = activity;
        this.minDurationMinutes = minDurationMinutes;
        this.temperaturePreference = temperaturePreference;
        this.windPreference = windPreference;
        this.precipitationPreference = precipitationPreference;
    }

    /** Trims a location name, maps blank names to {@code null} and caps the length. */
    static String normalizeLocationName(String locationName) {
        if (locationName == null || locationName.isBlank()) {
            return null;
        }
        String trimmed = locationName.strip();
        return trimmed.length() > LOCATION_NAME_MAX_LENGTH
                ? trimmed.substring(0, LOCATION_NAME_MAX_LENGTH)
                : trimmed;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getLocationId() {
        return locationId;
    }

    public String getLocationName() {
        return locationName;
    }

    public boolean hasLocationName() {
        return locationName != null && !locationName.isBlank();
    }

    /** Location name, or a placeholder for older profiles without a stored name. */
    public String getLocationLabel() {
        return hasLocationName() ? locationName : "Unbekannter Ort (ID " + locationId + ")";
    }

    public Activity getActivity() {
        return activity;
    }

    public int getMinDurationMinutes() {
        return minDurationMinutes;
    }

    public TemperaturePreference getTemperaturePreference() {
        return temperaturePreference;
    }

    public PrecipitationPreference getPrecipitationPreference() {
        return precipitationPreference;
    }

    public WindPreference getWindPreference() {
        return windPreference;
    }
}
