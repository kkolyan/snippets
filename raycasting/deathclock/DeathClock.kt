package game.visibility

import game.misc.data.Vector3Int
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.atan2
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt
import kotlin.math.tan

object DeathClock {
    fun calculate(
        source: Vector3Int,
        obstacles: Iterable<Vector3Int>,
        interestingPoints: Iterable<Vector3Int>,
        distance: Int
    ): MutableMap<Vector3Int, Float> {
        val visibilities = mutableMapOf<Vector3Int, Float>()
        merge(Sector.Phi, source, obstacles, interestingPoints, distance).forEach { visibilities[it.original] = it.diameter }
        merge(Sector.Theta, source, obstacles, interestingPoints, distance).forEach {
            visibilities[it.original] = 1f - (1f - visibilities[it.original]!!) * (1f - it.diameter)
        }
        return visibilities
    }

    fun merge(
        plane: Int,
        source: Vector3Int,
        obstacles: Iterable<Vector3Int>,
        interestingPoints: Iterable<Vector3Int>,
        distance: Int
    ): List<Sector> {
        fun inRange(point: Vector3Int): Boolean {
            return (point - source).magnitude < distance
        }
        val nodes = (
            interestingPoints.filter(::inRange).map { createSector(source, it, plane, false) }
                + obstacles.filter(::inRange).map { createSector(source, it, plane, true) }
            ).sortedBy { it.a0 }
        var i = 0
        var j = i + 1
        while (true) {
            if (i >= nodes.size) {
                break
            }
            val aLeft = nodes[i].a1
            val aRight: Float
            val rRight: Float
            val obstacleRight: Boolean
            if (j < nodes.size) {
                rRight = nodes[j].r
                aRight = nodes[j].a0
                obstacleRight = nodes[j].obstacle
            } else {
                rRight = nodes[j - nodes.size].r
                aRight = nodes[j - nodes.size].a0
                obstacleRight = nodes[j - nodes.size].obstacle
            }

            val overlap = aLeft - aRight
            if (overlap > 0 && nodes[i].diameter > 0.001) {
                if (nodes[i].r > rRight) {
                    if (obstacleRight) {
                        nodes[i].a1 = aRight
                    }
                } else {
                    if (nodes[i].obstacle) {
                        if (j < nodes.size) {
                            nodes[j].a0 = aLeft
                        } else {
                            nodes[j - nodes.size].a0 = (aLeft + PI * 2).toFloat()
                        }
                    }
                }
                j++
            } else {
                i++
                j = i + 1
            }
        }
        return nodes
    }

    fun createSector(source: Vector3Int, target: Vector3Int, plane: Int, obstacle: Boolean): Sector {
        val dx = (target.x - source.x).toFloat()
        val dy = (target.y - source.y).toFloat()
        val dz = (target.z - source.z).toFloat()
        val r = sqrt(dx * dx + dy * dy + dz * dz)
        val halfBeta = atan2(1f, r)
        val a = when (plane) {
            Sector.Phi -> atan2(dy, dx)
            Sector.Theta -> acos(dz / r)
            else -> throw IllegalStateException()
        }
        return Sector(
            original = target,
            a0 = a - halfBeta,
            a1 = a + halfBeta,
            r = r,
            obstacle = obstacle
        )
    }

    class Sector(
        val original: Vector3Int,
        a0: Float,//angular begin
        a1: Float,//angular end
        val r: Float,
        val obstacle: Boolean
    ) {

        var a0 = a0
            set(value) {
                field = min(field, value)
            }

        var a1 = a1
            set(value) {
                field = max(a0, value)
            }
        val diameter: Float get() = 2 * r * tan((a1 - a0) / 2)

        companion object {
            const val Phi = 0
            const val Theta = 1
        }
    }
}
