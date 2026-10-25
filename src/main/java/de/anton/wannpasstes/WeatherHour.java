package de.anton.wannpasstes;

import java.time.OffsetDateTime;

public class WeatherHour {
    private OffsetDateTime time;
    private double temperatureCelsius;
    private double windKmh;
    private double precipitationMm;
    private int weatherCode;

    public WeatherHour(double temperatureCelsius, OffsetDateTime time, double windKmh, double precipitationMm, int weatherCode) {
        this.temperatureCelsius = temperatureCelsius;
        this.time = time;
        this.windKmh = windKmh;
        this.precipitationMm = precipitationMm;
        this.weatherCode = weatherCode;
    }

    public OffsetDateTime getTime() {
        return time;
    }

    public void setTime(OffsetDateTime time) {
        this.time = time;
    }

    public double getTemperatureCelsius() {
        return temperatureCelsius;
    }

    public void setTemperatureCelsius(double temperatureCelsius) {
        this.temperatureCelsius = temperatureCelsius;
    }

    public double getWindKmh() {
        return windKmh;
    }

    public void setWindKmh(double windKmh) {
        this.windKmh = windKmh;
    }

    public double getPrecipitationMm() {
        return precipitationMm;
    }

    public void setPrecipitationMm(double precipitationMm) {
        this.precipitationMm = precipitationMm;
    }

    public int getWeatherCode() {
        return weatherCode;
    }

    public void setWeatherCode(int weatherCode) {
        this.weatherCode = weatherCode;
    }

    public boolean matches(
            double minTemperatureCelsius,
            double maxTemperatureCelsius,
            double maxWindKmh,
            double maxPrecipitationMm) {

        boolean temperatureMatches =
                temperatureCelsius >=minTemperatureCelsius
                    && temperatureCelsius <= maxTemperatureCelsius;

        boolean windMatches = windKmh <= maxWindKmh;
        boolean precipitationMatches = precipitationMm <= maxPrecipitationMm;

        return temperatureMatches && windMatches && precipitationMatches;
    }

}
