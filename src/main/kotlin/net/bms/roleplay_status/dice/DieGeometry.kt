package net.bms.roleplay_status.dice

import kotlin.math.*

/** Convex tabletop dice. d10 is the dual of a pentagonal antiprism; d12 the dual of an icosahedron. */
object DieGeometry {
    data class V(val x: Double, val y: Double, val z: Double) {
        operator fun plus(b: V) = V(x+b.x,y+b.y,z+b.z)
        operator fun minus(b: V) = V(x-b.x,y-b.y,z-b.z)
        operator fun times(k: Double) = V(x*k,y*k,z*k)
        fun dot(b: V) = x*b.x+y*b.y+z*b.z
        fun cross(b: V) = V(y*b.z-z*b.y,z*b.x-x*b.z,x*b.y-y*b.x)
        fun unit(): V = this * (1.0 / sqrt(dot(this)))
    }
    data class Mesh(val vertices: List<V>, val faces: List<List<Int>>)
    private fun hull(raw: List<V>): Mesh {
        val scale = raw.maxOf { sqrt(it.dot(it)) }
        val v = raw.map { it * (1.0 / scale) }
        val faces = linkedSetOf<List<Int>>()
        for (a in v.indices) for(b in a+1 until v.size) for(c in b+1 until v.size) {
            val cross = (v[b]-v[a]).cross(v[c]-v[a])
            if (cross.dot(cross) < 1e-12) continue
            var n = cross.unit()
            val d = n.dot(v[a])
            val distances = v.map { n.dot(it)-d }
            if (distances.any { it > 1e-7 } && distances.any { it < -1e-7 }) continue
            val indices = v.indices.filter { abs(distances[it]) < 1e-7 }
            faces.add(indices.sorted())
        }
        return Mesh(v, faces.map { ids ->
            val center = ids.map { v[it] }.reduce(V::plus) * (1.0/ids.size)
            val n = center.unit(); val u = (v[ids[0]]-center).unit(); val w = n.cross(u)
            ids.sortedBy { atan2((v[it]-center).dot(w),(v[it]-center).dot(u)) }
        })
    }
    private fun dual(mesh: Mesh): Mesh = hull(mesh.faces.map { ids -> val a = mesh.vertices[ids[0]]; val n = (mesh.vertices[ids[1]]-a).cross(mesh.vertices[ids[2]]-a).unit(); n * (1.0/n.dot(a)) })
    private val ico: Mesh by lazy {
        val p = (1.0+sqrt(5.0))/2
        hull(listOf(-1.0,1.0).flatMap { a -> listOf(-p,p).flatMap { b -> listOf(V(0.0,a,b),V(a,b,0.0),V(b,0.0,a)) } })
    }
    private val cache = HashMap<Int,Mesh>()
    @JvmStatic fun mesh(sides: Int): Mesh = cache.getOrPut(sides) {
        when(sides) {
            2 -> hull((0..1).flatMap { side -> (0 until 20).map { i -> val a=i*PI/10; V(cos(a),sin(a),if(side==0) -.15 else .15) } })
            4 -> hull(listOf(V(1.0,1.0,1.0),V(-1.0,-1.0,1.0),V(-1.0,1.0,-1.0),V(1.0,-1.0,-1.0)))
            6 -> hull(listOf(-1.0,1.0).flatMap { x -> listOf(-1.0,1.0).flatMap { y -> listOf(-1.0,1.0).map { z -> V(x,y,z) } } })
            8 -> hull(listOf(V(1.0,0.0,0.0),V(-1.0,0.0,0.0),V(0.0,1.0,0.0),V(0.0,-1.0,0.0),V(0.0,0.0,1.0),V(0.0,0.0,-1.0)))
            10 -> dual(hull((0..1).flatMap { layer -> (0 until 5).map { i -> val a=(i*2+layer)*PI/5; V(cos(a),sin(a),if(layer==0) -.55 else .55) } }))
            12 -> dual(ico)
            20 -> ico
            else -> error("Unsupported die: $sides")
        }
    }
    data class Face(val points: List<Pair<Double,Double>>, val shade: Double, val depth: Double)
    @JvmStatic fun project(sides: Int, spin: Double): List<Face> {
        val original = mesh(sides)
        val mesh = if(sides==2) original.copy(faces=original.faces.sortedByDescending { it.size }) else original
        val first = mesh.faces.first().map { mesh.vertices[it] }.reduce(V::plus).unit()
        val u = (mesh.vertices[mesh.faces.first()[0]]-first*first.dot(mesh.vertices[mesh.faces.first()[0]])).unit()
        val w = first.cross(u)
        val ax = spin*.83; val ay=spin
        val rotated = mesh.vertices.map { v ->
            val aligned = V(v.dot(u),v.dot(w),v.dot(first))
            val x = aligned.x*cos(ay)+aligned.z*sin(ay); val z= -aligned.x*sin(ay)+aligned.z*cos(ay)
            V(x, aligned.y*cos(ax)-z*sin(ax), aligned.y*sin(ax)+z*cos(ax))
        }
        return mesh.faces.mapNotNull { ids ->
            val points=ids.map { rotated[it] }
            val n=(points[1]-points[0]).cross(points[2]-points[0]).unit()
            if(n.z <= 0.001) null else Face(points.map { val perspective=3.5/(3.5-it.z); Pair(it.x*perspective,-it.y*perspective) }, .35+.65*n.z,points.sumOf { it.z }/points.size)
        }.sortedBy { it.depth }
    }
}
