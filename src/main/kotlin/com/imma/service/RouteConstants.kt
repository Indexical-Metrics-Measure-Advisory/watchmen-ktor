package com.imma.service

object RouteConstants {
    const val LOGIN: String = "/login/access-token"

    const val USER_SAVE: String = "/user"
    const val USER_FIND_BY_ID: String = "/user"
    const val USER_LIST_BY_NAME: String = "/user/name"
    const val USER_LIST_BY_NAME_FOR_HOLDER: String = "/query/user/group"
    const val USER_LIST_BY_IDS_FOR_HOLDER: String = "/user/ids"

    const val USER_GROUP_SAVE: String = "/user_group"
    const val USER_GROUP_FIND_BY_ID: String = "/user_group"
    const val USER_GROUP_LIST_BY_NAME: String = "/user_group/name"
    const val USER_GROUP_LIST_BY_NAME_FOR_HOLDER: String = "/query/user_group/space"
    const val USER_GROUP_LIST_BY_IDS_FOR_HOLDER: String = "/user_groups/ids"
}