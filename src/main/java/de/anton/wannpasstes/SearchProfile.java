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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 300, unique = true)
    private String nameKey;

    @Column(length = 255)
    private String locationName;

    @Min(1)
    @Column(nullable = false)
    private int locationId;

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
        this.name = name;
        this.locationId = locationId;
        this.activity = activity;
        this.minDurationMinutes = minDurationMinutes;
        this.temperaturePreference = temperaturePreference;
        this.windPreference = windPreference;
        this.precipitationPreference = precipitationPreference;
    }

    void update(String name, String nameKey, String locationName, ProfileForm form) {
        assignName(name, nameKey);
        this.locationName = locationName;
        locationId = form.locationId();
        activity = form.activity();
        minDurationMinutes = form.minDurationMinutes();
        temperaturePreference = form.temperaturePreference();
        windPreference = form.windPreference();
        precipitationPreference = form.precipitationPreference();
    }

    void assignName(String name, String nameKey) {
        this.name = name;
        this.nameKey = nameKey;
    }

    String nameKey() {
        return nameKey;
    }

    public String getLocationName() {
        return locationName;
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
