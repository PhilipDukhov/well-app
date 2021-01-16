package com.well.sharedMobile.puerh.call

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DataChannelChunkManagerTest {
    private val dataChannelChunkManager = DataChannelChunkManager()

    @Test
    fun intConversations() {
        val randomList = List(10) { Random.nextInt(2 shl (it + 4)) }
        randomList
            .forEach {
                val byteArray = it.toByteArray()
                assertEquals(
                    byteArray.readInt(0),
                    it,
                    "$it -> ${byteArray.toHexString()}"
                )
            }
        val byteArray = randomList
            .map(Int::toByteArray)
            .reduce { res, array -> res + array }
        assertEquals(
            byteArray.readInts(randomList.count()),
            randomList,
            "$randomList -> ${byteArray.toHexString()}"
        )
    }

    @Test
    fun dataChunk() {
        val randomList = List(5) { Random.nextInt(2 shl (it + 4)) }
        val iterator = randomList.iterator()
        val inputChunk = DataChannelChunkManager.DataChannelMessageChunk(
            iterator.next(),
            iterator.next(),
            iterator.next(),
            iterator.next(),
            iterator.next(),
            Random.nextBytes(100)
        )
        val outputChunk = DataChannelChunkManager.DataChannelMessageChunk(
            inputChunk.toByteArray()
        )
        assertEquals(inputChunk.id, outputChunk.id)
        assertEquals(inputChunk.chunkIndex, outputChunk.chunkIndex)
        assertEquals(inputChunk.chunkSize, outputChunk.chunkSize)
        assertEquals(inputChunk.chunksCount, outputChunk.chunksCount)
        assertEquals(inputChunk.msgSize, outputChunk.msgSize)
        assertByteArrayEquals(inputChunk.byteArray, outputChunk.byteArray)
    }

    @Test
    fun testChunks() {
        listOf(
            63001,
            83001,
            630123,
            10,
            100,
            1000,
        ).forEach { count ->
            val randomBytes = Random.nextBytes(count)
            val chunks = dataChannelChunkManager.splitByteArrayIntoChunks(randomBytes)
            chunks
                .shuffled()
                .withIndex()
                .forEach {
                    val res = dataChannelChunkManager.processByteArrayChunk(it.value)
                    if (it.index < chunks.lastIndex) {
                        assertNull(res, "$count ${it.index} ${chunks.lastIndex}")
                    } else {
                        randomBytes.zip(res!!).all { it.first == it.second }
                    }
                }
        }
    }
}