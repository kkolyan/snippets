package game.visibility

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.readValue
import game.areaeditor.area.state.AreaData
import game.areaeditor.area.state.CharacterObject
import game.areaeditor.area.state.TerrainObject
import game.misc.data.Vector3Int
import game.misc.remote.fromMapOfShit
import game.misc.remote.mapper
import org.apache.commons.lang3.time.StopWatch
import org.junit.Test
import java.io.File
import java.util.IntSummaryStatistics

class DeathClockTest {

    @Test
    fun test1() {
        val areaFile = File("specs/Locations/castle_1.area.yaml")
        val state = mapper.readValue<JsonNode>(areaFile).fromMapOfShit<AreaData>()
        val source = state.objects.filterIsInstance<CharacterObject>().first().place.cells.maxBy { it.y }!!
        val obstacles = state.objects.filterIsInstance<TerrainObject>().flatMap { it.place.cells }.toSet()
        val pareSurface = obstacles.filter { obstacles.contains(Vector3Int(it.x, it.y - 1, it.z)) }

        val resultsSize = IntSummaryStatistics()
        val times = 10000
        repeat(times) {
            val watch = StopWatch.createStarted()
            val results = DeathClock.calculate(source, obstacles, pareSurface, 50)
//            println(watch.time)
            resultsSize.accept(results.size)
        }
        println(resultsSize.average)
    }
}
