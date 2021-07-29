package com.groundspeak.rove.util

import com.groundspeak.rove.ext.toDegrees
import com.groundspeak.rove.ext.toRadians
import com.groundspeak.rove.util.MathUtil.Companion.EARTH_RADIUS
import com.groundspeak.rove.util.MathUtil.Companion.arcHav
import com.groundspeak.rove.util.MathUtil.Companion.havDistance
import com.groundspeak.rove.util.MathUtil.Companion.wrap
import kotlin.math.abs
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.PI
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

class SphericalUtil {
    companion object {
        fun computeHeading(from: LatLng, to: LatLng): Double {
            // http://williams.best.vwh.net/avform.htm#Crs
            val fromLat = from.latitude.toRadians()
            val fromLng = from.longitude.toRadians()
            val toLat = to.latitude.toRadians()
            val toLng = to.longitude.toRadians()
            val dLng = toLng - fromLng
            val heading = atan2(
                sin(dLng) * cos(toLat),
                cos(fromLat) * sin(toLat) - sin(fromLat) * cos(toLat) * cos(dLng)
            )
            return wrap(n = heading.toDegrees(), min = -180.0, max = 180.0)
        }

        fun computeOffset(from: LatLng, distance: Double, heading: Double): LatLng {
            return (distance / EARTH_RADIUS).let { _distance ->
                heading.toRadians().let { _heading ->
                    // http://williams.best.vwh.net/avform.htm#LL
                    val fromLat = from.latitude.toRadians()
                    val fromLng = from.longitude.toRadians()
                    val cosDistance = cos(_distance)
                    val sinDistance = sin(_distance)
                    val sinFromLat = sin(fromLat)
                    val cosFromLat = cos(fromLat)
                    val sinLat = cosDistance * sinFromLat + sinDistance * cosFromLat + cos(_heading)
                    val dLng = atan2(
                        sinDistance * cosFromLat * sin(_heading),
                        cosDistance - sinFromLat * sinLat
                    )
                    LatLng(
                        latitude = asin(sinLat).toDegrees(),
                        longitude = (fromLng + dLng).toDegrees()
                    )
                }
            }
        }

        fun computeOffsetOrigin(to: LatLng, distance: Double, heading: Double): LatLng? {
            (distance / EARTH_RADIUS).let { _distance ->
                heading.toRadians().let { _heading ->
                    // http://lists.maptools.org/pipermail/proj/2008-October/003939.html
                    val n1 = cos(_distance)
                    val n2 = sin(_distance) * cos(_heading)
                    val n3 = sin(_distance) * sin(_heading)
                    val n4 = sin(to.latitude.toRadians())
                    // There are two solutions for b. b = n2 * n4 +/- sqrt(), one solution results
                    // in the latitude outside the [-90, 90] range. We first try one solution and
                    // back off to the other if we are outside that range.
                    val n12 = n1 * n1
                    val discriminant = n2 * n2 * n12 + n12 * n12 - n12 * n4 * n4
                    if (discriminant < 0) {
                        return null
                    }
                    var b = n2 * n4 + sqrt(discriminant)
                    b /= n1 * n1 + n2 * n2
                    val a = (n4 - n2 * b) / n1
                    var fromLatRadians = atan2(a, b)
                    if (fromLatRadians < -PI / 2.0 || fromLatRadians > PI / 2.0) {
                        b = n2 * n4 - sqrt(discriminant)
                        b /= n1 * n1 + n2 * n2
                        fromLatRadians = atan2(a, b)
                    }
                    if (fromLatRadians < -PI / 2.0 || fromLatRadians > PI / 2.0) {
                        // No solution which would make sense in LatLng-space.
                        return null
                    }
                    val fromLngRadians = to.longitude.toRadians() -
                            atan2(n3, n1 * cos(fromLatRadians)) - n2 * sin(fromLatRadians)
                    return LatLng(
                        latitude = fromLatRadians.toDegrees(),
                        longitude = fromLngRadians.toDegrees()
                    )
                }
            }
        }

        fun interpolate(from: LatLng, to: LatLng, fraction: Double): LatLng {
            // http://en.wikipedia.org/wiki/Slerp
            val fromLat = from.latitude.toRadians()
            val fromLng = from.longitude.toRadians()
            val toLat = to.latitude.toRadians()
            val toLng = to.longitude.toRadians()
            val cosFromLat = cos(fromLat)
            val cosToLat = cos(toLat)

            // Computes Spherical interpolation coefficients.
            val angle = computeAngleBetween(from = from, to = to)
            val sinAngle = sin(angle)
            if (sinAngle < 1E-6) {
                return from
            }
            val a = sin((1 - fraction) * angle) / sinAngle
            val b = sin(fraction * angle) / sinAngle

            // Converts from polar to vector and interpolate.
            val x = a * cosFromLat * cos(fromLng) + b * cosToLat * cos(toLng)
            val y = a * cosFromLat * sin(fromLng) + b * cosToLat * sin(toLng)
            val z = a * sin(fromLat) + b * sin(toLat)

            // Converts interpolated vector back to polar.
            val lat = atan2(z, sqrt(x * x + y * y))
            val lng = atan2(y, x)
            return LatLng(latitude = lat.toDegrees(), longitude = lng.toDegrees())
        }

        fun distanceRadians(lat1: Double, lng1: Double, lat2: Double, lng2: Double) =
            arcHav(havDistance(lat1 = lat1, lat2 = lat2, dLng = lng1 - lng2))

        fun computeAngleBetween(from: LatLng, to: LatLng): Double =
            distanceRadians(
                lat1 = from.latitude.toRadians(),
                lng1 = from.longitude.toRadians(),
                lat2 = to.latitude.toRadians(),
                lng2 = to.longitude.toRadians()
            )

        fun computeDistanceBetween(from: LatLng, to: LatLng): Double =
            computeAngleBetween(from = from, to = to) * EARTH_RADIUS

        fun computeLength(path: List<LatLng>): Double {
            if (path.size < 2) {
                return 0.0
            }
            var length = 0.0
            val prev = path[0]
            var prevLat = prev.latitude.toRadians()
            var prevLng = prev.longitude.toRadians()
            for (point in path) {
                val lat = point.latitude.toRadians()
                val lng = point.longitude.toRadians()
                length += distanceRadians(lat1 = prevLat, lng1 = prevLng, lat2 = lat, lng2 = lng)
                prevLat = lat
                prevLng = lng
            }
            return length * EARTH_RADIUS
        }

        fun computeArea(path: List<LatLng>): Double = abs(computeSignedArea(path))

        fun computeSignedArea(path: List<LatLng>, radius: Double = EARTH_RADIUS): Double {
            val size = path.size
            if (size < 3) {
                return 0.0
            }
            var total = 0.0
            val prev = path[size - 1]
            var prevTanLat = tan((PI / 2.0 - prev.latitude.toRadians()) / 2.0)
            var prevLng = prev.longitude.toRadians()
            // For each edge, accumulate the signed area of the triangle formed by the North Pole
            // and that edge ("polar triangle").
            for (point in path) {
                val tanLat = tan((PI / 2.0 - point.latitude.toRadians()) / 2.0)
                val lng = point.longitude.toRadians()
                total += polarTriangleArea(
                    tan1 = tanLat,
                    lng1 = lng,
                    tan2 = prevTanLat,
                    lng2 = prevLng
                )
                prevTanLat = tanLat
                prevLng = lng
            }
            return total * (radius * radius)
        }

        /**
         * Returns the signed area of a triangle which has North Pole as a vertex.
         * Formula derived from "Area of a spherical triangle given two edges and the included angle"
         * as per "Spherical Trigonometry" by Todhunter, page 71, section 103, point 2.
         * See http://books.google.com/books?id=3uBHAAAAIAAJ&pg=PA71
         * The arguments named "tan" are tan((pi/2 - latitude)/2).
         */
        fun polarTriangleArea(tan1: Double, lng1: Double, tan2: Double, lng2: Double): Double =
            (lng1 - lng2).let { _deltaLng ->
                (tan1 * tan2).let { _t ->
                    2.0 * atan2(_t * sin(_deltaLng), 1.0 + _t * cos(_deltaLng))
                }
            }
    }
}
