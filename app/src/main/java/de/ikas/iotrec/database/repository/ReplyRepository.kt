package de.ikas.iotrec.database.repository

import androidx.annotation.WorkerThread
import de.ikas.iotrec.database.dao.ReplyDao
import de.ikas.iotrec.database.model.Reply
import de.ikas.iotrec.network.IotRecApiInit
import retrofit2.Response

class ReplyRepository(private val iotRecApi: IotRecApiInit, private val replyDao: ReplyDao) {

    @WorkerThread
    suspend fun insert(reply: Reply) {
        replyDao.insert(reply)
    }

    @WorkerThread
    fun deleteAll() {
        replyDao.deleteAll()
    }

    @WorkerThread
    suspend fun sendReply(experimentId: Int, reply: Reply): Response<Reply> {
        replyDao.insert(reply)
        return iotRecApi.createReply(experimentId, reply)
    }
}