package com.birdytalk.seedco.domain.units

/**
 * The three weight-display systems the app can switch between globally.
 *
 * @property label Short label shown in the persistent unit toggle.
 */
enum class UnitSystem(val label: String) {
    /** e.g. `14.33 lb`. */
    DECIMAL_POUNDS("lb"),

    /** e.g. `14 lb 5.3 oz`. */
    POUNDS_OUNCES("lb + oz"),

    /** e.g. `6.50 kg`. */
    KILOGRAMS("kg"),
}
