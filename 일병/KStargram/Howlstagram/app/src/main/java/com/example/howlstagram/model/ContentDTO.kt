package com.example.howlstagram.model

import java.sql.Timestamp

data class ContentDTO(
    var explain: String? = null,
    var imagerUrl: String? = null,
    var uid: String? = null,
    var userId: String? = null,
    var timestamp: Long? = null,
    var favoriteCount: Int = 0,
    var favorites: MutableMap<String, Boolean> = HashMap()
) {
    data class Comment(
        var uid: String? = null,
        var userId: String? = null,
        var comment: String? = null,
        var timestamp: Long? = null
    )
}


//사진 설명 이미지 URL ,UID는 고유 번호 userid, 올린시간, 좋아요,좋아요 중복체크
