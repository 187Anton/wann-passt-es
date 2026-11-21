package de.anton.wannpasstes;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

record ProfileForm(
        String profileName,
        @NotNull @Min(1) Integer locationId,
        @NotNull Activity activity,
        @NotNull @Min(1) Integer minDurationMinutes,
        @NotNull TemperaturePreference temperaturePreference,
        @NotNull WindPreference windPreference,
        @NotNull PrecipitationPreference precipitationPreference) {

    static ProfileForm from(SearchProfile profile) {
        return new ProfileForm(profile.getName(), profile.getLocationId(), profile.getActivity(),
                profile.getMinDurationMinutes(), profile.getTemperaturePreference(),
                profile.getWindPreference(), profile.getPrecipitationPreference());
    }

    String trimmedName() {
        return profileName == null ? "" : profileName.strip();
    }
}
