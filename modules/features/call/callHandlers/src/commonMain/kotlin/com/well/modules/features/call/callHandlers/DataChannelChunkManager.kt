package com.well.modules.features.call.callHandlers

import com.well.modules.atomic.AtomicMutableMap
import com.well.modules.atomic.AtomicRef
import com.well.modules.utils.kotlinUtils.map
import com.well.modules.utils.kotlinUtils.mapSecond
import kotlin.math.min

private typealias ChunkId = Int

class DataChannelChunkManager {
    private var dataMsgId by AtomicRef(0)
    private val incompleteMessages = AtomicMutableMap<ChunkId, Pair<Set<Int>, ByteArray>>()
    private val maxChunkByteSize = 63000

    fun splitByteArrayIntoChunks(byteArray: ByteArray): List<ByteArray> {
        val msgSize = byteArray.count()
        val chunksCount = ((msgSize - 1) / maxChunkByteSize) + 1
        val currentMsgId = dataMsgId.inc()
        return (0 until chunksCount)
            .map { i ->
                DataChannelMessageChunk(
                    id = currentMsgId,
                    msgSize = msgSize,
                    chunksCount = chunksCount,
                    chunkSize = maxChunkByteSize,
                    chunkIndex = i,
                    byteArray = byteArray.copyOfRange(
                        maxChunkByteSize * i,
                        min(maxChunkByteSize * (i + 1), byteArray.count()),
                    )
                ).toByteArray()
            }
    }

    fun processByteArrayChunk(chunkByteArray: ByteArray): ByteArray? =
        DataChannelMessageChunk(chunkByteArray).run {
            if (chunksCount > 1) {
                val (neededIds, fullByteArray) = incompleteMessages[id]
                    ?.mapSecond { it.copyOf() }
                    ?: (0 until chunksCount).toSet() to ByteArray(msgSize)
                byteArray.copyInto(
                    fullByteArray,
                    chunkIndex * chunkSize,
                )
                val leftIds = neededIds - chunkIndex
                if (leftIds.isNotEmpty()) {
                    incompleteMessages[id] = leftIds to fullByteArray
                    null
                } else {
                    incompleteMessages.remove(id)
                    fullByteArray
                }
            } else {
                byteArray
            }
        }

    internal class DataChannelMessageChunk {
        val id: ChunkId
        val msgSize: Int
        val chunksCount: Int
        val chunkSize: Int
        val chunkIndex: Int
        val byteArray: ByteArray

        constructor(
            id: Int,
            msgSize: Int,
            chunksCount: Int,
            chunkSize: Int,
            chunkIndex: Int,
            byteArray: ByteArray,
        ) {
            this.id = id
            this.msgSize = msgSize
            this.chunksCount = chunksCount
            this.chunkSize = chunkSize
            this.chunkIndex = chunkIndex
            this.byteArray = byteArray
        }

        constructor(byteArray: ByteArray) {
            val intsCount = 5
            val ints = byteArray.readInts(intsCount).iterator()
            id = ints.next()
            msgSize = ints.next()
            chunksCount = ints.next()
            chunkSize = ints.next()
            chunkIndex = ints.next()
            if (ints.hasNext()) {
                throw IllegalStateException("DataChannelMessageChunk extra int")
            }
            this.byteArray = byteArray.copyOfRange(
                Int.SIZE_BYTES * intsCount,
                byteArray.count()
            )
        }

        fun toByteArray(): ByteArray =
            id.toByteArray() +
                msgSize.toByteArray() +
                chunksCount.toByteArray() +
                chunkSize.toByteArray() +
                chunkIndex.toByteArray() +
                byteArray
    }
}

internal fun ByteArray.readInt(index: Int = 0): Int =
    (0 until Int.SIZE_BYTES).sumOf { i ->
        (this[index + i].toUByte().toInt()) shl (i * Byte.SIZE_BITS)
    }

internal fun ByteArray.readInts(
    count: Int,
    index: Int = 0,
): List<Int> = (index until count + index).map {
    readInt(it * Int.SIZE_BYTES)
}

internal fun Int.toByteArray() = ByteArray(Int.SIZE_BYTES) {
    (this ushr (it * Byte.SIZE_BITS) and 0xff).toByte()
}